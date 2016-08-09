---
layout: default
title: Getting Started
---

# Getting Started

The high level steps involved in setting up the Audit Mangement Service are:

//TODO: Shift these steps around for the flow of the getting started guide

1. Define audit events in an Audit Event Definition File.
2. Generate the client-side auditing library using the Audit Event Definition File and code generation plugin.
3. Install and configure Apache Kafka on a system.
4. It is recommended, that you install and configure Vertica 7.2.x on a separate machine.
5. Create and launch the Apache Kafka scheduler in order to stream the data from the Kafka messaging service into the Vertica database.
6. Register the application and add tenant(s) with the Audit Management Web Service. This will create the necessary database schema in Vertica. 
7. Use the client-side auditing library to send audit events to Kafka.

**// TODO There needs to be two types of guides:**

1. **Enterprise setup; explaining that Kafka and Vertica can be setup in a clusters and suggest the official documentation to consult.**
2. **Development setup; directing to the use of vagrant-vertica and vagrant-kafka VMs. Explain the security and clusterless caveats of this.**

## Deploying Vertica

Vertica is an SQL database designed for delivering speed, scalability and support for analytics. The Audit Management Service uses Vertica for the storage of streamed applications' tenants' audit events from a Kafka message bus. Analysis tools can then be used on the data to gather metrics of the use of audited applications.

### Enterprise Deployment

For Enterprise deployments of Vertica it is recommended that you follow the official HP Vertica documentation as it covers cluster setup, configuration and backup. Integration of Vertica with your Kafka broker cluster is also covered: [Official HP Vertica Documentation](https://my.vertica.com/documentation/vertica/7-2-x/)

**//TODO SHOULD I ADD OR MAKE REFERENCE TO THE audit-management/README.md HERE? IT CONTAINS COMMANDS REQUIRED TO CREATE THE READER ROLE AND THE DIFFERENT CAF AUDIT USERS?**

### Development Deployment

For Development deployments of Vertica it is recommended that you use [vagrant-vertica](https://github.hpe.com/caf/vagrant-vertica) and follow its supporting documentation to start a guest VM running Vertica with Vagrant.

Vagrant-vertica is not recommended for production deployments, the caveats to using it are that:

- Vertica DB usernames and passwords, used during automated installation, are held as plain text within the VM's provisioning scripts.
- It's a standalone single node setup only; provisioning scripts do not support clustered configurations.

## Deploying Kafka

Apache Kafka is a distributed, partitioned, replicated commit log service that provides messaging system functionality for producers and consumers of messages. Kafka's role in the Audit Management Service is that it receives events from client-side applications (producers) as topic messages. On the server-side the Kafka-Vertica Scheduler (consumer) reads event messages from per application per tenant Kafka topics and streams the events into Vertica.

### Enterprise Deployment

For Enterprise deployments of Kafka it is recommended that you follow the official Apache Kafka documentation as it covers clustered deployments and topic partitioning: [Apache Kafka Documentation](http://kafka.apache.org/documentation.html)

Integration of Vertica with your Kafka broker cluster is covered in the [Official HP Vertica Documentation](https://my.vertica.com/documentation/vertica/7-2-x/)

### Development Deployment

For Development deployments of Vertica it is recommended that you use [vagrant-kafka](https://github.hpe.com/caf/vagrant-kafka) and follow its supporting documentation to start a guest VM running Kafka with Vagrant.

Vagrant-kafka is not recommended for production deployments, the caveats to using it are that:

- It's a standalone single node setup only; provisioning scripts do not support multiple machine clustered configurations.

## Deploying the Audit Management Service

### Audit Management Web Service

The Audit Management Web Service offers a REST API for Audit Management users to register and prepare Vertica and the Kafka-Vertica Scheduler with their applications and tenants using their applications.

### Kafka-Vertica Scheduler

The Kafka-Vertica scheduler is responsible for consuming audit event messages, from per application per tenant Kafka topics, and streaming them into the appropriate Vertica database tables.

### Deployment with Chateau

**[Chateau](https://github.hpe.com/caf/chateau)** can launch workers and services, such as the Audit Management Service and the Kafka-Vertica Scheduler.

- To download and set up Chateau, follow the instructions in the [README.md](https://github.hpe.com/caf/chateau/blob/develop/README.md). 

- Follow the prerequisite instructions for the Audit Management Service [here](https://github.hpe.com/caf/chateau/blob/develop/services/audit-management/README.md). **!!!!(THIS README CONTAINS GOOD INFORMATION FOR REQUIRED VERTICA COMMANDS. IT MIGHT BE GOOD TO REFERENCE THIS DOCUMENT IN THE VERTICA ENTERPRISE DEPLOYMENT SECTION. FOR DEVELOPMENT DEPLOYMENT OF VERTICA THE ONLY COMMAND WITHIN THE DOCUMENT THAT IS REQUIRED TO BE RAN IS THE `vkconfig scheduler --add --config-schema auditscheduler command`.... PERHAPS ALL OF THE COMMANDS IN THE README SHOULD BE BROUGHT OUT INTO THIS DOCUMENT AND ADDED TO THEIR APPROPRIATE SECTIONS?)!!!!**

- To deploy the Audit Management Web Service and the Kafka-Vertica Scheduler, follow the [Service Deployment](https://github.hpe.com/caf/chateau/blob/develop/deployment.md) guide and use the following option with the deployment shell script.

  `./deploy-service.sh audit-management`

**// TODO : Supply a figure that shows the audit management web service and kafka-vertica schedulers running in Marathon**

## Writing an Application Audit Event Definition File

An application for auditing requires the construction of Audit Event Definition XML file that defines the name of the application and its events. With the use of the caf-audit-maven-plugin, the application's definition file is used to generate a client-side library that the audited application calls to log tenant events. The definition file is then also used to register the application and its events for auditing with the server-side Audit Management Web Service.

The figure below illustrates the Audit Event Definition XML File's schema.

![AuditEventDefinitionFileSchema](images/audit-event-definition-file-desc.png)

`AuditedApplication` is the root element.

`ApplicationId` identifies the application that the Audit Events are associated with.

For each Audit Event defined, `TypeId` is a string identifier for the particular event (e.g. viewDocument) and `CategoryId` is a string identifier for the category of the event.

A list of parameter elements are then defined for each Audit Event. This includes the `Name` of the parameter, the `Type` (i.e. string, short, int, long, float, double, boolean or date) and the `Description`. The `ColumnName` element is optional which can be used to force the use of a particular database column when storing the audit data. The `Constraints` element is also optional and this can be used to specify minimum and/or maximum length constraints for audit event parameters of `Type` string.

The following is an example of an Audit Event Definition File that is used to throughout this getting started guide:

	<?xml version="1.0" encoding="UTF-8"?>
	<AuditedApplication xmlns="http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd">
	  <ApplicationId>SampleApp</ApplicationId>
	  <AuditEvents>
	    <AuditEvent>
	      <TypeId>viewDocument</TypeId>
	      <CategoryId>documentEvents</CategoryId>
	      <Params>
	        <Param>
	          <Name>docId</Name>
	          <Type>long</Type>
	          <Description>Document Identifier</Description>
	        </Param>
	      </Params>
	    </AuditEvent>
	    <AuditEvent>
	      <TypeId>deleteDocument</TypeId>
	      <CategoryId>documentEvents</CategoryId>
	      <Params>
	        <Param>
	          <Name>docId</Name>
	          <Type>long</Type>
	          <Description>Document Identifier</Description>
	        </Param>
	        <Param>
	          <Name>authorisedBy</Name>
	          <Type>string</Type>
			  <Constraints>
				<MinLength>1</MinLength>
				<MaxLength>256</MaxLength>
			  </Constraints>
	          <Description>User who authorised the deletion</Description>
	        </Param>
	      </Params>
	    </AuditEvent>
	  </AuditEvents>
	</AuditedApplication>

## Using the Audit Management Web Service

To start using the web service, the endpoints can be exercised by accessing the Swagger Web UI at the following URL:

	http://<audit.web.service.host.address>:<port>/caf-audit-management-ui

Replace `<audit.web.service.host.address>` and `<port>` as necessary.

### Loading the XML Audit Events File

Application audit events that will occur are defined within the Audit Event Definition File. This file is then used to register the application on the server-side using the /applications endpoint as shown in the screenshot below:

![Overview](images/addApplication.png)

#### Verification Instructions

When an application events file is registered, this operation configures the Vertica database with audit management tables to record both the application specific audit events XML as well as tenants added through the service. See tables ApplicationEvents and TenantApplications under the AuditManagement schema in the Vertica database. An entry in the ApplicationEvents table will also be created to register the application events XML supplied. **TODO CONFIRM THIS BY CHECKING VERTICA FOR THESE TABLES AFTER SUPPLYING THE DEFINITION FILE**

Any further calls to load other application events XML will result in additional rows being added to the ApplicationEvents table only. **TODO CONFIRM THIS BY SUPPLYING THE DEFINITION FILE BUT WITH AN ADDITIONAL EVENT DEFINED**

### Adding Tenants

Once applications have been registered, tenants can then be added using the /tenants endpoint. The tenant and application identifiers need to be supplied in the call to this endpoint. It is possible to associate a tenant with more than one application by passing multiple application identifiers as a comma-separated list.

![Overview](images/addTenant.png)

#### Verification Instructions

Every time a new tenant is added, a new row is inserted into the TenantApplications table under the AuditManagement database schema.

Two new tenant specific database schemas are then created for the tenant in the Vertica database which comprise a number of tables. See [Auditing Database Tables](https://github.hpe.com/caf/caf-audit-management-service-container/blob/develop/documentation/auditing-database-tables.md). If the client-side auditing library has sent audit events messages for this tenant through to the Kafka messaging service, this audit event data should start to arrive in the application specific audit events table under the tenant specific schema created as part of the add tenant web service call.

## Generating the client-side Auditing Library

**// TODO : Write about how to use the caf-audit-maven-plugin to generate the client-side audit library from the Audit Event Definition XML.**

The CAF Audit plugin, a custom maven plugin, uses an application's Audit Event Definition XML file to auto-generate a client-side Java class named `AuditLog`. This auto-generated class is comprised of methods for sending audit event messages to the Apache Kafka messaging service.

**TODO - QUESTION: IS THE PLUGIN USED IN THE AUDITED APPLICATION'S POM? OR IS THE PLUGIN USED IN A GENERATOR PROJECT'S POM WHOSE SOLE PURPOSE IS FOR GENERATION THE JAVA LIBRARY FOR THE PURPOSE OF THEN BEING USED IN THE AUDITED APPLICATION'S CODE? MY GUESS IS THE FORMER**

### Application POM 

The application will need to include the custom plugin in the `<plugins>` section of the application’s POM file. It needs to 
reference the XML audit event file as shown below:

	<build>
	    <plugins>
	        <plugin>
	            <groupId>com.hpe.caf</groupId>
	            <artifactId>caf-audit-maven-plugin</artifactId>
	            <version>1.0</version>
	            <executions>
	                <execution>
	                    <id>generate-code</id>
	                    <phase>generate-sources</phase>
	                    <goals>
	                        <goal>xmltojava</goal>
	                    </goals>
	                </execution>
	            </executions>
	            <configuration>
	                <auditXMLConfig>src/main/xml/sampleapp-auditevents.xml</auditXMLConfig>
	                <packageName>${project.groupId}.auditing</packageName>
	            </configuration>
	        </plugin>
	    </plugins>
	</build>


The `xmltojava` goal of the plugin is used to generate the Java auditing code that will make up the library. The `auditXMLConfig` setting can be used to define the path to the Audit Event Definition file, and the `packageName` setting can be used to set the package in which the auditing code should be generated.

In this example the Audit Event Definition file is in the `src/main/xml/` folder, though of course it could be read from any folder. The name of the package to use is being built up by appending `.auditing` to the project's group identifier (i.e. `com.hpe.sampleapp` in this example).

## Using the client-side Auditing Library

**// TODO: Write about how to call the generated client-side library that passes the event messages onto Kafka.**

Using the sample audit events XML specified in the [Audit Event Definition File](https://github.hpe.com/caf/caf-audit-schema/blob/develop/README.md), the code generation plugin will auto-generate the following methods as part of the `AuditLog` class:

	/**
	 * Audit the viewDocument event
	 * @param channel Identifies the channel to be used for message queuing 
	 * @param tenantId Identifies the tenant that the user belongs to 
	 * @param userId Identifies the user who triggered the event 
	 * @param correlationId Identifies the same user action 
	 * @param docId Document Identifier 
	 */
	public static void auditViewDocument
	(
	    final AuditChannel channel,
	    final String tenantId,
	    final String userId,
	    final String correlationId,
	    final long docId
	)
	    throws Exception
	{
	    final AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
	    auditEventBuilder.setApplication(APPLICATION_IDENTIFIER);
	    auditEventBuilder.setTenant(tenantId);
	    auditEventBuilder.setUser(userId);
	    auditEventBuilder.setCorrelationId(correlationId);
	    auditEventBuilder.setEventType("documentEvents", "viewDocument");
	    auditEventBuilder.addEventParameter("docId", null, docId);
	
	    auditEventBuilder.send();
	}
	
	/**
	 * Audit the deleteDocument event
	 * @param channel Identifies the channel to be used for message queuing 
	 * @param tenantId Identifies the tenant that the user belongs to 
	 * @param userId Identifies the user who triggered the event 
	 * @param correlationId Identifies the same user action 
	 * @param docId Document Identifier 
	 * @param authorisedBy User who authorised the deletion 
	 */
	public static void auditDeleteDocument
	(
	    final AuditChannel channel,
	    final String tenantId,
	    final String userId,
	    final String correlationId,
	    final long docId,
	    final String authorisedBy
	)
	    throws Exception
	{
	    final AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
	    auditEventBuilder.setApplication(APPLICATION_IDENTIFIER);
	    auditEventBuilder.setTenant(tenantId);
	    auditEventBuilder.setUser(userId);
	    auditEventBuilder.setCorrelationId(correlationId);
	    auditEventBuilder.setEventType("documentEvents", "deleteDocument");
	    auditEventBuilder.addEventParameter("docId", null, docId);
	    auditEventBuilder.addEventParameter("authorisedBy", null, authorisedBy, 1, 256);
	
	    auditEventBuilder.send();
	}

Calls to methods `auditViewDocument` and `auditDeleteDocument` would then be made to send document event messages to Kafka.

### Verification Instructions

// TODO: Write about how to verify that the client-side library's calls actually end up with event data in Vertica.





-----------------------

### Adding a Job

1. Expand the PUT /jobs/{jobId} method. 
2. Enter a value for jobId. 
3. Click on the example value box on the right to fill in the new job body. 
4. Edit these fields with your own details:
 
 `name`: name of the job <br>
  `description`: description of the job <br>
  `externalData`: external data <br>
  `taskClassifier`: classifier of the task <br>
  `taskApiVersion`: API version of the task <br>
  `taskData`: data of the task (include a batch definition if sending to the batch worker) <br>
  `taskDataEncoding`: encoding of the task data, for example, `utf8` <br>
  `taskPipe`: name of the RabbitMQ queue feeding messages to the first worker <br>
  `targetPipe`: name of the final worker's output queue where tracking will stop

5. Press `Try it out!`. The resulting code will show whether the addition of the job succeeds or not. 
   - 201, if the job is successfully added
   - 204, if the job is successfully updated

![Add Job](images/JobServiceUIAddJob.PNG)

### Getting Jobs

1. Expand the GET /jobs method. 
2. Press `Try it out!`. The list of jobs in the system appears in the response body, including the job you just created.

![Add Job](images/JobServiceUIGet.PNG)

## Deploying an End-To-End System

In order to test an end-to-end Job Service system, you need to deploy and run:

- the Job Service (see _Deploying the Job Service with Chateau_)
- a job tracking worker (see _Deploying the Job Service with Chateau_)
- a batch worker 
- another service to send the tasks to, in this case, the example worker.

### Batch Worker

You can deploy the batch worker with Chateau.

Prerequisites for running the batch worker can be found [here](https://github.hpe.com/caf/chateau/blob/develop/services/batch-worker/README.md).

The following command with the deployment script deploys a batch worker:

`./deploy-service.sh batch-worker`

### Example Worker

You can deploy the example worker using Chateau.

Prerequisites for running the example worker can be found [here](https://github.hpe.com/caf/chateau/blob/develop/services/example-worker/README.md).

The following command with the deployment script deploys an example worker:

`./deploy-service.sh example-worker`

You can view the status of the services on Marathon at the following URL:

`<marathon-endpoint>/ui/#`

The figure shows you the health of the workers and services:

![Marathon Health](images/MarathonAllHealthy.png)

You also need dummy data in a datastore and a storage reference to this data. Dummy data can be uploaded from the document-generator. For more information on using the document generator, see the README.md.

### Send a Job

Open the Swagger user interface as explained under _Using the Job Service Web User Interface_.

Add a job with the new job body following this template:

```
{
  "name": "Job_1",
  "description": "end-to-end",
  "externalData": "string",
  "task": {
    "taskClassifier": "BatchWorker",
    "taskApiVersion": 1,
    "taskData": "{\"batchDefinition\":\"[\\\"2f0e1a924d954ed09966f91d726e4960/fda3cf959a1d456b8d54800ba9e9b2f5\\\",\\\"02f0e1a924d954ed09966f91d726e4960/fda3cf959a1d456b8d54800ba9e9b2f5\\\"]\",\"batchType\":\"AssetIdBatchPlugin\",\"taskMessageType\":\"ExampleWorkerTaskBuilder\",\"taskMessageParams\":{\"datastorePartialReference\":\"2f0e1a924d954ed09966f91d726e4960\",\"action\":\"REVERSE\"},\"targetPipe\":\"demo-example-in\"}",
    "taskDataEncoding": "utf8",
    "taskPipe": "demo-batch-in",
    "targetPipe": "demo-example-out"
  }
}
```

Note the following:

* `TaskClassifier` must be set to `BatchWorker` as you are sending the job to the batch worker.
* Set the `taskApiVersion` to 1.
* For the `taskData`, we are adding a batch definition with a storage reference and the `datastorePartialReference` is the container ID. This storage reference is the reference to the dummy data stored using document generator.
* Set `taskPipe` to the queue consumed by the first worker to which you want to send the work, in this case, the batch worker `demo-batch-in`. The batch can then be broken down into task items.
* Set `targetPipe` to the name of the final worker where tracking will stop, in this case, `demo-example-out`.

### Verification of correct setup

The message output to the example worker output queue, demo-example-out, contains no tracking information. The payload for the messages sent to RabbitMQ will look similar to the following. Notice that `tracking` is `null`.

```
{"version":3,"taskId":"j_demo_1.1","taskClassifier":"ExampleWorker","taskApiVersion":1,"taskData":"eyJ3b3JrZXJTdGF0dXMiOiJDT01QTEVURUQiLCJ0ZXh0RGF0YSI6eyJyZWZlcmVuY2UiOm51bGwsImRhdGEiOiJBQUFBQUFEdnY3MEFBQUR2djcwQUF3QURBQUFBQUFZRlMxQjBlSFF1TTJOdlpIUnpaWFFBQUFCa0FBQUFJQUFCQUFBQUFBQUFBQXdBQUFBSUFBQUFDQlB2djczdnY3MGFTQ2hhVkFBQUFBQUFGQUFVQWdGTFVIUjRkQzR5WTI5a2RITmxkQUFBQURJQUFBQWdBQUVBQUFBQUFBQUFEQUFBQUFnQUFBQUlaTysvdmUrL3ZlKy92VWdvV2swQUFBQUFBQlFBRkFJQlMxQjBlSFF1TVdOdlpIUnpaWFFBQUFBQUFBQUFJQUFCQUFBQUFBQUFBQXdBQUFBSUFBQUFDTysvdmM2VE5rZ29Xa1VBQUFBQUFCUUFGQUlCUzFBelkyOWtkSE5sZEhSNGRDNHpZMjlrZEhObGRBQUFBQXdBQUFBSUFBQUFDQlB2djczdnY3MGFTQ2hhVkFBQUFBQUFGQVFEUzFBeVkyOWtkSE5sZEhSNGRDNHlZMjlrZEhObGRBQUFBQXdBQUFBSUFBQUFDR1R2djczdnY3M3Z2NzFJS0ZwTkFBQUFBQUFVQkFOTFVERmpiMlIwYzJWMGRIaDBMakZqYjJSMGMyVjBBQUFBREFBQUFBZ0FBQUFJNzcrOXpwTTJTQ2hhUlFBQUFBQUFGQVFEUzFBPSJ9fQ==","taskStatus":"RESULT_SUCCESS","context":{},"to":"demo-example-out","tracking":null,"sourceInfo":{"name":"ExampleWorker","version":"1.0-SNAPSHOT"}}
```

The figure shows how to locate the stdout output for the job tracking worker, after clicking on the job tracking application in Marathon.

![Jobtracking Stdout](images/Jobtracking_stdout.png)

Open the stdout log file for the job tracking worker and verify the following:

* Message is registered and split into separate tasks by the batch worker.
* Separate messages are forwarded to the example worker input queue.
* Job status check returns Active for separated messages.
* Single message forwarded to the batch worker output queue.
* Job status check returns Completed for separated messages.
* Separate messages forwarded to the example worker output queue.
* Tracking information is removed from separate messages.

The output log should look something like this:

```
DEBUG [2016-06-29 16:21:44,765] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Registering new message 22
DEBUG [2016-06-29 16:21:44,766] com.hpe.caf.worker.core.WorkerCore: Received task j_demo_1.1 (message id: 22)
DEBUG [2016-06-29 16:21:44,766] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.1 active status is not being checked - it is not yet time for the status check to be performed: status check due at Wed Jun 29 16:21:49 UTC 2016
DEBUG [2016-06-29 16:21:44,766] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.1 (message id: 22) is not intended for this worker: input queue demo-jobtracking-in does not match message destination queue demo-example-in
DEBUG [2016-06-29 16:21:44,766] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Connecting to database jdbc:postgresql://16.49.191.34:5432/jobservice ...
INFO  [2016-06-29 16:21:44,793] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Reporting progress of job task j_demo_1.1 with status Active ...
DEBUG [2016-06-29 16:21:44,999] com.hpe.caf.worker.jobtracking.JobTrackingWorkerFactory: Forwarding task j_demo_1.1
DEBUG [2016-06-29 16:21:44,999] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.1 (message id: 22) being forwarded to queue demo-example-in
DEBUG [2016-06-29 16:21:45,001] com.hpe.caf.worker.queue.rabbit.WorkerPublisherImpl: Publishing message with ack id 22
DEBUG [2016-06-29 16:21:45,001] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: Listening for confirmation of publish sequence 22 (ack message: 22)
DEBUG [2016-06-29 16:21:45,002] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Registering new message 23
DEBUG [2016-06-29 16:21:45,002] com.hpe.caf.worker.core.WorkerCore: Received task j_demo_1.2 (message id: 23)
DEBUG [2016-06-29 16:21:45,002] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.2 active status is not being checked - it is not yet time for the status check to be performed: status check due at Wed Jun 29 16:21:49 UTC 2016
DEBUG [2016-06-29 16:21:45,003] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.2 (message id: 23) is not intended for this worker: input queue demo-jobtracking-in does not match message destination queue demo-example-in
DEBUG [2016-06-29 16:21:45,003] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Connecting to database jdbc:postgresql://16.49.191.34:5432/jobservice ...
DEBUG [2016-06-29 16:21:45,006] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: RabbitMQ broker ACKed published sequence id 22 (multiple: false)
INFO  [2016-06-29 16:21:45,029] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Reporting progress of job task j_demo_1.2* with status Active ...
DEBUG [2016-06-29 16:21:45,069] com.hpe.caf.worker.jobtracking.JobTrackingWorkerFactory: Forwarding task j_demo_1.2
DEBUG [2016-06-29 16:21:45,069] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.2 (message id: 23) being forwarded to queue demo-example-in
DEBUG [2016-06-29 16:21:45,071] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Acknowledging message 22
DEBUG [2016-06-29 16:21:45,072] com.hpe.caf.worker.queue.rabbit.WorkerPublisherImpl: Publishing message with ack id 23
DEBUG [2016-06-29 16:21:45,072] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: Listening for confirmation of publish sequence 23 (ack message: 23)
DEBUG [2016-06-29 16:21:45,076] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Registering new message 24
DEBUG [2016-06-29 16:21:45,077] com.hpe.caf.worker.core.WorkerCore: Received task ec9c4556-4753-478b-a714-bd57fde837b5 (message id: 24)
DEBUG [2016-06-29 16:21:45,077] com.hpe.caf.worker.core.WorkerCore: Task ec9c4556-4753-478b-a714-bd57fde837b5 active status is not being checked - it is not yet time for the status check to be performed: status check due at Wed Jun 29 16:21:49 UTC 2016
DEBUG [2016-06-29 16:21:45,077] com.hpe.caf.worker.core.WorkerCore: Task ec9c4556-4753-478b-a714-bd57fde837b5 (message id: 24) is not intended for this worker: input queue demo-jobtracking-in does not match message destination queue demo-batch-out
DEBUG [2016-06-29 16:21:45,077] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Connecting to database jdbc:postgresql://16.49.191.34:5432/jobservice ...
DEBUG [2016-06-29 16:21:45,078] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: RabbitMQ broker ACKed published sequence id 23 (multiple: false)
INFO  [2016-06-29 16:21:45,108] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Reporting progress of job task j_demo_1 with status Active ...
DEBUG [2016-06-29 16:21:45,124] com.hpe.caf.worker.jobtracking.JobTrackingWorkerFactory: Forwarding task ec9c4556-4753-478b-a714-bd57fde837b5
DEBUG [2016-06-29 16:21:45,124] com.hpe.caf.worker.core.WorkerCore: Task ec9c4556-4753-478b-a714-bd57fde837b5 (message id: 24) being forwarded to queue demo-batch-out
DEBUG [2016-06-29 16:21:45,130] com.hpe.caf.worker.queue.rabbit.WorkerPublisherImpl: Publishing message with ack id 24
DEBUG [2016-06-29 16:21:45,130] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: Listening for confirmation of publish sequence 24 (ack message: 24)
DEBUG [2016-06-29 16:21:45,130] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Acknowledging message 23
DEBUG [2016-06-29 16:21:45,132] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: RabbitMQ broker ACKed published sequence id 24 (multiple: false)
DEBUG [2016-06-29 16:21:45,133] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Acknowledging message 24
DEBUG [2016-06-29 16:21:47,955] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Registering new message 25
DEBUG [2016-06-29 16:21:47,957] com.hpe.caf.worker.core.WorkerCore: Received task j_demo_1.1 (message id: 25)
DEBUG [2016-06-29 16:21:47,957] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.1 active status is not being checked - it is not yet time for the status check to be performed: status check due at Wed Jun 29 16:21:49 UTC 2016
DEBUG [2016-06-29 16:21:47,958] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.1 (message id: 25) is not intended for this worker: input queue demo-jobtracking-in does not match message destination queue demo-example-out
DEBUG [2016-06-29 16:21:47,959] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Connecting to database jdbc:postgresql://16.49.191.34:5432/jobservice ...
INFO  [2016-06-29 16:21:47,989] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Reporting progress of job task j_demo_1.1 with status Completed ...
DEBUG [2016-06-29 16:21:48,020] com.hpe.caf.worker.jobtracking.JobTrackingWorkerFactory: Forwarding task j_demo_1.1
DEBUG [2016-06-29 16:21:48,020] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.1 (message id: 25) being forwarded to queue demo-example-out
DEBUG [2016-06-29 16:21:48,020] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.1 (message id: 25): removing tracking info from this message as tracking ends on publishing to the queue demo-example-out.
DEBUG [2016-06-29 16:21:48,024] com.hpe.caf.worker.queue.rabbit.WorkerPublisherImpl: Publishing message with ack id 25
DEBUG [2016-06-29 16:21:48,024] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: Listening for confirmation of publish sequence 25 (ack message: 25)
DEBUG [2016-06-29 16:21:48,026] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: RabbitMQ broker ACKed published sequence id 25 (multiple: false)
DEBUG [2016-06-29 16:21:48,027] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Acknowledging message 25
DEBUG [2016-06-29 16:21:49,246] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Registering new message 26
DEBUG [2016-06-29 16:21:49,247] com.hpe.caf.worker.core.WorkerCore: Received task j_demo_1.2 (message id: 26)
DEBUG [2016-06-29 16:21:49,247] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.2 active status is not being checked - it is not yet time for the status check to be performed: status check due at Wed Jun 29 16:21:49 UTC 2016
DEBUG [2016-06-29 16:21:49,247] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.2 (message id: 26) is not intended for this worker: input queue demo-jobtracking-in does not match message destination queue demo-example-out
DEBUG [2016-06-29 16:21:49,247] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Connecting to database jdbc:postgresql://16.49.191.34:5432/jobservice ...
INFO  [2016-06-29 16:21:49,274] com.hpe.caf.worker.jobtracking.JobTrackingWorkerReporter: Reporting progress of job task j_demo_1.2* with status Completed ...
DEBUG [2016-06-29 16:21:49,298] com.hpe.caf.worker.jobtracking.JobTrackingWorkerFactory: Forwarding task j_demo_1.2
DEBUG [2016-06-29 16:21:49,298] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.2 (message id: 26) being forwarded to queue demo-example-out
DEBUG [2016-06-29 16:21:49,298] com.hpe.caf.worker.core.WorkerCore: Task j_demo_1.2 (message id: 26): removing tracking info from this message as tracking ends on publishing to the queue demo-example-out.
DEBUG [2016-06-29 16:21:49,301] com.hpe.caf.worker.queue.rabbit.WorkerPublisherImpl: Publishing message with ack id 26
DEBUG [2016-06-29 16:21:49,301] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: Listening for confirmation of publish sequence 26 (ack message: 26)
DEBUG [2016-06-29 16:21:49,306] com.hpe.caf.worker.queue.rabbit.WorkerConfirmListener: RabbitMQ broker ACKed published sequence id 26 (multiple: false)
DEBUG [2016-06-29 16:21:49,306] com.hpe.caf.worker.queue.rabbit.WorkerQueueConsumerImpl: Acknowledging message 26
```

## Links

For more information on Chateau, go [here](https://github.hpe.com/caf/chateau).

For more information on Job Service templates, and configuration and property files, see [here](https://github.hpe.com/caf/chateau/blob/develop/services/job-service/README.md).

For more information on batch worker templates, and configuration and property files, see [here](https://github.hpe.com/caf/chateau/blob/develop/services/batch-worker/README.md).



# NOTES:

- AUDIT EVENT DEFINITION FILE DUPLICATION BETWEEN THIS DOCUMENT AND THE ARTCHITECTURE; WHICH ONE SHOULD EXIST? MAYBE IT WOULD BE BETTER FOR THE SCHEMA DEFINITION TO EXIST IN THE ARCHITECTURE DOC WHEREAS THE EXAMPLE OF A DEFINITION FILE WOULD EXIST IN THIS GETTING STARTED GUIDE.