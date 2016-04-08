package com.hpe.caf.services.audit.api.generated;

import com.hpe.caf.services.audit.api.generated.model.NewTenant;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-02-09T14:45:42.907Z")
public abstract class TenantsApiService {

      public abstract Response tenantsPost(NewTenant newTenant, SecurityContext securityContext)
              throws Exception;

}
