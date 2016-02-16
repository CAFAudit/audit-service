package com.hpe.caf.services.audit.api;

import com.hpe.caf.auditing.schema.AuditedApplication;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * JAXBUnmarshal is responsible for converting the application defined audit event XML to a Java object.
 */
public class JAXBUnmarshal {

    /**
     * Reads the specified xml file into the AuditedApplication object hierarchy.
     */
    public static AuditedApplication bindAuditEventsXml(InputStream xmlFile) throws JAXBException {

        final JAXBContext jaxbContext = JAXBContext.newInstance(AuditedApplication.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        return (AuditedApplication)unmarshaller.unmarshal(xmlFile);
    }
}
