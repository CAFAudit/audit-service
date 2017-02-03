package com.hpe.caf.auditing.kafka;

import org.junit.Test;
import org.mockito.Mockito;

public class KafkaAuditConnectionTest {

    @Test(expected = Exception.class)
    public void testCreateChannel() throws Exception {

        KafkaAuditConnection connection = Mockito.mock(KafkaAuditConnection.class);
        Mockito.doThrow(new Exception()).when(connection).createChannel();
        connection.createChannel();
    }

    @Test(expected = Exception.class)
    public void testSetApplication() throws Exception {

        KafkaAuditConnection connection = Mockito.mock(KafkaAuditConnection.class);
        Mockito.doThrow(new Exception()).when(connection).close();
        connection.close();
    }

}
