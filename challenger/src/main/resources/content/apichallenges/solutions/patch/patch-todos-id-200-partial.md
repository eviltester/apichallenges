---
title: API Challenges Solution For - PATCH todos/id 200 partial
seo_title: Solution: PATCH todos/id 200 Partial JSON | API Challenges
description: How to solve API challenge PATCH todos/id 200 using a partial JSON update.
lastmod: 2026-08-06
seo_description: Use this walkthrough to solve PATCH todos/id 200 partial JSON updates with request setup, headers, body content, and expected status code.
next_challenge: /apichallenges/solutions/patch/patch-todos-id-200-merge-patch
concepts_learned: HTTP PATCH||200 OK||partial update||partial update
concept_summary: Use this challenge to learn how PATCH applies a partial update to an existing resource.
concept_reference_label: HTTP PATCH Verb
concept_reference_url: /reference/http-verbs/http-patch
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a PATCH request to /api/todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Set Content-Type to application/json||Send only the todo fields to change and verify the response status is 200
showads: true
---


# How to complete the challenge `PATCH /api/todos/id (200) partial`

Use [`PATCH`](https://www.rfc-editor.org/rfc/rfc5789) with `Content-Type: application/json` when you want to update selected fields on an existing todo. Partial JSON updates are an API-specific PATCH style.

## PATCH /api/todos/id (200) partial

> Issue a PATCH request to update an existing todo using a partial JSON payload.

- Use `PATCH /api/todos/{id}` where `{id}` is an existing todo id.
- Add `Content-Type: application/json`.
- Send only the fields you want to change.
- Do not include `id` in the payload.
- The response should be `200`.
- Fields that are not in the payload should keep their existing values.
- You can [learn more about PATCH](/reference/http-verbs/http-patch) in the HTTP verbs tutorial.

## Basic Instructions

- Issue a `PATCH` request to:
  - `{{<ORIGIN_URL>}}/api/todos/{{firstTodoId}}`
- The request should have an `X-CHALLENGER` header so the challenge is tracked.
- The request body can be a partial JSON object:

```json
{
  "title": "patched partial todo"
}
```

### Try it now

If you don't know what todos are available then you can check by `GET /api/todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /api/todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/api/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /api/todos to create a todo item for this challenge">}}

{{<api-live-request method="PATCH" path="/api/todos/{{firstTodoId}}" expected-status="200" headers="Content-Type: application/json||Accept: application/json" body='{"title":"patched partial todo"}' details="true" summary="PATCH /api/todos/{id} with partial JSON to update a todo" open="true">}}

## Example Request

~~~~~~~~
> PATCH /api/todos/3 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: application/json
>
> {"title":"patched partial todo"}
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: application/json
< Accept-Patch: application/json, application/merge-patch+json, application/json-patch+json
< X-Challenger: x-challenger-guid
~~~~~~~~

Returned body:

```json
{
  "id": 3,
  "title": "patched partial todo",
  "doneStatus": false,
  "description": "existing description"
}
```

## Lessons Learned

- This API accepts `PATCH` with ordinary `application/json` as an API-specific partial update style.
- Unlike `PUT`, a `PATCH` body can focus on the fields being changed.
- Partial update tests should prove omitted fields remain unchanged.

## Suggested Experiments

- Change only `doneStatus`, then fetch the todo and confirm `title` and `description` were preserved.
- Send an empty `JSON` object and observe whether the API treats it as no-op or invalid.