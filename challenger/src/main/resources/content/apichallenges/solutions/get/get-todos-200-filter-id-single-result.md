---
date: 2026-07-31T09:00:00Z
lastmod: 2026-08-11
title: API Challenges Solution For - GET todos 200 filter id single result
seo_title: "Solution: GET todos filter id single result | API Challenges"
description: How to solve API challenge GET todos 200 filter id single result while multiple todos exist in the database.
seo_description: Use this walkthrough to solve GET todos filter id single result with an exact id query parameter and expected status 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-filter-description-regex
concepts_learned: HTTP GET||200 OK||query parameters||filtering
concept_summary: Use this challenge to learn how query parameters filter a collection resource.
concept_reference_label: HTTP GET Verb
concept_reference_url: /reference/http-verbs/http-get
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /api/todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add Accept application/json so the response is JSON||Make sure multiple todos exist, then filter by one exact id||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /api/todos (200) ? filter id single result`

How to issue a GET request on a top level entity endpoint and filter by one todo id while there are multiple todos in the database.

## GET /api/todos (200) ? filter id single result

> Issue a GET request on the `/api/todos` end point with an id filter that returns one todo while multiple todos exist in the database, requesting the response in JSON format.

- `id=3` means return the todo where the id is exactly 3
- there must be more than one todo in your current session
- the response should contain exactly one todo
- the returned todo should have the same `id` that you filtered on
- the returned todo list should be JSON, so send `Accept: application/json` if the response is not JSON

## Basic Instructions

- Issue a `GET` request to end point "/api/todos"
    - `{{<ORIGIN_URL>}}/api/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The request should have an `Accept: application/json` header so the API returns todos in JSON format
- Choose an id from the current todo list
- Add the id as an exact filter:
    - `{{<ORIGIN_URL>}}/api/todos?id=3`
- The response status code should be `200` because the request is accepted
- Check that the response contains one todo with the requested id

### Try it now

If you need to check which todo ids are available, use `GET /api/todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/api/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/todos to see the available todo ids">}}

If you do not have more than one todo, create another one using `POST /api/todos`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/api/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"single result comparison fixture","doneStatus":false,"description":"created for id filtering"}' details="true" summary="POST /api/todos to create another todo item">}}

The sample below uses `id=3`; edit the id if your current data uses a different todo id.

{{<api-live-request method="GET" path="/api/todos?id=3" expected-status="200" headers="Accept: application/json" details="true" summary="GET /api/todos?id=3 to return one matching todo" open="true">}}

## Example Request

~~~~~~~~
> GET /api/todos?id=3 HTTP/1.1
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

## Lessons Learned

- Filtering by exact `id` still returns a collection wrapper, not a single todo resource.
- Exact-match filters are useful for checking query semantics against path lookup semantics.
- A single-result collection should be verified by count and by field values.

## Suggested Experiments

- Compare `/api/todos?id=1` with `/api/todos/1` and note the difference between collection and resource responses.
- Filter by an `id` that does not exist and confirm the API returns an empty collection rather than `404 Not Found`.
