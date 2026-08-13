---
date: 2026-07-31T09:00:00Z
lastmod: 2026-08-11
title: API Challenges Solution For - GET todos 200 filter id greater than
seo_title: "Solution: GET todos filter id greater than | API Challenges"
description: How to solve API challenge GET todos 200 filter id greater than using an id greater-than query filter.
seo_description: Use this walkthrough to solve GET todos filter id greater than with an id greater-than query parameter and expected status 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-filter-id-less-than
concepts_learned: HTTP GET||200 OK||query parameters||filtering
concept_summary: Use this challenge to learn how query parameters filter a collection resource.
concept_reference_label: HTTP GET Verb
concept_reference_url: /reference/http-verbs/http-get
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add Accept application/json so the response is JSON||Add an id greater-than filter that returns some, but not all, todos||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos (200) ? filter id greater than`

How to issue a GET request on a top level entity endpoint and filter todos by ids greater than a supplied value.

## GET /todos (200) ? filter id greater than

> Issue a GET request on the `/todos` end point with an id filter to return todos with an id greater than a supplied value, requesting the response in JSON format.

- `id>5` means return todos where the id is greater than 5
- most tools and browsers will encode the `>` symbol for you when sending the request
- the response should contain at least one todo
- the response should be a subset of the current todos, not the full list
- every returned todo should have an `id` greater than the threshold
- the returned todo list should be JSON, so send `Accept: application/json` if the response is not JSON

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- The request should have an `Accept: application/json` header so the API returns todos in JSON format
- Add an id greater-than filter:
    - `{{<ORIGIN_URL>}}/todos?id>5`
- The response status code should be `200` because the request is accepted
- Check that every returned todo has an `id` greater than `5`

### Try it now

If you need to check which todo ids are available, use `GET /todos`. [See the solution](/apichallenges/solutions/get/get-todos-200).

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see the available todo ids">}}

The sample below uses `id>5`; edit the threshold if your current data needs a different subset.

{{<api-live-request method="GET" path="/todos?id>5" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?id>5 to return todos with ids greater than 5" open="true">}}

## Example Request

~~~~~~~~
> GET /todos?id>5 HTTP/1.1
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
    { "id": 6, "title": "higher id todo", "doneStatus": false, "description": "" }
  ]
}
```

## Lessons Learned

- Numeric comparison filters such as `id>1` depend on ordering and existing fixture data.
- Greater-than filters are boundary tests: the value equal to the boundary should not be included.
- The response can be correct even when ids are not contiguous.

## Suggested Experiments

- Compare `id>1` with `id>2`, and inspect which boundary ids disappear.
- Create a new todo, note its `id`, then filter above and below that `id` to test the comparison.
