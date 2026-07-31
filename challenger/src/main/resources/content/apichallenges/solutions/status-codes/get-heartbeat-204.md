---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - GET heartbeat 204
seo_title: Solution: GET heartbeat 204 | API Challenges
description: How to solve GET /heartbeat (204) and verify the no-content success response.
seo_description: Use this walkthrough to send GET /heartbeat and verify the 204 response.
next_challenge: /apichallenges/solutions/status-codes/x-challenger-too-long-431
schema_howto_steps: Create a GET request to /heartbeat||Include X-CHALLENGER so progress is tracked||Send the request||Verify the response status is 204 and the body is empty
showads: true
---

# How to complete the challenge `GET /heartbeat (204)`

Send a `GET` request to `/heartbeat`.

A `204 No Content` response means the request succeeded and the response body should be empty.

### Try it now

{{<api-live-request method="GET" path="/heartbeat" expected-status="204" headers="Accept: */*" details="true" summary="GET /heartbeat to receive 204 with no body" open="true">}}
