package com.hpe.caf.services.audit.api;

import com.hpe.caf.auditing.schema.AuditEvent;
import com.hpe.caf.auditing.schema.AuditEventParam;
import com.hpe.caf.auditing.schema.AuditEventParamType;
import com.hpe.caf.auditing.schema.AuditedApplication;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApplicationAddPost.class,JAXBUnmarshal.class})
public class ApplicationAddPostTest {

    private static String AUDIT_EVENTS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<AuditedApplication\n" +
            "\t\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "\t\txmlns=\"http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd\"\n" +
            "\t\txsi:schemaLocation=\"http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd\"\n" +
            ">\n" +
            "\t<ApplicationId>TestApplication</ApplicationId>\n" +
            "\t<AuditEvents>\n" +
            "\t\t<AuditEvent>\n" +
            "\t\t\t<TypeId>TestEvent1</TypeId>\n" +
            "\t\t\t<CategoryId>TestCategory1</CategoryId>\n" +
            "\t\t\t<Params>\n" +
            "\t\t\t\t<Param>\n" +
            "\t\t\t\t\t<Name>StringType</Name>\n" +
            "\t\t\t\t\t<Type>string</Type>\n" +
            "\t\t\t\t\t<ColumnName>StringType</ColumnName>\n" +
            "\t\t\t\t\t<Description>Description for StringType</Description>\n" +
            "\t\t\t\t</Param>\n" +
            "\t\t\t</Params>\n" +
            "\t\t</AuditEvent>\n" +
            "\t</AuditEvents>\n" +
            "</AuditedApplication>\n" +
            "\n";

    @Test
    public void testAddPost_Success_RegisterApplication () throws Exception {

        //  Mock JAXB xml binding code.
        AuditedApplication auditedApp = getAuditedApplication("TestApplication");
        PowerMockito.mockStatic(JAXBUnmarshal.class);
        PowerMockito.when(JAXBUnmarshal.bindAuditEventsXml(Mockito.any()))
                .thenReturn(auditedApp);

        //  Mock DatabaseHelper calls.
        DatabaseHelper mockDatabaseHelper = Mockito.mock(DatabaseHelper.class);
        Mockito.doNothing().when(mockDatabaseHelper).createAuditManagementSchema(Mockito.anyString());
        Mockito.when(mockDatabaseHelper.doesApplicationEventsRowExist(Mockito.anyString())).thenReturn(false);
        Mockito.doNothing().when(mockDatabaseHelper).insertApplicationEventsRow(Mockito.anyString(),Mockito.anyString());
        PowerMockito.whenNew(DatabaseHelper.class).withArguments(Mockito.any()).thenReturn(mockDatabaseHelper);

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        newEnv.put("database.url","testUrl");
        newEnv.put("database.service.account","testServiceUser");
        newEnv.put("database.service.account.password","testPassword");
        newEnv.put("database.loader.account","testLoaderUser");
        newEnv.put("database.loader.account.password","testPassword");
        newEnv.put("database.reader.role","testReaderRole");
        TestUtil.setSystemEnvironmentFields(newEnv);

        //  Test successful run of applications endpoint.
        InputStream auditConfigXMLStream = new ByteArrayInputStream(AUDIT_EVENTS_XML.getBytes(StandardCharsets.UTF_8));
        ApplicationAddPost.addApplication(auditConfigXMLStream);

        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).createAuditManagementSchema(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).doesApplicationEventsRowExist(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).insertApplicationEventsRow(Mockito.anyString(),Mockito.anyString());

    }

    @Test
    public void testAddPost_Success_ModifyApplication () throws Exception {

        //  Mock JAXB xml binding code.
        AuditedApplication auditedApp = getAuditedApplication("TestApplication");
        PowerMockito.mockStatic(JAXBUnmarshal.class);
        PowerMockito.when(JAXBUnmarshal.bindAuditEventsXml(Mockito.any()))
                .thenReturn(auditedApp);

        //  Mock DatabaseHelper calls.
        DatabaseHelper mockDatabaseHelper = Mockito.mock(DatabaseHelper.class);
        Mockito.doNothing().when(mockDatabaseHelper).createAuditManagementSchema(Mockito.anyString());
        Mockito.when(mockDatabaseHelper.doesApplicationEventsRowExist(Mockito.anyString())).thenReturn(true);
        Mockito.doNothing().when(mockDatabaseHelper).updateApplicationEventsRow(Mockito.anyString(),Mockito.anyString());
        List<String> tenants = new ArrayList<>();
        tenants.add("tenant1");
        tenants.add("tenant2");
        Mockito.when(mockDatabaseHelper.getTenantsForApp(Mockito.anyString())).thenReturn(tenants);
        Mockito.when(mockDatabaseHelper.doesColumnExist(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(false);
        Mockito.doNothing().when(mockDatabaseHelper).addColumn(Mockito.anyString());
        PowerMockito.whenNew(DatabaseHelper.class).withArguments(Mockito.any()).thenReturn(mockDatabaseHelper);

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        newEnv.put("database.url","testUrl");
        newEnv.put("database.service.account","testServiceUser");
        newEnv.put("database.service.account.password","testPassword");
        newEnv.put("database.loader.account","testLoaderUser");
        newEnv.put("database.loader.account.password","testPassword");
        newEnv.put("database.reader.role","testReaderRole");
        TestUtil.setSystemEnvironmentFields(newEnv);

        //  Test successful run of applications endpoint.
        InputStream auditConfigXMLStream = new ByteArrayInputStream(AUDIT_EVENTS_XML.getBytes(StandardCharsets.UTF_8));
        ApplicationAddPost.addApplication(auditConfigXMLStream);
        auditConfigXMLStream.close();

        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).createAuditManagementSchema(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).doesApplicationEventsRowExist(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).updateApplicationEventsRow(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).getTenantsForApp(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).doesColumnExist(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).addColumn(Mockito.anyString());

    }

    @Test(expected = BadRequestException.class)
    public void testAddPost_Failure_InvalidAuditEventsFile () throws Exception {

        //  Test expected failure run due to invalid XML.
        String INVALID_AUDIT_EVENTS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<AuditedApplication\n" +
                "\t\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "\t\txmlns=\"http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd\"\n" +
                "\t\txsi:schemaLocation=\"http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd\"\n" +
                ">\n" +
                "\t<InvalidApplicationId>TestApplication</InvalidApplicationId>\n" +
                "\t<AuditEvents>\n" +
                "\t\t<AuditEvent>\n" +
                "\t\t\t<TypeId>TestEvent1</TypeId>\n" +
                "\t\t\t<CategoryId>TestCategory1</CategoryId>\n" +
                "\t\t\t<Params>\n" +
                "\t\t\t\t<Param>\n" +
                "\t\t\t\t\t<Name>StringType</Name>\n" +
                "\t\t\t\t\t<Type>string</Type>\n" +
                "\t\t\t\t\t<ColumnName>StringType</ColumnName>\n" +
                "\t\t\t\t\t<Description>Description for StringType</Description>\n" +
                "\t\t\t\t</Param>\n" +
                "\t\t\t</Params>\n" +
                "\t\t</AuditEvent>\n" +
                "\t</AuditEvents>\n" +
                "</AuditedApplication>\n" +
                "\n";

        InputStream auditConfigXMLStream = new ByteArrayInputStream(INVALID_AUDIT_EVENTS_XML.getBytes(StandardCharsets.UTF_8));
        ApplicationAddPost.addApplication(auditConfigXMLStream);
        auditConfigXMLStream.close();

    }

    @Test(expected = Exception.class)
    public void testAddPost_Failure_FailedToBindXML () throws Exception {

        //  Mock JAXB xml binding code.
        PowerMockito.mockStatic(JAXBUnmarshal.class);
        PowerMockito.when(JAXBUnmarshal.bindAuditEventsXml(Mockito.any()))
                .thenThrow(new JAXBException("Test"));

        //  Test expected failure run due to invalid XML.
        InputStream auditConfigXMLStream = new ByteArrayInputStream(AUDIT_EVENTS_XML.getBytes(StandardCharsets.UTF_8));
        ApplicationAddPost.addApplication(auditConfigXMLStream);
        auditConfigXMLStream.close();

    }

    @Test(expected = BadRequestException.class)
    public void testAddPost_Failure_ApplicationIdNotFound () throws Exception {

        //  Mock JAXB xml binding code.
        AuditedApplication auditedApp = getAuditedApplication("");
        PowerMockito.mockStatic(JAXBUnmarshal.class);
        PowerMockito.when(JAXBUnmarshal.bindAuditEventsXml(Mockito.any()))
                .thenReturn(auditedApp);

        //  Test expected failure run due to invalid XML.
        InputStream auditConfigXMLStream = new ByteArrayInputStream(AUDIT_EVENTS_XML.getBytes(StandardCharsets.UTF_8));
        ApplicationAddPost.addApplication(auditConfigXMLStream);
        auditConfigXMLStream.close();

    }

    private AuditedApplication getAuditedApplication (String applicationId) throws Exception {

        AuditedApplication aa = new AuditedApplication();
        aa.setApplicationId(applicationId);

        AuditedApplication.AuditEvents aes = new AuditedApplication.AuditEvents();
        AuditEvent ae = new AuditEvent();

        AuditEventParam aep = new AuditEventParam();
        aep.setName("StringType");
        aep.setType(AuditEventParamType.STRING);
        aep.setColumnName("StringType");
        aep.setDescription("Description for StringType");

        AuditEvent.Params params = new AuditEvent.Params();
        params.getParam().add(aep);

        ae.setParams(params);
        aes.getAuditEvent().add(ae);
        aa.setAuditEvents(aes);

        return aa;
    }
}
