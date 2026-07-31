---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - PUT challenger guid RESTORE
seo_title: Solution: PUT challenger guid RESTORE | API Challenges
description: How to solve PUT /challenger/guid RESTORE by restoring challenger progress from a saved JSON payload.
seo_description: Use this walkthrough to PUT saved challenger progress back to /challenger/{guid}, verify the 200 response, and restore challenge completion.
next_challenge: /apichallenges/solutions/manage-session/put-challenger-guid-409-mismatch
schema_howto_steps: GET your challenger progress JSON||Send PUT /challenger/{guid} using that JSON as the request body||Use Content-Type application/json||Verify the response status is 200
showads: true
---

# How to complete the challenge `PUT /challenger/guid RESTORE`

First retrieve your progress with `GET /challenger/{guid}`.

Then send that JSON back to `PUT /challenger/{guid}` with `Content-Type: application/json`. When the URL GUID and payload challenger id match, the API restores the progress and returns `200`.

### Try it now

If you want to inspect the challenger payload first, get it with `GET /challenger/{guid}`. [See the solution](/apichallenges/solutions/manage-session/get-challenger-guid-existing-x-challenger-200).

{{<api-live-request method="GET" path="/challenger/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /challenger/{guid} to get the challenger payload">}}

{{<api-live-request method="PUT" path="/challenger/{{currentChallenger}}" expected-status="200" headers="Content-Type: application/json||Accept: application/json" body='{{currentChallengerJson}}' details="true" summary="PUT /challenger/{guid} to restore challenger progress" open="true">}}
