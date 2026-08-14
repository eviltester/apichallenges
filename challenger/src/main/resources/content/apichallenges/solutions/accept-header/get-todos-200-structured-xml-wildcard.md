---
date: 2026-08-14T10:00:00Z
lastmod: 2026-08-14
title: API Challenges Solution For - GET todos structured XML wildcard 200
seo_title: Solution: GET todos Structured XML Wildcard 200 | API Challenges
description: How to solve API challenge GET todos 200 by requesting a supported structured XML wildcard media type.
seo_description: Request todos with Accept application/*+xml and confirm the API chooses a supported todo XML media type with a structured Content-Type.
next_challenge: /apichallenges/solutions/content-type-header/post-todos-xml
concepts_learned: HTTP GET||200 OK||Accept header||structured media types
concept_summary: Use this challenge to learn how application/*+xml can negotiate a supported model-specific XML response.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Accept to application/*+xml||Send the request and verify status 200 and a structured +xml Content-Type
showads: true
---

# How to complete the challenge `GET /todos (200) structured XML wildcard`

This challenge uses a structured suffix wildcard: any supported `application` media type ending in `+xml`.

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/*+xml` to receive results in a supported structured XML format.

## Basic Instructions

- Issue a `GET` request to `/todos`
- Add the `X-CHALLENGER` header to track challenge completion
- Set `Accept` to `application/*+xml`
- Check that the response status is `200`
- Check that the response `Content-Type` is a supported structured todo `XML` media type

### Try it now

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/*+xml" details="true" summary="GET /todos with a structured XML wildcard" open="true">}}

## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> X-CHALLENGER: x-challenger-guid
> Accept: application/*+xml
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: application/todo+xml
< X-Challenger: x-challenger-guid
~~~~~~~~

The response body should contain the todo collection as `XML`.

## Lessons Learned

- `application/*+xml` can match a supported structured `XML` media type such as `application/todo+xml`.
- A suffix wildcard is narrower than `*/*` because it still requires an `application` media type ending in `+xml`.
- The server chooses the concrete response media type, so tests should read the returned `Content-Type`.

## Suggested Experiments

- Compare `Accept: application/*+xml` with `Accept: application/xml` and note the selected response media type.
- Try `Accept: application/*+json` and confirm that structured `JSON` negotiation is independent from plain `application/json`.
- Send `Accept: application/*+xml;q=0, application/json;q=1` and verify that the wildcard is excluded.
