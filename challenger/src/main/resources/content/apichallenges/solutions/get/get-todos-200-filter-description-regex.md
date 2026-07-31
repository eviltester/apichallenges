---
date: 2026-07-31T09:00:00Z
lastmod: 2026-07-31
title: API Challenges Solution For - GET todos 200 filter description regex
seo_title: "Solution: GET todos filter description regex | API Challenges"
description: How to solve API challenge GET todos 200 filter description regex using a regular expression description filter.
seo_description: Use this walkthrough to solve GET todos filter description regex with a regular expression query and expected status 200.
next_challenge: /apichallenges/solutions/get/get-todos-200-filter-description-wildcard
schema_howto_steps: Create a todo with a non-empty matching description if needed||Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add a description regex filter and verify status 200
showads: true
---

# How to complete the challenge `GET /todos (200) ? filter description regex`

How to issue a GET request on a top level entity endpoint and filter todos by matching the description with a regular expression.

## GET /todos (200) ? filter description regex

> Issue a GET request on the `/todos` end point with a regular expression filter on description that returns todos with non-empty descriptions.

- `description~=.*fixture.*` means return todos where the description matches the regular expression
- most tools and browsers will encode any reserved characters for you when sending the request
- the response should contain at least one todo
- every returned todo should have a non-empty `description`
- every returned description should match the regular expression

## Basic Instructions

- Create or update a todo so it has a description containing `fixture`
- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Add a regular expression filter:
    - `{{<ORIGIN_URL>}}/todos?description~=.*fixture.*`
- The response status code should be `200` because the request is accepted
- Check that every returned description is non-empty and contains `fixture`

### Try it now

Create a TODO with a matching description if necessary:

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"regex filter fixture","doneStatus":false,"description":"created fixture for regex filter"}'>}}

Filter by regular expression:

{{<api-live-request method="GET" path="/todos?description~=.*fixture.*" expected-status="200" headers="Accept: application/json">}}

## Example Request

~~~~~~~~
> GET /todos?description~=.*fixture.* HTTP/1.1
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
      "id": 8,
      "title": "regex filter fixture",
      "doneStatus": false,
      "description": "created fixture for regex filter"
    }
  ]
}
```
