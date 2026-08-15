---
date: 2026-08-14T10:05:00Z
lastmod: 2026-08-14
title: API Challenges Solution For - POST todos vendor XML
seo_title: Solution: POST todos Vendor XML | API Challenges
description: How to solve API challenge POST todos vendor XML by creating a todo with a model-specific XML Content-Type.
seo_description: Create a todo using Content-Type application/vnd.apichallenges.todo+xml, send a normal XML body, and request a negotiated response.
next_challenge: /apichallenges/solutions/content-type-header/post-todos-415
concepts_learned: HTTP POST||201 Created||Content-Type header||vendor media types
concept_summary: Use this challenge to learn how a vendor +xml Content-Type can identify a todo XML request body.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create a POST request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Content-Type to application/vnd.apichallenges.todo+xml||Send a valid XML todo body and request a supported response format||Verify status 201
showads: true
---

# How to complete the challenge `POST /api/todos vendor XML`

This challenge creates a todo with a model-specific `+xml` request body media type and asks for a negotiated response.

> Issue a POST request on the `/api/todos` end point to create a todo using Content-Type `application/vnd.apichallenges.todo+xml`, a normal todo XML body, and a negotiated response.

## Basic Instructions

- Issue a `POST` request to `/api/todos`
- Add the `X-CHALLENGER` header to track challenge completion
- Set `Content-Type` to `application/vnd.apichallenges.todo+xml`
- Set `Accept` to a supported response format, for example `application/json`
- Send a valid `<todo>` `XML` body without an `id`
- Check that the response status is `201`
- Check the `Location` header points to the created todo

### Try it now

{{<api-live-request method="POST" path="/api/todos" expected-status="201" headers="Content-Type: application/vnd.apichallenges.todo+xml||Accept: application/json" body='<todo><title>vendor XML todo</title><doneStatus>true</doneStatus><description>created with vendor XML</description></todo>' details="true" summary="POST /api/todos with vendor todo XML" open="true">}}

## Example Request

~~~~~~~~
> POST /api/todos HTTP/1.1
> Host: {{<HOST_URL>}}
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/vnd.apichallenges.todo+xml
> Accept: application/json

| <todo>
|   <title>vendor XML todo</title>
|   <doneStatus>true</doneStatus>
|   <description>created with vendor XML</description>
| </todo>
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 201 Created
< Content-Type: application/json
< Location: todos/123
< X-Challenger: x-challenger-guid
~~~~~~~~

The response body should contain the created todo in the negotiated response format.

## Lessons Learned

- `Content-Type: application/vnd.apichallenges.todo+xml` tells the API to parse the request body as todo `XML`.
- The request `Content-Type` and response `Accept` header can name different representations.
- Vendor `+xml` request support should be tested separately from generic `application/xml`.

## Suggested Experiments

- Change `Accept` to `application/vnd.apichallenges.todo+xml` and check whether the created todo is returned as vendor `XML`.
- Change the request `Content-Type` to `application/problem+xml` and confirm it is not treated as a todo body format.
- Send the same `<todo>` body with `Content-Type: text/xml` and compare the create behavior.
