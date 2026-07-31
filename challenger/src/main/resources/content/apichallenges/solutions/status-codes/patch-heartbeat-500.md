---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - PATCH heartbeat 500
seo_title: Solution: PATCH heartbeat 500 | API Challenges
description: How to solve PATCH /heartbeat (500) and trigger the simulated server error.
seo_description: Use this walkthrough to send PATCH /heartbeat, verify the 500 response, and see how the API exposes unsupported heartbeat behavior for the challenge.
next_challenge: /apichallenges/solutions/status-codes/trace-heartbeat-501
schema_howto_steps: Create a PATCH request to /heartbeat||Include X-CHALLENGER so progress is tracked||Send the request without a body||Verify the response status is 500
showads: true
---

# How to complete the challenge `PATCH /heartbeat (500)`

Send a `PATCH` request to `/heartbeat`.

This endpoint deliberately returns `500` for PATCH so you can practise observing server-error responses during API testing.

### Try it now

{{<api-live-request method="PATCH" path="/heartbeat" expected-status="500" headers="Accept: */*" details="true" summary="PATCH /heartbeat to trigger 500" open="true">}}
