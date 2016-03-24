package com.hpe.caf.services.audit.api;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DatabaseHelper class is responsible for database operations.
 */
public class DatabaseHelper {

    private static final String JDBC_VERTICA_PREFIX = "jdbc:vertica:";
    private static final String JDBC_DRIVER = "com.vertica.jdbc.Driver";
    private static final String ERR_MSG_DB_URL_FORMAT_INVALID = "Invalid database url string format - must start with jdbc:vertica:";

    private static AppConfig appConfig;

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

    /**
     * Instantiates a new DBUtil
     *
     * @param appConfig        Vertica database connection properties incl url (i.e. "jdbc:vertica://VerticaHost:portNumber/databaseName"), username and password
     */
    public DatabaseHelper(AppConfig appConfig)
    {
        DatabaseHelper.appConfig = appConfig;
    }

    /**
     * Checks if the target schema exists in the database.
     */
    public boolean doesSchemaExist(String schemaName) throws Exception {

        boolean exists = false;

        String schemaExistsSQL = "select 1 as schemaExists from  v_catalog.schemata where upper(schema_name) = upper(?)";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(schemaExistsSQL)
        ) {
            stmt.setString(1,schemaName);

            //  Execute a query to determine if the specified schema exists.
            LOG.debug("doesSchemaExist: Checking if schema exists...");
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                exists = rs.getInt("schemaExists") > 0;
            }
        }

        return exists;
    }

    /**
     * Checks if the target database table exists in the database.
     */
    public boolean doesTableExist(String tableSchema, String tableName) throws Exception {

        boolean exists = false;

        String tableExistsSQL = "select 1 as tableExists from v_catalog.tables where table_schema = ? and upper(table_name) = upper(?)";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(tableExistsSQL)
        ) {
            stmt.setString(1,tableSchema);
            stmt.setString(2,tableName);

            //  Execute a query to determine if the specified table exists.
            LOG.debug("doesTableExist: Checking if table exists...");
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                exists = rs.getInt("tableExists") > 0;
            }
        }

        return exists;
    }

    /**
     * Checks if the target database table column exists in the specified database table.
     */
    public boolean doesColumnExist(String tableSchema, String tableName, String columnName) throws Exception {

        boolean exists = false;

        String columnExistsSQL = "select 1 as columnExists from v_catalog.columns where table_schema = ? and upper(table_name) = upper(?) and upper(column_name) = upper(?)";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(columnExistsSQL)
        ) {
            stmt.setString(1,tableSchema);
            stmt.setString(2,tableName);
            stmt.setString(3,columnName);

            //  Execute a query to determine if the specified table column exists.
            LOG.debug("doesColumnExist: Checking if column exists...");
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                exists = rs.getInt("columnExists") > 0;
            }
        }

        return exists;
    }

    /**
     * Creates a new schema in the database.
     */
    public void createSchema(String schemaName) throws Exception {

        String createSchemaSQL = "CREATE SCHEMA IF NOT EXISTS " + schemaName;

        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            //  Execute a statement to create a new schema.
            stmt.execute(createSchemaSQL);
        }
    }

    /**
     * Grant USAGE on schema to the specified role.
     */
    public void grantUsageOnSchema(String schemaName, String roleName) throws Exception {

        String grantUsageOnSchemaSQL = "GRANT USAGE ON SCHEMA " + schemaName + " TO \"" + roleName + "\"";

        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            //  Execute a statement to grant usage on the schema to the specified role.
            stmt.execute(grantUsageOnSchemaSQL);
        }
    }

    /**
     * Grant USAGE on the audit scheduler schema to the specified user.
     */
    public void grantUsageOnAuditSchedulerSchema(String schemaName, String userName) throws Exception {

        String grantUsageOnSchemaSQL = "GRANT USAGE ON SCHEMA " + schemaName + " TO \"" + userName + "\"";

        try (
                Connection conn = getConnection(appConfig.getDatabaseLoaderAccount(), appConfig.getDatabaseLoaderAccountPassword());
                Statement stmt = conn.createStatement()
        ) {
            //  Execute a statement to grant usage on the schema to the specified user.
            stmt.execute(grantUsageOnSchemaSQL);
        }
    }

    /**
     * Creates a new table in the database.
     */
    public void createTable(String createTableSQL) throws Exception {
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            //  Execute a statement to create a new table.
            stmt.execute(createTableSQL);
        }
    }

    /**
     * Grant SELECT on the table to the specified role.
     */
    public void grantSelectOnTable(String tableName, String roleName) throws Exception {

        String grantSelectOnTableSQL = "GRANT SELECT ON TABLE " + tableName + " TO \"" + roleName + "\"";

        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            //  Execute a statement to grant select on the table to the specified role.
            stmt.execute(grantSelectOnTableSQL);
        }
    }

    /**
     * Adds a new database column to an existing database table.
     */
    public void addColumn(String addColumnSQL) throws Exception {
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            //  Execute a statement to add a new column.
            LOG.debug("Adding new table column...");
            stmt.execute(addColumnSQL);
        }
    }

    /**
     * Creates a connection to the Vertica database as the service account.
     */
    private static Connection getConnection
            () throws Exception {
        return getConnection(appConfig.getDatabaseServiceAccount(), appConfig.getDatabaseServiceAccountPassword());
    }

    /**
     * Creates a connection to the Vertica database as the specified user.
     */
    private static Connection getConnection(String username, String password) throws Exception {

        Connection conn;

        // Only JDBC/Vertica connections supported.
        final String dbURL = appConfig.getDatabaseURL().toLowerCase();
        if ( !dbURL.startsWith(JDBC_VERTICA_PREFIX) )
        {
            throw new BadRequestException(ERR_MSG_DB_URL_FORMAT_INVALID);
        }

        try{
            // Register JDBC driver.
            LOG.debug("Registering JDBC driver...");
            Class.forName(JDBC_DRIVER);

            // Open a connection.
            Properties myProp = new Properties();
            myProp.put("user", username);
            myProp.put("password", password);

            LOG.debug("Connecting to database...");
            conn = DriverManager.getConnection(dbURL, myProp);
        } catch(Exception ex){
            LOG.error("Cannot get connection");
            throw new BadRequestException(ex.getMessage());
        }

        return conn;
    }

    /**
     * Create the AuditManagement database schema and tables.
     */
    public void createAuditManagementSchema(String role) throws Exception {

        //  Create the AuditManagement schema if it does not already exist and grant usage to the audit reader role.
        LOG.debug("Creating the AuditManagement database schema...");
        createSchema("AuditManagement");
        grantUsageOnSchema("AuditManagement", role);

        //  Create AuditManagement.ApplicationEvents table to store application defined audit
        //  events XML and grant SELECT to the audit reader role.
        if (!doesTableExist("AuditManagement","ApplicationEvents")) {
            String sqlCreateApplicationEventsTable = "CREATE TABLE IF NOT EXISTS AuditManagement.ApplicationEvents ("
                    + "   applicationId varchar(128) not null primary key,"
                    + "   eventsXML long varchar not null )";
            LOG.debug("Creating the AuditManagement.ApplicationEvents database table...");
            createTable(sqlCreateApplicationEventsTable);
            grantSelectOnTable("AuditManagement.ApplicationEvents", role);
        }

        //  Create AuditManagement.TenantApplications table to store tenant and application
        //  mappings and grant SELECT to the audit reader role.
        if (!doesTableExist("AuditManagement","TenantApplications")) {
            String sqlCreateTenantApplicationsTable = "CREATE TABLE IF NOT EXISTS AuditManagement.TenantApplications ("
                    + "   tenantId varchar(128) not null,"
                    + "   applicationId varchar(128) not null,"
                    + "   PRIMARY KEY (tenantId, applicationId));"
                    + "ALTER TABLE AuditManagement.TenantApplications"
                    + "   ADD CONSTRAINT fk_applicationId FOREIGN KEY (applicationId)"
                    + "   REFERENCES AuditManagement.ApplicationEvents (applicationId);";
            LOG.debug("Creating the AuditManagement.TenantApplications database table...");
            createTable(sqlCreateTenantApplicationsTable);
            grantSelectOnTable("AuditManagement.TenantApplications", role);
        }
    }

    /**
     * Store application name and XML audit event mappings.
     */
    public void insertApplicationEventsRow(String applicationId, String eventsXML) throws Exception {

        String insertRowSQL = "INSERT INTO AuditManagement.ApplicationEvents (applicationId, eventsXML) VALUES (?,?)";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertRowSQL)
        ) {
            stmt.setString(1,applicationId);
            stmt.setString(2,eventsXML);

            LOG.debug("Inserting row into AuditManagement.ApplicationEvents for application {}...", applicationId);
            stmt.execute();
        }
    }

    /**
     * Modify application name and XML audit event mappings.
     */
    public void updateApplicationEventsRow(String applicationId, String eventsXML) throws Exception {

        String updateRowSQL = "UPDATE AuditManagement.ApplicationEvents SET eventsXML = ? WHERE applicationId = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(updateRowSQL)
        ) {
            stmt.setString(2,applicationId);
            stmt.setString(1,eventsXML);

            LOG.debug("Updating row in AuditManagement.ApplicationEvents for application {}...", applicationId);
            stmt.execute();
        }
    }

    /**
     * Store tenant and application mappings.
     */
    public void insertTenantApplicationsRow(String tenantId, String applicationId) throws Exception {

        String insertRowSQL = "INSERT INTO AuditManagement.TenantApplications (tenantId, applicationId) VALUES (?,?)";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertRowSQL)
        ) {
            stmt.setString(1,tenantId);
            stmt.setString(2,applicationId);

            LOG.debug("Inserting row into AuditManagement.TenantApplications for tenant {}, application {} ...", tenantId, applicationId);
            stmt.execute();
        }
    }

    /**
     * Remove tenant and application mapping.
     */
    public void deleteTenantApplicationsRow(String tenantId, String applicationId) throws Exception {

        String insertRowSQL = "DELETE FROM AuditManagement.TenantApplications WHERE tenantId = ? AND applicationId = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertRowSQL)
        ) {
            stmt.setString(1,tenantId);
            stmt.setString(2,applicationId);

            LOG.debug("Deleting row from AuditManagement.TenantApplications for tenant {}, application {} ...", tenantId, applicationId);
            stmt.execute();
        }
    }

    /**
     * Returns a list of tenants for the specified application identifier.
     */
    public List<String> getTenantsForApp(String applicationId) throws Exception {

        List<String> tenantList = new ArrayList<>();
        String getTenantsSQL = "SELECT tenantId FROM AuditManagement.TenantApplications WHERE applicationId = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(getTenantsSQL)
        ) {
            stmt.setString(1,applicationId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tenantList.add(rs.getString("tenantId"));
            }
        }

        return tenantList;
    }

    /**
     * Returns the events XML for the specified application identifier.
     */
    public String getEventsXMLForApp(String applicationId) throws Exception {

        String eventsXML="";
        String getEventsXmlSQL = "SELECT eventsXML FROM AuditManagement.ApplicationEvents WHERE applicationId = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(getEventsXmlSQL)
        ) {
            stmt.setString(1,applicationId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                eventsXML = rs.getString("eventsXML");
            }
        }

        return eventsXML;
    }

    /**
     * Check if the application defined audit events XML has already been registered.
     */
    public boolean doesApplicationEventsRowExist(String applicationId) throws Exception {

        boolean exists = false;

        String rowExistsSQL = "select 1 as rowExists from AuditManagement.ApplicationEvents where applicationId = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(rowExistsSQL)
        ) {
            stmt.setString(1,applicationId);

            //  Execute a query to determine if the specified table column exists.
            LOG.debug("doesApplicationEventsRowExist: Checking if ApplicationEvents row already exists...");
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                exists = rs.getInt("rowExists") > 0;
            }
        }

        return exists;
    }

    /**
     * Check if tenant/application mapping has already been registered.
     */
    public boolean doesTenantApplicationsRowExist(String tenantId, String applicationId) throws Exception {

        boolean exists = false;

        String rowExistsSQL = "select 1 as rowExists from AuditManagement.TenantApplications where tenantId = ? and applicationId = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(rowExistsSQL)
        ) {
            stmt.setString(1,tenantId);
            stmt.setString(2,applicationId);

            //  Execute a query to determine if the specified row exists.
            LOG.debug("doesTenantApplicationsRowExist: Checking if TenantApplications row already exists...");
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                exists = rs.getInt("rowExists") > 0;
            }
        }

        return exists;
    }

}

