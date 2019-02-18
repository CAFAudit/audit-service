!not-ready-for-release!

#### Version Number
${version-number}

#### New Features
- (SCMOD-5879)[]: Audit implementations broken into seperate jar file
    Auditing implementations, direct to elasticsearch and auditing via CAF Audit Webservice, have been broken into their own jars and out of the CAF Audit project. This change now means that the CAF Audit service container must container either one or both of the implementataion jars.
 The implementation to use can then be selected as before using the CAF_AUDIT_MODE evironment variable. 

#### Known Issues
