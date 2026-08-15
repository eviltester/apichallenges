---
title: API Challenges Solution For - GET Todos (200)
seo_title: Solution: GET Todos (200) Guide | API Challenges
description: How to solve the API challenge and GET all the Todos
lastmod: 2026-08-06
seo_description: Use this walkthrough to solve GET Todos (200) with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/get/get-todo-404
concepts_learned: HTTP GET||200 OK||safe method||collection resource
concept_summary: Use this challenge to learn how GET reads a collection resource without changing server state.
concept_reference_label: HTTP GET Verb
concept_reference_url: /reference/http-verbs/http-get
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /api/todos 200`.

How to solve the API challenge and issue a GET request to return all the Todos in default JSON format.

## GET /api/todos (200)

> Issue a `GET` request on the `/api/todos` end point

- This will show you all the todos in the system
- The return format is a useful guide for the syntax of the request you will send in POST messages
- Perform a `GET` prior to any amendment or deletion, to make sure that the data in the system is what you expect it to be.
- remember not to add a trailing `/` on the request e.g. `/api/todos/` - that is a different end point

## Basic Instructions

- Issue a GET request to end point "/api/todos"
    - `{{<ORIGIN_URL>}}/api/todos`
- The request should have an `X-CHALLENGER` header
- The response body shows all the todos.

### Try it now

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/todos to list all todos" open="true">}}


## Example Request

~~~~~~~~
> GET /api/todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Fri, 28 Aug 2020 13:15:04 GMT
< Content-Type: application/json
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Example Response Payload

~~~~~~~~
{
  "todos": [
    {
      "id": 6,
      "title": "process payroll",
      "doneStatus": false,
      "description": ""
    },
    {
      "id": 9,
      "title": "tidy meeting room",
      "doneStatus": false,
      "description": ""
    }
   ]
}
~~~~~~~~

## Overview Video

{{<youtube-embed key="OpisB0UZq0c" title="Solution video for GET all TODOs">}}

[Patreon ad free version](https://www.patreon.com/posts/41107610)

## Lessons Learned

- `GET /api/todos` is the baseline collection read used before most state-changing challenges.
- Safe reads let you inspect current state without creating or changing todos.
- A collection response is the easiest place to learn the default todo representation.

## Suggested Experiments

- Capture the ids returned by `GET /api/todos`, create a new todo, then call `GET /api/todos` again to see the collection change.
- Add `?doneStatus=false` and compare the filtered collection with the full baseline.