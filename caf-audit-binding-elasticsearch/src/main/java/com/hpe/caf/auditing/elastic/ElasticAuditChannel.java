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

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.healthcheck.HealthResult;
import com.hpe.caf.auditing.healthcheck.HealthStatus;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;

import java.io.IOException;

public class ElasticAuditChannel implements AuditChannel {

    private final RestHighLevelClient restHighLevelClient;

    public ElasticAuditChannel(RestHighLevelClient restHighLevelClient){
        this.restHighLevelClient = restHighLevelClient;
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
        final ClusterHealthRequest request = new ClusterHealthRequest().waitForYellowStatus();
        try {
            final ClusterHealthResponse health = restHighLevelClient.cluster().health(request, RequestOptions.DEFAULT);
            if(health.getStatus() == ClusterHealthStatus.RED) {
                return new HealthResult(HealthStatus.UNHEALTHY, "Elasticsearch cluster is unhealthy");
            }
        } catch (final IOException e) {
            return new HealthResult(HealthStatus.UNHEALTHY, "Error checking Elasticsearch status: "
                    +e.getMessage());
        }
        return HealthResult.HEALTHY;
    }

    @Override
    public void close() throws Exception {
        // Do nothing.
    }
}
