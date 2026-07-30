---
title: API Challenges Solution For - PATCH todos/id 200 merge patch
seo_title: Solution: PATCH todos/id 200 JSON Merge Patch | API Challenges
description: How to solve API challenge PATCH todos/id 200 using JSON Merge Patch.
lastmod: 2026-07-30
seo_description: Use this walkthrough to solve PATCH todos/id 200 with JSON Merge Patch request setup, headers, body content, and expected status code.
next_challenge: /apichallenges/solutions/patch/patch-todos-id-200-json-patch
schema_howto_steps: Create a PATCH request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Set Content-Type to application/merge-patch+json||Send a JSON Merge Patch object and verify the response status is 200
showads: true
---

# How to complete the challenge `PATCH /todos/id (200) merge-patch`

Use `PATCH` with `Content-Type: application/merge-patch+json` when you want to send a [JSON Merge Patch](https://www.rfc-editor.org/rfc/rfc7396) document.

> The merge-patch may look similar to the `application/json` patch. But the main difference is the standard handling. Any API can use whatever conventions it wants to handle an `application/json` patch. The API Challenges app ignores your request if you try to set a mandatory field to `null`. But in a `merge-patch+json` standard, a `null` is a delete and you can't delete a mandatory field so you would see an error. Feel free to experiment with this after you complete the challenge.

## PATCH /todos/id (200) merge-patch

> Issue a PATCH request to update an existing todo using JSON Merge Patch.

- Use `PATCH /todos/{id}` where `{id}` is an existing todo id.
- Add `Content-Type: application/merge-patch+json` for the [JSON Merge Patch standard](https://www.rfc-editor.org/rfc/rfc7396).
- Send a JSON object containing the fields to add, replace, or remove.
- The response should be `200`.
- Fields that are not in the merge patch document should keep their existing values.
- You can [learn more about PATCH](/tutorials/http-verbs#toc18) in the HTTP verbs tutorial.

## Basic Instructions

- Issue a `PATCH` request to:
  - `{{<ORIGIN_URL>}}/todos/{{firstTodoId}}`
- The request should have an `X-CHALLENGER` header so the challenge is tracked.
- The request body can be a JSON Merge Patch object:

```json
{
  "description": "patched with merge patch"
}
```

### Try it now

{{<api-live-request method="PATCH" path="/todos/{{firstTodoId}}" expected-status="200" headers="Content-Type: application/merge-patch+json||Accept: application/json" body='{"description":"patched with merge patch"}'>}}

## Example Request

~~~~~~~~
> PATCH /todos/3 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/merge-patch+json
> Accept: application/json
>
> {"description":"patched with merge patch"}
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
  "title": "existing title",
  "doneStatus": false,
  "description": "patched with merge patch"
}
```
