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

import com.hpe.caf.services.audit.api.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class ApiServiceUtilTest {

    @Test
    public void testGetAppConfigPropertiesSuccess () throws Exception {

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        newEnv.put("CAF_DATABASE_URL","testUrl");
        newEnv.put("CAF_DATABASE_SERVICE_ACCOUNT","testServiceUser");
        newEnv.put("CAF_DATABASE_SERVICE_ACCOUNT_PASSWORD","testPassword");
        newEnv.put("CAF_DATABASE_LOADER_ACCOUNT","testLoaderUser");
        newEnv.put("CAF_DATABASE_LOADER_ACCOUNT_PASSWORD","testPassword");
        newEnv.put("CAF_DATABASE_READER_ROLE","testReaderRole");
        TestUtil.setSystemEnvironmentFields(newEnv);

        //  Test successful call to class method.
        AppConfig configProps = ApiServiceUtil.getAppConfigProperties();
        Assert.assertEquals(configProps.getDatabaseURL(),"testUrl");
        Assert.assertEquals(configProps.getDatabaseServiceAccount(),"testServiceUser");
        Assert.assertEquals(configProps.getDatabaseServiceAccountPassword(),"testPassword");
        Assert.assertEquals(configProps.getDatabaseLoaderAccount(),"testLoaderUser");
        Assert.assertEquals(configProps.getDatabaseLoaderAccountPassword(),"testPassword");
        Assert.assertEquals(configProps.getDatabaseReaderRole(),"testReaderRole");
    }

    @Test(expected = BadRequestException.class)
    public void testGetAppConfigPropertiesFailure_MissingDatabaseUrl () throws Exception {

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        TestUtil.setSystemEnvironmentFields(newEnv);

        //  Test expected failure call to class method because of missing database url.
        AppConfig configProps = ApiServiceUtil.getAppConfigProperties();
    }

    @Test(expected = BadRequestException.class)
    public void testGetAppConfigPropertiesFailure_MissingServiceUserCredentials () throws Exception {

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        newEnv.put("CAF_DATABASE_URL","testUrl");
        TestUtil.setSystemEnvironmentFields(newEnv);

        //  Test expected failure call to class method because of missing service user credentials.
        AppConfig configProps = ApiServiceUtil.getAppConfigProperties();
    }

    @Test(expected = BadRequestException.class)
    public void testGetAppConfigPropertiesFailure_MissingLoaderUserCredentials () throws Exception {

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        newEnv.put("CAF_DATABASE_URL","testUrl");
        newEnv.put("CAF_DATABASE_SERVICE_ACCOUNT","testServiceUser");
        newEnv.put("CAF_DATABASE_SERVICE_ACCOUNT_PASSWORD","testPassword");
        TestUtil.setSystemEnvironmentFields(newEnv);

        //  Test expected failure call to class method because of missing loader user credentials.
        AppConfig configProps = ApiServiceUtil.getAppConfigProperties();
    }

    @Test
    public void testIsNotNullOrEmpty () throws Exception {

        String test = "Test String";
        boolean returnValue = ApiServiceUtil.isNotNullOrEmpty(test);
        Assert.assertTrue(returnValue);
    }

    @Test
    public void testIsNotNullOrEmpty_Null () throws Exception {

        String test = null;
        boolean returnValue = ApiServiceUtil.isNotNullOrEmpty(test);
        Assert.assertFalse(returnValue);
    }

    @Test
    public void testIsNotNullOrEmpty_Empty () throws Exception {

        String test = "";
        boolean returnValue = ApiServiceUtil.isNotNullOrEmpty(test);
        Assert.assertFalse(returnValue);
    }

    @Test
    public void testGetVerticaType_short () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("short",null);
        Assert.assertEquals("int", returnValue);
    }

    @Test
    public void testGetVerticaType_int () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("int",null);
        Assert.assertEquals("int", returnValue);
    }

    @Test
    public void testGetVerticaType_long () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("long",null);
        Assert.assertEquals("int", returnValue);
    }

    @Test
    public void testGetVerticaType_float () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("float",null);
        Assert.assertEquals("float", returnValue);
    }

    @Test
    public void testGetVerticaType_double () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("double",null);
        Assert.assertEquals("float", returnValue);
    }

    @Test
    public void testGetVerticaType_boolean () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("boolean",null);
        Assert.assertEquals("boolean", returnValue);
    }

    @Test
    public void testGetVerticaType_date () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("date",null);
        Assert.assertEquals("timestamp", returnValue);
    }

    @Test
    public void testGetVerticaType_string_max_varchar () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("string",65000);
        Assert.assertEquals("varchar(65000)", returnValue);
    }

    @Test
    public void testGetVerticaType_string_max_long_varchar () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("string",32000000);
        Assert.assertEquals("long varchar(32000000)", returnValue);
    }

    @Test
    public void testGetVerticaType_string_null () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("string",null);
        Assert.assertEquals("varchar(65000)", returnValue);
    }

    @Test(expected = Exception.class)
    public void testGetVerticaType_unknown_type () throws Exception{
        String returnValue = ApiServiceUtil.getVerticaType("test",null);
    }
}
