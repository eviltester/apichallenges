# EvoMaster Examples

This folder contains helper code used during EvoMaster experiments with API Challenges:

- `convert-openapi-for-evomaster.mjs` converts OpenAPI files into a shape that EvoMaster handles more effectively in black-box mode.
- `whitebox-driver` is a small JVM EvoMaster driver that wraps the existing packaged API Challenges jar for white-box testing.

## OpenAPI Converter

EvoMaster works best when the OpenAPI file is explicit enough for automated request generation. In our experiments that meant:

- path parameters copied into each operation
- stronger schema types inferred from examples and descriptions
- GUID-like values kept as `type: string` by default, without `format: uuid`

The converter script reads an OpenAPI JSON file and writes a converted JSON file.

### Usage

```shell
node convert-openapi-for-evomaster.mjs input-openapi.json evomaster-openapi.json
```

Useful options:

```shell
node convert-openapi-for-evomaster.mjs input.json output.json --keep-path-level
node convert-openapi-for-evomaster.mjs input.json output.json --no-strong-schemas
node convert-openapi-for-evomaster.mjs input.json output.json --add-uuid-format
```

By default, the script:

- copies path-level parameters to each operation
- removes the original path-level parameter block
- infers missing schema types where it can
- removes or avoids adding `format: uuid`

The `--add-uuid-format` option is available because UUID formats are valid OpenAPI, but EvoMaster warned about `format: uuid` during our experiments. For an EvoMaster-preferred file, leaving GUID-like values as plain strings produced less noise.

## Running EvoMaster

Example shape of an EvoMaster run:

```shell
java -jar evomaster.jar \
  --problemType REST \
  --schema evomaster-openapi.json \
  --base http://localhost:4567 \
  --header0 "X-CHALLENGER:rest-api-challenges-single-player" \
  --maxTime 2m \
  --outputFolder generated-api-challenges \
  --outputFormat PYTHON_UNITTEST
```

For current API Challenges builds, try the generated OpenAPI first:

```text
http://localhost:4567/api/docs/openapi-3.0.json?strongschema=true&pathparams=operation
```

Use this script when you have an OpenAPI file that still uses shared path parameters or weak schemas.

## White-Box Driver

The `whitebox-driver` folder contains an EvoMaster `ExternalSutController` for the existing packaged API Challenges jar:

```text
challenger/target/apichallenges.jar
```

The driver starts that jar in a separate JVM and lets EvoMaster add its Java agent to the process. This means EvoMaster can use bytecode coverage feedback while it fuzzes the REST API, without moving the driver into the main application code.

By default the driver:

- starts API Challenges on port `4567`
- uses `-memory`, `-sim-sqlite-memory`, and `-noshutdown`
- points EvoMaster at `/api/docs/openapi-3.0.json?strongschema=true&pathparams=operation`
- provides the single-player `X-CHALLENGER` header value
- exposes the EvoMaster controller on port `40100`

Build the packaged API Challenges jar first, then build the driver:

```shell
cd examples/tools/evomaster/whitebox-driver
mvn compile
```

Start the driver in one terminal:

```shell
mvn exec:java
```

If another API Challenges instance is already using port `4567`, choose another SUT port:

```shell
mvn exec:java "-Dapichallenges.port=4568"
```

The jar path can also be supplied explicitly:

```shell
mvn exec:java "-Dapichallenges.jar=D:\github\apichallenges\challenger\target\apichallenges.jar" "-Dapichallenges.port=4568"
```

Then run EvoMaster in another terminal:

```shell
java -jar D:\temp\evomaster-apichallenges-eval\evomaster.jar ^
  --blackBox false ^
  --sutControllerPort 40100 ^
  --maxTime 2m ^
  --outputFolder D:\temp\evomaster-apichallenges-eval\generated-api-challenges-whitebox ^
  --outputFormat JAVA_JUNIT_5
```

During the first white-box experiment, EvoMaster successfully instrumented the packaged jar and reported bytecode coverage:

```text
Potential faults:             2
Generated tests:              127
Evaluated HTTP calls:         2257
Schema endpoints with 2xx:    19 / 31
Covered targets:              33409
Line coverage:                14385 / 29116, 49%
Branch coverage:              3681 / 14066
```

The two reported faults were the same conservative XSS classification seen in black-box runs: API Challenges accepted and echoed an HTML-looking string in JSON. This is worth reviewing in UI rendering contexts, but it is not automatically proof of browser-executable XSS.

### Reset Behavior

The default driver reset is a no-op because API Challenges starts with useful sample todo data. This is good enough for exploration, but it means EvoMaster can still see stateful behavior while replaying and minimizing tests.

For experiments that need a blank Thingifier database between tests, start the driver with:

```shell
mvn exec:java "-Dapichallenges.resetWithAdminClear=true"
```

That starts the jar with `-enableadminapi` and calls Thingifier's admin clear endpoint from `resetStateOfSUT()`. A stronger future version would reset the app back to its original seeded data rather than simply clearing data.
