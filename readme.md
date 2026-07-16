# API Challenges

API Challenges is a self-teaching API practice application.

This repository contains the runnable API Challenges application and its
automated acceptance test suite:

- `challenger` builds the deployable `apichallenges.jar`
- `challengerAuto` runs the API-level challenge regression suite

Thingifier remains in the separate
[`eviltester/thingifier`](https://github.com/eviltester/thingifier) repository.
Until Thingifier is published as a normal Maven artifact, CI checks out
Thingifier and installs its library artifacts into the Maven cache before
building this repository.

## Build

Install Thingifier locally first:

```shell
git clone https://github.com/eviltester/thingifier.git ../thingifier
mvn -B -f ../thingifier/pom.xml -pl ercoremodel,thingifier -am install \
  -DskipTests \
  -Dspotless.check.skip=true \
  -Dcheckstyle.skip=true \
  -Dpmd.skip=true
```

Then build API Challenges:

```shell
mvn -B clean test
mvn -B -pl challenger -am package
```

The deployable app is:

```text
challenger/target/apichallenges.jar
```

Run it with:

```shell
java -jar challenger/target/apichallenges.jar
```

Then visit:

- <http://localhost:4567>
- <http://localhost:4567/challenges>
- <http://localhost:4567/docs>
- <http://localhost:4567/docs/swagger-ui>

## Challenger Auto

Run the full local regression suite:

```shell
mvn -B -pl challengerAuto -am test
```

Run specific repository modes:

```shell
mvn -B -pl challengerAuto -am test \
  -Dchallenger.auto.target=local \
  -Dchallenger.auto.local.repository=memory \
  -Dchallenger.auto.local.playerMode=multi

mvn -B -pl challengerAuto -am test \
  -Dchallenger.auto.target=local \
  -Dchallenger.auto.local.repository=sqlite-memory \
  -Dchallenger.auto.local.playerMode=multi
```

## Docker

Build the jar first, then build the image:

```shell
mvn -B -pl challenger -am package
docker build -t eviltester/apichallenges -f docker/apichallenges/Dockerfile .
```

Run the image:

```shell
docker run --rm -p 4567:4567 eviltester/apichallenges
```

The GitHub Actions Docker workflow builds and smoke-tests the image. On `master`,
`main`, and version tags it can push to Docker Hub when `DOCKERHUB_USERNAME` and
`DOCKERHUB_TOKEN` repository secrets are configured.

## Code Formatting

Java source formatting is enforced with Spotless and google-java-format using
the AOSP four-space style.

Check formatting:

```shell
mvn spotless:check
```

Apply formatting:

```shell
mvn spotless:apply
```
