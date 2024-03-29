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
package com.hpe.caf.auditing.internal;

import com.hpe.caf.util.processidentifier.ProcessIdentifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;

/**
 * Common functionality used by the auditing infrastructure.
 */
final class AuditLogHelper
{

    private static final UUID processId = ProcessIdentifier.getProcessId();

    // NB: If this is causing contention then we could use ThreadLocal to
    // make it thread-specific (and also might lead to less confusion as
    // it would always align with increasing time - which it doesn't
    // necessarily when it is process-wide)
    private static final AtomicLong nextEventId = new AtomicLong(0);

    private static final Clock systemClock = Clock.systemUTC();

    private static final String systemName = getSystemName();

    private AuditLogHelper()
    {
    }

    public static UUID getProcessId()
    {
        return processId;
    }

    public static long getThreadId()
    {
        return Thread.currentThread().getId();
    }

    public static long getNextEventId()
    {
        return nextEventId.getAndIncrement();
    }

    public static Instant getCurrentTime()
    {
        return systemClock.instant();
    }

    public static String getCurrentTimeSource()
    {
        return systemName;
    }

    private static String getSystemName()
    {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
