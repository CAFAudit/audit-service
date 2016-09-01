## CAF_7008 - Audit multi-tenancy ##

Verify that audit events are logged for the correct tenants

**Test Steps**

1. Create a number of tenants and applications using the audit management web service
2. Use the test harness to send audit events for each of the tenants and applications
3. Check that the audit events are stored in the correct Vertica database for each tenant and application

**Test Data**

N/A

**Expected Result**

Audit events are successfully stored in the correct Vertica database depending on the tenant that sent them.

**JIRA Link** - [CAF-490](https://jira.autonomy.com/browse/CAF-490)
