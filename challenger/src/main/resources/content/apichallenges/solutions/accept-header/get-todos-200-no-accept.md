---
date:  2021-05-29T10:32:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos No Accept Header 200
seo_title: Solution GET todos No Accept Header 200 | API Challenges
description: How to solve API challenge GET todos No Accept 200 to GET the todos with no accept header present.
seo_description: Use this walkthrough to solve GET todos No Accept Header with request setup, key headers, and expected status codes so you can complete the challenge.
next_challenge: /apichallenges/solutions/accept-header/get-todos-406
concepts_learned: HTTP GET||200 OK||Accept header||content negotiation
concept_summary: Use this challenge to learn how the Accept header changes the response format for default JSON.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a GET request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set the Accept header to the required media type and verify response format||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /api/todos No Accept (200)`

Accept headers are optional. Most API clients will add one by default. But we do not need to pass in an `Accept` header to successfully GET all the todos in JSON format. This challenge allows us to test this, to complete it we must ensure that we do not pass in an accept header.

## GET /api/todos No Accept (200)

When we issue a request with no accept header, we should receive the default from the server. But... sending a request without an Accept header might be harder depending on the tool we use.

> Issue a GET request on the `/api/todos` end point with no `Accept` header to receive results in JSON format.

- `GET` request will receive a response with all the todo items
    - e.g. `GET /api/todos` to get all the todo items
- `200` is a success code, in this case it means the end point exists and the todo items were returned
- `No Accept` means that the request should not include an `Accept` header
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/api/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/api/todos`
- The request should not have an `Accept` header at all
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the body of the message has JSON format data, which is the default from the server
- Check the `content-type` header in the response has `application/json`

Sometimes it is useful to send our requests through a proxy or look at the debug output from our tool to make sure that we are sending the requests we expect to send.

A Proxy can also be used to amend the request and remove the headers.

cURL may provide more flexibility for doing this than other tools.

In Insomnia, right click on the [Send] button and Generate Client Code for cURL. Then amend the code to remove the header by adding `--header 'Accept:'`


## cURL Details

~~~~~~~~
curl --request GET \
  --url {{<ORIGIN_URL>}}/api/todos \
  --header 'X-CHALLENGER: x-challenger-guid'
  --header 'Accept:'
  -v
~~~~~~~~

Hints:

- add `--header 'Accept:'` to the generated code to remove Accept header from the request.
- add `-v` to the generated code if you want to see the full response output.


### Try it now

The HTTP client below will send the request, but the browser will add an `Accept` header. You cannot complete this challenge from the client below; try it and see for yourself.

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="" details="true" challenge-request="true" summary="GET /api/todos without Accept to use the default response format">}}


## Example Request

~~~~~~~~
> GET /api/todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: curl/7.64.1
> X-CHALLENGER: x-challenger-guid
~~~~~~~~

## Example Response

~~~~~~~~ 
< HTTP/1.1 200 OK
< Connection: close
< Date: Sat, 29 May 2021 10:35:04 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```json
{"todos":[{"id":280,"title":"install webcam","doneStatus":false,"description":""}]}
```


## Overview Video

{{<youtube-embed key="CSVP2PcvOdg" title="Solution to Get all Todos in defaulted format">}}

[Patreon ad free version](https://www.patreon.com/posts/51831718)

## Lessons Learned

- No `Accept` header lets the server choose its default representation, which can hide client assumptions about response format.
- This challenge separates "server can return `JSON`" from "client explicitly requested `JSON`".
- Default response behavior should be documented by checking both status and `Content-Type`.

## Suggested Experiments

- Run the same request with no `Accept`, then with `Accept: application/json`, and compare the body and `Content-Type`.
- Add `Accept: application/xml` after the no-header request to see how explicit negotiation changes the representation.
