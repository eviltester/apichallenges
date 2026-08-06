---
date:  2026-07-20T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST todos 422
seo_title: Solution: POST todos 422 Guide | API Challenges
description: How to solve API challenge POST todos 422 by sending valid JSON with invalid todo field data.
seo_description: Use this walkthrough to solve POST todos 422 with request setup, key headers, invalid doneStatus data, and expected status codes.
next_challenge: /apichallenges/solutions/post-create/post-todos-422-title-too-long
concepts_learned: HTTP POST||422 Unprocessable Content||CRUD create||validation error
concept_summary: Use this challenge to learn how APIs reject invalid create request bodies.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
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
### Try it now

{{<api-live-request method="POST" path="/todos" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"title":"invalid doneStatus","doneStatus":"truthy","description":"created from the solution page"}' details="true" summary="POST /todos with invalid doneStatus to trigger 422" open="true">}}

## Lessons Learned

- `doneStatus` must be a boolean value, not a string or arbitrary token.
- Type validation errors are different from missing-field and length errors.
- `422 Unprocessable Content` can describe semantically invalid data even when `JSON` syntax is correct.

## Suggested Experiments

- Send `"doneStatus":"true"` and then `"doneStatus":true` to compare string versus boolean handling.
- Omit `doneStatus` entirely and see whether the defaulting behavior differs from invalid type handling.