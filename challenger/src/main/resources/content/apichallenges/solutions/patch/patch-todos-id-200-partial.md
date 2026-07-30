---
title: API Challenges Solution For - PATCH todos/id 200 partial
seo_title: Solution: PATCH todos/id 200 Partial JSON | API Challenges
description: How to solve API challenge PATCH todos/id 200 using a partial JSON update.
lastmod: 2026-07-30
seo_description: Use this walkthrough to solve PATCH todos/id 200 partial JSON updates with request setup, headers, body content, and expected status code.
next_challenge: /apichallenges/solutions/patch/patch-todos-id-200-merge-patch
schema_howto_steps: Create a PATCH request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Set Content-Type to application/json||Send only the todo fields to change and verify the response status is 200
showads: true
---

# How to complete the challenge `PATCH /todos/id (200) partial`

Use [`PATCH`](https://www.rfc-editor.org/rfc/rfc5789) with `Content-Type: application/json` when you want to update selected fields on an existing todo. Partial JSON updates are an API-specific PATCH style.

## PATCH /todos/id (200) partial

> Issue a PATCH request to update an existing todo using a partial JSON payload.

- Use `PATCH /todos/{id}` where `{id}` is an existing todo id.
- Add `Content-Type: application/json`.
- Send only the fields you want to change.
- Do not include `id` in the payload.
- The response should be `200`.
- Fields that are not in the payload should keep their existing values.
- You can [learn more about PATCH](/tutorials/http-verbs#toc18) in the HTTP verbs tutorial.

## Basic Instructions

- Issue a `PATCH` request to:
  - `{{<ORIGIN_URL>}}/todos/{{firstTodoId}}`
- The request should have an `X-CHALLENGER` header so the challenge is tracked.
- The request body can be a partial JSON object:

```json
{
  "title": "patched partial todo"
}
```

### Try it now

{{<api-live-request method="PATCH" path="/todos/{{firstTodoId}}" expected-status="200" headers="Content-Type: application/json||Accept: application/json" body='{"title":"patched partial todo"}'>}}

## Example Request

~~~~~~~~
> PATCH /todos/3 HTTP/1.1
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
