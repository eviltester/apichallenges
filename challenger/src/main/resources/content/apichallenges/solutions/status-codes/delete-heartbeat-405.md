---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - DELETE heartbeat 405
seo_title: Solution: DELETE heartbeat 405 | API Challenges
description: How to solve DELETE /heartbeat (405) and trigger Method Not Allowed.
seo_description: Use this walkthrough to send DELETE /heartbeat, verify the 405 response, and understand why heartbeat only supports the intended safe methods.
next_challenge: /apichallenges/solutions/status-codes/patch-heartbeat-500
schema_howto_steps: Create a DELETE request to /heartbeat||Include X-CHALLENGER so progress is tracked||Send the request||Verify the response status is 405
showads: true
---

# How to complete the challenge `DELETE /heartbeat (405)`

The `/heartbeat` endpoint exists, but it does not allow the `DELETE` method.

Send a `DELETE` request to `/heartbeat` and verify the response status is `405 Method Not Allowed`.

### Try it now

{{<api-live-request method="DELETE" path="/heartbeat" expected-status="405" headers="Accept: */*" details="true" summary="DELETE /heartbeat to trigger 405" open="true">}}
