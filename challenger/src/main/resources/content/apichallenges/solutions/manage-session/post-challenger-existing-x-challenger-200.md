---
date:  2026-07-31T11:30:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - POST challenger existing X-CHALLENGER
seo_title: Solution: POST challenger existing X-CHALLENGER | API Challenges
description: How to solve POST /challenger with an existing X-CHALLENGER header.
seo_description: Use this walkthrough to restore a saved challenger using POST /challenger and an existing X-CHALLENGER header.
next_challenge: /apichallenges/solutions/manage-session/get-challenger-guid-existing-x-challenger-200
schema_howto_steps: Start with a saved challenger GUID||Send POST /challenger with that GUID in X-CHALLENGER||Verify the response status is 200||Use the restored challenger for later requests
showads: true
---

# How to complete the challenge `POST /challenger (existing X-CHALLENGER)`

This persistence challenge restores a saved challenger by sending a `POST` request to `/challenger` with the existing challenger GUID in the `X-CHALLENGER` header.

It differs from creating a new challenger because the request includes an existing `X-CHALLENGER` value and expects the server to restore that challenger rather than create a new one.

### Try it now

{{<api-live-request method="POST" path="/challenger" expected-status="200" headers="Accept: application/json" details="true" summary="POST /challenger with X-CHALLENGER to resume the current challenger" open="true">}}
