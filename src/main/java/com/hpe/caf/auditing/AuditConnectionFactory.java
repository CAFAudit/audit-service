package com.hpe.caf.auditing;

import com.hpe.caf.auditing.kafka.KafkaAuditConnection;

public class AuditConnectionFactory {

    /**
     * Create Kafka connection for the Audit application using the specified
     * list of seed brokers.
     */
    public static AuditConnection createConnection(String kafkaBrokers)
    {
        return new KafkaAuditConnection(kafkaBrokers);
    }
}
