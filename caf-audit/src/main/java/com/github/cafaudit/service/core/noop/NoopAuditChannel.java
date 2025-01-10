/*
 * Copyright 2015-2025 Open Text.
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
package com.github.cafaudit.service.core.noop;

import com.github.cafaudit.service.core.AuditChannel;
import com.github.cafaudit.service.core.AuditCoreMetadataProvider;
import com.github.cafaudit.service.core.AuditEventBuilder;

final class NoopAuditChannel implements AuditChannel
{
    public NoopAuditChannel()
    {
    }

    @Override
    public void declareApplication(final String applicationId)
    {
    }

    @Override
    public AuditEventBuilder createEventBuilder()
    {
        return new NoopAuditEventBuilder();
    }

    @Override
    public AuditEventBuilder createEventBuilder(final AuditCoreMetadataProvider coreMetadataProvider)
    {
        return new NoopAuditEventBuilder();
    }

    @Override
    public void close()
    {
    }
}
