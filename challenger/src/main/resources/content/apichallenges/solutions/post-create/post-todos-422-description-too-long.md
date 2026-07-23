---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-20
title: API Challenges Solution For - POST todos 422 - description too long
seo_title: Solution POST todos 422 description too long | API Challenges
description: How to solve API challenge POST todos 422 description too long.
seo_description: Use this walkthrough to solve POST todos 422 description too long with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/post-create/post-todos-201-max-content
schema_howto_steps: Create a POST request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Send a description longer than 200 characters||Send the request and verify the response status is 422
showads: true
---

# How to complete the challenge `POST /todos (422) description too long`

Issue a `POST` request to `/todos` with a `description` longer than the maximum allowed length.

The response should be `422 Unprocessable Content` and include a validation error explaining that the description exceeded the maximum length.

Keep the JSON syntax valid; the point of the challenge is entity validation, not malformed JSON.
