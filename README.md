# Auditing Test Project
This testing project sends a set of audit event messages to the Apache Kafka messaging service and then verifies the audit event details have been loaded from Kafka into the Vertica database.

## Audit Events Definition File
In order to use CAF Auditing in a test application, the auditing events that the application uses must be specified along with the parameters that are associated with each of the events in an [Audit Event Definition File](https://github.hpe.com/caf/caf-audit-schema/blob/develop/README.md). The definition file used for testing in this project can be found in the `\sample-test-scripts\xml\` folder:

	<?xml version="1.0" encoding="UTF-8"?>
	<AuditedApplication
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xmlns="http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd"
			xsi:schemaLocation="http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd"
	>
		<ApplicationId>TestApplication</ApplicationId>
		<AuditEvents>
			<AuditEvent>
				<TypeId>TestEvent1</TypeId>
				<CategoryId>TestCategory1</CategoryId>
				<Params>
					<Param>
						<Name>StringType</Name>
						<Type>string</Type>
						<ColumnName>StringType</ColumnName>
						<Description>Description for StringType</Description>
					</Param>
					<Param>
						<Name>Int16Type</Name>
						<Type>short</Type>
						<ColumnName>Int16Type</ColumnName>
						<Description>Description for Int16Type</Description>
					</Param>
					<Param>
						<Name>Int32Type</Name>
						<Type>int</Type>
						<ColumnName>Int32Type</ColumnName>
						<Description>Description for Int32Type</Description>
					</Param>
					<Param>
						<Name>Int64Type</Name>
						<Type>long</Type>
						<ColumnName>Int64Type</ColumnName>
						<Description>Description for Int64Type</Description>
					</Param>
					<Param>
						<Name>FloatType</Name>
						<Type>float</Type>
						<ColumnName>FloatType</ColumnName>
						<Description>Description for FloatType</Description>
					</Param>
					<Param>
						<Name>DoubleType</Name>
						<Type>double</Type>
						<ColumnName>DoubleType</ColumnName>
						<Description>Description for DoubleType</Description>
					</Param>
					<Param>
						<Name>BooleanType</Name>
						<Type>boolean</Type>
						<ColumnName>BooleanType</ColumnName>
						<Description>Description for BooleanType</Description>
					</Param>
					<Param>
						<Name>DateType</Name>
						<Type>date</Type>
						<ColumnName>DateType</ColumnName>
						<Description>Description for DateType</Description>
					</Param>
				</Params>
			</AuditEvent>
			<AuditEvent>
				<TypeId>TestEvent2</TypeId>
				<CategoryId>TestCategory2</CategoryId>
				<Params>
					<Param>
						<Name>StringType2</Name>
						<Type>string</Type>
						<ColumnName>StringType2</ColumnName>
						<Description>Description for StringType2</Description>
					</Param>
					<Param>
						<Name>Int16Type2</Name>
						<Type>short</Type>
						<ColumnName>Int16Type2</ColumnName>
						<Description>Description for Int16Type2</Description>
					</Param>
				</Params>
			</AuditEvent>
			<AuditEvent>
				<TypeId>TestEvent3</TypeId>
				<CategoryId>TestCategory3</CategoryId>
				<Params>
					<Param>
						<Name>StringType3</Name>
						<Type>string</Type>
						<ColumnName>StringType3</ColumnName>
						<Description>Description for StringType3</Description>
					</Param>
					<Param>
						<Name>DateType2</Name>
						<Type>date</Type>
						<ColumnName>DateType2</ColumnName>
						<Description>Description for DateType2</Description>
					</Param>
				</Params>
			</AuditEvent>
		</AuditEvents>
	</AuditedApplication>

## Audit Events Test Data
The audit event message test data should be defined in a yaml file named `caf-audit-qa.yaml` in the root folder of the project. The yaml definition needs to include the number of messages to be sent, the [auto-generated auditing class](https://github.hpe.com/caf/caf-audit-maven-plugin) method to be called for the audit event along with test values for each of the parameters associated with the audit event. A sample test data file for the audit events XML presented above is given next:

	---
	numberOfMessages: 3
	messages:
	- auditLogMethodParams:
	  - name: "tenantId"
	    value: "tenant1"
	  - name: "userId"
	    value: "user1@hpe.com"
	  - name: "correlationId"
	    value: "correlation1"
	  - name: "StringType"
	    value: "user1"
	  - name: "Int16Type"
	    value: 1
	  - name: "Int32Type"
	    value: 1
	  - name: "Int64Type"
	    value: 1
	  - name: "FloatType"
	    value: 123.45
	  - name: "DoubleType"
	    value: 678.8
	  - name: "BooleanType"
	    value: true
	  - name: "DateType"
	    value: 2016-01-14 12:45:26.319
	  auditLogMethod: "auditTestEvent1"
	- auditLogMethodParams:
	  - name: "tenantId"
	    value: "tenant1"
	  - name: "userId"
	    value: "user2@hpe.com"
	  - name: "correlationId"
	    value: "correlation2"
	  - name: "StringType"
	    value: "user1"
	  - name: "Int16Type"
	    value: 2
	  - name: "Int32Type"
	    value: 2
	  - name: "Int64Type"
	    value: 2
	  - name: "FloatType"
	    value: 23.45
	  - name: "DoubleType"
	    value: 123.4
	  - name: "BooleanType"
	    value: false
	  - name: "DateType"
	    value: 2016-01-18 12:45:26.319
	  auditLogMethod: "auditTestEvent1"
	- auditLogMethodParams:
	  - name: "tenantId"
	    value: "tenant1"
	  - name: "userId"
	    value: "user3@hpe.com"
	  - name: "correlationId"
	    value: "correlation3"
	  - name: "StringType2"
	    value: "user3"
	  - name: "Int16Type2"
	    value: 3
	  auditLogMethod: "auditTestEvent2"
 
In this example, 3 messages will be sent to Kafka for the tenant with id `tenant1` i.e. 2 for audit event `TestEvent1` and 1 for audit event `TestEvent2`.

## Apache Kafka Configuration
Kafka details should be provided in the `cfg_caf_audit-qa_KafkaAuditConfigurationfile` config file which can be found in the `sample-test-scripts\config\` folder. This needs to include the ip address of the Kafka broker.

## Vertica Configuration
Vertica database details should be provided in the `cfg_caf_audit-qa_VerticaAuditConfiguration` config file which can be found in the `sample-test-scripts\config\` folder. This needs to include the database connection url, username and password for the Vertica database as well as the target database table that the audit event message data will be loaded into.

## Database Set-up 
The project assumes that the necessary database schema has been created in Vertica for both the application defined audit events XML and the tenant specified in the test data file. The [CAF Audit Management Web Service](https://github.hpe.com/caf/caf-audit-management-service) should be used beforehand to register the application audit events XML and create the tenant within the application.

## Vertica Job Scheduler
The project assumes that a Vertica job scheduler has been created and launched for the tenant used in the test data. The role of the job scheduler is to load the audit event data from Kafka into the Vertica database. When the tenant is added within the application using the [CAF Audit Management Web Service](https://github.hpe.com/caf/caf-audit-management-service), the job scheduler will be automatically created and launched.
 