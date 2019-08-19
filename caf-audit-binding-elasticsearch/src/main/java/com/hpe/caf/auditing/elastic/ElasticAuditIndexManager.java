/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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

import com.google.common.io.ByteStreams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import org.elasticsearch.client.indices.GetIndexTemplatesRequest;
import org.elasticsearch.client.indices.GetIndexTemplatesResponse;

public class ElasticAuditIndexManager {

    private static final Logger LOG = LogManager.getLogger(ElasticAuditIndexManager.class.getName());
    private static final String INDEX_TEMPLATE_NAME = "caf_audit_template";

    private final RestHighLevelClient restHighLevelClient;

    private final int numberOfShards;
    private final int numberOfReplicas;

    public ElasticAuditIndexManager(final int numberOfShards, final int numberOfReplicas, RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
        this.numberOfShards = numberOfShards;
        this.numberOfReplicas = numberOfReplicas;
    }
    public void createIndexTemplate()
    {
        try {
            if (isIndexTemplatePresent()) {
                return;
            }
            final PutIndexTemplateRequest request = new PutIndexTemplateRequest(INDEX_TEMPLATE_NAME);
            //  Configure the number of shards and replicas the new index should have.
            final Settings indexSettings = Settings.builder()
                .put("number_of_shards", numberOfShards)
                .put("number_of_replicas", numberOfReplicas)
                .build();
            request.settings(indexSettings);
            request.mapping(getTenantIndexTypeMappingsBuilder());
            request.version();
            restHighLevelClient.indices().putTemplate(request, RequestOptions.DEFAULT);
        } catch (final IOException ex) {
            LOG.error("An error occured contacting elasticsearch: ", ex);
        }
    }
    
    private boolean isIndexTemplatePresent() throws IOException
    {
        final String forceUpdate = System.getenv("CAF_FORCE_TEMPLATE_UPDATE");
        if (forceUpdate != null && Boolean.getBoolean(forceUpdate)) {
            return false;
        }
        final GetIndexTemplatesRequest request = new GetIndexTemplatesRequest(INDEX_TEMPLATE_NAME);
        final GetIndexTemplatesResponse indexTemplate = restHighLevelClient.indices().getIndexTemplate(request, RequestOptions.DEFAULT);
        return indexTemplate.getIndexTemplates().stream().anyMatch(template -> template.name().equals(INDEX_TEMPLATE_NAME));
    }

    private XContentBuilder getTenantIndexTypeMappingsBuilder() {
        //  Get the contents of the index mapping file and assign to byte array before attempting to parse JSON
        final byte[] cafAuditEventTenantIndexMappingsBytes;
        try (final InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(ElasticAuditConstants.Index.TYPE_MAPPING_RESOURCE)){
            if(inputStream== null)
            {
                final String errorMessage = "Unable to read bytes from " + ElasticAuditConstants.Index.TYPE_MAPPING_RESOURCE;
                LOG.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            cafAuditEventTenantIndexMappingsBytes = ByteStreams.toByteArray(inputStream);
        } catch (IOException e) {
            String errorMessage = "Unable to read bytes from " + ElasticAuditConstants.Index.TYPE_MAPPING_RESOURCE;
            LOG.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }

        //  Parse JSON from the bytes and return as a mapping builder
        try {
            XContentParser parser = XContentFactory.xContent(XContentType.JSON)
                    .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                            cafAuditEventTenantIndexMappingsBytes);
            parser.close();
            return XContentFactory.jsonBuilder().copyCurrentStructure(parser);
        } catch (IOException e) {
            String errorMessage = "Unable to parse JSON from " + ElasticAuditConstants.Index.TYPE_MAPPING_RESOURCE;
            LOG.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
