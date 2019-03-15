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

import com.hpe.caf.auditing.exception.AuditConfigurationException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * A factory for Elastic Search TransportClients.
 */
public class ElasticAuditTransportClientFactory {

    private static final Logger LOG = LogManager.getLogger(ElasticAuditTransportClientFactory.class.getName());

    private static final String ES_HOST_AND_PORT_NOT_PROVIDED = "Elasticsearch host and port have not been provided";
    private static final String ES_HOST_NOT_PROVIDED = "Elasticsearch host has not been provided";
    private static final String ES_PORT_NOT_PROVIDED = "Elasticsearch port has not been provided";

    private ElasticAuditTransportClientFactory() {
    }

    /**
     * Returns an elastic search TransportClient.
     *
     * @param hostAndPortValues comma separated list of Elasticsearch host:port values
     * @param clusterName Elasticsearch cluster name
     * @return TransportClient
     * @throws AuditConfigurationException exception thrown if host is unknown
     */
    public static TransportClient getTransportClient(String hostAndPortValues, String clusterName)
        throws AuditConfigurationException {
        final TransportClient transportClient;

        if (hostAndPortValues != null && !hostAndPortValues.isEmpty()) {
            //  Split comma separated list of ES hostname and port values.
            final String[] hostAndPortArray = hostAndPortValues.split(",");

            // If there is more than one Elasticsearch host then set the client.transport.sniff to true as we are dealing with a cluster
            Settings.Builder settingsBuilder = Settings.builder()
                    .put("cluster.name", clusterName);
            if (hostAndPortArray.length > 1) {
                settingsBuilder.put("client.transport.sniff", true);
            }
            Settings settings = settingsBuilder.build();
            transportClient = new PreBuiltTransportClient(settings);

            //  For each ES hostname and port, add a transport address that will be used to connect to.
            for (final String hostAndPort : hostAndPortArray) {
                final String host;
                final int port;
                try {
                    // Add scheme to make the resulting URI valid.
                    URI uri = new URI("http://" + hostAndPort);
                    host = uri.getHost();
                    port = uri.getPort();

                    if (uri.getHost() == null) {
                        throw new URISyntaxException(uri.toString(), ES_HOST_NOT_PROVIDED);
                    } else if (uri.getPort() == -1) {
                        throw new URISyntaxException(uri.toString(), ES_PORT_NOT_PROVIDED);
                    }

                    try {
                        transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
                        LOG.debug("Elasticsearch initialization - added host: " + host);
                    } catch (UnknownHostException e) {
                        LOG.error("Host unavailable or unknown: " + e.getMessage(), e);
                    }

                } catch (URISyntaxException e) {
                    LOG.error(e.getMessage());
                    throw new AuditConfigurationException(e.getMessage(), e);
                }
            }

            if (transportClient.listedNodes().isEmpty()) {
                final String errorMessage = "Elasticsearch transport client is not configured to communicate with " +
                        "any nodes";
                LOG.error(errorMessage);
                throw new AuditConfigurationException(errorMessage);
            }

            LOG.debug("Elasticsearch client initialized: " + transportClient.listedNodes().toString());
        } else {
            //  ES host and port not specified.
            LOG.error(ES_HOST_AND_PORT_NOT_PROVIDED);
            throw new AuditConfigurationException(ES_HOST_AND_PORT_NOT_PROVIDED);
        }

        return transportClient;
    }
}
