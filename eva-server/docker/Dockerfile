FROM tomcat:9.0.5-jre8-alpine
ADD eva.war /usr/local/tomcat/webapps/
ADD tomcat-users.xml /usr/local/tomcat/conf/
ADD context.xml /usr/local/tomcat/conf/
ADD manager.xml /usr/local/tomcat/conf/Catalina/localhost/
RUN chgrp -R 0 /usr/local/tomcat/conf && chmod -R g=u /usr/local/tomcat/conf
RUN chgrp -R 0 /usr/local/tomcat/logs && chmod -R g=u /usr/local/tomcat/logs
RUN chgrp -R 0 /usr/local/tomcat/webapps && chmod -R g=u /usr/local/tomcat/webapps
RUN chgrp -R 0 /usr/local/tomcat/temp && chmod -R g=u /usr/local/tomcat/temp

EXPOSE 8080
CMD ["catalina.sh", "run"]
