---
date:  2026-08-01T09:00:00Z
lastmod: 2026-08-01
title: API Challenges Solution For - PUT todos no id 422
seo_title: Solution: PUT todos no id 422 | API Challenges
description: How to solve API challenge PUT todos no id 422.
seo_description: Use this walkthrough to solve PUT /todos no id 422 by sending a valid update payload without any identifier in the URL or body.
next_challenge: /apichallenges/solutions/put-update/put-todos-id-404-not-found
schema_howto_steps: Create a PUT request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a JSON payload without an id field||Send the request and verify the response status is 422
showads: true
---

# How to complete the challenge `PUT /todos no id (422)`

Issue a `PUT` request to `/todos` without an id in the URL or the request body.

The `/todos` endpoint exists, but the API cannot know which todo to update. Because the request reaches a valid endpoint but is missing required update information, the response should be `422 Unprocessable Content`.

## Basic Instructions

- Send `PUT /todos`
- Add an `X-CHALLENGER` header to track challenge completion
- Set `Content-Type` to `application/json`
- Do not include an `id` field in the JSON payload
- Include a valid `title` so the missing identifier is the relevant error
- Verify the response status is `422`

Example body:

```json
{
  "title": "missing identifier",
  "doneStatus": false,
  "description": "no id in URL or body"
}
```

### Try it now

{{<api-live-request method="PUT" path="/todos" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"title":"missing identifier","doneStatus":false,"description":"no id in URL or body"}' details="true" summary="PUT /todos without an id in the URL or payload" open="true">}}
