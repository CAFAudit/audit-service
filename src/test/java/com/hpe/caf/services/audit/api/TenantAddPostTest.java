package com.hpe.caf.services.audit.api;

import com.hpe.caf.services.audit.api.generated.model.NewTenant;
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
@PrepareForTest({TenantAddPost.class,KafkaScheduler.class})
public class TenantAddPostTest {

    @Test
    public void testAddTenant_Success () throws Exception {

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
        TransformHelper mockTransformHelper = Mockito.mock(TransformHelper.class);
        Mockito.when(mockTransformHelper.doCreateTableTransform(Mockito.any(), Mockito.anyString(), Mockito.anyString())).thenReturn("");
        PowerMockito.whenNew(TransformHelper.class).withNoArguments().thenReturn(mockTransformHelper);

        //  Mock KafkaScheduler calls.
        PowerMockito.mockStatic(KafkaScheduler.class);
        PowerMockito.doNothing().when(KafkaScheduler.class, "createScheduler", Mockito.any(), Mockito.anyString());
        PowerMockito.doNothing().when(KafkaScheduler.class, "associateTopic", Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        newEnv.put("CAF_DATABASE_URL","testUrl");
        newEnv.put("CAF_DATABASE_SERVICE_ACCOUNT","testServiceUser");
        newEnv.put("CAF_DATABASE_SERVICE_ACCOUNT_PASSWORD","testPassword");
        newEnv.put("CAF_DATABASE_LOADER_ACCOUNT","testLoaderUser");
        newEnv.put("CAF_DATABASE_LOADER_ACCOUNT_PASSWORD","testPassword");
        newEnv.put("CAF_DATABASE_READER_ROLE","testReaderRole");
        TestUtil.setSystemEnvironmentFields(newEnv);

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
        Mockito.verify(mockTransformHelper, Mockito.times(2)).doCreateTableTransform(Mockito.any(),Mockito.anyString(),Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).createTable(Mockito.anyString());
        Mockito.verify(mockDatabaseHelper, Mockito.times(2)).grantSelectOnTable(Mockito.anyString(),Mockito.anyString());

    }

    @Test(expected = Exception.class)
    public void testAddTenant_Failure_TenantAppsTableIsMissing () throws Exception {

        //  Mock DatabaseHelper calls.
        DatabaseHelper mockDatabaseHelper = Mockito.mock(DatabaseHelper.class);
        Mockito.when(mockDatabaseHelper.doesTableExist(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        PowerMockito.whenNew(DatabaseHelper.class).withArguments(Mockito.any()).thenReturn(mockDatabaseHelper);

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        newEnv.put("CAF_DATABASE_URL","testUrl");
        newEnv.put("CAF_DATABASE_SERVICE_ACCOUNT","testServiceUser");
        newEnv.put("CAF_DATABASE_SERVICE_ACCOUNT_PASSWORD","testPassword");
        newEnv.put("CAF_DATABASE_LOADER_ACCOUNT","testLoaderUser");
        newEnv.put("CAF_DATABASE_LOADER_ACCOUNT_PASSWORD","testPassword");
        newEnv.put("CAF_DATABASE_READER_ROLE","testReaderRole");
        TestUtil.setSystemEnvironmentFields(newEnv);

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
        NewTenant newTenant = new NewTenant();
        newTenant.setTenantId("testtenant_1");

        List<String> applications = new ArrayList<>();
        applications.add("Application1");
        applications.add("Application2");
        newTenant.setApplication(applications);

        TenantAddPost.addTenant(newTenant);
    }
}
