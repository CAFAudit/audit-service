# Audit Monkey

## Description
The Audit Monkey provides the functional ability to test the Audit Service including the auto-generated AuditLog and the Audit Web Service.  
The Audit Monkey has the ability to send Audit Events both directly to Elasticsearch and via the Audit Web Service.  
The Audit Monkey also has the ability to generate significant volumes of data as it can be run in both single-threaded and multi-threaded modes.  

## Configuration

### Functionality
The Audit Monkey can be run in a number of functional configurations. Details of these functional configurations are outlined in the table below.  
The three key functional configurations cover:  

* Sending Audit Events directly to Elasticsearch or via the Audit Web Service
* Sending Audit Events continuously or in random mode featuring sleeps
* Executing as a single-threaded process or as a multi-threaded process

<table>
  <tr>
    <th>Mode</th>
    <th>Options</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>CAF_AUDIT_MODE</td>
    <td>elasticsearch, webservice</td>
    <td>
      <ul>
        <li><b>Elasticsearch:</b> Audit Events are sent directly to Elasticsearch</li>
        <li><b>WebService:</b> Audit Events are sent via the RESTful API of the Audit WebService to Elasticsearch</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>CAF_AUDIT_MONKEY_MODE</td>
    <td>standard, random, demo</td>
    <td>
      <ul>
        <li><b>Standard:</b> The Audit Monkey attempts to send the specified number of Audit Events as quickly as possible</li>
        <li><b>Random:</b> The Audit Monkey attempts to send portions of the overall specified number of Audit Events interlaced with pauses of execution, to create a pseudo-random sequence of Audit Events</li>
        <li><b>Demo:</b> The Audit Monkey generates random data across a number of tenants, users, and audit events to simulate data generated in a real world scenario</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>CAF_AUDIT_MONKEY_NUM_OF_THREADS</td>
    <td>Single-Threaded, Multi-Threaded</td>
    <td>
      <ul>
        <li><b>Single-Threaded:</b> Setting CAF_AUDIT_MONKEY_NUM_OF_THREADS to 1, will run the Audit Monkey as a single threaded process</li>
        <li><b>Multi-Threaded:</b> Setting CAF_AUDIT_MONKEY_NUM_OF_THREADS to value greater than 1, will run the Audit Monkey as a multi-threaded process executing in the number of threads provided</li>
      </ul>
    </td>
  </tr>
</table>

### Configuration can be set via Environment Variables  
The following parameters may be set as required:

<table>
  <tr>
    <th>Environment Variable</th>
    <th>Default, [Options]</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>CAF_AUDIT_MODE</td>
    <td>NONE, [elasticsearch, webservice]</td>
    <td>Determines if the Audit Monkey sends Audit Events directly to Elasticsearch or via the WebService</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_TENANT_ID</td>
    <td>acmecorp, [Any String]</td>
    <td>Tenant Id, forms the index for the Audit Events within Elasticsearch</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_CORRELATION_ID</td>
    <td>UUID, [UUID, Any String]</td>
    <td>Can uniquely identify a particular run of the Audit Monkey</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_USER_ID</td>
    <td>road.runner@acme.com, [Any String]</td>
    <td>Configurable field, available to the user. User who triggered the Audit Event</td>
  </tr>
  <tr>
     <td>CAF_ELASTIC_PROTOCOL</td>
     <td>http, [http or https]</td>
     <td>The protocol used to connect to the Elasticsearch server. e.g. http or https</td>
   </tr>
  <tr>
    <td>CAF_ELASTIC_HOST_AND_PORT_VALUES</td>
    <td>NONE, Any String</td>
    <td>A comma separated list of hostnames and ports to use when contacting elasticsearch. eg. localhost:9200,otherHost:9200</td>
  </tr>
  <tr>
    <td>CAF_ELASTIC_HOST_VALUES</td>
    <td>NONE, Any String</td>
    <td>This is an an alternative variable, with comma separated list of hostnames to use when contacting elasticsearch. eg. localhost,otherHost.</td>
  </tr>
  <tr>
    <td>CAF_ELASTIC_PORT_VALUE</td>
    <td>NONE, 9200</td>
    <td>The REST port of the Elasticsearch server listens on. eg. 9200. This is an alternative variable used to construct elastic search host and port by combining with CAF_ELASTIC_HOST_VALUES.</td>
  </tr>
  <tr>
    <td>CAF_ELASTIC_USERNAME</td>
    <td>null, [Any String]</td>
    <td>Elasticsearch username. Defaults to null (anonymous access).</td>
  </tr>
  <tr>
    <td>CAF_ELASTIC_PASSWORD</td>
    <td>null, [Any String]</td>
    <td>Elasticsearch password. Defaults to null (anonymous access).</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_WEBSERVICE_ENDPOINT_URL</td>
    <td>NONE, Any String</td>
    <td>The CAF Audit Webservice url endpoint to use when sending audit events.</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_MONKEY_MODE</td>
    <td>standard, [standard, random, demo]</td>
    <td>Type of Audit Monkey to run</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_MONKEY_NUM_OF_EVENTS</td>
    <td>1, [Any Int]</td>
    <td>Number of Audit Events to produce and send to Elasticsearch</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_MONKEY_NUM_OF_THREADS</td>
    <td>1, [Any Int]</td>
    <td>Number of threads to spin up which will send Audit Events</td>
  </tr>
</table>

## Execution

### Prerequisite

1. An instance of Elasticsearch available
2. Audit Web Service, (only required if Audit Events are to be sent via the Web Service)

### How to Run the Audit Monkey
1. docker pull cafaudit/audit-monkey:4.0.0
2. docker run [OPTIONS] \<IMAGE\_ID\>

e.g.  
```
docker run -e CAF_AUDIT_MODE=elasticsearch -e CAF_AUDIT_MONKEY_MODE=standard -e CAF_ELASTIC_PROTOCOL=http -e CAF_ELASTIC_HOST_VALUES=localhost -e CAF_ELASTIC_PORT_VALUE=9200 -e CAF_ELASTIC_USERNAME=jim -e CAF_ELASTIC_PASSWORD=secret -e CAF_AUDIT_MONKEY_NUM_OF_EVENTS=5000 -e CAF_AUDIT_MONKEY_NUM_OF_THREADS=10 <IMAGE_ID>
```

Run the Audit Monkey sending [5000] Audit Events [elasticsearch] to Elasticsearch in [Standard] mode using [10] threads

e.g.  
```
docker run -e CAF_AUDIT_TENANT_ID=wsTestId -e CAF_AUDIT_MODE=webservice -e CAF_AUDIT_WEBSERVICE_ENDPOINT_URL=192.168.56.10:25080 -e CAF_AUDIT_MONKEY_MODE=random -e CAF_AUDIT_MONKEY_NUM_OF_EVENTS=50 -e CAF_AUDIT_MONKEY_NUM_OF_THREADS=5 <IMAGE_ID>
```  

Run the Audit Monkey sending [50] Audit Events for Tenant Id [wsTestId] through the [Audit WebService] operating on host [192.168.56.10] and port [25080] in [Random] mode using [5] threads

e.g.  
```
docker run -e CAF_AUDIT_MODE=elasticsearch -e CAF_AUDIT_MONKEY_MODE=demo -e CAF_AUDIT_MONKEY_NUM_OF_EVENTS=10000 -e CAF_ELASTIC_PROTOCOL=http -e CAF_ELASTIC_HOST_VALUES=localhost -e CAF_ELASTIC_PORT_VALUE=9200 -e CAF_ELASTIC_USERNAME=jim -e CAF_ELASTIC_PASSWORD=secret -e CAF_AUDIT_MONKEY_NUM_OF_THREADS=25 <IMAGE_ID>
```

Run the Audit Monkey sending [10,000] Audit Events [elasticsearch] to Elasticsearch in [demo] mode using [25] threads
