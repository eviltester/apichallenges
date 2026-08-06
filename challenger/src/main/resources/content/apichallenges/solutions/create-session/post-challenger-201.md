---
title: API Challenges Solution For - POST Challenger (201)
seo_title: Solution: POST Challenger (201) | API Challenges
description: How to use a POST request to create a Challenger session and start using the API challenges
lastmod: 2026-08-06
seo_description: Use this walkthrough to solve POST Challenger (201) with request setup, key headers, and expected status codes so you can complete the challenge.
next_challenge: /apichallenges/solutions/first-challenge/get-challenges-200
concepts_learned: HTTP POST||201 Created||session state||X-CHALLENGER
concept_summary: Use this challenge to learn how the app creates a tracked challenge session.
concept_reference_label: API Testing Concepts and Coverage
concept_reference_url: /tutorials/testing-apis
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /tutorials/http-verbs
schema_howto_steps: Create a POST request to /challenger||Send the request with no body to create a new challenger session||Capture the returned X-CHALLENGER value for subsequent challenge requests||Send the request and verify the response status is 201
showads: true
---


# POST Challenger 201 Solution

How to complete the challenge `POST /challenger 201`.

## POST /challenger (201)

> Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion.

- This challenge is essential if you want to persist your sessions in multi-user mode
- This challenge is optional if you want to work in single-user mode

## Basic Instructions

- Issue a POST request to end point "/challenger"
   - `{{<ORIGIN_URL>}}/challenger`
- The response will have an `X-CHALLENGER` header
- Use this in any future requests to track your progress
- The `LOCATION` header has a url to access your challenge status through the GUI

### Try it now

{{<api-live-request method="POST" path="/challenger" expected-status="201" use-challenger="false" headers="Accept: application/json" details="true" summary="POST /challenger to create a new challenger session" open="true">}}


## Example Request

~~~~~~~~
POST /challenger HTTP/1.1
Host: localhost:4567
User-Agent: rest-client
Accept: */*
Content-Length: 0
~~~~~~~~

## Example Response

~~~~~~~~
HTTP/1.1 201 Created
Date: Tue, 28 Jul 2020 14:26:48 GMT
X-CHALLENGER: rest-api-challenges-single-player
Location: /gui/challenges
Content-Type: text/html;charset=utf-8
Transfer-Encoding: chunked
Server: Jetty(9.4.z-SNAPSHOT)
~~~~~~~~

## Overview Video

{{<youtube-embed key="tNGuZMQgHxw" title="Solution to create challenge session">}}

[Patreon ad free version](https://www.patreon.com/posts/39882254)

## Lessons Learned

- `POST /challenger` creates a new session resource and returns its identifier in `X-CHALLENGER`.
- Session creation is the setup step that makes later challenge completion trackable.
- The session `id` is both response data and a required request header for future work.

## Suggested Experiments

- Create two challenger sessions and compare how their `X-CHALLENGER` values isolate progress.
- Use a newly returned `X-CHALLENGER` on `GET /challenges` and confirm completion starts from a fresh state.