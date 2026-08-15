---
date:  2021-07-25T08:45:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET authorized secret note 200
seo_title: Solution: GET authorized secret note 20 | API Challenges
description: How to solve GET /api/secret/note (200) - authorized to access secret note
seo_description: Use this walkthrough to solve GET authorized secret note with request setup, key headers, and expected status codes so you can complete the challenge.
next_challenge: /apichallenges/solutions/authorization/post-secret-note-200
concepts_learned: HTTP GET||200 OK||authorization||X-AUTH-TOKEN
concept_summary: Use this challenge to learn how protected resources respond when authorization uses X-AUTH-TOKEN.
concept_reference_label: REST API Basics
concept_reference_url: /reference/rest-api-basics
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /api/secret/note||Add the X-AUTH-TOKEN header from your authenticated challenger session||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200
showads: true
---


# How to complete the GET Authorized secret note challenge

To access the secret note we need to be Authenticated and Authorized, only then can we GET protected information.
The API Challenge returns a status code of 200 and the secret note when we are authorized to do so.

## 	Authorization Challenge

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password. This value is obtained when completing [`POST /api/secret/token (201)`](/apichallenges/solutions/authentication/post-secret-201).

The `X-CHALLENGER` header authenticates you to access a specific set of secret notes, and the `X-AUTH-TOKEN` authorizes you to gain access.

- Authentication is "are you who you say you are" (`X-CHALLENGER`)
- Authorization is "do you have the right permissions" (`X-AUTH-TOKEN`)


## GET /api/secret/note (200)

> Issue a GET request on the `/api/secret/note` end point receive 200 when valid X-AUTH-TOKEN used - response body should contain the note

- `GET` request means use the HTTP Verb GET
    - e.g. `GET /api/secret/note` sends to the secret note endpoint
- `valid X-AUTH-TOKEN used` means a custom header named `X-AUTH-TOKEN` should be added to the message with the value received from the `POST /api/secret/token (201)` response
- add the `X-CHALLENGER` header to track progress
- Receive a 200 response because both `X-CHALLENGER` and `X-AUTH-TOKEN` are for the same user. The Response should contain the content of the secret note.

## Basic Instructions

- Create a new request for the `/api/secret/note` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/api/secret/note`
- The verb should be a `GET`
- Ensure there is a custom header with the name `X-AUTH-TOKEN` and the value is the same as received in the `/api/secret/token` response
- The request should have an `X-CHALLENGER` header to track challenge completion
- You should receive a 200 response and the body of the response will contain the secret note

### Try it now

If you do not already have an auth token, create one with `POST /api/secret/token`. [See the solution](/apichallenges/solutions/authentication/post-secret-201).

{{<api-live-request method="POST" path="/api/secret/token" expected-status="201" headers="Authorization: Basic YWRtaW46cGFzc3dvcmQ=||Accept: */*" details="true" summary="POST /api/secret/token to create an auth token">}}

{{<api-live-request method="GET" path="/api/secret/note" expected-status="200" headers="X-AUTH-TOKEN: {{authToken}}||Accept: application/json" details="true" summary="GET /api/secret/note with X-AUTH-TOKEN to read the secret note" open="true">}}


## Example Request

~~~~~~~~
> GET /api/secret/note HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: insomnia/2021.2.2
> X-CHALLENGER: x-challenger-guid
> X-AUTH-TOKEN: x-auth-token-value
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sun, 25 Jul 2021 11:02:17 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Example body of the response:

```javascript
{
  "note": "my note edited"
}
```

## Overview Video

{{<youtube-embed key="2uRpzr2OmEY" title="Solution to Get authorization challenge using header">}}

[Patreon ad free version](https://www.patreon.com/posts/54089625)

## Lessons Learned

- `GET /api/secret/note` uses a valid `X-AUTH-TOKEN` to read protected state.
- The custom token header proves permission to read, while `X-CHALLENGER` identifies the session data.
- A `200 OK` protected read should return note content without modifying it.

## Suggested Experiments

- Read the note, update it with a valid `POST`, then read it again to prove the token can access changed state.
- Move the same token into `Authorization: Bearer` and compare which authorization challenge is completed.