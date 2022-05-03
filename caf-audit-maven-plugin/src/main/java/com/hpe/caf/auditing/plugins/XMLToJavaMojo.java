/*
 * Copyright 2015-2022 Micro Focus or one of its affiliates.
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

import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "xmltojava",  defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class XMLToJavaMojo extends AbstractMojo{

    private static final String TRANSFORM_XSD_FILEPATH = "schema/AuditedApplication.xsd";
    private static final String TRANSFORM_TEMPLATE_NAME = "AuditTransform.vm";
    private static final String TRANSFORM_OUTPUT_FILENAME = "AuditLog.java";
    private static final String TRANSFORM_GENSOURCES_DIRECTORY = "/generated-sources/src/";
    private static final String TRANSFORM_GENSOURCES_TEST_DIRECTORY = "/generated-test-sources/src/";

    @Parameter(defaultValue = "${project}", readonly = true )
    private MavenProject project;

    /**
     *  Specifies the application audit events XML configuration file.
     */
    @Parameter(property = "auditXMLConfig", defaultValue = "${project.basedir}/AuditConfig.xml")
    private File auditXMLConfig;

    /**
     *  Specifies the package name of the generated Java transform file.
     */
    @Parameter(property = "packageName", defaultValue = "${project.groupId}.auditing")
    private String packageName;

    /**
     *  Indicates if the generated Java should be added as a test resource. If not set, or set to false, it is added as
     *  a main resource.
     */
    @Parameter(property = "generateAsTestResource", defaultValue = "false")
    private boolean generateAsTestResource;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try
        {
            //  Create instance of class needed to perform XML to Java transform.
            XMLToJavaTransform transform = new XMLToJavaTransform(this.auditXMLConfig, this.packageName);

            // If generateAsTestResource has been set to true, set the output directory to be test resource directory.
            String transformGenSourcesDir;
            if (generateAsTestResource) {
                transformGenSourcesDir = TRANSFORM_GENSOURCES_TEST_DIRECTORY;
            } else {
                transformGenSourcesDir = TRANSFORM_GENSOURCES_DIRECTORY;
            }

            //  Create generated sources directory for transform output and automatically add as
            //  an additional source directory for the build process.
            Path path = Paths.get(project.getBuild().getDirectory(),transformGenSourcesDir,this.packageName.replace(".",File.separator));
            File outputDirectory = new File(path.toString());

            //  Perform XML to Java transform.
            transform.doTransform(outputDirectory, TRANSFORM_XSD_FILEPATH, TRANSFORM_TEMPLATE_NAME, TRANSFORM_OUTPUT_FILENAME);
            
            //get the path till target/src path so it can be added as the additional source directory
            Path buildPath = Paths.get(project.getBuild().getDirectory(),transformGenSourcesDir);
            //  Automatically add output directory an additional source directory for the build process.
            if (buildPath.toFile().exists() && project != null) {
                if (generateAsTestResource) {
                    project.addTestCompileSourceRoot(buildPath.toString());
                } else {
                    project.addCompileSourceRoot(buildPath.toString());
                }
            }
        }
        catch( Exception e )
        {
            throw new MojoExecutionException("Exception : " + e);
        }
    }
}
