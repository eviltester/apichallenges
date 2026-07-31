---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - PUT todos/id 422
seo_title: Solution: PUT todos/id 422 Guide | API Challenges
description: How to solve API challenge PUT todos/id 422 invalid create with PUT.
seo_description: Use this walkthrough to solve PUT todos/id 422 with request setup, key headers, auto id validation, and expected status codes.
next_challenge: /apichallenges/solutions/post-update/post-todos-id-200
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
