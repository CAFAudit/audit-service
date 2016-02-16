package com.hpe.caf.services.audit.api;

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

                        //  Create new schema for the specified tenant.
                        LOG.debug("addTenant: Creating new database schema for tenant '{}'...", tenantId);
                        databaseHelper.createSchema(tenantId);

                        InputStream auditConfigXMLStream = new ByteArrayInputStream(auditConfigXMLString.getBytes(StandardCharsets.UTF_8));

                        //  Create <tenantId>.Audit<application> table based on the audit events XML.
                        LOG.debug("addTenant: Creating new auditing table for tenant '{}', application '{}'...", tenantId, application);
                        TransformHelper transform = new TransformHelper();
                        String createTableSQL = transform.doCreateTableTransform(auditConfigXMLStream,TRANSFORM_TEMPLATE_NAME,tenantId);
                        databaseHelper.createTable(createTableSQL);

                        LOG.info("addTenant: Database changes complete for tenant '{}', application '{}'...", tenantId, application);
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
