/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

public class XMLToJavaTransformTest {

    private static final String PACKAGE_NAME = "com.hpe.caf.auditing.plugins.unittest";
    private static final String OUTPUT_PATH = "generated-sources\\src\\main\\java\\com\\hpe\\caf\\auditing\\plugins\\unittest";
    private static final String OUTPUT_FILENAME = "AuditLog.java";
    private static final String XSD_FILEPATH = "schema/AuditedApplication.xsd";
    private static final String VELOCITY_TEMPLATE_NAME = "AuditTransform.vm";
    private static final String INVALID_VELOCITY_TEMPLATE_NAME = "DoesNotExist.vm";

    private static final String VALID_XML_AUDIT_FILENAME = "xml/AuditConfig.xml";
    private static final String INVALID_XML_AUDIT_FILENAME = "xml/InvalidAuditConfig.xml";

    private File auditXMLConfig;
    private File outputDirectory;

    public XMLToJavaTransformTest() throws URISyntaxException {
        File file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        String targetPath = file.getParent();
        outputDirectory = new File(targetPath, OUTPUT_PATH);
    }

    @After
    public void tearDown() throws Exception {

        //  Delete generated .Java file after every test if it exists.
        File file = new File(outputDirectory,OUTPUT_FILENAME);
        if (file.exists())
        {
            file.delete();
        }
    }

    @Test
    public void testTransformSuccess() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        this.auditXMLConfig = new File(classLoader.getResource(VALID_XML_AUDIT_FILENAME).getFile());
        Assert.assertNotNull(this.auditXMLConfig);
        Assert.assertTrue(this.auditXMLConfig.exists());

        XMLToJavaTransform transform = new XMLToJavaTransform(this.auditXMLConfig, PACKAGE_NAME);
        Assert.assertNotNull(transform);

        transform.doTransform(outputDirectory, XSD_FILEPATH, VELOCITY_TEMPLATE_NAME, OUTPUT_FILENAME);

        //  Verify .Java file was generated.
        File generatedJavaFile = new File( outputDirectory, OUTPUT_FILENAME );
        Assert.assertTrue(generatedJavaFile.exists());

        //  Verify generated output matches expected output.
        File expected = new File(classLoader.getResource("transform/AuditLog.java").getFile());
        Assert.assertEquals(FileUtils.readLines(expected), FileUtils.readLines(generatedJavaFile));
    }

    @Test
    public void testTransformFailure_XMLNotFound() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        this.auditXMLConfig = null;

        XMLToJavaTransform transform = new XMLToJavaTransform(this.auditXMLConfig, PACKAGE_NAME);
        Assert.assertNotNull(transform);

        try
        {
            transform.doTransform(outputDirectory, XSD_FILEPATH, VELOCITY_TEMPLATE_NAME, OUTPUT_FILENAME);
            Assert.fail("Should throw an exception" );
        }
        catch( MojoExecutionException ee )
        {
            final String msg = "The audit events XML configuration file cannot be found.";
            Assert.assertEquals(msg, ee.getMessage());
        }

        //  Verify .Java file was not generated.
        File generatedJavaFile = new File( outputDirectory, OUTPUT_FILENAME);
        Assert.assertFalse(generatedJavaFile.exists());

    }

    @Test
    public void testTransformFailure_XMLNotValid() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        this.auditXMLConfig = new File(classLoader.getResource(INVALID_XML_AUDIT_FILENAME).getFile());
        Assert.assertNotNull(this.auditXMLConfig);
        Assert.assertTrue(this.auditXMLConfig.exists());

        XMLToJavaTransform transform = new XMLToJavaTransform(this.auditXMLConfig, PACKAGE_NAME);
        Assert.assertNotNull(transform);

        try
        {
            transform.doTransform(outputDirectory, XSD_FILEPATH, VELOCITY_TEMPLATE_NAME, OUTPUT_FILENAME);
            Assert.fail("Should throw an exception" );
        }
        catch( MojoExecutionException ee )
        {
            final String msg = "Exception : org.apache.maven.plugin.MojoExecutionException: The audit events XML configuration file does not conform to the schema.";
            Assert.assertEquals(msg, ee.getMessage());
        }

        //  Verify .Java file was not generated.
        File generatedJavaFile = new File( outputDirectory, OUTPUT_FILENAME);
        Assert.assertFalse(generatedJavaFile.exists());

    }

    @Test
    public void testTransformFailure_XSDNotFound() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        this.auditXMLConfig = new File(classLoader.getResource(VALID_XML_AUDIT_FILENAME).getFile());
        Assert.assertNotNull(this.auditXMLConfig);
        Assert.assertTrue(this.auditXMLConfig.exists());

        XMLToJavaTransform transform = new XMLToJavaTransform(this.auditXMLConfig, PACKAGE_NAME);
        Assert.assertNotNull(transform);

        try
        {
            transform.doTransform(outputDirectory, XSD_FILEPATH, INVALID_VELOCITY_TEMPLATE_NAME, OUTPUT_FILENAME);
            Assert.fail("Should throw an exception" );
        }
        catch( MojoExecutionException ee )
        {
            final String msg = "Exception : org.apache.maven.plugin.MojoExecutionException: The audit events transform template cannot be found.";
            Assert.assertEquals(msg, ee.getMessage());
        }

        //  Verify .Java file was not generated.
        File generatedJavaFile = new File( outputDirectory, OUTPUT_FILENAME);
        Assert.assertFalse(generatedJavaFile.exists());

    }

}
