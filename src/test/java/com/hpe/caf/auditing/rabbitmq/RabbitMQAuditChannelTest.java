package com.hpe.caf.auditing.rabbitmq;

import org.junit.Test;
import org.mockito.Mockito;

public class RabbitMQAuditChannelTest {

    @Test(expected = Exception.class)
    public void testClose() throws Exception {

        RabbitMQAuditChannel channel = Mockito.mock(RabbitMQAuditChannel.class);
        Mockito.doThrow(new Exception()).when(channel).close();
        channel.close();
    }

    @Test(expected = Exception.class)
    public void testDeclareApplication() throws Exception {

        RabbitMQAuditChannel channel = Mockito.mock(RabbitMQAuditChannel.class);
        Mockito.doThrow(new Exception()).when(channel).declareApplication(Mockito.anyString());
        channel.declareApplication(Mockito.anyString());
    }
}
