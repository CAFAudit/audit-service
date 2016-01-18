package com.hpe.testapplication.auditing;

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
