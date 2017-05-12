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

import java.net.UnknownHostException;

public class WebserviceClientAuditChannelTest {

    String testHostAndPort = "testHost:8080";

    @BeforeClass
    public static void setup() {
        // Test the Auditing library in webserviceclient mode
        System.setProperty("AUDIT_LIB_MODE", "webserviceclient");
    }

    @Test
    public void testClose() throws Exception {
        WebserviceClientAuditChannel channel = new WebserviceClientAuditChannel(testHostAndPort);
        channel.close();
    }

    @Test
    public void testCreateEventBuilder() throws Exception {
        WebserviceClientAuditChannel channel = new WebserviceClientAuditChannel(testHostAndPort);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test
    public void testCreateEventBuilderWithAuditCoreMetadataProvider() throws Exception {
        AuditCoreMetadataProvider acmp = AuditNewEventFactory.createNewEvent();
        WebserviceClientAuditChannel channel = new WebserviceClientAuditChannel(testHostAndPort);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder(acmp);
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test(expected = UnknownHostException.class)
    public void testUnknownHost() throws Exception {

        AuditConnection auditConnection = AuditConnectionHelper.getWebserviceAuditConnection(testHostAndPort);

        AuditChannel auditChannel = auditConnection.createChannel();

        // Create new Audit Event Builder
        AuditEventBuilder auditEventBuilder = auditChannel.createEventBuilder();

        //  Send audit event message to Elasticsearch.
        auditEventBuilder.send();
    }

}
