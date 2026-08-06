---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET challenger guid 200
seo_title: Solution: GET challenger guid 200 | API Challenges
description: How to solve GET /challenger/guid (200) by restoring an existing saved challenger from persistence.
seo_description: Use this walkthrough to restore a saved challenger with GET /challenger/{guid}, verify the 200 response, and confirm the active session changes.
next_challenge: /apichallenges/solutions/manage-session/post-challenger-existing-x-challenger-200
concepts_learned: HTTP GET||200 OK||X-CHALLENGER||session state
concept_summary: Use this challenge to learn how challenge progress can be restored, updated, or rejected using session identifiers.
concept_reference_label: API Testing Concepts and Coverage
concept_reference_url: /tutorials/testing-apis
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /tutorials/http-basics
schema_howto_steps: Create a challenger session||Allow or arrange for that saved challenger to be absent from memory||Send GET /challenger/{guid} with the saved challenger id||Verify the response status is 200
showads: true
---


# How to complete the challenge `GET /challenger/guid (200)`

This challenge applies when challenger persistence is enabled. Use the saved challenger GUID in the URL so the server can restore that challenger's progress from persistence into memory.

The request path is `/challenger/{guid}`. Replace `{guid}` with the saved challenger id.

For active sessions, the same endpoint also returns challenger progress data. For this persistence challenge, the important behaviour is that the challenger id can be restored and the response status is `200`.

### Try it now

{{<api-live-request method="GET" path="/challenger/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /challenger/{guid} to get challenger progress" open="true">}}

## Lessons Learned

- `GET /challenger/{guid}` can restore a saved challenger into memory when persistence has it available.
- This route makes a `GET` do more than a normal safe read, so it is worth noting the API-specific behavior.
- Restored sessions should bring back challenge progress, not just the identifier.

## Suggested Experiments

- Save progress, restart or clear memory if possible, then use `GET /challenger/{guid}` to observe restoration.
- Request a made-up challenger guid and compare the error with a real saved guid.