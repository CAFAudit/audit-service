package com.hpe.caf.services.audit.api.generated;

import com.hpe.caf.services.audit.api.TenantAddPost;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-02-09T14:45:42.907Z")
public class TenantsApiServiceImpl extends TenantsApiService {

    @Override
    public Response tenantsPost(String tenantId, List<String> application, SecurityContext securityContext)
        throws Exception {

            TenantAddPost.addTenant(tenantId, application);
            return Response.ok().build();
    }
}
