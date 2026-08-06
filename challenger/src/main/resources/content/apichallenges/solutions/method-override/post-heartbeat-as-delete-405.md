---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST heartbeat as DELETE 405
seo_title: Solution: POST heartbeat as DELETE 405 | API Challenges
description: How to solve POST /heartbeat as DELETE using X-HTTP-Method-Override.
seo_description: Use this walkthrough to send POST /heartbeat with X-HTTP-Method-Override DELETE, verify the 405 response, and complete the method override challenge.
next_challenge: /apichallenges/solutions/method-override/post-heartbeat-as-patch-500
concepts_learned: HTTP POST||method override||HTTP DELETE||405 Method Not Allowed
concept_summary: Use this challenge to learn how method override changes a POST into DELETE and still returns the underlying endpoint status.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /reference/testing-apis
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

## Lessons Learned

- `X-HTTP-Method-Override: DELETE` lets a `POST` tunnel a method that the endpoint then evaluates as `DELETE`.
- The final status comes from the effective method, not from the outer `POST`.
- Method override tests should verify both the override header and the endpoint rules.

## Suggested Experiments

- Remove `X-HTTP-Method-Override` and compare plain `POST /heartbeat` with the overridden `DELETE`.
- Change only the override value to `PATCH` and check whether the status follows `PATCH` behavior.