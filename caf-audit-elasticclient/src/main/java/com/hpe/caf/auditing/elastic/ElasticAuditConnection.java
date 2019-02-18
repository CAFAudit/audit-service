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

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;

public class ElasticAuditConnection implements AuditConnection {

    private TransportClient transportClient;
    private ElasticAuditIndexManager indexManager;

    public ElasticAuditConnection() {

    }

    private static int getNumberFromSysPropertyOrEnvVariable(final String environmentVariable,
                                                             final int defaultTo) throws ConfigurationException {
        try {
            final String envVarValue = System.getProperty(environmentVariable, System.getenv(environmentVariable));
            if (envVarValue != null) {
                return Integer.parseInt(envVarValue);
            }
        } catch (final NumberFormatException nfe) {
            throw new ConfigurationException(environmentVariable + " environment variable should only contain " +
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
    public void initialize(final ConfigurationSource configSource) throws ConfigurationException
    {
        //  Get Elasticsearch configuration.
        final ElasticAuditConfiguration config;
        if (configSource == null) {
            config = new ElasticAuditConfiguration();

            // Get the Elasticsearch host and port values from env var
            config.setHostAndPortValues(
                System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES,
                                   System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES)));

            // Get the Elasticsearch cluster name from env var else default to "elasticsearch-cluster"
            String clusterName = System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_CLUSTER_NAME,
                                                    System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_CLUSTER_NAME));
            if (clusterName == null) {
                clusterName = ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_CLUSTER_NAME;
            }
            config.setClusterName(clusterName);

            // Get the Elasticsearch number of shards per index from env var else default to '5'
            config.setNumberOfShards(
                getNumberFromSysPropertyOrEnvVariable(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_NUMBER_OF_SHARDS,
                                                      ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_NUMBER_OF_SHARDS));

            // Get the Elasticsearch number of replicas per shard from env var else default to '1'
            config.setNumberOfReplicas(
                getNumberFromSysPropertyOrEnvVariable(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_NUMBER_OF_REPLICAS,
                                                      ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_NUMBER_OF_REPLICAS));

        } else {
            config = configSource.getConfiguration(ElasticAuditConfiguration.class);
        }

        //  Get Elasticsearch connection.
        transportClient = ElasticAuditTransportClientFactory.getTransportClient(config.getHostAndPortValues(),
                                                                                config.getClusterName());

        //  Get Elasticsearch index manager.
        indexManager = new ElasticAuditIndexManager(config, transportClient);
    }

    @Override
    public void close() throws Exception {
        transportClient.close();
        indexManager = null;
    }
}
