# Using App Analytics for Auditing Reporting

### Prerequisites:

- Postgres 9.4 or greater
- Marathon Docker Dev Vagrant VM ( Install pack is in perforce under P4V\belfast\PolicyServer\PolicyWorkerAggregator\InstallPack )
- Vertica DB, running and containing Audit Data
    - Vertica DB VM: [https://github.hpe.com/caf/vagrant-vertica](https://github.hpe.com/caf/vagrant-vertica)
    - Follow [Auditing](https://rndwiki.corp.hpecorp.net/confluence/display/CAF/Auditing) for Vertica DB Audit Data setup instructions

### Create Postgres database for App Analytics

From your Postgres DB host and using a Command Line Interface such as CMD or BASH:

1. Login to Postgres DB  
Command: psql -U postgres
2. Create the database for App Analytics  
Command: CREATE DATABASE aspen_analytics;

There are no further utilities required to setup App Analytics Postgres DB. App Analytics uses database properties defined in 'anayltics-services.yaml' (Section: **Setting up and integrating App Analytics with CAF Audit data**) and a mechanism called Flyway to take care of schema and table creation for you when you first run App Analytics.

### Creating App Analytics User for querying CAF Audit data

App Analytics should only be connecting to Vertica using its own read only user. Create and configure a new read only user with the following instructions:

1. SSH to your Vertica host machine
2. Switch to the Vertica DBAdmin user  
Command: su dbadmin  
Password: password 
3. Open the Vertica Admin Tool  
Command: /opt/vertica/bin/admintools
4. Choose the "Connect to Database" option  
Password: CAFAudit
5. From the Vertica Database interactive terminal 
    1. Create a new user for App Analytics  
Command:  CREATE USER "app-analytics-audit-user" IDENTIFIED BY 'CAFAudit' ;
    2. Grant the new user the CAF Audit reader role  
Command:  GRANT "caf-audit-read" TO "app-analytics-audit-user" ;
    3. Enable the App Analytics user's CAF Audit reader role  
Command:  ALTER USER "app-analytics-audit-user" DEFAULT ROLE "caf-audit-read" ;

### Setting up and integrating App Analytics with CAF Audit data

App Analytics retrieves its configuration from an 'analytics-service.yaml' settings file. This configuration file contains properties for IDOL or ElasticSearch, Postgres and Vertica.

1. Create 'analytics-service.yaml' and configure Postgres ('db') and Vertica ('vertica.db') properties values. Although IDOL is not used for querying Audit Data in Vertica it is required that you set a host for App Analytics to run ('content' properties).  
Example 'analytics-service.yaml' config is attached to this wiki page: [analytics-services.yaml](https://rndwiki.corp.hpecorp.net/confluence/download/attachments/1038399424/analytics-services.yaml?version=1&modificationDate=1462273592000&api=v2)
2. Store your 'analytics-service.yaml' within your Marathon Docker Dev Vagrant VM user folder '~/' and '/var/lib/analytics-services/' directories.
3. Run App Analytics container  
Command: docker run -d --name analytics -p 10010:8080 -v ~/:/var/lib/analytics-services rh7-artifactory.hpswlabs.hp.com:8444/aspen/analytics:latest
4. Go to http://&lt;MARATHON_DOCKER_DEV_VAGRANT_VM_HOSTNAME&gt;:10010/swagger/ to use App Analytics REST API via Swagger UI

### Using App Analytics API to query Vertica Audit data

It is possible to query Audit data in Vertica using App Anayltics Data Access SQL API and/or Data Access Data Templates APIs.

You can read more about the Analytics service [here](https://rndwiki.corp.hpecorp.net/confluence/display/CAF/Analytics+Microservice).

Data Access SQL API** is a REST POST, to /platform-services/api/sqldata, that takes in raw SQL in JSON format for a Tenant ID specified in the request’s X-TENANT-ID header. Example:
 
```
{
  "sql": "SELECT * FROM SampleApp WHERE eventTime > '2016-04-06 09:51:26.125' AND eventTime < '2016-04-06 09:51:26.376'"
}
```

Data Access Data Templates APIs** allow you to create parameterisable SQL queries as templates via POST to /platform-services/api/data in JSON format. Example:

 
```
{
  "id": "events-by-time",
  "description": "Return the events by date between to and from time slots",
  "sqlTemplate": "SELECT * FROM SampleApp WHERE eventTime > ':fromTime' AND eventTime < ':toTime'",
  "verticaRef": "id",
  "idolRef": null
}
```

You can then query this template with a POST to /platform-services/api/data/events-by-time for a Tenant ID specified in the request’s X-TENANT-ID header and body containing JSON in the following format:

```
{
    "params" : {
                "fromTime": "2016-04-06 09:51:26.125",
                "toTime": "2016-04-07 14:19:40.834"
    }
}
```

The response looks like the following:

```
{
  "data": [
    {
      "key": null,
      "values": [
        {
          "eventParamInt64Type": 2,
          "eventParamDateType2": null,
          "eventParamStringType": "user1",
          "eventParamFloatType": 23.45,
          "eventTimeSource": "RAINE12",
          "eventParamInt32Type": 2,
          "userId": "user2@hpe.com",
          "eventParamBooleanType": false,
          "eventParamInt16Type2": null,
          "eventParamDateType": 1453121126319,
          "threadId": 1,
          "eventTypeId": "TestEvent1",
          "eventParamInt16Type": 2,
          "processId": "d01b6de8-eb27-4dd6-a4b0-d7327d42bc77",
          "eventParamStringType2": null,
          "eventParamStringType3": null,
          "eventTime": 1459936286372,
          "eventParamDoubleType": 123.4,
          "correlationId": "correlation2",
          "eventOrder": 1,
          "eventCategoryId": "TestCategory1"
        },
        {
          "eventParamInt64Type": null,
          "eventParamDateType2": null,
          "eventParamStringType": null,
          "eventParamFloatType": null,
          "eventTimeSource": "RAINE12",
          "eventParamInt32Type": null,
          "userId": "user3@hpe.com",
          "eventParamBooleanType": null,
          "eventParamInt16Type2": 3,
          "eventParamDateType": null,
          "threadId": 1,
          "eventTypeId": "TestEvent2",
          "eventParamInt16Type": null,
          "processId": "d01b6de8-eb27-4dd6-a4b0-d7327d42bc77",
          "eventParamStringType2": "user3",
          "eventParamStringType3": null,
          "eventTime": 1459936286376,
          "eventParamDoubleType": null,
          "correlationId": "correlation3",
          "eventOrder": 2,
          "eventCategoryId": "TestCategory2"
        }
      ]
    }
  ]
}
```

If App Analytics did not return fields with null values we could use Data Templates for reporting requirements. Data Templates can be auto setup using REST requests or by populating the aspen_analytics Postgres DB data_template table.
