---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST secret note 401
seo_title: Solution: POST secret note 401 | API Challenges
description: How to solve POST /api/secret/note (401) by omitting the X-AUTH-TOKEN header.
seo_description: Use this walkthrough to send POST /api/secret/note without X-AUTH-TOKEN, verify the 401 response, and confirm the unauthorized challenge is completed.
next_challenge: /apichallenges/solutions/authorization/post-secret-note-403
concepts_learned: HTTP POST||401 Unauthorized||authorization||missing auth token
concept_summary: Use this challenge to learn how protected resources respond when authorization uses missing auth token.
concept_reference_label: REST API Basics
concept_reference_url: /reference/rest-api-basics
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a POST request to /api/secret/note||Send a valid JSON note body||Do not include X-AUTH-TOKEN||Verify the response status is 401
showads: true
---


# How to complete the challenge `POST /api/secret/note (401)`

Use `POST /api/secret/note` with a valid JSON note payload, but do not include the `X-AUTH-TOKEN` header.

The challenger header still identifies your session, but the missing auth token means the API should return `401`.

### Try it now

{{<api-live-request method="POST" path="/api/secret/note" expected-status="401" headers="Accept: application/json" body='{"note":"note without token"}' details="true" summary="POST /api/secret/note without an auth token to trigger 401" open="true">}}

## Lessons Learned

- A valid request body is not enough when a protected write lacks `X-AUTH-TOKEN`.
- `401 Unauthorized` should occur before the server accepts a new secret note value.
- This challenge separates payload validation from authorization validation.

## Suggested Experiments

- Send the same `JSON` body first without `X-AUTH-TOKEN`, then with the token, and compare whether the note changes.
- Remove `Content-Type` as well as the token to see which error the API prioritizes.