#### Version Number
${version-number}

#### New Features
- [SCMOD-5879](https://autjira.microfocus.com/browse/SCMOD-5879): Audit implementations broken into separate jar files  
Auditing implementations, direct to elasticsearch and auditing via CAF Audit Webservice, have been broken into their own jars and out of the CAF Audit project. This change now means that the CAF Audit service container must contain either one or both of the implementation jars.  
The implementation to use can then be selected as before using the CAF_AUDIT_MODE environment variable.  
- [SCMOD-5968](https://portal.digitalsafe.net/browse/SCMOD-5968): Updated to use openSUSE Leap 15 base images
- [CAF-4049](https://portal.digitalsafe.net/browse/CAF-4049): Use CAF_AUDIT_MONKEY_JAVA_OPTS in startup
- [CAF-3718](https://portal.digitalsafe.net/browse/CAF-3718): Updated Audit Service to support multi-process logging
- [Updated base projects to get new swagger branding](https://github.com/CAFAudit/audit-service/pull/45)
- [Adding audit-service-deploy to the build process](https://github.com/CAFAudit/audit-service/commit/0150a6f00437b1afe5ed70fc3740e81989ebae48)

#### Known Issues
- None
