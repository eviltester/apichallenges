---
title: API Challenges Solution For - GET Challenges (200)
seo_title: Solution: GET Challenges (200) | API Challenges
description: How to use a GET request with an x-challenger header to get the progress status of all the API Challenges
lastmod: 2026-08-06
seo_description: Use this walkthrough to solve GET Challenges (200) with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/get/get-todos-200
concepts_learned: HTTP GET||200 OK||API discovery||safe method
concept_summary: Use this challenge to learn how a safe GET request retrieves the list of available challenges.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /tutorials/http-verbs
concept_reference_label_2: REST API Basics
concept_reference_url_2: /tutorials/rest-api-basics
schema_howto_steps: Create a GET request to /challenges||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /challenges (200)`.

How to use a GET request with an x-challenger header to get the progress status of all the API Challenges.

## GET /challenges (200)

> Issue a `GET` request on the `/challenges` end point

- This will show you the status of all the challenges in your REST Client, if you include an `X-CHALLENGER` guid header in your request.

## Basic Instructions

- Issue a GET request to end point "/challenges"
    - `{{<ORIGIN_URL>}}/challenges`
- The request should have an `X-CHALLENGER` header
- The response body shows the status of all the challenges.

### Try it now

{{<api-live-request method="GET" path="/challenges" expected-status="200" headers="Accept: application/json" details="true" summary="GET /challenges to list the available challenges" open="true">}}


## Example Request

~~~~~~~~
> GET /challenges HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Thu, 27 Aug 2020 13:38:45 GMT
< Content-Type: application/json
< Location: /gui/challenges/x-challenger-guid
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Example Response Payload

~~~~~~~~
{
  "challenges": [
    {
      "name": "POST /challenger (201)",
      "description": "Issue a POST request on the `/challenger` end point, 
            with no body, to create a new challenger session. 
            Use the generated X-CHALLENGER header in 
            future requests to track challenge completion.",
      "status": true
    },
    {
      "name": "GET /challenges (200)",
      "description": "Issue a GET request on the `/challenges` end point",
      "status": true
    }
  ]
}
~~~~~~~~

## Overview Video

{{<youtube-embed key="DrAjk2NaPRo" title="Solution to Get Challenges progress">}}

[Patreon ad free version](https://www.patreon.com/posts/41106708)

## Lessons Learned

- `GET /challenges` exposes progress state rather than todo application data.
- This endpoint is a dashboard API: the response tells you what the server thinks you have completed.
- Including `X-CHALLENGER` changes the result from generic challenge info to your tracked session.

## Suggested Experiments

- Call `GET /challenges`, complete one challenge, then call it again and compare the completion flags.
- Request `/challenges` with and without `X-CHALLENGER` to see how session tracking changes the response.