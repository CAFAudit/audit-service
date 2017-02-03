## CAF_7007 - Automate Vertica scheduler configuration and launch  - DEPRECATED##

Verify that the configuration and launch of the Vertica scheduler is automated

**Test Steps**

1. Create a number of tenants using the audit management web service
2. Check that the Vertica scheduler is configured and launched automatically in a marathon container
3. Run the audit qa utility to verify that the audit events make it into the Vertica database

**Test Data**

N/A

**Expected Result**

The Vertica scheduler is automatically configured and launched for each tenant. Any audit events sent to Kafka are picked up and written to Vertica.

**JIRA Link** - [CAF-500](https://jira.autonomy.com/browse/CAF-500), [CAF-651](https://jira.autonomy.com/browse/CAF-651)

**Notes** - This is now deprecated by [CAF-1377](https://jira.autonomy.com/browse/CAF-1377) as there is only a single Kafka-Vertica scheduler for all tenants that is pre-configured.
