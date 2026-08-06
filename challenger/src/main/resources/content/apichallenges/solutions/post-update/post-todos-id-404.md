---
date:  2024-01-01T11:26:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST todos/id 404
seo_title: Solution: POST todos/id 404 Guide | API Challenges
description: How to solve API challenge POST todos/id 404 to try to update a todo which does not exist.
seo_description: Use this walkthrough to solve POST todos/id 404 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/put-update/put-todos-id-200-update-full
concepts_learned: HTTP POST||404 Not Found||resource URL||missing resource
concept_summary: Use this challenge to learn how this API uses POST to update an existing todo resource.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a POST request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send a valid JSON payload that matches the field and content constraints||Send the request and verify the response status is 404
showads: true
---


# How to complete the challenge `POST /todos/id (404)`

How to use a POST request to try to update a todo item in the application, but the todo item id should not exist.

## POST /todos/id (404)

> Issue a POST request to try and update a todo, but no todo with this id should exist

- `POST` request will update a todo if the provided `id` exists `/todos/id` end point
    - e.g. `POST /todos/3` for a todo with `id==3`
- `404` is a failure code, in this case it means no todo with this id exists
- The body of the message should be a `json` or `xml` partial set of `todo` details,
-  and the `json` or `xml` should be defined in the `content-type` header
- The 404 response should have an error message explaining the problem


## Basic Instructions

- Issue a `POST` request to end point "/todos/id"
    - where `id` is replaced with the id of a todo that does not exist
        - if you don't know any then a `GET /todos` would show a list of todos.
    - `{{<ORIGIN_URL>}}/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The `content-type` in the message should be `application/json` because we are sending a JSON payload
- The Payload should have a partial set of todo details. e.g.

```json
{
  "title": "updated title"
}
```
- The response status code should be `404` when the details are valid and the id does not exist.
- The body of the response will be a JSON showing the error.

```json
{
  "errorMessages": [
    "No such todo entity instance with id == 200 found"
  ]
}
```

### Try it now

{{<api-live-request method="POST" path="/todos/{{missingTodoId}}" expected-status="404" headers="Content-Type: application/json||Accept: application/json" body='{"title":"solution widget todo","doneStatus":true,"description":"created from the solution page"}' details="true" summary="POST /todos/{id} to update a missing todo and trigger 404" open="true">}}


## Example Request

~~~~~~~~
> POST /todos/200 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Content-Type: application/json
> Accept: */*
> Content-Length: 32

| 	{
| 		"title": "updated title"
| 	}
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 404 Not Found
< Connection: close
< Date: Sat, 06 Feb 2021 12:08:58 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur

| 	{
| 	  "errorMessages": [
| 	    "No such todo entity instance with id == 200 found"
| 	  ]
| 	}
~~~~~~~~

## Lessons Learned

- `POST /todos/{id}` to a missing `id` shows that update-style `POST` still depends on an existing target.
- `404 Not Found` prevents the request from becoming an accidental create.
- Missing-resource update tests should use a clearly absent `id` to avoid flaky results.

## Suggested Experiments

- Try the same payload against an existing `id` and a missing `id`, then compare `200 OK` with `404 Not Found`.
- Create a todo after the failed update and confirm the failed `POST` did not reserve or create that `id`.