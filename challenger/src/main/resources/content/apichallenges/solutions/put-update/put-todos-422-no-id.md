---
date:  2026-08-01T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT todos no id 422
seo_title: Solution: PUT todos no id 422 | API Challenges
description: How to solve API challenge PUT todos no id 422.
seo_description: Use this walkthrough to solve PUT /api/todos no id 422 by sending a valid update payload without any identifier in the URL or body.
next_challenge: /apichallenges/solutions/put-update/put-todos-id-404-not-found
concepts_learned: HTTP PUT||422 Unprocessable Content||idempotent method||id handling
concept_summary: Use this challenge to learn how PUT handles id handling for todo resources.
concept_reference_label: HTTP PUT Verb
concept_reference_url: /reference/http-verbs/http-put
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a PUT request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a JSON payload without an id field||Send the request and verify the response status is 422
showads: true
---


# How to complete the challenge `PUT /api/todos no id (422)`

Issue a `PUT` request to `/api/todos` without an id in the URL or the request body.

The `/api/todos` endpoint exists, but the API cannot know which todo to update. Because the request reaches a valid endpoint but is missing required update information, the response should be `422 Unprocessable Content`.

## Basic Instructions

- Send `PUT /api/todos`
- Add an `X-CHALLENGER` header to track challenge completion
- Set `Content-Type` to `application/json`
- Do not include an `id` field in the JSON payload
- Include a valid `title` so the missing identifier is the relevant error
- Verify the response status is `422`

Example body:

```json
{
  "title": "missing identifier",
  "doneStatus": false,
  "description": "no id in URL or body"
}
```

### Try it now

{{<api-live-request method="PUT" path="/api/todos" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"title":"missing identifier","doneStatus":false,"description":"no id in URL or body"}' details="true" summary="PUT /api/todos without an id in the URL or payload" open="true">}}

## Lessons Learned

- `PUT /api/todos` without any `id` leaves the server unable to identify the resource to replace.
- A full representation still needs a target identity for update semantics.
- `422 Unprocessable Content` can signal missing addressing information, not just bad field data.

## Suggested Experiments

- Add only the body `id` and compare this failure with the successful `PUT /api/todos` body-id challenge.
- Send the same no-id payload to `POST /api/todos` and observe whether create semantics differ.