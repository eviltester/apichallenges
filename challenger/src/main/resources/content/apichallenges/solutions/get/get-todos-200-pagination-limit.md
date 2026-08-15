---
date: 2026-07-30T09:00:00Z
lastmod: 2026-08-11
title: API Challenges Solution For - GET todos 200 pagination limit
seo_title: "Solution: GET todos 200 pagination limit | API Challenges"
description: How to solve API challenge GET todos 200 pagination limit using the _limit query parameter.
seo_description: Use this walkthrough to solve GET todos 200 pagination limit by requesting up to 8 todos with _limit and verifying the response status is 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-pagination-limit-offset
concepts_learned: HTTP GET||200 OK||query parameters||pagination
concept_summary: Use this challenge to learn how limit and offset query parameters paginate a collection resource.
concept_reference_label: HTTP GET Verb
concept_reference_url: /reference/http-verbs/http-get
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add Accept application/json so the response is JSON||Add _limit=8 to limit the returned todos||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /api/todos (200) ?_limit`

How to issue a GET request on a top level entity endpoint and use pagination to limit the returned todos.

## GET /api/todos (200) ?_limit

> Issue a GET request on the `/api/todos` end point with a pagination limit to retrieve up to 8 todos, requesting the response in JSON format.

- `_limit` controls the maximum number of todos returned
- the maximum supported `_limit` value is `20`
- this challenge is passed by requesting up to 8 todos and receiving a `200` response
- if your current session has fewer than 8 todos, the response will contain fewer todos
- if necessary, add header `Accept: application/json` so the filtered and sorted todo collection is visible as JSON

## Basic Instructions

- Issue a `GET` request to end point "/api/todos"
    - `{{<ORIGIN_URL>}}/api/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The request should have an `Accept: application/json` header so the API returns todos in JSON format
- Add `_limit=8` as a URL parameter:
    - `{{<ORIGIN_URL>}}/api/todos?_limit=8`
- The response status code should be `200` because the request is accepted
- Check that the response body contains no more than 8 todos

### Try it now

{{<api-live-request method="GET" path="/api/todos?_limit=8" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/todos?_limit=8 to limit the number of todos returned" open="true">}}

## Example Request

~~~~~~~~
> GET /api/todos?_limit=8 HTTP/1.1
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
    { "id": 1, "title": "scan paperwork", "doneStatus": false, "description": "" },
    { "id": 2, "title": "file paperwork", "doneStatus": false, "description": "" },
    { "id": 3, "title": "process payroll", "doneStatus": false, "description": "" },
    { "id": 4, "title": "review invoices", "doneStatus": false, "description": "" },
    { "id": 5, "title": "update calendar", "doneStatus": false, "description": "" },
    { "id": 6, "title": "archive notes", "doneStatus": false, "description": "" },
    { "id": 7, "title": "send report", "doneStatus": false, "description": "" },
    { "id": 8, "title": "book meeting", "doneStatus": false, "description": "" }
  ]
}
```

## Lessons Learned

- `_limit` is a client-side request for a maximum number of returned todos.
- A response with fewer items than `_limit` can be correct when the collection is smaller.
- Limit tests should count array entries, not only read the status code.

## Suggested Experiments

- Compare `_limit=1`, `_limit=2`, and no `_limit` to see how the collection size changes.
- Set `_limit=0` and inspect whether the API treats zero as an empty page or invalid input.
