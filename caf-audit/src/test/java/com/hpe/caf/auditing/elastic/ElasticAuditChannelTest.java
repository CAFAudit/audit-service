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
package com.hpe.caf.auditing.elastic;

import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.internal.AuditNewEventFactory;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ElasticAuditChannelTest {

    private TransportClient mockTransportClient;
    private ElasticAuditIndexManager mockElasticAuditIndexManager;

    @Before
    public void setup() {
        mockTransportClient = Mockito.mock(TransportClient.class);
    }

    @Test
    public void testClose() throws Exception {
        ElasticAuditChannel channel = new ElasticAuditChannel(mockTransportClient, mockElasticAuditIndexManager);
        channel.close();
    }

    @Test
    public void testCreateEventBuilder() throws Exception {
        ElasticAuditChannel channel = new ElasticAuditChannel(mockTransportClient, mockElasticAuditIndexManager);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test
    public void testCreateEventBuilderWithAuditCoreMetadataProvider() throws Exception {
        AuditCoreMetadataProvider acmp = AuditNewEventFactory.createNewEvent();
        ElasticAuditChannel channel = new ElasticAuditChannel(mockTransportClient, mockElasticAuditIndexManager);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder(acmp);
        Assert.assertNotNull(auditEventBuilder);
    }

}
