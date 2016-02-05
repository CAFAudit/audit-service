//package com.hpe.caf.services.audit.api;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import java.io.InputStream;
//import java.lang.reflect.Field;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(ApplicationAddPost.class)
//public class ApplicationAddPostTest {
//
//    private static final String XML_AUDIT_VALID_FILENAME = "xml/AuditConfigValid.xml";
//    private static final String XML_AUDIT_INVALID_FILENAME = "xml/AuditConfigInValid.xml";
//    private static final String XML_AUDIT_MISSING_APP_ID_FILENAME = "xml/AuditConfigMissingAppId.xml";
//
//    @Test
//    public void testCreateDatabaseSchema() throws Exception {
//        //  Mock DatabaseHelper calls.
//        DatabaseHelper mockDatabaseHelper = Mockito.mock(DatabaseHelper.class);
//        Mockito.when(mockDatabaseHelper.doesTableExist(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
//        Mockito.doNothing().when(mockDatabaseHelper).createTable(Mockito.anyString());
//        PowerMockito.whenNew(DatabaseHelper.class).withArguments(Mockito.any()).thenReturn(mockDatabaseHelper);
//
//        //  Mock database connection properties.
//        HashMap newEnv = new HashMap();
//        newEnv.put("database.url","test");
//        newEnv.put("database.schema","test");
//        newEnv.put("database.username","test");
//        newEnv.put("database.password","test");
//        set(newEnv);
//
//        //  Load Audit events XML file.
//        ClassLoader classLoader = getClass().getClassLoader();
//        InputStream auditEventsXMLIS;
//        auditEventsXMLIS = classLoader.getResource(XML_AUDIT_VALID_FILENAME).openStream();
//        Assert.assertNotNull(auditEventsXMLIS);
//
//        //  Test successful run of create new table.
//        ApplicationAddPost.addApplication(auditEventsXMLIS);
//
//        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).doesTableExist(Mockito.anyString(),Mockito.anyString());
//        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).doesColumnExist(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
//        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).addColumn(Mockito.anyString());
//        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).createTable(Mockito.anyString());
//    }
//
//    @Test
//    public void testModifyDatabaseSchemaColumnsExist() throws Exception {
//        //  Mock DatabaseHelper calls.
//        DatabaseHelper mockDatabaseHelper = Mockito.mock(DatabaseHelper.class);
//        Mockito.when(mockDatabaseHelper.doesTableExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
//        Mockito.when(mockDatabaseHelper.doesColumnExist(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
//        Mockito.doNothing().when(mockDatabaseHelper).addColumn(Mockito.anyString());
//        PowerMockito.whenNew(DatabaseHelper.class).withArguments(Mockito.any()).thenReturn(mockDatabaseHelper);
//
//        //  Mock database connection properties.
//        HashMap newEnv = new HashMap();
//        newEnv.put("database.url","test");
//        newEnv.put("database.schema","test");
//        newEnv.put("database.username","test");
//        newEnv.put("database.password","test");
//        set(newEnv);
//
//        //  Load Audit events XML file.
//        ClassLoader classLoader = getClass().getClassLoader();
//        InputStream auditEventsXMLIS;
//        auditEventsXMLIS = classLoader.getResource(XML_AUDIT_VALID_FILENAME).openStream();
//        Assert.assertNotNull(auditEventsXMLIS);
//
//        //  Test successful run of table modification.
//        ApplicationAddPost.addApplication(auditEventsXMLIS);
//
//        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).doesTableExist(Mockito.anyString(),Mockito.anyString());
//        // 10 audit event parameters specified in XML_AUDIT_NEW_TABLE_FILENAME
//        Mockito.verify(mockDatabaseHelper, Mockito.times(10)).doesColumnExist(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
//        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).addColumn(Mockito.anyString());
//        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).createTable(Mockito.anyString());
//    }
//
//    @Test
//    public void testModifyDatabaseSchemaColumnsDontExist() throws Exception {
//        //  Mock DatabaseHelper calls.
//        DatabaseHelper mockDatabaseHelper = Mockito.mock(DatabaseHelper.class);
//        Mockito.when(mockDatabaseHelper.doesTableExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
//        Mockito.when(mockDatabaseHelper.doesColumnExist(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(false);
//        Mockito.doNothing().when(mockDatabaseHelper).addColumn(Mockito.anyString());
//        PowerMockito.whenNew(DatabaseHelper.class).withArguments(Mockito.any()).thenReturn(mockDatabaseHelper);
//
//        //  Mock database connection properties.
//        HashMap newEnv = new HashMap();
//        newEnv.put("database.url","test");
//        newEnv.put("database.schema","test");
//        newEnv.put("database.username","test");
//        newEnv.put("database.password","test");
//        set(newEnv);
//
//        //  Load Audit events XML file.
//        ClassLoader classLoader = getClass().getClassLoader();
//        InputStream auditEventsXMLIS;
//        auditEventsXMLIS = classLoader.getResource(XML_AUDIT_VALID_FILENAME).openStream();
//        Assert.assertNotNull(auditEventsXMLIS);
//
//        //  Test successful run of table modification.
//        ApplicationAddPost.addApplication(auditEventsXMLIS);
//
//        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).doesTableExist(Mockito.anyString(),Mockito.anyString());
//        // 10 audit event parameters specified in XML_AUDIT_NEW_TABLE_FILENAME
//        Mockito.verify(mockDatabaseHelper, Mockito.times(10)).doesColumnExist(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
//        Mockito.verify(mockDatabaseHelper, Mockito.times(10)).addColumn(Mockito.anyString());
//        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).createTable(Mockito.anyString());
//    }
//
//    @Test(expected = ApiException.class)
//    public void testAddApplicationFailure_AppConfigMissing() throws Exception {
//
//        //  Load Audit events XML file.
//        ClassLoader classLoader = getClass().getClassLoader();
//        InputStream auditEventsXMLIS;
//        auditEventsXMLIS = classLoader.getResource(XML_AUDIT_VALID_FILENAME).openStream();
//        Assert.assertNotNull(auditEventsXMLIS);
//
//        //  Test failed run due to database config properties missing.
//        ApplicationAddPost.addApplication(auditEventsXMLIS);
//    }
//
//    @Test(expected = ApiException.class)
//    public void testAddApplicationFailure_XMLNotValid() throws Exception {
//        //  Mock database connection properties.
//        HashMap newEnv = new HashMap();
//        newEnv.put("database.url","test");
//        newEnv.put("database.schema","test");
//        newEnv.put("database.username","test");
//        newEnv.put("database.password","test");
//        set(newEnv);
//
//        //  Load Audit events XML file.
//        ClassLoader classLoader = getClass().getClassLoader();
//        InputStream auditEventsXMLIS;
//        auditEventsXMLIS = classLoader.getResource(XML_AUDIT_INVALID_FILENAME).openStream();
//        Assert.assertNotNull(auditEventsXMLIS);
//
//        //  Test failed run due to invalid XML.
//        ApplicationAddPost.addApplication(auditEventsXMLIS);
//    }
//
//    @Test(expected = ApiException.class)
//    public void testAddApplicationFailure_MissingAppId() throws Exception {
//        //  Mock database connection properties.
//        HashMap newEnv = new HashMap();
//        newEnv.put("database.url","test");
//        newEnv.put("database.schema","test");
//        newEnv.put("database.username","test");
//        newEnv.put("database.password","test");
//        set(newEnv);
//
//        //  Load Audit events XML file.
//        ClassLoader classLoader = getClass().getClassLoader();
//        InputStream auditEventsXMLIS;
//        auditEventsXMLIS = classLoader.getResource(XML_AUDIT_MISSING_APP_ID_FILENAME).openStream();
//        Assert.assertNotNull(auditEventsXMLIS);
//
//        //  Test failed run due to invalid XML.
//        ApplicationAddPost.addApplication(auditEventsXMLIS);
//    }
//
//    public static void set(Map<String, String> newenv) throws Exception {
//        Class[] classes = Collections.class.getDeclaredClasses();
//        Map<String, String> env = System.getenv();
//        for(Class cl : classes) {
//            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
//                Field field = cl.getDeclaredField("m");
//                field.setAccessible(true);
//                Object obj = field.get(env);
//                Map<String, String> map = (Map<String, String>) obj;
//                map.clear();
//                map.putAll(newenv);
//            }
//        }
//    }
//}
