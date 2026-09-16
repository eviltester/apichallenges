---
date: 2026-09-16T10:00:00Z
lastmod: 2026-09-16
title: API Challenges OpenAPI Files, EvoMaster Review, And Bug Fixes
seo_title: API Challenges OpenAPI Files, EvoMaster Testing, and Bug Fixes
description: API Challenges now includes more OpenAPI file styles, contract-focused bug fixes, and an EvoMaster automation tool review.
seo_description: Learn about the API Challenges OpenAPI file variations, operation-level parameters, EvoMaster testing, and recent API contract bug fixes.
categories: Change Log||API Testing||OpenAPI
tags: API Challenges||OpenAPI||EvoMaster||Contract Testing||API Testing
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# API Challenges OpenAPI Files, EvoMaster Review, And Bug Fixes

We have updated API Challenges with a few bug fixes, some OpenAPI documentation improvements, and a new set of OpenAPI file variations intended to work better with a wider range of API tooling.

These changes came from reviewing API Challenges with automation tools that read the OpenAPI file, generate requests, compare the documented contract with the actual runtime behaviour, and report mismatches.

## OpenAPI File Variations

The [API Challenges OpenAPI download page](/apichallenges/openapi) now offers five styles of OpenAPI file for OpenAPI 3.0, 3.1 and 3.2.

- standard validation - the normal OpenAPI file for using the API as intended.
- strong schemas - the same API surface with more explicit schema details for tools that benefit from stricter request and response shapes.
- operation parameters - a more tool-friendly version where path parameters are repeated on each operation instead of only being shared at the path level.
- strong schemas + operation parameters - combines stronger schemas with operation-level path parameters.
- less validation - a more permissive file for exploratory testing and negative testing through OpenAPI-driven clients.

The operation parameters files are the more operational OpenAPI files for API Challenges. They are particularly useful for automated tools and generated clients that do not fully process path-level parameters. The OpenAPI specification allows shared path-level parameters, but some tools achieve better route coverage and produce clearer generated requests when the parameters are declared directly on every operation.

For example, the operation parameters file can help a test generator understand that `GET /api/todos/{id}`, `PUT /api/todos/{id}` and `HEAD /api/todos/{id}` each need an `id` path parameter, without relying on shared metadata from the path.

The strong schema files can also help automation tools make better choices because fields such as ids, booleans, arrays and response bodies are described more explicitly.

The less validation files are there for testing. They relax some of the restrictions that a Swagger UI style client might otherwise enforce before a request reaches the server. This makes them useful when you deliberately want to explore invalid values, unexpected methods, and edge cases.

You can download the files from the [API Challenges OpenAPI page](/apichallenges/openapi).

## Bug Fixes

The recent fixes were mostly about making the API runtime behaviour and the OpenAPI documentation agree more closely.

We changed the single player challenger id from a readable string to a valid reserved UUID:

```text
5afe0000-5eed-4000-8000-000000defa17
```

Oooh, leet speak:

```
safe0000-seed-4000-8000-000000default

default ~= defalt

default approximately equals defalt
```

This helps tools that validate UUID formats in headers and schemas, while still giving us a recognizable value for the single player session.

The API now also returns API resource locations from API routes. For example, challenger creation points to an `/api/challenger/{guid}` resource rather than a GUI page.

We also tightened up several response behaviours:

- `OPTIONS /api/todos` now documents and returns `200`.
- challenger database error responses now return JSON with an `application/json` content type.
- empty API `404` fallback responses now return JSON unless the request explicitly prefers HTML.
- generated and hand-written OpenAPI responses now describe more of the response bodies and status codes that the API can return.
- `HEAD /api/todos/{id}` no longer tries to write a response body, avoiding server errors caused by mismatched response lengths. This was found by using EvoMaster (a new recommended tool).

These should help when experimenting with OpenAPI and standards driven tooling.

## EvoMaster Review

We also added a review page for [EvoMaster](/tools/automation/evomaster).

EvoMaster is an open source API automation and fuzzing tool. It can read an OpenAPI file, generate HTTP requests, run those requests against an API, and produce tests and reports from what it finds.

For API Challenges, EvoMaster helped fuzz and scan the the OpenAPI contract. It highlighted undocumented status codes, content type mismatches, response bodies that were not described correctly, path parameter modelling issues, and a few runtime behaviours that were worth fixing.

EvoMaster findings still need human review. Some findings are genuine bugs, some are contract documentation gaps, and some are useful warnings that need context. I will add this tool to any API testing workflow: it gives a repeatable tool to put stress on the API contract and helps reveal places where the documentation and implementation do not match.

Read the full [EvoMaster API fuzzer overview and review](/tools/automation/evomaster) for setup notes, findings, and the OpenAPI file style that worked best during the review.
