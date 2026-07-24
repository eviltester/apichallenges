# syntax=docker/dockerfile:1

# Builds the multiplayer/cloud-storage API Challenges image directly from this
# split repository. The app still depends on Thingifier as a source-built Maven
# dependency, so the build stage installs the required Thingifier modules first.

FROM maven:3.9.9-eclipse-temurin-21 AS build

ARG APICHALLENGES_THINGIFIER_REF=master
# Railway can reuse Docker layers aggressively. Bump this value when the
# source-built Thingifier dependency must be refreshed from the same ref.
ARG APICHALLENGES_THINGIFIER_CACHE_BUST=20260724-thingifier-c2f638dd

WORKDIR /workspace

RUN apt-get update \
        && apt-get install -y --no-install-recommends git ca-certificates \
        && rm -rf /var/lib/apt/lists/*

RUN echo "Thingifier ref: ${APICHALLENGES_THINGIFIER_REF}" \
        && echo "Thingifier cache bust: ${APICHALLENGES_THINGIFIER_CACHE_BUST}" \
        && git clone --depth 1 --branch ${APICHALLENGES_THINGIFIER_REF} https://github.com/eviltester/thingifier.git thingifier \
        && git -C thingifier log -1 --oneline

COPY . apichallenges

RUN mvn -B -f thingifier/pom.xml -pl ercoremodel,thingifier -am install \
        -DskipTests \
        -Dspotless.check.skip=true \
        -Dcheckstyle.skip=true \
        -Dpmd.skip=true

RUN mvn -B -f apichallenges/pom.xml -pl challenger -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /opt/app

COPY --from=build /workspace/apichallenges/challenger/target/apichallenges.jar /opt/app/apichallenges.jar

EXPOSE 4567

ENTRYPOINT ["java", "-jar", "/opt/app/apichallenges.jar"]
CMD ["-model=challengeapi", "-noshutdown", "-multiplayer", "-cloudstorage"]
