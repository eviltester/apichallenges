---
date:  2025-01-01T12:53:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - PUT todos/id 200 full update
seo_title: Solution: PUT todos/id 200 full update | API Challenges
description: How to solve API challenge PUT todos/id 200 to update a todo in the application with a full payload.
seo_description: Master PUT /todos/{id} full updates by sending all required fields, setting correct headers, and validating the 200 response payload.
next_challenge: /apichallenges/solutions/put-update/put-todos-id-200-update-partial
concepts_learned: HTTP PUT||200 OK||idempotent method||full update
concept_summary: Use this challenge to learn how PUT handles full update for todo resources.
concept_reference_label: HTTP PUT Verb
concept_reference_url: /reference/http-verbs/http-put
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a PUT request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send a valid JSON payload that matches the field and content constraints||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `PUT /todos/id (200) full update`

How to use a PUT request to successfully update a todo item in the application using a full payload.

PUT request updates are idempotent so should generate the same response each time.

## PUT /todos/id (200) full update

> Issue a PUT request to successfully update a todo using a full payload

- `PUT` request will update a todo if the provided `id` exists `/todos/id` end point
    - e.g. `PUT /todos/3` for a todo with `id==3`
- `200` is an success code, in this case it means the todo was updated
- The body of the message should be a `json` or `xml` full set of `todo` details,
-  and the `json` or `xml` should be defined in the `content-type` header
- the id can optionally be included in the payload, but if it is then it should match the id in the url
- all required fields must be included


## Basic Instructions

- Issue a `PUT` request to end point "/todos/id"
    - where `id` is replaced with the id of an existing todo
        - if you don't know any then a `GET /todos` would show a list of todos
    - `{{<ORIGIN_URL>}}/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have a full set of todo details. e.g.

```json
{
  "id": 3,
  "title": "updated title",
  "doneStatus": false,
  "description": "updated description"
}
```
- The id included in the payload should be the same as the id of the url because we cannot update the id, the id is auto generated.
- The response status code should be `200` when all the details are valid.
- The body of the response will a JSON showing the full todo details, and your updated values should be present.

```json
{
  "id": 3,
  "title": "updated title",
  "doneStatus": false,
  "description": "updated description"
}
```

NOTE: if you haven't read the documentation and don't know what format to use then issue a GET request for a single entity and the payload format for the `POST` is likely to be pretty close.

NOTE: because you add an id to the payload you risk triggering an error validation if the id in the payload is different from the id in the URL.

### Try it now

If you don't know what todos are available then you can check by `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo fixture","doneStatus":false,"description":"created from the solution page"}' details="true" summary="POST /todos to create a todo item for this challenge">}}

{{<api-live-request method="PUT" path="/todos/{{firstTodoId}}" expected-status="200" headers="Content-Type: application/json||Accept: application/json" body='{"id":"{{firstTodoId}}","title":"full update from widget","doneStatus":true,"description":"updated from the solution page"}' details="true" summary="PUT /todos/{id} with a full body to update a todo" open="true">}}


## Example Request

~~~~~~~~
> PUT /todos/3 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 32

| 	{
|     "id": 3,
| 	  "title": "updated title",
| 	  "doneStatus": false,
| 	  "description": "updated description"
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
  "title": "updated title",
  "doneStatus": false,
  "description": "updated description"
}
```

## Lessons Learned

- Full `PUT` update asks the client to send a complete todo representation.
- Required fields should be preserved or supplied intentionally because replacement semantics can overwrite omissions.
- The response should be compared with the pre-update resource to confirm every intended field changed.

## Suggested Experiments

- Change all mutable fields in one `PUT`, then fetch the todo to verify the full replacement.
- Omit an optional field from the full update and compare whether the API clears, defaults, or preserves it.