FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY robodepot/pom.xml robodepot/pom.xml
RUN mvn -pl robodepot -am -DskipTests dependency:go-offline

COPY robodepot robodepot
RUN mvn -pl robodepot -am -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/robodepot/target/robodepot.jar /app/robodepot.jar

EXPOSE 4567
ENTRYPOINT ["java", "-jar", "/app/robodepot.jar"]
CMD ["-port=4567"]
