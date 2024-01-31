/*
 * Copyright 2015-2024 Open Text.
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

import com.hpe.caf.auditing.exception.AuditConfigurationException;
import com.hpe.caf.auditing.noop.NoopAuditConnection;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;

public class AuditConnectionFactory
{

    /**
     * Create connection for the Audit application. Returns NoopAuditConnection if an 'CAF_AUDIT_MODE' environment variable has been set
     * to 'NONE'. If 'CAF_AUDIT_MODE' has been set to 'webservice' this returns a WebServiceClientAuditConnection. If 'CAF_AUDIT_MODE' has
     * been set to 'elasticsearch' this returns an ElasticAuditConnection.
     *
     * @return the connection to the audit server, depending on the setting of the 'CAF_AUDIT_MODE' environment variable
     * @throws AuditConfigurationException if the audit server details cannot be retrieved.
     */
    public static AuditConnection createConnection() throws AuditConfigurationException
    {
        final String auditLibMode = System.getProperty("CAF_AUDIT_MODE", System.getenv("CAF_AUDIT_MODE"));
        if (auditLibMode == null) {
            throw new AuditConfigurationException("No Auditing mode has been provided.");
        }
        // If the CAF_AUDIT_MODE environment variable has been set to NONE return the NO-OP implementation
        if (auditLibMode.equals("NONE")) {
            return new NoopAuditConnection();
        }
        final Reflections reflections = new Reflections("com.hpe.caf.auditing");
        final Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(AuditImplementation.class);
        if(annotatedClasses == null){
            throw new RuntimeException("No implemenation for auditing have been provided.");
        }
        final List<Class<?>> implementations = annotatedClasses.stream()
            .filter(e -> e.getAnnotation(AuditImplementation.class).value().equals(auditLibMode)).collect(Collectors.toList());
        if (implementations.size() > 1) {
            throw new RuntimeException("More than one implementation has been found for the audit mode selected.");
        }
        if (implementations.isEmpty()) {
            throw new AuditConfigurationException("No auditing implementations have been found for the mode selected.");
        }
        try {
            final AuditConnectionProvider connectionProvider
                = (AuditConnectionProvider) implementations.iterator().next().getDeclaredConstructor().newInstance();
            return connectionProvider.getConnection();
        } catch (final InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException ex) {
            throw new RuntimeException("Unable to instantiate provider for the requested auditing implemenation.", ex);
        }
    }
}
