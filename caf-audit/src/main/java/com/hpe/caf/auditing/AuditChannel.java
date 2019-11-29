/*
 * Copyright 2015-2020 Micro Focus or one of its affiliates.
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
package com.hpe.caf.auditing;

import static com.hpe.caf.auditing.internal.AuditNewEventFactory.createNewEvent;
import java.io.IOException;

public interface AuditChannel extends AutoCloseable
{
    /**
     * Prepares the auditing infrastructure to receive events for the specified application.
     *
     * @param applicationId the identifier of the application
     * @throws java.io.IOException if an error is encountered
     */
    void declareApplication(String applicationId) throws IOException;

    /**
     * Creates an object which can be used for preparing and sending an audit event.
     *
     * @return an audit event builder
     */
    default AuditEventBuilder createEventBuilder()
    {
        return createEventBuilder(createNewEvent());
    }

    /**
     * Creates an object which can be used for preparing and sending an audit event.
     *
     * @param coreMetadataProvider provides values for the core system-provided metadata
     * @return an audit event builder
     */
    AuditEventBuilder createEventBuilder(AuditCoreMetadataProvider coreMetadataProvider);
}
