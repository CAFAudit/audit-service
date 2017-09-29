/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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
package com.hpe.caf.auditing.internal;

import java.time.Instant;
import java.util.UUID;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;

/**
 * This class is used to provide the core system-generated event metadata for new events generated in this process.
 */
public final class AuditNewEventFactory
{
    private AuditNewEventFactory()
    {
    }

    /**
     * Constructs an implementation of {@link AuditCoreMetadataProvider} which provides the system-generated event metadata for an event
     * which is happening presently on the current thread.
     *
     * @return the system-generated metadata for an event occurring now
     */
    public static AuditCoreMetadataProvider createNewEvent()
    {
        final long threadId = AuditLogHelper.getThreadId();
        final long eventOrder = AuditLogHelper.getNextEventId();
        final Instant eventTime = AuditLogHelper.getCurrentTime();

        return new AuditCoreMetadataProvider()
        {
            @Override
            public UUID getProcessId()
            {
                return AuditLogHelper.getProcessId();
            }

            @Override
            public long getThreadId()
            {
                return threadId;
            }

            @Override
            public long getEventOrder()
            {
                return eventOrder;
            }

            @Override
            public Instant getEventTime()
            {
                return eventTime;
            }

            @Override
            public String getEventTimeSource()
            {
                return AuditLogHelper.getCurrentTimeSource();
            }
        };
    }
}
