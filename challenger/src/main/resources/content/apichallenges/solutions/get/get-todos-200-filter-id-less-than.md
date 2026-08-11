---
date: 2026-07-31T09:00:00Z
lastmod: 2026-08-11
title: API Challenges Solution For - GET todos 200 filter id less than
seo_title: "Solution: GET todos filter id less than | API Challenges"
description: How to solve API challenge GET todos 200 filter id less than using an id less-than query filter.
seo_description: Use this walkthrough to solve GET todos filter id less than with an id less-than query parameter and expected status 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-filter-id-single-result
concepts_learned: HTTP GET||200 OK||query parameters||filtering
concept_summary: Use this challenge to learn how query parameters filter a collection resource.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add Accept application/json so the response is JSON||Add an id less-than filter that returns some, but not all, todos||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos (200) ? filter id less than`

How to issue a GET request on a top level entity endpoint and filter todos by ids less than a supplied value.

## GET /todos (200) ? filter id less than

> Issue a GET request on the `/todos` end point with an id filter to return todos with an id less than a supplied value, requesting the response in JSON format.

- `id<6` means return todos where the id is less than 6
- most tools and browsers will encode the `<` symbol for you when sending the request
- the response should contain at least one todo
- the response should be a subset of the current todos, not the full list
- every returned todo should have an `id` less than the threshold
- the returned todo list should be JSON, so send `Accept: application/json` if the response is not JSON

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The request should have an `Accept: application/json` header so the API returns todos in JSON format
- Add an id less-than filter:
    - `{{<ORIGIN_URL>}}/todos?id<6`
- The response status code should be `200` because the request is accepted
- Check that every returned todo has an `id` less than `6`

### Try it now

If you need to check which todo ids are available, use `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see the available todo ids">}}

The sample below uses `id<6`; edit the threshold if your current data needs a different subset.

{{<api-live-request method="GET" path="/todos?id<6" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?id<6 to return todos with ids less than 6" open="true">}}

## Example Request

~~~~~~~~
> GET /todos?id<6 HTTP/1.1
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
    { "id": 2, "title": "lower id todo", "doneStatus": false, "description": "" }
  ]
}
```

## Lessons Learned

- `id<` filtering checks the lower side of numeric range handling.
- The boundary value itself should be excluded from a strict less-than result.
- Empty results can still be valid when no resource satisfies the comparison.

## Suggested Experiments

- Use the smallest known todo `id` as the boundary and confirm the response becomes empty or very small.
- Pair `id<` with a `GET /todos` baseline so you can explain every returned `id`.
