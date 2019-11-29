/*
 * Copyright 2015-2020 Micro Focus or one of its affiliates.
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

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;

import java.net.Proxy;
import java.net.URL;

public class WebServiceClientAuditChannel implements AuditChannel {

    private final URL webServiceEndpointUrl;

    private final Proxy httpProxy;

    /**
     * Audit Webservice Client Channel object used to create new instances of the Webservice Client Audit Event Builder
     * @param webServiceEndpointUrl webservice HTTP endpoint URL object
     * @param httpProxy the proxy that HTTP requests to the webservice endpoint will be routed via
     */
    public WebServiceClientAuditChannel(final URL webServiceEndpointUrl, final Proxy httpProxy) {
        this.webServiceEndpointUrl = webServiceEndpointUrl;
        this.httpProxy = httpProxy;
    }

    /**
     * No implementation
     */
    @Override
    public void declareApplication(String applicationId) {
        // Do nothing
    }

    /**
     * Create a Webservice Client Audit Event Builder with provided Audit Event Metadata
     * @param coreMetadataProvider provides values for the core system-provided metadata
     * @return A new instance of the WebServiceClientAuditEventBuilder
     */
    @Override
    public AuditEventBuilder createEventBuilder(AuditCoreMetadataProvider coreMetadataProvider) {
        return new WebServiceClientAuditEventBuilder(webServiceEndpointUrl, httpProxy, coreMetadataProvider);
    }

    /**
     * No implementation
     */
    @Override
    public void close() {
        // Do nothing
    }
}
