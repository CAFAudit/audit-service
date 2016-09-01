## CAF_7011 - Add tenant with valid characters in tenantId ##

Verify that the audit management web service adds the tenant successfully with account_ prepended onto the name of the tenant in Vertica

**Test Steps**

1. Call the audit management web service to create a new application
2. Add a tenant using only lowercase letters and numbers

**Test Data**

tenantIds containing lowercase characters and numbers

**Expected Result**

The web service will create the tenant adit event table successfully prepended with account_ in the Vertica database and the scheduler table will be called auditscheduler_tenantname. The scheduler containers will be successfully deployed and the audit qa utility will run as expected.

**JIRA Link** - [CAF-941](https://jira.autonomy.com/browse/CAF-941)
