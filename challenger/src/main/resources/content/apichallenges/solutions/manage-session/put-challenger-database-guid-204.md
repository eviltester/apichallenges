---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT challenger database guid Update
seo_title: Solution: PUT challenger database guid Update | API Challenges
description: How to solve PUT /api/challenger/database/guid (Update) by restoring todo data from a saved database payload.
seo_description: Use this walkthrough to PUT saved todo database JSON to /api/challenger/database/{guid} and verify the 204 response.
next_challenge: /apichallenges/solutions/mix-accept-content/post-xml-accept-json
concepts_learned: HTTP PUT||204 No Content||X-CHALLENGER||session state
concept_summary: Use this challenge to learn how challenge progress can be restored, updated, or rejected using session identifiers.
concept_reference_label: API Testing Concepts and Coverage
concept_reference_url: /reference/testing-apis
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: GET your challenger database JSON||Send PUT /api/challenger/database/{guid} using that JSON as the body||Use Content-Type application/json||Verify the response status is 204
showads: true
---


# How to complete the challenge `PUT /api/challenger/database/guid (Update)`

First retrieve your todo database with `GET /api/challenger/database/{guid}`.

Then send that JSON payload to `PUT /api/challenger/database/{guid}` with `Content-Type: application/json`. A successful restore returns `204 No Content`.

### Try it now

If you want to inspect the todo database payload first, get it with `GET /api/challenger/database/{guid}`. [See the solution](/apichallenges/solutions/manage-session/get-challenger-database-guid-200).

{{<api-live-request method="GET" path="/api/challenger/database/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/challenger/database/{guid} to get the todo database payload">}}

{{<api-live-request method="PUT" path="/api/challenger/database/{{currentChallenger}}" expected-status="204" headers="Content-Type: application/json||Accept: application/json" body='{{currentTodosJson}}' details="true" summary="PUT /api/challenger/database/{guid} to restore the todo database" open="true">}}

## Lessons Learned

- `PUT /api/challenger/database/{guid}` restores todo data from a previously exported database payload.
- `204 No Content` indicates the restore was accepted without returning the restored dataset.
- State restore tests need a follow-up `GET /api/todos` to prove the body took effect.

## Suggested Experiments

- Change todo data, restore the database export with `PUT`, then verify individual todo ids returned.
- Remove one todo from the saved database payload and observe how the restore changes the collection.