---
date:  2026-07-20T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST todos 422 - title too long
seo_title: Solution POST todos 422 title too long | API Challenges
description: How to solve API challenge POST todos 422 title too long.
seo_description: Use this walkthrough to solve POST todos 422 title too long with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/post-create/post-todos-422-description-too-long
concepts_learned: HTTP POST||422 Unprocessable Content||CRUD create||validation error
concept_summary: Use this challenge to learn how APIs reject invalid create request bodies.
concept_reference_label: HTTP POST Verb
concept_reference_url: /reference/http-verbs/http-post
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a POST request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a title longer than 50 characters||Send the request and verify the response status is 422
showads: true
---


# How to complete the challenge `POST /todos (422) title too long`

Issue a `POST` request to `/todos` with a `title` longer than the maximum allowed length.

```json
{
  "title": "this title has far too many characters to validate.",
  "doneStatus": true,
  "description": "should trigger a 422 error"
}
```

The response should be `422 Unprocessable Content`.

```json
{
  "errorMessages": [
    "Failed Validation: Maximum allowable length exceeded for title - maximum allowed is 50"
  ]
}
```
### Try it now

{{<api-live-request method="POST" path="/todos" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"title":"{{title51}}","doneStatus":true,"description":"created from the solution page"}' details="true" summary="POST /todos with a long title to trigger 422" open="true">}}

## Lessons Learned

- `title` has its own maximum length and should be tested separately from `description`.
- A field-length error should not depend on the rest of the payload being unusual.
- Title boundaries matter because titles are required and visible in collection summaries.

## Suggested Experiments

- Compare a `50` character `title` with a `51` character `title` while keeping `description` short.
- Test an empty `title` separately to distinguish missing or blank validation from length validation.