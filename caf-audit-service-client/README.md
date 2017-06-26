# Audit Web Service Client

This project is a Java library to communicate with the [CAF Audit Web Service](https://github.com/CAFAudit/audit-service/tree/develop/caf-audit-service). It allows callers to index audit event messages into Elasticsearch.

## Usage

This project builds a Java library that can be used to make calls to the Audit Web Service. The library should take a dependency on `caf-audit-service-client` using the following Maven coordinates:

	<dependency>
		<groupId>com.github.cafaudit</groupId>
		<artifactId>caf-audit-service-client</artifactId>
		<version>3.0.0</version>
	</dependency>