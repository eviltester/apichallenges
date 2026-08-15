---
date:  2026-07-20T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT todos/id no title 422
seo_title: Solution: PUT todos/id no title 422 | API Challenges
description: How to solve API challenge PUT todos/id no title 422.
seo_description: Use this walkthrough to solve PUT todos/id no title 422 with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/put-update/put-todos-422-no-id
concepts_learned: HTTP PUT||422 Unprocessable Content||idempotent method||field validation
concept_summary: Use this challenge to learn how PUT handles field validation for todo resources.
concept_reference_label: HTTP PUT Verb
concept_reference_url: /reference/http-verbs/http-put
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a PUT request to /api/todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Omit the mandatory title field||Send the request and verify the response status is 422
showads: true
---


# How to complete the challenge `PUT /api/todos/{id} no title (422)`

Issue a `PUT` request to an existing todo and omit the mandatory `title` field.

The response should be `422 Unprocessable Content` because the request body is valid JSON, but the replacement todo fails validation.

Look for the validation message containing:

```text
title : field is mandatory
```
### Try it now

If you don't know what todos are available then you can check by `GET /api/todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /api/todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/api/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /api/todos to create a todo item for this challenge">}}

{{<api-live-request method="PUT" path="/api/todos/{{firstTodoId}}" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"doneStatus":true,"description":"missing title"}' details="true" summary="PUT /api/todos/{id} without a title to trigger 422" open="true">}}

## Lessons Learned

- `title` is mandatory for this `PUT` update, even when the resource `id` is valid.
- Required-field validation happens after the API has identified the target resource.
- A missing required field is different from an empty or too-long value.

## Suggested Experiments

- Add `title` back without changing any other field and confirm the request becomes valid.
- Send `title` as an empty string and compare that error with the missing-title response.