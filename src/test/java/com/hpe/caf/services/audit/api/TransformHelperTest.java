package com.hpe.caf.services.audit.api;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class TransformHelperTest {

    private static final String XML_AUDIT_VALID_FILENAME = "xml/AuditConfigValid.xml";
    private static final String VELOCITY_TEMPLATE_NAME = "AuditTransform.vm";

    @Test
    public void testCreateTableTransform () throws Exception {

        //  Load Audit events XML file.
        ClassLoader classLoader = getClass().getClassLoader();

        File auditEventsXMLFile = new File(classLoader.getResource(XML_AUDIT_VALID_FILENAME).getFile());
        Assert.assertNotNull(auditEventsXMLFile);
        Assert.assertTrue(auditEventsXMLFile.exists());

        InputStream is;
        is = new FileInputStream(auditEventsXMLFile);

        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doCreateTableTransform(is,VELOCITY_TEMPLATE_NAME,"public");

        String EXPECTED_CREATE_TABLE_OUTPUT = "CREATE TABLE IF NOT EXISTS public.AuditApplicationX\n" +
                "(\n" +
                "processId varchar(128),\n" +
                "threadId int,\n" +
                "eventOrder int,\n" +
                "eventTime timestamp,\n" +
                "eventTimeSource varchar(128),\n" +
                "userId varchar(128),\n" +
                "eventCategoryId varchar(128),\n" +
                "eventTypeId varchar(128),\n" +
                "correlationId varchar(128),\n" +
                "eventParamString_Param varchar(65000),\n" +
                "eventParamInt16_Param int,\n" +
                "eventParamInt32_Param int,\n" +
                "eventParamInt64_Param int,\n" +
                "eventParamFloat_Param float,\n" +
                "eventParamDouble_Param float,\n" +
                "eventParamBoolean_Param boolean,\n" +
                "eventParamDate_Param timestamp,\n" +
                "eventParamString_Param2 varchar(65000),\n" +
                "eventParamInt16_Param2 int\n" +
                ");\n" +
                "ALTER TABLE public.AuditApplicationX\n" +
                "ADD PRIMARY KEY (processId,threadId,eventOrder);\n";

        Assert.assertEquals(createTableSQL.replaceAll("[\n\r]", ""), EXPECTED_CREATE_TABLE_OUTPUT.replaceAll("[\n\r]", ""));

        is.close();
    }

    @Test
    public void testModifyTableTransformStringType () throws Exception {

        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doModifyTableTransform(VELOCITY_TEMPLATE_NAME,"public","TestTable","TestColumn","string");
        String EXPECTED_MODIFY_TABLE_OUTPUT_STRING = "ALTER TABLE public.TestTable ADD COLUMN TestColumn varchar(65000)\n";
        Assert.assertEquals(createTableSQL.replaceAll("[\n\r]", ""), EXPECTED_MODIFY_TABLE_OUTPUT_STRING.replaceAll("[\n\r]", ""));
    }

    @Test
    public void testModifyTableTransformShortType () throws Exception {

        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doModifyTableTransform(VELOCITY_TEMPLATE_NAME,"public","TestTable","TestColumn","short");
        String EXPECTED_MODIFY_TABLE_OUTPUT_SHORT = "ALTER TABLE public.TestTable ADD COLUMN TestColumn int\n";
        Assert.assertEquals(createTableSQL.replaceAll("[\n\r]", ""), EXPECTED_MODIFY_TABLE_OUTPUT_SHORT.replaceAll("[\n\r]", ""));
    }

    @Test
    public void testModifyTableTransformIntType () throws Exception {

        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doModifyTableTransform(VELOCITY_TEMPLATE_NAME,"public","TestTable","TestColumn","int");
        String EXPECTED_MODIFY_TABLE_OUTPUT_INT = "ALTER TABLE public.TestTable ADD COLUMN TestColumn int\n";
        Assert.assertEquals(createTableSQL.replaceAll("[\n\r]", ""), EXPECTED_MODIFY_TABLE_OUTPUT_INT.replaceAll("[\n\r]", ""));
    }

    @Test
    public void testModifyTableTransformLongType () throws Exception {

        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doModifyTableTransform(VELOCITY_TEMPLATE_NAME,"public","TestTable","TestColumn","long");
        String EXPECTED_MODIFY_TABLE_OUTPUT_LONG = "ALTER TABLE public.TestTable ADD COLUMN TestColumn int\n";
        Assert.assertEquals(createTableSQL.replaceAll("[\n\r]", ""), EXPECTED_MODIFY_TABLE_OUTPUT_LONG.replaceAll("[\n\r]", ""));
    }

    @Test
    public void testModifyTableTransformFloatType () throws Exception {

        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doModifyTableTransform(VELOCITY_TEMPLATE_NAME,"public","TestTable","TestColumn","float");
        String EXPECTED_MODIFY_TABLE_OUTPUT_FLOAT = "ALTER TABLE public.TestTable ADD COLUMN TestColumn float\n";
        Assert.assertEquals(createTableSQL.replaceAll("[\n\r]", ""), EXPECTED_MODIFY_TABLE_OUTPUT_FLOAT.replaceAll("[\n\r]", ""));
    }

    @Test
    public void testModifyTableTransformDoubleType () throws Exception {

        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doModifyTableTransform(VELOCITY_TEMPLATE_NAME,"public","TestTable","TestColumn","double");
        String EXPECTED_MODIFY_TABLE_OUTPUT_DOUBLE = "ALTER TABLE public.TestTable ADD COLUMN TestColumn float\n";
        Assert.assertEquals(createTableSQL.replaceAll("[\n\r]", ""), EXPECTED_MODIFY_TABLE_OUTPUT_DOUBLE.replaceAll("[\n\r]", ""));
    }

    @Test
    public void testModifyTableTransformBooleanType () throws Exception {

        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doModifyTableTransform(VELOCITY_TEMPLATE_NAME,"public","TestTable","TestColumn","boolean");
        String EXPECTED_MODIFY_TABLE_OUTPUT_BOOLEAN = "ALTER TABLE public.TestTable ADD COLUMN TestColumn boolean\n";
        Assert.assertEquals(createTableSQL.replaceAll("[\n\r]", ""), EXPECTED_MODIFY_TABLE_OUTPUT_BOOLEAN.replaceAll("[\n\r]", ""));
    }

    @Test
    public void testModifyTableTransformDateType () throws Exception {

        TransformHelper transform = new TransformHelper();
        String createTableSQL = transform.doModifyTableTransform(VELOCITY_TEMPLATE_NAME,"public","TestTable","TestColumn","date");
        String EXPECTED_MODIFY_TABLE_OUTPUT_DATE = "ALTER TABLE public.TestTable ADD COLUMN TestColumn timestamp\n";
        Assert.assertEquals(createTableSQL.replaceAll("[\n\r]", ""), EXPECTED_MODIFY_TABLE_OUTPUT_DATE.replaceAll("[\n\r]", ""));
    }
}

