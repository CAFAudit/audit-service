/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.testapplication.auditing;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Utility class for database operations.
 */
public class DBUtil {

    private static final String JDBC_DRIVER = "com.vertica.jdbc.Driver";
    private static final String OUTPUT_FILENAME = "caf-audit-qa-output.txt";

    private static String user;         // username of the database user account
    private static String passw;        // password of the user
    private static String dbTable;      // Vertica database table used for the test
    private static String dbURL;        // Vertica database connection string url (i.e. "jdbc:vertica://VerticaHost:portNumber/databaseName")

    private static final Logger LOG = LoggerFactory.getLogger(DBUtil.class);

    /**
     * Instantiates a new DBUtil
     *
     * @param configSource            Vertica connection configuration details
     */
    public DBUtil(final ConfigurationSource configSource) throws ConfigurationException {
        final VerticaAuditConfiguration config = configSource.getConfiguration(VerticaAuditConfiguration.class);
        dbURL = Objects.requireNonNull(config.getDatabaseURL());
        dbTable =  Objects.requireNonNull(config.getTable());
        user = Objects.requireNonNull(config.getUsername());
        passw = Objects.requireNonNull(config.getPassword());
    }

    /**
     *  Truncates the specified table.
     */
    public void truncateTable() throws Exception {

        String truncateTableSQL = "TRUNCATE TABLE " + dbTable;

        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            //  Execute a query to determine if the specified table column exists.
            LOG.debug("truncateTable: Truncating table...");
            stmt.execute(truncateTableSQL);
        }
    }

    /**
     *  Write table rows to disk.
     */
    public void writeTableRowsToDisk() throws Exception {

        String getTableRowsSQL = "SELECT * FROM " + dbTable;

        try (
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(OUTPUT_FILENAME)), "UTF-8"));
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            ResultSet rs = stmt.executeQuery(getTableRowsSQL);
            ResultSetMetaData rsmd = rs.getMetaData();

            int numberOfColumns = rsmd.getColumnCount();

            //  Write table row contents to disk.
            LOG.debug("writeTableRowsToDisk: Writing table rows to disk...");

            for (int i = 1; i <= numberOfColumns; i++) {
                if (i > 1) writer.append(",  ");
                String columnName = rsmd.getColumnName(i);
                writer.append(columnName);
            }
            writer.println();

            while (rs.next()) {
                for (int i = 1; i <= numberOfColumns; i++) {
                    if (i > 1) writer.append(",  ");
                    String columnValue = rs.getString(i);
                    writer.append(columnValue);
                }
                writer.println();
            }
        }
    }

    /**
     *  Return table rows as a list.
     */
    public List<HashMap<String,Object>> getTableRowsAsList() throws Exception {

        List<HashMap<String,Object>> rsList;
        String getTableRowsSQL = "SELECT * FROM " + dbTable;

        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            ResultSet rs = stmt.executeQuery(getTableRowsSQL);
            rsList = convertResultSetToList(rs);
        }

        return rsList;
    }

    /**
     * Creates a connection to the Vertica database.
     */
    private static Connection getConnection
    () throws Exception {

        Connection conn;

        try{
            // Register JDBC driver.
            LOG.debug("Registering JDBC driver...");
            Class.forName(JDBC_DRIVER);

            // Open a connection.
            Properties myProp = new Properties();
            myProp.put("user", user);
            myProp.put("password", passw);

            LOG.debug("Connecting to database...");
            conn = DriverManager.getConnection(dbURL, myProp);
        } catch(Exception ex){
            LOG.error("Cannot get connection");
            throw new Exception(ex);
        }

        return conn;
    }

    private List<HashMap<String,Object>> convertResultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<HashMap<String,Object>> list = new ArrayList<>();

        while (rs.next()) {
            HashMap<String,Object> row = new HashMap<>(columns);
            for(int i=1; i<=columns; ++i) {
                row.put(md.getColumnName(i).replace("eventParam",""),rs.getObject(i));
            }
            list.add(row);
        }

        return list;
    }
}


