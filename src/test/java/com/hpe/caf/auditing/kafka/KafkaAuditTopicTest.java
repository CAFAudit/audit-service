package com.hpe.caf.auditing.kafka;

import org.junit.Assert;
import org.junit.Test;

public class KafkaAuditTopicTest {

    @Test
    public void testGetTopicName() throws Exception {
        String topicName = KafkaAuditTopic.getTopicName("App","Tenant");
        Assert.assertEquals("AuditEventTopic.App.Tenant",topicName);
    }

}
