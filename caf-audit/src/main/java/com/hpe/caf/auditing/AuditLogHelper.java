package com.hpe.caf.auditing;

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
public final class AuditLogHelper {

    private static final UUID processId = ProcessIdentifier.getProcessId();

    // NB: If this is causing contention then we could use ThreadLocal to
    // make it thread-specific (and also might lead to less confusion as
	// it would always align with increasing time - which it doesn't
	// necessarily when it is process-wide)
    private static final AtomicLong nextEventId = new AtomicLong(0);

    private static final Clock systemClock = Clock.systemUTC();

    private static final String systemName = getSystemName();

    private AuditLogHelper() {
    }

    public static UUID getProcessId() {
        return processId;
    }

    public static long getThreadId() {
        return Thread.currentThread().getId();
    }

    public static long getNextEventId() {
        return nextEventId.getAndIncrement();
    }

    public static Instant getCurrentTime() {
        return systemClock.instant();
    }

    public static String getCurrentTimeSource() {
        return systemName;
    }

    private static String getSystemName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
