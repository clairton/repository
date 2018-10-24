FROM maven:3-jdk-8-alpine

RUN apk add openssl gnupg --no-cache --update

VOLUME /root/.m2

RUN mkdir /app
WORKDIR /app
VOLUME /app