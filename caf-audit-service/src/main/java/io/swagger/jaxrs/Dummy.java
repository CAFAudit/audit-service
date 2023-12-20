/*
 * Copyright 2015-2024 Open Text.
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
package io.swagger.jaxrs;

/**
 * This class under package named io.swagger.jaxrs created to avoid compilation 
 * error in swagger generated Api class com.hpe.caf.services.audit.server.api.AuditeventsApi. 
 * The error is due to unused import io.swagger.jaxrs.*.
 * For this, the dependency io.swagger:swagger-jersey2-jaxrs should be declared as compile time dependency.
 * But io.swagger:swagger-jersey2-jaxrs is required as runtime dependency 
 * to use io.swagger.jersey.config.JerseyJaxrsConfig class in web.xml.
 */
final class Dummy {

    private Dummy() {
        
    }
}
