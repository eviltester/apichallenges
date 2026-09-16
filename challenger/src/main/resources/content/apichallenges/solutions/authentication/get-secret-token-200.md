---
date:  2021-07-24T08:30:00Z
lastmod: 2026-08-22
title: API Challenges Solution For - authentication passed 200
seo_title: Solution: authentication passed 200 | API Challenges
description: How to solve GET /api/secret/token (200) - authenticate with username and password for basic auth.
seo_description: Use this walkthrough to solve authentication passed 200 with request setup, key headers, and expected status codes so you can complete the challenge.
next_challenge: /apichallenges/solutions/authorization/get-secret-note-403
concepts_learned: HTTP GET||200 OK||Basic Auth||authentication
concept_summary: Use this challenge to learn how valid Basic Auth returns an auth token.
concept_reference_label: REST API Basics
concept_reference_url: /reference/rest-api-basics
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /api/secret/token||Add Basic Authorization with the username and password required by the challenge||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200||Capture the returned X-AUTH-TOKEN for later authorization challenges
showads: true
---


# How to complete the basic auth authentication challenge

One way of authenticating a user is through Basic Auth which requires a username and password in the Auth header.

## 	Authentication Challenge

Most of the challenges simply require the correct payload, and an X-Challenger header to track the session. The authentication challenges require an extra header, the value for which can only be obtained with a username and password.

## GET /api/secret/token (200)

> Issue a GET request on the `/api/secret/token` end point and receive 200 when Basic auth username/password is admin/password

- `GET` request means use the HTTP Verb GET
    - e.g. `GET /api/secret/token` sends to the secret token endpoint
- `Basic auth` means include the [Basic Authorization header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)
- `username/password is admin/password` the authorisation header value is base 64 encoded, and the details should  match `admin` as the username, and `password` for the password
- add the `X-CHALLENGER` header to track progress and because the authentication code we need is associated with the `X-CHALLENGER` session
- Receive a 200 response because the pre-generated session token has been retrieved to allow authorization to access the secret notes


## Basic Instructions

- Create a new request for the `/api/secret/token` end point
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/api/secret/token`
- The verb should be a `GET`
- Add a Basic Auth header by selecting "Basic" from the "Auth" tab and entering a username and password of admin/password i.e. use username "admin", password "password"
- No request body is needed
- You should receive a 200 response - meaning the token has been retrieved
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response should have an `X-AUTH-TOKEN` header which you will include in the messages for `GET /api/secret/note (200)`, `POST /api/secret/note (200)`, `GET /api/secret/note (Bearer)`, and `POST /api/secret/note (Bearer)`

### Try it now

{{<api-live-request method="GET" path="/api/secret/token" expected-status="200" headers="Authorization: Basic YWRtaW46cGFzc3dvcmQ=||Accept: */*" details="true" summary="GET /api/secret/token with valid credentials to retrieve an auth token" open="true">}}


## Example Request

~~~~~~~~
> GET /api/secret/token HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Authorization: Basic YWRtaW46cGFzc3dvcmQ=
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sat, 24 Jul 2021 12:06:09 GMT
< X-AUTH-TOKEN: d432f0a3-a81b-4fc8-8e89-24848cc27f34
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Content-Type: application/json
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur

{"token":"d432f0a3-a81b-4fc8-8e89-24848cc27f34"}
~~~~~~~~

## Basic Auth uses Base64 Encoding

The `Authorization` header does not send the username and password in plain text, it uses Base64 to obscure the details.

You could see that "admin:password" converts to the Base64 string `YWRtaW46cGFzc3dvcmQ=` by using a Base64 decoder/encoder like https://www.base64decode.org/

Or you could decode it in the browser dev console by typing:

```javascript
atob('YWRtaW46cGFzc3dvcmQ=')
```

The command to encode a string as base64 is `btoa`

## Extras

- try creating a base64 Authorization header by hand, without using the "Auth" tab in Insomnia


## Lessons Learned

- `Basic Auth` sends username and password as `Base64` encoded credentials in the `Authorization` header.
- A successful `GET /api/secret/token` retrieves the pre-generated token for later protected requests.
- The returned `X-AUTH-TOKEN` is tied to the current `X-CHALLENGER` session.

## Suggested Experiments

- Decode `YWRtaW46cGFzc3dvcmQ=` locally and confirm it represents the configured username and password pair.
- Authenticate twice in the same session and compare whether the token changes or remains stable.
