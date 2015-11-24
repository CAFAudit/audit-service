package com.hpe.caf.auditing;

import com.hpe.caf.api.Codec;
import com.hpe.caf.api.CodecException;
import com.hpe.caf.api.worker.TaskMessage;
import com.hpe.caf.api.worker.TaskStatus;
import com.hpe.caf.codec.JsonCodec;
import com.hpe.caf.worker.audit.AuditWorkerConstants;
import com.hpe.caf.worker.audit.AuditWorkerTask;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;

/**
 * Common functionality used by the auto-generated AuditLog classes.
 */
public final class AuditLogHelper {

    private static final UUID processId = UUID.randomUUID();

    // NB: If this is causing contention then we could use ThreadLocal to
    // make it thread-specific (and also might lead to less confusion as
	// it would always align with increasing time - which it doesn't
	// necessarily when it is process-wide)
    private static final AtomicLong nextEventId = new AtomicLong(0);

    private static final Clock systemClock = Clock.systemUTC();

    private static final String systemName = getSystemName();

    private static final Codec codec = new JsonCodec();

    private AuditLogHelper() {
    }

    /*
     * Create an instance of the audit worker task comprising audit event details.
     */
    public static AuditWorkerTask createAuditWorkerTask()
    {
        final AuditWorkerTask auditWorkerTask = new AuditWorkerTask();
        auditWorkerTask.setProcessId(processId);
        auditWorkerTask.setThreadId(Thread.currentThread().getId());
        auditWorkerTask.setEventOrder(nextEventId.getAndIncrement());
        auditWorkerTask.setEventTime(systemClock.instant().toString());
        auditWorkerTask.setEventTimeSource(systemName);

        return auditWorkerTask;
    }

    /*
     * Publish audit event details on the queue.
     */
    public static void sendAuditWorkerTask
    (
        final AuditChannel channel,
        final String queueName,
        final AuditWorkerTask auditWorkerTask
    )
        throws IOException
    {
        // Generate a random task id
        final String taskId = UUID.randomUUID().toString();

        // Serialise the AuditWorkerTask
        // Wrap any CodecException as a RuntimeException as it shouldn't happen
        final byte[] taskData;
        try {
            taskData = codec.serialise(auditWorkerTask);
        } catch (CodecException e) {
            throw new RuntimeException(e);
        }

        // Construct the task message
        final TaskMessage taskMessage = new TaskMessage(
            taskId,
            AuditWorkerConstants.WORKER_NAME,
            AuditWorkerConstants.WORKER_API_VER,
            taskData,
            TaskStatus.NEW_TASK,
            Collections.<String, byte[]>emptyMap());

        // Serialise it
        // Wrap any CodecException as a RuntimeException as it shouldn't happen
        final byte[] taskMessageBytes;
        try {
            taskMessageBytes = codec.serialise(taskMessage);
        } catch (CodecException e) {
            throw new RuntimeException(e);
        }

        // Send the message
        channel.publish(queueName, taskMessageBytes);
    }

    private static String getSystemName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
