---
title: API Challenges Solution For - PATCH todos/id 200 json patch
seo_title: Solution: PATCH todos/id 200 JSON Patch | API Challenges
description: How to solve API challenge PATCH todos/id 200 using JSON Patch operations.
lastmod: 2026-07-30
seo_description: Use this walkthrough to solve PATCH todos/id 200 with JSON Patch request setup, headers, body content, and expected status code.
next_challenge: /gui/challenges
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
- You can [learn more about PATCH](/tutorials/http-verbs#toc18) in the HTTP verbs tutorial.

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

{{<api-live-request method="PATCH" path="/todos/{{firstTodoId}}" expected-status="200" headers="Content-Type: application/json-patch+json||Accept: application/json" body='[{"op":"replace","path":"/title","value":"patched with json patch"}]'>}}

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
