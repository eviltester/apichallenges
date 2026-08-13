---
title: API Challenges Solution For - QUERY todos 200 JSONPath
seo_title: Solution: QUERY todos 200 JSONPath Body | API Challenges
description: How to solve API challenge QUERY todos 200 using JSONPath query content in the request body.
lastmod: 2026-08-12
seo_description: Use this walkthrough to solve QUERY todos 200 JSONPath with request setup, headers, body content, and expected status code.
next_challenge: /apichallenges/solutions/query/query-todos-200-structured-json
concepts_learned: HTTP QUERY||JSONPath||application/jsonpath||200 OK||safe method
concept_summary: Use this challenge to learn how QUERY can send a JSONPath expression in the request body to filter resources without changing state.
concept_reference_label: HTTP QUERY Verb
concept_reference_url: /reference/http-verbs/http-query
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a QUERY request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Set Content-Type to application/jsonpath||Send $.todos[?(@.doneStatus == true)] in the request body and verify the response status is 200
showads: true
---

# How to complete the challenge `QUERY /todos JSONPath (200)`

`QUERY` is a safe read method that can send query content in the request body. For this challenge, use a JSONPath expression to request todos where `doneStatus=true`.

## QUERY /todos JSONPath (200)

> Issue a QUERY request on the `/todos` end point with JSONPath query content to get only todos which are done. There must exist both done and not done todos to pass this challenge.

- Use the `QUERY` method with `/todos`.
- Add `Content-Type: application/jsonpath`.
- Add `Accept: application/json` so you can inspect the response.
- Send `$.todos[?(@.doneStatus == true)]` in the request body.
- Make sure your challenger data has at least one todo with `"doneStatus": true` and at least one with `"doneStatus": false`.

## Basic Instructions

- Issue a `QUERY` request to:
  - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header so the challenge is tracked.
- The request body should be a JSONPath expression:

```text
$.todos[?(@.doneStatus == true)]
```

### Try it now

If you need fixture data, create one done todo and one not-done todo first. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"done JSONPath query fixture","doneStatus":true,"description":"created for QUERY JSONPath filtering"}' details="true" summary="POST /todos to create a done todo fixture">}}

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"not done JSONPath query comparison","doneStatus":false,"description":"created for QUERY JSONPath filtering"}' details="true" summary="POST /todos to create a not-done comparison todo">}}

Issue the QUERY request with the JSONPath filter in the body:

{{<api-live-request method="QUERY" path="/todos" expected-status="200" headers="Content-Type: application/jsonpath||Accept: application/json" body='$.todos[?(@.doneStatus == true)]' details="true" summary="QUERY /todos with a JSONPath doneStatus filter" open="true">}}

## Example Request

~~~~~~~~
> QUERY /todos HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/jsonpath
> Accept: application/json
>
> $.todos[?(@.doneStatus == true)]
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
      "id": 42,
      "title": "done todo for JSONPath query",
      "doneStatus": true,
      "description": "created for query challenge"
    }
  ]
}
```

## Lessons Learned

- `QUERY /todos` can use `Content-Type: application/jsonpath` when the request body is a `JSONPath` expression.
- `JSONPath` starts from `$`, uses `@` for the current item in a filter, and can select matching objects from the collection.
- API Challenges expects the `JSONPath` expression to select complete todo objects, not isolated fields such as titles.
- The `Accept-Query` response header lists the supported `QUERY` body media types.

## Suggested Experiments

- Send `$.todos` to return every todo and compare it with `GET /todos`.
- Send `$.todos[?(@.doneStatus == false)]` to return todos that are not done.
- Send `$.todos[?(@.description != '')]` after creating a todo with a description.
- Try `$.todos[*].title` and observe that selecting only field values is rejected.
- Try the same completed-todo filter as a `Structured JSON` body with `Content-Type: application/vnd.apichallenges.todo-query+json` and `{"filter":{"doneStatus":true}}`.
- Use the [method reference](/reference/http-verbs/http-query#http-query-structured-json-body) for extra `QUERY` experiments and details on structured query documents.
