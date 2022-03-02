FROM arm64v8/openjdk:8u322-jre
MAINTAINER Alok Singh (alok.ku.singh@gmail.com)
RUN groupadd -r singh && useradd --no-log-init -r -g singh alok
USER alok:singh
VOLUME /Users/aloksingh/BankStatements
VOLUME /Users/aloksingh/logs
EXPOSE 8081
ARG JAR_FILE
COPY ${JAR_FILE} /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","/app.jar"]