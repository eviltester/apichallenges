---
date: 2026-07-30T09:00:00Z
lastmod: 2026-08-11
title: API Challenges Solution For - GET todos 200 pagination and sort
seo_title: "Solution: GET todos 200 pagination and sort | API Challenges"
description: How to solve API challenge GET todos 200 pagination and sort using _sortBy, _limit, and _offset query parameters.
seo_description: Use this walkthrough to solve GET todos 200 pagination and sort with _sortBy=-id, _limit, _offset, and expected status 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-pagination-filter
concepts_learned: HTTP GET||200 OK||query parameters||sorting
concept_summary: Use this challenge to learn how query parameters sort a collection resource.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add Accept application/json so the response is JSON||Add _sortBy=-id with _limit=5 and _offset=5||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos (200) ?_sortBy&_limit&_offset`

How to issue a GET request on a top level entity endpoint and combine sorting with pagination.

## GET /todos (200) ?_sortBy&_limit&_offset

> Issue a GET request on the `/todos` end point with sorting and pagination parameters, requesting the response in JSON format.

- `_sortBy=-id` sorts todos by `id` from highest to lowest
- `_limit=5` asks for 5 returned todos
- `_offset=5` skips 5 todos after sorting
- sorting is applied before pagination
- if your current session has fewer todos after the offset, the response will contain fewer todos
- double check that header `Accept: application/json` exists to ensure the filtered and sorted todo collection is visible as JSON

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The request should have an `Accept: application/json` header so the API returns todos in JSON format
- Add `_sortBy=-id`, `_limit=5`, and `_offset=5` as URL parameters:
    - `{{<ORIGIN_URL>}}/todos?_sortBy=-id&_limit=5&_offset=5`
- The response status code should be `200` because the request is accepted
- Check that the response body contains no more than 5 todos and the ids are still descending

### Try it now

{{<api-live-request method="GET" path="/todos?_sortBy=-id&_limit=5&_offset=5" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?_sortBy=-id&_limit=5&_offset=5 to sort and paginate todos" open="true">}}

## Example Request

~~~~~~~~
> GET /todos?_sortBy=-id&_limit=5&_offset=5 HTTP/1.1
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
    { "id": 15, "title": "fifth page item", "doneStatus": false, "description": "" },
    { "id": 14, "title": "sixth page item", "doneStatus": false, "description": "" },
    { "id": 13, "title": "seventh page item", "doneStatus": false, "description": "" },
    { "id": 12, "title": "eighth page item", "doneStatus": false, "description": "" },
    { "id": 11, "title": "ninth page item", "doneStatus": false, "description": "" }
  ]
}
```

## Lessons Learned

- Sorting before pagination makes page contents predictable across repeated requests.
- Pagination plus `_sortBy` needs assertions about global order, not just each page separately.
- Changing sort direction should change which records appear on the first page.

## Suggested Experiments

- Request `_sortBy=+id&_limit=2`, then `_sortBy=-id&_limit=2`, and compare first-page ids.
- Move from `_offset=0` to `_offset=2` with the same sort and check that the ordered sequence continues.
