## CAF_7004 - Test audit management web service ##

Verify that new applications and new tenants can be added using the audit management web service

**Test Steps**

1. Call the audit management web service to create a number of new applications using valid xml files
2. The xml for the audit event definition will be stored in the Vertica database
3. Call the audit management web service to create a new tenant configuring it to use some of the new applications
4. Check the Vertica database and there should be a new database schema for the new tenant with an application table for each application

**Test Data**

N/A

**Expected Result**

The new tenant is successfully created in the Vertica database with tables for each application

**JIRA Link** - [CAF-491](https://jira.autonomy.com/browse/CAF-491)
