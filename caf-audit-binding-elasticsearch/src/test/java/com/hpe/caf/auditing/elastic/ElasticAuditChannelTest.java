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

import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.internal.AuditNewEventFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexTemplatesRequest;
import org.elasticsearch.client.indices.GetIndexTemplatesResponse;
import org.elasticsearch.client.indices.IndexTemplateMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;

public class ElasticAuditChannelTest {

    private RestHighLevelClient mockClient;

    @Before
    public void setup() throws IOException {
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
        ElasticAuditChannel channel = new ElasticAuditChannel(mockClient);
        channel.close();
    }

    @Test
    public void testCreateEventBuilder() throws Exception {
        ElasticAuditChannel channel = new ElasticAuditChannel(mockClient);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test
    public void testCreateEventBuilderWithAuditCoreMetadataProvider() throws Exception {
        AuditCoreMetadataProvider acmp = AuditNewEventFactory.createNewEvent();
        ElasticAuditChannel channel = new ElasticAuditChannel(mockClient);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder(acmp);
        Assert.assertNotNull(auditEventBuilder);
    }

}
