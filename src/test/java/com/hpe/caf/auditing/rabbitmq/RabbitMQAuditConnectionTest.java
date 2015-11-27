package com.hpe.caf.auditing.rabbitmq;

import com.hpe.caf.auditing.rabbitmq.RabbitMQAuditConnection;
import com.rabbitmq.client.Connection;
import org.junit.Test;
import org.mockito.Mockito;

public class RabbitMQAuditConnectionTest {

    @Test(expected = Exception.class)
    public void testCreateChannel() throws Exception {

        RabbitMQAuditConnection connection = Mockito.mock(RabbitMQAuditConnection.class);
        Mockito.doThrow(new Exception()).when(connection).createChannel();
        connection.createChannel();
    }

    @Test(expected = Exception.class)
    public void testClose() throws Exception {

        Connection rabbitMQConnection = Mockito.mock(Connection.class);

        RabbitMQAuditConnection connection = Mockito.mock(RabbitMQAuditConnection.class);
        Mockito.doThrow(new Exception()).when(connection).close();
        connection.close();
    }

}
