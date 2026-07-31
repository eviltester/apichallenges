---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - GET challenger guid existing X-CHALLENGER
seo_title: Solution: GET challenger guid existing X-CHALLENGER | API Challenges
description: How to solve GET /challenger/guid (existing X-CHALLENGER) and export challenger progress.
seo_description: Use this walkthrough to GET challenger progress as JSON for the current challenger session, verify the 200 response, and review the saved status.
next_challenge: /apichallenges/solutions/manage-session/put-challenger-guid-restore-200
schema_howto_steps: Create or use an active challenger session||Send GET /challenger/{guid} for that challenger||Include X-CHALLENGER so the action is tracked||Save the JSON response for later restore requests
showads: true
---

# How to complete the challenge `GET /challenger/guid (existing X-CHALLENGER)`

Use `GET /challenger/{guid}` to retrieve your current challenger progress as JSON.

The `{guid}` in the URL should match the active `X-CHALLENGER` value. The response body is the progress payload you can later send back to `PUT /challenger/{guid}`.

This is useful when you want to save your challenge progress outside the browser.

### Try it now

{{<api-live-request method="GET" path="/challenger/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /challenger/{guid} with X-CHALLENGER to get current progress" open="true">}}
