package com.hpe.caf.auditing.kafka;

import org.junit.Test;
import org.mockito.Mockito;

public class KafkaAuditChannelTest {

    @Test(expected = Exception.class)
    public void testClose() throws Exception {

        KafkaAuditChannel channel = Mockito.mock(KafkaAuditChannel.class);
        Mockito.doThrow(new Exception()).when(channel).close();
        channel.close();
    }

    @Test(expected = Exception.class)
    public void testCreateEventBuilder() throws Exception {

        KafkaAuditChannel channel = Mockito.mock(KafkaAuditChannel.class);
        Mockito.doThrow(new Exception()).when(channel).createEventBuilder();
        channel.createEventBuilder();
    }

}
