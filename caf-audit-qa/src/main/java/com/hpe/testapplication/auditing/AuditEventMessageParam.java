package com.hpe.testapplication.auditing;

/**
 * Class to represent message parameter test data.
 */
public class AuditEventMessageParam {

    public AuditEventMessageParam() {
    }

    public AuditEventMessageParam(String name, String type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    private String name;
    private String type;
    private Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
