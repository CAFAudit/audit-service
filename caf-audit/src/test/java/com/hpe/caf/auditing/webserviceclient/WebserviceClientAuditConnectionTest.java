/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Map;

public class WebserviceClientAuditConnectionTest {

    String testWebserviceHttpsEndpoint = "https://testWsHost:8080/caf-audit-service/v1";

    static class TestEnvironmentVariablesOverrider {
        @SuppressWarnings("unchecked")
        public static void configureEnvironmentVariable(String name, String value) throws Exception {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.put(name, value);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.put(name, value);
        }
    }

    @BeforeClass
    public static void setup() {
        // Test the Auditing library in webserviceclient mode
        System.setProperty("AUDIT_LIB_MODE", "webserviceclient");
    }

    @Test(expected = MalformedURLException.class)
    public void testWebserviceClientBadWebserviceEndpoint() throws Exception {

        String invalidWebserviceEndpoint = "thisIsNotAValidEndpointUrl";

        AuditConnectionHelper.getWebserviceAuditConnection(invalidWebserviceEndpoint);
    }

    @Test(expected = UnknownHostException.class)
    public void testWebserviceClientBadHttpsProxy() throws Exception {

        TestEnvironmentVariablesOverrider.configureEnvironmentVariable("no_proxy", "");
        TestEnvironmentVariablesOverrider.configureEnvironmentVariable("http_proxy", "");
        TestEnvironmentVariablesOverrider.configureEnvironmentVariable("https_proxy", "https://a-https-proxy:8081");

        AuditConnection auditConnection = AuditConnectionHelper.getWebserviceAuditConnection(testWebserviceHttpsEndpoint);
        AuditChannel auditChannel = auditConnection.createChannel();

        // Create new Audit Event Builder
        AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

        //  Send audit event message to Elasticsearch.
        auditEventBuilder.send();
    }

}
