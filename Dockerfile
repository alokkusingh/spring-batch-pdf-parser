FROM arm64v8/openjdk:17
MAINTAINER Alok Singh (alok.ku.singh@gmail.com)
RUN groupadd -g 600 singh && useradd -u 601 -g 600 alok
ARG JAR_FILE
RUN mkdir -p /opt/logs
RUN mkdir -p /home/alok
RUN chown -R alok /opt
RUN chown -R alok /home/alok
USER alok
COPY src/main/resources/keystore.jks /home/alok/keystore.jks
COPY src/main/resources/truststore.jks /home/alok/trustore.jks
COPY ${JAR_FILE} /opt/app.jar
EXPOSE 8081
WORKDIR /opt
# Removed sprint porfiles from here, isnted it will be set using configMap in Kueberenetes
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-Dspring.profiles.active=prod,mqtt","-jar","/opt/app.jar"]
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","/opt/app.jar"]
