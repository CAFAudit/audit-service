## CAF_7009 - Audit management web service - Vertica scheduler per tenant - DEPRECATED##

Verify that new applications and new tenants can be added using the audit management web service. There will be only one scheduler configured per tenant

**Test Steps**

1. Call the audit management web service to create a number of new applications using valid xml files
2. The xml for the audit event definition will be stored in the Vertica database
3. Call the audit management web service to create a number of new tenants configuring them to use some of the new applications
4. Check the Vertica database and there should be a new database schema for the new tenant with an application table for each application
5. Verify that only a single scheduler per tenant is configured in the Vertica database
6. The schema tables for each Vertica scheduler will also register each target database table where the tenant has been registered with more than one application. (kafka\_offsets table, target\_schema column and target\_table column)

**Test Data**

N/A

**Expected Result**

Only one scheduler is registered for each tenant no matter how many applications are configured for the tenant. Where the tenant is configured with more than one application the Vertica scheduler schema tables will include details of all applications configured for the tenant.

**JIRA Link** - [CAF-668](https://jira.autonomy.com/browse/CAF-668)

**Notes** - This is now deprecated by [CAF-1377](https://jira.autonomy.com/browse/CAF-1377) as there is only a single Kafka-Vertica scheduler for all tenants that is pre-configured.
