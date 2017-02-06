# Audit Management Web Service Client

This project is a Java library to communicate with the [CAF Audit Management Web Service](https://github.hpe.com/caf/audit-service/tree/develop/caf-audit-management-service). It allows callers to register application specific audit event definitions and register new tenants for applications.

## Usage

This project builds a Java library that can be used to make calls to the Audit Management Web Service. The library should take a dependency on `caf-audit-management-client` using the following Maven coordinates:

	<dependency>
		<groupId>com.hpe.caf</groupId>
		<artifactId>caf-audit-management-client</artifactId>
		<version>1.0</version>
	</dependency>