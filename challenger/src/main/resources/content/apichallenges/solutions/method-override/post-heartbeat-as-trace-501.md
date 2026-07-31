---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - POST heartbeat as TRACE 501
seo_title: Solution: POST heartbeat as TRACE 501 | API Challenges
description: How to solve POST /heartbeat as TRACE using X-HTTP-Method-Override.
seo_description: Use this walkthrough to send POST /heartbeat with X-HTTP-Method-Override TRACE and verify the 501 response.
next_challenge: /apichallenges/solutions/authentication/post-secret-401
schema_howto_steps: Create a POST request to /heartbeat||Add X-HTTP-Method-Override TRACE||Include X-CHALLENGER so progress is tracked||Verify the response status is 501
showads: true
---

# How to complete the challenge `POST /heartbeat as Trace (501)`

Send a normal `POST` request to `/heartbeat`, but add this header:

```http
X-HTTP-Method-Override: TRACE
```

The server treats the request as TRACE and returns `501 Not Implemented`.

### Try it now

{{<api-live-request method="POST" path="/heartbeat" expected-status="501" headers="X-HTTP-Method-Override: TRACE||Accept: */*" details="true" summary="POST /heartbeat with TRACE override to trigger 501" open="true">}}
