## CAF_7012 - Add tenant with invalid characters in tenantId ##

Verify that the audit management web service throws the correct error when invalid characters are used in the tenantId

**Test Steps**

1. Call the audit management web service to create a new application
2. Add a tenant using uppercase letters, hyphens and periods

**Test Data**

tenantIds containing uppercase characters, hyphens and periods

**Expected Result**

The web service will reject the tenantId and a 400 error message is displayed.

**JIRA Link** - [CAF-941](https://jira.autonomy.com/browse/CAF-941)
