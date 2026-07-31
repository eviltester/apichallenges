---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - POST secret note Bearer
seo_title: Solution: POST secret note Bearer | API Challenges
description: How to solve POST /secret/note (Bearer) with an Authorization Bearer token.
seo_description: Use this walkthrough to send POST /secret/note with Authorization Bearer, verify the 200 response, and confirm the secret note update is tracked.
next_challenge: /apichallenges/solutions/miscellaneous/delete-all-todos
schema_howto_steps: Authenticate with POST /secret/token to obtain X-AUTH-TOKEN||Create a POST request to /secret/note||Use Authorization Bearer with that token value||Send a valid JSON note body and verify the response status is 200
showads: true
---

# How to complete the challenge `POST /secret/note (Bearer)`

First complete [`POST /secret/token (201)`](/apichallenges/solutions/authentication/post-secret-201) to obtain an `X-AUTH-TOKEN` value.

Then send `POST /secret/note` with a JSON note payload and use the token as a Bearer token in the `Authorization` header. Do not send the token as `X-AUTH-TOKEN` for this challenge.

### Try it now

If you do not already have an auth token, create one with `POST /secret/token`. [See the solution](/apichallenges/solutions/authentication/post-secret-201).

{{<api-live-request method="POST" path="/secret/token" expected-status="201" headers="Authorization: Basic YWRtaW46cGFzc3dvcmQ=||Accept: */*" details="true" summary="POST /secret/token to create an auth token">}}

{{<api-live-request method="POST" path="/secret/note" expected-status="200" headers="Authorization: Bearer {{authToken}}||Accept: application/json" body='{"note":"bearer note from solution widget"}' details="true" summary="POST /secret/note with a Bearer token to update the secret note" open="true">}}
