---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-20
title: API Challenges Solution For - PUT todos/id no title 422
seo_title: Solution: PUT todos/id no title 422 | API Challenges
description: How to solve API challenge PUT todos/id no title 422.
seo_description: Use this walkthrough to solve PUT todos/id no title 422 with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/put-update/put-todos-id-422-no-amend-id
schema_howto_steps: Create a PUT request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Omit the mandatory title field||Send the request and verify the response status is 422
showads: true
---

# How to complete the challenge `PUT /todos/{id} no title (422)`

Issue a `PUT` request to an existing todo and omit the mandatory `title` field.

The response should be `422 Unprocessable Content` because the request body is valid JSON, but the replacement todo fails validation.

Look for the validation message containing:

```text
title : field is mandatory
```
### Try it now

{{<api-live-request method="PUT" path="/todos/{{firstTodoId}}" expected-status="422" headers="Content-Type: application/json||Accept: application/json" body='{"doneStatus":true,"description":"missing title"}'>}}
