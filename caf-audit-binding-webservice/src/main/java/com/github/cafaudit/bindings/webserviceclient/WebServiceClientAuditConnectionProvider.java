/*
 * Copyright 2015-2026 Open Text.
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
package com.github.cafaudit.bindings.webserviceclient;

import com.github.cafaudit.service.core.AuditConnection;
import com.github.cafaudit.service.core.AuditConnectionProvider;
import com.github.cafaudit.service.core.AuditImplementation;
import com.github.cafaudit.service.core.exception.AuditConfigurationException;

@AuditImplementation("webservice")
public class WebServiceClientAuditConnectionProvider implements AuditConnectionProvider
{
    @Override
    public AuditConnection getConnection() throws AuditConfigurationException
    {
        return new WebServiceClientAuditConnection();
    }

}
