package com.hpe.caf.auditing.kafka;

import org.junit.Assert;
import org.junit.Test;

public class KafkaAuditTopicTest {

    @Test
    public void testGetTopicName() throws Exception {
        String topicName = KafkaAuditTopic.getTopicName("TestTopic");
        Assert.assertEquals("AuditEventTopic.TestTopic",topicName);
    }

}
