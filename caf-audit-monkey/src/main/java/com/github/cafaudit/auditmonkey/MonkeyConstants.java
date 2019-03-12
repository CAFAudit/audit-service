/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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
package com.github.cafaudit.auditmonkey;

public class MonkeyConstants
{
    // Monkey Mode: Direct to Elasticsearch OR Via Webservice
    public static final String CAF_AUDIT_MODE = "CAF_AUDIT_MODE";
    public static final String ELASTICSEARCH = "elasticsearch";
    public static final String WEBSERVICE = "webservice";
    
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

    // Monkey Modes
    public static final String CAF_AUDIT_STANDARD_MONKEY = "standard";
    public static final String CAF_AUDIT_RANDOM_MONKEY = "random";
    public static final String CAF_AUDIT_DEMO_MONKEY = "demo";
    public static final String[] ARRAY_OF_MONKEYS = new String[]{CAF_AUDIT_STANDARD_MONKEY, CAF_AUDIT_RANDOM_MONKEY, CAF_AUDIT_DEMO_MONKEY};
    
    // Monkey environment variables
    public static final String CAF_AUDIT_MONKEY_MODE = "CAF_AUDIT_MONKEY_MODE";
    public static final String CAF_AUDIT_MONKEY_NUM_OF_EVENTS = "CAF_AUDIT_MONKEY_NUM_OF_EVENTS";
    public static final String CAF_AUDIT_MONKEY_NUM_OF_THREADS = "CAF_AUDIT_MONKEY_NUM_OF_THREADS";

}
