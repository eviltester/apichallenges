---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST heartbeat as PATCH 500
seo_title: Solution: POST heartbeat as PATCH 500 | API Challenges
description: How to solve POST /heartbeat as PATCH using X-HTTP-Method-Override.
seo_description: Use this walkthrough to send POST /heartbeat with X-HTTP-Method-Override PATCH, verify the 500 response, and complete the method override challenge.
next_challenge: /apichallenges/solutions/method-override/post-heartbeat-as-trace-501
concepts_learned: HTTP POST||method override||HTTP PATCH||500 Internal Server Error
concept_summary: Use this challenge to learn how method override changes a POST into PATCH and still returns the underlying endpoint status.
concept_reference_label: HTTP POST Verb
concept_reference_url: /reference/http-verbs/http-post
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /reference/testing-apis
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

## Lessons Learned

- `X-HTTP-Method-Override: PATCH` can reach server behavior that a client or proxy might otherwise block.
- The `500 Internal Server Error` result is deliberately about the effective `PATCH /heartbeat`.
- Override handling is a good place to inspect logs or raw responses because the outer method is misleading.

## Suggested Experiments

- Send direct `PATCH /heartbeat`, then `POST /heartbeat` with the override, and compare status lines.
- Use an invalid override value and observe whether the API rejects the header or ignores it.