---
date:  2026-07-20T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST todos 422 - extra field
seo_title: Solution POST todos 422 extra field | API Challenges
description: How to solve API challenge POST todos 422 extra field.
seo_description: Use this walkthrough to solve POST todos 422 extra field with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/put-create/put-todos-422-create
concepts_learned: HTTP POST||422 Unprocessable Content||CRUD create||validation error
concept_summary: Use this challenge to learn how APIs reject invalid create request bodies.
concept_reference_label: HTTP POST Verb
concept_reference_url: /reference/http-verbs/http-post
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a POST request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a valid JSON payload with an unsupported extra field||Send the request and verify the response status is 422
showads: true
---


# How to complete the challenge `POST /api/todos (422) extra`

Issue a `POST` request to `/api/todos` with a field that is not part of the todo schema.

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

{{<api-live-request method="POST" path="/api/todos" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"title":"extra field","doneStatus":true,"description":"created from the solution page","priority":"high"}' details="true" summary="POST /api/todos with an extra field to trigger 422" open="true">}}

## Lessons Learned

- Strict request-body validation can reject fields that are syntactically valid `JSON` but outside the schema.
- Extra-field tests reveal whether the API ignores unknown data or treats it as a client error.
- `422 Unprocessable Content` here is about contract enforcement, not media type.

## Suggested Experiments

- Add one unknown property, then add two, and compare whether the error identifies all unexpected fields.
- Remove only the extra property and confirm the same payload becomes a successful create.