---
date:  2026-07-20T09:00:00Z
lastmod: 2026-08-01
title: API Challenges Solution For - PUT todos/id no amend id 422
seo_title: Solution: PUT todos/id no amend id 422 | API Challenges
description: How to solve API challenge PUT todos/id no amend id 422.
seo_description: Use this walkthrough to solve PUT todos/id no amend id 422 with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/delete/delete-todos-id-204
schema_howto_steps: Create a PUT request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send a body id that differs from the URL id||Send the request and verify the response status is 422
showads: true
---

# How to complete the challenge `PUT /todos/{id} no amend id (422)`

Issue a `PUT` request to an existing todo and include an `id` in the body that does not match the id in the URL.

The response should be `422 Unprocessable Content` because the API does not allow the primary key to be amended through the request body.

Look for the validation message containing:

```text
Can not amend id from
```
### Try it now

If you don't know what todos are available then you can check by `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /todos to create a todo item for this challenge">}}

{{<api-live-request method="PUT" path="/todos/{{firstTodoId}}" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"id":"{{missingTodoId}}","title":"mismatch id","doneStatus":true,"description":"created from the solution page"}' details="true" summary="PUT /todos/{id} with a mismatched body id to trigger 422" open="true">}}
