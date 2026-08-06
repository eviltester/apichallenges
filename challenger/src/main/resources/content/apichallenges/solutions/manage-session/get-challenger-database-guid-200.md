---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET challenger database guid 200
seo_title: Solution: GET challenger database guid 200 | API Challenges
description: How to solve GET /challenger/database/guid (200) by exporting the current todo database.
seo_description: Use this walkthrough to GET the current todo database for a challenger, verify the 200 response, and save the body for later restore practice.
next_challenge: /apichallenges/solutions/manage-session/put-challenger-database-guid-204
concepts_learned: HTTP GET||200 OK||X-CHALLENGER||session state
concept_summary: Use this challenge to learn how challenge progress can be restored, updated, or rejected using session identifiers.
concept_reference_label: API Testing Concepts and Coverage
concept_reference_url: /reference/testing-apis
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Use an active challenger session||Send GET /challenger/database/{guid}||Save the returned todos database JSON||Verify the response status is 200
showads: true
---


# How to complete the challenge `GET /challenger/database/guid (200)`

Use `GET /challenger/database/{guid}` to export the todo database for your challenger session.

The `{guid}` in the URL should be your active challenger GUID. The response body can be saved and later sent back to `PUT /challenger/database/{guid}`.

### Try it now

{{<api-live-request method="GET" path="/challenger/database/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /challenger/database/{guid} to export the todo database" open="true">}}

## Lessons Learned

- `GET /challenger/database/{guid}` exports todo data rather than challenge-completion metadata.
- Database export is the fixture backup step for repeatable stateful tests.
- The returned `JSON` should be treated as data to restore later, not just something to display.

## Suggested Experiments

- Export the database, create or delete a todo, then export again and compare the two payloads.
- Restore the saved database payload and confirm the todo collection returns to the earlier state.