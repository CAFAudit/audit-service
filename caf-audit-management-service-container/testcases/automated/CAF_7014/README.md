## CAF_7014 - Vertica automatically picking up new partitions ##

Verify that Vertica automatically picks up new partitions in the Kafka auditing topic so no audit events are lost

**Test Steps**

1. Start Kafka and Vertica 
2. Start the caf-audit-management-service
3. Connect to the swagger UI page for caf-audit-management-service 
4. Add application
5. Add tenant and register with Application
6. Send messages and verify that they are received by your scheduler and taken into your vertica database. 
7. Increment the number of partitions in kafka by the following command: 
`/opt/kafka/bin/kafka-topics.sh --zookeeper localhost:2181 --alter --topic AuditEventTopic.MyApp.tenant1 --partitions 3` 
8. Send messages to kafka. Keep sending until you observe messages not getting received. This will be because the messages are in a different partition. (you can verify that messages are in a specific partition with the following command: 
`/opt/kafka/bin/kafka-run-class.sh kafka.tools.SimpleConsumerShell --broker-list <broker address>:<port> --topic AuditEventTopic.MyDemo.tenant1 --partition 0` )
9. Call the UpdatePartitions web method from swagger UI with your application and tenant IDs

**Test Data**

N/A

**Expected Result**

Once the UpdatePartitions calls is made the messages will be received into the Vertica database

**JIRA Link** - [CAF-460](https://jira.autonomy.com/browse/CAF-460)
