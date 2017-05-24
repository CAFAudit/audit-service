package com.github.cafaudit.auditmonkey;

public class AuditMonkeyConstants
{
    // Monkey Mode: Direct to Elasticsearch OR Via Webservice
    public static final String CAF_AUDIT_MODE = "CAF_AUDIT_MODE";
    
    // Configurable data fields
    public static final String CAF_AUDIT_TENANT_ID = "CAF_AUDIT_TENANT_ID";
    public static final String CAF_AUDIT_CORRELATION_ID = "CAF_AUDIT_CORRELATION_ID";
    public static final String CAF_AUDIT_USER_ID = "CAF_AUDIT_USER_ID"; 

    // Elasticsearch environment variables
    public static final String ES_CLUSTERNAME = "ES_CLUSTERNAME";
    public static final String ES_HOSTNAME = "ES_HOSTNAME";
    public static final String ES_PORT = "ES_PORT";

    // Audit WebService environment variables
    public static final String WS_HOSTNAME = "WS_HOSTNAME";
    public static final String WS_PORT = "WS_PORT";

    // Monkey
    public static final String STANDARD_MONKEY = "standard";
    public static final String RANDOM_MONKEY = "random";
    public static final String NUM_OF_EVENTS = "NUM_OF_EVENTS";
    public static final String CAF_AUDIT_MONKEY_MODE = "CAF_AUDIT_MONKEY_MODE";

}
