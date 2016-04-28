# Audit Event Definition Schema

This contains the exact XML Schema file that the Audit Event Definition File must adhere to.

## Using the Schema File

If you reference the XML Schema file from your Audit Event Definition File then you should be able to use the Validate functionality that is built into most IDEs and XML Editors. This will allow you to easily check for syntax errors in your Audit Event Definition File. To do this add the standard `xsi:schemaLocation` attribute to the root `AuditedApplication` element.

Change the `AuditedApplication` element from:

	<AuditedApplication xmlns="http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd">

to:

	<AuditedApplication xmlns="http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd"
	                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	                    xsi:schemaLocation="http://www.hpe.com/CAF/Auditing/Schema/AuditedApplication.xsd http://rh7-artifactory.hpswlabs.hp.com:8081/artifactory/policyengine-release/com/hpe/caf/caf-audit-schema/1.0/caf-audit-schema-1.0.jar!/schema/AuditedApplication.xsd">

Many IDEs and XML Editors will also use the schema file to provide IntelliSense and type-ahead when the definition file is being authored.