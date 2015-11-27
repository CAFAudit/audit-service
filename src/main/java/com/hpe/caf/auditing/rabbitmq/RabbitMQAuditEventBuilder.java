package com.hpe.caf.auditing.rabbitmq;

import com.hpe.caf.api.Codec;
import com.hpe.caf.api.CodecException;
import com.hpe.caf.api.worker.TaskMessage;
import com.hpe.caf.api.worker.TaskStatus;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.AuditLogHelper;
import com.hpe.caf.codec.JsonCodec;
import com.hpe.caf.worker.audit.AuditWorkerConstants;
import com.hpe.caf.worker.audit.AuditWorkerTask;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

final class RabbitMQAuditEventBuilder implements AuditEventBuilder
{
    private static final UUID processId = AuditLogHelper.getProcessId();
    private static final Codec codec = new JsonCodec();

    private final Channel rabbitMQChannel;
    private final AuditWorkerTask auditWorkerTask;
    private final List<String> eventParameters;

    public RabbitMQAuditEventBuilder(Channel rabbitMQChannel)
    {
        this.rabbitMQChannel = rabbitMQChannel;
        this.auditWorkerTask = createAuditWorkerTask();
        this.eventParameters = new ArrayList();
    }

    private static AuditWorkerTask createAuditWorkerTask()
    {
        final AuditWorkerTask auditWorkerTask = new AuditWorkerTask();
        auditWorkerTask.setProcessId(processId);
        auditWorkerTask.setThreadId(AuditLogHelper.getThreadId());
        auditWorkerTask.setEventOrder(AuditLogHelper.getNextEventId());
        auditWorkerTask.setEventTime(AuditLogHelper.getCurrentTime().toString());
        auditWorkerTask.setEventTimeSource(AuditLogHelper.getCurrentTimeSource());

        return auditWorkerTask;
    }

    @Override
    public void setApplication(final String applicationId) {
        auditWorkerTask.setApplicationId(applicationId);
    }

    @Override
    public void setUser(final String userId) {
        auditWorkerTask.setUserId(userId);
    }

    @Override
    public void setEventType
    (
        final String eventCategoryId,
        final String eventTypeId
    )
    {
        auditWorkerTask.setEventTypeId(eventTypeId);
    }

    @Override
    public void addEventParameter
    (
        final String name,
        final String columnName,
        final String value
    )
    {
        eventParameters.add(value);
    }

    @Override
    public void send() throws IOException
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

        // Get the queue name
        final String applicationId = auditWorkerTask.getApplicationId();
        final String queueName = RabbitMQAuditQueue.getQueueName(applicationId);

        // Send the message
        rabbitMQChannel.basicPublish(
            "", queueName, MessageProperties.TEXT_PLAIN, taskMessageBytes);
    }
}
