---
date:  2021-05-09T09:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos json 200
seo_title: Solution: GET todos json 200 | API Challenges
description: How to solve API challenge GET todos json 200 to accept the todos in json format.
seo_description: Use this walkthrough to solve GET todos json 200 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/accept-header/get-todos-200-any
concepts_learned: HTTP GET||200 OK||Accept header||content negotiation
concept_summary: Use this challenge to learn how the Accept header changes the response format for JSON.
concept_reference_label: HTTP Basics
concept_reference_url: /tutorials/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /tutorials/http-verbs
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set the Accept header to the required media type and verify response format||Send the request and verify the response status is 200
showads: true
---


# GET all the todos in JSON format

How to complete the challenge `GET /todos JSON (200)` to successfully GET all the todos in JSON format.

## GET /todos JSON (200)

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/json` to receive results in JSON format

- `GET` request will receive a response with all the todo items
    - e.g. `GET /todos` to get all the todo items
- `200` is a success code, in this case it means the end point exists and the todo items were returned
- `Accept` means that an `Accept` header was added to specify that the todos should be returned in **JSON** format
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Accept` header specifying JSON format by using a value of `application/json`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the body of the message has JSON format data
- Check the `content-type` header in the response has `application/json`


### Try it now

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos with Accept: application/json to request JSON" open="true">}}


## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: application/json
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sun, 09 May 2021 11:07:48 GMT
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
            "id": 16,
            "title": "process payroll",
            "doneStatus": false,
            "description": ""
        },
        {
            "id": 15,
            "title": "pay invoices",
            "doneStatus": false,
            "description": ""
        }
    ]
}
```

## Overview Video

{{<youtube-embed key="79JTHiby2Qw" title="Solution to GET todos in JSON format">}}

[Patreon ad free version](https://www.patreon.com/posts/51045284)

## Lessons Learned

- `Accept: application/json` is an explicit request for `JSON`, not just acceptance of the server default.
- `Content-Type` should confirm the negotiated response, but the body should still be parsed as `JSON`.
- `+json` media types describe `JSON`-shaped bodies; they still need explicit server support.

## Suggested Experiments

- Compare `Accept: application/json` with `Accept: */*`; `*/*` means any response format is acceptable, so check whether the server chooses `JSON` as the default.
- Try `Accept: application/*+json` to ask for any structured `+json` media type, then compare the status code and `Content-Type` with the standard `application/json` response.
- Try a specific `+json` media type such as `application/problem+json` or `application/vnd.api+json`; these still describe `JSON` bodies, but the API may reject them if it does not support that representation.
- Send `Accept: application/json, application/xml;q=0.5` and confirm that `JSON` is preferred over `XML` when both formats are acceptable.
