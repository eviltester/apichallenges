---
date: 2026-08-14T09:50:00Z
lastmod: 2026-08-14
title: API Challenges Solution For - GET todos text XML 200
seo_title: Solution: GET todos text XML 200 | API Challenges
description: How to solve API challenge GET todos 200 by requesting the todo collection with Accept text/xml.
seo_description: Request todos with Accept text/xml and confirm that the API returns an XML todo collection using the text/xml media type.
next_challenge: /apichallenges/solutions/accept-header/get-todos-200-vendor-xml
concepts_learned: HTTP GET||200 OK||Accept header||XML media types
concept_summary: Use this challenge to learn that text/xml can be supported as an XML response media type.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a GET request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Accept to text/xml||Send the request and verify status 200 and Content-Type text/xml
showads: true
---

# How to complete the challenge `GET /api/todos (200) text/xml`

This challenge requests the todo collection using `text/xml`, an older generic `XML` media type.

> Issue a GET request on the `/api/todos` end point with an `Accept` header of `text/xml` to receive results in XML format.

## Basic Instructions

- Issue a `GET` request to `/api/todos`
- Add the `X-CHALLENGER` header to track challenge completion
- Set `Accept` to `text/xml`
- Check that the response status is `200`
- Check that the response `Content-Type` is `text/xml`

### Try it now

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="Accept: text/xml" details="true" summary="GET /api/todos with Accept text/xml" open="true">}}

## Example Request

~~~~~~~~
> GET /api/todos HTTP/1.1
> Host: {{<HOST_URL>}}
> X-CHALLENGER: x-challenger-guid
> Accept: text/xml
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: text/xml
< X-Challenger: x-challenger-guid
~~~~~~~~

The response body should contain the todo collection as `XML`.

## Lessons Learned

- `text/xml` can be negotiated separately from `application/xml`.
- Client tests should assert the exact response `Content-Type` when the media type is part of the requirement.
- Multiple media types can render the same resource structure as `XML`.

## Suggested Experiments

- Compare `Accept: text/xml` with `Accept: application/xml` and inspect whether only the response media type changes.
- Try `Accept: text/xml;q=0.4, application/json;q=1` and confirm that `JSON` becomes preferred.
- Send `Content-Type: text/xml` on a create request and compare request parsing with `application/xml`.
