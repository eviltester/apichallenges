---
date: 2026-08-14T09:55:00Z
lastmod: 2026-08-14
title: API Challenges Solution For - GET todos vendor XML 200
seo_title: Solution: GET todos Vendor XML 200 | API Challenges
description: How to solve API challenge GET todos 200 by requesting a todo-specific vendor XML media type.
seo_description: Request todos with application/vnd.apichallenges.todo+xml and confirm the vendor XML response representation and Content-Type header.
next_challenge: /apichallenges/solutions/accept-header/get-todos-200-structured-xml-wildcard
concepts_learned: HTTP GET||200 OK||Accept header||vendor media types
concept_summary: Use this challenge to learn how an explicit vendor +xml media type can represent a todo resource.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a GET request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Accept to application/vnd.apichallenges.todo+xml||Send the request and verify status 200 and the vendor XML Content-Type
showads: true
---

# How to complete the challenge `GET /api/todos (200) vendor XML`

This challenge requests a todo-specific `+xml` media type.

> Issue a GET request on the `/api/todos` end point with an `Accept` header of `application/vnd.apichallenges.todo+xml` to receive results in todo XML format.

## Basic Instructions

- Issue a `GET` request to `/api/todos`
- Add the `X-CHALLENGER` header to track challenge completion
- Set `Accept` to `application/vnd.apichallenges.todo+xml`
- Check that the response status is `200`
- Check that the response `Content-Type` is `application/vnd.apichallenges.todo+xml`

### Try it now

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="Accept: application/vnd.apichallenges.todo+xml" details="true" summary="GET /api/todos with vendor todo XML" open="true">}}

## Example Request

~~~~~~~~
> GET /api/todos HTTP/1.1
> Host: {{<HOST_URL>}}
> X-CHALLENGER: x-challenger-guid
> Accept: application/vnd.apichallenges.todo+xml
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: application/vnd.apichallenges.todo+xml
< X-Challenger: x-challenger-guid
~~~~~~~~

The response body should still contain a todo collection encoded as `XML`.

## Lessons Learned

- `application/vnd.apichallenges.todo+xml` is accepted because the suffix is `+xml` and the subtype names the `todo` model.
- Vendor media types let an API be more specific than generic `application/xml`.
- Explicit model support matters; not every `+xml` media type is a valid todo representation.

## Suggested Experiments

- Try `Accept: application/vnd.apichallenges.todos+xml` and compare plural model naming behavior.
- Try `Accept: application/problem+xml` and confirm that an error-document media type is not treated as todo `XML`.
- Request the same collection with generic `application/xml` and compare the body shape.
