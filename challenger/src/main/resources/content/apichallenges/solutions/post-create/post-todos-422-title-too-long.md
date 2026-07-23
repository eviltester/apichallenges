---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-20
title: API Challenges Solution For - POST todos 422 - title too long
seo_title: Solution POST todos 422 title too long | API Challenges
description: How to solve API challenge POST todos 422 title too long.
seo_description: Use this walkthrough to solve POST todos 422 title too long with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/post-create/post-todos-422-description-too-long
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

{{<api-live-request method="POST" path="/todos" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"title":"{{title51}}","doneStatus":true,"description":"created from the solution page"}'>}}
