# Upgrade AMWS 1.4 (or earlier) for Single Kafka Vertica Scheduler Usage

The CAF Audit Management Web Service (AMWS) has been modified to use a single Kafka-Vertica scheduler for all tenants. See [CAF-1377](https://jira.autonomy.com/browse/CAF-1377) for further details. The purpose of this document is to provide a set of upgrade instructions for both the AMWS as well as any existing Audit scheduler entries in Vertica. The upgrade steps are relevant to AMWS version 1.4 or earlier.

Some of the upgrade steps require the use of a database tool that can connect to the Vertica database to run SQL and drop schema objects. It has been assumed that DbVisualizer will be used for these steps. 

## Upgrade Steps

1. Make a record of the Vertica service and loader account information as specified in the AMWS configuration.

2. Destroy the existing AMWS and any Kafka-Vertica Audit scheduler applications in Marathon. Existing scheduler applications will be registered under the *caf-audit-schedulers* group in Marathon. The *caf-audit-schedulers* group can then be removed as well.

3. Create a single Kafka-Vertica Audit scheduler to handle audit events for all tenants. 

	###### 3.1 Create a new Kafka-Vertica scheduler
	The vkconfig script, which comes pre-packaged and installed with the Vertica rpm, should be used with the *scheduler* command and *--add* option to do this. Note that the database loader account information recorded in step 1 is required here:

			sudo /opt/vertica/packages/kafka/bin/vkconfig scheduler --add 
				--config-schema auditscheduler 
				--brokers [BROKERS] 
				--username "[LOADER-USERNAME]" 
				--password [LOADER-PASSWORD] 
				--operator "\"[OPERATOR]\""
	
	where:
	
	* [BROKERS] - This specifies the kafka broker(s) to be used, it is formatted as a comma separated list of address:port endpoints.
	* [LOADER-USERNAME] - This is the vertica database loader account name recorded in step 1 (e.g. caf-audit-loader).
	* [LOADER-PASSWORD] - This is the password for the vertica database loader account recorded in step 1.
	* [OPERATOR] - This is the vertica database loader account name recorded in step 1 (e.g. caf-audit-loader). 
	
	Example:
	
			sudo /opt/vertica/packages/kafka/bin/vkconfig scheduler --add 
				--config-schema auditscheduler 
				--brokers 192.168.56.20:9092 
				--username "caf-audit-loader" 
				--password c@FaL0Ad3r 
				--operator "\"caf-audit-loader\""

	Note: This step assumes you have followed the pre-requisite steps after Vertica installation for AMWS usage. See [here](https://github.hpe.com/caf/chateau/blob/develop/services/audit-management/README.md) for further details which describes the database role and service accounts to be created.

	###### 3.2 Grant user access to the new Kafka-Vertica scheduler
	You need to grant the AMWS database user access to objects contained within the new Kafka-Vertica scheduler schema. This step is required in order to support AMWS calls to keep Vertica topic configuration and Kafka topic partitions consistent. Run the following SQL against the Vertica database:
	
			GRANT USAGE ON SCHEMA auditscheduler TO "[SERVICE-USERNAME]";

	where:

	* [SERVICE-USERNAME] - This is the vertica database service account name recorded in step 1 (e.g. caf-audit-service).
	
4. Deploy the AMWS and Kafka-Vertica Audit scheduler using the [Chateau](https://github.hpe.com/caf/chateau/blob/develop/deployment.md) deployment tool set.

		./deploy-service.sh audit-management

5. Perform database clean-up.
 
	In order for all tenants to be managed by the new Kafka-Vertica scheduler, existing audit scheduler and tenant related rows need removed from the Vertica database. Tenants can then be registered via the AMWS afterwards.

	###### 5.1 Make a record of all tenants in the system
	Make a record of all tenant identfiers registered in the system. Run the following SQL against the Vertica database to do this:

		SELECT REPLACE(schema_name,'account_','') as TenantId 
		FROM schemata 
		WHERE schema_name LIKE 'account_%';

	###### 5.2 Clean-up database rows.
	Existing scheduler and tenant related entries in the Vertica database need cleaned-up before tenant registration can be performed. This clean-up can be done using the DbVisualizer database tool.

	####### 5.2.1 Audit Scheduler Rows.
	Existing audit scheduler schemas can be identified by the *auditscheduler_* prefix followed by the tenantId. 

	For each of the tenants that has a schema with the *auditscheduler_* prefix, use DbVisualizer to remove each schema by right clicking on the schema name under Schemas in the object tree and select the *Drop Schema...* option. Make sure you check *Cascade Objects* on the resulting dialog before clicking on *Execute*. Then click *Yes* to confirm.

	####### 5.2.2 Tenant Rows.
	Existing tenant related schemas can be identified by the *account_* prefix followed by the tenantId. 

	####### 5.2.2.1 Tenant Tables.
	Each tenant related schema will comprise a number of tables. These include one or more application specific audit tables for storing audit event information as well as a table for storing rejection events. The application audit tables are identified by a *Audit* prefix and appended with the application name. The rejection table is named kafka_rej.

	For each of the tenants recorded in step 5.1, both their application audit and kafka rejection tables need removed. Using DbVisualizer, right click on the table under the tenant related schema and select the *Drop Table...* option. Click on *Execute* and then *Yes* to confirm.

	####### 5.2.2.2 Tenant per Application Rows.
	Tenant per application rows also need removed as part of the clean-up task for all tenants recorded in step 5.1. Using DbVisualizer, right click on the *TenantApplications* table under the *AuditManagement* schema in the object tree and select the *Empty Table...* option. Then click *Execute* on the resulting dialog followed by *Yes* to confirm.
	
6. Register tenants.

	Now that the database clean-up is complete, all tenants can be registered using the AMWS. For each tenant recorded in step 5.1, issue the following cURL command:

		curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
		  "tenantId": "[TENANTID]",
		  "application": [
		    "[APPLICATION]"
		  ]
		}' 'http://[HOSTNAME]:[SERVICEPORT]/caf-audit-management/v1/tenants'

	where:

	* [TENANTID] - This identifies the tenant identifier as recorded in step 5.1.
	* [APPLICATION] - This identifies the application name to use for tenant registration.
	* [HOSTNAME] - This identifies the host machine.
	* [SERVICEPORT] - This property specifies the external port number on the host machine that will be forwarded to the containers internal 8080 port. This port is used to call the CAF Audit Management Service web service.

	Note that this steps requires the cURL executable.

	To verify, make sure a new row has been added to the *tenantApplications* table under the *AuditManagement* schema in the object tree for the specified tenant identifier and application. Further,  additional application specific audit and kafka rejection tables will be created under the existing tenant related schema for the specified tenant.
