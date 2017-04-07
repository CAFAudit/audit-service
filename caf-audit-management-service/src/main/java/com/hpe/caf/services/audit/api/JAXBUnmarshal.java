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
