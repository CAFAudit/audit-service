## CAF_7015 - Update partitions web service method - negative tests ##

Verify that the correct error codes are returned when invalid information is passed to the Update Partitions web service method

**Test Steps**

1. Call the Update Partitions method from the CAF Audit Management Service using a tenantId that contains invalid characters
2. Call the Update Partitions method from the CAF Audit Management Service using a tenantId that does not exist
3. Call the Update Partitions method from the CAF Audit Management Service using an applicationId that does not exist

**Test Data**

N/A

**Expected Result**

1. Error code 400 - Bad Request is returned
2. Error code 404 - Not Found is returned
3. Error code 404 - Not Found is returned
 
**JIRA Link** - [CAF-460](https://jira.autonomy.com/browse/CAF-460)
