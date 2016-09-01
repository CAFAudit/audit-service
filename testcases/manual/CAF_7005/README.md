## CAF_7005 - Test audit management web service with invalid xml input ##

Verify that the audit management web service throws the correct error when invalid xml is used as the input

**Test Steps**



- Call the audit management web service to create a new application using an invalid xml file

**Test Data**

N/A

**Expected Result**

The web service should return a status 400 Bad Request and correct error message shown

**JIRA Link** - [CAF-491](https://jira.autonomy.com/browse/CAF-491)
