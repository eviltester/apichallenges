---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - DELETE heartbeat 405
seo_title: Solution: DELETE heartbeat 405 | API Challenges
description: How to solve DELETE /api/heartbeat (405) and trigger Method Not Allowed.
seo_description: Use this walkthrough to send DELETE /api/heartbeat, verify the 405 response, and understand why heartbeat only supports the intended safe methods.
next_challenge: /apichallenges/solutions/status-codes/patch-heartbeat-500
concepts_learned: HTTP DELETE||405 Method Not Allowed||status code||error handling
concept_summary: Use this challenge to learn how the API reports 405 Method Not Allowed for this endpoint and method.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /reference/testing-apis
schema_howto_steps: Create a DELETE request to /api/heartbeat||Include X-CHALLENGER so progress is tracked||Send the request||Verify the response status is 405
showads: true
---


# How to complete the challenge `DELETE /api/heartbeat (405)`

The `/api/heartbeat` endpoint exists, but it does not allow the `DELETE` method.

Send a `DELETE` request to `/api/heartbeat` and verify the response status is `405 Method Not Allowed`.

### Try it now

{{<api-live-request method="DELETE" path="/api/heartbeat" expected-status="405" headers="Accept: */*" details="true" summary="DELETE /api/heartbeat to trigger 405" open="true">}}

## Lessons Learned

- `DELETE /api/heartbeat` reaches a real endpoint with a disallowed method, causing `405 Method Not Allowed`.
- `405 Method Not Allowed` is different from `404 Not Found` because the resource exists but the method is not supported.
- The raw response status line is useful when teaching method-level failures.

## Suggested Experiments

- Compare `DELETE /api/heartbeat` with `GET /api/heartbeat` and inspect both raw status lines.
- Check whether the `Allow` header explains which methods `/api/heartbeat` accepts.