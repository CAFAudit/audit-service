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
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A factory for Elastic Search TransportClients.
 */
public class ElasticAuditRestHighLevelClientFactory {

    private static final Logger LOG = LogManager.getLogger(ElasticAuditRestHighLevelClientFactory.class.getName());

    private static final String ES_HOST_AND_PORT_NOT_PROVIDED = "Elasticsearch host and port have not been provided";
    private static final String ES_HOST_NOT_PROVIDED = "Elasticsearch host has not been provided";
    private static final String ES_PORT_NOT_PROVIDED = "Elasticsearch port has not been provided";

    private ElasticAuditRestHighLevelClientFactory() {
    }

    /**
     * Returns an elastic search high level client.
     *
     * @param hostAndPortValues comma separated list of Elasticsearch host:port values
     * @return RestHighLevelClient
     * @throws AuditConfigurationException exception thrown if host is unknown
     */
    public static RestHighLevelClient getHighLevelClient(final String hostValues, final String portValue)
        throws AuditConfigurationException {

        if (hostValues != null && !hostValues.isEmpty()) {
            //  Split comma separated list of ES hostname values.
            final String[] hostArray = hostValues.split(",");

            if (hostArray.length == 0) {
                final String errorMessage = "No hosts configured.";
                LOG.error(errorMessage);
                throw new AuditConfigurationException(errorMessage);
            }

            final List<HttpHost> httpHostList = new ArrayList<>();
            for (final String host : hostArray) {
                try{
                    final URI uri;
                    if(!host.contains(":")){
                        uri = new URI("http://" + host + ":" + portValue);
                    }else{
                        uri = new URI("http://" + host);
                    }
                      
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
            final RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);

            return restHighLevelClient;
        } else {
            //  ES host and port not specified.
            LOG.error(ES_HOST_AND_PORT_NOT_PROVIDED);
            throw new AuditConfigurationException(ES_HOST_AND_PORT_NOT_PROVIDED);
        }
    }
}
