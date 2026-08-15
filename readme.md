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
- <http://localhost:4567/api/challenges>
- <http://localhost:4567/api/docs>
- <http://localhost:4567/api/docs/swagger-ui>

## Challenger Auto

Run the full local regression suite:

```shell
mvn -B -pl challengerAuto -am test
```

Run specific repository modes:

Set `challenger.auto.apiRouteMode` to `api` for canonical `/api/...` routes, or `legacy`
for root compatibility routes.

```shell
mvn -B -pl challengerAuto -am test \
  -Dchallenger.auto.target=local \
  -Dchallenger.auto.local.repository=memory \
  -Dchallenger.auto.local.playerMode=multi \
  -Dchallenger.auto.apiRouteMode=api

mvn -B -pl challengerAuto -am test \
  -Dchallenger.auto.target=local \
  -Dchallenger.auto.local.repository=sqlite-memory \
  -Dchallenger.auto.local.playerMode=multi \
  -Dchallenger.auto.apiRouteMode=legacy
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

## Railway S3 Persistence

API Challenges can persist multiplayer challenger progress and per-session todo
data to a private Railway Storage Bucket using the S3-compatible API. The Docker
image starts with `-cloudstorage`; when running the jar directly, add that
argument yourself:

```shell
java -jar challenger/target/apichallenges.jar -model=challengeapi -noshutdown -multiplayer -cloudstorage
```

To enable persistence on Railway:

1. Create a Railway Storage Bucket in the same project and environment as the
   API Challenges service.
2. Connect the bucket credentials to the API Challenges service using Railway
   variable references or automatic provisioning.
3. Set these API Challenges service variables:

```text
AWS_ALLOW_SAVE=true
AWS_ALLOW_LOAD=true
```

Railway buckets expose raw S3 variables named `BUCKET`, `ENDPOINT`, `REGION`,
`ACCESS_KEY_ID`, and `SECRET_ACCESS_KEY`. API Challenges can use those directly.
It also supports the AWS SDK-style names below if you prefer to map variables
manually:

```text
AWS_S3_BUCKET_NAME=${{Bucket.BUCKET}}
AWS_ENDPOINT_URL=${{Bucket.ENDPOINT}}
AWS_DEFAULT_REGION=${{Bucket.REGION}}
AWS_ACCESS_KEY_ID=${{Bucket.ACCESS_KEY_ID}}
AWS_SECRET_ACCESS_KEY=${{Bucket.SECRET_ACCESS_KEY}}
```

Use `BUCKET` for the S3 bucket name. Railway also exposes
`RAILWAY_BUCKET_NAME`, but that is the display name, not the S3 API bucket name.

Railway buckets normally use virtual-hosted-style S3 URLs. If the bucket
credentials say path-style URLs are required, set:

```text
AWS_S3_URL_STYLE=path
```

No public bucket configuration is needed; API Challenges only needs server-side
authenticated S3 access.

S3 saving is delayed until a challenger has completed 10 unique challenges. This
keeps throwaway challenger creation in memory only. The threshold can be tuned:

```text
API_CHALLENGES_S3_SAVE_AFTER_COMPLETED_CHALLENGES=10
```

Once the threshold is reached, normal autosave resumes and writes three objects
under the managed prefix:

```text
apichallenges/sessions/{guid}.data.txt
apichallenges/sessions/{guid}.content.txt
apichallenges/sessions/{guid}.activity.txt
```

Cleanup is app-managed because Railway Storage Buckets do not currently support
bucket lifecycle configuration. Defaults:

```text
API_CHALLENGES_S3_CLEANUP_ENABLED=true
API_CHALLENGES_S3_RETENTION_DAYS=7
API_CHALLENGES_S3_CLEANUP_INTERVAL_HOURS=24
API_CHALLENGES_S3_ACTIVITY_MARKER_INTERVAL_HOURS=24
API_CHALLENGES_S3_CLEANUP_SINGLE_PLAYER_ENABLED=false
```

Cleanup scans only the managed prefix, deletes sessions unused for the retention
window, skips currently loaded in-memory sessions, and skips the single-player
session unless explicitly enabled. Railway S3 API operations are currently
unlimited/free; storage is billed per GB-month.

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

Install the local Git hooks so formatting and static-analysis failures are
caught before CI:

```shell
git config core.hooksPath .githooks
```

On macOS or Linux, make the hooks executable if needed:

```shell
chmod +x .githooks/pre-commit
chmod +x .githooks/pre-push
```

The pre-commit hook runs the CI Java 21 static-analysis build:

```shell
mvn -B -DskipTests verify
```

The pre-push hook runs the CI Spotless check. Set `JAVA_HOME` or
`JAVA_HOME_21` to a JDK 21 install before committing or pushing.
