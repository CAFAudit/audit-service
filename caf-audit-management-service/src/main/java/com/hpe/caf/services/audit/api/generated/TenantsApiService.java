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
package com.hpe.caf.services.audit.api.generated;

import com.hpe.caf.services.audit.api.generated.model.NewTenant;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-02-09T14:45:42.907Z")
public abstract class TenantsApiService {

      public abstract Response tenantsPost(NewTenant newTenant, SecurityContext securityContext)
              throws Exception;

      public abstract Response tenantsUpdatePartitionCountPost(String tenantId, String applicationId, SecurityContext securityContext)
              throws Exception;
}
