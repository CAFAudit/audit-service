## CAF_7001 - Test vagrant images for Vertica and Kafka ##

Run and perform sanity check on vagrant images for Vertica and Kakfa

**Test Steps**

QA - Vagrant-Kafka 
Basic test:

1. Install VirtualBox & Vagrant. 
2. Pull latest vagrant-kafka sources from GIT. 
3. Run vagrant up. 
4. Follow Test Kafka Cluster Set-up (i.e. Single Broker Cluster) steps in vagrant-kafka.md documentation. 
5. Follow Test Kafka-Manager Set-up steps in vagrant-kafka.md documentation. 

QA - Vagrant-Vertica
 
1. Install VirtualBox & Vagrant. 
2. Pull latest vagrant-vertica sources from GIT. 
3. Copy Vertica download file from \\BELFS - see vagrant-vertica.md documentation. 
4. Run vagrant up. 
5. Verify sample database/table creation:
6. Create ssh connection to Vertica VM (e.g. via Putty)
7. Login as dbadmin with password
8. Launch Vertica's Admintools and confirm 'CAFAudit' database has been installed and up and running
9. Use IntelliJ plugin or 3rd party SQL client tool to connect to the Vertica database and confirm a new table 'ApplicationX' has been created
10. Confirm provisioned Kafka integration set-up by confirming scheduler has been created and recorded as expected	in the relevant database tables. Connect to DB using SQL client and run:
	1. select * FROM kafka_config.kafka_scheduler
	2. select * FROM kafka_config.kafka_targets
	3. 
select * FROM kafka_config.kafka_scheduler_history


**Test Data**

Vagrant files for both Vertica and Kakfa

**Expected Result**

Both machines run successfully and the tests as described behave as expected

**JIRA Link** - [CAF-373](https://jira.autonomy.com/browse/CAF-373)

