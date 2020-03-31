# syntax=docker/dockerfile:experimental

FROM node:8 as front
RUN git clone https://github.com/smartcommunitylab/resourcemanager-admin.git
WORKDIR resourcemanager-admin
RUN npm install && npm run dist

FROM maven:3-jdk-8 as mvn
COPY src /tmp/src
COPY pom.xml /tmp/pom.xml
COPY --from=front /resourcemanager-admin/dist/* /tmp/src/main/resources/public/
WORKDIR /tmp
RUN --mount=type=bind,target=/root/.m2,source=/root/.m2,from=smartcommunitylab/resourcemanager:cache-alpine mvn package -DskipTests

FROM adoptopenjdk/openjdk8:alpine
ARG VER=0.0.1-SNAPSHOT
ARG USER=rsmanager
ARG USER_ID=810
ARG USER_GROUP=rsmanager
ARG USER_GROUP_ID=810
ARG USER_HOME=/home/${USER}
ENV FOLDER=/tmp/target
ENV APP=resourcemanager-${VER}.jar
# create a user group and a user
RUN  addgroup -g ${USER_GROUP_ID} ${USER_GROUP}; \
     adduser -u ${USER_ID} -D -g '' -h ${USER_HOME} -G ${USER_GROUP} ${USER} ;

WORKDIR ${USER_HOME}
COPY --chown=rsmanager:rsmanager --from=mvn /tmp/target/${APP} ${USER_HOME}
USER rsmanager
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar ${APP}"]
