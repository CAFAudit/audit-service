package com.hpe.caf.services.audit.api;

import com.hpe.caf.auditing.schema.AuditEvent;
import com.hpe.caf.auditing.schema.AuditEventParam;
import com.hpe.caf.auditing.schema.AuditEventParamType;
import com.hpe.caf.auditing.schema.AuditedApplication;
import com.hpe.caf.services.audit.api.exceptions.BadRequestException;
import com.hpe.caf.services.audit.api.generated.model.NewTenant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TenantAddPost.class,KafkaScheduler.class,ApiServiceUtil.class})
public class TenantAddPostTest {

    private HashMap<String, String> newEnv;

    @Test
    public void testAddTenant_Success () throws Exception {

        //  Mock AppConfig calls.
        AppConfig mockAppConfig = Mockito.mock(AppConfig.class);
        Mockito.when(mockAppConfig.getCAFAuditManagementDisable()).thenReturn("false");

        //  Mock ApiServiceUtil calls.
        AuditedApplication auditedApp = TestUtil.getAuditedApplication("TestApplication");
        PowerMockito.mockStatic(ApiServiceUtil.class);
        PowerMockito.when(ApiServiceUtil.getAppConfigProperties())
                .thenReturn(mockAppConfig);
        PowerMockito.when(ApiServiceUtil.getAuditedApplication(Mockito.any()))
                .thenReturn(auditedApp);

        //  Mock DatabaseHelper calls.
        DatabaseHelper mockDatabaseHelper = Mockito.mock(DatabaseHelper.class);
        Mockito.when(mockDatabaseHelper.doesTableExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(mockDatabaseHelper.getEventsXMLForApp(Mockito.anyString())).thenReturn("Events XML");
        Mockito.when(mockDatabaseHelper.doesTenantApplicationsRowExist(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Mockito.doNothing().when(mockDatabaseHelper).insertTenantApplicationsRow(Mockito.anyString(),Mockito.anyString());
        Mockito.doNothing().when(mockDatabaseHelper).createSchema(Mockito.anyString());
        Mockito.doNothing().when(mockDatabaseHelper).createTable(Mockito.anyString());
        Mockito.doNothing().when(mockDatabaseHelper).grantSelectOnTable(Mockito.anyString(),Mockito.anyString());
        PowerMockito.whenNew(DatabaseHelper.class).withArguments(Mockito.any()).thenReturn(mockDatabaseHelper);

        //  Mock TransformHelper calls.
        XMLToSQLTransform mockXMLToSQLTransform = Mockito.mock(XMLToSQLTransform.class);
        Mockito.when(mockXMLToSQLTransform.getCreateDatabaseTableSQL(Mockito.anyString())).thenReturn("");
        PowerMockito.whenNew(XMLToSQLTransform.class).withArguments(Mockito.any()).thenReturn(mockXMLToSQLTransform);

        //  Mock KafkaScheduler calls.
        PowerMockito.mockStatic(KafkaScheduler.class);
        PowerMockito.doNothing().when(KafkaScheduler.class, "associateTopic", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        NewTenant newTenant = new NewTenant();
        newTenant.setTenantId("testtenant");

        List<String> applications = new ArrayList<>();
        applications.add("Application1");
        applications.add("Application2");
        newTenant.setApplication(applications);

        TenantAddPost.addTenant(newTenant);

        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).doesTableExist(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).getEventsXMLForApp(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).doesTenantApplicationsRowExist(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).insertTenantApplicationsRow(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).createSchema(Mockito.anyString());
        Mockito.verify(mockXMLToSQLTransform, Mockito.times(2)).getCreateDatabaseTableSQL(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).createTable(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).grantSelectOnTable(Mockito.anyString(),Mockito.anyString());

    }

    @Test
    public void testAddTenant_Success_WebServiceDisabled () throws Exception {

        //  Mock AppConfig calls.
        AppConfig mockAppConfig = Mockito.mock(AppConfig.class);
        Mockito.when(mockAppConfig.getCAFAuditManagementDisable()).thenReturn("true");

        //  Mock ApiServiceUtil calls.
        AuditedApplication auditedApp = TestUtil.getAuditedApplication("TestApplication");
        PowerMockito.mockStatic(ApiServiceUtil.class);
        PowerMockito.when(ApiServiceUtil.getAppConfigProperties())
                .thenReturn(mockAppConfig);
        PowerMockito.when(ApiServiceUtil.getAuditedApplication(Mockito.any()))
                .thenReturn(auditedApp);

        //  Mock DatabaseHelper calls.
        DatabaseHelper mockDatabaseHelper = Mockito.mock(DatabaseHelper.class);
        Mockito.when(mockDatabaseHelper.doesTableExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(mockDatabaseHelper.getEventsXMLForApp(Mockito.anyString())).thenReturn("Events XML");
        Mockito.when(mockDatabaseHelper.doesTenantApplicationsRowExist(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Mockito.doNothing().when(mockDatabaseHelper).insertTenantApplicationsRow(Mockito.anyString(),Mockito.anyString());
        Mockito.doNothing().when(mockDatabaseHelper).createSchema(Mockito.anyString());
        Mockito.doNothing().when(mockDatabaseHelper).createTable(Mockito.anyString());
        Mockito.doNothing().when(mockDatabaseHelper).grantSelectOnTable(Mockito.anyString(),Mockito.anyString());
        PowerMockito.whenNew(DatabaseHelper.class).withArguments(Mockito.any()).thenReturn(mockDatabaseHelper);

        //  Mock TransformHelper calls.
        XMLToSQLTransform mockXMLToSQLTransform = Mockito.mock(XMLToSQLTransform.class);
        Mockito.when(mockXMLToSQLTransform.getCreateDatabaseTableSQL(Mockito.anyString())).thenReturn("");
        PowerMockito.whenNew(XMLToSQLTransform.class).withArguments(Mockito.any()).thenReturn(mockXMLToSQLTransform);

        //  Mock KafkaScheduler calls.
        PowerMockito.mockStatic(KafkaScheduler.class);
        PowerMockito.doNothing().when(KafkaScheduler.class, "associateTopic", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        NewTenant newTenant = new NewTenant();
        newTenant.setTenantId("testtenant");

        List<String> applications = new ArrayList<>();
        applications.add("Application1");
        applications.add("Application2");
        newTenant.setApplication(applications);

        TenantAddPost.addTenant(newTenant);

        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).doesTableExist(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).getEventsXMLForApp(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).doesTenantApplicationsRowExist(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).insertTenantApplicationsRow(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).createSchema(Mockito.anyString());
        Mockito.verify(mockXMLToSQLTransform, Mockito.times(0)).getCreateDatabaseTableSQL(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).createTable(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).grantSelectOnTable(Mockito.anyString(),Mockito.anyString());

    }

    @Test(expected = Exception.class)
    public void testAddTenant_Failure_TenantAppsTableIsMissing () throws Exception {

        //  Mock AppConfig calls.
        AppConfig mockAppConfig = Mockito.mock(AppConfig.class);
        Mockito.when(mockAppConfig.getCAFAuditManagementDisable()).thenReturn("false");

        //  Mock ApiServiceUtil calls.
        AuditedApplication auditedApp = TestUtil.getAuditedApplication("TestApplication");
        PowerMockito.mockStatic(ApiServiceUtil.class);
        PowerMockito.when(ApiServiceUtil.getAppConfigProperties())
                .thenReturn(mockAppConfig);
        PowerMockito.when(ApiServiceUtil.getAuditedApplication(Mockito.any()))
                .thenReturn(auditedApp);

        //  Mock DatabaseHelper calls.
        DatabaseHelper mockDatabaseHelper = Mockito.mock(DatabaseHelper.class);
        Mockito.when(mockDatabaseHelper.doesTableExist(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        PowerMockito.whenNew(DatabaseHelper.class).withArguments(Mockito.any()).thenReturn(mockDatabaseHelper);

        NewTenant newTenant = new NewTenant();
        newTenant.setTenantId("testtenant");

        List<String> applications = new ArrayList<>();
        applications.add("Application1");
        applications.add("Application2");
        newTenant.setApplication(applications);

        TenantAddPost.addTenant(newTenant);

        Mockito.verify(mockDatabaseHelper, Mockito.times(1)).doesTableExist(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).getEventsXMLForApp(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).doesTenantApplicationsRowExist(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).insertTenantApplicationsRow(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).createSchema(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).createTable(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(0)).grantSelectOnTable(Mockito.anyString(),Mockito.anyString());

    }

    @Test(expected = BadRequestException.class)
    public void testAddTenant_Failure_InvalidTenantId_UpperCase () throws Exception {

        //  Mock AppConfig calls.
        AppConfig mockAppConfig = Mockito.mock(AppConfig.class);
        Mockito.when(mockAppConfig.getCAFAuditManagementDisable()).thenReturn("false");

        //  Mock ApiServiceUtil calls.
        AuditedApplication auditedApp = TestUtil.getAuditedApplication("TestApplication");
        PowerMockito.mockStatic(ApiServiceUtil.class);
        PowerMockito.when(ApiServiceUtil.getAppConfigProperties())
                .thenReturn(mockAppConfig);
        PowerMockito.when(ApiServiceUtil.getAuditedApplication(Mockito.any()))
                .thenReturn(auditedApp);

        NewTenant newTenant = new NewTenant();
        newTenant.setTenantId("testTenant1");

        List<String> applications = new ArrayList<>();
        applications.add("Application1");
        applications.add("Application2");
        newTenant.setApplication(applications);

        TenantAddPost.addTenant(newTenant);
    }

    @Test(expected = BadRequestException.class)
    public void testAddTenant_Failure_InvalidTenantId_Underscore () throws Exception {
        //  Mock AppConfig calls.
        AppConfig mockAppConfig = Mockito.mock(AppConfig.class);
        Mockito.when(mockAppConfig.getCAFAuditManagementDisable()).thenReturn("false");

        //  Mock ApiServiceUtil calls.
        AuditedApplication auditedApp = TestUtil.getAuditedApplication("TestApplication");
        PowerMockito.mockStatic(ApiServiceUtil.class);
        PowerMockito.when(ApiServiceUtil.getAppConfigProperties())
                .thenReturn(mockAppConfig);
        PowerMockito.when(ApiServiceUtil.getAuditedApplication(Mockito.any()))
                .thenReturn(auditedApp);

        NewTenant newTenant = new NewTenant();
        newTenant.setTenantId("testtenant_1");

        List<String> applications = new ArrayList<>();
        applications.add("Application1");
        applications.add("Application2");
        newTenant.setApplication(applications);

        TenantAddPost.addTenant(newTenant);
    }

}
