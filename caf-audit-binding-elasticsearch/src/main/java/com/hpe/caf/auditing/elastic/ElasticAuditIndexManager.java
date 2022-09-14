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
package com.hpe.caf.auditing.elastic;

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.ExistsIndexTemplateRequest;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.opensearch.client.opensearch.indices.PutIndexTemplateRequest;
import org.opensearch.client.opensearch.indices.put_index_template.IndexTemplateMapping;

public final class ElasticAuditIndexManager {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticAuditIndexManager.class.getName());
    private static final String INDEX_TEMPLATE_NAME = "caf-audit-template";

    private ElasticAuditIndexManager(){}

    public static void createIndexTemplate(final int numberOfShards, final int numberOfReplicas,
                                           final OpenSearchClient openSearchClient, final boolean isForceIndexTemplateUpdate)
        throws IOException
    {
        if (isForceIndexTemplateUpdate || !isIndexTemplatePresent(openSearchClient)) {

            final IndexSettings indexSettings = new IndexSettings.Builder()
                .numberOfShards(String.valueOf(numberOfShards))
                .numberOfReplicas(String.valueOf(numberOfReplicas))
                .build();

            final IndexTemplateMapping indexMapping = new IndexTemplateMapping.Builder()
                .settings(indexSettings)
                .mappings(getTenantIndexTypeMapping())
                .build();

            final PutIndexTemplateRequest request = new PutIndexTemplateRequest.Builder()
                .name(INDEX_TEMPLATE_NAME)
                .indexPatterns("*" + ElasticAuditConstants.Index.SUFFIX)
                .template(indexMapping)
                .build();

            openSearchClient.indices().putIndexTemplate(request);
        }
    }

    private static boolean isIndexTemplatePresent(final OpenSearchClient openSearchClient) throws IOException
    {
        final ExistsIndexTemplateRequest request = new ExistsIndexTemplateRequest.Builder().name(INDEX_TEMPLATE_NAME).build();
        return openSearchClient.indices().existsIndexTemplate(request).value();
    }

    private static TypeMapping getTenantIndexTypeMapping() {
        //  Get the contents of the index mapping file and assign to TypeMapping
        try (final InputStream inputStream = ElasticAuditIndexManager.class.getClassLoader()
            .getResourceAsStream(ElasticAuditConstants.Index.TYPE_MAPPING_RESOURCE);
             final JsonParser jsonValueParser = Json.createParser(inputStream)) {
            if (inputStream == null)
            {
                final String errorMessage = "Unable to read bytes from " + ElasticAuditConstants.Index.TYPE_MAPPING_RESOURCE;
                LOG.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            return TypeMapping._DESERIALIZER.deserialize(jsonValueParser, new JacksonJsonpMapper());

        } catch (final IOException e) {
            String errorMessage = "Unable to read bytes from " + ElasticAuditConstants.Index.TYPE_MAPPING_RESOURCE;
            LOG.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
