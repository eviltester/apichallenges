---
date:  2026-08-01T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT todos/id 200 no body id
seo_title: Solution: PUT todos/id 200 no body id | API Challenges
description: How to solve API challenge PUT todos/id no body id 200.
seo_description: Use this walkthrough to solve PUT /todos/{id} no body id 200 by using the URL id as the update identifier for a full update.
next_challenge: /apichallenges/solutions/put-update/put-todos-id-422-no-title
concepts_learned: HTTP PUT||200 OK||idempotent method||id handling
concept_summary: Use this challenge to learn how PUT handles id handling for todo resources.
concept_reference_label: HTTP PUT Verb
concept_reference_url: /reference/http-verbs/http-put
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a PUT request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send a valid JSON payload without an id field||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `PUT /todos/{id} no body id (200)`

Issue a `PUT` request to `/todos/{id}` and do not include an `id` field in the request body.

This is a common PUT design: the URL identifies the resource and the body supplies the replacement state.

## Basic Instructions

- Send `PUT /todos/{id}` where `{id}` is an existing todo id
- Add an `X-CHALLENGER` header to track challenge completion
- Set `Content-Type` to `application/json`
- Do not include an `id` field in the JSON payload
- Include a valid `title`
- Verify the response status is `200`

Example body:

```json
{
  "title": "updated using URL id",
  "doneStatus": false,
  "description": "the id is only in the URL"
}
```

### Try it now

If you don't know what todos are available then you can check by `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /todos to create a todo item for this challenge">}}

{{<api-live-request method="PUT" path="/todos/{{firstTodoId}}" expected-status="200" headers="Content-Type: application/json||Accept: application/json" body='{"title":"updated using URL id","doneStatus":false,"description":"the id is only in the URL"}' details="true" summary="PUT /todos/{id} without an id in the payload" open="true">}}

## Lessons Learned

- `PUT /todos/{id}` can use the path parameter as the update target when the body omits `id`.
- Omitting body `id` avoids one class of mismatch while still requiring a valid representation.
- The URL is part of the contract, not just routing decoration.

## Suggested Experiments

- Add a matching body `id` and compare the response with the no-body-id request.
- Change only the path `id` while keeping the body the same to see which resource is updated.