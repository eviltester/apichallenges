---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - POST heartbeat as DELETE 405
seo_title: Solution: POST heartbeat as DELETE 405 | API Challenges
description: How to solve POST /heartbeat as DELETE using X-HTTP-Method-Override.
seo_description: Use this walkthrough to send POST /heartbeat with X-HTTP-Method-Override DELETE, verify the 405 response, and complete the method override challenge.
next_challenge: /apichallenges/solutions/method-override/post-heartbeat-as-patch-500
schema_howto_steps: Create a POST request to /heartbeat||Add X-HTTP-Method-Override DELETE||Include X-CHALLENGER so progress is tracked||Verify the response status is 405
showads: true
---

# How to complete the challenge `POST /heartbeat as DELETE (405)`

Send a normal `POST` request to `/heartbeat`, but add this header:

```http
X-HTTP-Method-Override: DELETE
```

The server treats the request as a DELETE and returns `405`.

### Try it now

{{<api-live-request method="POST" path="/heartbeat" expected-status="405" headers="X-HTTP-Method-Override: DELETE||Accept: */*" details="true" summary="POST /heartbeat with DELETE override to trigger 405" open="true">}}
