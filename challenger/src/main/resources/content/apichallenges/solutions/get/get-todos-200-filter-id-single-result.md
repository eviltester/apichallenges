---
date: 2026-07-31T09:00:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - GET todos 200 filter id single result
seo_title: "Solution: GET todos filter id single result | API Challenges"
description: How to solve API challenge GET todos 200 filter id single result while multiple todos exist in the database.
seo_description: Use this walkthrough to solve GET todos filter id single result with an exact id query parameter and expected status 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-filter-description-regex
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Make sure multiple todos exist, then filter by one exact id||Send the request and verify the response status is 200
showads: true
---

# How to complete the challenge `GET /todos (200) ? filter id single result`

How to issue a GET request on a top level entity endpoint and filter by one todo id while there are multiple todos in the database.

## GET /todos (200) ? filter id single result

> Issue a GET request on the `/todos` end point with an id filter that returns one todo while multiple todos exist in the database.

- `id=3` means return the todo where the id is exactly 3
- there must be more than one todo in your current session
- the response should contain exactly one todo
- the returned todo should have the same `id` that you filtered on

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Choose an id from the current todo list
- Add the id as an exact filter:
    - `{{<ORIGIN_URL>}}/todos?id=3`
- The response status code should be `200` because the request is accepted
- Check that the response contains one todo with the requested id

### Try it now

If you need to check which todo ids are available, use `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see the available todo ids">}}

If you do not have more than one todo, create another one using `POST /todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"single result comparison fixture","doneStatus":false,"description":"created for id filtering"}' details="true" summary="POST /todos to create another todo item">}}

The sample below uses `id=3`; edit the id if your current data uses a different todo id.

{{<api-live-request method="GET" path="/todos?id=3" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?id=3 to return one matching todo" open="true">}}

## Example Request

~~~~~~~~
> GET /todos?id=3 HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: application/json
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Content-Type: application/json
< X-Challenger: x-challenger-guid
~~~~~~~~

Returned body:

```json
{
  "todos": [
    { "id": 3, "title": "single matching todo", "doneStatus": false, "description": "" }
  ]
}
```
