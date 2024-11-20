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
package com.github.cafaudit.service.dropwizard;

import com.codahale.metrics.health.HealthCheck;
import com.github.cafaudit.service.core.AuditChannel;
import com.github.cafaudit.service.core.AuditConnection;
import com.github.cafaudit.service.core.AuditConnectionFactory;
import com.github.cafaudit.service.core.exception.AuditConfigurationException;
import com.github.cafaudit.service.core.healthcheck.HealthResult;
import com.github.cafaudit.service.core.healthcheck.HealthStatus;

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
