---
date:  2026-07-20T09:00:00Z
lastmod: 2026-07-20
title: API Challenges Solution For - PUT challenger guid 409 mismatch
seo_title: Solution: PUT challenger guid 409 mismatch | API Challenges
description: How to solve API challenge PUT challenger guid 409 mismatch.
seo_description: Use this walkthrough to solve PUT challenger guid 409 mismatch with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/manage-session/save-restore-session
schema_howto_steps: GET your challenger state||PUT it to /challenger/{different-guid} while the payload keeps your real X-CHALLENGER||Verify the response status is 409
showads: true
---

# How to complete the challenge `PUT /challenger/guid (409) mismatch`

Get your current challenger state from `/challenger/{guid}`.

Then send that JSON payload to `PUT /challenger/{different-guid}` while leaving the payload `xChallenger` value unchanged.

The response should be `409 Conflict`:

```json
{
  "errorMessages": [
    "URL GUID does not match payload X-CHALLENGER"
  ]
}
```

This is a conflict because the URL identifies one challenger while the payload identifies another.
### Try it now

{{<api-live-request method="PUT" path="/challenger/{{mismatchedChallenger}}" expected-status="409" headers="Content-Type: application/json||Accept: application/json" body='{{currentChallengerJson}}'>}}
