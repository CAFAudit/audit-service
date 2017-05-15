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

    private HttpURLConnection webserviceHttpUrlConnection;

    public WebserviceClientAuditConnection(final ConfigurationSource configSource) throws WebserviceClientException,
            MalformedURLException, ConfigurationException {
        //  Get Webservice configuration.
        final WebserviceClientAuditConfiguration config = configSource.getConfiguration(WebserviceClientAuditConfiguration.class);

        URL webserviceEndpointUrl = new URL(config.getWebserviceEndpoint() + "/auditevents"); // TODO: How should this prepend forward slash? What if the user has already provided a forward slash via configuration?

        try {
            //  If the webservice endpoint is not included in no-proxy, depending on the webservice endpoint protocol set,
            //  route through http or https proxy. Else create a HttpUrlConnection or HttpsUrlConnection based upon the webserviceEndpoint protocol.
            String noProxyList = System.getProperty("no_proxy", System.getenv("no_proxy"));
            if (noProxyList == null || !noProxyList.contains(webserviceEndpointUrl.getHost())) { // TODO: -Check if the fetching of env var is case sensitive. -Check if the proxy variables would ever be passed as sys vars, or would they only ever be passed as env vars?
                if (webserviceEndpointUrl.getProtocol().equals("http")) {
                    // If a HTTP Proxy has been set then create a HttpURLConnection based upon it, else create a HttpURLConnection without proxy
                    String httpProxy = System.getProperty("http_proxy", System.getenv("http_proxy"));
                    if (httpProxy != null && !httpProxy.isEmpty()) {
                        URL httpProxyUrl = new URL(httpProxy);
                        InetSocketAddress proxyInet = new InetSocketAddress(httpProxyUrl.getHost(), httpProxyUrl.getPort());
                        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
                        webserviceHttpUrlConnection = (HttpURLConnection) webserviceEndpointUrl.openConnection(proxy);
                    } else {
                        webserviceHttpUrlConnection = (HttpURLConnection) webserviceEndpointUrl.openConnection();
                    }
                } else if (webserviceEndpointUrl.getProtocol().equals("https")) {
                    // If a HTTPS Proxy has been set then create a HttpsURLConnection based upon it, else create a HttpsURLConnection without proxy
                    String httpsProxy = System.getProperty("https_proxy", System.getenv("https_proxy"));
                    if (httpsProxy != null && !httpsProxy.isEmpty()) {
                        URL httpsProxyUrl = new URL(httpsProxy);
                        InetSocketAddress proxyInet = new InetSocketAddress(httpsProxyUrl.getHost(), httpsProxyUrl.getPort());
                        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
                        webserviceHttpUrlConnection = (HttpsURLConnection) webserviceEndpointUrl.openConnection(proxy);
                    } else {
                        webserviceHttpUrlConnection = (HttpsURLConnection) webserviceEndpointUrl.openConnection();
                    }
                }
            } else {
                if (webserviceEndpointUrl.getProtocol().equals("https")) {
                    webserviceHttpUrlConnection = (HttpsURLConnection) webserviceEndpointUrl.openConnection();
                } else {
                    webserviceHttpUrlConnection = (HttpURLConnection) webserviceEndpointUrl.openConnection();
                }
            }
        } catch (IOException ioe) {
            String errorMessage = "Unable to open HTTP Connection to " + webserviceEndpointUrl.toExternalForm();
            LOG.error(errorMessage, ioe);
            throw new WebserviceClientException(errorMessage, ioe);
        }
    }

    @Override
    public AuditChannel createChannel() throws IOException {
        return new WebserviceClientAuditChannel(webserviceHttpUrlConnection);
    }

    @Override
    public void close() throws Exception {

    }
}
