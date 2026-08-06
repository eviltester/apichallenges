---
date:  2026-08-01T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT todos 200 body id
seo_title: Solution: PUT todos 200 body id | API Challenges
description: How to solve API challenge PUT todos body id 200.
seo_description: Use this walkthrough to solve PUT /todos body id 200 by sending an existing todo id in the JSON payload and verifying the 200 response.
next_challenge: /apichallenges/solutions/put-update/put-todos-id-200-no-body-id
concepts_learned: HTTP PUT||200 OK||idempotent method||id handling
concept_summary: Use this challenge to learn how PUT handles id handling for todo resources.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a PUT request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a valid JSON payload with an existing todo id||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `PUT /todos body id (200)`

Issue a `PUT` request to `/todos` and include the id of an existing todo in the request body.

This API allows the identifier for a PUT update to come from the payload, so `/todos` can update a specific todo when the body contains an existing `id`.

## Basic Instructions

- Send `PUT /todos`
- Add an `X-CHALLENGER` header to track challenge completion
- Set `Content-Type` to `application/json`
- Include an `id` for an existing todo in the JSON payload
- Include a valid `title` because this API treats PUT as a replacement update
- Verify the response status is `200`

Example body:

```json
{
  "id": 3,
  "title": "updated using body id",
  "doneStatus": true,
  "description": "updated by PUT /todos"
}
```

### Try it now

If you don't know what todos are available then you can check by `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /todos to create a todo item for this challenge">}}

{{<api-live-request method="PUT" path="/todos" expected-status="200" headers="Content-Type: application/json||Accept: application/json" body='{"id":"{{firstTodoId}}","title":"updated using body id","doneStatus":true,"description":"updated by PUT /todos"}' details="true" summary="PUT /todos with an existing id in the payload" open="true">}}

## Lessons Learned

- `PUT /todos` can update when the target `id` is supplied in the body instead of the URL.
- Body-identified updates are API-specific and should be documented clearly in tests.
- The response should confirm which existing todo `id` was updated.

## Suggested Experiments

- Send the same body to `PUT /todos` twice and confirm the second call does not create a duplicate.
- Change the body `id` to a missing value and compare update success with validation failure.