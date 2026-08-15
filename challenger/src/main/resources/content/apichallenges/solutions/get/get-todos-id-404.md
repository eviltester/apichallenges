---
date:  2020-12-15T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos id 404
seo_title: Solution: GET todos id 404 Guide | API Challenges
description: How to solve challenge GET todos id 404.
seo_description: Use this walkthrough to solve GET todos id 404 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/get/get-todos-200-filter
concepts_learned: HTTP GET||404 Not Found||resource URL||safe method
concept_summary: Use this challenge to learn how GET reports a missing or incorrect resource URL.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a GET request to /api/todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 404
showads: true
---


# How to complete the challenge `GET /api/todos/id 404`.

How to receive a 404 status code response by trying to GET a todo item by id for a non-existent todo item.

## GET /api/todos/id (404)

> 	Issue a GET request on the `/api/todos/{id}` end point for a todo that does not exist

- This will show you a 404 status code in the API response
- 404 status code means Not Found
- The `{id}` means, replace this with the id of a non-existant todo item

## Basic Instructions

- Issue a GET request to end point "/api/todos/{id}"
    - `{{<ORIGIN_URL>}}/api/todos/{id}`
- The request should have an `X-CHALLENGER` header
- The response status code should be `404` because `{id}` does not exist
- an error message should be shown in the response body

### Try it now

{{<api-live-request method="GET" path="/api/todos/{{missingTodoId}}" expected-status="404" headers="Accept: application/json" details="true" summary="GET /api/todos/{id} to request a missing todo" open="true">}}


## Example Request

~~~~~~~~
> GET /api/todos/20 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 404 Not Found
< Connection: close
< Date: Thu, 27 Aug 2020 13:53:54 GMT
< Content-Type: application/json
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Example Response Body

~~~~~~~~
{
  "errorMessages": [
    "Could not find an instance with todos/20"
  ]
}
~~~~~~~~


## Overview Video

{{<youtube-embed key="1S5kpd8-xfM" title="Solution to Get Missing Todo 404">}}

[Patreon ad free version](https://www.patreon.com/posts/41109076)

## Lessons Learned

- `GET /api/todos/{id}` with a missing `id` tests resource absence, not route absence.
- `404 Not Found` can be a valid outcome when the URL pattern is correct but the item is gone.
- A good missing-id test proves the `id` was absent before making the request.

## Suggested Experiments

- Delete a known todo, then request the same `/api/todos/{id}` and compare with a never-used `id`.
- Request `/api/todo/{id}` as a separate test to see route mismatch versus missing resource.