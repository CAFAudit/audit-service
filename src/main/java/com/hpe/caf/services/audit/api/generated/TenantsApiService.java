package com.hpe.caf.services.audit.api.generated;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-02-09T14:45:42.907Z")
public abstract class TenantsApiService {

      public abstract Response tenantsPost(String tenantId, List<String> application, SecurityContext securityContext)
              throws Exception;

}
