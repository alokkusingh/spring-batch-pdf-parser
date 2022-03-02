FROM arm64v8/openjdk:8u322-jre
MAINTAINER Alok Singh (alok.ku.singh@gmail.com)
RUN groupadd -r singh && useradd --no-log-init -r -g singh alok
ARG JAR_FILE
COPY ${JAR_FILE} /opt/app.jar
#RUN chown -R alok:singh /Users
#RUN chown -R alok:singh /opt
#RUN chown -R alok:singh /opt/logs
#RUN chown -R alok:singh /home/alok
USER alok:singh
VOLUME /opt/logs
VOLUME /home/alok
VOLUME /Users/aloksingh/BankStatements
EXPOSE 8081
WORKDIR /opt
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","/opt/app.jar"]
#ENTRYPOINT ["/usr/bin/tail "]
