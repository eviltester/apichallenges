---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-20
title: API Challenges Solution For - PUT todos/id no amend id 422
seo_title: Solution: PUT todos/id no amend id 422 | API Challenges
description: How to solve API challenge PUT todos/id no amend id 422.
seo_description: Use this walkthrough to solve PUT todos/id no amend id 422 with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/delete/delete-todos-id-200
schema_howto_steps: Create a PUT request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send a body id that differs from the URL id||Send the request and verify the response status is 422
showads: true
---

# How to complete the challenge `PUT /todos/{id} no amend id (422)`

Issue a `PUT` request to an existing todo and include an `id` in the body that does not match the id in the URL.

The response should be `422 Unprocessable Content` because the API does not allow the primary key to be amended through the request body.

Look for the validation message containing:

```text
Can not amend id from
```
