---
title: API Challenges Solution For - PATCH todos/id 200 json patch
seo_title: Solution: PATCH todos/id 200 JSON Patch | API Challenges
description: How to solve API challenge PATCH todos/id 200 using JSON Patch operations.
lastmod: 2026-08-06
seo_description: Use this walkthrough to solve PATCH todos/id 200 with JSON Patch request setup, headers, body content, and expected status code.
next_challenge: /apichallenges/solutions/options/options-todos-200
concepts_learned: HTTP PATCH||200 OK||JSON Patch||partial update
concept_summary: Use this challenge to learn how PATCH applies a JSON Patch to an existing resource.
concept_reference_label: HTTP PATCH Verb
concept_reference_url: /reference/http-verbs/http-patch
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a PATCH request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Set Content-Type to application/json-patch+json||Send a JSON Patch operation array and verify the response status is 200
showads: true
---


# How to complete the challenge `PATCH /todos/id (200) json-patch`

Use `PATCH` with `Content-Type: application/json-patch+json` when you want to send a [JSON Patch](https://www.rfc-editor.org/rfc/rfc6902) operation list.

## PATCH /todos/id (200) json-patch

> Issue a PATCH request to update an existing todo using JSON Patch operations.

- Use `PATCH /todos/{id}` where `{id}` is an existing todo id.
- Add `Content-Type: application/json-patch+json` for the [JSON Patch standard](https://www.rfc-editor.org/rfc/rfc6902).
- Send an array of JSON Patch operations.
- The response should be `200`.
- Use JSON Pointer paths such as `/title`, `/description`, or `/doneStatus`.
- You can [learn more about PATCH](/reference/http-verbs/http-patch) in the HTTP verbs tutorial.

## Basic Instructions

- Issue a `PATCH` request to:
  - `{{<ORIGIN_URL>}}/todos/{{firstTodoId}}`
- The request should have an `X-CHALLENGER` header so the challenge is tracked.
- The request body can be a JSON Patch operation array:

```json
[
  {
    "op": "replace",
    "path": "/title",
    "value": "patched with json patch"
  }
]
```

### Try it now

If you don't know what todos are available then you can check by `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /todos to create a todo item for this challenge">}}

{{<api-live-request method="PATCH" path="/todos/{{firstTodoId}}" expected-status="200" headers="Content-Type: application/json-patch+json||Accept: application/json" body='[{"op":"replace","path":"/title","value":"patched with json patch"}]' details="true" summary="PATCH /todos/{id} with JSON Patch to update a todo" open="true">}}

## Example Request

~~~~~~~~
> PATCH /todos/3 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json-patch+json
> Accept: application/json
>
> [{"op":"replace","path":"/title","value":"patched with json patch"}]
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
  "title": "patched with json patch",
  "doneStatus": false,
  "description": "existing description"
}
```

## Lessons Learned

- `application/json-patch+json` sends an operation list, so the patch document is about actions, not final state.
- `JSON Pointer` paths such as `/title` target fields inside the todo representation.
- Patch tests should assert both the operation response and the final resource state.

## Suggested Experiments

- Replace `/title`, then fetch the todo and confirm only the title changed.
- Try an invalid `JSON Pointer` path and compare the error with a valid operation list.
