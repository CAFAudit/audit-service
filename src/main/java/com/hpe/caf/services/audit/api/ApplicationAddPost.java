package com.hpe.caf.services.audit.api;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.auditing.schema.AuditedApplication;
import com.hpe.caf.auditing.schema.AuditEvent;
import com.hpe.caf.auditing.schema.AuditEventParam;
import com.hpe.caf.auditing.schema.AuditEventParamType;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * ApplicationAddPost is responsible for registering application defined audit event XML.
 */
public class ApplicationAddPost {

    private static final String TRANSFORM_XSD_FILEPATH = "schema/AuditedApplication.xsd";
    private static final String TRANSFORM_TEMPLATE_NAME = "AuditTransform.vm";
    private static final String CUSTOM_EVENT_PARAM_PREFIX = "eventParam";

    private static final String ERR_MSG_XML_NOT_VALID = "The audit events XML configuration file does not conform to the schema.";
    private static final String ERR_MSG_XML_READ_FAILURE = "Failed to bind the XML audit events file.";
    private static final String ERR_MSG_XML_APPID_VALUE_MISSING = "The application identifier has not been supplied in the XML audit events file.";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationAddPost.class);

    public static void addApplication(InputStream auditXMLConfig) throws Exception {
        try
        {
            LOG.info("addApplication: Starting...");

            //  InputStream will need read multiple times, so convert to byte array first.
            byte[] auditXMLConfigBytes = IOUtils.toByteArray(auditXMLConfig);

            //  Check validity of XML and throw error if invalid.
            ByteArrayInputStream is = new ByteArrayInputStream(auditXMLConfigBytes);
            LOG.debug("addApplication: Checking validity of audit events XML...");
            boolean isValid = isXMLValid(is,TRANSFORM_XSD_FILEPATH);
            if (!isValid) {
                LOG.error("addApplication: Error - '{}'", ERR_MSG_XML_NOT_VALID);
                throw new BadRequestException(ERR_MSG_XML_NOT_VALID);
            }

            //  Read the application event data xml file - XML/Java binding.
            AuditedApplication auditAppData;
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(auditXMLConfigBytes);
                LOG.debug("addApplication: Binding audit events XML to AuditedApplication...");
                auditAppData = JAXBUnmarshal.bindAuditEventsXml(bais);
            } catch (JAXBException e) {
                LOG.error("addApplication: Error - '{}'", ERR_MSG_XML_READ_FAILURE);
                throw new Exception(ERR_MSG_XML_READ_FAILURE);
            }

            //  Get ApplicationId from the application event data object.
            LOG.debug("addApplication: Getting ApplicationId from audit events XML...");
            String application = auditAppData.getApplicationId();
            if (isNotNullOrEmpty(application)) {
                LOG.info("addApplication: ApplicationId is '{}'...",application);

                //  Get app config settings.
                LOG.debug("addApplication: Reading database connection properties...");
                AppConfig properties = ApiServiceUtil.getAppConfigProperties();

                //  Get database helper instance.
                DatabaseHelper databaseHelper = new DatabaseHelper(properties);

                //  Ensure the AuditManagement database schema and tables are in place.
                LOG.debug("addApplication: Creating Audit Management database schema if necessary...");
                databaseHelper.createAuditManagementSchema(properties.getDatabaseReaderAccount());

                //  Check if row already exists in AuditManagement.ApplicationEvents for the specified application
                String auditXMLConfigString = new String(auditXMLConfigBytes);
                auditXMLConfigString = auditXMLConfigString.replaceAll(">\\s*<", "><").replace("\r\n","");

                LOG.debug("addApplication: Checking if AuditManagement.ApplicationEvents row already exists for application '{}'...",application);
                boolean rowExists = databaseHelper.doesApplicationEventsRowExist(application);
                if (!rowExists){
                    LOG.debug("addApplication: Creating new row in AuditManagement.ApplicationEvents for application '{}'...",application);
                    databaseHelper.insertApplicationEventsRow(application,auditXMLConfigString);
                }
                else {

                    //  The application has already been registered. So update ApplicationEvents
                    //  table with audit events XML changes.
                    LOG.debug("addApplication: Updating row in AuditManagement.ApplicationEvents for application '{}'...",application);
                    databaseHelper.updateApplicationEventsRow(application,auditXMLConfigString);

                    //  Identify all tenants currently associated with the application.
                    LOG.debug("addApplication: Getting list of tenants for application '{}'...",application);
                    List<String> tenants = databaseHelper.getTenantsForApp(application);

                    //  For each tenant, modify table schema if necessary.
                    LOG.debug("addApplication: Modifying tenant auditing tables where necessary...");
                    for (String tenantId : tenants) {
                        String tableName = "Audit" + application;
                        boolean tableModified = ModifyDatabaseSchema(auditAppData, databaseHelper, tenantId, tableName);
                        if (tableModified) {
                            LOG.debug("addApplication: Table '{}' modified...", tenantId + "." + tableName);
                        }
                        else {
                            LOG.debug("addApplication: Table schema for '{}' is up to date...", tenantId + "." + tableName);
                        }
                    }
                }
                LOG.info("addApplication: Database changes complete for application '{}'...",application);
            }
            else {
                LOG.debug("addApplication: ApplicationId not found. Nothing to be done therefore...");
                throw new BadRequestException(ERR_MSG_XML_APPID_VALUE_MISSING);
            }

            LOG.info("addApplication: Done.");
        }
        catch( Exception e ) {
            LOG.error("addApplication: Error - {}", e.toString());
            throw e;
        }
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
            LOG.debug("ModifyDatabaseSchema: checking if column '{}' exists...", columnName);
            boolean columnExists = databaseHelper.doesColumnExist(schema, tableName, columnName);

            if (!columnExists) {
                //  Get event param type.
                AuditEventParamType paramType = aep.getType();

                //  Generate 'ALTER TABLE ADD COLUMN' statement.
                TransformHelper transform = new TransformHelper();
                String addColumnSQL = transform.doModifyTableTransform(TRANSFORM_TEMPLATE_NAME, schema, tableName, columnName, paramType.value());

                //  Execute SQL.
                LOG.debug("ModifyDatabaseSchema: Column '{}' does not exist...", columnName);
                databaseHelper.addColumn(addColumnSQL);
                LOG.debug("ModifyDatabaseSchema: New column '{}' added ...", columnName);
                tableModified = true;
            } else {
                LOG.debug("ModifyDatabaseSchema: Column '{}' already exists...", columnName);
            }
        }

        return tableModified;
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
            LOG.error("isXMLValid: Error - '{}'", e.toString());
        }

        return isValid;
    }

    private static boolean isNotNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }
}
