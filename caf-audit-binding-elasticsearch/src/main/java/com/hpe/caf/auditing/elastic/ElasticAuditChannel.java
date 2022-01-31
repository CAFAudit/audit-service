/*
 * Copyright 2015-2021 Micro Focus or one of its affiliates.
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.healthcheck.HealthResult;
import com.hpe.caf.auditing.healthcheck.HealthStatus;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticAuditChannel implements AuditChannel {
private static final Logger logger = LoggerFactory.getLogger(ElasticAuditChannel.class);
    private final RestHighLevelClient restHighLevelClient;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ElasticAuditChannel(RestHighLevelClient restHighLevelClient, ObjectMapper objectMapper){
        this.restHighLevelClient = restHighLevelClient;
        this.restClient = restHighLevelClient.getLowLevelClient();
        this.objectMapper = objectMapper;
    }

    @Override
    public void declareApplication(String applicationId) throws IOException {
        // Do nothing.
    }

    @Override
    public AuditEventBuilder createEventBuilder(AuditCoreMetadataProvider coreMetadataProvider) {
        return new ElasticAuditEventBuilder(restHighLevelClient, coreMetadataProvider);
    }

    @Override
    public HealthResult healthCheck()
    {
        //Calling to OpenSearch to get health status
        final Response response;
        try {
            logger.debug("Executing ES cluster health check...");
            final String endPoint = "_cluster/health";
            final Request healthRequest = new Request("GET", endPoint);
            healthRequest.addParameter("wait_for_status", "yellow");
            response = restClient.performRequest(healthRequest);
        } catch (final IOException ex) {
            logger.error("Error executing cluster health check request.", ex);
            return new HealthResult(HealthStatus.UNHEALTHY, "Elasticsearch cluster is unhealthy");
        }
        return healthResponse(response.getEntity());
    }

    private HealthResult healthResponse(final HttpEntity httpEntity){
        String healthResponse = "";
        try{
            healthResponse = EntityUtils.toString(httpEntity);
        }catch (Exception e){
            if(!validString(healthResponse)){
                logger.error("HealthCheck response could not be processed", e);
                return new HealthResult(HealthStatus.UNHEALTHY, "HealthCheck response could not be processed");
            }
            return new HealthResult(HealthStatus.UNHEALTHY, "Elasticsearch cluster is unhealthy");
        }

        final String status;
        try {
            status = readTree(objectMapper, healthResponse).get("status").asText();
        } catch (Exception ex) {
            logger.error("HealthCheck response could not be processed", ex);
            return new HealthResult(HealthStatus.UNHEALTHY, "HealthCheck response could not be processed");
        }

        logger.debug("Got ES status : {}", status);
        if (status.equals("red")) {
            logger.error("Elasticsearch is unhealthy.");
           return new HealthResult(HealthStatus.UNHEALTHY, "Opensearch Status is invalid: " + status);
        }
        return HealthResult.HEALTHY;
    }

    private JsonNode readTree(final ObjectMapper objectMapper, final String content) throws JsonProcessingException, IOException
    {
        try {
            return objectMapper.readTree(content);
        } catch (final JsonProcessingException ex) {
            throw ex;
        } catch (final IOException ex) {
            throw new IOException(ex);
        }
    }

    private boolean validString(final String s){
        if((s.isEmpty()) || (s.equals("")) || (s==null)){
            return false;
        }
        return true;
    }

    @Override
    public void close() throws Exception {
        // Do nothing.
    }
}
