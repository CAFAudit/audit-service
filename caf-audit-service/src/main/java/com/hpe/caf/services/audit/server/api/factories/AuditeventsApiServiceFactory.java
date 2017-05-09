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
package com.hpe.caf.services.audit.server.api.factories;

import com.hpe.caf.services.audit.server.api.AuditeventsApiService;
import com.hpe.caf.services.audit.server.api.impl.AuditeventsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2017-04-28T07:15:58.947+01:00")
public class AuditeventsApiServiceFactory {

   private final static AuditeventsApiService service = new AuditeventsApiServiceImpl();

   public static AuditeventsApiService getAuditeventsApi()
   {
      return service;
   }
}
