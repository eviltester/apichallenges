---
date:  2021-04-12T09:30:00Z
lastmod: 2026-02-18
title: API Challenges Solution For - DELETE todos/id 204
seo_title: Solution: DELETE todos/id 204 | API Challenges
description: How to solve API challenge DELETE todos/id 204 to delete a todo in the application.
seo_description: Use this walkthrough to solve DELETE todos/id 204 with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/options/options-todos-200
schema_howto_steps: Create a DELETE request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 204
showads: true
---

# Delete a todo item in the application

How to complete the challenge `DELETE /todos/id (204)` to successfully delete a todo item in the application.

## DELETE /todos/id (204)

> 	Issue a DELETE request to successfully delete a todo

- `DELETE` request will delete a todo if the provided `id` exists `/todos/id` end point
    - e.g. `DELETE /todos/3` to delete the todo with `id==3`
- `204` is a success code, in this case it means the todo was deleted and there is no response body
- add the `X-CHALLENGER` header


## Basic Instructions

- Issue a `DELETE` request to end point "/todos/id"
    - where `id` is replaced with the id of an existing todo
        - if you don't know any then a `GET /todos` would show a list of todos, or you could `POST /todos` to create one.
    - e.g using endpoint
        - `{{<ORIGIN_URL>}}/todos/id`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The response status code should be `204` when all the details are valid and the todo exists.
- To double check that the todo item was deleted, then you could issue a `GET` request on the todo directly and receive a `404` or issue a `GET` request on `/todos` and check it is not in the list of todos.

### Try it now

If you do not have a todo to delete, create one first:

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"todo to delete","doneStatus":false,"description":"created so the delete request has an existing todo"}'>}}

Then delete an existing todo:

{{<api-live-request method="DELETE" path="/todos/{{lastCreatedTodoId}}" expected-status="204" headers="Accept: application/json" refresh-after-execute="false" resolve-dynamic-on-execute="false">}}


## Example Request

~~~~~~~~
> DELETE /todos/62 HTTP/1.1
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







