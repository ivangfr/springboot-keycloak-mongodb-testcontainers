FROM openjdk:8-jre-alpine

LABEL maintainer="ivangfr@yahoo.com.br"

RUN apk --no-cache add curl jq

ARG JAR_FILE
ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

EXPOSE 8080