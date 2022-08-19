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
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.healthcheck.HealthResult;
import com.hpe.caf.auditing.healthcheck.HealthStatus;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.cluster.HealthRequest;
import org.opensearch.client.opensearch.cluster.HealthResponse;

public class ElasticAuditChannel implements AuditChannel {
    private static final Logger logger = LoggerFactory.getLogger(ElasticAuditChannel.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenSearchClient openSearchClient;

    public ElasticAuditChannel(OpenSearchClient openSearchClient){
        this.openSearchClient = openSearchClient;
    }

    @Override
    public void declareApplication(String applicationId) throws IOException {
        // Do nothing.
    }

    @Override
    public AuditEventBuilder createEventBuilder(AuditCoreMetadataProvider coreMetadataProvider) {
        return new ElasticAuditEventBuilder(openSearchClient, coreMetadataProvider);
    }

    @Override
    public HealthResult healthCheck()
    {
        //Calling to OpenSearch to get health status
        final HealthResponse response;
        try {
            logger.debug("Executing ES cluster health check...");
            final HealthRequest clusterHealthRequest = new HealthRequest.Builder()
                .waitForStatus(org.opensearch.client.opensearch._types.HealthStatus.Yellow)
                .build();
            response = openSearchClient.cluster().health(clusterHealthRequest);
            if(response.status().equals(org.opensearch.client.opensearch._types.HealthStatus.Red)){
                logger.error("OpenSearch is unhealthy.");
                return new HealthResult(HealthStatus.UNHEALTHY, "OpenSearch Status is invalid: " + response.status().toString());
            }else{
                return HealthResult.HEALTHY;
            }
        } catch (final IOException ex) {
            logger.error("Error executing cluster health check request.", ex);
            return new HealthResult(HealthStatus.UNHEALTHY, "OpenSearch cluster is unhealthy");
        }
    }

    @Override
    public void close() throws Exception {
        // Do nothing.
    }
}
