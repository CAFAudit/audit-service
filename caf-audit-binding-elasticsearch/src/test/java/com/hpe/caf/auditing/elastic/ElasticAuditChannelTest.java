/*
 * Copyright 2015-2024 Open Text.
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.GetIndexTemplateRequest;
import org.opensearch.client.opensearch.indices.GetIndexTemplateResponse;
import org.opensearch.client.opensearch.indices.OpenSearchIndicesClient;
import org.opensearch.client.opensearch.indices.get_index_template.IndexTemplateItem;

public class ElasticAuditChannelTest {

    private OpenSearchClient mockOpenSearchClient;

    @Before
    public void setup() throws IOException {
        final GetIndexTemplateResponse response = Mockito.mock(GetIndexTemplateResponse.class);
        final List<IndexTemplateItem> list = new ArrayList<>();
        final List<String> patterns = new ArrayList<>();
        patterns.add("*_audit");
        final IndexTemplateItem metadata = new IndexTemplateItem.Builder()
            .name("caf-audit-template")
            .indexTemplate(t -> t.priority(0L).indexPatterns(patterns).composedOf("test"))
            .build();
        list.add(metadata);
        mockOpenSearchClient = Mockito.mock(OpenSearchClient.class);
        final OpenSearchIndicesClient indiciesClient = Mockito.mock(OpenSearchIndicesClient.class);
        Mockito.when(mockOpenSearchClient.indices()).thenReturn(indiciesClient);
        Mockito.when(indiciesClient.getIndexTemplate(any(GetIndexTemplateRequest.class))).thenReturn(response);
    }

    @Test
    public void testClose() throws Exception {
        ElasticAuditChannel channel = new ElasticAuditChannel(mockOpenSearchClient);
        channel.close();
    }

    @Test
    public void testCreateEventBuilder() throws Exception {
        ElasticAuditChannel channel = new ElasticAuditChannel(mockOpenSearchClient);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        Assert.assertNotNull(auditEventBuilder);
    }

    @Test
    public void testCreateEventBuilderWithAuditCoreMetadataProvider() throws Exception {
        AuditCoreMetadataProvider acmp = AuditNewEventFactory.createNewEvent();
        ElasticAuditChannel channel = new ElasticAuditChannel(mockOpenSearchClient);
        AuditEventBuilder auditEventBuilder = channel.createEventBuilder(acmp);
        Assert.assertNotNull(auditEventBuilder);
    }

}
