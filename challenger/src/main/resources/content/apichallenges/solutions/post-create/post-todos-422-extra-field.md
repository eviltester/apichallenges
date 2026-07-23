---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-20
title: API Challenges Solution For - POST todos 422 - extra field
seo_title: Solution POST todos 422 extra field | API Challenges
description: How to solve API challenge POST todos 422 extra field.
seo_description: Use this walkthrough to solve POST todos 422 extra field with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/put-create/put-todos-422-create
schema_howto_steps: Create a POST request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a valid JSON payload with an unsupported extra field||Send the request and verify the response status is 422
showads: true
---

# How to complete the challenge `POST /todos (422) extra`

Issue a `POST` request to `/todos` with a field that is not part of the todo schema.

```json
{
  "title": "create new todo",
  "doneStatus": false,
  "description": "created via API Challenges",
  "extra": "not part of the schema"
}
```

The response should be `422 Unprocessable Content` because the payload is syntactically valid, but the todo data cannot be processed as a todo.
### Try it now

{{<api-live-request method="POST" path="/todos" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"title":"extra field","doneStatus":true,"description":"created from the solution page","priority":"high"}'>}}
