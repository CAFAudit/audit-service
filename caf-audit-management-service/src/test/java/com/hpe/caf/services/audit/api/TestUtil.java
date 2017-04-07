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

import com.hpe.caf.auditing.schema.AuditEvent;
import com.hpe.caf.auditing.schema.AuditEventParam;
import com.hpe.caf.auditing.schema.AuditEventParamType;
import com.hpe.caf.auditing.schema.AuditedApplication;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class TestUtil {

    public static void setSystemEnvironmentFields(Map<String, String> newenv) throws Exception {
        Class[] classes = Collections.class.getDeclaredClasses();

        //  Get map view of the current system environment.
        Map<String, String> env = System.getenv();
        for(Class cl : classes) {
            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                map.clear();
                map.putAll(newenv);
            }
        }
    }

    public static AuditedApplication getAuditedApplication (String applicationId) throws Exception {

        AuditedApplication aa = new AuditedApplication();
        aa.setApplicationId(applicationId);

        AuditedApplication.AuditEvents aes = new AuditedApplication.AuditEvents();
        AuditEvent ae = new AuditEvent();

        AuditEventParam aep = new AuditEventParam();
        aep.setName("StringType");
        aep.setType(AuditEventParamType.STRING);
        aep.setColumnName("StringType");
        aep.setDescription("Description for StringType");

        AuditEvent.Params params = new AuditEvent.Params();
        params.getParam().add(aep);

        ae.setParams(params);
        aes.getAuditEvent().add(ae);
        aa.setAuditEvents(aes);

        return aa;
    }

}
