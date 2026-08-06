---
date:  2021-04-23T09:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos xml 200
seo_title: Solution: GET todos xml 200 Guide | API Challenges
description: How to solve API challenge GET todos xml 200 to accept the todos in xml format.
seo_description: Use this walkthrough to solve GET todos xml 200 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/accept-header/get-todos-200-json
concepts_learned: HTTP GET||200 OK||Accept header||content negotiation
concept_summary: Use this challenge to learn how the Accept header changes the response format for XML.
concept_reference_label: HTTP Basics
concept_reference_url: /tutorials/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /tutorials/http-verbs
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set the Accept header to the required media type and verify response format||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos XML (200)`

When we issue a GET request we can use the `Accept` header to request a specific format of result from the API. In this case we will ask for XML to successfully GET all the todos in XML format.

## GET /todos XML (200)

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml` to receive results in XML format

- `GET` request will receive a response with all the todo items
    - e.g. `GET /todos` to get all the todo items
- `200` is a success code, in this case it means the end point exists and the todo items were returned
- `Accept` means that an `Accept` header was added to specify that the todos should be returned in XML format
- add the `X-CHALLENGER` header to track progress


## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos`
- The request should have an `Accept` header specifying XML format by using a value of `application/xml`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` when all the details are valid.
- Check the body of the message has XML format data
- Check the `content-type` header in the response has `application/xml`


### Try it now

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/xml" details="true" summary="GET /todos with Accept: application/xml to request XML" open="true">}}


## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: application/xml
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Thu, 22 Apr 2021 16:49:31 GMT
< Content-Type: application/xml
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example Response body:

```xml
<todos>
  <todo>
    <doneStatus>false</doneStatus>
    <description/>
    <id>273</id>
    <title>scan paperwork</title>
  </todo>
  <todo>
    <doneStatus>false</doneStatus>
    <description/>
    <id>277</id>
    <title>pay invoices</title>
  </todo>
</todos>
```


## Overview Video

{{<youtube-embed key="cLeEuZm2VG8" title="Solution to Get all Todos in XML format">}}

[Patreon ad free version](https://www.patreon.com/posts/50348257)

## Lessons Learned

- `Accept: application/xml` asks for an `XML` representation of the same `/todos` collection.
- The resource stays the same even though element names, nesting, and parsing rules change from `JSON` to `XML`.
- A successful `XML` response should be checked with an `XML` parser, not just by spotting angle brackets.

## Suggested Experiments

- Compare `Accept: application/xml` with `Accept: application/json` and confirm the todo count is the same in both representations.
- Remove the `Accept` header after the `XML` request and note whether the server falls back to `application/json`.
- Try `Accept: text/xml` and check whether the API treats the older generic `XML` media type as acceptable.
- Try `Accept: application/*+xml` to see whether structured `+xml` suffix negotiation is supported.
- Try a vendor-style header such as `Accept: application/vnd.apichallenges.todo+xml` and record whether it is treated as todo `XML` or rejected.
- Try `Accept: application/problem+xml` and confirm that an error-document media type is not treated as a normal todo-list representation.
