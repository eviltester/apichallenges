---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - POST heartbeat as PATCH 500
seo_title: Solution: POST heartbeat as PATCH 500 | API Challenges
description: How to solve POST /heartbeat as PATCH using X-HTTP-Method-Override.
seo_description: Use this walkthrough to send POST /heartbeat with X-HTTP-Method-Override PATCH, verify the 500 response, and complete the method override challenge.
next_challenge: /apichallenges/solutions/method-override/post-heartbeat-as-trace-501
schema_howto_steps: Create a POST request to /heartbeat||Add X-HTTP-Method-Override PATCH||Include X-CHALLENGER so progress is tracked||Verify the response status is 500
showads: true
---

# How to complete the challenge `POST /heartbeat as PATCH (500)`

Send a normal `POST` request to `/heartbeat`, but add this header:

```http
X-HTTP-Method-Override: PATCH
```

The server treats the request as a PATCH and returns the simulated `500` response.

### Try it now

{{<api-live-request method="POST" path="/heartbeat" expected-status="500" headers="X-HTTP-Method-Override: PATCH||Accept: */*" details="true" summary="POST /heartbeat with PATCH override to trigger 500" open="true">}}
