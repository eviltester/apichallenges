---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - POST secret note 401
seo_title: Solution: POST secret note 401 | API Challenges
description: How to solve POST /secret/note (401) by omitting the X-AUTH-TOKEN header.
seo_description: Use this walkthrough to send POST /secret/note without X-AUTH-TOKEN, verify the 401 response, and confirm the unauthorized challenge is completed.
next_challenge: /apichallenges/solutions/authorization/post-secret-note-403
schema_howto_steps: Create a POST request to /secret/note||Send a valid JSON note body||Do not include X-AUTH-TOKEN||Verify the response status is 401
showads: true
---

# How to complete the challenge `POST /secret/note (401)`

Use `POST /secret/note` with a valid JSON note payload, but do not include the `X-AUTH-TOKEN` header.

The challenger header still identifies your session, but the missing auth token means the API should return `401`.

### Try it now

{{<api-live-request method="POST" path="/secret/note" expected-status="401" headers="Accept: application/json" body='{"note":"note without token"}' details="true" summary="POST /secret/note without an auth token to trigger 401" open="true">}}
