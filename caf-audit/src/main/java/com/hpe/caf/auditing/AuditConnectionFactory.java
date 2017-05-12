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
import com.hpe.caf.auditing.elastic.ElasticAuditConnection;
import com.hpe.caf.auditing.noop.NoopAuditConnection;
import com.hpe.caf.auditing.webserviceclient.WebserviceClientAuditConnection;

public class AuditConnectionFactory {

    /**
     * Create connection for the Audit application. Returns NoopAuditConnection if an 'AUDIT_LIB_MODE' environment
     * variable has not been set. If 'AUDIT_LIB_MODE' has been set to 'webserviceclient' this returns a
     * WebserviceClientAuditConnection. If 'AUDIT_LIB_MODE' has been set to 'elasticsearchdirect' this returns an
     * ElasticAuditConnection.
     *
     * @param configSource the configuration source
     * @return the connection to the audit server, depending on the setting of the 'AUDIT_LIB_MODE' environment variable
     * @throws ConfigurationException if the audit server details cannot be retrieved from the configuration source
     */
    public static AuditConnection createConnection(final ConfigurationSource configSource) throws ConfigurationException {
        String auditLibMode = System.getProperty("AUDIT_LIB_MODE", System.getenv("AUDIT_LIB_MODE"));

        // If the AUDIT_LIB_MODE environment variable has not been set return the NOOP implementation
        if (auditLibMode == null) {
            return new NoopAuditConnection(configSource);
        }

        // Return WebServiceClient or Direct to Elastic search impl depending on AUDIT_LIB_MODE's value
        if (auditLibMode.equals("webserviceclient")) {
            return new WebserviceClientAuditConnection(configSource);
        } else if (auditLibMode.equals("elasticsearchdirect")) {
            return new ElasticAuditConnection(configSource);
        }

        // Throw a RuntimeException if an unknown AUDIT_LIB_MODE is specified
        throw new RuntimeException("Unknown AUDIT_LIB_MODE specified");
    }
}
