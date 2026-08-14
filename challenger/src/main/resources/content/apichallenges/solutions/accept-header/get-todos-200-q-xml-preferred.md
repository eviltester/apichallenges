---
date: 2026-08-14T09:30:00Z
lastmod: 2026-08-14
title: API Challenges Solution For - GET todos q XML preferred 200
seo_title: Solution: GET todos q XML Preferred 200 | API Challenges
description: How to solve API challenge GET todos 200 with Accept q-values preferring XML over JSON.
seo_description: Request todos with Accept quality values so XML is preferred over JSON and confirm the negotiated XML response.
next_challenge: /apichallenges/solutions/accept-header/get-todos-200-q-json-preferred
concepts_learned: HTTP GET||200 OK||Accept header||quality values
concept_summary: Use this challenge to learn how Accept q-values can prefer XML while keeping JSON acceptable.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Accept to application/json;q=0.5, application/xml;q=1||Send the request and verify status 200 and Content-Type application/xml
showads: true
---

# How to complete the challenge `GET /todos (200) q XML preferred`

This challenge uses `Accept` quality values to say that both `JSON` and `XML` are acceptable, but `XML` is preferred.

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/json;q=0.5, application/xml;q=1` to receive results in XML format.

## Basic Instructions

- Issue a `GET` request to `/todos`
- Add the `X-CHALLENGER` header to track challenge completion
- Set `Accept` to `application/json;q=0.5, application/xml;q=1`
- Check that the response status is `200`
- Check that the response `Content-Type` is `application/xml`

### Try it now

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json;q=0.5, application/xml;q=1" details="true" summary="GET /todos with q-values preferring XML" open="true">}}

## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> X-CHALLENGER: x-challenger-guid
> Accept: application/json;q=0.5, application/xml;q=1
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: application/xml
< X-Challenger: x-challenger-guid
~~~~~~~~

The response body should contain the todo collection as `XML`.

## Lessons Learned

- `q=1` gives `application/xml` a stronger preference than `application/json;q=0.5`.
- Lower-priority `JSON` remains acceptable, so it can still be used if the `XML` representation is unavailable.
- A negotiated response should be verified with the response `Content-Type`, not only with the request header.

## Suggested Experiments

- Remove `;q=1` from `application/xml` and confirm that the missing quality value still behaves like `1.0`.
- Change the `XML` quality to `q=0.4` and watch the preferred response switch to `application/json`.
- Add an unsupported high-priority media type before the two supported types and confirm the fallback still works.
