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
package com.hpe.caf.auditing.webserviceclient;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class WebServiceClientAuditConnection implements AuditConnection {

    private static final Logger LOG = LogManager.getLogger(WebServiceClientAuditConnection.class.getName());

    private static String NO_PROXY = "NO_PROXY";
    private static String HTTP_PROXY = "HTTP_PROXY";
    private static String HTTPS_PROXY = "HTTPS_PROXY";

    private final Proxy httpProxy;

    private final URL webServiceEndpointUrl;

    /**
     * Audit WebService Client Connection object used to create new instances of the WebService Client Audit Channel
     * @throws ConfigurationException When the url provided via system property or environment variable causes a malformed URL exception.
     */
    public WebServiceClientAuditConnection() throws ConfigurationException
    {
        try {
            //  Get Webservice endpoint URL
            this.webServiceEndpointUrl = new URL(getWebServiceEndpointFullPath(
                System.getProperty("CAF_AUDIT_WEBSERVICE_ENDPOINT_URL",
                                   System.getenv("CAF_AUDIT_WEBSERVICE_ENDPOINT_URL"))));
        } catch (final MalformedURLException mue) {
            throw new ConfigurationException("Unable to create URL from Audit Web Service Endpoint configuration property", mue);
        }
        // Get Proxy object based on NO_PROXY, HTTP_PROXY and HTTPS_PROXY environment variables
        this.httpProxy = getProxy(webServiceEndpointUrl);
    }

    private Proxy getProxy(final URL webServiceEndpointUrl) throws ConfigurationException {
        String webserviceEndpointUrlProtocol = webServiceEndpointUrl.getProtocol();
        //  If the webservice endpoint is not included in no-proxy, depending on the webservice endpoint protocol
        //  set, return http or https proxy object. Else return null.
        String noProxyList = getNoProxyList();
        if (noProxyList == null || !noProxyList.contains(webServiceEndpointUrl.getHost())) {
            LOG.debug(webServiceEndpointUrl.getHost() + " is not included in the list of no_proxy hosts. " +
                    "Attempting to create " + webserviceEndpointUrlProtocol.toUpperCase() + " proxy");
            if (webserviceEndpointUrlProtocol.equals("http")) {
                // If a HTTP Proxy has been set and the WS Endpoint Protocol is HTTP, return a Proxy based upon it
                String httpProxy = getHttpProxy();
                if (httpProxy != null && !httpProxy.isEmpty()) {
                    URL httpProxyUrl;
                    try {
                        httpProxyUrl = new URL(httpProxy);
                    } catch (final MalformedURLException mue) {
                        String errorMessage = "Unable to create URL for HTTP Proxy: " + httpProxy;
                        throw new ConfigurationException(errorMessage, mue);
                    }
                    InetSocketAddress proxyInet = new InetSocketAddress(httpProxyUrl.getHost(),
                            httpProxyUrl.getPort());
                    return new Proxy(Proxy.Type.HTTP, proxyInet);
                }
            } else if (webserviceEndpointUrlProtocol.equals("https")) {
                // If a HTTPS Proxy has been set and the WS Endpoint Protocol is HTTPS, return a Proxy based upon it
                String httpsProxy = getHttpsProxy();
                if (httpsProxy != null && !httpsProxy.isEmpty()) {
                    URL httpsProxyUrl;
                    try {
                        httpsProxyUrl = new URL(httpsProxy);
                    } catch (final MalformedURLException mue) {
                        String errorMessage = "Unable to create URL for HTTPS Proxy: " + httpsProxy;
                        throw new ConfigurationException(errorMessage, mue);
                    }
                    InetSocketAddress proxyInet = new InetSocketAddress(httpsProxyUrl.getHost(),
                            httpsProxyUrl.getPort());
                    return new Proxy(Proxy.Type.HTTP, proxyInet);
                }
            }
        }
        LOG.debug(webServiceEndpointUrl.getHost() + " is included in the list of no_proxy hosts or there are no HTTP " +
                "or HTTPS proxies set to base one upon.");
        return null;
    }

    private String getWebServiceEndpointFullPath(String webServiceEndpoint) {
        // Append 'auditevents' accordingly to create the full path to the webservice
        if (webServiceEndpoint.endsWith("/")) {
            return webServiceEndpoint + "auditevents";
        }
        return webServiceEndpoint + "/auditevents";
    }

    private String getNoProxyList() {
        String noProxyList = System.getProperty(NO_PROXY, System.getenv(NO_PROXY));
        if (noProxyList == null) {
            noProxyList = System.getProperty(NO_PROXY.toLowerCase());
        }
        return noProxyList;
    }

    private String getHttpProxy() {
        String httpProxy = System.getProperty(HTTP_PROXY, System.getenv(HTTP_PROXY));
        if (httpProxy == null) {
            httpProxy = System.getProperty(HTTP_PROXY.toLowerCase());
        }
        return httpProxy;
    }

    private String getHttpsProxy() {
        String httpsProxy = System.getProperty(HTTPS_PROXY, System.getenv(HTTPS_PROXY));
        if (httpsProxy == null) {
            httpsProxy = System.getProperty(HTTPS_PROXY.toLowerCase());
        }
        return httpsProxy;
    }

    /**
     * Creates a WebService Client Audit Channel that can be used to create WebService Client Audit Event Builder
     * @return a new instance of WebServiceClientAuditChannel
     */
    @Override
    public AuditChannel createChannel() {
        return new WebServiceClientAuditChannel(webServiceEndpointUrl, httpProxy);
    }

    /**
     * No Implementation
     */
    @Override
    public void close() {
        // Do nothing
    }
}
