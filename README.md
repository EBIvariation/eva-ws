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

Enabling Oauth2 Security
------------------------

In order to enable Oauth2 Security you must enable the `oauth2-security` profile in the spring-boot application in any
way supported by Spring-boot as stated in [Spring documentation](http://docs.spring.io/spring-boot/docs/current/reference/html/howto-properties-and-configuration.html#howto-set-active-spring-profiles). In adition, the application requires configuration to point to the public user check endpoints
of your oauth2 compatible authentication service. More information is available in [Spring documentation](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-security.html#boot-features-security-oauth2-resource-server).

When using Oauth2 Security, swagger UI will be still enabled and public to review the API but no command sent from the ui will be authorized.
