---
date: 2026-08-14T09:40:00Z
lastmod: 2026-08-14
title: API Challenges Solution For - GET todos q rejects all 406
seo_title: Solution: GET todos q Rejects All 406 | API Challenges
description: How to solve API challenge GET todos 406 by excluding supported JSON and XML media types with q=0.
seo_description: Send Accept q=0 values for JSON and XML to reject supported todo representations and confirm the 406 response.
next_challenge: /apichallenges/solutions/accept-header/get-todos-406-unsupported-json-suffix
concepts_learned: HTTP GET||406 Not Acceptable||Accept header||quality values
concept_summary: Use this challenge to learn that Accept q=0 excludes a media type from negotiation.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Accept to application/json;q=0, application/xml;q=0||Send the request and verify status 406
showads: true
---

# How to complete the challenge `GET /todos (406) q rejects all`

This challenge sends an `Accept` header that names supported formats but explicitly rejects both of them.

> Issue a GET request on the `/todos` end point with an `Accept` header of `application/json;q=0, application/xml;q=0` to receive 406 'NOT ACCEPTABLE' status code.

## Basic Instructions

- Issue a `GET` request to `/todos`
- Add the `X-CHALLENGER` header to track challenge completion
- Set `Accept` to `application/json;q=0, application/xml;q=0`
- Check that the response status is `406`
- Check that the error response is returned in a format your client can inspect

### Try it now

{{<api-live-request method="GET" path="/todos" expected-status="406" headers="Accept: application/json;q=0, application/xml;q=0" details="true" summary="GET /todos while rejecting JSON and XML" open="true">}}

## Example Request

~~~~~~~~
> GET /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> X-CHALLENGER: x-challenger-guid
> Accept: application/json;q=0, application/xml;q=0
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 406 Not Acceptable
< Content-Type: application/json
< X-Challenger: x-challenger-guid
~~~~~~~~

The error body should explain that no acceptable response type was available.

## Lessons Learned

- `q=0` excludes a media type, even when the API normally supports that representation.
- Listing only excluded `JSON` and `XML` choices leaves no normal todo representation to return.
- A `406` response can be the correct result when the client rules out every supported format.

## Suggested Experiments

- Change `application/json;q=0` to `application/json;q=0.1` and confirm that the request can succeed.
- Add `*/*;q=0.1` and observe whether the server can choose a default fallback.
- Compare this with an unsupported type such as `application/gzip`, where no supported media type is named.
