---
date: 2026-07-31T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos 200 filter description wildcard
seo_title: "Solution: GET todos filter description wildcard | API Challenges"
description: How to solve API challenge GET todos 200 filter description wildcard using a wildcard description filter.
seo_description: Use this walkthrough to solve GET todos filter description wildcard with a wildcard query and expected status 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-sort-ascending
concepts_learned: HTTP GET||200 OK||query parameters||filtering
concept_summary: Use this challenge to learn how query parameters filter a collection resource.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a todo with a non-empty matching description if needed||Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add a description wildcard filter and verify status 200
showads: true
---


# How to complete the challenge `GET /todos (200) ? filter description wildcard`

How to issue a GET request on a top level entity endpoint and filter todos by matching the description with a wildcard pattern.

## GET /todos (200) ? filter description wildcard

> Issue a GET request on the `/todos` end point with a wildcard filter on description that returns todos with non-empty descriptions.

- `description*=*fixture*` means return todos where the description contains `fixture`
- `*` matches any number of characters
- `?` matches a single character
- most tools and browsers will encode reserved characters for you when sending the request
- every returned todo should have a non-empty matching `description`

## Basic Instructions

- Create or update a todo so it has a description containing `fixture`
- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Add a wildcard filter:
    - `{{<ORIGIN_URL>}}/todos?description*=*fixture*`
- The response status code should be `200` because the request is accepted
- Check that every returned description is non-empty and contains `fixture`

### Try it now

If you need a matching todo, create one with a non-empty description containing `fixture`. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"wildcard filter fixture","doneStatus":false,"description":"created fixture for wildcard filter"}' details="true" summary="POST /todos to create a wildcard filter fixture">}}

Filter by wildcard:

{{<api-live-request method="GET" path="/todos?description*=*fixture*" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?description*=*fixture* to filter descriptions by wildcard" open="true">}}

## Example Request

~~~~~~~~
> GET /todos?description*=*fixture* HTTP/1.1
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
      "id": 9,
      "title": "wildcard filter fixture",
      "doneStatus": false,
      "description": "created fixture for wildcard filter"
    }
  ]
}
```

## Lessons Learned

- The `description*=` query uses wildcard matching rather than regular-expression syntax.
- `*` and `?` patterns are easier for broad searches but less precise than regex filters.
- Wildcard tests should prove both matching and non-matching descriptions.

## Suggested Experiments

- Change the wildcard from `*fixture*` to `created*` and compare how many descriptions match.
- Try a single-character `?` wildcard in the pattern and confirm it behaves differently from `*`.