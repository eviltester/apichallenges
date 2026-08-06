---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET heartbeat 204
seo_title: Solution: GET heartbeat 204 no content | API Challenges
description: How to solve GET /heartbeat (204) and verify the no-content success response.
seo_description: Use this walkthrough to send GET /heartbeat, verify the 204 no-content response, and confirm the request body stays empty for the challenge.
next_challenge: /apichallenges/solutions/status-codes/x-challenger-too-long-431
concepts_learned: HTTP GET||204 No Content||status code||error handling
concept_summary: Use this challenge to learn how the API reports 204 No Content for this endpoint and method.
concept_reference_label: HTTP Basics
concept_reference_url: /tutorials/http-basics
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /tutorials/testing-apis
schema_howto_steps: Create a GET request to /heartbeat||Include X-CHALLENGER so progress is tracked||Send the request||Verify the response status is 204 and the body is empty
showads: true
---


# How to complete the challenge `GET /heartbeat (204)`

Send a `GET` request to `/heartbeat`.

A `204 No Content` response means the request succeeded and the response body should be empty.

### Try it now

{{<api-live-request method="GET" path="/heartbeat" expected-status="204" headers="Accept: */*" details="true" summary="GET /heartbeat to receive 204 with no body" open="true">}}

## Lessons Learned

- `GET /heartbeat` is a successful health check with no response body.
- `204 No Content` means the absence of a body is the expected success signal.
- Health endpoints are useful for connectivity checks without changing application state.

## Suggested Experiments

- Compare body length and headers for `GET /heartbeat` versus `GET /todos`.
- Add an `Accept` header and confirm the no-body response still does not need a representation.