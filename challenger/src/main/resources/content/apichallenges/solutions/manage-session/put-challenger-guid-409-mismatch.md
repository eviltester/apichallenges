---
date:  2026-07-20T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT challenger guid 409 mismatch
seo_title: Solution: PUT challenger guid 409 mismatch | API Challenges
description: How to solve API challenge PUT challenger guid 409 mismatch.
seo_description: Use this walkthrough to solve PUT challenger guid 409 mismatch with request setup, key headers, and expected status codes.
next_challenge: /apichallenges/solutions/manage-session/put-challenger-guid-create-201
concepts_learned: HTTP PUT||409 Conflict||X-CHALLENGER||session state
concept_summary: Use this challenge to learn how challenge progress can be restored, updated, or rejected using session identifiers.
concept_reference_label: API Testing Concepts and Coverage
concept_reference_url: /tutorials/testing-apis
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /tutorials/http-basics
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

If you want to inspect the challenger payload first, get it with `GET /challenger/{guid}`. [See the solution](/apichallenges/solutions/manage-session/get-challenger-guid-existing-x-challenger-200).

{{<api-live-request method="GET" path="/challenger/{{currentChallenger}}" expected-status="200" headers="Accept: application/json" details="true" summary="GET /challenger/{guid} to get the challenger payload">}}

{{<api-live-request method="PUT" path="/challenger/{{mismatchedChallenger}}" expected-status="409" headers="Content-Type: application/json||Accept: application/json" body='{{currentChallengerJson}}' details="true" summary="PUT /challenger/{different-guid} to trigger a GUID mismatch" open="true">}}

## Lessons Learned

- `409 Conflict` exposes a mismatch between the target guid and the challenger data being restored.
- Restore endpoints need consistency checks so one session cannot be accidentally written as another.
- Conflict tests should compare identifiers in the URL, body, and `X-CHALLENGER` header.

## Suggested Experiments

- Change only the URL guid while keeping the saved payload untouched and confirm the conflict remains.
- Correct the guid mismatch and compare the successful restore response.