package com.hpe.caf.services.audit.client;

import com.hpe.caf.services.audit.client.api.DefaultApi;
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
        DefaultApi client = new DefaultApi();

        exception.expect(ApiException.class);
        client.applicationsPost(null);
    }

    @Test
    public void testApplicationsPostNonexistentHost() throws Exception {
        File auditEventsDefFile = new File(getClass().getClassLoader().getResource("auditEventsDefinition.xml").getFile());

        DefaultApi client = new DefaultApi();
        client.getApiClient().setBasePath("http://non.existent.host/v1");

        exception.expect(Exception.class);
        client.applicationsPost(auditEventsDefFile);
    }

    @Test
    public void testTenantsPostNullTenant() throws Exception {
        DefaultApi client = new DefaultApi();

        List<String> applications = new ArrayList<String>();
        applications.add("TestApplication1");
        applications.add("TestApplication2");

        exception.expect(ApiException.class);
        client.tenantsPost(null,applications);
    }

    @Test
    public void testTenantsPostNullApplication() throws Exception {
        DefaultApi client = new DefaultApi();
        client.getApiClient().setBasePath("http://non.existent.host/v1");

        String tenant = "TestTenant";

        exception.expect(ApiException.class);
        client.tenantsPost(tenant, null);
    }

    @Test
    public void testTenantsPostNonexistentHost() throws Exception {
        DefaultApi client = new DefaultApi();

        String tenant = "TestTenant";
        List<String> applications = new ArrayList<String>();
        applications.add("TestApplication1");
        applications.add("TestApplication2");

        exception.expect(Exception.class);
        client.tenantsPost(tenant, applications);
    }
}
