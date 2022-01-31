/*
 * Copyright 2015-2021 Micro Focus or one of its affiliates.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.internal.AuditNewEventFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.GetIndexTemplatesRequest;
import org.opensearch.client.indices.GetIndexTemplatesResponse;
import org.opensearch.client.indices.IndexTemplateMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;

public class ElasticAuditChannelTest {

    private RestHighLevelClient mockClient;
    private ObjectMapper objectMapper;

    @Before
    public void setup() throws IOException {
        objectMapper = Mockito.mock(ObjectMapper.class);
        final GetIndexTemplatesResponse response = Mockito.mock(GetIndexTemplatesResponse.class);
        final List<IndexTemplateMetadata> list = new ArrayList<>();
        final List<String> patterns = new ArrayList<>();
        patterns.add("*_audit");
        final IndexTemplateMetadata metadata = new IndexTemplateMetadata("caf-audit-template", 0, null, patterns, null, null, null);
        list.add(metadata);
        mockClient = Mockito.mock(RestHighLevelClient.class);
        final IndicesClient indiciesClient = Mockito.mock(IndicesClient.class);
        Mockito.when(mockClient.indices()).thenReturn(indiciesClient);
        Mockito.when(indiciesClient.getIndexTemplate(any(GetIndexTemplatesRequest.class), any())).thenReturn(response);
    }

    @Test
    public void testClose() throws Exception {
        ElasticAuditChannel channel = new ElasticAuditChannel(mockClient, objectMapper);
        channel.close();
    }

    @Test
    public void testCreateEventBuilder() throws Exception {
        ElasticAuditChannel channel = new ElasticAuditChannel(mockClient, objectMapper);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test
    public void testCreateEventBuilderWithAuditCoreMetadataProvider() throws Exception {
        AuditCoreMetadataProvider acmp = AuditNewEventFactory.createNewEvent();
        ElasticAuditChannel channel = new ElasticAuditChannel(mockClient, objectMapper);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder(acmp);
        Assert.assertNotNull(auditEventBuilder);
    }

}
