FROM arm64v8/openjdk:17
MAINTAINER Alok Singh (alok.ku.singh@gmail.com)
RUN groupadd -g 600 singh && useradd -u 601 -g 600 alok
ARG JAR_FILE
RUN mkdir -p /opt/logs
RUN mkdir -p /home/alok
RUN chown -R alok /opt
RUN chown -R alok /home/alok
USER alok
COPY ${JAR_FILE} /opt/app.jar
EXPOSE 8081
WORKDIR /opt
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-Dspring.profiles.active=prod","-jar","/opt/app.jar"]