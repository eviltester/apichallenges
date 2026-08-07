---
title: API Challenges Simulation Mode
seo_title: Simulation Mode | API Challenges Practice Mode
description: A simulated API tutorial - follow the guided instructions and learn how to use your API Tool without any side-effects or risk.
lastmod: 2026-07-29
seo_description: Use API Challenges Simulation to practice safely, understand request-response behavior, and build confidence with guided exercises before advanced testing.
og_image: /images/hero/api-simulator-browser-requests-1600x720.jpg
og_image_alt: Simulation Mode hero image showing live in-browser HTTP request examples and the Simulator request widget.
schema_image: /images/hero/api-simulator-browser-requests-1600x720.jpg
twitter_card: summary_large_image
---

# Simulation Mode

<figure class="content-hero-figure simulator-hero-image">
  <img src="/images/hero/api-simulator-browser-requests-1600x720.jpg" width="1600" height="720" loading="eager" decoding="async" alt="Simulation Mode hero image showing live in-browser HTTP request examples and the Simulator request widget.">
</figure>

The API has a simulation mode, it uses hard coded data in responses, but tries to mimic some conditions.

## Learning Path

Simulation Mode is the low-risk rehearsal API. The responses are predictable, so it works well immediately after the [REST API Tutorial](/tutorials/rest-api-tutorial) when you want to focus on request structure rather than data setup.

Use [HTTP Basics](/reference/http-basics) when you want to interpret the raw message, [HTTP Methods and Verbs](/reference/http-verbs) when you want to understand why a method succeeds or fails, and [How to Test REST APIs](/tutorials/rest-api-testing) when you are ready to plan coverage. For stateful CRUD practice, move on to the [Simple API](/practice-modes/simpleapi), then use the [API Challenge Solutions](/apichallenges/solutions) to compare your observations with worked examples.

## Overview of Simulation Mode

{{<youtube-embed key="jlbLr2Ddo6s" title="How to use simulation mode">}}

[Patreon ad free video](https://www.patreon.com/posts/54383023)

## About Simulation Mode

The API simulation mode, uses hard coded data for responses, but tries to mimic conditions e.g. JSON vs XML payloads.

The simulator is stateless and does not track your usage, making it deterministic for multiple users. Which means:

*   Entities created do not show in the 'entities' call, but can be retrieved by a 'GET'
*   Entities deleted do not show in the 'entities' and respond to a 404, but the delete for them will return a 200... you can only delete 'specific' entities, other entities will respond with a forbidden request.
*   There are 'inconsistencies' but they are logical based on the needs of a stateless simulator. Use the actual API that underpins the challenges or the Simple API if you want a 'real' API.

## How to Use

Work through the requests in sequence to achieve a fairly logical interaction.

- try different tooling, the only difference then will be the tool because the API is fairly forgiving and no-one else can interfere with your practice. Use it to learn the tools.
- try different automated execution approaches. The API is simple, there are only a few requests and sequences, so use it to learn a new automated execution tool. It won't change as you are automating, if something goes wrong then it is most likely some nuance of the tool.

This simulator is designed to make starting with API testing as simple as possible.

## Suggested Request Sequence

Use this list as a quick map of the simulator flow.

- Get all the entities with `GET /sim/entities`.
- Get one entity with `GET /sim/entities/1`.
- Try a missing entity with `GET /sim/entities/13`.
- Create an entity with `POST /sim/entities`.
- Amend an entity with `POST /sim/entities/10`.
- Amend an entity with `PUT /sim/entities/10`.
- Delete an entity with `DELETE /sim/entities/9`.
- Check deletion with `GET /sim/entities/9`.
- Discover allowed methods with `OPTIONS /sim/entities`.
- Compare unsupported methods such as `DELETE /sim/entities` and `PATCH /sim/entities`.
- Compare `HEAD /sim/entities` with `GET /sim/entities`.

You can use any REST Client to make these requests but if you want to follow the tutorial in detail with explanations then [follow the interactive tutorial](/tutorials/api-simulator-walkthrough) for hands-on practice with the API Simulator.

## Automating Examples

The Simulator is a very simple set of endpoints to automate because it doesn't matter what order the tests run and the results are idempotent so they will always be the same.

I have created an example set of very simple Java `@Test` methods using RestAssured which automate the Simulator.

[Simulator Automated Execution Coverage](https://github.com/eviltester/apichallenges/blob/main/challengerAuto/src/test/java/uk/co/compendiumdev/simulator/SimulatorHttpTest.java)

## Swagger OpenAPI File

You can download a simple Swagger [OpenAPI File for simulation mode](/sim/docs/openapi-3.1.json).

Versioned OpenAPI JSON files are also available:

- [OpenAPI 3.0 JSON](/sim/docs/openapi-3.0.json) - [download](/sim/docs/openapi-3.0.json?download)
- [OpenAPI 3.1 JSON](/sim/docs/openapi-3.1.json) - [download](/sim/docs/openapi-3.1.json?download)
- [OpenAPI 3.2 JSON](/sim/docs/openapi-3.2.json) - [download](/sim/docs/openapi-3.2.json?download)


OpenAPI 3.2 describes `QUERY /sim/entities` as a native method.

```http
QUERY /sim/entities HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Accept: application/json

id<3
```

## Simulation Mode Walkthrough - Insomnia

{{<youtube-embed key="CG3G5lpxE0Y" title="How to use Insomnia with simulation mode as example api">}}

[Patreon ad free video](https://www.patreon.com/posts/54383155)

## Simulation Mode Walkthrough - Postman

{{<youtube-embed key="CF3gVz9zc2s" title="How to use Postman with simulation mode as example api">}}

[Patreon ad free video](https://www.patreon.com/posts/54383110)
