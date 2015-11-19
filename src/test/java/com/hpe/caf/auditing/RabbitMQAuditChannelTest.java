package com.hpe.caf.auditing;

import com.rabbitmq.client.Channel;
import org.junit.Test;
import org.mockito.Mockito;

public class RabbitMQAuditChannelTest {

    @Test(expected = Exception.class)
    public void testPublish() throws Exception {

        RabbitMQAuditChannel channel = Mockito.mock(RabbitMQAuditChannel.class);
        Mockito.doThrow(new Exception()).when(channel).publish(Mockito.anyString(), Mockito.any(byte[].class));
        channel.publish(Mockito.anyString(), Mockito.any(byte[].class));
    }

    @Test(expected = Exception.class)
    public void testClose() throws Exception {

        RabbitMQAuditChannel channel = Mockito.mock(RabbitMQAuditChannel.class);
        Mockito.doThrow(new Exception()).when(channel).close();
        channel.close();
    }

    @Test(expected = Exception.class)
    public void testDeclareQueue() throws Exception {

        RabbitMQAuditChannel channel = Mockito.mock(RabbitMQAuditChannel.class);
        Mockito.doThrow(new Exception()).when(channel).declareQueue(Mockito.anyString());
        channel.declareQueue(Mockito.anyString());
    }
}
