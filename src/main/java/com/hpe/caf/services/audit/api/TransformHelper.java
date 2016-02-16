package com.hpe.caf.services.audit.api;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * The TransformHelper class is responsible for generating sql statements for table creation and column addition.
 */
public class TransformHelper {

    private static final String ERR_MSG_PARSING_XML = "Error parsing document";
    private static final String ERR_MSG_AUDIT_EVENTS_TEMPLATE_NOT_FOUND = "The audit events transform template cannot be found.";

    private final VelocityContext context = new VelocityContext();

    private static final Logger LOG = LoggerFactory.getLogger(TransformHelper.class);

    public TransformHelper() {

        //  Initialise Velocity.
        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
    }

    /**
     * Parses the audit events XML and generates a sql statement for table creation.
     */
    public String doCreateTableTransform(InputStream auditXMLConfig, String templateName, String schema) throws Exception {

        //  Build a Document from the audit events XML file.
        SAXBuilder builder;
        Document root;

        try
        {
            LOG.debug("doCreateTableTransform: Building DOM document from audit events XML...");
            builder = new SAXBuilder();
            root = builder.build(auditXMLConfig);
        }
        catch (IOException | JDOMException e) {
            LOG.error("doCreateTableTransform: {}",ERR_MSG_PARSING_XML);
            throw new Exception(ERR_MSG_PARSING_XML + " : " + e);
        }

        //  Construct Velocity context object and populate it.
        LOG.debug("doCreateTableTransform: Constructing Velocity context...");
        this.context.put("root", root);
        this.context.put("schema", schema);
        this.context.put("transformContext", "CREATE");

        //  Generate 'CREATE TABLE' sql statement.
        return doTransform(templateName);
    }

    /**
     * Generates a sql statement for column addition.
     */
    public String doModifyTableTransform(String templateName, String schema, String tableName, String columnName, String columnType) throws Exception {

        //  Construct Velocity context object and populate it.
        LOG.debug("doModifyTableTransform: Constructing Velocity context...");
        this.context.put("transformContext", "MODIFY");
        this.context.put("schema", schema);
        this.context.put("tableName", tableName);
        this.context.put("columnName", columnName);
        this.context.put("columnType", columnType);

        //  Generate 'ALTER TABLE ADD COLUMN' sql statement.
        return doTransform(templateName);
    }

    /**
     *  Utilises Velocity template engine to generate CREATE TABLE or ALTER TABLE ADD COLUMN sql statement.
     */
    private String doTransform(String templateName) throws Exception {

        //  Get Velocity template instance.
        Template template;
        try
        {
            LOG.debug("doTransform: Getting Velocity template...");
            template = Velocity.getTemplate(templateName);
        }
        catch( ResourceNotFoundException rnfe )
        {
            //  Cannot find the transform template.
            LOG.error("doTransform: {}",ERR_MSG_AUDIT_EVENTS_TEMPLATE_NOT_FOUND);
            throw new Exception(ERR_MSG_AUDIT_EVENTS_TEMPLATE_NOT_FOUND);
        }

        StringWriter writer = new StringWriter();

        //  Perform the XML to SQL transformation.
        LOG.debug("doTransform: Performing XML to 'CREATE TABLE' SQL transformation...");
        template.merge(context, writer);

        return writer.toString();
    }
}
