## CAF_7003 - Test audit event integration between Kakfa and Vertica ##

Verify that audit messages sent to Kakfa are consumed by Vertica and can be retrieved from Vertica

**Test Steps**

- Use the audit event test harness to send a specific number of audit messages to Kakfa
- Query the Vertica database to return the sent messages
- Test with both small numbers and large numbers of messages

**Test Data**

N/A

**Expected Result**

The number of messages returned from Vertica exactly matches the number of messages sent to Kafka

**JIRA Link** - [CAF-437](https://jira.autonomy.com/browse/CAF-437)
