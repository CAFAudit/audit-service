# Code Generation Plugin

`caf-audit-maven-plugin` is the code generation plugin used to generate auditing code to be used by the [auditing library](https://github.hpe.com/caf/caf-audit).

## Usage

The plugin should be referenced from the plugins section of a client-side auditing library POM. 

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

The `xmltojava` goal of the plugin is used to generate the Java auditing code that will make up the library. 
The `auditXMLConfig` setting can be used to define the path to the Audit Event Definition file.
The `packageName` setting can be used to set the package in which the auditing code should be generated.
	
