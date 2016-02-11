import com.hpe.caf.services.audit.client.ApiClient;
import com.hpe.caf.services.audit.client.api.DefaultApi;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by crooksph on 05/02/2016.
 */
public class AuditManagementWebServiceIT {

    private String connectionString;
    ApiClient client = new ApiClient();
    DefaultApi auditManagementClient;


    @Before
    public void setup() {
        connectionString = System.getenv("webserviceurl");
        client.setBasePath(connectionString);
        auditManagementClient = new DefaultApi(client);
    }


    @Test
    public void testApplicationPost() throws Exception {
        File auditEventsDefFile = new File(getClass().getClassLoader().getResource("auditEventsDefinition.xml").getFile());

        auditManagementClient.applicationsPost(auditEventsDefFile);
    }
}
