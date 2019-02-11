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
package com.hpe.caf.auditing;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.noop.NoopAuditConnection;

public class AuditConnectionFactory {

    /**
     * Create connection for the Audit application. Returns NoopAuditConnection if an 'CAF_AUDIT_MODE' environment
     * variable has not been set. If 'CAF_AUDIT_MODE' has been set to 'webservice' this returns a
     * WebServiceClientAuditConnection. If 'CAF_AUDIT_MODE' has been set to 'direct' this returns an
     * ElasticAuditConnection.
     *
     * @param configSource the configuration source
     * @return the connection to the audit server, depending on the setting of the 'CAF_AUDIT_MODE' environment variable
     * @throws ConfigurationException if the audit server details cannot be retrieved from the configuration source.
     * Or (if CAF_AUDIT_MODE=webservice) if the webservice endpoint URL, passed via configuration, or if HTTP or HTTPS
     * Proxy URLs are malformed
     */
    public static AuditConnection createConnection(final ConfigurationSource configSource) throws
            ConfigurationException {
        String auditLibMode = System.getProperty("CAF_AUDIT_MODE", System.getenv("CAF_AUDIT_MODE"));

        // If the CAF_AUDIT_MODE environment variable has not been set return the NOOP implementation
        if (auditLibMode == null) {
            return new NoopAuditConnection(configSource);
        }

        // Return WebServiceClientAuditConnection or ElasticAuditConnection impl depending on CAF_AUDIT_MODE's value
        switch (auditLibMode.toLowerCase()) {
            case "webservice":
//                return new WebServiceClientAuditConnection(configSource);
            case "direct":
//                return new ElasticAuditConnection(configSource);
            default:
                // Throw a RuntimeException if an unknown CAF_AUDIT_MODE is specified
                throw new RuntimeException("Unknown CAF_AUDIT_MODE specified");
        }
    }

    /**
     * Create connection for the Audit application. Returns NoopAuditConnection if an 'CAF_AUDIT_MODE' environment
     * variable has not been set. If 'CAF_AUDIT_MODE' has been set to 'direct' this returns an ElasticAuditConnection
     * if its required system properties or environment variables for configuration have been set.
     *
     * @return the connection to the audit server, depending on the setting of the 'CAF_AUDIT_MODE' environment variable
     * @throws ConfigurationException if the configuration details cannot be retrieved from system properties or
     * environment variables for building ElasticAuditConfiguration. Also thrown if 'CAF_AUDIT_MODE' has been set to
     * webservice.
     */
    public static AuditConnection createConnection() throws ConfigurationException {
        return createConnection(null);
    }
}
