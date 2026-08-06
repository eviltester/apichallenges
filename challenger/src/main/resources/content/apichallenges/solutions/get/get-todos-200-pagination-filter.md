---
date: 2026-07-30T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos 200 pagination and filter
seo_title: "Solution: GET todos 200 pagination and filter | API Challenges"
description: How to solve API challenge GET todos 200 pagination and filter using a todo field filter with _limit and _offset.
seo_description: Use this walkthrough to solve GET todos 200 pagination and filter with doneStatus=false, _limit, _offset, and expected status 200.
next_challenge: /apichallenges/solutions/head/head-todos-200
concepts_learned: HTTP GET||200 OK||query parameters||filtering
concept_summary: Use this challenge to learn how query parameters filter a collection resource.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add doneStatus=false with _limit=2 and _offset=1||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos (200) ?filter&_limit&_offset`

How to issue a GET request on a top level entity endpoint and combine a todo field filter with pagination.

## GET /todos (200) ?filter&_limit&_offset

> Issue a GET request on the `/todos` end point with a query filter and pagination parameters.

- `doneStatus=false` filters the collection to only not done todos
- `_limit=2` asks for 2 returned todos
- `_offset=1` skips 1 todo after filtering
- filtering is applied before pagination
- if your current session has fewer matching todos after the offset, the response will contain fewer todos

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Add `doneStatus=false`, `_limit=2`, and `_offset=1` as URL parameters:
    - `{{<ORIGIN_URL>}}/todos?doneStatus=false&_limit=2&_offset=1`
- The response status code should be `200` because the request is accepted
- Check that the response body contains no more than 2 todos and all returned todos have `doneStatus` set to `false`

### Try it now

{{<api-live-request method="GET" path="/todos?doneStatus=false&_limit=2&_offset=1" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?doneStatus=false&_limit=2&_offset=1 to filter and paginate todos" open="true">}}

## Example Request

~~~~~~~~
> GET /todos?doneStatus=false&_limit=2&_offset=1 HTTP/1.1
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
    { "id": 2, "title": "not done todo", "doneStatus": false, "description": "" },
    { "id": 3, "title": "another not done todo", "doneStatus": false, "description": "" }
  ]
}
```

## Lessons Learned

- Filtering before pagination changes which records are eligible for a page.
- Small `_limit` values make it easier to spot whether pagination happens after filtering.
- Page assertions should track both result count and field criteria.

## Suggested Experiments

- Use `doneStatus=true&_limit=1&_offset=0`, then increase `_offset` and ensure every page still contains only done todos.
- Compare a filtered page with the same `_limit` and `_offset` without the filter to see how the dataset changes.