FROM adoptopenjdk/openjdk8:jre8u252-b09-alpine
MAINTAINER Pau Casas Prunés <admin@code.parts>
EXPOSE 5052
COPY build/distributions/query-service.zip /opt/
RUN unzip /opt/query-service.zip -d /opt
RUN apk update && apk add bash
