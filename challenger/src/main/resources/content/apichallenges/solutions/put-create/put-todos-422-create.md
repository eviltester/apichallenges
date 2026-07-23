---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-20
title: API Challenges Solution For - PUT todos/id 422
seo_title: Solution: PUT todos/id 422 Guide | API Challenges
description: How to solve API challenge PUT todos/id 422 invalid create with PUT.
seo_description: Use this walkthrough to solve PUT todos/id 422 with request setup, key headers, auto id validation, and expected status codes.
next_challenge: /apichallenges/solutions/post-update/post-todos-id-200
schema_howto_steps: Create a PUT request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send valid todo JSON without an id field||Send the request and verify the response status is 422
showads: true
---

# How to complete the challenge `PUT /todos/{id} (422)`

Issue a `PUT` request to `/todos/{id}` using an id that does not exist.

This API does not allow creating todos with a caller-selected auto-generated id, so the valid JSON reaches the write use case but is rejected as unprocessable.

The response should be `422 Unprocessable Content` with this message:

```json
{
  "errorMessages": [
    "Cannot create todo with PUT due to Auto fields id"
  ]
}
```
### Try it now

{{<api-live-request method="PUT" path="/todos/11" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"title":"solution widget todo","doneStatus":false,"description":"created from the solution page"}'>}}
