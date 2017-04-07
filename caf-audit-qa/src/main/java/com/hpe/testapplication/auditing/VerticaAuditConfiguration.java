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

/**
 * Vertica database audit configuration class.
 */
public class VerticaAuditConfiguration {

    /**
     * Vertica database connection string url (i.e. "jdbc:vertica://VerticaHost:portNumber/databaseName")
     */
    private String databaseURL;

    /**
     * Vertica database table to be used for this test.
     */
    private String table;

    /**
     * username of the database user account.
     */
    private String username;

    /**
     * password of the database user account.
     */
    private String password;

    public VerticaAuditConfiguration() {

    }

    /**
     * Getter for property 'databaseURL'.
     *
     * @return Value for property 'databaseURL'.
     */
    public String getDatabaseURL() {
        return databaseURL;
    }

    /**
     * Setter for property 'databaseURL'.
     *
     * @param databaseURL Value to set for property 'databaseURL'.
     */
    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    /**
     * Getter for property 'table'.
     *
     * @return Value for property 'table'.
     */
    public String getTable() {
        return table;
    }

    /**
     * Setter for property 'table'.
     *
     * @param table Value to set for property 'table'.
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * Getter for property 'username'.
     *
     * @return Value for property 'username'.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter for property 'username'.
     *
     * @param username Value to set for property 'username'.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter for property 'password'.
     *
     * @return Value for property 'password'.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for property 'password'.
     *
     * @param password Value to set for property 'password'.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
