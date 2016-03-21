package com.hpe.caf.services.audit.api;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * TenantAddPost is responsible for registering tenants with applications defined audit event XML.
 */
public class TenantAddPost {

    private static final String ERR_MSG_TENANT_APPS_IS_MISSING = "The AuditManagement.TenantApplications table does not exist.";
    private static final String TRANSFORM_TEMPLATE_NAME = "AuditTransform.vm";
    private static final String KAFKA_SCHEDULER_NAME_SUFFIX = "AuditScheduler";
    private static final String KAFKA_TARGET_TABLE_PREFIX = "Audit";
    private static final String KAFKA_REJECT_TABLE = "kafka_rej";
    private static final String KAFKA_TARGET_TOPIC_PREFIX = "AuditEventTopic.";

    private static final Logger LOG = LoggerFactory.getLogger(TenantAddPost.class);

    public static void addTenant(String tenantId, List<String> applications) throws Exception {
        try
        {
            LOG.info("addTenant: Starting...");

            //  Get app config settings.
            LOG.debug("addTenant: Reading database connection properties...");
            AppConfig properties = ApiServiceUtil.getAppConfigProperties();

            //  Get database helper instance.
            DatabaseHelper databaseHelper = new DatabaseHelper(properties);

            //  Make sure the AuditManagement.TenantApplications table exists before we continue.
            boolean tableExists = databaseHelper.doesTableExist("AuditManagement","TenantApplications");
            if (!tableExists) {
                LOG.error("addTenant: Error - '{}'", ERR_MSG_TENANT_APPS_IS_MISSING);
                throw new Exception(ERR_MSG_TENANT_APPS_IS_MISSING);
            }

            //  Iterate through each application, add the necessary tenant/application mapping and tenant
            //  related schema and auditing table.
            for (String application : applications) {

                //  Need to make sure that audit events XML has been registered for this application.
                LOG.debug("addTenant: Checking audit events XML has been registered for application '{}'...",application);
                String auditConfigXMLString = databaseHelper.getEventsXMLForApp(application);
                if (!Objects.equals("", auditConfigXMLString)) {

                    //  Audit events XML for this application has been found. Now check if tenantId/application
                    //  mapping already exists in AuditManagement.TenantApplications.
                    LOG.debug("addTenant: Checking if AuditManagement.TenantApplications row exists for tenant '{}', application '{}'...", tenantId, application);
                    boolean rowExists = databaseHelper.doesTenantApplicationsRowExist(tenantId,application);
                    if (!rowExists){

                        LOG.info("addTenant: Starting database changes for tenant '{}', application '{}'...", tenantId, application);

                        //  Row does not yet exists, so create entry in AuditManagement.TenantApplications for
                        //  tenantId/application.
                        LOG.debug("addTenant: Creating new row in AuditManagement.TenantApplications for tenant '{}', application '{}'...", tenantId, application);
                        databaseHelper.insertTenantApplicationsRow(tenantId,application);

                        try {
                            //  Create new schema for the specified tenant and grant usage to the audit reader role.
                            LOG.debug("addTenant: Creating new database schema for tenant '{}'...", tenantId);
                            databaseHelper.createSchema(tenantId);
                            databaseHelper.grantUsageOnSchema(tenantId, properties.getDatabaseReaderRole());

                            InputStream auditConfigXMLStream = new ByteArrayInputStream(auditConfigXMLString.getBytes(StandardCharsets.UTF_8));

                            //  Create <tenantId>.Audit<application> table based on the audit events XML.
                            LOG.debug("addTenant: Creating new auditing table for tenant '{}', application '{}'...", tenantId, application);
                            TransformHelper transform = new TransformHelper();
                            String createTableSQL = transform.doCreateTableTransform(auditConfigXMLStream,TRANSFORM_TEMPLATE_NAME,tenantId);
                            databaseHelper.createTable(createTableSQL);

                            //  Grant SELECT on the new table to the audit reader role.
                            String tableName = StringUtils.substringBetween(createTableSQL, "CREATE TABLE IF NOT EXISTS ", "(");
                            databaseHelper.grantSelectOnTable(tableName, properties.getDatabaseReaderRole());

                            LOG.info("addTenant: Database changes complete for tenant '{}', application '{}'...", tenantId, application);

                            //  Create Kafka scheduler (per tenant) and associate a topic with that scheduler.
                            String schedulerName = tenantId + KAFKA_SCHEDULER_NAME_SUFFIX;

                            //  If the specified scheduler name already exists, then ignore creation step.
                            LOG.debug("addTenant: Checking if Vertica scheduler '{}' has already been created...", schedulerName);
                            boolean schemaExists = databaseHelper.doesSchemaExist(schedulerName);
                            if (!schemaExists) {
                                LOG.info("addTenant: Creating Kafka scheduler...");
                                KafkaScheduler.createScheduler(properties, schedulerName);

                                LOG.info("addTenant: Launching Kafka scheduler...");
                                KafkaScheduler.launchScheduler(properties, tenantId, schedulerName);
                            }

                            String targetTable = new StringBuilder()
                                    .append(tenantId)
                                    .append(".")
                                    .append(KAFKA_TARGET_TABLE_PREFIX)
                                    .append(application)
                                    .toString();

                            String rejectionTable = new StringBuilder()
                                    .append(tenantId)
                                    .append(".")
                                    .append(KAFKA_REJECT_TABLE)
                                    .toString();

                            String targetTopic = new StringBuilder()
                                    .append(KAFKA_TARGET_TOPIC_PREFIX)
                                    .append(application)
                                    .append(".")
                                    .append(tenantId)
                                    .toString();

                            LOG.info("addTenant: Associating Kafka topic with the scheduler...");
                            KafkaScheduler.associateTopic(properties, schedulerName, targetTable, rejectionTable, targetTopic);
  
                        } catch (Exception ex) {
                            //  Something unexpected has gone wrong. Delete tenant/application mapping to facilitate retry,
                            databaseHelper.deleteTenantApplicationsRow(tenantId,application);
                            throw ex;
                        }
                    }
                }
            }

            LOG.info("addTenant: Done.");
        }
        catch( Exception e ) {
            LOG.error("Error - '{}'", e.toString());
            throw e;
        }
    }
}
