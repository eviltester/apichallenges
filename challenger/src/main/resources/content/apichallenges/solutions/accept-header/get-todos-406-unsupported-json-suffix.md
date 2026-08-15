---
date: 2026-08-14T09:45:00Z
lastmod: 2026-08-14
title: API Challenges Solution For - GET todos unsupported structured JSON 406
seo_title: Solution: GET todos Unsupported Structured JSON 406 | API Challenges
description: How to solve API challenge GET todos 406 with unsupported structured JSON Accept media types.
seo_description: Request todos with application/problem+json or application/*+json and confirm that structured JSON does not match plain application/json.
next_challenge: /apichallenges/solutions/accept-header/get-todos-200-text-xml
concepts_learned: HTTP GET||406 Not Acceptable||Accept header||structured media types
concept_summary: Use this challenge to learn that structured +json media types are not aliases for application/json.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a GET request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Accept to application/problem+json or application/*+json||Send the request and verify status 406
showads: true
---

# How to complete the challenge `GET /api/todos (406) unsupported +json`

This challenge asks for a structured `+json` media type that the todo endpoint does not support as a normal resource representation.

> Issue a GET request on the `/api/todos` end point with an `Accept` header such as `application/problem+json` or `application/*+json` to receive 406 'NOT ACCEPTABLE' status code.

## Basic Instructions

- Issue a `GET` request to `/api/todos`
- Add the `X-CHALLENGER` header to track challenge completion
- Set `Accept` to `application/problem+json`
- Check that the response status is `406`

### Try it now

{{<api-live-request method="GET" path="/api/todos" expected-status="406" headers="Accept: application/problem+json" details="true" summary="GET /api/todos with unsupported problem JSON Accept" open="true">}}

## Example Request

~~~~~~~~
> GET /api/todos HTTP/1.1
> Host: {{<HOST_URL>}}
> X-CHALLENGER: x-challenger-guid
> Accept: application/problem+json
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 406 Not Acceptable
< Content-Type: application/json
< X-Challenger: x-challenger-guid
~~~~~~~~

The API may still format its error payload as `application/json`, but that is not the same as returning the requested todo representation.

## Lessons Learned

- `application/problem+json` is normally an error document media type, not a todo collection representation.
- `application/*+json` does not match plain `application/json` because the plain subtype does not end in `+json`.
- Structured media types need explicit endpoint support before they can be used in `Accept` negotiation.

## Suggested Experiments

- Retry with `Accept: application/problem+json, application/json;q=0.5` and confirm that the supported fallback can succeed.
- Send `Accept: application/vnd.apichallenges.todo+json` and check whether the API advertises that vendor `JSON` representation.
- Compare this failure with `Accept: application/*+xml`, which can match a supported structured todo `XML` representation.
