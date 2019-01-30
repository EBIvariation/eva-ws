European Variation Archive (EVA)
======

The European Variation Archive is an open-access database of all types of genetic variation data from all species. The service is available in https://www.ebi.ac.uk/eva

This repository contains the core of the application and the web services.

Build
-----

In order to build EVA, you need to install the Java Development Kit 8 and Maven.

All the dependencies will be downloaded by Maven.

If you want to build all the modules, including dgva-server, you will need to download the Oracle drivers used in dgva-server from the Oracle Maven Repository. In order to do so, [register with Oracle](https://login.oracle.com/mysso/signon.jsp) and [set up your Maven security](https://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9016). See https://github.com/EBIvariation/eva-ws/issues/81#issuecomment-369212870 for more information.

There are some properties that you have to provide. If you can access https://github.com/EBIvariation/configuration/, use those configuration files. If not, save the next snippet in your ~/.m2/settings.xml to use a Maven profile and fill all the properties (fill empty values when needed and change everything starting with "your_"):

```xml
<settings>
    <profiles>
        <profile>
            <id>production</id>
            <properties>
                <dgvapro.host>jdbc:oracle:thin:@your_host:your_port:your_dgva_db</dgvapro.host>
                <dgvapro.user></dgvapro.user>
                <dgvapro.passwd></dgvapro.passwd>
                <eva.evapro.datasource></eva.evapro.datasource>
                <eva.evapro.jdbc.url>jdbc:postgresql://your_host:your_port/your_eva_db</eva.evapro.jdbc.url>
                <eva.evapro.user></eva.evapro.user>
                <eva.evapro.password></eva.evapro.password>
                <eva.mongo.host></eva.mongo.host>
                <eva.mongo.user></eva.mongo.user>
                <eva.mongo.passwd></eva.mongo.passwd>
                <eva.mongo.auth.db></eva.mongo.auth.db>
                <eva.mongo.read-preference></eva.mongo.read-preference>
                <eva.mongo.collections.annotation-metadata></eva.mongo.collections.annotation-metadata>
                <eva.mongo.collections.annotations></eva.mongo.collections.annotations>
                <eva.mongo.collections.features></eva.mongo.collections.features>
                <eva.mongo.collections.files></eva.mongo.collections.files>
                <eva.mongo.collections.variants></eva.mongo.collections.variants>
            </properties>
        </profile>
    </profiles>

    <servers>
        <server>
            <id>maven.oracle.com</id>
            <username>your_oracle_user</username>
            <password>your_oracle_encrypted_password</password>
            <configuration>
              <basicAuthScope>
                <host>ANY</host>
                <port>ANY</port>
                <realm>OAM 11g</realm>
              </basicAuthScope>
              <httpConfiguration>
                <all>
                  <params>
                    <property>
                      <name>http.protocol.allow-circular-redirects</name>
                      <value>%b,true</value>
                    </property>
                  </params>
                </all>
              </httpConfiguration>
            </configuration>
      </server>
   </servers>
</settings>
```

After setting up your Maven security and Maven settings, you can install the whole project and run the tests using the above "production" profile with `mvn clean install -Pproduction`. This will generate the ".war" files that you can deploy in Tomcat or other Java container. You have to set up the JNDI credentials in Tomcat (conf/context.xml), matching the "eva.evapro.datasource" and "eva.evapro.jdbc.url" properties.

If you don't need to build dgva-server, you can build the ".war" files for the rest of the modules running `mvn clean install -pl '!dgva-server'`.


Testing
-------

The tests implemented are both unit and integration tests. You can run them with `mvn test` from the root folder.

If you are compiling and want to skip the tests, you can do `mvn clean install -Pproduction -DskipTests`.

For manual testing, you can deploy the ".war" files and go to the Swagger page to get an overview of the endpoints and run them manually. If you name the artifact "eva.war" and deploy it locally, the Swagger URL is "localhost:8080/eva/swagger-ui.html".

Enabling OAuth2 Security
------------------------

In order to enable OAuth2 Security you must enable the `oauth2-security` profile in the spring-boot application in any way supported by Spring-boot as stated in [Spring documentation](http://docs.spring.io/spring-boot/docs/current/reference/html/howto-properties-and-configuration.html#howto-set-active-spring-profiles). In adition, the application requires configuration to point to the public user check endpoints of your OAuth2 compatible authentication service. More information is available in [Spring documentation](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-security.html#boot-features-security-oauth2-resource-server).

When using OAuth2 Security, Swagger UI will be still enabled and public to review the API but no command sent from the UI will be authorized.
