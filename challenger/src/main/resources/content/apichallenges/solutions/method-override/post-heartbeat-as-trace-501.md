---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST heartbeat as TRACE 501
seo_title: Solution: POST heartbeat as TRACE 501 | API Challenges
description: How to solve POST /heartbeat as TRACE using X-HTTP-Method-Override.
seo_description: Use this walkthrough to send POST /heartbeat with X-HTTP-Method-Override TRACE, verify the 501 response, and complete the method override challenge.
next_challenge: /apichallenges/solutions/authentication/post-secret-401
concepts_learned: HTTP POST||method override||HTTP TRACE||501 Not Implemented
concept_summary: Use this challenge to learn how method override changes a POST into TRACE and still returns the underlying endpoint status.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /tutorials/http-verbs
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /tutorials/testing-apis
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

Browser HTTP clients cannot send `TRACE` methods. The HTTP client below is closed by default because it cannot complete this challenge from the browser; try it for yourself to see.

{{<api-live-request method="POST" path="/heartbeat" expected-status="501" headers="X-HTTP-Method-Override: TRACE||Accept: */*" details="true" challenge-request="true" summary="POST /heartbeat with TRACE override to trigger 501">}}

## Lessons Learned

- `X-HTTP-Method-Override: TRACE` demonstrates how unsupported effective methods are reported.
- `501 Not Implemented` should be tied to `TRACE`, not the carrier `POST`.
- Browser clients often restrict `TRACE`, so override support can make the test possible.

## Suggested Experiments

- Compare direct `TRACE /heartbeat` with the override version if your client allows direct `TRACE`.
- Change the override header case or spacing and see how strict the parser is.