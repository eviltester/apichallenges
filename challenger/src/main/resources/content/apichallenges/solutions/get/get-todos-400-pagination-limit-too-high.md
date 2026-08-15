---
date: 2026-07-30T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos 400 pagination limit too high
seo_title: "Solution: GET todos 400 pagination limit too high | API Challenges"
description: How to solve API challenge GET todos 400 pagination limit too high using an unsupported _limit value.
seo_description: Use this walkthrough to solve GET todos 400 pagination limit too high by sending _limit=21, confirming rejection, and verifying status 400.
next_challenge: /apichallenges/solutions/get/get-todos-200-pagination-sort
concepts_learned: HTTP GET||400 Bad Request||query parameters||validation
concept_summary: Use this challenge to learn how APIs reject invalid query parameter values.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /reference/testing-apis
schema_howto_steps: Create a GET request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add _limit=21 to exceed the configured maximum||Send the request and verify the response status is 400
showads: true
---


# How to complete the challenge `GET /api/todos (400) ?_limit too high`

How to issue a GET request on a top level entity endpoint with a pagination limit above the allowed maximum.

## GET /api/todos (400) ?_limit too high

> Issue a GET request on the `/api/todos` end point with a pagination limit above the configured maximum to receive a 400 status code.

- the maximum supported `_limit` value is `20`
- `_limit=21` is too high for this API
- this challenge is passed by receiving a `400` response for the invalid pagination request

## Basic Instructions

- Issue a `GET` request to end point "/api/todos"
    - `{{<ORIGIN_URL>}}/api/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Add `_limit=21` as a URL parameter:
    - `{{<ORIGIN_URL>}}/api/todos?_limit=21`
- The response status code should be `400` because the pagination limit is too high

### Try it now

{{<api-live-request method="GET" path="/api/todos?_limit=21" expected-status="400" headers="Accept: application/json" details="true" summary="GET /api/todos?_limit=21 to trigger validation for a high limit" open="true">}}

## Example Request

~~~~~~~~
> GET /api/todos?_limit=21 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: application/json
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 400 Bad Request
< Connection: close
< Content-Type: application/json
< X-Challenger: x-challenger-guid
~~~~~~~~

The response body contains validation details explaining that the requested pagination limit is too high.

## Lessons Learned

- `_limit` has an upper bound, so pagination parameters need validation like request bodies do.
- `400 Bad Request` is appropriate when the query parameter itself is invalid.
- Boundary tests should check just below, at, and above the documented maximum.

## Suggested Experiments

- Compare `_limit=20` with `_limit=21` to identify the first failing value.
- Send a non-numeric `_limit` and compare its error body with the too-high limit response.