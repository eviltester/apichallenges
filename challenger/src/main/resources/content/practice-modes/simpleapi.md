---
title: API Challenges Simple API
seo_title: Simple API Practice Mode | API Challenges Practice Mode
description: The Simple API is a multi-user REST API that you can use to practice testing without any authentication.
lastmod: 2026-08-14
seo_description: Use API Challenges Simple API to practice safely, understand request-response behavior, and build confidence with guided exercises before advanced testing.
og_image: /images/hero/simple-api-no-auth-practice-1600x720.jpg
og_image_alt: Simple API hero image showing no-auth CRUD practice endpoints, the live Simple API page, data explorer, and API docs.
schema_image: /images/hero/simple-api-no-auth-practice-1600x720.jpg
---



# Simple API

<figure class="content-hero-figure simple-api-hero-image">
  <img src="/images/hero/simple-api-no-auth-practice-1600x720.jpg" width="1600" height="720" loading="eager" decoding="async" alt="Simple API hero image showing no-auth CRUD practice endpoints, the live Simple API page, data explorer, and API docs.">
</figure>

The API Challenges Simple API is an easy-to-use API where you can GET, DELETE, PUT and POST without any authentication.

## Learning Path

Use the Simple API when you want to stop reading about REST and start changing data. It is a good follow-on from the [REST API Tutorial](/tutorials/rest-api-tutorial) because you can practise create, read, update, and delete requests without setting up authentication.

If you need a refresher while experimenting, the useful companions are [REST API Basics](/reference/rest-api-basics) for resources and CRUD, [HTTP Methods and Verbs](/reference/http-verbs) for method expectations, and [How to Test REST APIs](/tutorials/rest-api-testing) for turning the requests into test coverage. After this, move into the [API Challenges overview](/apichallenges) and use the [API Challenge Solutions](/apichallenges/solutions) to compare your approach with guided walkthroughs.

## Overview of Simple API

{{<youtube-embed key="EBXSJ0C2j5I" title="Simple API Overview">}}


[Patreon ad free video](https://www.patreon.com/posts/126496992)

## About Simple API

To help you get started with API testing and practice using your tools, we have created the Simple API.

The Simple API has a single end point `/simpleapi/items` and you can `GET`, `DELETE`, `PUT` and `POST` without
requiring any authentication or authorization, making Simple API a recommended first step in your API Testing learning journey.

Data will refresh automatically when low, and there is a limit to the number of items that can be added.

Because the API has no unstructured text fields e.g. `description`, there is no way to add any potentially offensive
or personal information.

To create a new item you need to add a unique `ISBN`. We have added an endpoint to generate a random ISBN `/simpleapi/randomisbn`.

Or you can click the button below and copy and paste the value into your APi call.

{{<PARTIAL_SNIPPET filename="partials/generate-random-isbn.html">}}

## Why did we create this?

We noticed that most APIs, including our API Challenges, require some sort of authentication to use the full capabilities
of the API, and we wanted to make learning easier.

We wanted to put no barriers between yourself and your learning how to use APIs.

The [documentation](/simpleapi/docs) explains the data formats and the validations. Additionally, you can download an
Open API Swagger File to load into your API tool of choice and start testing straight away.

## Testing Experiments

When you want to move beyond the basic create, read, update, and delete flow, use the [Simple API testing experiments](/practice-modes/simpleapi/experiments) page. It describes practical coverage ideas as experiments: what to vary, what to observe, and what might indicate a bug.

## Swagger OpenAPI File

You can download a Simple API Swagger [OpenAPI File for Simple Api](/practice-modes/simpleapi-openapi).
