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

import java.util.List;

/**
 * Class to represent message test data.
 */
public class AuditEventMessage {

    public AuditEventMessage() {
    }

    public AuditEventMessage(String method) {
        this.method = method;
    }

    private String method;
    private List<AuditEventMessageParam> params;

    public String getAuditLogMethod() {
        return method;
    }

    public void setAuditLogMethod(String method) {
        this.method = method;
    }

    public List<AuditEventMessageParam> getAuditLogMethodParams() {
        return params;
    }

    public void setAuditLogMethodParams(List<AuditEventMessageParam> params) {
        this.params = params;
    }
}
