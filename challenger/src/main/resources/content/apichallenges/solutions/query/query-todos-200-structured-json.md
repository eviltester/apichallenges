---
title: API Challenges Solution For - QUERY todos 200 Structured JSON
seo_title: Solution: QUERY todos 200 Structured JSON Body | API Challenges
description: How to solve API challenge QUERY todos 200 using Structured JSON query content in the request body.
lastmod: 2026-08-12
seo_description: Use this walkthrough to solve QUERY todos 200 Structured JSON with request setup, headers, body content, and expected status code.
next_challenge: /apichallenges/solutions/patch/patch-todos-id-200-partial
concepts_learned: HTTP QUERY||Structured JSON||application/vnd.apichallenges.todo-query+json||200 OK||safe method
concept_summary: Use this challenge to learn how QUERY can send a Structured JSON query document in the request body to filter resources without changing state.
concept_reference_label: HTTP QUERY Verb
concept_reference_url: /reference/http-verbs/http-query
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a QUERY request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Content-Type to application/vnd.apichallenges.todo-query+json||Send a Structured JSON filter for doneStatus true in the request body and verify the response status is 200
showads: true
---

# How to complete the challenge `QUERY /todos Structured JSON (200)`

`QUERY` is a safe read method that can send query content in the request body. For this challenge, use a Structured JSON query document to request todos where `doneStatus=true`.

## QUERY /todos Structured JSON (200)

> Issue a QUERY request on the `/todos` end point with a Structured JSON query body to get only todos which are done. There must exist both done and not done todos to pass this challenge.

- Use the `QUERY` method with `/todos`.
- Add `Content-Type: application/vnd.apichallenges.todo-query+json`.
- Add `Accept: application/json` so you can inspect the response.
- Send a `filter` object for `"doneStatus": true` in the request body.
- Make sure your challenger data has at least one todo with `"doneStatus": true` and at least one with `"doneStatus": false`.

## Basic Instructions

- Issue a `QUERY` request to:
  - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header so the challenge is tracked.
- The request body should be a Structured JSON query document:

[The Structured JSON query body format is described in the HTTP QUERY method reference](/reference/http-verbs/http-query#http-query-structured-json-body).

```json
{
  "filter": {
    "doneStatus": true
  }
}
```

### Try it now

If you need fixture data, create one done todo and one not-done todo first. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"done Structured JSON query fixture","doneStatus":true,"description":"created for QUERY Structured JSON filtering"}' details="true" summary="POST /todos to create a done todo fixture">}}

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"not done Structured JSON query comparison","doneStatus":false,"description":"created for QUERY Structured JSON filtering"}' details="true" summary="POST /todos to create a not-done comparison todo">}}

Issue the QUERY request with the Structured JSON filter in the body:

{{<api-live-request method="QUERY" path="/todos" expected-status="200" headers="Content-Type: application/vnd.apichallenges.todo-query+json||Accept: application/json" body='{"filter":{"doneStatus":true}}' details="true" summary="QUERY /todos with a Structured JSON doneStatus filter" open="true">}}

## Example Request

~~~~~~~~
> QUERY /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/vnd.apichallenges.todo-query+json
> Accept: application/json
>
> {"filter":{"doneStatus":true}}
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: application/json
< Accept-Query: application/x-www-form-urlencoded, application/jsonpath, application/vnd.apichallenges.todo-query+json
< X-Challenger: x-challenger-guid
~~~~~~~~

Returned body:

```json
{
  "todos": [
    {
      "id": 43,
      "title": "done todo for Structured JSON query",
      "doneStatus": true,
      "description": "created for query challenge"
    }
  ]
}
```

## Lessons Learned

- `QUERY /todos` can use `Content-Type: application/vnd.apichallenges.todo-query+json` when the request body is a `Structured JSON` query document.
- The body describes query criteria; it is not a todo representation and it does not create or amend data.
- A `filter` object can match fields by exact `JSON` values, so booleans are sent as `true` or `false`, not as strings.
- `Structured JSON` differs from `JSONPath`: `JSONPath` selects from a response-shaped document, while `Structured JSON` describes query criteria for the API to apply.

## Suggested Experiments

- Send `{"filter":{"doneStatus":false}}` to return todos that are not done.
- Send `{"filter":{"id":{"greaterThan":1,"lessThan":5}}}` to filter numeric ids.
- Send `{"filter":{"title":{"contains":"query"}}}` after creating todos with matching titles.
- Send `{"filter":{"doneStatus":false},"sort":[{"field":"title","direction":"asc"}]}` to combine filtering and sorting.
- Send `{"sort":[{"field":"id","direction":"desc"}],"limit":5,"offset":0}` to page through a sorted collection.
- Use the [method reference](/reference/http-verbs/http-query#http-query-structured-json-body) for more `QUERY` details and `Structured JSON` examples.
