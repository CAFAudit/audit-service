package com.hpe.caf.services.audit.api;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.auditing.schema.AuditedApplication;
import com.hpe.caf.auditing.schema.AuditEvent;
import com.hpe.caf.auditing.schema.AuditEventParam;
import com.hpe.caf.auditing.schema.AuditEventParamType;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * ApplicationAddPost is responsible for parsing the audit events XML file and either creating a new database table
 * if it does not already exist or add new columns to support audit event metadata missing from the existing table.
 */
public class ApplicationAddPost {

    private static final String TRANSFORM_XSD_FILEPATH = "schema/AuditedApplication.xsd";
    private static final String TRANSFORM_TEMPLATE_NAME = "AuditTransform.vm";
    private static final String CUSTOM_EVENT_PARAM_PREFIX = "eventParam";

    private static final String ERR_MSG_XML_NOT_VALID = "The audit events XML configuration file does not conform to the schema.";
    private static final String ERR_MSG_XML_READ_FAILURE = "Failed to bind the XML audit events file.";
    private static final String ERR_MSG_XML_APPID_VALUE_MISSING = "The application identifier has not been supplied in the XML audit events file.";
    private static final String ERR_MSG_DB_CONNECTION_PROPS_MISSING = "One or more Vertica database connection properties have not been provided.";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationAddPost.class);

    public static void addApplication(InputStream auditXMLConfig) throws Exception {
        try
        {
            LOG.info("Starting...");

            //  Read in app config properties.
            AppConfig properties;
            try {
                properties = getAppConfigProperties();

                //  Make sure DB connection properties have been specified.
                if (properties.getDatabaseURL() == null ||
                        properties.getDatabaseSchema() == null ||
                        properties.getDatabaseUsername() == null ||
                        properties.getDatabasePassword() == null) {
                    throw new BadRequestException(ERR_MSG_DB_CONNECTION_PROPS_MISSING);
                }
            } catch (NullPointerException npe) {
                throw new BadRequestException(ERR_MSG_DB_CONNECTION_PROPS_MISSING);
            }

            //  InputStream will need read multiple times, so convert to byte array first.
            byte[] auditXMLConfigBytes = IOUtils.toByteArray(auditXMLConfig);

            //  Check validity of XML and throw error if invalid.
            ByteArrayInputStream is = new ByteArrayInputStream(auditXMLConfigBytes);
            LOG.info("Checking validity of XML audit events file...");
            boolean isValid = isXMLValid(is,TRANSFORM_XSD_FILEPATH);
            if (!isValid) {
                throw new BadRequestException(ERR_MSG_XML_NOT_VALID);
            }

            //  Read the application event data xml file - XML/Java binding.
            AuditedApplication auditAppData;
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(auditXMLConfigBytes);
                auditAppData = readAuditAppDataXmlFile(bais);
            } catch (JAXBException e) {
                LOG.error(ERR_MSG_XML_READ_FAILURE);
                throw new Exception(ERR_MSG_XML_READ_FAILURE);
            }

            //  Get ApplicationId from the application event data object. This will identify the
            //  name of the database table to create or modify.
            LOG.debug("Getting ApplicationId from Audit events XML file...");
            String tableName = auditAppData.getApplicationId();
            if (isNotNullOrEmpty(tableName)) {
                //  Check if table already exists.
                LOG.debug("Checking if the database table already exists...");
                DatabaseHelper databaseHelper = new DatabaseHelper(properties);
                String dbSchema = properties.getDatabaseSchema();
                boolean tableExists = databaseHelper.doesTableExist(dbSchema,tableName);

                //  If table does not exist, generate CREATE TABLE statement and execute, otherwise
                //  identify missing columns and modify existing table through ALTER TABLE ADD COLUMN statements.
                if (!tableExists){
                    LOG.debug("Table does not exist - creating new database table...");
                    ByteArrayInputStream bais = new ByteArrayInputStream(auditXMLConfigBytes);
                    CreateDatabaseSchema(bais, databaseHelper);
                    LOG.info("New database table {} created...", dbSchema + "." + tableName);
                }
                else {
                    LOG.debug("Table already exists - modifying existing one...");
                    boolean tableModified = ModifyDatabaseSchema(auditAppData, databaseHelper, dbSchema, tableName);
                    if (tableModified) {
                        LOG.info("Table {} modified...", dbSchema + "." + tableName);
                    }
                    else {
                        LOG.info("Table schema for {} is up to date...", dbSchema + "." + tableName);
                    }
                }
            }
            else {
                LOG.debug("ApplicationId not found. Nothing to be done therefore...");
                throw new BadRequestException(ERR_MSG_XML_APPID_VALUE_MISSING);
            }

            LOG.info("Complete.");
        }
        catch( Exception e ) {
            LOG.error("Error - {}", e.toString());
            throw e;
        }
    }

    /**
     * Perform Audit events XML transform to 'CREATE TABLE' SQL statement and execute.
     */
    private static void CreateDatabaseSchema(InputStream auditXMLConfig, DatabaseHelper databaseHelper) throws Exception {

        //  Perform XML to 'CREATE TABLE' SQL transformation.
        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doCreateTableTransform(auditXMLConfig,TRANSFORM_TEMPLATE_NAME);

        // Connect to database and create the new table to store the audit events' data.
        databaseHelper.createTable(createTableSQL);
    }

    /**
     * Generate sql statements for column addition to the specified table and execute.
     */
    private static boolean ModifyDatabaseSchema(AuditedApplication auditApplicationData, DatabaseHelper databaseHelper, String schema, String tableName) throws Exception {

        boolean tableModified = false;

        //  Identify all audit event parameters.
        List<AuditEvent> eventList = auditApplicationData.getAuditEvents().getAuditEvent();

        ArrayList<AuditEventParam> paramsList = new ArrayList<>();
        for (AuditEvent anEventList : eventList) {
            List<AuditEventParam> params = anEventList.getParams().getParam();
            paramsList.addAll(params);
        }

        //  Iterate through each parameter name and identify those missing from the
        //  database table. Then modify the table to support the new parameter event data.
        for (AuditEventParam aep : paramsList) {
            String columnName = CUSTOM_EVENT_PARAM_PREFIX + (isNotNullOrEmpty(aep.getColumnName()) ? aep.getColumnName() : aep.getName());

            // Check if column exists.
            LOG.debug("ModifyDatabaseSchema: checking if column {} exists...", columnName);
            boolean columnExists = databaseHelper.doesColumnExist(schema, tableName, columnName);

            if (!columnExists) {
                //  Get event param type.
                AuditEventParamType paramType = aep.getType();

                //  Generate 'ALTER TABLE ADD COLUMN' statement.
                TransformHelper transform = new TransformHelper();
                String addColumnSQL = transform.doModifyTableTransform(TRANSFORM_TEMPLATE_NAME, schema, tableName, columnName, paramType.value());

                //  Execute SQL.
                LOG.debug("ModifyDatabaseSchema: Column {} does not exist...", columnName);
                databaseHelper.addColumn(addColumnSQL);
                LOG.debug("ModifyDatabaseSchema: New column {} added ...", columnName);
                tableModified = true;
            } else {
                LOG.debug("ModifyDatabaseSchema: Column {} already exists...", columnName);
            }
        }

        return tableModified;
    }

    /**
     * Load required inputs from config.properties or environment variables.
     */
    private static AppConfig getAppConfigProperties() {
        AnnotationConfigApplicationContext propertiesApplicationContext = new AnnotationConfigApplicationContext();
        propertiesApplicationContext.register(PropertySourcesPlaceholderConfigurer.class);
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(AppConfig.class);
        propertiesApplicationContext.registerBeanDefinition("AppConfig", beanDefinition);
        propertiesApplicationContext.refresh();
        return propertiesApplicationContext.getBean(AppConfig.class);
    }

    /**
     * Reads the specified xml file into the AuditedApplication object hierarchy.
     */
    private static AuditedApplication readAuditAppDataXmlFile(InputStream xmlFile) throws JAXBException {

        final JAXBContext jaxbContext = JAXBContext.newInstance(AuditedApplication.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return (AuditedApplication)unmarshaller.unmarshal(xmlFile);
    }

    /**
     * Performs validation check on the specified xml input stream.
     */
    private static boolean isXMLValid (InputStream xmlInputStream, String xsdName) {

        boolean isValid = false;

        try(
                InputStream xsdInputStream = ApplicationAddPost.class.getClassLoader().getResourceAsStream(xsdName)
        ) {
            SchemaFactory factory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdInputStream));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlInputStream));
            isValid = true;
        }
        catch( Exception e ) {
            LOG.error("Error - {}", e.toString());
        }

        return isValid;
    }

    private static boolean isNotNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }
}
