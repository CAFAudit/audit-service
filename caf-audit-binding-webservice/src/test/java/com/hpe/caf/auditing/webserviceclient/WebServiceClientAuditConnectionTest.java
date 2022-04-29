/*
 * Copyright 2015-2022 Micro Focus or one of its affiliates.
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
package com.hpe.caf.auditing.webserviceclient;

import com.hpe.caf.auditing.*;
import com.hpe.caf.auditing.exception.AuditConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import java.net.UnknownHostException;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

public class WebServiceClientAuditConnectionTest {
    private static final String TEST_WEB_SERVICE_HTTPS_ENDPOINT = "https://testWsHost:8080/caf-audit-service/v1";
    private static final String NO_PROXY = "NO_PROXY";
    private static final String HTTP_PROXY = "HTTP_PROXY";
    private static final String HTTPS_PROXY = "HTTPS_PROXY";

    @BeforeClass
    public static void setup() {
        // Test the Auditing library in webservice mode
        System.setProperty("CAF_AUDIT_MODE", "webservice");
    }

    @Test(expected = AuditConfigurationException.class)
    public void testWebServiceClientBadWebserviceEndpoint() throws Exception {

        String invalidWebServiceEndpoint = "thisIsNotAValidEndpointUrl";
        System.setProperty("CAF_AUDIT_WEBSERVICE_ENDPOINT_URL", invalidWebServiceEndpoint);
        AuditConnectionFactory.createConnection();
    }

    @Test(expected = UnknownHostException.class)
    public void testWebServiceClientUnknownHttpsProxy() throws Exception {
        withEnvironmentVariable(NO_PROXY, "")
            .and(HTTP_PROXY, "")
            .and(HTTPS_PROXY, "https://a-https-proxy:8081")
            .execute(() -> {
                System.setProperty("CAF_AUDIT_WEBSERVICE_ENDPOINT_URL", TEST_WEB_SERVICE_HTTPS_ENDPOINT);
                AuditConnection auditConnection = AuditConnectionFactory.createConnection();
                AuditChannel auditChannel = auditConnection.createChannel();

                // Create new Audit Event Builder
                AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

                //  Send audit event message to Elasticsearch.
                auditEventBuilder.send();
            });
    }

    @Test(expected = AuditConfigurationException.class)
    public void testWebServiceClientMalformedHttpsProxy() throws Exception {
        withEnvironmentVariable(NO_PROXY, "")
            .and(HTTP_PROXY, "")
            .and(HTTPS_PROXY, "notAValidUrl")
            .execute(() -> {
                System.setProperty("CAF_AUDIT_WEBSERVICE_ENDPOINT_URL", TEST_WEB_SERVICE_HTTPS_ENDPOINT);
                AuditConnectionFactory.createConnection();
            });
    }

    @Test(expected = AuditConfigurationException.class)
    public void testWebServiceClientMalformedHttpProxy() throws Exception {
        String testWebServiceHttpEndpoint = "http://testWsHost:8080/caf-audit-service/v1";
        withEnvironmentVariable(NO_PROXY, "")
            .and(HTTP_PROXY, "notAValidUrl")
            .and(HTTPS_PROXY, "")
            .execute(() -> {
                System.setProperty("CAF_AUDIT_WEBSERVICE_ENDPOINT_URL", testWebServiceHttpEndpoint);
                AuditConnectionFactory.createConnection();
            });

    }
}
