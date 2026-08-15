---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST challenger existing X-CHALLENGER
seo_title: Solution: POST challenger existing X-CHALLENGER | API Challenges
description: How to solve POST /api/challenger with an existing X-CHALLENGER header.
seo_description: Use this walkthrough to restore a saved challenger using POST /api/challenger and an existing X-CHALLENGER header.
next_challenge: /apichallenges/solutions/manage-session/get-challenger-guid-existing-x-challenger-200
concepts_learned: HTTP POST||200 OK||X-CHALLENGER||session state
concept_summary: Use this challenge to learn how challenge progress can be restored, updated, or rejected using session identifiers.
concept_reference_label: API Testing Concepts and Coverage
concept_reference_url: /reference/testing-apis
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Start with a saved challenger GUID||Send POST /api/challenger with that GUID in X-CHALLENGER||Verify the response status is 200||Use the restored challenger for later requests
showads: true
---


# How to complete the challenge `POST /api/challenger (existing X-CHALLENGER)`

This persistence challenge restores a saved challenger by sending a `POST` request to `/api/challenger` with the existing challenger GUID in the `X-CHALLENGER` header.

It differs from creating a new challenger because the request includes an existing `X-CHALLENGER` value and expects the server to restore that challenger rather than create a new one.

### Try it now

{{<api-live-request method="POST" path="/api/challenger" expected-status="200" headers="Accept: application/json" details="true" summary="POST /api/challenger with X-CHALLENGER to resume the current challenger" open="true">}}

## Lessons Learned

- `POST /api/challenger` with an existing `X-CHALLENGER` restores that session instead of creating a fresh one.
- `200 OK` signals reuse or restoration, contrasting with `201 Created` for new sessions.
- Session bootstrap endpoints can be sensitive to whether a header is present.

## Suggested Experiments

- Call `POST /api/challenger` with no `X-CHALLENGER`, then with an existing one, and compare status plus headers.
- Try an unknown `X-CHALLENGER` value and see whether the API restores, creates, or rejects it.