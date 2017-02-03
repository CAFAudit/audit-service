package com.hpe.caf.services.audit.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

public class DBUtil {

    private static final String JDBC_DRIVER = "com.vertica.jdbc.Driver";
    private static final String OUTPUT_FILENAME = "./test-case/output.txt";

    private static String user;     // username of the database user account
    private static String passw;    // password of the user
    private static String dbTable;  // Vertica database table used for the test
    private static String dbURL;    // Vertica database connection string url (i.e. "jdbc:vertica://VerticaHost:portNumber/databaseName")

    private static final Logger LOG = LoggerFactory.getLogger(DBUtil.class);


    public DBUtil(final String verticaHost, final String databaseName, final String databasePort, final String databaseSchema, final String tableName, final String databaseUsername, final String databasePassword) {
        dbURL = MessageFormat.format("jdbc:vertica://{0}:{1}/{2}", Objects.requireNonNull(verticaHost), Objects.requireNonNull(databasePort), Objects.requireNonNull(databaseName));
        dbTable = MessageFormat.format("{0}.Audit{1}", Objects.requireNonNull(databaseSchema), Objects.requireNonNull(tableName));
        user = Objects.requireNonNull(databaseUsername);
        passw = Objects.requireNonNull(databasePassword);
    }


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


    private static Connection getConnection
    () throws Exception {

        Connection conn;

        try{
            // Register JDBC driver.
            LOG.info("Registering JDBC driver...");
            Class.forName(JDBC_DRIVER);

            // Open a connection.
            Properties myProp = new Properties();
            myProp.put("user", user);
            myProp.put("password", passw);

            LOG.info("Connecting to database...");
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
