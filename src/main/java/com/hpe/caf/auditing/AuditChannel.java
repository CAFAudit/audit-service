package com.hpe.caf.auditing;

import java.io.IOException;

public interface AuditChannel extends AutoCloseable
{
    /**
     * Publish a message.
     *
     * @param routingKey the routing key
     * @param body the message body
     * @throws java.io.IOException if an error is encountered
     */
    void publish(String routingKey, byte[] body) throws IOException;

    /**
     * Declare a queue.
     *
     * @param queueName the name of the queue
     * @throws java.io.IOException if an error is encountered
     */
    void declareQueue(String queueName) throws IOException;
}
