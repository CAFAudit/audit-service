# Audit Monkey

## Description
The Audit Monkey provides the functional ability to test the Audit Service including the auto-generated AuditLog, (both the direct and webservice variants) and the Audit WebService.  
The Audit Monkey has the ability to send Audit Events both directly to Elasticsearch and via the Audit WebService.
The Audit Monkey also has the ability to generate significant volumes of data as it can be run in both single-threaded and multi-threaded modes.  

## Configuration

### Modes
The Audit Monkey can be run in a number of modes. Details of these modes are outlined in the table below.  
The three key modes cover:  

* Sending Audit Events directly to Elasticsearch or via the Audit WebService
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
    <td>direct, webservice</td>
    <td>
      <ul>
        <li><b>Direct:</b> Audit Events are sent directly to Elasticsearch</li>
        <li><b>WebService:</b> Audit Events are sent via the RESTful API of the Audit WebService to Elasticsearch</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td>CAF_AUDIT_MONKEY_MODE</td>
    <td>standard, random</td>
    <td>
      <ul>
        <li><b>Standard:</b> The Audit Monkey attempts to send the specified number of Audit Events as quickly as possible</li>
        <li><b>Random:</b> The Audit Monkey attempts to send portions of the overall specified number of Audit Events interlaced with pauses of execution, to create a pseudo-random sequence of Audit Events</li>
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
    <td>direct, [direct, webservice]</td>
    <td>Determines if the Audit Monkey sends Audit Events directly to Elasticsearch or via the WebService</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_TENANT_ID</td>
    <td>acmecorp, [String]</td>
    <td>Tenant Id, forms the index for the Audit Events within Elasticsearch</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_CORRELATION_ID</td>
    <td>UUID, [Auto Generated UUID, String]</td>
    <td>Can uniquely identify a particular run of the Audit Monkey</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_USER_ID</td>
    <td>road.runner@acme.com, [String]</td>
    <td>Configurable field, available to the user. User who triggered the Audit Event</td>
  </tr>
  <tr>
    <td>ES_CLUSTERNAME</td>
    <td>elasticserach-cluster, [String]</td>
    <td>Name of the Elasticsearch Cluster the Audit Monkey is to run against</td>
  </tr>
  <tr>
    <td>ES_HOSTNAME</td>
    <td>192.168.56.10, [IP Address, Hostname]</td>
    <td>IP Address or Hostname of Elasticsearch</td>
  </tr>
  <tr>
    <td>ES_PORT</td>
    <td>9300, [Port Number]</td>
    <td>Network Port Number of Elasticsearch</td>
  </tr>
  <tr>
    <td>WS_HOSTNAME</td>
    <td>192.168.56.10, [IP Address, Hostname]</td>
    <td>IP Address or Hostname of the Audit WebService</td>
  </tr>
  <tr>
    <td>WS_PORT</td>
    <td>25080, [Port Number]</td>
    <td>Network Port Number of the Audit WebService</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_MONKEY_MODE</td>
    <td>standard, [standard, random]</td>
    <td>Type of Audit Monkey to run</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_MONKEY_NUM_OF_EVENTS</td>
    <td>1, [Integer]</td>
    <td>Number of Audit Events to produce and send to Elasticsearch</td>
  </tr>
  <tr>
    <td>CAF_AUDIT_MONKEY_NUM_OF_THREADS</td>
    <td>1, [Integer]</td>
    <td>Number of threads to spin up which will send Audit Events</td>
  </tr>
</table>

## Execution

### How to Run the Audit Monkey
1. docker pull rh7-artifactory.svs.hpeswlab.net:8443/caf/audit-monkey:3.1.0-SNAPSHOT
2. docker run [OPTIONS] \<IMAGE\_ID\>

e.g.  
```
docker run -e CAF_AUDIT_MODE=direct -e CAF_AUDIT_MONKEY_MODE=standard -e CAF_AUDIT_MONKEY_NUM_OF_EVENTS=5000 -e CAF_AUDIT_MONKEY_NUM_OF_THREADS=10 <IMAGE_ID>
```

Run the Audit Monkey sending [5000] Audit Events [directly] to Elasticsearch in [Standard] mode using [10] threads

e.g.  
```
docker run -e CAF_AUDIT_TENANT_ID=wsTestId -e CAF_AUDIT_MODE=webservice -e WS_HOSTNAME=192.168.56.10 -e WS_PORT=25080 -e CAF_AUDIT_MONKEY_MODE=random -e CAF_AUDIT_MONKEY_NUM_OF_EVENTS=50 -e CAF_AUDIT_MONKEY_NUM_OF_THREADS=5 <IMAGE_ID>
```  

Run the Audit Monkey sending [50] Audit Events for Tenant Id [wsTestId] through the [Audit WebService] operating on host [192.168.56.10] and port [25080] in [Random] mode using [5] threads
