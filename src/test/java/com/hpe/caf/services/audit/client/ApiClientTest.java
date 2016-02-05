package com.hpe.caf.services.audit.client;

import com.hpe.caf.services.audit.client.api.DefaultApi;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

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
}
