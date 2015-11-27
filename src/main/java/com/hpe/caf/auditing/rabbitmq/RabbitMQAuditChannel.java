package com.hpe.caf.auditing.rabbitmq;

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.Collections;

final class RabbitMQAuditChannel implements AuditChannel
{
    private final Channel rabbitMQChannel;

    /*
     * Constructor.
     */
    public RabbitMQAuditChannel(Channel rabbitMQChannel) throws IOException {
        this.rabbitMQChannel = rabbitMQChannel;
    }

    /**
     * Declare the specified application
     */
    @Override
    public void declareApplication(String applicationId) throws IOException
    {
        final String queueName = RabbitMQAuditQueue.getQueueName(applicationId);

        /**
         * The queue contents are durable and should be disk backed.
         * The queue can be used by any channel consumer.
         * Do not automatically remove the queue if it becomes empty.
         */
        rabbitMQChannel.queueDeclare(
            queueName, true, false, false, Collections.emptyMap());
    }

    @Override
    public AuditEventBuilder createEventBuilder()
    {
        return new RabbitMQAuditEventBuilder(rabbitMQChannel);
    }

    /**
     * Close the channel.
     */
    @Override
    public void close() throws Exception {
        rabbitMQChannel.close();
    }
}
