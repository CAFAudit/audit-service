## CAF_7006 - Test audit management web service with incorrect database connection details ##

Verify that the audit management web service throws the correct error when incorrect database connection details are used

**Test Steps**

- Call the audit management web service to create a new application using incorrect database connection details

**Test Data**

N/A

**Expected Result**

The web service should return a status 400 Bad Request and correct error message shown

**JIRA Link** - [CAF-491](https://jira.autonomy.com/browse/CAF-491)
