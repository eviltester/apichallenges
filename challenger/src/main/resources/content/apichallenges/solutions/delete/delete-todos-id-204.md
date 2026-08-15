---
date:  2021-04-12T09:30:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - DELETE todos/id 204
seo_title: Solution: DELETE todos/id 204 | API Challenges
description: How to solve API challenge DELETE todos/id 204 to delete a todo in the application.
seo_description: Use this walkthrough to solve DELETE todos/id 204 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/query/query-todos-200
concepts_learned: HTTP DELETE||204 No Content||CRUD delete||resource URL
concept_summary: Use this challenge to learn how DELETE removes a resource and returns no response body.
concept_reference_label: HTTP DELETE Verb
concept_reference_url: /reference/http-verbs/http-delete
concept_reference_label_2: REST API Basics
concept_reference_url_2: /reference/rest-api-basics
schema_howto_steps: Create a DELETE request to /api/todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 204
showads: true
---


# Delete a todo item in the application

How to complete the challenge `DELETE /api/todos/id (204)` to successfully delete a todo item in the application.

## DELETE /api/todos/id (204)

> 	Issue a DELETE request to successfully delete a todo

- `DELETE` request will delete a todo if the provided `id` exists `/api/todos/id` end point
    - e.g. `DELETE /api/todos/3` to delete the todo with `id==3`
- `204` is a success code, in this case it means the todo was deleted and there is no response body
- add the `X-CHALLENGER` header


## Basic Instructions

- Issue a `DELETE` request to end point "/api/todos/id"
    - where `id` is replaced with the id of an existing todo
        - if you don't know any then a `GET /api/todos` would show a list of todos, or you could `POST /api/todos` to create one.
    - e.g using endpoint
        - `{{<ORIGIN_URL>}}/api/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `204` when all the details are valid and the todo exists.
- To double check that the todo item was deleted, then you could issue a `GET` request on the todo directly and receive a `404` or issue a `GET` request on `/api/todos` and check it is not in the list of todos.

### Try it now

Delete an existing todo.

If you don't know what todos are available then you can check by `GET /api/todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/todos to see what todos are available now">}}

If you have already deleted all todos, create one using `POST /api/todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/api/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo to delete","doneStatus":false,"description":"created from the delete solution page"}' details="true" summary="POST /api/todos to create a todo item for deletion">}}

Then, delete an existing todo:

{{<api-live-request method="DELETE" path="/api/todos/{{firstTodoId}}" expected-status="204" headers="Accept: application/json" auto-create-first-todo="false" refresh-after-execute="false" details="true" summary="DELETE /api/todos/{id} to delete a specific todo" open="true">}}

After you've deleted something. You should really check that it has been deleted by issuing a `GET /api/todos/{id}` request and make sure the item has been deleted.

## Example Request

~~~~~~~~
> DELETE /api/todos/62 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: */*
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 204 No Content
< Connection: close
< Date: Thu, 27 Aug 2020 14:25:53 GMT
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

## Lessons Learned

- `DELETE /api/todos/{id}` removes an existing resource and returns `204 No Content`.
- A successful delete should be verified by a follow-up read, because the response body is intentionally empty.
- Repeating the same delete is a useful way to explore idempotency and missing-resource behavior.

## Suggested Experiments

- Delete a todo, then call `GET /api/todos/{id}` to confirm the API no longer returns it.
- Send the identical `DELETE` again and compare the second status with the first `204 No Content`.