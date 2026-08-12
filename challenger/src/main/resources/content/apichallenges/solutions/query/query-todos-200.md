---
title: API Challenges Solution For - QUERY todos 200
seo_title: Solution: QUERY todos 200 Body Filter | API Challenges
description: How to solve API challenge QUERY todos 200 using form-encoded query content in the request body.
lastmod: 2026-08-11
seo_description: Use this walkthrough to solve QUERY todos 200 with request setup, headers, body content, and expected status code.
next_challenge: /apichallenges/solutions/query/query-todos-200-jsonpath
concepts_learned: HTTP QUERY||200 OK||safe method||request body query
concept_summary: Use this challenge to learn how QUERY can send query criteria in the request body without changing state.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a QUERY request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Content-Type to application/x-www-form-urlencoded||Send doneStatus=true in the request body and verify the response status is 200
showads: true
---


# How to complete the challenge `QUERY /todos (200)`

`QUERY` is a safe read method that lets you send query content in the request body. For this challenge, use it to request todos where `doneStatus=true`.

## QUERY /todos (200)

> Issue a QUERY request on the `/todos` end point with form-encoded query content to get only todos which are done. There must exist both done and not done todos to pass this challenge.

- Use the `QUERY` method with `/todos`.
- Add `Content-Type: application/x-www-form-urlencoded`.
- Add `Accept: application/json` if you want a JSON response.
- Send `doneStatus=true` in the request body.
- Make sure your challenger data has at least one todo with `"doneStatus": true` and at least one with `"doneStatus": false`.

## Basic Instructions

- Issue a `QUERY` request to:
  - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header so the challenge is tracked.
- The request body should be form URL encoded:

```text
doneStatus=true
```

### Try it now

If you need fixture data, create one done todo and one not-done todo first. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"done query fixture","doneStatus":true,"description":"created for QUERY filtering"}' details="true" summary="POST /todos to create a done todo fixture">}}

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"not done query comparison","doneStatus":false,"description":"created for QUERY filtering"}' details="true" summary="POST /todos to create a not-done comparison todo">}}

Issue the QUERY request with the filter in the body:

{{<api-live-request method="QUERY" path="/todos" expected-status="200" headers="Content-Type: application/x-www-form-urlencoded||Accept: application/json" body='doneStatus=true' details="true" summary="QUERY /todos with doneStatus=true to return only done todos" open="true">}}

## Example Request

~~~~~~~~
> QUERY /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/x-www-form-urlencoded
> Accept: application/json
>
> doneStatus=true
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: application/json
< Accept-Query: application/x-www-form-urlencoded
< X-Challenger: x-challenger-guid
~~~~~~~~

Returned body:

```json
{
  "todos": [
    {
      "id": 41,
      "title": "done todo for query",
      "doneStatus": true,
      "description": "created for query challenge"
    }
  ]
}
```

## Lessons Learned

- `QUERY /todos` is a safe read that sends filter criteria in the request body.
- `Content-Type: application/x-www-form-urlencoded` describes the query body, while `Accept` describes the returned representation.
- `Accept-Query` in the response documents which query content types are supported.

## Suggested Experiments

- Send `doneStatus=false` in the `QUERY` body and compare with the `doneStatus=true` result.
- Repeat the same criteria as `GET /todos?doneStatus=true` and compare URL visibility with body-based criteria.
- Try the same completed-todo filter as a `JSONPath` `QUERY` body with `Content-Type: application/jsonpath` and `$.todos[?(@.doneStatus == true)]`.
- Read the [method reference](/reference/http-verbs#http-query-verb) for more `QUERY` body formats, including `application/jsonpath`.
