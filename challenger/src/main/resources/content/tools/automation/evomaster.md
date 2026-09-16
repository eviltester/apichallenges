---
title: EvoMaster API Fuzzer - Overview and Review
seo_title: EvoMaster API Fuzzer Review for OpenAPI Based API Testing
description: A practical review of EvoMaster as an API automation and fuzzing tool, including setup notes, OpenAPI requirements, and findings from API Challenges.
lastmod: 2026-09-16
seo_description: Review EvoMaster for REST API fuzzing and OpenAPI based test generation, including setup experience, useful findings, false positives, and API Challenges lessons learned.
showads: true
---

# EvoMaster

EvoMaster is an open source API automation and fuzzing tool.

It is not a REST client. It is not an online client. It is not a proxy.

It is an OpenAPI specification file driven automation which parses and covers the spec in various ways without human intervention. It calls the API for us, generates many test cases, and writes out executable scripts and reports.

EvoMaster can be found at:

- [EvoMaster on GitHub](https://github.com/WebFuzzing/EvoMaster)

## How EvoMaster works

EvoMaster reads an API description, such as an OpenAPI file, then generates and runs HTTP requests against the target API.

It can work in black-box mode from an OpenAPI file. We used an OpenAPI file to evaluate EvoMaster with API Challenges.

For REST APIs, EvoMaster needs an OpenAPI or Swagger schema. The schema can be downloaded from a URL, loaded from a local file, or supplied with a `file://` URL. EvoMaster supports OpenAPI/Swagger versions 2.0, 3.0 and 3.1. At the time of writing, OpenAPI 3.2 support is on hold because of parser support.

EvoMaster can also work with other API styles:

- GraphQL APIs can be tested by using `--problemType GRAPHQL` and setting `--base` to the GraphQL endpoint which responds with schema introspection queries.
- RPC APIs are supported, including gRPC and Thrift, but RPC fuzzing currently requires writing an EvoMaster driver that uses the API client library to make the calls.
- JVM APIs, such as Java or Kotlin applications, can be tested in white-box mode with an EvoMaster driver. This gives EvoMaster access to bytecode analysis and usually improves coverage.
- Postman collections can be used to seed initial test cases, but the coverage model is not the API Spec, it is the set of endpoints and requests you have modelled in Postman.

The tool tries to find interesting behaviours by varying paths, parameters, headers, payloads, content types, and sequences of calls. It then writes:

- generated tests
- an HTML report
- a JSON report
- helper scripts for viewing the report

In our experiments the generated tests were Python `unittest` files using `requests`, which made them easy to inspect and rerun.

## Installing And Running EvoMaster

For the API Challenges experiments I downloaded the EvoMaster jar and ran it with Java.

The basic trigger command was:

```shell
java -jar evomaster.jar \
  --problemType REST \
  --schema apichallenges-openapi.json \
  --base http://localhost:4567 \
  --header0 "X-CHALLENGER:5afe0000-5eed-4000-8000-000000defa17" \
  --maxTime 2m \
  --ratePerMinute 120 \
  --outputFolder generated-api-challenges \
  --outputFormat PYTHON_UNITTEST
```

The `X-CHALLENGER` header matters for API Challenges because the application tracks challenge completion against a challenger session. But this also demonstrates that you can configure whatever headers you need for EvoMaster to work against your API.


## OpenAPI Files Matter

EvoMaster worked best with an OpenAPI file that was explicit and operational.

By "operational" I mean that the path parameters are defined on each operation, rather than shared at the path level.

For example, this path-level style is valid OpenAPI:

```json
{
  "/api/todos/{id}": {
    "parameters": [
      {
        "name": "id",
        "in": "path",
        "required": true,
        "schema": {
          "type": "integer"
        }
      }
    ],
    "get": {
      "responses": {
        "200": {
          "description": "OK"
        }
      }
    }
  }
}
```

But EvoMaster gave better results when the parameter was copied into the operation:

```json
{
  "/api/todos/{id}": {
    "get": {
      "parameters": [
        {
          "name": "id",
          "in": "path",
          "required": true,
          "schema": {
            "type": "integer"
          }
        }
      ],
      "responses": {
        "200": {
          "description": "OK"
        }
      }
    }
  }
}
```

As a result, API Challenges now supports generating stronger, and more EvoMaster-friendly operational format OpenAPI files, e.g.

```text
/api/docs/openapi-3.0.json?strongschema=true&pathparams=operation
```

Before that features was available, I created a small converter script for experiments. The script copies shared path parameters into each operation and can infer stronger schema types from examples and descriptions.

The example script is in:

- `examples/tools/evomaster/convert-openapi-for-evomaster.mjs` [on github](https://github.com/eviltester/apichallenges/tree/main/examples/tools/evomaster)

The default example script behaviour removes or avoids adding `format: uuid` because EvoMaster warned about UUID formats in our experiments. It keeps GUID-like values as `type: string` unless `--add-uuid-format` is used.

## Experience Setting It Up

The setup was straightforward once I had the OpenAPI file in the right shape.

The early runs were useful, but noisy. EvoMaster warned about:

- path-level parameters
- missing schema types
- `format: uuid`
- undocumented response statuses
- response bodies that were not described by OpenAPI

The path-level parameter approach in the OpenAPI file meant that EvoMaster generated less coverage of the API endpoints. Coverage improved when path parameters were copied into each operation.

Strong schemas also helped. When the OpenAPI spec described `doneStatus` as a boolean and `id` as an integer, EvoMaster no longer had to guess those values as strings.

The `format: uuid` warning was still useful because it showed the difference between a generally strong schema and an EvoMaster-preferred schema. For EvoMaster, a GUID-like field might work better as:

```json
{
  "type": "string"
}
```

rather than:

```json
{
  "type": "string",
  "format": "uuid"
}
```

So you might have to tweak your OpenAPI spec file to get the most out of EvoMaster.

## What EvoMaster Found In API Challenges

EvoMaster found a mix of API behaviour issues, OpenAPI contract issues, and tool-noise that needed human review.

Useful findings included:

- OpenAPI path parameters were better when generated on each operation
- schemas needed stronger property types
- some error responses returned JSON-looking content as `text/plain`
- some challenger database responses returned `text/html` where the OpenAPI expected JSON
- some documented response schemas did not match empty response bodies
- `format: uuid` did not work well with EvoMaster for the API Challenges challenger id
- todo title and description values could echo HTML-looking strings in JSON

Basically - not everything the API provided was modelled in the OpenAPI file.

Some of these were actually bugs, but I ran the tool initially against an old version to check. I'd already fixed most of these bugs due to working with [API Spector](/tools/clients/api-spector) and the API Spector fuzzer.

But, EvoMaster found some `text/plain` issues and a `Location` header that I wanted to fix.

The XSS-labelled findings need careful interpretation.

```text
<img src=x onerror=alert('XSS')>
```

 EvoMaster reported reflected values like the above as XSS because the API echoed the string. In the API response this was JSON, not rendered HTML. That was still a useful warning, but it is not automatically proof of exploitable browser XSS. It prompted us to check where that value might later be rendered - I did have to double check the UI Data Explorer and it was fine.

## False Positives And Noisy Findings

Some findings were not application bugs but required me to amend the OpenAPI spec to better model the API.

For example:

- missing or unexpected response bodies that were not well documentated
- undocumented status codes that the OpenAPI had not described yet
- the XSS report because the value was echoed in JSON


I did find that EvoMaster, because it was triggering many endpoints actually created and cleaned up data before manual inspection so I had to work a little harder to investigate some reported issues.

The fastest way to use the results was:

1. Open the HTML report.
2. Check the generated Python test for the request and payload.
3. Reproduce the request manually if needed.
4. Decide whether the issue was API behaviour, OpenAPI documentation, or tool noise.

## Using AI in Combination with EvoMaster

I used GPT-5.5 to perform the initial run of EvoMaster (and create the conversion script).

The docs on the github page were enough for Codex to try Docker first, then use the jar file, and run against the API.

Also the output reports are provided in multiple formats and the AI can easily read, parse and summarize these.

I did have to supply my ID to OpenAI to gain "Daybreak Blue" access to allow Chat GPT to report on and investigate the security reports.

I was able to use OpenCode and MiMo V2.5 Free to read and summarize the security reports without any guards or identification.

The fact that it is a well documented tool with a CLI interface makes it perfect to use in combination with an AI for interactive investigation of your API.

## Iterating The API And OpenAPI

EvoMaster was very useful for iterating API Challenges itself.

The first runs highlighted generated OpenAPI weaknesses. We then improved the OpenAPI generation so that tools could request:

- stronger schemas
- operation-level path parameters

Because EvoMaster covers every endpoint in your schema I ran the API Challenges in no-shutdown mode but I could have excluded the shutdown endpoint from the command line configuration:

e.g.

```
--endpointExclude "/api/shutdown,/api/admin"
```

It is also possible to target a specifc subset of endpoints with `--endpointFocus` or `--endpointTagFilter`



Earlier runs produced path-scope warnings but when the OpenAPi spec used operation style spec format the tool coverage and output improved.

```text
/api/docs/openapi-3.0.json?strongschema=true&pathparams=operation
```

had:

- no path-scope warnings
- no missing operation path parameters
- better endpoint coverage

Three or Four iterations of running EvoMaster then amending the OpenAPI spec did result in an improved OpenAPI file and tightened up the actual API behaviour.

The second to last local run generated 77 tests, covered 234 targets, executed 21 of 31 endpoints with 2xx responses, and reduced the reported potential faults to 7.

The (what I thought was the) last run generated 93 tests, covered 270 targets, executed 21 of 31 endpoints with 2xx responses, and reported 3 potential faults. It also evaluated 17,593 HTTP calls. The 93 tests are the HTTP calls and responses that it thought were interesting enough to report.

- the XSS issue
- a 500 error on some endpoints

I was tempted to think the 500 status might well have been due to 'load' but they did reveal a subtle bug in my underlying framework which caused Jetty to throw an exception during response processing. This is a good example of how fuzzing can help because it could easily have been shown in live running logs but missed unless appropriate monitors have been setup on your logs.

Target count varies between runs because EvoMaster is stochastic: it mutates requests, sequences calls, creates data, deletes data, and discovers new reachable behaviour as it runs. A slightly different generated sequence can expose extra statuses, response shapes, validation paths, or stateful behaviour, which then become covered targets.

EvoMaster basically runs for the time you tell it to, and it randomly generates as many requests during that time as it can. `--maxTime` sets a search budget that it will consume.

```
--maxTime 2m
```

A 'final'  minute run found only the XSS false positive after 19,048 calls.

You can configure the EvoMaster to run for hours and stop after various stopping criteria if you want to do more complex fuzzing runs.

## WhiteBox Fuzzing

If your API is JVM based then you can use EvoMaster as a driver for the application and it can instrument your code as it fuzzes, this allows it to explore 'code coverage' and other bytecode derived information during the fuzz process.

I created an example in the apichallenges repo

- `examples/tools/evomaster/` [on github](https://github.com/eviltester/apichallenges/tree/main/examples/tools/evomaster)

This uses the external driver capabilities and was a little slower in 2 minutes than the HTTP tests but provided a different insight.

The EvoMaster documentation recommends starting with 10 minute runs and then moving to 24 hours for most benefits.

I haven't pushed the fuzzing for this length yet but I did get a lot of value from the 2 minute runs I've tried.

I upped the white box execution to 20 minutes and it found a few extra issues relating to OpenAPI contracts. So there may well be some additional benefit in running it longer.

## Reading The Report

The output folder contains:

- `EvoMaster_Test.py`
- `report.json`
- `index.html`
- `low-code-index.html`
- `webreport.py`
- `webreport.bat`

All of these were generated as output from the tool run.

The generated Python test file is the most useful artifact when investigating a finding. It shows the exact request, headers, payload, assertions, and cleanup actions.

The HTML report is useful for scanning the findings. It has a pretty dashboard and you can drill down into the 'tests' and results. Run the local report helper from the output folder:

```shell
python webreport.py
```

On Windows, `webreport.bat` is also generated.

## Summary - Very Useful For API And OpenAPI Feedback

EvoMaster was very useful. I highly recommend it.

It did not replace exploratory testing, REST clients, or proxies.

I used it to stress the API with a lot of combinations of request and data to see what cracks it could find. And it did find cracks.

The main value for me was that EvoMaster:

- generated requests I would not have tried manually
- exposed weak OpenAPI descriptions
- made response contract mismatches obvious
- found an issue, I don't think I would have found for a long time
- worked very well in combination with AI CLI tooling

As a side-effect:

- generated reproducible tests
- helped verify that OpenAPI generation changes improved tool compatibility

I recommend EvoMaster as an automation and fuzzing tool for API projects that already have an OpenAPI file. It really doesn't take much setup and you can get value from a few minutes of execution.

For best results, give it a strong operational OpenAPI file, and treat every reported finding as a prompt for investigation rather than an automatic defect.
