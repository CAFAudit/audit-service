package com.hpe.caf.auditing.kafka;

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;

public final class KafkaAuditConnection implements AuditConnection
{
    private final Producer<String, String> producer;

    public KafkaAuditConnection(String kafkaBrokers)
    {
        final Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBrokers);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("linger.ms", 0);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer(props);
    }

    @Override
    public AuditChannel createChannel() {
        return new KafkaAuditChannel(producer);
    }

    @Override
    public void close() {
        producer.close();
    }
}
