---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-20
title: API Challenges Solution For - POST todos 422
seo_title: Solution: POST todos 422 Guide | API Challenges
description: How to solve API challenge POST todos 422 by sending valid JSON with invalid todo field data.
seo_description: Use this walkthrough to solve POST todos 422 with request setup, key headers, invalid doneStatus data, and expected status codes.
next_challenge: /apichallenges/solutions/post-create/post-todos-422-title-too-long
schema_howto_steps: Create a POST request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send JSON with an invalid doneStatus value||Send the request and verify the response status is 422
showads: true
---

# How to complete the challenge `POST /todos (422)`

Issue a `POST` request to `/todos` with syntactically valid JSON that fails todo validation.

For this challenge, send `doneStatus` as a string rather than a boolean:

```json
{
  "title": "create new todo",
  "doneStatus": "bob",
  "description": "created via API Challenges"
}
```

The response should be `422 Unprocessable Content` because the request body can be parsed, but the todo data is not valid.

```json
{
  "errorMessages": [
    "Failed Validation: doneStatus should be BOOLEAN"
  ]
}
```

Remember to include your `X-CHALLENGER` header so the challenge is tracked.
