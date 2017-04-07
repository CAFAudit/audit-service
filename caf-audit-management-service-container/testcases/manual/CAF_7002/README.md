## CAF_7002 - Database script auto-generation - DEPRECATED##

Run the database script autogeneration script to ensure that Vertica database tables are updated as desired.

**Test Steps**

Use the database utility (see caf-audit-dbutility repository) with an XML audit events file (describing audit data to be captured):

The following instructions assume a Vertica database named CAFAudit is available (see vagrant-vertica).


- Test table creation:
- Create a file named config.properties - for example
	- **AuditEventsDatabaseUtility required inputs**
	- **Vertica database connection string url:** databaseURL=jdbc:vertica://<vertica host>:<port number>/CAFAudit
	- **Database schema name:** schema=public
	- **Username of a database user account:** username=dbadmin
	- **Password of the database user account:** password=CAFAudit
	- **Filepath for the Audit events XML file:** auditEventsXMLFile=C:/Test/AuditConfig.xml
- Run executable jar: java -jar caf-audit-dbutility-1.0-SNAPSHOT-jar-with-dependencies.jar

Assuming the Vertica database, did not already contain a table matching the ApplicationId value in the XML audit event file, then verify that this table has been created with the expected columns matching the audit event parameters specified in the XML file.



- Test table modification:
	- Run executable jar again with the same XML and verify dbutility reports no failure.
	- Modify the XML with additional AuditEvents section and re-run dbutility. This verifies that the database table has been modified with new columns to support new audit event data added."


**Test Data**

N/A

**Expected Result**

The DB utility either creates a new table based on the ApplicationId value if the table does not yet exist or it modifies it by adding new columns to support new audit event parameters.

**JIRA Link** - [CAF-179](https://jira.autonomy.com/browse/CAF-179)
