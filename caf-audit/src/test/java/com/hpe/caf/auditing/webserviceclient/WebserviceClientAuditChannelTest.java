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
import com.hpe.caf.auditing.internal.AuditNewEventFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class WebserviceClientAuditChannelTest {

    private static URL webserviceHttpURLConnection;

    @BeforeClass
    public static void setup() throws IOException {
        webserviceHttpURLConnection = new URL("http://testWsHost:8080/caf-audit-service/v1/audtevents");
        // Test the Auditing library in webservice mode
        System.setProperty("AUDIT_LIB_MODE", "webservice");
    }

    @Test
    public void testClose() throws Exception {
        WebserviceClientAuditChannel channel = new WebserviceClientAuditChannel(webserviceHttpURLConnection, null);
        channel.close();
    }

    @Test
    public void testCreateEventBuilder() throws Exception {
        WebserviceClientAuditChannel channel = new WebserviceClientAuditChannel(webserviceHttpURLConnection, null);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test
    public void testCreateEventBuilderWithAuditCoreMetadataProvider() throws Exception {
        AuditCoreMetadataProvider acmp = AuditNewEventFactory.createNewEvent();
        WebserviceClientAuditChannel channel = new WebserviceClientAuditChannel(webserviceHttpURLConnection, null);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder(acmp);
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test(expected = IOException.class)
    public void testWebserviceClientBadWebserviceHost() throws Exception {

        WebserviceClientAuditChannel channel = new WebserviceClientAuditChannel(webserviceHttpURLConnection, null);

        // Create new Audit Event Builder
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder();

        //  Send audit event message to Elasticsearch.
        auditEventBuilder.send();
    }

}
