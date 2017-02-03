package com.hpe.caf.services.audit.client;

import com.hpe.caf.services.audit.client.api.ApplicationsApi;
import com.hpe.caf.services.audit.client.api.TenantsApi;
import com.hpe.caf.services.audit.client.model.NewTenant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by crooksph on 04/02/2016.
 */
public class ApiClientTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testApplicationsPostNullFile() throws Exception {
        ApplicationsApi client = new ApplicationsApi();

        exception.expect(ApiException.class);
        client.applicationsPost(null);
    }

    @Test
    public void testApplicationsPostNonexistentHost() throws Exception {
        File auditEventsDefFile = new File(getClass().getClassLoader().getResource("auditEventsDefinition.xml").getFile());

        ApplicationsApi client = new ApplicationsApi();
        client.getApiClient().setBasePath("http://non.existent.host/v1");

        exception.expect(Exception.class);
        client.applicationsPost(auditEventsDefFile);
    }

    @Test
    public void testTenantsPostNullNewTenant() throws Exception {
        TenantsApi client = new TenantsApi();

        List<String> applications = new ArrayList<String>();
        applications.add("TestApplication1");
        applications.add("TestApplication2");

        exception.expect(ApiException.class);
        client.tenantsPost(null);
    }

    @Test
    public void testTenantsPostNonexistentHost() throws Exception {
        TenantsApi client = new TenantsApi();

        String tenant = "TestTenant";
        List<String> applications = new ArrayList<String>();
        applications.add("TestApplication1");
        applications.add("TestApplication2");

        NewTenant newTenant = new NewTenant();
        newTenant.setTenantId(tenant);
        newTenant.setApplication(applications);

        exception.expect(Exception.class);
        client.tenantsPost(newTenant);
    }
}
