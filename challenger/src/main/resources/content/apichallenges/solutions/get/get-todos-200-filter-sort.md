---
date: 2026-07-30T09:00:00Z
lastmod: 2026-08-11
title: API Challenges Solution For - GET todos 200 filter and sort
seo_title: "Solution: GET todos 200 filter and sort | API Challenges"
description: How to solve API challenge GET todos 200 filter and sort using a query filter with the _sortBy query parameter.
seo_description: Use this walkthrough to solve GET todos 200 filter and sort with a query filter, _sortBy, and expected status 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-pagination-limit
concepts_learned: HTTP GET||200 OK||query parameters||filtering and sorting
concept_summary: Use this challenge to learn how filtering and sorting query parameters combine on a collection resource.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add Accept application/json so the response is JSON||Add a todo query filter and _sortBy to sort the filtered results||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos (200) ? filter and _sortBy`

How to issue a GET request on a top level entity endpoint, filter the returned todos, and sort the filtered results.

## GET /todos (200) ? filter and _sortBy

> Issue a GET request on the `/todos` end point with a query filter and a query parameter to sort the filtered todos, requesting the response in JSON format.

- todo fields can be used as URL parameters to filter collection results
- `_sortBy` is the URL parameter used to sort collection results
- prefix a sort field with `-` to sort descending
- for example, `doneStatus=false&_sortBy=-id` returns only not done todos, sorted by `id` from highest to lowest
- ensure header `Accept: application/json` exists so the filtered and sorted todo collection is visible as JSON

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The request should have an `Accept: application/json` header so the API returns todos in JSON format
- Add `doneStatus=false` and `_sortBy=-id` as URL parameters:
    - `{{<ORIGIN_URL>}}/todos?doneStatus=false&_sortBy=-id`
- The response status code should be `200` because the request is accepted
- Check that all returned todos have `doneStatus` set to `false`, and that the ids are descending

### Try it now

{{<api-live-request method="GET" path="/todos?doneStatus=false&_sortBy=-id" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?doneStatus=false&_sortBy=-id to filter and sort todos" open="true">}}

## Example Request

~~~~~~~~
> GET /todos?doneStatus=false&_sortBy=-id HTTP/1.1
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
      "title": "not done with highest id",
      "doneStatus": false,
      "description": ""
    },
    {
      "id": 3,
      "title": "not done with lower id",
      "doneStatus": false,
      "description": ""
    }
  ]
}
```

## Lessons Learned

- Combining a filter with `_sortBy` tests operation order: narrow the set, then order the survivors.
- Multi-parameter queries need assertions for both inclusion and ordering.
- Sorting a filtered subset can hide missed data if you only inspect the first item.

## Suggested Experiments

- Sort the filtered results by `-title` and verify every returned todo still matches `doneStatus`.
- Add one done and one not-done fixture with similar titles to prove sorting does not override filtering.
