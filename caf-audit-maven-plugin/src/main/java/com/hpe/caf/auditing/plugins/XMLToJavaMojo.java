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
    private static final String TRANSFORM_GENSOURCES_DIRECTORY = "/generated-sources/src/main/java/";

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

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try
        {
            //  Create instance of class needed to perform XML to Java transform.
            XMLToJavaTransform transform = new XMLToJavaTransform(this.auditXMLConfig, this.packageName);

            //  Create generated sources directory for transform output and automatically add as
            //  an additional source directory for the build process.
            Path path = Paths.get(project.getBuild().getDirectory(),TRANSFORM_GENSOURCES_DIRECTORY,this.packageName.replace(".",File.separator));
            File outputDirectory = new File(path.toString());

            //  Perform XML to Java transform.
            transform.doTransform(outputDirectory, TRANSFORM_XSD_FILEPATH, TRANSFORM_TEMPLATE_NAME, TRANSFORM_OUTPUT_FILENAME);

            //  Automatically add output directory an additional source directory for the build process.
            if (outputDirectory.exists() && project != null) {
                project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
            }
        }
        catch( Exception e )
        {
            throw new MojoExecutionException("Exception : " + e);
        }
    }
}
