## CAF_7016 - Set Vertica topic partitions to match Kafka partitions when adding application ##

Verify that Vertica topic partition count matches the Kafka partition count when adding an application

**Test Steps**

1. Start Vertica and Kafka 
2. Start the caf-audit-management-service
3. Connect to the swagger UI page for caf-audit-management-service 
4. Add application with your xml (here its called MyDemo) 
5. Go into Kafka and create a topic (tenant1) using the command: 
/opt/kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 6 --topic AuditEventTopic.MyDemo.tenant1 
6. Add a tenant using swagger UI with the message body:
{ 
    ""tenantId"":""tenant1"", 
    ""application"":[""MyDemo""] 
} 
7. Send messages to Kafka to the topic and observe all messages are put into your CAFAudit database or look at the result from this select statement:
select * from auditscheduler_tenant1.kafka_offsets_topk;

**Test Data**

N/A

**Expected Result**

There are entries for all partitions (in this case 0-5 in kpartition column)

**JIRA Link** - [CAF-951](https://jira.autonomy.com/browse/CAF-951)
