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
package com.hpe.caf.auditing.webserviceclient;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class WebserviceClientAuditConnection implements AuditConnection {

    private static final Logger LOG = LogManager.getLogger(WebserviceClientAuditConnection.class.getName());

    private static final String NO_PROXY = "NO_PROXY";
    private static final String HTTP_PROXY = "HTTP_PROXY";
    private static final String HTTPS_PROXY = "HTTPS_PROXY";

    private HttpURLConnection webserviceHttpUrlConnection;

    public WebserviceClientAuditConnection(final ConfigurationSource configSource) throws WebserviceClientException,
            MalformedURLException, ConfigurationException {
        //  Get Webservice configuration.
        final WebserviceClientAuditConfiguration config =
                configSource.getConfiguration(WebserviceClientAuditConfiguration.class);

        this.webserviceHttpUrlConnection = getWebserviceHttpUrlConnection(config.getWebserviceEndpoint());
    }

    private HttpURLConnection getWebserviceHttpUrlConnection(String webserviceEndpoint) throws
            WebserviceClientException, MalformedURLException {
        URL webserviceEndpointUrl = new URL(getWebserviceEndpointFullPath(webserviceEndpoint));
        String webserviceEndpointUrlProtocol = webserviceEndpointUrl.getProtocol();
        try {
            //  If the webservice endpoint is not included in no-proxy, depending on the webservice endpoint protocol
            //  set, route through http or https proxy. Else create a HttpUrlConnection or HttpsUrlConnection based
            //  upon the webserviceEndpoint protocol.
            String noProxyList = getNoProxyList();
            if (noProxyList == null || !noProxyList.contains(webserviceEndpointUrl.getHost())) {
                LOG.debug(webserviceEndpointUrl.getHost() + " is not included in the list of no_proxy hosts. " +
                        "Attempting to create connection through " + webserviceEndpointUrlProtocol.toUpperCase()
                        + " proxy");
                if (webserviceEndpointUrlProtocol.equals("http")) {
                    // If a HTTP Proxy has been set then create a HttpURLConnection based upon it, else create a HttpURLConnection without proxy
                    String httpProxy = getHttpProxy();
                    if (httpProxy != null && !httpProxy.isEmpty()) {
                        URL httpProxyUrl = new URL(httpProxy);
                        InetSocketAddress proxyInet = new InetSocketAddress(httpProxyUrl.getHost(),
                                httpProxyUrl.getPort());
                        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
                        return (HttpURLConnection) webserviceEndpointUrl.openConnection(proxy);
                    } else {
                        LOG.debug("HTTP proxy is not set, connection to webservice is not proxied");
                        return (HttpURLConnection) webserviceEndpointUrl.openConnection();
                    }
                } else if (webserviceEndpointUrlProtocol.equals("https")) {
                    // If a HTTPS Proxy has been set then create a HttpsURLConnection based upon it, else create a HttpsURLConnection without proxy
                    String httpsProxy = getHttpsProxy();
                    if (httpsProxy != null && !httpsProxy.isEmpty()) {
                        URL httpsProxyUrl = new URL(httpsProxy);
                        InetSocketAddress proxyInet = new InetSocketAddress(httpsProxyUrl.getHost(),
                                httpsProxyUrl.getPort());
                        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
                        return (HttpsURLConnection) webserviceEndpointUrl.openConnection(proxy);
                    } else {
                        LOG.debug("HTTPS proxy is not set, connection to webservice is not proxied");
                        return (HttpsURLConnection) webserviceEndpointUrl.openConnection();
                    }
                }
            }

            LOG.debug(webserviceEndpointUrl.getHost() + " is included in the list of no_proxy hosts. Attempting to " +
                    "create " + webserviceEndpointUrlProtocol.toUpperCase() + " connection to webservice");
            if (webserviceEndpointUrlProtocol.equals("https")) {
                return (HttpsURLConnection) webserviceEndpointUrl.openConnection();
            }
            return (HttpURLConnection) webserviceEndpointUrl.openConnection();
        } catch (IOException ioe) {
            String errorMessage = "Unable to open " + webserviceEndpointUrlProtocol.toUpperCase()
                    + " URL Connection to " + webserviceEndpointUrl.toExternalForm();
            LOG.error(errorMessage, ioe);
            throw new WebserviceClientException(errorMessage, ioe);
        }
    }

    private String getWebserviceEndpointFullPath(String webserviceEndpoint) {
        // Append 'auditevents' accordingly to create the full path to the webservice
        if (webserviceEndpoint.endsWith("/")) {
            return webserviceEndpoint + "auditevents";
        }
        return webserviceEndpoint + "/auditevents";
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

    @Override
    public AuditChannel createChannel() throws IOException {
        return new WebserviceClientAuditChannel(webserviceHttpUrlConnection);
    }

    @Override
    public void close() throws Exception {

    }
}
