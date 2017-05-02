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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.ByteStreams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.*;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticAuditIndexManager {

    private static final Logger LOG = LogManager.getLogger(ElasticAuditIndexManager.class.getName());

    private final TransportClient transportClient;
    private final LoadingCache<String, String> indexCache;
    private final ElasticAuditConfiguration config;

    private static final String indexTypeName = "cafAuditEvent";

    public ElasticAuditIndexManager(ElasticAuditConfiguration config, TransportClient transportClient) {
        this.transportClient = transportClient;
        this.config = config;

        //  Configure in memory cache to hold list of Elasticsearch indexes already created.
        indexCache = CacheBuilder
                .newBuilder()
                .build(
                        //  CacheLoader used to create new index and load into cache if not already cached.
                        new CacheLoader<String, String>() {
                            public String load(String indexName) {
                                createIndex(indexName);
                                return indexName;
                            }
                        }
                );
    }

    /**
     *  Method used to query the index cache.
     */
    public String getIndex(String indexName){
        return indexCache.getUnchecked(indexName);
    }

    /**
     *  Returns immediately if index already exists in Elasticsearch, otherwise it creates a new index in Elasticsearch
     *  with type mappings.
     */
    private void createIndex(String indexName){
        final String indexAlreadyExistsMessage = "Index " + indexName + " already exists";

        //  Return immediately if index already exists. Otherwise, create a new one.
        if (indexExists(indexName)) {
            LOG.debug(indexAlreadyExistsMessage);
            return;
        }

        //  Create a new index as it does not currently exist.
        LOG.debug("Creating a new index in Elasticsearch.");

        //  Configure the number of shards and replicas the new index should have.
        Settings indexSettings = Settings.builder()
                .put("number_of_shards", config.getNumberOfShards())
                .put("number_of_replicas", config.getNumberOfReplicas())
                .build();
        CreateIndexRequest indexRequest = new CreateIndexRequest(indexName, indexSettings);

        String cafAuditEventTenantIndexMappingsFileName = "CafAuditEventTenantIndexMappings.json";
        //  Get the contents of the index mapping file and assign to byte array before attempting to parse JSON
        byte[] cafAuditEventTenantIndexMappingsBytes;
        try {
            cafAuditEventTenantIndexMappingsBytes = ByteStreams.toByteArray(getClass().getClassLoader()
                    .getResourceAsStream(cafAuditEventTenantIndexMappingsFileName));
        } catch (IOException e) {
            String errorMessage = "Unable to read bytes from " + cafAuditEventTenantIndexMappingsFileName;
            LOG.error(errorMessage);
            throw new RuntimeException(e);
        }

        //  Parse JSON from the bytes and assign to the mapping builder
        XContentBuilder mappingBuilder;
        try {
            XContentParser parser = XContentFactory.xContent(XContentType.JSON)
                    .createParser(NamedXContentRegistry.EMPTY, cafAuditEventTenantIndexMappingsBytes);
            parser.close();
            mappingBuilder = jsonBuilder().copyCurrentStructure(parser);
        } catch (IOException e) {
            String errorMessage = "Unable to parse JSON from " + cafAuditEventTenantIndexMappingsFileName;
            LOG.error(errorMessage);
            throw new RuntimeException(e);
        }

        //  Add the type mappings to the index request
        indexRequest.mapping(indexTypeName, mappingBuilder);

        try {
            //  Use IndicesAdminClient to create the new index. This operation
            //  needs to be acknowledged.
            if (transportClient.admin().indices().create(indexRequest).actionGet().isAcknowledged()) {
                //  Index creation has been acknowledged.
                LOG.debug("Index " + indexName + " has been created");
            } else {
                //  Index creation has not been acknowledged. Adding further check here in case it has
                //  been created by another thread.
                if (!indexExists(indexName)){
                    String errorMessage = "Failed to create index " + indexName;
                    LOG.error(errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
            }
        } catch (ResourceAlreadyExistsException raee) {
            //  Index already exists. Just ignore.
            LOG.debug(indexAlreadyExistsMessage, raee);
        }
    }

    private boolean indexExists(final String indexName) {
        return transportClient.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
    }
}
