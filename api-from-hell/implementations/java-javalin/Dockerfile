FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY implementations/java-javalin ./implementations/java-javalin
RUN mvn -B -f implementations/java-javalin/pom.xml package dependency:copy-dependencies

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY catalog ./catalog
COPY --from=build /workspace/implementations/java-javalin/target/classes ./classes
COPY --from=build /workspace/implementations/java-javalin/target/dependency ./dependency

ENV PORT=3001
ENV FROMHELL_CATALOG=/app/catalog/fromhell-catalog.json
EXPOSE 3001

CMD ["java", "-cp", "classes:dependency/*", "dev.eviltester.fromhell.ApiFromHellJavalinMain"]
