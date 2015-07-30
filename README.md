European Variation Archive (EVA)
======

The European Variation Archive is an open-access database of all types of genetic variation data from all species. The service is available in https://www.ebi.ac.uk/eva

This repository contains the core of the application and the web services.

Build
-----

In order to build EVA, you need to install the Java Development Kit 7 and Maven.

The main dependency is OpenCGA 0.5.2, which you can get from https://github.com/opencb/opencga. Please follow the download/compilation instructions there.

After it has been compiled, just run `mvn package` and you should obtain a WAR file which you can deploy in Tomcat or other Java container.
