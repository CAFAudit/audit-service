!not-ready-for-release!

#### Version Number
${version-number}

#### New Features

#### Breaking Changes
- **US361030**: Java 8 and Java 11 support dropped  
  Java 17 is now the minimum supported version.

- **US361030**: SSL configuration environment variables changed  
  The `SSL_TOMCAT_*` environment variables are no longer respected.  
  The following environment variables are now used to configure SSL:
  - `SSL_KEYSTORE_PATH`
  - `SSL_KEYSTORE`
  - `SSL_KEYSTORE_TYPE` (Optional, defaults to `JKS`)
  - `SSL_KEYSTORE_PASSWORD`
  - `SSL_CERT_ALIAS`
  - `SSL_VALIDATE_CERTS` (Optional, defaults to `false`)

#### Known Issues
