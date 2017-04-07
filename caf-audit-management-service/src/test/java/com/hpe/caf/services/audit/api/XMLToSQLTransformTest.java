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

import com.hpe.caf.auditing.schema.AuditedApplication;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class XMLToSQLTransformTest {

    private static final String XML_AUDIT_VALID_FILENAME = "xml/AuditConfigValid.xml";

    @Test
    public void testCreateTableTransform () throws Exception {

        //  Load Audit events XML file.
        ClassLoader classLoader = getClass().getClassLoader();

        File auditEventsXMLFile;
        auditEventsXMLFile = new File(classLoader.getResource(XML_AUDIT_VALID_FILENAME).getFile());
        Assert.assertNotNull(auditEventsXMLFile);
        Assert.assertTrue(auditEventsXMLFile.exists());

        InputStream is;
        is = new FileInputStream(auditEventsXMLFile);
        byte[] auditXMLConfigBytes = IOUtils.toByteArray(is);
        AuditedApplication auditAppData = ApiServiceUtil.getAuditedApplication(auditXMLConfigBytes);

        //  Perform XML to CREATE TABLE sql transform.
        XMLToSQLTransform transform = new XMLToSQLTransform(auditAppData);
        String createTableSQL = transform.getCreateDatabaseTableSQL("public");

        //  Verify expected sql statement.
        String EXPECTED_CREATE_TABLE_OUTPUT = "CREATE TABLE IF NOT EXISTS public.AuditApplicationX\n" +
                "(\n" +
                "processId varchar(128),\n" +
                "threadId int,\n" +
                "eventOrder int,\n" +
                "eventTime timestamp,\n" +
                "eventTimeSource varchar(128),\n" +
                "userId varchar(128),\n" +
                "correlationId varchar(128),\n" +
                "eventCategoryId varchar(128),\n" +
                "eventTypeId varchar(128),\n" +
                "eventParamString_Param varchar(65000),\n" +
                "eventParamInt16_Param int,\n" +
                "eventParamInt32_Param int,\n" +
                "eventParamInt64_Param int,\n" +
                "eventParamFloat_Param float,\n" +
                "eventParamDouble_Param float,\n" +
                "eventParamBoolean_Param boolean,\n" +
                "eventParamDate_Param timestamp,\n" +
                "eventParamString_Param2 long varchar(32000000),\n" +
                "eventParamInt16_Param2 int,\n" +
                "PRIMARY KEY (processId,threadId,eventOrder)\n" +
                ");\n";

        Assert.assertEquals(EXPECTED_CREATE_TABLE_OUTPUT.replaceAll("[\n\r]", ""),createTableSQL.replaceAll("[\n\r]", ""));

        is.close();
    }

}

