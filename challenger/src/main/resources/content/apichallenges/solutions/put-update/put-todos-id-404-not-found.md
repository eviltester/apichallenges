---
date:  2026-08-01T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT todos/id not found 404
seo_title: Solution: PUT todos/id not found 404 | API Challenges
description: How to solve API challenge PUT todos/id not found 404.
seo_description: Use this walkthrough to solve PUT /api/todos/{id} not found 404 by using a missing URL id and no id in the request body.
next_challenge: /apichallenges/solutions/put-update/put-todos-id-422-no-amend-id
concepts_learned: HTTP PUT||404 Not Found||idempotent method||missing resource
concept_summary: Use this challenge to learn how PUT handles missing resource for todo resources.
concept_reference_label: HTTP PUT Verb
concept_reference_url: /reference/http-verbs/http-put
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Find an id that does not exist||Create a PUT request to /api/todos/{id} with that missing URL id||Send a valid JSON payload without an id field||Send the request and verify the response status is 404
showads: true
---


# How to complete the challenge `PUT /api/todos/{id} not found (404)`

Issue a `PUT` request to `/api/todos/{id}` where the URL id does not exist, and do not include an `id` in the request body.

This is an update attempt against a specific URL resource. Since that resource does not exist, the response should be `404 Not Found`.

## Basic Instructions

- Send `PUT /api/todos/{id}` where `{id}` does not exist
- Add an `X-CHALLENGER` header to track challenge completion
- Set `Content-Type` to `application/json`
- Do not include an `id` field in the JSON payload
- Include a valid `title`
- Verify the response status is `404`

Example body:

```json
{
  "title": "missing todo",
  "doneStatus": false,
  "description": "URL id does not exist"
}
```

### Try it now

{{<api-live-request method="PUT" path="/api/todos/{{missingTodoId}}" expected-status="404" headers="Content-Type: application/json||Accept: application/json" body='{"title":"missing todo","doneStatus":false,"description":"URL id does not exist"}' details="true" summary="PUT /api/todos/{id} with a missing URL id and no body id" open="true">}}

## Lessons Learned

- A valid `PUT` body cannot update a resource `id` that does not exist.
- `404 Not Found` here is about the target path resource, not body validation.
- Missing-resource update tests should keep the payload valid so the status is unambiguous.

## Suggested Experiments

- Reuse the same payload against an existing `id` and compare `200 OK` with the missing-id `404 Not Found`.
- Create a new todo after the failed update and check that the failed `PUT` did not create that `id`.