# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jre

WORKDIR /opt/app
COPY challenger/target/apichallenges.jar /opt/app/apichallenges.jar

EXPOSE 4567

ENTRYPOINT ["java", "-jar", "/opt/app/apichallenges.jar"]
CMD ["-model=challengeapi", "-noshutdown", "-multiplayer"]
