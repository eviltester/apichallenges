---
date: 2026-07-30T09:00:00Z
lastmod: 2026-07-30
title: API Challenges Solution For - GET todos 200 sort multiple fields
seo_title: "Solution: GET todos 200 sort multiple fields | API Challenges"
description: How to solve API challenge GET todos 200 sort multiple fields using the _sortBy query parameter.
seo_description: Use this walkthrough to solve GET todos 200 sort multiple fields with the _sortBy query parameter and verify the response status is 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-filter-sort
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add _sortBy with comma-separated todo fields||Send the request and verify the response status is 200
showads: true
---

# How to complete the challenge `GET /todos (200) ? _sortBy multiple`

How to issue a GET request on a top level entity endpoint and sort the returned todos by more than one field.

## GET /todos (200) ? _sortBy multiple

> Issue a GET request on the `/todos` end point with a query parameter to sort todos by multiple fields.

- `_sortBy` is the URL parameter used to sort collection results
- separate multiple sort fields with commas
- a plain field name sorts ascending
- prefix a field with `-` to sort descending
- for example, `_sortBy=doneStatus,-id` sorts by `doneStatus` ascending, then by `id` descending within each done status

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Add `_sortBy=doneStatus,-id` as a URL parameter:
    - `{{<ORIGIN_URL>}}/todos?_sortBy=doneStatus,-id`
- The response status code should be `200` because the request is accepted
- Check that todos are grouped by `doneStatus`, and within each group the ids are descending

### Try it now

{{<api-live-request method="GET" path="/todos?_sortBy=doneStatus,-id" expected-status="200" headers="Accept: application/json">}}

## Example Request

~~~~~~~~
> GET /todos?_sortBy=doneStatus,-id HTTP/1.1
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
      "id": 8,
      "title": "not done with higher id",
      "doneStatus": false,
      "description": ""
    },
    {
      "id": 3,
      "title": "not done with lower id",
      "doneStatus": false,
      "description": ""
    },
    {
      "id": 9,
      "title": "done with higher id",
      "doneStatus": true,
      "description": ""
    }
  ]
}
```
