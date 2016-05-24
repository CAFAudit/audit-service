package com.hpe.caf.services.audit.api;

import com.hpe.caf.auditing.schema.AuditedApplication;
import com.hpe.caf.services.audit.api.exceptions.BadRequestException;
import com.hpe.caf.services.audit.api.generated.model.NewTenant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * TenantAddPost is responsible for registering tenants with applications defined audit event XML.
 */
public class TenantAddPost {

    private static final String ERR_MSG_TENANT_APPS_IS_MISSING = "The AuditManagement.TenantApplications table does not exist.";

    private static final Logger LOG = LoggerFactory.getLogger(TenantAddPost.class);

    public static void addTenant(NewTenant newTenant) throws Exception {
        try {
            //  Get app config settings.
            AppConfig properties = ApiServiceUtil.getAppConfigProperties();

            //  Only proceed if audit management web service has not been disabled.
            if (properties.getCAFAuditManagementDisable() == null ||
                    (properties.getCAFAuditManagementDisable() != null &&
                     properties.getCAFAuditManagementDisable().toUpperCase().equals("FALSE"))) {

                LOG.info("addTenant: Starting...");

                String tenantId = newTenant.getTenantId();

                //  Make sure the tenant id does not contain any invalid characters.
                if (containsInvalidCharacters(tenantId)) {
                    LOG.error("addTenant: Error - '{}'", ApiServiceUtil.ERR_MSG_TENANTID_CONTAINS_INVALID_CHARS);
                    throw new BadRequestException(ApiServiceUtil.ERR_MSG_TENANTID_CONTAINS_INVALID_CHARS);
                }

                //  Get database helper instance.
                DatabaseHelper databaseHelper = new DatabaseHelper(properties);

                //  Make sure the AuditManagement.TenantApplications table exists before we continue.
                boolean tableExists = databaseHelper.doesTableExist("AuditManagement", "TenantApplications");
                if (!tableExists) {
                    LOG.error("addTenant: Error - '{}'", ERR_MSG_TENANT_APPS_IS_MISSING);
                    throw new Exception(ERR_MSG_TENANT_APPS_IS_MISSING);
                }

                //  Iterate through each application, add the necessary tenant/application mapping and tenant
                //  related schema and auditing table.
                for (String application : newTenant.getApplication()) {

                    //  Need to make sure that audit events XML has been registered for this application.
                    LOG.debug("addTenant: Checking audit events XML has been registered for application '{}'...", application);
                    String auditConfigXMLString = databaseHelper.getEventsXMLForApp(application);
                    if (!Objects.equals("", auditConfigXMLString)) {

                        //  Audit events XML for this application has been found. Now check if tenantId/application
                        //  mapping already exists in AuditManagement.TenantApplications.
                        LOG.debug("addTenant: Checking if AuditManagement.TenantApplications row exists for tenant '{}', application '{}'...", tenantId, application);
                        boolean rowExists = databaseHelper.doesTenantApplicationsRowExist(tenantId, application);
                        if (!rowExists) {

                            LOG.info("addTenant: Starting database changes for tenant '{}', application '{}'...", tenantId, application);

                            //  Row does not yet exists, so create entry in AuditManagement.TenantApplications for
                            //  tenantId/application.
                            LOG.debug("addTenant: Creating new row in AuditManagement.TenantApplications for tenant '{}', application '{}'...", tenantId, application);
                            databaseHelper.insertTenantApplicationsRow(tenantId, application);

                            try {
                                //  Create new schema for the specified tenant and grant usage to the audit reader role.
                                LOG.debug("addTenant: Creating new database schema for tenant '{}'...", tenantId);
                                String tenantSchemaName = ApiServiceUtil.TENANTID_SCHEMA_PREFIX + tenantId;
                                databaseHelper.createSchema(tenantSchemaName);
                                databaseHelper.grantUsageOnSchema(tenantSchemaName, properties.getDatabaseReaderRole());

                                //  Read the application event data xml file - XML/Java binding.
                                AuditedApplication auditAppData = ApiServiceUtil.getAuditedApplication(auditConfigXMLString.getBytes(StandardCharsets.UTF_8));

                                //  Create <tenantId>.Audit<application> table based on the application event data xml file.
                                LOG.debug("addTenant: Creating new auditing table for tenant '{}', application '{}'...", tenantId, application);
                                XMLToSQLTransform transform = new XMLToSQLTransform(auditAppData);
                                String createTableSQL = transform.getCreateDatabaseTableSQL(tenantSchemaName);
                                databaseHelper.createTable(createTableSQL);

                                //  Grant SELECT on the new table to the audit reader role.
                                String tableName = StringUtils.substringBetween(createTableSQL, "CREATE TABLE IF NOT EXISTS ", "(");
                                databaseHelper.grantSelectOnTable(tableName, properties.getDatabaseReaderRole());

                                LOG.info("addTenant: Database changes complete for tenant '{}', application '{}'...", tenantId, application);

                                //  Create Kafka scheduler (per tenant) and associate a topic with that scheduler.
                                String schedulerName = ApiServiceUtil.KAFKA_SCHEDULER_NAME_PREFIX + tenantId;

                                //  If the specified scheduler name already exists, then ignore creation step.
                                LOG.debug("addTenant: Checking if Vertica scheduler '{}' has already been created...", schedulerName);
                                boolean schemaExists = databaseHelper.doesSchemaExist(schedulerName);
                                if (!schemaExists) {
                                    LOG.info("addTenant: Creating Kafka scheduler...");
                                    KafkaScheduler.createScheduler(properties, schedulerName);

                                    LOG.info("addTenant: Launching Kafka scheduler...");
                                    KafkaScheduler.launchScheduler(properties, tenantId, schedulerName);

                                    // Granting USAGE on new schema to the web service account so they can check existence next time round.
                                    databaseHelper.grantUsageOnAuditSchedulerSchema(schedulerName, properties.getDatabaseServiceAccount());
                                }

                                String targetTable = new StringBuilder()
                                        .append(tenantSchemaName)
                                        .append(".")
                                        .append(ApiServiceUtil.KAFKA_TARGET_TABLE_PREFIX)
                                        .append(application)
                                        .toString();

                                String rejectionTable = new StringBuilder()
                                        .append(tenantSchemaName)
                                        .append(".")
                                        .append(ApiServiceUtil.KAFKA_REJECT_TABLE)
                                        .toString();

                                String targetTopic = new StringBuilder()
                                        .append(ApiServiceUtil.KAFKA_TARGET_TOPIC_PREFIX)
                                        .append(".")
                                        .append(application)
                                        .append(".")
                                        .append(tenantId)
                                        .toString();

                                LOG.info("addTenant: Associating Kafka topic with the scheduler...");
                                KafkaScheduler.associateTopic(properties, schedulerName, targetTable, rejectionTable, targetTopic);

                            } catch (Exception ex) {
                                //  Something unexpected has gone wrong. Delete tenant/application mapping to facilitate retry.
                                databaseHelper.deleteTenantApplicationsRow(tenantId, application);
                                throw ex;
                            }
                        }
                    }
                }

                LOG.info("addTenant: Done.");
            }
        }
        catch(Exception e){
            LOG.error("Error - '{}'", e.toString());
            throw e;
        }
    }

    /**
     * Returns TRUE if the specified tenantId contains invalid characters, otherwise FALSE.
     */
    private static boolean containsInvalidCharacters(String tenantId) {
        if (tenantId.matches(ApiServiceUtil.TENANTID_INVALID_CHARS_REGEX)) {
            return false;
        }
        return true;
    }

}
