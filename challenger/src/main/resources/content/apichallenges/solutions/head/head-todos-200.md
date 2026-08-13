---
date:  2021-01-24T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - HEAD todos 200
seo_title: Solution: HEAD todos 200 Guide | API Challenges
description: How to solve API challenge HEAD todos 200
seo_description: Use this walkthrough to solve HEAD todos 200 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/post-create/post-todos-201
concepts_learned: HTTP HEAD||200 OK||headers-only response||safe method
concept_summary: Use this challenge to learn how HEAD checks a resource without returning a response body.
concept_reference_label: HTTP HEAD Verb
concept_reference_url: /reference/http-verbs/http-head
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a HEAD request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `HEAD /todos (200)`

How to issue a HEAD request and see the results of a GET request without the body of the response, this can be useful for checking the existence of an item when automating.

## HEAD /todos (200)

> 	Issue a HEAD request on the `/todos` end point

- `HEAD` request is basically a `GET` but doesn't return the body
- Use it to 'ping' an end point and see if it exists, or to check the Headers are working correctly
- Usually returns a 200 (if it exists) or a 404 (if it doesn't exist)

## Basic Instructions

- Issue a HEAD request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `200` because the end point exists
- Compare the response with the response from `GET /todos`

### Try it now

{{<api-live-request method="HEAD" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="HEAD /todos to read response headers without a body" open="true">}}


## Example Request

~~~~~~~~
> HEAD /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: keep-alive
< Date: Thu, 27 Aug 2020 14:09:19 GMT
< Content-Type: application/json
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Overview Video

{{<youtube-embed key="zKbytTelP84" title="Solution to HEAD specific Todo endpoint">}}

[Patreon ad free version](https://www.patreon.com/posts/41230531)

## Lessons Learned

- `HEAD /todos` returns metadata for the collection without sending the response body.
- Headers from `HEAD` should help clients decide whether a later `GET` is worth making.
- A passing `HEAD` test should assert the body is empty, not merely ignored.

## Suggested Experiments

- Send `HEAD /todos` and `GET /todos` back to back and compare status plus response headers.
- Change the `Accept` header on `HEAD` and see whether content negotiation affects `Content-Type`.