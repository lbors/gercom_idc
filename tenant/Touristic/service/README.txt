Touristic CDN service auto-configuration.

command: python3 tourCDNdeployment.py slice_creation_response app-data.json 

Files description:

   1. tourCDNdeployment.py : This script is executed from the tenant after the slice creation for the Touristic service auto-configuration.
   2. app-data.json : Describe the DNS configuration message format
   3. slice_creation_responce: This yaml has been provided from UFG and describe the slice that has been created.
   4. yaml-files_touristicCDN_service_touristic_cdn_slice-activator-to-service-orchestrator-adaptor_v2.yaml: This file contains the scipts needed for touristic service configuration. This script has been created according to dogot's file.
