---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-20
title: API Challenges Solution For - GET challenges 431 X-CHALLENGER too long
seo_title: Solution: GET challenges 431 X-CHALLENGER too long | API Challenges
description: How to solve API challenge GET challenges 431 X-CHALLENGER too long.
seo_description: Use this walkthrough to solve GET challenges 431 X-CHALLENGER too long with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/method-override/all-method-overrides
schema_howto_steps: Start with your real X-CHALLENGER value||Append enough characters to make the header longer than 100 characters||GET /challenges and verify the response status is 431
showads: true
---

# How to complete the challenge `GET /challenges (431) X-CHALLENGER too long`

Issue a `GET` request to `/challenges` with an `X-CHALLENGER` header longer than 100 characters.

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
