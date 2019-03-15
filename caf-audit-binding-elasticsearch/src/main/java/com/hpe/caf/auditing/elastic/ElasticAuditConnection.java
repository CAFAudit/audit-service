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
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;

public class ElasticAuditConnection implements AuditConnection {

    private final TransportClient transportClient;
    private ElasticAuditIndexManager indexManager;

    public ElasticAuditConnection() throws AuditConfigurationException
    {
            final String hostAndPorts = 
                System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES,
                                   System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES));

            // Get the Elasticsearch cluster name from env var else default to "elasticsearch-cluster"
            String clusterName = System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_CLUSTER_NAME,
                                                    System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_CLUSTER_NAME));
            if (clusterName == null) {
                clusterName = ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_CLUSTER_NAME;
            }

            // Get the Elasticsearch number of shards per index from env var else default to '5'
            final int numberOfShards =
                getNumberFromSysPropertyOrEnvVariable(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_NUMBER_OF_SHARDS,
                                                      ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_NUMBER_OF_SHARDS);

            // Get the Elasticsearch number of replicas per shard from env var else default to '1'
            final int numberOfReplicas = 
                getNumberFromSysPropertyOrEnvVariable(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_NUMBER_OF_REPLICAS,
                                                      ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_NUMBER_OF_REPLICAS);

        //  Get Elasticsearch connection.
        transportClient = ElasticAuditTransportClientFactory.getTransportClient(hostAndPorts, clusterName);

        //  Get Elasticsearch index manager.
        indexManager = new ElasticAuditIndexManager(numberOfShards, numberOfReplicas, transportClient);
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
        //  Share the Elasticsearch transport client across channels.
        return new ElasticAuditChannel(transportClient, indexManager);
    }

    @Override
    public void close() throws Exception {
        transportClient.close();
        indexManager = null;
    }
}
