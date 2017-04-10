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
package com.hpe.caf.auditing.kafka;

import com.hpe.caf.auditing.AuditChannel;
import com.hpe.caf.auditing.AuditEventBuilder;
import org.apache.kafka.clients.producer.Producer;
import com.hpe.caf.auditing.AuditCoreMetadataProvider;

final class KafkaAuditChannel implements AuditChannel
{
    private Producer<String, String> producer;

    public KafkaAuditChannel(final Producer<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public void declareApplication(String applicationId) {
        // Nothing to do - topics are auto-created
    }

    @Override
    public AuditEventBuilder createEventBuilder(final AuditCoreMetadataProvider coreMetadataProvider) {
        return new KafkaAuditEventBuilder(producer, coreMetadataProvider);
    }

    @Override
    public void close() {
        producer = null;
    }
}
