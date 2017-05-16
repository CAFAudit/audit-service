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
package com.hpe.caf.auditing;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.elastic.ElasticAuditConfiguration;
import com.hpe.caf.auditing.webserviceclient.WebserviceClientAuditConfiguration;
import com.hpe.caf.auditing.webserviceclient.WebserviceClientException;

import java.net.MalformedURLException;

public class AuditConnectionHelper
{

    /**
     * Returns an instance of an ElasticsearchAuditConnection (if environment variable AUDIT_LIB_MODE is set to
     * 'direct')
     * @param esHostAndPorts comma separated list of Elasticsearch host and ports
     * @param esClusterName the name of the Elasticsearch cluster
     * @return configured ElasticAuditConnection
     * @throws ConfigurationException if there is an error with the ConfigurationSource
     */
    public static AuditConnection getElasticAuditConnection(String esHostAndPorts, String esClusterName)
            throws ConfigurationException, MalformedURLException, WebserviceClientException {

        return AuditConnectionFactory.createConnection(new ConfigurationSource()
        {

            @Override
            public <T> T getConfiguration(Class<T> aClass) throws ConfigurationException
            {
                ElasticAuditConfiguration elasticAuditConfiguration = new ElasticAuditConfiguration();
                elasticAuditConfiguration.setHostAndPortValues(esHostAndPorts);
                elasticAuditConfiguration.setClusterName(esClusterName);
                return (T) elasticAuditConfiguration;
            }
        });
    }

    /**
     * Returns an instance of an WebserviceClientAuditConnection (if environment variable AUDIT_LIB_MODE is set to
     * 'webservice')
     * @param webserviceEndpoint the webservice endpoint
     * @return configured ElasticAuditConnection
     * @throws ConfigurationException if there is an error with the ConfigurationSource
     */
    public static AuditConnection getWebserviceAuditConnection(String webserviceEndpoint)
            throws ConfigurationException, MalformedURLException, WebserviceClientException {

        return AuditConnectionFactory.createConnection(new ConfigurationSource()
        {

            @Override
            public <T> T getConfiguration(Class<T> aClass) throws ConfigurationException
            {
                WebserviceClientAuditConfiguration webserviceClientAuditConfiguration = new WebserviceClientAuditConfiguration();
                webserviceClientAuditConfiguration.setWebserviceEndpoint(webserviceEndpoint);
                return (T) webserviceClientAuditConfiguration;
            }
        });
    }
}
