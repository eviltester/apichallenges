---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - PUT challenger database guid Update
seo_title: Solution: PUT challenger database guid Update | API Challenges
description: How to solve PUT /challenger/database/guid (Update) by restoring todo data from a saved database payload.
seo_description: Use this walkthrough to PUT saved todo database JSON to /challenger/database/{guid} and verify the 204 response.
next_challenge: /apichallenges/solutions/mix-accept-content/post-xml-accept-json
schema_howto_steps: GET your challenger database JSON||Send PUT /challenger/database/{guid} using that JSON as the body||Use Content-Type application/json||Verify the response status is 204
showads: true
---

# How to complete the challenge `PUT /challenger/database/guid (Update)`

First retrieve your todo database with `GET /challenger/database/{guid}`.

Then send that JSON payload to `PUT /challenger/database/{guid}` with `Content-Type: application/json`. A successful restore returns `204 No Content`.

### Try it now

If you want to inspect the todo database payload first, get it with `GET /challenger/database/{guid}`. [See the solution](/apichallenges/solutions/manage-session/get-challenger-database-guid-200).

{{<api-live-request method="GET" path="/challenger/database/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /challenger/database/{guid} to get the todo database payload">}}

{{<api-live-request method="PUT" path="/challenger/database/{{currentChallenger}}" expected-status="204" headers="Content-Type: application/json||Accept: application/json" body='{{currentTodosJson}}' details="true" summary="PUT /challenger/database/{guid} to restore the todo database" open="true">}}
