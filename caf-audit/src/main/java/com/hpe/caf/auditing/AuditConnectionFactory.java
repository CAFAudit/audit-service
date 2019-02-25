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
import com.hpe.caf.auditing.noop.NoopAuditConnection;
import com.hpe.caf.util.ModuleLoader;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class AuditConnectionFactory
{

    /**
     * Create connection for the Audit application. Returns NoopAuditConnection if an 'CAF_AUDIT_MODE' environment variable has been
     * set to 'NONE'. If 'CAF_AUDIT_MODE' has been set to 'webservice' this returns a WebServiceClientAuditConnection. If 'CAF_AUDIT_MODE' has been
     * set to 'elasticsearch' this returns an ElasticAuditConnection.
     *
     * @return the connection to the audit server, depending on the setting of the 'CAF_AUDIT_MODE' environment variable
     * @throws ConfigurationException if the audit server details cannot be retrieved from the configuration source. Or (if
     * CAF_AUDIT_MODE=webservice) if the webservice endpoint URL, passed via configuration, or if HTTP or HTTPS Proxy URLs are malformed
     */
    public static AuditConnection createConnection() throws
        ConfigurationException
    {
        final String auditLibMode = System.getProperty("CAF_AUDIT_MODE", System.getenv("CAF_AUDIT_MODE"));
        // If the CAF_AUDIT_MODE environment variable has been set to NONE return the NO-OP implementation
        if (auditLibMode.equals("NONE")) {
            return new NoopAuditConnection();
        }
        final Collection<AuditConnectionProvider> auditConnectionImpls = ModuleLoader.getServices(AuditConnectionProvider.class);
        if (auditConnectionImpls == null || auditConnectionImpls.isEmpty()) {
            // Throw a RuntimeException if there are no auditing implementations available
            throw new RuntimeException("No Auditting implementations have been provided.");
        }
        final Map<String, AuditConnectionProvider> auditConnectionImplementations
            = auditConnectionImpls.stream().collect(Collectors.toMap(e -> e.getClass().getSimpleName(), e -> e));
        // Return WebServiceClientAuditConnection or ElasticAuditConnection impl depending on CAF_AUDIT_MODE's value
        final AuditConnectionProvider connection;
        switch (auditLibMode.toLowerCase()) {
            case "webservice":
                connection = auditConnectionImplementations.get("WebServiceClientAuditConnectionProvider");
                break;
            case "elasticsearch":
                connection = auditConnectionImplementations.get("ElasticAuditConnectionProvider");
                break;
            default:
                // Throw a RuntimeException if an unknown CAF_AUDIT_MODE is specified
                throw new RuntimeException("Unknown CAF_AUDIT_MODE specified");
        }
        if (connection == null) {
            throw new ConfigurationException("Specified auditing implementation could not be found.");
        }
        return connection.getConnection();
    }
}
