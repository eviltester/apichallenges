---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST secret note 403
seo_title: Solution: POST secret note 403 | API Challenges
description: How to solve POST /secret/note (403) by sending an invalid X-AUTH-TOKEN value.
seo_description: Use this walkthrough to send POST /secret/note with a wrong X-AUTH-TOKEN, verify the 403 response, and confirm the forbidden challenge result.
next_challenge: /apichallenges/solutions/authorization/get-secret-note-bearer
concepts_learned: HTTP POST||403 Forbidden||authorization||invalid auth token
concept_summary: Use this challenge to learn how protected resources respond when authorization uses invalid auth token.
concept_reference_label: REST API Basics
concept_reference_url: /reference/rest-api-basics
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a POST request to /secret/note||Send a valid JSON note body||Include an X-AUTH-TOKEN header with an invalid value||Verify the response status is 403
showads: true
---


# How to complete the challenge `POST /secret/note (403)`

Use `POST /secret/note` with a valid JSON note payload and include an `X-AUTH-TOKEN` header, but make the token value invalid.

The API can see an auth token was supplied, but it does not match the current challenger, so the response should be `403`.

### Try it now

{{<api-live-request method="POST" path="/secret/note" expected-status="403" headers="X-AUTH-TOKEN: wrong-token||Accept: application/json" body='{"note":"note with wrong token"}' details="true" summary="POST /secret/note with a wrong auth token to trigger 403" open="true">}}

## Lessons Learned

- A wrong `X-AUTH-TOKEN` blocks the write even when the note payload is otherwise valid.
- `403 Forbidden` protects existing secret note state from tokens that look present but are not trusted.
- Testing invalid authorization should include confirming that state did not change afterward.

## Suggested Experiments

- Try to write with a fake token, then read with a real token to check the note stayed unchanged.
- Use a valid token from a different `X-CHALLENGER` and compare it with a completely made-up token.