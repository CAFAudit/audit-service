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
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * A factory for Elastic Search TransportClients.
 */
public class ElasticAuditTransportClientFactory {

    private ElasticAuditTransportClientFactory() {
    }

    /**
     * Returns an elastic search TransportClient.
     *
     * @param hosts list of Elasticsearch hosts
     * @param port Elasticsearch port
     * @param clusterName Elasticsearch cluster name
     * @return TransportClient
     * @throws ConfigurationException exception thrown if host is unknown
     */
    public static TransportClient getTransportClient(List<String> hosts, int port, String clusterName) throws ConfigurationException{
        final TransportClient transportClient;
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", clusterName).build();
            transportClient = new PreBuiltTransportClient(settings);

            //  Support for multiple hosts.
            for (final String host : hosts) {
                transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
            }
        } catch (UnknownHostException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }

        return transportClient;
    }
}
