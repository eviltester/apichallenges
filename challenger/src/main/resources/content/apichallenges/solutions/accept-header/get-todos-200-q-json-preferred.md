---
date: 2026-08-14T09:35:00Z
lastmod: 2026-08-14
title: API Challenges Solution For - GET todos q JSON preferred 200
seo_title: Solution: GET todos q JSON Preferred 200 | API Challenges
description: How to solve API challenge GET todos 200 with Accept q-values preferring JSON over XML.
seo_description: Request todos with Accept quality values so JSON is preferred over XML and confirm the negotiated JSON response.
next_challenge: /apichallenges/solutions/accept-header/get-todos-406-q-rejects-all
concepts_learned: HTTP GET||200 OK||Accept header||quality values
concept_summary: Use this challenge to learn how Accept q-values can prefer JSON while keeping XML acceptable.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Accept to application/xml;q=0.5, application/json;q=1||Send the request and verify status 200 and Content-Type application/json
showads: true
---

# How to complete the challenge `GET /todos (200) q JSON preferred`

This challenge reverses the previous preference: both `XML` and `JSON` are acceptable, but `JSON` has the higher quality value.

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml;q=0.5, application/json;q=1` to receive results in JSON format.

## Basic Instructions

- Issue a `GET` request to `/todos`
- Add the `X-CHALLENGER` header to track challenge completion
- Set `Accept` to `application/xml;q=0.5, application/json;q=1`
- Check that the response status is `200`
- Check that the response `Content-Type` is `application/json`

### Try it now

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/xml;q=0.5, application/json;q=1" details="true" summary="GET /todos with q-values preferring JSON" open="true">}}

## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> X-CHALLENGER: x-challenger-guid
> Accept: application/xml;q=0.5, application/json;q=1
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: application/json
< X-Challenger: x-challenger-guid
~~~~~~~~

The response body should contain a `JSON` object with a `todos` array.

## Lessons Learned

- `application/json;q=1` outranks `application/xml;q=0.5` even when `XML` appears first.
- Quality values are clearer than relying only on comma order in longer `Accept` headers.
- `JSON` and `XML` can both be acceptable while one format remains the preferred result.

## Suggested Experiments

- Swap the order of the two media types without changing the quality values and confirm `application/json` still wins.
- Set both quality values to `q=1` and observe how the server breaks the tie.
- Try `Accept: application/problem+json;q=1, application/json;q=0.5` and check that the supported fallback is still usable.
