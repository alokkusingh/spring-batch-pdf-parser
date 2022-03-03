FROM arm64v8/openjdk:8u322-jre
MAINTAINER Alok Singh (alok.ku.singh@gmail.com)
#RUN groupadd -r singh && useradd --no-log-init -r -g singh alok
RUN useradd -u $UID -ms /bin/bash $USER
ARG JAR_FILE
COPY ${JAR_FILE} /opt/app.jar
RUN mkdir -p /opt/logs
RUN mkdir -p /home/alok
RUN mkdir -p /Users/aloksingh/BankStatements
RUN chown -R alok /Users
RUN chown -R alok /opt
RUN chown -R alok /opt/logs
RUN chown -R alok /home/alok
USER alok
VOLUME /opt/logs
VOLUME /home/alok
VOLUME /Users/aloksingh/BankStatements
EXPOSE 8081
WORKDIR /opt
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","/opt/app.jar"]
#ENTRYPOINT ["/usr/bin/tail "]
