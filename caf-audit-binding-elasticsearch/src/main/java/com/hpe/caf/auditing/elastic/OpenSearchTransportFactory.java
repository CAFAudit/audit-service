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

import com.hpe.caf.auditing.exception.AuditConfigurationException;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;

/**
 * A factory for Open Search TransportClients.
 */
public class OpenSearchTransportFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OpenSearchTransportFactory.class.getName());

    private static final String ES_HOST_AND_PORT_NOT_PROVIDED = "Elasticsearch host and port have not been provided";
    private static final String ES_HOST_NOT_PROVIDED = "Elasticsearch host has not been provided";
    private static final String ES_PORT_NOT_PROVIDED = "Elasticsearch port has not been provided";

    private OpenSearchTransportFactory() {
    }

    /**
     * Returns an open search transport for creating a client.
     *
     * @param hostAndPortValues comma separated list of Elasticsearch host:port values
     * @param elasticUsername Elasticsearch username
     * @param elasticPassword Elasticsearch password
     * @return OpenSearchTransport
     * @throws AuditConfigurationException exception thrown if host is unknown
     */
    public static OpenSearchTransport getOpenSearchTransport(final String elasticProtocol, final String hostAndPortValues,
                                                         final String elasticUsername, final String elasticPassword)
        throws AuditConfigurationException {

        if (hostAndPortValues != null && !hostAndPortValues.isEmpty()) {
            //  Split comma separated list of ES hostname and port values.
            final String[] hostAndPortArray = hostAndPortValues.split(",");

            if (hostAndPortArray.length == 0) {
                final String errorMessage = "No hosts configured.";
                LOG.error(errorMessage);
                throw new AuditConfigurationException(errorMessage);
            }

            final List<HttpHost> httpHostList = new ArrayList<>();
            for (final String hostAndPort : hostAndPortArray) {
                try{
                    final URI uri = new URI(elasticProtocol + "://" + hostAndPort);

                    if (uri.getHost() == null) {
                        throw new URISyntaxException(uri.toString(), ES_HOST_NOT_PROVIDED);
                    } else if (uri.getPort() == -1) {
                        throw new URISyntaxException(uri.toString(), ES_PORT_NOT_PROVIDED);
                    }

                    httpHostList.add(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()));

                    LOG.debug("Elasticsearch initialization - added host: " + uri.toString());

                } catch (URISyntaxException e) {
                    LOG.error(e.getMessage());
                    throw new AuditConfigurationException(e.getMessage(), e);
                }
            }

            final RestClientBuilder restClientBuilder = RestClient.builder(httpHostList.toArray(new HttpHost[0]));

            if (credentialsSupplied(elasticUsername, elasticPassword)) {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                                                   new UsernamePasswordCredentials(elasticUsername, elasticPassword));

                restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider));
            }
            
            final RestClient restClient = restClientBuilder.build();
            return new RestClientTransport(restClient, new JacksonJsonpMapper());
        } else {
            //  ES host and port not specified.
            LOG.error(ES_HOST_AND_PORT_NOT_PROVIDED);
            throw new AuditConfigurationException(ES_HOST_AND_PORT_NOT_PROVIDED);
        }
    }

    private static boolean credentialsSupplied(final String elasticUsername, final String elasticPassword)
    {
        return elasticUsername != null && !elasticUsername.trim().isEmpty() && elasticPassword != null;
    }
}
