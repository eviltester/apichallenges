---
date:  2026-07-20T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT todos/id 422
seo_title: Solution: PUT todos/id 422 Guide | API Challenges
description: How to solve API challenge PUT todos/id 422 invalid create with PUT.
seo_description: Use this walkthrough to solve PUT todos/id 422 with request setup, key headers, auto id validation, and expected status codes.
next_challenge: /apichallenges/solutions/post-update/post-todos-id-200
concepts_learned: HTTP PUT||422 Unprocessable Content||idempotent method||create validation
concept_summary: Use this challenge to learn how PUT create attempts can fail validation when the body is incomplete.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /tutorials/http-verbs
concept_reference_label_2: REST API Basics
concept_reference_url_2: /tutorials/rest-api-basics
schema_howto_steps: Send GET /todos?_sortBy=-id to identify an id that does not exist||Create a PUT request to /todos/{id} using that missing id||Include X-CHALLENGER so the challenge is tracked in your current session||Send todo JSON with an id field that matches the missing URL id||Send the request and verify the response status is 422
showads: true
---


# How to complete the challenge `PUT /todos/{id} (422)`

Issue a `PUT` request to `/todos/{id}` using an id that does not exist.

This API does not allow creating todos with a caller-selected auto-generated id, so the valid JSON reaches the write use case but is rejected as unprocessable.

If you do not know which todo ids already exist, first send:

```http
GET /todos?_sortBy=-id
```

This lists the todos from highest id to lowest id. If you are using the public site, the URL is:

```text
https://apichallenges.eviltester.com/todos?_sortBy=-id
```

{{<api-live-request method="GET" path="/todos?_sortBy=-id" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?_sortBy=-id to identify an id that does not exist">}}

Choose an id higher than the highest returned id, then use that same missing id in the `PUT` URL and in the request body.

The response should be `422 Unprocessable Content` with this message:

```json
{
  "errorMessages": [
    "Cannot create todo with PUT due to Auto fields id"
  ]
}
```
### Try it now

{{<api-live-request method="PUT" path="/todos/{{missingTodoId}}" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"id":{{missingTodoId}},"title":"solution widget todo","doneStatus":false,"description":"created from the solution page"}' details="true" summary="PUT /todos/{id} with a matching body id to attempt creating a todo and trigger 422" open="true">}}

## Lessons Learned

- `PUT /todos/{id}` can be used for create-or-replace in some APIs, but this API rejects caller-selected auto ids.
- `422 Unprocessable Content` here comes from a business rule after the request shape is understood.
- Idempotent method semantics do not override resource-specific validation rules.

## Suggested Experiments

- Try a missing `id` with and without a matching body `id` and compare validation messages.
- Create the resource another way first, then repeat `PUT /todos/{id}` against that existing `id`.