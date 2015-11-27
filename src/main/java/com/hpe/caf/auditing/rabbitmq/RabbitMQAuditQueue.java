package com.hpe.caf.auditing.rabbitmq;

final class RabbitMQAuditQueue
{
    private RabbitMQAuditQueue() {
    }

    public static String getQueueName(String applicationId) {
        return "AuditEventQueue." + applicationId;
    }
}
