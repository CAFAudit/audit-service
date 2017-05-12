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

import java.io.IOException;

public class WebserviceClientAuditConnection implements AuditConnection {

    private final String webserviceHostAndPort;

    public WebserviceClientAuditConnection(final ConfigurationSource configSource) throws ConfigurationException {
        //  Get Webservice configuration.
        final WebserviceClientAuditConfiguration config = configSource.getConfiguration(WebserviceClientAuditConfiguration.class);

        this.webserviceHostAndPort = config.getHostAndPort();
    }

    @Override
    public AuditChannel createChannel() throws IOException {
        return new WebserviceClientAuditChannel(webserviceHostAndPort);
    }

    @Override
    public void close() throws Exception {

    }
}
