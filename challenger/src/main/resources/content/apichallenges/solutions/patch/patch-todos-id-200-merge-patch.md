---
title: API Challenges Solution For - PATCH todos/id 200 merge patch
seo_title: Solution: PATCH todos/id 200 JSON Merge Patch | API Challenges
description: How to solve API challenge PATCH todos/id 200 using JSON Merge Patch.
lastmod: 2026-08-06
seo_description: Use this walkthrough to solve PATCH todos/id 200 with JSON Merge Patch request setup, headers, body content, and expected status code.
next_challenge: /apichallenges/solutions/patch/patch-todos-id-200-json-patch
concepts_learned: HTTP PATCH||200 OK||JSON Merge Patch||partial update
concept_summary: Use this challenge to learn how PATCH applies a JSON Merge Patch to an existing resource.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
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
- You can [learn more about PATCH](/reference/http-verbs/http-patch) in the HTTP verbs tutorial.

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

If you don't know what todos are available then you can check by `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /todos to create a todo item for this challenge">}}

{{<api-live-request method="PATCH" path="/todos/{{firstTodoId}}" expected-status="200" headers="Content-Type: application/merge-patch+json||Accept: application/json" body='{"description":"patched with merge patch"}' details="true" summary="PATCH /todos/{id} with JSON Merge Patch to update a todo" open="true">}}

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

## Lessons Learned

- `application/merge-patch+json` describes desired field changes as a partial object.
- Merge Patch semantics differ from ordinary partial `JSON`, especially around `null` values.
- This format is useful for small updates where operation lists would be verbose.

## Suggested Experiments

- Send a merge patch with one changed field and compare it with a `JSON Patch` `replace` operation.
- Try setting an optional field to `null` and observe whether the API removes it, ignores it, or rejects it.
