---
date:  2026-07-20T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST todos 422 - description too long
seo_title: Solution POST todos 422 description too long | API Challenges
description: How to solve API challenge POST todos 422 description too long.
seo_description: Use this walkthrough to solve POST todos 422 description too long with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/post-create/post-todos-201-max-content
concepts_learned: HTTP POST||422 Unprocessable Content||CRUD create||validation error
concept_summary: Use this challenge to learn how APIs reject invalid create request bodies.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /tutorials/http-verbs
concept_reference_label_2: REST API Basics
concept_reference_url_2: /tutorials/rest-api-basics
schema_howto_steps: Create a POST request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a description longer than 200 characters||Send the request and verify the response status is 422
showads: true
---


# How to complete the challenge `POST /todos (422) description too long`

Issue a `POST` request to `/todos` with a `description` longer than the maximum allowed length.

The response should be `422 Unprocessable Content` and include a validation error explaining that the description exceeded the maximum length.

Keep the JSON syntax valid; the point of the challenge is entity validation, not malformed JSON.
### Try it now

{{<api-live-request method="POST" path="/todos" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"title":"description too long","doneStatus":true,"description":"{{description201}}"}' details="true" summary="POST /todos with a long description to trigger 422" open="true">}}

## Lessons Learned

- A readable `JSON` body can still fail when `description` exceeds its field limit.
- Field-specific validation should point to the oversized `description`, not a generic parse problem.
- This boundary differs from the whole-request `413 Content Too Large` size limit.

## Suggested Experiments

- Send `200` description characters, then `201`, and compare success with `422 Unprocessable Content`.
- Keep `description` invalid while changing `title` to prove the reported error stays focused.