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

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;

public class ElasticAuditConnection implements AuditConnection {

    private final TransportClient transportClient;

    public ElasticAuditConnection(ConfigurationSource configSource) throws ConfigurationException {
        //  Get Elasticsearch configuration.
        final ElasticAuditConfiguration config = configSource.getConfiguration(ElasticAuditConfiguration.class);

        //  Get Elasticsearch connection.
        transportClient = ElasticAuditTransportClientFactory.getTransportClient(config.getHostAndPort(), config.getClusterName());
    }

    @Override
    public AuditChannel createChannel() throws IOException {
        //  Share the Elasticsearch transport client across channels.
        return new ElasticAuditChannel(transportClient);
    }

    @Override
    public void close() throws Exception {
        transportClient.close();
    }
}
