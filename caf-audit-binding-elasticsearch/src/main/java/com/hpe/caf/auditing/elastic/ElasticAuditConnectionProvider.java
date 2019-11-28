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
package com.hpe.caf.auditing.elastic;

import com.hpe.caf.auditing.AuditConnection;
import com.hpe.caf.auditing.AuditConnectionProvider;
import com.hpe.caf.auditing.AuditImplementation;
import com.hpe.caf.auditing.exception.AuditConfigurationException;

@AuditImplementation("elasticsearch")
public class ElasticAuditConnectionProvider implements AuditConnectionProvider
{
    @Override
    public AuditConnection getConnection() throws AuditConfigurationException
    {
        return new ElasticAuditConnection();
    }

}
