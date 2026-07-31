---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - TRACE heartbeat 501
seo_title: Solution: TRACE heartbeat 501 | API Challenges
description: How to solve TRACE /heartbeat (501) and trigger Not Implemented.
seo_description: Use this walkthrough to send TRACE /heartbeat, verify the 501 response, and see how the API reports an unimplemented heartbeat method for the challenge.
next_challenge: /apichallenges/solutions/status-codes/get-heartbeat-204
schema_howto_steps: Create a TRACE request to /heartbeat||Include X-CHALLENGER so progress is tracked||Send the request||Verify the response status is 501
showads: true
---

# How to complete the challenge `TRACE /heartbeat (501)`

Send a `TRACE` request to `/heartbeat`.

Some clients hide uncommon HTTP methods. If yours does, look for a custom method option and enter `TRACE`.

### Try it now

Browser HTTP clients cannot send `TRACE` methods. The HTTP client below is closed by default because it cannot complete this challenge from the browser; try it for yourself to see.

{{<api-live-request method="TRACE" path="/heartbeat" expected-status="501" headers="Accept: */*" details="true" challenge-request="true" summary="TRACE /heartbeat to trigger 501">}}
