/*
 * Copyright 2015-2020 Micro Focus or one of its affiliates.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticAuditConnection implements AuditConnection {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticAuditConnection.class.getName());

    private final RestHighLevelClient restHighLevelClient;
    private final int numberOfShards;
    private final int numberOfReplicas;
    private final boolean isForceIndexTemplateUpdate;
    private boolean isIndexTemplateCreated = false;

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
            
            if(hostAndPorts == null){
                final String[] hostArray = hostValues.split(",");
                if (hostArray.length == 0) {
                    final String errorMessage = "No hosts configured.";
                    LOG.error(errorMessage);
                    throw new AuditConfigurationException(errorMessage);
                }
                for (int index = 0; index < hostArray.length; index++){
                    hostAndPortBuilder.append(hostArray[index]).append(':').append(port).append(',');
                }
            }
            
            final String hostAndPortsStr;
            if (hostAndPortBuilder.length() > 0) {
                hostAndPortsStr = hostAndPortBuilder.substring(0, hostAndPortBuilder.length() - 1);
            }else{
                hostAndPortsStr = hostAndPorts;
            }
        // Get the Elasticsearch number of shards per index from env var else default to '5'
        numberOfShards = getNumberFromSysPropertyOrEnvVariable(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_NUMBER_OF_SHARDS,
                                                               ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_NUMBER_OF_SHARDS);

        // Get the Elasticsearch number of replicas per shard from env var else default to '1'
        numberOfReplicas = getNumberFromSysPropertyOrEnvVariable(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_NUMBER_OF_REPLICAS,
                                                                 ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_NUMBER_OF_REPLICAS);
        final String forceUpdate = System.getenv("CAF_AUDIT_FORCE_INDEX_TEMPLATE_UPDATE");
        isForceIndexTemplateUpdate = forceUpdate != null ? Boolean.parseBoolean(forceUpdate) : false;

        //  Get Elasticsearch connection.
        restHighLevelClient = ElasticAuditRestHighLevelClientFactory.getHighLevelClient(getElasticProtocol(), hostAndPortsStr);
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
        if (!isIndexTemplateCreated) {
            //Create index template.
            ElasticAuditIndexManager.createIndexTemplate(numberOfShards, numberOfReplicas, restHighLevelClient,
                                                         isForceIndexTemplateUpdate);
            isIndexTemplateCreated = true;
        }
        //  Share the Elasticsearch client across channels.
        return new ElasticAuditChannel(restHighLevelClient);
    }

    private static String getElasticProtocol()
    {
        final String elasticProtocol
            = System.getProperty(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_PROTOCOL,
                                 System.getenv(ElasticAuditConstants.ConfigEnvVar.CAF_ELASTIC_PROTOCOL));

        return (elasticProtocol == null)
            ? ElasticAuditConstants.ConfigDefault.CAF_ELASTIC_PROTOCOL
            : elasticProtocol;
    }

    @Override
    public void close() throws Exception {
        restHighLevelClient.close();
    }
}
