---
title: API Challenges Solution For - GET todos id 200
seo_title: Solution: GET todos id 200 Guide | API Challenges
description: How to solve challenge GET todos id 200.
lastmod: 2026-08-06
seo_description: Use this walkthrough to solve GET todos id 200 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/get/get-todos-id-404
concepts_learned: HTTP GET||200 OK||resource URL||safe method
concept_summary: Use this challenge to learn how GET reads a single resource by id.
concept_reference_label: HTTP GET Verb
concept_reference_url: /reference/http-verbs/http-get
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos/id 200`.

How to issue a GET request for an existing todo item using the id of the item and receive a 200 status code and see the todo item as JSON in the response body.

## GET /todos/id (200)

> 	Issue a GET request on the `/todos/{id}` end point to return a specific todo

- This will show you a todo in the API response
- 200 status code means OK
- The response is the basic JSON format you use in a POST request to create a todo
- The `{id}` means, replace this with the id of an existing todo item

## Basic Instructions

- Issue a GET request to end point "/todos/{id}"
    - `{{<ORIGIN_URL>}}/todos/{id}`
- The request should have an `X-CHALLENGER` header
- The response status code should be `200`

### Try it now

If you don't know what todos are available then you can check by `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /todos to create a todo item for this challenge">}}

{{<api-live-request method="GET" path="/todos/{{firstTodoId}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos/{id} to retrieve a specific todo" open="true">}}


## Example Request

~~~~~~~~
> GET /todos/79 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Tue, 01 Sep 2020 13:35:41 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Example Response Body

~~~~~~~~
{
  "todos": [
    {
      "id": 79,
      "title": "tidy meeting room",
      "doneStatus": false,
      "description": ""
    }
  ]
}
~~~~~~~~


## Overview Video

{{<youtube-embed key="JDbbSY3U_rY" title="Solution to Get Specific Todo by ID">}}

[Patreon ad free version](https://www.patreon.com/posts/41108384)

## Lessons Learned

- `GET /todos/{id}` reads one resource and should return the same fields seen in the collection.
- Path parameters target a specific resource, unlike query filters that still return collections.
- A single-resource response is useful for verifying create, update, and delete follow-up behavior.

## Suggested Experiments

- Choose an `id` from `GET /todos`, then request `/todos/{id}` and compare the field values.
- Update that todo and repeat the same `GET /todos/{id}` to confirm the resource changed.