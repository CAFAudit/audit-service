package com.hpe.caf.services.audit.api;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class ApiServiceUtilTest {

    @Test
    public void testGetAppConfigPropertiesSuccess () throws Exception {

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        newEnv.put("database.url","testUrl");
        newEnv.put("database.service.account","testServiceUser");
        newEnv.put("database.service.account.password","testPassword");
        newEnv.put("database.loader.account","testLoaderUser");
        newEnv.put("database.loader.account.password","testPassword");
        newEnv.put("database.reader.role","testReaderRole");
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
        newEnv.put("database.url","testUrl");
        TestUtil.setSystemEnvironmentFields(newEnv);

        //  Test expected failure call to class method because of missing service user credentials.
        AppConfig configProps = ApiServiceUtil.getAppConfigProperties();
    }

    @Test(expected = BadRequestException.class)
    public void testGetAppConfigPropertiesFailure_MissingLoaderUserCredentials () throws Exception {

        //  Set-up test database connection properties.
        HashMap<String, String> newEnv  = new HashMap<>();
        newEnv.put("database.url","testUrl");
        newEnv.put("database.service.account","testServiceUser");
        newEnv.put("database.service.account.password","testPassword");
        TestUtil.setSystemEnvironmentFields(newEnv);

        //  Test expected failure call to class method because of missing loader user credentials.
        AppConfig configProps = ApiServiceUtil.getAppConfigProperties();
    }

}
