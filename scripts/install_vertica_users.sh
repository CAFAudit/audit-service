#!/bin/bash

VERTICA_INSTALL_DIR=/opt/vertica

# Database audit management web service user
VERTICA_CA_SERVICE_DATABASE_ACCOUNT=caf-audit-service
VERTICA_CA_SERVICE_DATABASE_ACCOUNT_PASSWORD="'c@Fa5eR51cE'"

# Database audit management reporting role
VERTICA_CA_READER_DATABASE_ROLE=caf-audit-read

# Database audit management reporting user
VERTICA_CA_READER_DATABASE_ACCOUNT=caf-audit-reader
VERTICA_CA_READER_DATABASE_ACCOUNT_PASSWORD="'c@FaR3aD3R'"

# Database audit management user for kafka/vertica integration
VERTICA_CA_LOADER_DATABASE_ACCOUNT=caf-audit-loader
VERTICA_CA_LOADER_DATABASE_ACCOUNT_PASSWORD="'c@FaL0Ad3r'"

# VSQL command line usage.
VSQL=${VERTICA_INSTALL_DIR}/bin/vsql

##### Functions

function createCAFAuditReaderRole {
	echo "Creating ${VERTICA_CA_READER_DATABASE_ROLE} database role..."
	su - dbadmin -c "${VSQL} -d ${VERTICA_DATABASE_NAME} -w ${VERTICA_DATABASE_PASSWORD} -c \"CREATE ROLE \\\"${VERTICA_CA_READER_DATABASE_ROLE}\\\" \"" 
}

function createCAFAuditReaderUser {
	echo "Creating ${VERTICA_CA_READER_DATABASE_ACCOUNT} database user..."
	su - dbadmin -c "${VSQL} -d ${VERTICA_DATABASE_NAME} -w ${VERTICA_DATABASE_PASSWORD} -c \"CREATE USER \\\"${VERTICA_CA_READER_DATABASE_ACCOUNT}\\\" IDENTIFIED BY ${VERTICA_CA_READER_DATABASE_ACCOUNT_PASSWORD}\"" 

	echo "Granting caf-audit-read role to ${VERTICA_CA_READER_DATABASE_ACCOUNT}..."
	su - dbadmin -c "${VSQL} -d ${VERTICA_DATABASE_NAME} -w ${VERTICA_DATABASE_PASSWORD} -c \"GRANT \\\"${VERTICA_CA_READER_DATABASE_ROLE}\\\" TO \\\"${VERTICA_CA_READER_DATABASE_ACCOUNT}\\\" \"" 
	
	echo "Enabling caf-audit-read role by default to ${VERTICA_CA_READER_DATABASE_ROLE}..."
	su - dbadmin -c "${VSQL} -d ${VERTICA_DATABASE_NAME} -w ${VERTICA_DATABASE_PASSWORD} -c \"ALTER USER \\\"${VERTICA_CA_READER_DATABASE_ACCOUNT}\\\" DEFAULT ROLE \\\"${VERTICA_CA_READER_DATABASE_ROLE}\\\" \"" 
}

function createCAFAuditServiceUser {
	echo "Creating ${VERTICA_CA_SERVICE_DATABASE_ACCOUNT} database user..."
	su - dbadmin -c "${VSQL} -d ${VERTICA_DATABASE_NAME} -w ${VERTICA_DATABASE_PASSWORD} -c \"CREATE USER \\\"${VERTICA_CA_SERVICE_DATABASE_ACCOUNT}\\\" IDENTIFIED BY ${VERTICA_CA_SERVICE_DATABASE_ACCOUNT_PASSWORD}\"" 

	echo "Granting CREATE on database ${VERTICA_DATABASE_NAME} to ${VERTICA_CA_SERVICE_DATABASE_ACCOUNT}..."
	su - dbadmin -c "${VSQL} -d ${VERTICA_DATABASE_NAME} -w ${VERTICA_DATABASE_PASSWORD} -c \"GRANT CREATE ON DATABASE ${VERTICA_DATABASE_NAME} TO \\\"${VERTICA_CA_SERVICE_DATABASE_ACCOUNT}\\\" \"" 
}

function createCAFAuditLoaderUser {
	echo "Creating ${VERTICA_CA_LOADER_DATABASE_ACCOUNT} database user..."
	su - dbadmin -c "${VSQL} -d ${VERTICA_DATABASE_NAME} -w ${VERTICA_DATABASE_PASSWORD} -c \"CREATE USER \\\"${VERTICA_CA_LOADER_DATABASE_ACCOUNT}\\\" IDENTIFIED BY ${VERTICA_CA_LOADER_DATABASE_ACCOUNT_PASSWORD}\"" 

	echo "Granting PSEUDOSUPERUSER role to ${VERTICA_CA_LOADER_DATABASE_ACCOUNT}..."
	su - dbadmin -c "${VSQL} -d ${VERTICA_DATABASE_NAME} -w ${VERTICA_DATABASE_PASSWORD} -c \"GRANT PSEUDOSUPERUSER TO \\\"${VERTICA_CA_LOADER_DATABASE_ACCOUNT}\\\" \"" 
	
	echo "Enabling PSEUDOSUPERUSER role by default to ${VERTICA_CA_LOADER_DATABASE_ACCOUNT}..."
	su - dbadmin -c "${VSQL} -d ${VERTICA_DATABASE_NAME} -w ${VERTICA_DATABASE_PASSWORD} -c \"ALTER USER \\\"${VERTICA_CA_LOADER_DATABASE_ACCOUNT}\\\" DEFAULT ROLE PSEUDOSUPERUSER\"" 
}

# Vertica database creation.
createCAFAuditReaderRole;
createCAFAuditReaderUser;
createCAFAuditServiceUser;
createCAFAuditLoaderUser;
