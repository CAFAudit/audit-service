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
package com.hpe.caf.auditing.elastic;

import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.internal.AuditNewEventFactory;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.rest.RestStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.doThrow;

public class ElasticAuditEventBuilderTest {

    private ElasticAuditEventBuilder eventBuilder;
    private TransportClient mockTransportClient;

    @Before
    public void setup() {
        mockTransportClient = Mockito.mock(TransportClient.class);
        AuditCoreMetadataProvider acmp = AuditNewEventFactory.createNewEvent();

        eventBuilder = new ElasticAuditEventBuilder(mockTransportClient, acmp);
        eventBuilder.setApplication("TestApplication");
        eventBuilder.setUser("TestUser");
        eventBuilder.setEventType("TestCategory","TestType");
        eventBuilder.addEventParameter("param1","param1","test");
        eventBuilder.addEventParameter("param2","param2",Short.MAX_VALUE);
        eventBuilder.addEventParameter("param3","param3",Integer.MAX_VALUE);
        eventBuilder.addEventParameter("param4","param4",Long.MAX_VALUE);
        eventBuilder.addEventParameter("param5","param5",Float.MAX_VALUE);
        eventBuilder.addEventParameter("param6","param6", Double.MAX_VALUE);
        eventBuilder.addEventParameter("param7","param7", true);
    }

    @Test
    public void testSend() throws Exception {
        //  Mock classes for unit test.
        RestStatus restStatus = RestStatus.CREATED;
        IndexResponse mockIndexResponse = Mockito.mock(IndexResponse.class);
        IndexRequestBuilder mockIndexRequestBuilder = Mockito.mock(IndexRequestBuilder.class);
        Mockito.when(mockIndexResponse.status()).thenReturn(restStatus);
        Mockito.when(mockTransportClient.prepareIndex(Mockito.anyString(), Mockito.anyString())).thenReturn(mockIndexRequestBuilder);
        Mockito.when(mockIndexRequestBuilder.setSource(Mockito.anyMapOf(String.class, Object.class))).thenReturn(mockIndexRequestBuilder);
        Mockito.when(mockIndexRequestBuilder.get()).thenReturn(mockIndexResponse);

        //  Send audit event details to ES.
        eventBuilder.send();

        //  Verify calls made in unit test.
        Mockito.verify(mockTransportClient, Mockito.times(1)).prepareIndex(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(mockIndexRequestBuilder, Mockito.times(1)).setSource(Mockito.anyMapOf(String.class, Object.class));
        Mockito.verify(mockIndexRequestBuilder, Mockito.times(1)).get();
    }

    @Test(expected = Exception.class)
    public void testSend_NoNodeAvailableException() throws Exception {
        //  Throw NoNodeAvailableException() to simulate node disconnect for example.
        doThrow(new NoNodeAvailableException("Failed!")).when(mockTransportClient).prepareIndex(Mockito.anyString(), Mockito.anyString());

        //  Send audit event details to ES.
        eventBuilder.send();

        //  Verify calls made in unit test.
        Mockito.verify(mockTransportClient, Mockito.times(1)).prepareIndex(Mockito.anyString(), Mockito.anyString());
    }

    @Test(expected = Exception.class)
    public void testSend_Exception() throws Exception {
        //  Throw Exception().
        doThrow(new Exception("Failed!")).when(mockTransportClient).prepareIndex(Mockito.anyString(), Mockito.anyString());

        //  Send audit event details to ES.
        eventBuilder.send();

        //  Verify calls made in unit test.
        Mockito.verify(mockTransportClient, Mockito.times(1)).prepareIndex(Mockito.anyString(), Mockito.anyString());
    }

}
