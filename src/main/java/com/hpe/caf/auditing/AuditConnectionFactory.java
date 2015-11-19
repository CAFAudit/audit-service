package com.hpe.caf.auditing;

import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.config.Config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AuditConnectionFactory {

    public static AuditConnection createConnection(AuditConfiguration config) throws IOException, TimeoutException {
        return new RabbitMQAuditConnection(config.getQueueHost(), config.getQueuePort(), config.getQueueUser(), config.getQueuePassword());
    }

    public static AuditConnection createConnection(String host, int port, String user, String password) throws IOException, TimeoutException {
        return new RabbitMQAuditConnection(host, port, user, password);
    }

    public static AuditConnection createConnection(ConnectionOptions opts, Config config) throws IOException, TimeoutException {
        return new RabbitMQAuditConnection(opts, config);
    }
}
