package com.hpe.caf.services.audit.api;

import com.hpe.caf.auditing.schema.AuditEvent;
import com.hpe.caf.auditing.schema.AuditEventParam;
import com.hpe.caf.auditing.schema.AuditEventParamType;
import com.hpe.caf.services.audit.api.exceptions.BadRequestException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpe.caf.auditing.schema.AuditedApplication;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ApplicationAddPost is responsible for registering application defined audit event XML.
 */
public class ApplicationAddPost {

    private static final String AUDITED_APPLICATION_XSD_FILEPATH = "schema/AuditedApplication.xsd";
    private static final String ERR_MSG_XML_NOT_VALID = "The audit events XML configuration file does not conform to the schema.";
    private static final String ERR_MSG_XML_APPID_VALUE_MISSING = "The application identifier has not been supplied in the XML audit events file.";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationAddPost.class);

    public static void addApplication(InputStream auditXMLConfig) throws Exception {
        try
        {
            //  Get app config settings.
            AppConfig properties = ApiServiceUtil.getAppConfigProperties();

            //  Only proceed if audit management web service has not been disabled.
            if (properties.getCAFAuditManagementDisable() == null ||
                    (properties.getCAFAuditManagementDisable() != null &&
                            properties.getCAFAuditManagementDisable().toUpperCase().equals("FALSE"))) {

                LOG.info("addApplication: Starting...");

                //  InputStream will need read multiple times, so convert to byte array first.
                byte[] auditXMLConfigBytes = IOUtils.toByteArray(auditXMLConfig);

                //  Check validity of XML and throw error if invalid.
                ByteArrayInputStream is = new ByteArrayInputStream(auditXMLConfigBytes);
                LOG.debug("addApplication: Checking validity of audit events XML...");
                boolean isValid = isXMLValid(is, AUDITED_APPLICATION_XSD_FILEPATH);
                if (!isValid) {
                    LOG.error("addApplication: Error - '{}'", ERR_MSG_XML_NOT_VALID);
                    throw new BadRequestException(ERR_MSG_XML_NOT_VALID);
                }

                //  Read the application event data xml file - XML/Java binding.
                AuditedApplication auditAppData = ApiServiceUtil.getAuditedApplication(auditXMLConfigBytes);

                //  Get ApplicationId from the application event data object.
                LOG.debug("addApplication: Getting ApplicationId from audit events XML...");
                String application = auditAppData.getApplicationId();
                if (ApiServiceUtil.isNotNullOrEmpty(application)) {
                    LOG.info("addApplication: ApplicationId is '{}'...", application);

                    //  Get database helper instance.
                    DatabaseHelper databaseHelper = new DatabaseHelper(properties);

                    //  Ensure the AuditManagement database schema and tables are in place.
                    LOG.debug("addApplication: Creating Audit Management database schema if necessary...");
                    databaseHelper.createAuditManagementSchema(properties.getDatabaseReaderRole());

                    //  Check if row already exists in AuditManagement.ApplicationEvents for the specified application
                    String auditXMLConfigString = new String(auditXMLConfigBytes);
                    auditXMLConfigString = auditXMLConfigString.replaceAll(">\\s*<", "><").replace("\r\n", "");

                    LOG.debug("addApplication: Checking if AuditManagement.ApplicationEvents row already exists for application '{}'...", application);
                    boolean rowExists = databaseHelper.doesApplicationEventsRowExist(application);
                    if (!rowExists) {
                        LOG.debug("addApplication: Creating new row in AuditManagement.ApplicationEvents for application '{}'...", application);
                        databaseHelper.insertApplicationEventsRow(application, auditXMLConfigString);
                    } else {
                        //  The application has already been registered. So update ApplicationEvents
                        //  table with audit events XML changes.
                        LOG.debug("addApplication: Updating row in AuditManagement.ApplicationEvents for application '{}'...", application);
                        databaseHelper.updateApplicationEventsRow(application, auditXMLConfigString);

                        //  Identify all tenants currently associated with the application.
                        LOG.debug("addApplication: Getting list of tenants for application '{}'...", application);
                        List<String> tenants = databaseHelper.getTenantsForApp(application);

                        //  For each tenant, modify existing table schema if necessary.
                        LOG.debug("addApplication: Modifying tenant auditing tables where necessary...");
                        for (String tenantId : tenants) {
                            String tableName = "Audit" + application;
                            String tenantSchemaName = ApiServiceUtil.TENANTID_SCHEMA_PREFIX + tenantId;
                            boolean tableModified = modifyDatabaseTable(auditAppData,databaseHelper,tenantSchemaName, tableName);
                            if (tableModified) {
                                LOG.debug("addApplication: Table '{}' modified...", tenantSchemaName + "." + tableName);
                            } else {
                                LOG.debug("addApplication: Table schema for '{}' is up to date...", tenantSchemaName + "." + tableName);
                            }
                        }
                    }
                    LOG.info("addApplication: Database changes complete for application '{}'...", application);
                } else {
                    LOG.debug("addApplication: ApplicationId not found. Nothing to be done therefore...");
                    throw new BadRequestException(ERR_MSG_XML_APPID_VALUE_MISSING);
                }

                LOG.info("addApplication: Done.");
            }
        }
        catch( Exception e ) {
            LOG.error("addApplication: Error - {}", e.toString());
            throw e;
        }
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

    /**
     * Generates and executes ALTER TABLE SQL based on missing columns specified in the audited application data xml file.
     */
    private static boolean modifyDatabaseTable(AuditedApplication auditApplicationData, DatabaseHelper databaseHelper, String schemaName, String tableName) throws Exception {

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
            String columnName = ApiServiceUtil.CUSTOM_EVENT_PARAM_PREFIX + (ApiServiceUtil.isNotNullOrEmpty(aep.getColumnName()) ? aep.getColumnName() : aep.getName());

            // Check if column exists.
            LOG.debug("modifyDatabaseTable: checking if column '{}' exists...", columnName);
            boolean columnExists = databaseHelper.doesColumnExist(schemaName, tableName, columnName);

            if (!columnExists) {
                //  Get event param type.
                AuditEventParamType paramType = aep.getType();

                //  Get max length constraint.
                Integer maxLengthConstraint = null;
                if (aep.getConstraints() != null) {
                    maxLengthConstraint = aep.getConstraints().getMaxLength().intValue();
                }

                //  Generate 'ALTER TABLE ADD COLUMN' statement.
                String addColumnSQL = getModifyTableSQL(schemaName, tableName, columnName, paramType.value(), maxLengthConstraint);

                //  Execute SQL.
                LOG.debug("modifyDatabaseTable: Column '{}' does not exist...", columnName);
                databaseHelper.addColumn(addColumnSQL);
                LOG.debug("modifyDatabaseTable: New column '{}' added ...", columnName);
                tableModified = true;
            } else {
                LOG.debug("modifyDatabaseTable: Column '{}' already exists...", columnName);
            }
        }

        return tableModified;
    }

    /**
     * Generates an ALTER TABLE sql statement for column addition.
     */
    private static String getModifyTableSQL(String schemaName, String tableName, String columnName, String columnType, Integer maxLengthConstraint) throws Exception {

        //  Generate 'ALTER TABLE ADD COLUMN' sql statement.
        return "ALTER TABLE " + schemaName + "." + tableName + " ADD COLUMN " + columnName + " " + ApiServiceUtil.getVerticaType(columnType, maxLengthConstraint);
    }

}
