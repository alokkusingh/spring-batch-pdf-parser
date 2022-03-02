FROM arm64v8/openjdk:8u322-jre
MAINTAINER Alok Singh (alok.ku.singh@gmail.com)
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/urandom","-jar","/app.jar"]