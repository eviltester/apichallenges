---
date:  2026-07-20T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET heartbeat 431 X-CHALLENGER too long
seo_title: Solution: GET heartbeat 431 X-CHALLENGER too long | API Challenges
description: How to solve API challenge GET heartbeat 431 X-CHALLENGER too long.
seo_description: Use this walkthrough to solve GET heartbeat 431 X-CHALLENGER too long with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/method-override/post-heartbeat-as-delete-405
concepts_learned: HTTP GET||431 Request Header Fields Too Large||request headers||boundary testing
concept_summary: Use this challenge to learn how APIs can reject request headers that are too large.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /reference/testing-apis
schema_howto_steps: Start with your real X-CHALLENGER value||Append enough characters to make the header longer than 100 characters||GET /api/heartbeat and verify the response status is 431
showads: true
---


# How to complete the challenge `GET /api/heartbeat (431) X-CHALLENGER too long`

Issue a `GET` request to `/api/heartbeat` with an `X-CHALLENGER` header longer than 100 characters.

To have the challenge tracked, start the header value with your real challenger GUID, then append extra characters.

Example:

```text
X-CHALLENGER: your-real-guid-followed-by-extra-characters-to-exceed-the-limit
```

The response should be `431 Request Header Fields Too Large`.

```json
{
  "errorMessages": [
    "X-CHALLENGER header is too large, maximum allowed is 100 characters"
  ]
}
```
### Try it now

{{<api-live-request method="GET" path="/api/heartbeat" expected-status="431" headers="X-CHALLENGER: {{oversizedChallenger}}||Accept: application/json" use-challenger="false" details="true" summary="GET /api/heartbeat with an oversized X-CHALLENGER to trigger 431" open="true">}}

## Lessons Learned

- `431 Request Header Fields Too Large` is a header-size failure, not an application payload error.
- The `X-CHALLENGER` boundary shows that tracking headers need validation like user data.
- Header-limit tests should change only header length so the cause stays obvious.

## Suggested Experiments

- Increase the `X-CHALLENGER` value one chunk at a time to find the exact length boundary.
- Compare oversized `X-CHALLENGER` with an oversized todo body to distinguish `431 Request Header Fields Too Large` from `413 Content Too Large`.