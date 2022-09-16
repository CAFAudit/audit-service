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
import java.util.Map;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch.indices.ExistsIndexTemplateRequest;
import org.opensearch.client.opensearch.indices.ExistsTemplateRequest;
import org.opensearch.client.opensearch.indices.PutTemplateRequest;

public final class ElasticAuditIndexManager {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticAuditIndexManager.class.getName());
    private static final String INDEX_TEMPLATE_NAME = "caf-audit-template";

    private ElasticAuditIndexManager(){}

    public static void createTemplate(final int numberOfShards, final int numberOfReplicas,
                                           final OpenSearchClient openSearchClient, final boolean isForceIndexTemplateUpdate)
        throws IOException
    {
        if (isForceIndexTemplateUpdate || !isTemplatePresent(openSearchClient)) {
      
            final Map<String, JsonData> indexSettings  = Map.of("number_of_shards", JsonData.of(numberOfShards),
                                                          "number_of_replicas", JsonData.of(numberOfReplicas));
            final PutTemplateRequest request = new PutTemplateRequest.Builder()
                .name(INDEX_TEMPLATE_NAME)
                .indexPatterns("*" + ElasticAuditConstants.Index.SUFFIX)
                .settings(indexSettings)
                .mappings(getTenantTypeMapping())
                .build();

            openSearchClient.indices().putTemplate(request);
        }
    }

    private static boolean isTemplatePresent(final OpenSearchClient openSearchClient) throws IOException
    {
        final ExistsTemplateRequest request = new ExistsTemplateRequest.Builder().name(INDEX_TEMPLATE_NAME).build();
        return openSearchClient.indices().existsTemplate(request).value();
    }

    private static TypeMapping getTenantTypeMapping() {
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
