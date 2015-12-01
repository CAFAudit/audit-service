package com.hpe.caf.auditing.kafka;

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditEventBuilder;
import org.apache.kafka.clients.producer.Producer;

final class KafkaAuditChannel implements AuditChannel
{
    private Producer<String, String> producer;

    public KafkaAuditChannel(final Producer<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public AuditEventBuilder createEventBuilder() {
        return new KafkaAuditEventBuilder(producer);
    }

    @Override
    public void close() {
        producer = null;
    }
}
