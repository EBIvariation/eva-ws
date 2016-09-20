European Variation Archive (EVA)
======

The European Variation Archive is an open-access database of all types of genetic variation data from all species. The service is available in https://www.ebi.ac.uk/eva

This repository contains the core of the application and the web services.

Build
-----

In order to build EVA, you need to install the Java Development Kit 7 and Maven.

The project dependencies are OpenCGA and Variation-Commons 

You can get OpenCGA 0.5.2 from https://github.com/opencb/opencga, branch `hotfix/0.5`. Please follow the download/compilation instructions there.

You can get Variation-Commons from https://github.com/EBIvariation/variation-commons. This project can be installed with just `mvn clean install`.

After it has been compiled, if you just want to build the WAR, run `mvn package -DskipTests` and you should obtain a file to deploy in Tomcat or other Java container.

Testing
-------

The tests implemented so far are integration (not unit) tests, so a working WAR file needs to be created first. The Jetty plugin for Maven has been included to ease the testing process.

1. Fill the datasource information in the file `eva-server/src/main/webapp/WEB-INF/jetty-env.xml`
2. Build the WAR file as described in the section above
3. Run `mvn jetty:run` from the eva-server subfolder
4. Run `mvn test` from the root folder

