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

import java.time.Instant;
import java.util.UUID;

/**
 * An object which implements this interface can be passed to the {@link AuditChannel#createEventBuilder(AuditCoreMetadataProvider)}
 * method and the audit implementation is expected to use it to retrieve the system-generated event metadata.
 */
public interface AuditCoreMetadataProvider
{
    /**
     * Returns a globally unique identifier that can be used to correlate all audit events that were raised from the same operating system
     * process.
     *
     * @return the unique process identifier
     */
    UUID getProcessId();

    /**
     * Returns a process-specific identifier that can be used to correlate all audit events that were raised from the same logical thread.
     *
     * @return the thread identifier
     */
    long getThreadId();

    /**
     * Returns an integer which can be used to order events within a thread. This is more reliable and finer-grained than the time, as
     * several events could happen at the same time, or indeed the time could be adjusted. Note that this number cannot be used to order
     * events across threads. Also note the implementation is free to skip numbers, so skipped numbers do not necessarily mean that there
     * are missing events - this number should only be interpreted to order events that happened on the same process thread.
     *
     * @return the position of the event relative to other events
     */
    long getEventOrder();

    /**
     * Returns the time that the event occurred according to the source of the time.
     *
     * @return the time that the event occurred
     */
    Instant getEventTime();

    /**
     * Returns the source of the time. This can be useful when comparing events that happened in different processes on the same machine.
     *
     * @return the source of the time
     */
    String getEventTimeSource();
}
