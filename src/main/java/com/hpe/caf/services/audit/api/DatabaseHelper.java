package com.hpe.caf.services.audit.api;

import java.sql.*;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DatabaseHelper class is responsible for database operations.
 */
public class DatabaseHelper {

    static final String JDBC_VERTICA_PREFIX = "jdbc:vertica:";
    static final String JDBC_DRIVER = "com.vertica.jdbc.Driver";
    static final String ERR_MSG_DB_URL_FORMAT_INVALID = "Invalid database url string format - must start with jdbc:vertica:";

    private static AppConfig appConfig;

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

    /**
     * Instantiates a new DBUtil
     *
     * @param appConfig        Vertica database connection properties incl url (i.e. "jdbc:vertica://VerticaHost:portNumber/databaseName"), username and password
     */
    public DatabaseHelper(AppConfig appConfig)
    {
        this.appConfig = appConfig;
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
     * Creates a new table in the database.
     */
    public void createTable(String createTableSQL) throws Exception {
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            //  Execute a statement to create a new table.
            LOG.debug("Creating database table...");
            stmt.execute(createTableSQL);
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
     * Creates a connection to the Vertica database.
     */
    private static Connection getConnection
            () throws Exception {

        Connection conn;

        // Only JDBC/Vertica connections supported.
        final String dbURL = appConfig.getDatabaseURL().toLowerCase();
        if ( !dbURL.startsWith(JDBC_VERTICA_PREFIX) )
        {
            throw new ApiException(ERR_MSG_DB_URL_FORMAT_INVALID);
        }

        try{
            // Register JDBC driver.
            LOG.debug("Registering JDBC driver...");
            Class.forName(JDBC_DRIVER);

            // Open a connection.
            Properties myProp = new Properties();
            myProp.put("user", appConfig.getDatabaseUsername());
            myProp.put("password", appConfig.getDatabasePassword());

            LOG.debug("Connecting to database...");
            conn = DriverManager.getConnection(dbURL, myProp);
        } catch(Exception ex){
            LOG.error("Cannot get connection");
            throw new ApiException(ex.getMessage());
        }

        return conn;
    }
}

