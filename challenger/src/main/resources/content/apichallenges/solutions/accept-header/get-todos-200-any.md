---
date:  2021-05-29T09:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos any 200
seo_title: Solution: GET todos any 200 Guide | API Challenges
description: How to solve API challenge GET todos any 200 to accept the todos in default format.
seo_description: Use this walkthrough to solve GET todos any 200 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/accept-header/get-todos-200-xml-pref
concepts_learned: HTTP GET||200 OK||Accept header||content negotiation
concept_summary: Use this challenge to learn how the Accept header changes the response format for default JSON.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a GET request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set the Accept header to the required media type and verify response format||Send the request and verify the response status is 200
showads: true
---


# GET all the todos in default format

How to complete the challenge `GET /api/todos ANY (200)` to successfully GET all the todos in default format.

## GET /api/todos ANY (200)

> Issue a GET request on the `/api/todos` end point with an `Accept` header of `*/*` to receive results in Default format

- `GET` request will receive a response with all the todo items
    - e.g. `GET /api/todos` to get all the todo items
- `200` is a success code, in this case it means the end point exists and the todo items were returned
- `Accept` means that an `Accept` header was added to specify that the todos should be returned in **ANY** format i.e. the default from the server
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/api/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/api/todos`
- The request should have an `Accept` header specifying ANY format by using a value of `*/*`, our application defaults to JSON
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the body of the message has JSON format data, which is the default from the server
- Check the `content-type` header in the response has `application/json`


### Try it now

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="Accept: */*" details="true" summary="GET /api/todos with Accept: */* to request any supported format" open="true">}}


## Example Request

~~~~~~~~
> GET /api/todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sat, 29 May 2021 09:06:15 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```json
{
  "todos": [
    {
      "id": 235,
      "title": "pay invoices",
      "doneStatus": false,
      "description": ""
    },
    {
      "id": 239,
      "title": "tidy meeting room",
      "doneStatus": false,
      "description": ""
    }
  ]
}
```


## Overview Video

{{<youtube-embed key="O4DhJ8Ohkk8" title="Solution to Get all Todos in default format">}}

[Patreon ad free version](https://www.patreon.com/posts/51830126)

## Lessons Learned

- `*/*` in `Accept` says the client has no format preference, so the server default becomes part of the behavior under test.
- A `200 OK` response with wildcard negotiation still needs a `Content-Type` check because the requested format was deliberately broad.
- This challenge is useful for finding the API fallback representation before writing stricter `Accept` tests.

## Suggested Experiments

- Send `Accept: */*`, then remove the `Accept` header entirely and compare the selected `Content-Type`.
- Send `Accept: image/png` and confirm the API reports negotiation failure instead of silently returning `JSON`.
