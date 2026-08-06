---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PATCH heartbeat 500
seo_title: Solution: PATCH heartbeat 500 | API Challenges
description: How to solve PATCH /heartbeat (500) and trigger the simulated server error.
seo_description: Use this walkthrough to send PATCH /heartbeat, verify the 500 response, and see how the API exposes unsupported heartbeat behavior for the challenge.
next_challenge: /apichallenges/solutions/status-codes/trace-heartbeat-501
concepts_learned: HTTP PATCH||500 Internal Server Error||status code||error handling
concept_summary: Use this challenge to learn how the API reports 500 Internal Server Error for this endpoint and method.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /reference/testing-apis
schema_howto_steps: Create a PATCH request to /heartbeat||Include X-CHALLENGER so progress is tracked||Send the request without a body||Verify the response status is 500
showads: true
---


# How to complete the challenge `PATCH /heartbeat (500)`

Send a `PATCH` request to `/heartbeat`.

This endpoint deliberately returns `500` for PATCH so you can practise observing server-error responses during API testing.

### Try it now

{{<api-live-request method="PATCH" path="/heartbeat" expected-status="500" headers="Accept: */*" details="true" summary="PATCH /heartbeat to trigger 500" open="true">}}

## Lessons Learned

- `PATCH /heartbeat` triggers a server-error path on purpose for the challenge.
- `500 Internal Server Error` tells you the request reached the server but the server failed to handle it.
- Error-status tests should capture response body, headers, and logs if available.

## Suggested Experiments

- Compare direct `PATCH /heartbeat` with method override to `PATCH` and see if both reach the same failure.
- Retry the request and note whether the simulated `500 Internal Server Error` is stable or intermittent.