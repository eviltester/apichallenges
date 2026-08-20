FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY implementations/java-raw-http ./implementations/java-raw-http
RUN mvn -B -f implementations/java-raw-http/pom.xml clean package dependency:copy-dependencies

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY catalog ./catalog
COPY --from=build /workspace/implementations/java-raw-http/target/classes ./classes
COPY --from=build /workspace/implementations/java-raw-http/target/dependency ./dependency

ENV PORT=3001
ENV FROMHELL_CATALOG=/app/catalog/fromhell-catalog.json
EXPOSE 3001

CMD ["java", "-cp", "classes:dependency/*", "dev.eviltester.fromhell.ApiFromHellRawHttpMain"]
