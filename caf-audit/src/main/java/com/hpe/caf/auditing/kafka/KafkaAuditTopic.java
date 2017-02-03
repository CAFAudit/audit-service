package com.hpe.caf.auditing.kafka;

final class KafkaAuditTopic
{
    private KafkaAuditTopic() {
    }

    public static String getTopicName(String applicationId, String tenantId) {
        return "AuditEventTopic." + applicationId + "." + tenantId;
    }
}
