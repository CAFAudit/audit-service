## CAF_7017 - Use a single Kafka Vertica Scheduler ##

Verify that when new tenants can be added using the audit management web service they all use the single preconfigured Kafka Vertica scheduler.

**Test Steps**

1. Call the audit management web service to create a number of new tenants configuring them to use a variety of the applications
2. Check the Vertica database and there should be a new database schema for the new tenant with an application table for each application
3. Verify that only a single scheduler is configured in the Vertica database and in Marathon
4. The schema tables for each Vertica scheduler will also register each target database table where the tenant has been registered with more than one application. (kafka\_offsets table, target\_schema column and target\_table column)

**Test Data**

N/A

**Expected Result**

All tenants added will use the preconfigured scheduler. Where the tenant is configured with more than one application the Vertica scheduler schema tables will include details of all applications configured for the tenant.

**JIRA Link** - [CAF-1377](https://jira.autonomy.com/browse/CAF-1377)
