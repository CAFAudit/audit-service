/*
 * Copyright 2015-2023 Open Text.
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
import com.hpe.caf.auditing.internal.AuditNewEventFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class WebServiceClientAuditChannelTest {

    private static URL webServiceHttpURLConnection;

    @BeforeClass
    public static void setup() throws IOException {
        webServiceHttpURLConnection = new URL("http://testWsHost:8080/caf-audit-service/v1/audtevents");
        // Test the Auditing library in webservice mode
        System.setProperty("CAF_AUDIT_MODE", "webservice");
    }

    @Test
    public void testCreateEventBuilder() throws Exception {
        WebServiceClientAuditChannel channel = new WebServiceClientAuditChannel(webServiceHttpURLConnection, null);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test
    public void testCreateEventBuilderWithAuditCoreMetadataProvider() throws Exception {
        AuditCoreMetadataProvider acmp = AuditNewEventFactory.createNewEvent();
        WebServiceClientAuditChannel channel = new WebServiceClientAuditChannel(webServiceHttpURLConnection, null);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder(acmp);
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test(expected = IOException.class)
    public void testWebserviceClientBadWebserviceHost() throws Exception {

        WebServiceClientAuditChannel channel = new WebServiceClientAuditChannel(webServiceHttpURLConnection, null);

        // Create new Audit Event Builder
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder();

        //  Send audit event message to Elasticsearch.
        auditEventBuilder.send();
    }

}
