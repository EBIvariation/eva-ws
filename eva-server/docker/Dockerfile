FROM tomcat:9.0.5-jre8-alpine
ADD eva.war /usr/local/tomcat/webapps/
ADD tomcat-users.xml /usr/local/tomcat/conf/
ADD context.xml /usr/local/tomcat/conf/
ADD manager.xml /usr/local/tomcat/conf/Catalina/localhost/

EXPOSE 8080
CMD ["catalina.sh", "run"]
