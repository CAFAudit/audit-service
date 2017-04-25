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

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;
import com.hpe.caf.auditing.AuditEventBuilder;
import com.hpe.caf.auditing.internal.AuditNewEventFactory;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;

public class ElasticAuditChannel implements AuditChannel {

    private final TransportClient transportClient;

    public ElasticAuditChannel(TransportClient transportClient){
        this.transportClient = transportClient;
    }

    @Override
    public void declareApplication(String applicationId) throws IOException {
        // Do nothing.
    }

    @Override
    public AuditEventBuilder createEventBuilder() {
        return new ElasticAuditEventBuilder(transportClient, AuditNewEventFactory.createNewEvent());
    }

    @Override
    public AuditEventBuilder createEventBuilder(AuditCoreMetadataProvider coreMetadataProvider) {
        return new ElasticAuditEventBuilder(transportClient, coreMetadataProvider);
    }

    @Override
    public void close() throws Exception {
        // Do nothing.
    }
}
