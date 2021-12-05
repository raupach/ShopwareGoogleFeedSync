# Shopware - Google - Feed - Sync
This tool can be used to export tagged products from Shopware 6 to Google Drive.


### Prerequisites
- Java 14 JRE or JDK
- Tested with Shopware 6.4.3.0

### Create application.properties
Go to /src/resources.
There is already a sample file (application.properties-example) available. Please 
copy it to application.properties. Adjust the configuration to your needs.


### Shopware preparation
1. Go to Properties->System->Integration
2. Create a new integration (name: printful, administrator: on) and add id and secret to your application.properties