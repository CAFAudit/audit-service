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
package com.hpe.caf.auditing.noop;

import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditIndexingHint;

import java.util.Date;

final class NoopAuditEventBuilder implements AuditEventBuilder
{
    public NoopAuditEventBuilder()
    {
    }

    @Override
    public void setApplication(final String applicationId)
    {
    }

    @Override
    public void setUser(final String userId)
    {
    }

    @Override
    public void setTenant(final String tenantId)
    {
    }

    @Override
    public void setCorrelationId(final String correlationId)
    {
    }

    @Override
    public void setEventType(final String eventCategoryId, final String eventTypeId)
    {
    }

    @Override
    public void addEventParameter(final String name, final String columnName, final String value)
    {
    }

    @Override
    public void addEventParameter(final String name, final String columnName, final String value, final AuditIndexingHint indexingHint)
    {
    }

    @Override
    public void addEventParameter(final String name, final String columnName, final short value)
    {
    }

    @Override
    public void addEventParameter(final String name, final String columnName, final int value)
    {
    }

    @Override
    public void addEventParameter(final String name, final String columnName, final long value)
    {
    }

    @Override
    public void addEventParameter(final String name, final String columnName, final float value)
    {
    }

    @Override
    public void addEventParameter(final String name, final String columnName, final double value)
    {
    }

    @Override
    public void addEventParameter(final String name, final String columnName, final boolean value)
    {
    }

    @Override
    public void addEventParameter(final String name, final String columnName, final Date value)
    {
    }

    @Override
    public void send()
    {
    }
}
