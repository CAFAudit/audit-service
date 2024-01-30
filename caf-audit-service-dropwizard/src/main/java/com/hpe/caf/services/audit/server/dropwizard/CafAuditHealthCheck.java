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
package com.hpe.caf.services.audit.server.dropwizard;

import com.codahale.metrics.health.HealthCheck;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionFactory;
import com.hpe.caf.auditing.exception.AuditConfigurationException;
import com.hpe.caf.auditing.healthcheck.HealthResult;
import com.hpe.caf.auditing.healthcheck.HealthStatus;

final class CafAuditHealthCheck extends HealthCheck
{
    private final AuditConnection connection;

    public CafAuditHealthCheck() throws AuditConfigurationException
    {
        connection = AuditConnectionFactory.createConnection();
    }

    @Override
    protected Result check() throws Exception
    {
        try (final AuditChannel channel = connection.createChannel()) {
            final HealthResult healthResult = channel.healthCheck();
            if (healthResult.getStatus() == HealthStatus.HEALTHY) {
                return Result.healthy();
            } else {
                return Result.unhealthy(healthResult.getMessage());
            }
        }
    }
}
