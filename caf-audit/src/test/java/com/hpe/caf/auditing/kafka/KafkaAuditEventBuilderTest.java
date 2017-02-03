package com.hpe.caf.auditing.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.doThrow;

public class KafkaAuditEventBuilderTest {

    @Test(expected = Exception.class)
    public void testSend() throws Exception {

        Producer<String, String> mockProducer = Mockito.mock(Producer.class);
        doThrow(new RuntimeException()).when(mockProducer).send(Mockito.any());

        KafkaAuditEventBuilder eventBuilder = new KafkaAuditEventBuilder(mockProducer);
        eventBuilder.setApplication("TestApplication");
        eventBuilder.setUser("TestUser");
        eventBuilder.setEventType("TestCategory","TestType");
        eventBuilder.addEventParameter("param1","param1","test");
        eventBuilder.addEventParameter("param2","param2",Short.MAX_VALUE);
        eventBuilder.addEventParameter("param3","param3",Integer.MAX_VALUE);
        eventBuilder.addEventParameter("param4","param4",Long.MAX_VALUE);
        eventBuilder.addEventParameter("param5","param5",Float.MAX_VALUE);
        eventBuilder.addEventParameter("param6","param6", Double.MAX_VALUE);
        eventBuilder.addEventParameter("param7","param7", true);
        eventBuilder.send();
    }

}
