---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET secret note Bearer
seo_title: Solution: GET secret note Bearer | API Challenges
description: How to solve GET /secret/note (Bearer) with an Authorization Bearer token.
seo_description: Use this walkthrough to send GET /secret/note with Authorization Bearer, check the authenticated 200 response, and confirm progress is tracked.
next_challenge: /apichallenges/solutions/authorization/post-secret-note-bearer
concepts_learned: HTTP GET||200 OK||authorization||Bearer token
concept_summary: Use this challenge to learn how protected resources respond when authorization uses Bearer token.
concept_reference_label: REST API Basics
concept_reference_url: /reference/rest-api-basics
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
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

## Lessons Learned

- `Authorization: Bearer` is the standard header form for presenting a token on protected reads.
- This challenge distinguishes framework-standard authorization from the API-specific `X-AUTH-TOKEN` header.
- The token value can be the same secret, but the header scheme changes how the server interprets it.

## Suggested Experiments

- Send the valid token as `Authorization: Bearer <token>` and then as `X-AUTH-TOKEN` to compare supported auth styles.
- Omit the `Bearer` scheme word while keeping the token value and check whether the parser rejects it.