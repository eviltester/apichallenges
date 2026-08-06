---
date:  2026-07-31T11:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - TRACE heartbeat 501
seo_title: Solution: TRACE heartbeat 501 | API Challenges
description: How to solve TRACE /heartbeat (501) and trigger Not Implemented.
seo_description: Use this walkthrough to send TRACE /heartbeat, verify the 501 response, and see how the API reports an unimplemented heartbeat method for the challenge.
next_challenge: /apichallenges/solutions/status-codes/get-heartbeat-204
concepts_learned: HTTP TRACE||501 Not Implemented||status code||error handling
concept_summary: Use this challenge to learn how the API reports 501 Not Implemented for this endpoint and method.
concept_reference_label: HTTP Basics
concept_reference_url: /tutorials/http-basics
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /tutorials/testing-apis
schema_howto_steps: Create a TRACE request to /heartbeat||Include X-CHALLENGER so progress is tracked||Send the request||Verify the response status is 501
showads: true
---


# How to complete the challenge `TRACE /heartbeat (501)`

Send a `TRACE` request to `/heartbeat`.

Some clients hide uncommon HTTP methods. If yours does, look for a custom method option and enter `TRACE`.

### Try it now

Browser HTTP clients cannot send `TRACE` methods. The HTTP client below is closed by default because it cannot complete this challenge from the browser; try it for yourself to see.

{{<api-live-request method="TRACE" path="/heartbeat" expected-status="501" headers="Accept: */*" details="true" challenge-request="true" summary="TRACE /heartbeat to trigger 501">}}

## Lessons Learned

- `TRACE /heartbeat` exercises an uncommon method that many clients or browsers restrict.
- `501 Not Implemented` means the server does not implement the requested method behavior.
- Client tooling can be part of the test risk when a method is not widely exposed.

## Suggested Experiments

- Try `TRACE /heartbeat` in two different clients and compare whether the request can be sent at all.
- Use method override to simulate `TRACE` and compare with direct `TRACE` behavior.