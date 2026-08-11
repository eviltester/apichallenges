---
date: 2026-07-30T09:00:00Z
lastmod: 2026-08-11
title: API Challenges Solution For - GET todos 200 sort ascending
seo_title: "Solution: GET todos 200 sort ascending | API Challenges"
description: How to solve API challenge GET todos 200 sort ascending using the _sortBy query parameter.
seo_description: Use this walkthrough to solve GET todos 200 sort ascending with the _sortBy query parameter and verify the response status is 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-sort-descending
concepts_learned: HTTP GET||200 OK||query parameters||sorting
concept_summary: Use this challenge to learn how query parameters sort a collection resource.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add Accept application/json so the response is JSON||Add _sortBy with a todo field to sort ascending||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos (200) ? _sortBy ascending`

How to issue a GET request on a top level entity endpoint and sort the returned todos in ascending order.

## GET /todos (200) ? _sortBy ascending

> Issue a GET request on the `/todos` end point with a query parameter to sort todos ascending by a field, requesting the response in JSON format.

- `GET` request will return items from the `/todos` end point
- `200` is the success code meaning the request was accepted
- `_sortBy` is the URL parameter used to sort collection results
- use a todo field name as the value, e.g. `_sortBy=title`
- a plain field name sorts ascending
- `+field` also means ascending, but using just the field name avoids client URL encoding surprises
- ensure header `Accept: application/json` exists so the filtered and sorted todo collection is visible as JSON

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The request should have an `Accept: application/json` header so the API returns todos in JSON format
- Add `_sortBy=title` as a URL parameter:
    - `{{<ORIGIN_URL>}}/todos?_sortBy=title`
- The response status code should be `200` because the request is accepted
- Check that the returned todos are ordered by `title` from A to Z

### Try it now

{{<api-live-request method="GET" path="/todos?_sortBy=title" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?_sortBy=title to sort todos by title" open="true">}}

## Example Request

~~~~~~~~
> GET /todos?_sortBy=title HTTP/1.1
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
      "id": 4,
      "title": "file paperwork",
      "doneStatus": false,
      "description": ""
    },
    {
      "id": 2,
      "title": "process payroll",
      "doneStatus": true,
      "description": ""
    }
  ]
}
```

## Lessons Learned

- `_sortBy=+id` makes ascending order explicit instead of relying on a default.
- Sorting tests should compare adjacent values, not just the first and last record.
- A stable sort gives clients predictable collection browsing.

## Suggested Experiments

- Sort by `+title` and compare alphabetic order with `+id`.
- Add a new todo and confirm its position is based on the sorted field, not creation time alone.
