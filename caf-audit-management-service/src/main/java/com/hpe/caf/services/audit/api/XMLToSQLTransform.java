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
package com.hpe.caf.services.audit.api;

import com.hpe.caf.auditing.schema.AuditEvent;
import com.hpe.caf.auditing.schema.AuditEventParam;
import com.hpe.caf.auditing.schema.AuditEventParamType;
import com.hpe.caf.auditing.schema.AuditedApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The XMLToSQLTransform class is responsible for generating table creation SQL from XML.
 */
public class XMLToSQLTransform {

    private static AuditedApplication auditApplicationData;

    private static final Logger LOG = LoggerFactory.getLogger(XMLToSQLTransform.class);

    public XMLToSQLTransform(AuditedApplication auditApplicationData) {
        XMLToSQLTransform.auditApplicationData = auditApplicationData;
    }

    private class ColumnDefinition
    {
        private String name;
        private int maxLength;
        private String definition;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(final int maxLength) {
            this.maxLength = maxLength;
        }

        public String getDefinition()
        {
            return definition;
        }

        public void setDefinition(final String definition)
        {
            this.definition = definition;
        }
    }

    /**
     *  Generates CREATE TABLE SQL based on the audited application data xml file.
     */
    public String getCreateDatabaseTableSQL(String schemaName) throws Exception {

        LOG.debug("getCreateDatabaseTableSQL: Generating CREATE TABLE sql for audited application {} ...", auditApplicationData.getApplicationId());

        //  Identify table name from the application identifier.
        String TABLE_NAME_PREFIX = "Audit";
        String tableName = schemaName + "." + TABLE_NAME_PREFIX + auditApplicationData.getApplicationId();

        //  Get list of fixed column definitions.
        LOG.debug("getCreateDatabaseTableSQL: Getting fixed column definitions ...");
        String fixedColumnDefinitions = getFixedColumnDefinitions();

        //  Get list of fixed column definitions.
        LOG.debug("getCreateDatabaseTableSQL: Getting audit event parameter definitions ...");
        String auditEventParameterDefinitions = getAuditEventParameterDefinitions();

        //  Primary key declaration.
        String PRIMARY_KEY_COLUMNS = "processId,threadId,eventOrder";
        String primaryKeyDeclaration = "PRIMARY KEY (" + PRIMARY_KEY_COLUMNS + ")\n";

        LOG.debug("getCreateDatabaseTableSQL: CREATE TABLE sql for audited application {} has been generated ...", auditApplicationData.getApplicationId());

        //  Generate CREATE TABLE sql statement.
        return "CREATE TABLE IF NOT EXISTS " + tableName + "\n" +
                "(\n" +
                fixedColumnDefinitions +
                auditEventParameterDefinitions +
                primaryKeyDeclaration +
                ");\n";

    }

    /**
     *  Returns list of fixed column definitions to be used in CREATE TABLE sql.
     */
    private String getFixedColumnDefinitions() {

        return "processId varchar(128),\n" +
                "threadId int,\n" +
                "eventOrder int,\n" +
                "eventTime timestamp,\n" +
                "eventTimeSource varchar(128),\n" +
                "userId varchar(128),\n" +
                "correlationId varchar(128),\n" +
                "eventCategoryId varchar(128),\n" +
                "eventTypeId varchar(128),\n";
    }

    /**
     *  Returns list of audit event parameter definitions to be used in CREATE TABLE sql.
     */
    private String getAuditEventParameterDefinitions() throws Exception {

        StringBuilder aepDefinition = new StringBuilder();
        ArrayList<ColumnDefinition> columnDefinitions = new ArrayList<>();

        //  Iterate through parameters configured in the audited application data xml.
        List<AuditEvent> eventList = auditApplicationData.getAuditEvents().getAuditEvent();

        ArrayList<AuditEventParam> paramsList = new ArrayList<>();
        for (AuditEvent anEventList : eventList) {
            List<AuditEventParam> params = anEventList.getParams().getParam();
            paramsList.addAll(params);
        }

        for (AuditEventParam aep : paramsList) {
            //  Identify column name, type and maxLength constraint.
            String columnName = ApiServiceUtil.CUSTOM_EVENT_PARAM_PREFIX + (ApiServiceUtil.isNotNullOrEmpty(aep.getColumnName()) ? aep.getColumnName() : aep.getName());
            AuditEventParamType paramType = aep.getType();

            Integer maxLengthConstraint = ApiServiceUtil.VERTICA_MAX_VARCHAR_SIZE;
            if (aep.getConstraints() != null) {
                if (aep.getConstraints().getMaxLength() != null) {
                    maxLengthConstraint = aep.getConstraints().getMaxLength().intValue();
                }
            }

            //  Get column definition for the specified parameter.
            String columnDefinitionString = columnName + " " + ApiServiceUtil.getVerticaType(paramType.toString(), maxLengthConstraint);

            // Has the column already been processed.
            if (containsColumnName(columnDefinitions, columnName)) {
                //  If column is of type string, then we need to consider a change to the size
                //  if this definition is greater than that defined previously.
                if (paramType == AuditEventParamType.STRING) {
                    Integer prevMaxLengthConstraint = getColumnMaxLength(columnDefinitions, columnName);
                    if (maxLengthConstraint > prevMaxLengthConstraint) {
                       updateColumnDefinition(columnDefinitions, columnName, maxLengthConstraint, columnDefinitionString);
                    }
                }
            }
            else {
                //  Column has not been processed yet.
                ColumnDefinition colDefinition = new ColumnDefinition();
                colDefinition.setName(columnName);
                colDefinition.setMaxLength(maxLengthConstraint);

                colDefinition.setDefinition(columnDefinitionString);
                columnDefinitions.add(colDefinition);
            }
        }

        //  Create audit event parameter definitions list.
        for (ColumnDefinition colDefn : columnDefinitions) {
            aepDefinition.append(colDefn.getDefinition()).append(",\n");
        }

        return aepDefinition.toString();
    }

    /**
     *  Returns true if the specified audit event parameter name is listed in the column definition list.
     */
    private boolean containsColumnName(List<ColumnDefinition> list, String name) {
        for (ColumnDefinition object : list) {
            if (object.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Gets the maxLength size for the specified audit event parameter name in the column definition list.
     */
    private Integer getColumnMaxLength(List<ColumnDefinition> list, String name) {
        Integer columnMaxLength = null;

        for (ColumnDefinition object : list) {
            if (object.getName().equals(name)) {
                columnMaxLength = object.getMaxLength();
                break;
            }
        }
        return columnMaxLength;
    }

    /**
     *  Modifies the maxLength and column definition for the specified audit event parameter name in the column definition list.
     */
    private void updateColumnDefinition(List<ColumnDefinition> list, String name, Integer maxLength, String columnDefinition) {
        for (ColumnDefinition object : list) {
            if (object.getName().equals(name)) {
                object.setMaxLength(maxLength);
                object.setDefinition(columnDefinition);
                break;
            }
        }
    }
}
