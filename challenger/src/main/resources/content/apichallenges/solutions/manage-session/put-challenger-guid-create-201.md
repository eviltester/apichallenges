---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - PUT challenger guid CREATE
seo_title: Solution: PUT challenger guid CREATE | API Challenges
description: How to solve PUT /challenger/guid CREATE by restoring a challenger that is not currently in memory.
seo_description: Use this walkthrough to create a challenger in memory from saved challenger progress using PUT /challenger/{guid}.
next_challenge: /apichallenges/solutions/manage-session/get-challenger-database-guid-200
schema_howto_steps: Start with saved challenger progress JSON||Use a challenger GUID that is not currently in memory||PUT the saved JSON to /challenger/{guid}||Verify the response status shows the challenger was created
showads: true
---

# How to complete the challenge `PUT /challenger/guid CREATE`

This challenge is like the restore challenge, except the challenger should not already be active in memory.

Use the saved challenger progress JSON as the request body and send it to `PUT /challenger/{guid}`. If the GUID is restorable but not currently active, the API creates that challenger in memory from the payload.

### Try it now

If you want to inspect the challenger payload shape first, get it with `GET /challenger/{guid}`. [See the solution](/apichallenges/solutions/manage-session/get-challenger-guid-existing-x-challenger-200).

{{<api-live-request method="GET" path="/challenger/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /challenger/{guid} to get the challenger payload">}}

The main request below uses a restorable copy of that payload with a different GUID.

{{<api-live-request method="PUT" path="/challenger/{{restoredChallenger}}" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{{currentChallengerJsonForRestoredChallenger}}' details="true" summary="PUT /challenger/{guid} to create a restorable challenger" open="true">}}
