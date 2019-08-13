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

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.exception.AuditConfigurationException;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class ElasticAuditConnection implements AuditConnection {

    private final RestHighLevelClient restHighLevelClient;
    private ElasticAuditIndexManager indexManager;

    public ElasticAuditConnection() throws AuditConfigurationException
    {
            final String hostValues = 
                System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_VALUES,
                                   System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_VALUES));
            final String port = 
                System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_PORT_VALUE,
                                   System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_PORT_VALUE));

            // Get the Elasticsearch number of shards per index from env var else default to '5'
            final int numberOfShards =
                getNumberFromSysPropertyOrEnvVariable(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_NUMBER_OF_SHARDS,
                                                      ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_NUMBER_OF_SHARDS);

            // Get the Elasticsearch number of replicas per shard from env var else default to '1'
            final int numberOfReplicas = 
                getNumberFromSysPropertyOrEnvVariable(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_NUMBER_OF_REPLICAS,
                                                      ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_NUMBER_OF_REPLICAS);

        //  Get Elasticsearch connection.
        restHighLevelClient = ElasticAuditRestHighLevelClientFactory.getHighLevelClient(hostValues, port);

        //  Get Elasticsearch index manager.
        indexManager = new ElasticAuditIndexManager(numberOfShards, numberOfReplicas, restHighLevelClient);
    }

    private static int getNumberFromSysPropertyOrEnvVariable(final String environmentVariable,
                                                             final int defaultTo) throws AuditConfigurationException {
        try {
            final String envVarValue = System.getProperty(environmentVariable, System.getenv(environmentVariable));
            if (envVarValue != null) {
                return Integer.parseInt(envVarValue);
            }
        } catch (final NumberFormatException nfe) {
            throw new AuditConfigurationException(environmentVariable + " environment variable should only contain " +
                    "numbers", nfe);
        }
        return defaultTo;
    }

    @Override
    public AuditChannel createChannel() throws IOException {
        //  Share the Elasticsearch client across channels.
        return new ElasticAuditChannel(restHighLevelClient, indexManager);
    }

    @Override
    public void close() throws Exception {
        restHighLevelClient.close();
        indexManager = null;
    }
}
