FROM maven:3.6-jdk-11-slim as BUILD
COPY . /src
WORKDIR /src
RUN mvn install -DskipTests

FROM openjdk:11.0.1-jre-slim-stretch
EXPOSE 8080
WORKDIR /app
ARG JAR=library-0.0.1-SNAPSHOT.jar

COPY --from=BUILD /src/target/$JAR /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
