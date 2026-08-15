---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET challenger guid existing X-CHALLENGER
seo_title: Solution: GET challenger guid existing X-CHALLENGER | API Challenges
description: How to solve GET /api/challenger/guid (existing X-CHALLENGER) and export challenger progress.
seo_description: Use this walkthrough to GET challenger progress as JSON for the current challenger session, verify the 200 response, and review the saved status.
next_challenge: /apichallenges/solutions/manage-session/put-challenger-guid-restore-200
concepts_learned: HTTP GET||200 OK||X-CHALLENGER||session state
concept_summary: Use this challenge to learn how challenge progress can be restored, updated, or rejected using session identifiers.
concept_reference_label: API Testing Concepts and Coverage
concept_reference_url: /reference/testing-apis
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create or use an active challenger session||Send GET /api/challenger/{guid} for that challenger||Include X-CHALLENGER so the action is tracked||Save the JSON response for later restore requests
showads: true
---


# How to complete the challenge `GET /api/challenger/guid (existing X-CHALLENGER)`

Use `GET /api/challenger/{guid}` to retrieve your current challenger progress as JSON.

The `{guid}` in the URL should match the active `X-CHALLENGER` value. The response body is the progress payload you can later send back to `PUT /api/challenger/{guid}`.

This is useful when you want to save your challenge progress outside the browser.

### Try it now

{{<api-live-request method="GET" path="/api/challenger/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/challenger/{guid} with X-CHALLENGER to get current progress" open="true">}}

## Lessons Learned

- `GET /api/challenger/{guid}` with an existing `X-CHALLENGER` exports progress for the current session.
- The URL guid and header session need to be understood separately when testing state endpoints.
- The response is useful as a portable snapshot of challenge completion.

## Suggested Experiments

- Compare the exported progress before and after completing a challenge in the same session.
- Send the same URL while changing `X-CHALLENGER` and observe which session the API reports.