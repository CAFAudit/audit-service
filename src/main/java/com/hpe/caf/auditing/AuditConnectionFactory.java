package com.hpe.caf.auditing;

import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.config.Config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AuditConnectionFactory {

    /**
     * Create RabbitMQ connection for the Audit application using the supplied configuration file.
     * @param config audit configuration class
     */
    public static AuditConnection createConnection(AuditConfiguration config) throws IOException, TimeoutException {
        return new RabbitMQAuditConnection(config.getQueueHost(), config.getQueuePort(), config.getQueueUser(), config.getQueuePassword());
    }

    /**
     * Create RabbitMQ connection for the Audit application using the supplied host, port, user and password parameters.
     * @param host the host or IP running RabbitMQ
     * @param port the port that the RabbitMQ server is exposed on
     * @param user the username to use when authenticating with RabbitMQ
     * @param password the password to use when autenticating with RabbitMQ
     * @return a valid connection to RabbitMQ, managed by Lyra
     */
    public static AuditConnection createConnection(String host, int port, String user, String password) throws IOException, TimeoutException {
        return new RabbitMQAuditConnection(host, port, user, password);
    }

    /**
     * Create RabbitMQ connection for the Audit application using the supplied Lyra options and configuration files.
     * @param opts the Lyra ConnectionOptions
     * @param config the Lyra Config
     */
    public static AuditConnection createConnection(ConnectionOptions opts, Config config) throws IOException, TimeoutException {
        return new RabbitMQAuditConnection(opts, config);
    }
}
