package com.hpe.caf.services.audit.api.generated;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-02-09T14:45:42.907Z")
public class TenantsApiServiceFactory {

   private final static TenantsApiService service = new TenantsApiServiceImpl();

   public static TenantsApiService getTenantsApi()
   {
      return service;
   }
}
