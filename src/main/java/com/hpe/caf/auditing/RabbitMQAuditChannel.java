package com.hpe.caf.auditing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.Collections;

public class RabbitMQAuditChannel implements AuditChannel{

    private Channel rabbitMQChannel;

    public RabbitMQAuditChannel(Channel rabbitMQChannel) throws IOException {
        this.rabbitMQChannel = rabbitMQChannel;
    }

    @Override
    public void publish(String routingKey, byte[] body) throws IOException {
        rabbitMQChannel.basicPublish("", routingKey, MessageProperties.TEXT_PLAIN, body);
    }

    @Override
    public void close() throws Exception {
        rabbitMQChannel.close();
    }

    @Override
    public void declareQueue(String queueName) throws IOException {

        /**
         * The queue contents are durable and should be disk backed.
         * The queue can be used by any channel consumer.
         * Do not automatically remove the queue if it becomes empty.
         */
        rabbitMQChannel.queueDeclare(queueName, true, false, false, Collections.emptyMap());
    }
}
