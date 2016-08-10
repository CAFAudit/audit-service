# Upgrade AMWS 1.4 (or earlier) for Single Kafka Vertica Scheduler Usage

The CAF Audit Management Web Service (AMWS) has been modified to use a single Kafka-Vertica scheduler for all tenants. See [CAF-1377](https://jira.autonomy.com/browse/CAF-1377) for further details. The purpose of this document is to provide a set of upgrade instructions for both the AMWS as well as any existing Audit scheduler entries in Vertica. The upgrade steps are relevant to AMWS version 1.4 or earlier.

## Upgrade Steps

1. Make a record of the Vertica service and loader account information as specified in the AMWS configuration.

2. Destroy the existing AMWS and any Kafka-Vertica Audit scheduler applications in Marathon. Existing scheduler applications will be registered under the *caf-audit-schedulers* group in Marathon. The *caf-audit-schedulers* group can then be removed as well.

3. Create a single Kafka-Vertica Audit scheduler to handle audit events for all tenants. 

	###### 3.1 Create a new Kafka-Vertica scheduler
	The vkconfig script, which comes pre-packaged and installed with the Vertica rpm, should be used with the *scheduler* command and *--add* option to do this. Note that the database loader account information recorded in *step 1* is required here:

			/opt/vertica/packages/kafka/bin/vkconfig scheduler --add 
				--config-schema auditscheduler 
				--brokers [BROKERS] 
				--username [LOADER-USERNAME] 
				--password [LOADER-PASSWORD] 
				--operator [OPERATOR]
	
	where:
	
	* [BROKERS] - This specifies the kafka broker(s) to be used, it is formatted as a comma separated list of address:port endpoints.
	* [LOADER-USERNAME] - This is the vertica database loader account name recorded in *step 1* (e.g. "caf-audit-loader").
	* [LOADER-PASSWORD] - This is the password for the vertica database loader account recorded in *step 1*.
	* [OPERATOR] - This is the vertica database loader account name wrapped in double quotes(e.g. "\"caf-audit-loader\"").
	
	Example:
	
			/opt/vertica/packages/kafka/bin/vkconfig scheduler --add 
				--config-schema auditscheduler 
				--brokers 192.168.56.20:9092 
				--username "caf-audit-loader" 
				--password c@FaL0Ad3r 
				--operator "\"caf-audit-loader\""

	Note: This step assumes you have followed the pre-requisite steps after Vertica installation for AMWS usage. See [here](https://github.hpe.com/caf/chateau/blob/develop/services/audit-management/README.md) for further details which describes the database role and service accounts to be created.

	###### 3.2 Launch the new Kafka-Vertica scheduler
	The vkconfig script is now used to launch the scheduler using the *launch* command to do this. Note that the database loader account information recorded in *step 1* is also required here:

			/opt/vertica/packages/kafka/bin/vkconfig launch 
				--config-schema auditscheduler 
				--username [LOADER-USERNAME] 
				--password [LOADER-PASSWORD] 
	
	where:
	
	* [LOADER-USERNAME] - This is the vertica database loader account name recorded in *step 1* (e.g. "caf-audit-loader").
	* [LOADER-PASSWORD] - This is the password for the vertica database loader account recorded in *step 1*.
	
	Example:
	
			/opt/vertica/packages/kafka/bin/vkconfig launch
				--config-schema auditscheduler 
				--username "caf-audit-loader" 
				--password c@FaL0Ad3r 

	###### 3.3 Grant user access to the new Kafka-Vertica scheduler
	You need grant the AMWS database user access to objects contained within the new Kafka-Vertica scheduler schema. Run the following SQL against the Vertica database:
	
			GRANT USAGE ON SCHEMA auditscheduler TO [SERVICE-USERNAME];

	where:

	* [SERVICE-USERNAME] - This is the vertica database service account name recorded in step 1 (e.g. "caf-audit-service").

4. Deploy the AMWS and Kafka-Vertica Audit scheduler using the [Chateau](https://github.hpe.com/caf/chateau/blob/develop/deployment.md) deployment tool set.

		./deploy-service.sh audit-management

5. Associate existing topics with the new Kafka-Vertica scheduler.

	Existing scheduler entries in the Vertica database need updated so that they are all now managed by the new single Kafka-Vertica scheduler. The vkconfig script should be used to do this using the topic command and --add option. This requires a three step process in order to identify the relevent database entries for existing audit schedulers, generate the necesssary vkconfig commands and then execute each of these vkconfig commands to associate the existing topic with the new scheduler.
	
	The following steps include SQL which can be executed using any database tool that supports a Vertica database connection and allows SQL commands to be run against the Vertica database, e.g. DbVisualizer.

	###### 5.1 Identify existing topics
	First the relevant database entries for existing audit schedulers need identfied. The following SQL should be run against the Vertica database to do this. The output includes a set of further SQL statements which then need to be run in order to generate the correct vkconfig command for each existing topic:

		-- Identify existing scheduler rows and part-generate vkconfig command
		SELECT REPLACE(REPLACE('SELECT ''/opt/vertica/packages/kafka/bin/vkconfig topic --add '' || 
		 '' --config-schema auditscheduler '' || 
		 '' --target '' || target || 
		 '' --rejection-table '' || rejection ||
		 '' --topic ''  || topic ||
		 '' --parser KafkaJSONParser '' ||
		 '' --username caf-audit-loader '' ||
		 '' --password c@FaL0Ad3r '' ||	
		 '' --jdbc-url [JDBC-URL] '' ||	
		 '' --num-partitions 1 '' as ''Associate Topic Command'' ' ||
		'from ' ||	
		'( ' ||	
		'        select distinct ko.target_schema || ''.'' || ko.target_table as ''target'', ko.ktopic as ''topic'', kt.rejection_table as ''rejection'' ' ||	
		'        from [SCHEMA_NAME].kafka_offsets ko ' ||	
		'        join [SCHEMA_NAME].kafka_targets kt ' ||	
		'        on ko.target_schema = kt.target_schema ' ||	
		'        and ko.target_table = kt.target_table ' ||	
		') _ttr;','[SCHEMA_NAME]',schema_name),'[JDBC-URL]','<INSERT JDBC-URL VALUE HERE>')
		FROM schemata 
		WHERE schema_owner = 'caf-audit-loader' 
		AND schema_name <> 'auditscheduler';

	where:

	* <INSERT JDBC-URL VALUE HERE> - should be replaced with the Vertica database connection url (e.g. jdbc:vertica://192.168.56.30:5433/CAFAudit).

	The output from running the above SQL, assuming existing schedulers already exist, will be similar to:

		SELECT '/opt/vertica/packages/kafka/bin/vkconfig topic --add ' || 
		 ' --config-schema auditscheduler ' || 
		 ' --target ' || target || 
		 ' --rejection-table ' || rejection ||
		 ' --topic '  || topic ||
		 ' --parser KafkaJSONParser ' ||
		 ' --username caf-audit-loader ' ||
		 ' --password c@FaL0Ad3r ' || 
		 ' --jdbc-url jdbc:vertica://192.168.56.30:5433/CAFAudit ' || 
		 ' --num-partitions 1 ' as 'Associate Topic Command' from (         select distinct ko.target_schema || '.' || ko.target_table as 'target', ko.ktopic as 'topic', kt.rejection_table as 'rejection'         from auditscheduler_11118980547822592.kafka_offsets ko         join auditscheduler_11118980547822592.kafka_targets kt         on ko.target_schema = kt.target_schema         and ko.target_table = kt.target_table ) _ttr;

	###### 5.2 Generate vkconfig commands
	For each of the SQL commands returned in the output of step 5.1, then run each against the Vertica database. This will return the corresponding vkconfig command in full, similar to that shown below:

		/opt/vertica/packages/kafka/bin/vkconfig topic --add  --config-schema auditscheduler  --target account_11118980547822592.AuditCAFStorageService --rejection-table account_11118980547822592.kafka_rej --topic AuditEventTopic.CAFStorageService.11118980547822592 --parser KafkaJSONParser  --username caf-audit-loader  --password c@FaL0Ad3r  --jdbc-url jdbc:vertica://192.168.56.30:5433/CAFAudit  --num-partitions 1 

	###### 5.3 Execute vkconfig commands

	Finally, each vkconfig statement generated as part of output of step 5.2 should be executed on the Vertica instance to associate the existing topic with the new Kafka-Vertica scheduler.

6. Clean-up the Vertica Database.

	Database rows belonging to the old Kafka-Vertica schedulers for each tenant now need removed from the CAFAudit Vertica database. The relevant schemas belonging to these schedulers need identified, SQL commands generated to drop these schemas and then the SQL commands executed against the Vertica database.

	SQL commands specified in this section can be executed using any database tool that supports a Vertica database connection and allows SQL commands to be run against the Vertica database, e.g. DbVisualizer.

	###### 6.1 Generate clean-up SQL
	First the relevant schemas for existing audit schedulers need identfied and corresponding clean-up SQL generated for each. The following SQL should be run against the Vertica database to do this:

		SELECT REPLACE('DROP SCHEMA CAFAudit.[SCHEMA_NAME] cascade', '[SCHEMA_NAME]', schema_name) 
		FROM schemata 
		WHERE schema_owner = 'caf-audit-loader' 
		AND schema_name <> 'auditscheduler';

	The output includes a set of further SQL statements which then need to be run in order to remove the relevant rows from the database. The output will be similar to:

		DROP SCHEMA CAFAudit.auditscheduler_11118980547822592 cascade

	###### 6.2 Execute clean-up SQL
	Run each of the *DROP SCHEMA* statements returned from step 6.2 against the Vertica database to remove the actual rows from the database belonging to the old audit schedulers.