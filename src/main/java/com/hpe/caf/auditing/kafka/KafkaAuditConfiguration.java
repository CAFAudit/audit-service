package com.hpe.caf.auditing.kafka;

import com.hpe.caf.api.Configuration;

/**
 * The Kafka audit configuration class.
 */
@Configuration
public class KafkaAuditConfiguration {

    /**
     * A list of host/port pairs to use for establishing the initial connection to the Kafka cluster.
     * This list should be in the form host1:port1,host2:port2,....
     */
    private String bootstrapServers;

    /**
     * The number of acknowledgments the producer requires the leader to have received before
     * considering a request complete.
     *      acks=0 If set to zero then the producer will not wait for any acknowledgment from the server at all.
     *      acks=1 This will mean the leader will write the record to its local log but will respond without
     *      awaiting full acknowledgement from all followers.
     *      acks=all This means the leader will wait for the full set of in-sync replicas to acknowledge the record.
     */
    private String acks = "all";

    /**
     * Setting a value greater than zero will cause the client to resend any record whose send fails with
     * a potentially transient error.
     */
    private int retries = 0;

    public KafkaAuditConfiguration() {

    }

    /**
     * Getter for property 'bootstrapServers'.
     *
     * @return Value for property 'bootstrapServers'.
     */
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    /**
     * Setter for property 'bootstrapServers'.
     *
     * @param bootstrapServers Value to set for property 'queueHost'.
     */
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Getter for property 'acks'.
     *
     * @return Value for property 'acks'.
     */
    public String getAcks() {
        return acks;
    }

    /**
     * Setter for property 'acks'.
     *
     * @param acks Value to set for property 'queueHost'.
     */
    public void setAcks(String acks) {
        this.acks = acks;
    }

    /**
     * Getter for property 'retries'.
     *
     * @return Value for property 'retries'.
     */
    public int getRetries() {
        return retries;
    }

    /**
     * Setter for property 'retries'.
     *
     * @param retries Value to set for property 'queueHost'.
     */
    public void setRetries(int retries) {
        this.retries = retries;
    }
}
