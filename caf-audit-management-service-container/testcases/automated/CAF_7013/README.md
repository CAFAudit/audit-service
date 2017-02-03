## CAF_7013 - Disabling audit management web service ##

Verify that it is possible to disable Auditing via an environment variable

**Test Steps**

1. Deploy latest audit management service without CAF_AUDIT_MANAGEMENT_DISABLE environment variable present and test application and tenant registration
2. Deploy latest audit management service with CAF_AUDIT_MANAGEMENT_DISABLE environment variable present and set to false, test application and tenant registration
3. Deploy latest audit management service with CAF_AUDIT_MANAGEMENT_DISABLE environment variable present and set to true,  test application and tenant registration

**Test Data**

N/A

**Expected Result**

1. Application and tenant registration works as expected.
2. Application and tenant registration works as expected.
3. Application and tenant registration calls simply return http 200 without doing anything.

**JIRA Link** - [CAF-885](https://jira.autonomy.com/browse/CAF-885)
