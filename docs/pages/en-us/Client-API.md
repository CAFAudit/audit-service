---
layout: default
title: Client-side API
---

# Audit Client

The `caf-audit` library offers a convenient set of classes for creating a connection and sending audit events to persistent storage.

## Using Auditing Objects

This section covers `caf-audit` library classes and how to use them as objects within your application.

The order of instantiation and use of these objects for sending audit events is as follows:

1. If you do not have an existing [`ConfigurationSource`](#ConfigurationSource) object, create one for retrieving and holding configuration details.
2. Create an [`AuditConnection`](#AuditConnection) object by passing the `AuditConnectionFactory` the [`ConfigurationSource`](#ConfigurationSource).
3. Use the `AuditConnectionFactory` to create the [`AuditConnection`](#AuditConnection) object. You will need to pass it the [`ConfigurationSource`](#ConfigurationSource).
3. Create an [`AuditChannel`](#AuditChannel) object from the [`AuditConnection`](#AuditConnection) object.
4. Use the [`AuditEventBuilder`](#AuditEventBuilder) object to construct audit events and send them to Elasticsearch.

### ConfigurationSource

[comment]: <> (The caf-audit Getting-Started.md documentation content contains duplication of the ConfigurationSource section. It is important that any changes here must also be included within the Getting-Started.md content.)

You may already have a CAF configuration source in your application. It is a general framework that abstracts the source of the configuration, allowing it to come from any of the following:

- environment variables
- files
- a REST service
- a custom source that better integrates with the host application.

A `ConfigurationSource` object is required for the [`AuditConnectionFactory`](#AuditConnectionFactory) object to produce an [`AuditConnection`](#AuditConnection) object.

If you're not already using CAF's configuration mechanism, this sample code illustrates the generation of a `ConfigurationSource` object.

	import com.hpe.caf.api.*;
	import com.hpe.caf.cipher.NullCipherProvider;
	import com.hpe.caf.config.system.SystemBootstrapConfiguration;
	import com.hpe.caf.naming.ServicePath;
	import com.hpe.caf.util.ModuleLoader;
	
	public static ConfigurationSource createCafConfigSource() throws Exception
	{
	    System.setProperty("CAF_CONFIG_PATH", "/etc/sampleapp/config");
	    System.setProperty("CAF_APPNAME", "sampleappgroup/sampleapp");
	
	    BootstrapConfiguration bootstrap = new SystemBootstrapConfiguration();
	    Cipher cipher = ModuleLoader.getService(CipherProvider.class, NullCipherProvider.class).getCipher(bootstrap);
	    ServicePath path = bootstrap.getServicePath();
	    Codec codec = ModuleLoader.getService(Codec.class);
	    return ModuleLoader.getService(ConfigurationSourceProvider.class).getConfigurationSource(bootstrap, cipher, path, codec);
	}

To compile the above sample code, add the following dependencies to your POM:

	<dependency>
	    <groupId>com.github.cafapi</groupId>
	    <artifactId>caf-api</artifactId>
	    <version>1.6.0-176</version>
	</dependency>
	<dependency>
	    <groupId>com.github.cafapi.cipher</groupId>
	    <artifactId>cipher-null</artifactId>
	    <version>1.6.0-176</version>
	</dependency>
	<dependency>
	    <groupId>com.github.cafapi.config</groupId>
	    <artifactId>config-system</artifactId>
	    <version>1.6.0-176</version>
	</dependency>
	<dependency>
	    <groupId>com.github.cafapi.util</groupId>
	    <artifactId>util-moduleloader</artifactId>
	    <version>1.6.0-176</version>
	</dependency>
	<dependency>
	    <groupId>com.github.cafapi.util</groupId>
	    <artifactId>util-naming</artifactId>
	    <version>1.6.0-176</version>
	</dependency>

To use JSON-encoded files for your configuration, add the following additional dependencies to your POM:

	<!-- Runtime-only Dependencies -->
	<dependency>
	    <groupId>com.github.cafapi.config</groupId>
	    <artifactId>config-file</artifactId>
	    <version>1.6.0-176</version>
	    <scope>runtime</scope>
	</dependency>
	<dependency>
	    <groupId>com.github.cafapi.code</groupId>
	    <artifactId>codec-json</artifactId>
	    <version>1.6.0-176</version>
	    <scope>runtime</scope>
	</dependency>
	<dependency>
	    <groupId>io.dropwizard</groupId>
	    <artifactId>dropwizard-core</artifactId>
	    <version>0.8.4</version>
	    <scope>runtime</scope>
	</dependency>

#### Example

In the [`ConfigurationSource`](#ConfigurationSource) above, we used JSON-encoded files with the following parameters:

- `CAF_CONFIG_PATH: /etc/sampleapp/config`
- `CAF_APPNAME: sampleappgroup/sampleapp`

Given this configuration, you would configure Auditing by creating a file named `cfg_sampleappgroup_sampleapp_ElasticAuditConfiguration` in the `/etc/sampleapp/config/` directory. The contents of this file should be similar to the following:

	{
	    "hostAndPortValues": "<Elasticsearch_Cluser_Node_1>:<Port_Number>,<Elasticsearch_Cluser_Node_2>:<Port_Number>",
	    "clusterName": "elasticsearchcluster",
	    "numberOfShards": "5",
	    "numberOfReplicas": "1"
	}

where:

- `hostAndPortValues` refers to one or more of the nodes of the Elasticsearch cluster as a comma-separated list.
- `clusterName` name of the Elasticsearch cluster. Defaults to "elasticsearch".
- `numberOfShards` the number of primary shards that an index should have. Defaults to 5.
- `numberOfReplicas` the number of replica shards (copies) that each primary shard should have. Defaults to 1.

### AuditConnection

The `AuditConnection` object represents a logical connection to the datastore (that is, Elasticsearch in this implementation). It is a thread-safe object. ***You should expect this object to take some time to construct. The application should hold on to it and re-use it, rather than constantly re-construct it.***

The `AuditConnection` object can be constructed using the static `createConnection()` method in the `AuditConnectionFactory` class. This method takes a [`ConfigurationSource`](#ConfigurationSource) parameter, which is the standard method of configuration in CAF:

	AuditConnection auditConnection = null;
    try {
        // Setup Elasticsearch Connection
        auditConnection = new AuditConnectionFactory().createConnection(createCafConfigSource());
    } catch (Exception e) {
        System.out.println("Unable to create Audit Connection");
        e.printStackTrace();
    }

### AuditChannel

An `AuditChannel` object is constructed from the [`AuditConnection`](#AuditConnection) object.

This object represents a logical channel to the datastore (that is, Elasticsearch in this implementation). ***It is NOT a thread-safe object and must not be shared across threads without synchronization.*** However, you will have no issue constructing multiple `AuditChannel` objects simultaneously on different threads. The objects are lightweight and caching them is not that important.

The `AuditChannel` object can be constructed using the `createChannel()` method on the [`AuditConnection`](#AuditConnection) object. It does not take any parameters:

	AuditChannel auditChannel = null;
	try {
	    // Setup a connection channel to Elasticsearch
	    auditChannel = auditConnection.createChannel();
	} catch (IOException e) {
	    System.out.println("Unable to create Audit Channel from Audit Connection");
	    e.printStackTrace();
	}

### AuditEventBuilder

An `AuditEventBuilder` object is constructed from the [`AuditChannel`](#AuditChannel) object.

It constructs and sends an audit event. You should have one [`AuditEventBuilder`](#AuditEventBuilder) object for each audit event.

The `AuditEventBuilder` object is created using the `createEventBuilder()` method on the [`AuditChannel`](#AuditChannel) object. It does not take any parameters.
	
	/*
	  The following code constructs an application event for a tenant who has deleted a document. It then sends the event to storage.
	*/
	// Create an auditEventBuilder object from the AuditChannel
	final AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
	// Set the Application ID of the event
	auditEventBuilder.setApplication(APPLICATION_IDENTIFIER);
	// Set the Tenant ID of the event
	auditEventBuilder.setTenant(tenantId);
	// Set the Tenant's User ID
	auditEventBuilder.setUser(userId);
	// Set the Correlation ID
	auditEventBuilder.setCorrelationId(correlationId);
	// Set the Event Category and Type
	auditEventBuilder.setEventType("documentEvents", "deleteDocument");
	// Add Event Parameters
		// Add an Event Parameter for holding the Document ID that was deleted
	auditEventBuilder.addEventParameter("docId", null, docId);
		// Add an Event Parameter for holding the User who authorised the deletion of the document. Add length constraints for this parameter as it is of type String
	auditEventBuilder.addEventParameter("authorisedBy", null, authorisedBy, 1, 256);
	
	// Send the constructed event to storage
	auditEventBuilder.send();

Typically, this object is only used indirectly. Normally, you generate a type-safe client-side auditing library using the code generation plugin. Internally, the auto-generated code makes use of the `AuditEventBuilder` object. For information on how to generate a type-safe client-side auditing library for your application, visit the Getting Started guide [here](Getting-Started).
