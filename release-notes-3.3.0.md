#### Version Number
${version-number}

#### New Features
- [SCMOD-5879](https://autjira.microfocus.com/browse/SCMOD-5879): Audit implementations broken into separate jar files  
Auditing implementations, direct to elasticsearch and auditing via CAF Audit Webservice, have been broken into their own jars and out of the CAF Audit project. This change now means that the CAF Audit service container must contain either one or both of the implementation jars.  
The implementation to use can then be selected as before using the CAF_AUDIT_MODE environment variable.  

#### Known Issues
