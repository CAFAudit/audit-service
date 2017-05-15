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
            String noProxies = System.getProperty("no_proxy", System.getenv("no_proxy"));
            if (noProxies == null || !noProxies.contains(webserviceEndpointUrl.getHost())) { // TODO: -Check if the fetching of env var is case sensitive. -Check if the proxy variables would ever be passed as sys vars, or would they only ever be passed as env vars?
                if (webserviceEndpointUrl.getProtocol().equals("http")) {
                    URL httpProxy = new URL(System.getProperty("http_proxy", System.getenv("http_proxy")));
                    InetSocketAddress proxyInet = new InetSocketAddress(httpProxy.getHost(), httpProxy.getPort());
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
                    webserviceHttpUrlConnection = (HttpURLConnection) webserviceEndpointUrl.openConnection(proxy);
                } else if (webserviceEndpointUrl.getProtocol().equals("https")) {
                    URL httpsProxy = new URL(System.getProperty("https_proxy", System.getenv("https_proxy")));
                    InetSocketAddress proxyInet = new InetSocketAddress(httpsProxy.getHost(), httpsProxy.getPort());
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
                    webserviceHttpUrlConnection = (HttpsURLConnection) webserviceEndpointUrl.openConnection(proxy);
                }
            } else {
                if (webserviceEndpointUrl.getProtocol().equals("https")) {
                    webserviceHttpUrlConnection = (HttpsURLConnection) webserviceEndpointUrl.openConnection();
                } else {
                    webserviceHttpUrlConnection = (HttpURLConnection) webserviceEndpointUrl.openConnection();
                }
            }
        } catch (IOException ioe) {
            String errorMessage = "Unable to open HTTP Connection to " + webserviceEndpointUrl.toExternalForm();  // TODO: Breakpoint to see if the url object can return the full endpoint url.
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
