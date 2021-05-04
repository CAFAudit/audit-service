/*
 * Copyright 2015-2021 Micro Focus or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.caf.auditing.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;

public class XMLToJavaTransform {

    private final File auditXMLConfig;
    private final String packageName;

    public XMLToJavaTransform(File auditXMLConfig, String packageName){

        this.auditXMLConfig = auditXMLConfig;
        this.packageName = packageName;
    }

    public void doTransform(File outputDirectory,
                            String xsdName,
                            String templateName,
                            String outputFilename
    ) throws Exception {
        boolean bool = false;

        //  Check that the audit events XML config file exists and conforms to the XML schema.
        //  The XSD is provided via caf-audit-schema as a remote resource.
        if (this.auditXMLConfig == null || !this.auditXMLConfig.exists())
        {
            throw new MojoExecutionException("The audit events XML configuration file cannot be found.");
        }

        try(
                InputStream xmlInputStream = new FileInputStream(this.auditXMLConfig);
                InputStream xsdInputStream = this.getClass().getClassLoader().getResourceAsStream(xsdName)
        )
        {
            //  Check that the audit events XML config file conforms to the XML schema.
            //  The XSD is provided via caf-audit-schema as a remote resource.
            bool = validateXMLAgainstXSD(xmlInputStream, xsdInputStream);
            if (bool != true)
            {
                throw new MojoExecutionException("The audit events XML configuration file does not conform to the schema.");
            }

            //  Generate output directory if needed. This folder is needed to store the transform .java file.
            //  File outputDirectory = new File(outputDirectory);
            if (!outputDirectory.exists())
            {
                bool = outputDirectory.mkdirs();
                if (bool != true)
                {
                    throw new Exception("Generated java sources directory cannot be created.");
                }
            }

            //  Initialise Velocity which will be used to perform the XML to JAVA transformation.
            Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            Velocity.init();

            //  Build a Document from the audit events XML configuration file.
            SAXBuilder builder;
            Document root = null;

            try
            {
                builder = new SAXBuilder();
                root = builder.build(this.auditXMLConfig.getAbsolutePath());
            }
            catch (IOException ioe) {
                throw new MojoExecutionException("Error parsing document : " + ioe);
            }
            catch (JDOMException jdome) {
                throw new MojoExecutionException("Error parsing document : " + jdome);
            }

            //  Construct Velocity context object and populate it.
            VelocityContext context = new VelocityContext();
            context.put("root", root);
            context.put("packageName", packageName);

            //  Get Velocity template instance.
            Template template = null;
            try
            {
                template = Velocity.getTemplate(templateName);
            }
            catch( ResourceNotFoundException rnfe )
            {
                //  Cannot find the transform template.
                throw new MojoExecutionException("The audit events transform template cannot be found.");
            }

            //  Create a new .java file for transform output.
            String transformOutputFile = outputDirectory + File.separator + outputFilename;
            File f = new File(transformOutputFile);
            if (f.exists())
            {
                //  Delete if file already exists.
                f.delete();
            }

            bool = f.createNewFile();
            if(bool == true)
            {
                String outFileName = f.getAbsolutePath();

                try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFileName)))) {

                    //  Perform the XML to Java transformation.
                    template.merge(context, writer);
                }
            }
        }
        catch( Exception e )
        {
            throw new MojoExecutionException("Exception : " + e);
        }
    }

    private static boolean validateXMLAgainstXSD(InputStream xml, InputStream xsd)
    {
        try
        {
            //  Try and validate the incoming XML against the specified XSD.
            SchemaFactory factory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsd));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
            return true;
        }
        catch(Exception ex)
        {
            return false;
        }
    }
}
