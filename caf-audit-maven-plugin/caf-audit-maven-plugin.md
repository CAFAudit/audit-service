# CAF Audit Plugin

Applications will define audit events that will occur in the system. The definition of these audit events will be
recorded in an XML audit event file. The CAF Audit plugin, a custom maven plugin, uses this XML file to auto-generate a 
client side Java class named `AuditLog`. This auto-generated class comprises methods for sending audit event messages to the Elasticsearch.  

## XML Audit Event File
The XML audit event file used by the custom plugin defines the audit events that the application uses. A sample XML is 
provided next.

```xml
<AuditedApplication xmlns="https://cafaudit.github.io/audit-service/schema/AuditedApplication.xsd">
  <ApplicationId>ProductX</ApplicationId>
  <AuditEvents>
    <AuditEvent>
      <TypeId>viewDocument</TypeId>
      <CategoryId>documentEvents</CategoryId>
      <Params>
        <Param>
          <Name>docId</Name>
          <Type>long</Type>
          <Description>Document identifier</Description >
        </Param>
      </Params>
    </AuditEvent>
    <AuditEvent>
      <TypeId>policyApplied</TypeId>
      <CategoryId>policyEvents</CategoryId>
      <Params>
        <Param>
          <Name>policyId</Name>
          <Type>long</Type>
          <Description>Policy identifier</Description >
        </Param>
        <Param>
          <Name>policyName</Name>
          <Type>string</Type>
          <IndexingHint>keyword</IndexingHint>
          <Description>Policy Name</Description >
        </Param>
        <Param>
          <Name>policyDef</Name>
          <Type>string</Type>
          <IndexingHint>fulltext</IndexingHint>
          <ColumnName>policyDefinition</ColumnName>
          <Description>Policy definition</Description >
        </Param>
      </Params>
    </AuditEvent>
  </AuditEvents>
</AuditedApplication>
```

`AuditedApplication` is the root element. `ApplicationId` identifies the name of the application that the audit event type is associated with. For each audit event defined, `TypeId` is a string identifier for the particular event (e.g. viewDocument) and `CategoryId` is a string identifier for the category of event. A list of parameter elements are then defined for each Audit Event. This includes the `Name` of the parameter, the `Type` (i.e. string, short, int, long, float, double, boolean or date) and the `Description`. The `IndexingHint` (i.e. fulltext or keyword) is optional and can be used to specify an indexing hint when storing audit event parameter data of `Type` string. The `Constraints` element is also optional and this can be used to specify minimum and/or maximum length constraints for audit event parameters of `Type` string. The `ColumnName` element too is optional which can be used to force the use of a particular database column when storing the audit data. 

## Application POM 

The application will need to include the custom plugin in the `<plugins>` section of the application’s POM file. It needs to 
reference the XML audit event file as shown below:

```xml
<plugin>
	<groupId>com.github.cafaudit</groupId>
	<artifactId>caf-audit-maven-plugin</artifactId>
	<version>4.0.0</version>
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
		<auditXMLConfig>.\sample-test-scripts\xml\AuditEventsConfig.xml</auditXMLConfig>
		<packageName>com.hpe.productx.auditing</packageName>
	</configuration>
</plugin >
```

The XML audit event file path is configurable through the `<auditXMLConfig>` element. The package name for the auto-generated 
`AuditLog` class is configurable through the `<packageName>` element.

## Auto-Generated Methods

Using the sample XML above, the CAF Audit plugin will auto-generate the following methods as part of the AuditLog class:

```
/**
     * Audit the viewDocument event
     * @param channel Identifies the channel to be used for message queuing 
     * @param userId Identifies the user who triggered the event 
     * @param docId Document identifier 
     */
    public static void auditViewDocument
    (
        final AuditChannel channel,
        final String userId,
        final long docId
    )
        throws Exception
    {
        final AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        auditEventBuilder.setApplication(APPLICATION_IDENTIFIER);
        auditEventBuilder.setUser(userId);
        auditEventBuilder.setEventType("documentEvents", "viewDocument");
        auditEventBuilder.addEventParameter("docId", null, docId);

        auditEventBuilder.send();
    }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
    /**
     * Audit the policyApplied event
     * @param channel Identifies the channel to be used for message queuing 
     * @param userId Identifies the user who triggered the event 
     * @param policyId Policy identifier 
     * @param policyName Policy Name 
     * @param policyDef Policy definition 
     */
    public static void auditPolicyApplied
    (
        final AuditChannel channel,
        final String userId,
        final long policyId,
        final String policyName,
        final String policyDef
    )
        throws Exception
    {
        final AuditEventBuilder auditEventBuilder = channel.createEventBuilder();
        auditEventBuilder.setApplication(APPLICATION_IDENTIFIER);
        auditEventBuilder.setUser(userId);
        auditEventBuilder.setEventType("policyEvents", "policyApplied");
        auditEventBuilder.addEventParameter("policyId", null, policyId);
        auditEventBuilder.addEventParameter("policyName", null, policyName, AuditIndexingHint.KEYWORD);
        auditEventBuilder.addEventParameter("policyDef", "policyDefinition", policyDef, AuditIndexingHint.FULLTEXT);

        auditEventBuilder.send();
    }
```

Calls to methods `auditViewDocument` and `auditPolicyApplied` would then be made to send document and policy event messages respectively to Elasticsearch.


