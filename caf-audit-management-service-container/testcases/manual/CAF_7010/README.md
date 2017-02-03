## CAF_7010 - Auditing - allow injection of a no-op library ##

Verify that it is possible to disable Auditing via injection of a no-op library

**Test Steps**

**Test with no-op library:**

1. Unzip CAF-718-TEST.zip (attached JIRA)
2. Browse to CAF-718-TEST\Audit-Jars\ and copy caf-audit-noop-1.0-SNAPSHOT.jar into CAF-718-TEST\
3. Modify yaml and config files as necessary in CAF-718-TEST\
4. Run: java -jar -DCAF_APPNAME=caf/audit-qa -DCAF_CONFIG_PATH=[PATH_TO_TEST_FOLDER_WITH_CONFIG_FILES] caf-audit-qa-1.0-SNAPSHOT.jar
5. Verify from output that no messages have actually been sent to Kafka (i.e. you should see no reference to "kafka-producer*" in output.)
6. You could also double check no topic has been created in Kafka for this test.
                
**Test with op library:**

1. Replace CAF-718-TEST\caf-audit-noop-1.0-SNAPSHOT.jar with CAF-718-TEST\Audit-Jars\caf-audit-1.1-20160321.221435-14.jar.                
2. Run: java -jar -DCAF_APPNAME=caf/audit-qa -DCAF_CONFIG_PATH=[PATH_TO_TEST_FOLDER_WITH_CONFIG_FILES] caf-audit-qa-1.0-SNAPSHOT.jar
3. Verify from output that messages have been sent to Kafka (i.e. you should see references to "kafka-producer*" in output.)
4. You could also double check that a topic has been created and populated in Kafka for this test.


**Test Data**

N/A

**Expected Result**

1. With the no-op library injected no audit events will be sent to Kafka and will also not reach the Vertica database
2. With the op library injected the audit events will be sent to Kafka and inserted into the Vertica database as normal

**JIRA Link** - [CAF-718](https://jira.autonomy.com/browse/CAF-718)
