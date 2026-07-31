---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - GET secret note Bearer
seo_title: Solution: GET secret note Bearer | API Challenges
description: How to solve GET /secret/note (Bearer) with an Authorization Bearer token.
seo_description: Use this walkthrough to send GET /secret/note with Authorization Bearer, check the authenticated 200 response, and confirm progress is tracked.
next_challenge: /apichallenges/solutions/authorization/post-secret-note-bearer
schema_howto_steps: Authenticate with POST /secret/token to obtain X-AUTH-TOKEN||Create a GET request to /secret/note||Use Authorization Bearer with that token value||Verify the response status is 200
showads: true
---

# How to complete the challenge `GET /secret/note (Bearer)`

First complete [`POST /secret/token (201)`](/apichallenges/solutions/authentication/post-secret-201) to obtain an `X-AUTH-TOKEN` value.

Then send `GET /secret/note` using that token as a Bearer token in the `Authorization` header. Do not send the token as `X-AUTH-TOKEN` for this challenge.

### Try it now

If you do not already have an auth token, create one with `POST /secret/token`. [See the solution](/apichallenges/solutions/authentication/post-secret-201).

{{<api-live-request method="POST" path="/secret/token" expected-status="201" headers="Authorization: Basic YWRtaW46cGFzc3dvcmQ=||Accept: */*" details="true" summary="POST /secret/token to create an auth token">}}

{{<api-live-request method="GET" path="/secret/note" expected-status="200" headers="Authorization: Bearer {{authToken}}||Accept: application/json" details="true" summary="GET /secret/note with a Bearer token to read the secret note" open="true">}}
