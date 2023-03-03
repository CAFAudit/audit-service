/*
 * Copyright 2015-2023 Open Text.
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
package com.hpe.caf.auditing.healthcheck;

public final class HealthResult
{
    private final HealthStatus status;
    private final String message;

    public static final HealthResult HEALTHY = new HealthResult(HealthStatus.HEALTHY);

    public HealthResult(final HealthStatus status)
    {
        this.status = status;
        this.message = null;
    }

    public HealthResult(final HealthStatus status, final String message)
    {
        this.status = status;
        this.message = message;
    }

    public HealthStatus getStatus()
    {
        return status;
    }

    public String getMessage()
    {
        return message;
    }
}
