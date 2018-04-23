FROM openjdk:8-jdk-alpine

LABEL maintainer="ivangfr@yahoo.com.br"

RUN apk update && apk add ca-certificates wget && update-ca-certificates && apk add --no-cache curl jq

ARG JAR_FILE

ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

EXPOSE 8080