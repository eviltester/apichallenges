---
date:  2025-01-01T12:53:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT todos/id 200 full update
seo_title: Solution: PUT todos/id 200 partial | API Challenges
description: How to solve API challenge PUT todos/id 200 to update a todo in the application with a full payload.
seo_description: Learn how partial PUT /api/todos/{id} updates behave, which fields can be omitted, and how to verify a 200 response with expected changes.
next_challenge: /apichallenges/solutions/put-update/put-todos-200-body-id
concepts_learned: HTTP PUT||200 OK||idempotent method||partial update
concept_summary: Use this challenge to learn how PUT handles partial update for todo resources.
concept_reference_label: HTTP PUT Verb
concept_reference_url: /reference/http-verbs/http-put
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a PUT request to /api/todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send a valid JSON payload that matches the field and content constraints||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `PUT /api/todos/id (200) partial update`

How to use a PUT request to successfully update a todo item in the application using a partial payload.

PUT request updates are idempotent so should generate the same response each time. A partial PUT update is different from a partial POST update. With a POST update any missing fields in the response will not be amended. With a PUT update the missing fields will be set to their default or empty values.

This behaviour varies for different APIs. Some APIs may not allow partial PUT updates.

## PUT /api/todos/id (200) partial update

> Issue a PUT request to successfully update a todo using a partial payload

- `PUT` request will update a todo if the provided `id` exists `/api/todos/id` end point
    - e.g. `PUT /api/todos/3` for a todo with `id==3`
- `200` is an success code, in this case it means the todo was updated
- The body of the message should be a `json` or `xml` partial set of `todo` details,
-  and the `json` or `xml` should be defined in the `content-type` header
- the id does not need to be included in the payload, but if it is then it should match the id in the url
- the fields included in the message should be the mandatory fields, otherwise the payload will not validate


## Basic Instructions

- Issue a `PUT` request to end point "/api/todos/id"
    - where `id` is replaced with the id of an existing todo
        - if you don't know any then a `GET /api/todos` would show a list of todos
    - `{{<ORIGIN_URL>}}/api/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have a partial set of todo details. All mandatory fields should be included e.g.

```json
{
  "title": "partial update for title"
}
```
- Title is a mandatory field, without a default value
- `doneStatus` is boolean and defaults to `false`
- `description` has a default value of `""`
- The id included in the payload should be the same as the id of the url because we cannot update the id, the id is auto generated.
- The response status code should be `200` when all the details are valid.
- The body of the response will a JSON showing the full todo details, and your updated values should be present.

```json
{
  "id": 3,
  "title": "partial update for title",
  "doneStatus": false,
  "description": ""
}
```

NOTE: if you haven't read the documentation and don't know what format to use then issue a GET request for a single entity and the payload format for the `POST` is likely to be pretty close.

NOTE: because you add an id to the payload you risk triggering an error validation if the id in the payload is different from the id in the URL.

NOTE: PUT is idempotent so the result will always be the same, regardless of the initial values of the todo prior to the update request. As a follow on exercise check that this statement is true.

### Try it now

If you don't know what todos are available then you can check by `GET /api/todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /api/todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/api/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /api/todos to create a todo item for this challenge">}}

{{<api-live-request method="PUT" path="/api/todos/{{firstTodoId}}" expected-status="200" headers="Content-Type: application/json||Accept: application/json" body='{"title":"partial update from widget"}' details="true" summary="PUT /api/todos/{id} with a partial body to update a todo" open="true">}}


## Example Request

~~~~~~~~
> PUT /api/todos/3 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 32

| 	{
|     "title": "partial update for title"
| 	}
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Sat, 06 Feb 2021 12:08:58 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Returned body:

```json
{
  "id": 3,
  "title": "partial update for title",
  "doneStatus": false,
  "description": ""
}
```

## Lessons Learned

- This API accepts a partial-looking `PUT`, which is worth testing because many APIs treat `PUT` as full replacement.
- Required fields still matter even when the payload is described as partial.
- Partial `PUT` behavior should be documented so clients do not assume standard merge semantics.

## Suggested Experiments

- Send a `PUT` body with only `title` plus required fields and inspect what happens to `description`.
- Compare this partial `PUT` with `PATCH /api/todos/{id}` using the same changed field.