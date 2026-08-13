---
date: 2026-07-30T09:00:00Z
lastmod: 2026-08-11
title: API Challenges Solution For - GET todos 200 pagination limit and offset
seo_title: "Solution: GET todos 200 pagination limit and offset | API Challenges"
description: How to solve API challenge GET todos 200 pagination limit and offset using the _limit and _offset query parameters.
seo_description: Use this walkthrough to solve GET todos 200 pagination by combining _limit and _offset, checking the returned page size, and verifying status 200.
next_challenge: /apichallenges/solutions/get/get-todos-400-pagination-limit-too-high
concepts_learned: HTTP GET||200 OK||query parameters||pagination
concept_summary: Use this challenge to learn how limit and offset query parameters paginate a collection resource.
concept_reference_label: HTTP GET Verb
concept_reference_url: /reference/http-verbs/http-get
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add Accept application/json so the response is JSON||Add _limit=5 and _offset=5 to page through todos||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos (200) ?_limit&_offset`

How to issue a GET request on a top level entity endpoint and use pagination to skip records before returning a limited page.

## GET /todos (200) ?_limit&_offset

> Issue a GET request on the `/todos` end point with pagination limit and offset parameters, requesting the response in JSON format.

- `_limit` controls the maximum number of todos returned
- `_offset` controls how many todos are skipped before returning results
- if `_offset` is not supplied, the default offset is `0`
- this challenge is passed by requesting up to 5 todos after skipping 5 todos
- if your current session has fewer todos after the offset, the response will contain fewer todos
- header `Accept: application/json` should be present so the filtered and sorted todo collection is visible as JSON

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The request should have an `Accept: application/json` header so the API returns todos in JSON format
- Add `_limit=5` and `_offset=5` as URL parameters:
    - `{{<ORIGIN_URL>}}/todos?_limit=5&_offset=5`
- The response status code should be `200` because the request is accepted
- Check that the response body contains no more than 5 todos

### Try it now

{{<api-live-request method="GET" path="/todos?_limit=5&_offset=5" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?_limit=5&_offset=5 to request the next page of todos" open="true">}}

## Example Request

~~~~~~~~
> GET /todos?_limit=5&_offset=5 HTTP/1.1
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
    { "id": 6, "title": "archive notes", "doneStatus": false, "description": "" },
    { "id": 7, "title": "send report", "doneStatus": false, "description": "" },
    { "id": 8, "title": "book meeting", "doneStatus": false, "description": "" },
    { "id": 9, "title": "check stock", "doneStatus": false, "description": "" },
    { "id": 10, "title": "close ticket", "doneStatus": false, "description": "" }
  ]
}
```

## Lessons Learned

- `_limit` controls page size and `_offset` controls where the page starts in the collection.
- Offset-based pagination depends on a stable ordering, even when no explicit sort is supplied.
- Tests should record ids across pages to catch overlap or skipped records.

## Suggested Experiments

- Request `_limit=2&_offset=0`, then `_limit=2&_offset=2`, and check that ids do not repeat.
- Try an `_offset` beyond the collection size and observe whether the API returns an empty list or an error.
