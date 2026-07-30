---
date: 2026-07-30T09:00:00Z
lastmod: 2026-07-30
title: API Challenges Solution For - GET todos 200 sort descending
seo_title: "Solution: GET todos 200 sort descending | API Challenges"
description: How to solve API challenge GET todos 200 sort descending using the _sortBy query parameter.
seo_description: Use this walkthrough to solve GET todos 200 sort descending with the _sortBy query parameter and verify the response status is 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-sort-multiple
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add _sortBy with a minus-prefixed todo field to sort descending||Send the request and verify the response status is 200
showads: true
---

# How to complete the challenge `GET /todos (200) ? _sortBy descending`

How to issue a GET request on a top level entity endpoint and sort the returned todos in descending order.

## GET /todos (200) ? _sortBy descending

> Issue a GET request on the `/todos` end point with a query parameter to sort todos descending by a field.

- `_sortBy` is the URL parameter used to sort collection results
- prefix a field with `-` to sort descending
- for example, `_sortBy=-id` sorts the todos from highest id to lowest id
- use a field that exists on a todo, such as `id`, `title`, `doneStatus`, or `description`

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Add `_sortBy=-id` as a URL parameter:
    - `{{<ORIGIN_URL>}}/todos?_sortBy=-id`
- The response status code should be `200` because the request is accepted
- Check that the returned todos are ordered by `id` from highest to lowest

### Try it now

{{<api-live-request method="GET" path="/todos?_sortBy=-id" expected-status="200" headers="Accept: application/json">}}

## Example Request

~~~~~~~~
> GET /todos?_sortBy=-id HTTP/1.1
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
    {
      "id": 10,
      "title": "highest id first",
      "doneStatus": false,
      "description": ""
    },
    {
      "id": 9,
      "title": "then the next id",
      "doneStatus": true,
      "description": ""
    }
  ]
}
```
