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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElasticAuditConnection implements AuditConnection {
     private static final Logger LOG = LogManager.getLogger(ElasticAuditConnection.class.getName());

    private final RestHighLevelClient restHighLevelClient;
    private ElasticAuditIndexManager indexManager;

    public ElasticAuditConnection() throws AuditConfigurationException
    {
            final String hostAndPorts = 
                System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES,
                                   System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_AND_PORT_VALUES));
            final String hostValues = 
                System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_VALUES,
                                   System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_HOST_VALUES));
            final String port = 
                System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_PORT_VALUE,
                                   System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_PORT_VALUE));
            final StringBuilder hostAndPortBuilder = new StringBuilder();
            final String[] hostArray;
            
            if(hostAndPorts == null){
                hostArray = hostValues.split(",");
                if (hostArray.length == 0) {
                    final String errorMessage = "No hosts configured.";
                    LOG.error(errorMessage);
                    throw new AuditConfigurationException(errorMessage);
                }
                for (int index = 0; index < hostArray.length; index++){
                    hostAndPortBuilder.append(hostArray[index]).append(':') .append(port).append(',');
                }
            }
            
            String hostAndPortStr = "";
            if (hostAndPortBuilder.length() > 0) {
                hostAndPortStr = hostAndPortBuilder.substring(0, hostAndPortBuilder.length() - 1);

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
        restHighLevelClient = ElasticAuditRestHighLevelClientFactory.getHighLevelClient(hostAndPortStr);

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
