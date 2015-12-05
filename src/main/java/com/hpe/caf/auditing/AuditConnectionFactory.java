package com.hpe.caf.auditing;

import com.hpe.caf.api.ConfigurationException;
import com.hpe.caf.api.ConfigurationSource;
import com.hpe.caf.auditing.kafka.KafkaAuditConnection;

public class AuditConnectionFactory {

    /**
     * Create Kafka connection for the Audit application using the specified
     * list of seed brokers.
     */
    public static AuditConnection createConnection(final ConfigurationSource configSource) throws ConfigurationException {
        return new KafkaAuditConnection(configSource);
    }
}
