package com.hpe.caf.auditing;

import com.hpe.caf.util.rabbitmq.RabbitUtil;
import com.rabbitmq.client.Connection;
import net.jodah.lyra.ConnectionOptions;
import net.jodah.lyra.config.Config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQAuditConnection implements AuditConnection{

    private Connection rabbitMQConnection;

    /**
     * Constructor to create a new Lyra managed RabbitMQ connection with a default Lyra configuration.
     * @param host the host or IP running RabbitMQ
     * @param port the port that the RabbitMQ server is exposed on
     * @param user the username to use when authenticating with RabbitMQ
     * @param pass the password to use when authenticating with RabbitMQ
     * @return a valid connection to RabbitMQ
     * @throws IOException if the connection fails to establish
     * @throws TimeoutException if the connection fails to establish
     */
    public RabbitMQAuditConnection(String host, int port, String user, String pass) throws IOException, TimeoutException {

        this.rabbitMQConnection = RabbitUtil.createRabbitConnection(host, port, user, pass);
    }

    /**
     * Constructor to create a new Lyra managed RabbitMQ connection with custom settings.
     * @param opts the Lyra ConnectionOptions
     * @param config the Lyra Config
     * @return a valid connection to RabbitMQ, managed by Lyra
     * @throws IOException if the connection fails to establish
     * @throws TimeoutException if the connection fails to establish
     */
    public RabbitMQAuditConnection(ConnectionOptions opts, Config config)
            throws IOException, TimeoutException
    {
        this.rabbitMQConnection = RabbitUtil.createRabbitConnection(opts, config);
    }

    /**
     * Create a RabbitMQ channel for the audit application.
     * @return a valid RabbitMQ channel
     * @throws IOException if the channel cannot be created
     */
    @Override
    public AuditChannel createChannel() throws IOException {
        return new RabbitMQAuditChannel(rabbitMQConnection.createChannel());
    }

    /**
     * Close the RabbitMQ connection.
     */
    @Override
    public void close() throws Exception {
        rabbitMQConnection.close();
    }
}
