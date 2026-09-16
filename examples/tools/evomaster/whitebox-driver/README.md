# API Challenges EvoMaster White-Box Driver

This is a small EvoMaster external controller for running white-box experiments against the packaged API Challenges jar.

The driver starts the existing jar in a separate JVM and lets EvoMaster add its Java agent to that process. This gives EvoMaster coverage feedback while keeping the example outside the main application code.

## Build

From this folder:

```shell
mvn compile
```

## Start The Driver

```shell
mvn exec:java
```

By default the driver looks for:

```text
../../../../challenger/target/apichallenges.jar
```

Override the jar path or port with system properties:

```shell
mvn exec:java -Dapichallenges.jar=D:\github\apichallenges\challenger\target\apichallenges.jar -Dapichallenges.port=4568
```

The driver serves the EvoMaster controller on port `40100` by default. Override it with:

```shell
mvn exec:java -Devomaster.controller.port=40101
```

## Run EvoMaster

Start the driver first, then run EvoMaster:

```shell
java -jar D:\temp\evomaster-apichallenges-eval\evomaster.jar --blackBox false --sutControllerPort 40100 --maxTime 2m --outputFolder D:\temp\evomaster-apichallenges-eval\generated-api-challenges-whitebox --outputFormat JAVA_JUNIT_5
```

The driver exposes the strong, operation-parameter OpenAPI URL:

```text
/api/docs/openapi-3.0.json?strongschema=true&pathparams=operation
```

It also provides EvoMaster with a fixed `X-CHALLENGER` header using the single-player session value.

## Reset Behavior

The default reset hook is intentionally a no-op because API Challenges starts with useful sample todo data.

For experiments that need a blank database between tests, start the driver with:

```shell
mvn exec:java -Dapichallenges.resetWithAdminClear=true
```

That adds `-enableadminapi` when starting the jar and calls Thingifier's admin clear endpoint from `resetStateOfSUT()`.
