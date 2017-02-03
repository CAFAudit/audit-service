package com.hpe.caf.auditing.kafka;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditConnection;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;

public final class KafkaAuditConnection implements AuditConnection
{
    private final Producer<String, String> producer;

    public KafkaAuditConnection(ConfigurationSource configSource) throws ConfigurationException {
        final KafkaAuditConfiguration config = configSource.getConfiguration(KafkaAuditConfiguration.class);

        final Properties props = new Properties();
        props.put("bootstrap.servers", config.getBootstrapServers());
        props.put("acks", config.getAcks());
        props.put("retries", config.getRetries());
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
