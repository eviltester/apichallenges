---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT challenger guid CREATE
seo_title: Solution: PUT challenger guid CREATE | API Challenges
description: How to solve PUT /api/challenger/guid CREATE by restoring a challenger that is not currently in memory.
seo_description: Use this walkthrough to create a challenger in memory from saved challenger progress using PUT /api/challenger/{guid}.
next_challenge: /apichallenges/solutions/manage-session/get-challenger-database-guid-200
concepts_learned: HTTP PUT||201 Created||X-CHALLENGER||session state
concept_summary: Use this challenge to learn how challenge progress can be restored, updated, or rejected using session identifiers.
concept_reference_label: API Testing Concepts and Coverage
concept_reference_url: /reference/testing-apis
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Start with saved challenger progress JSON||Use a challenger GUID that is not currently in memory||PUT the saved JSON to /api/challenger/{guid}||Verify the response status shows the challenger was created
showads: true
---


# How to complete the challenge `PUT /api/challenger/guid CREATE`

This challenge is like the restore challenge, except the challenger should not already be active in memory.

Use the saved challenger progress JSON as the request body and send it to `PUT /api/challenger/{guid}`. If the GUID is restorable but not currently active, the API creates that challenger in memory from the payload.

### Try it now

If you want to inspect the challenger payload shape first, get it with `GET /api/challenger/{guid}`. [See the solution](/apichallenges/solutions/manage-session/get-challenger-guid-existing-x-challenger-200).

{{<api-live-request method="GET" path="/api/challenger/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/challenger/{guid} to get the challenger payload">}}

The main request below uses a restorable copy of that payload with a different GUID.

{{<api-live-request method="PUT" path="/api/challenger/{{restoredChallenger}}" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{{currentChallengerJsonForRestoredChallenger}}' details="true" summary="PUT /api/challenger/{guid} to create a restorable challenger" open="true">}}

## Lessons Learned

- `PUT /api/challenger/{guid}` can create an in-memory challenger from saved progress when the guid is not active.
- `201 Created` here refers to recreating a challenger session, not creating a todo.
- This is a useful example of `PUT` as create-or-replace for a known resource identifier.

## Suggested Experiments

- Restore the same saved challenger twice and compare first-create behavior with later restore behavior.
- Alter the saved progress body slightly and check whether the created session reflects that change.