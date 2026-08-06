---
date:  2021-01-30T09:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos 200 filter
seo_title: Solution: GET todos 200 filter | API Challenges
description: How to solve API challenge GET todos 200 filter to use URL parameters to filter the results.
seo_description: Use this walkthrough to solve GET todos 200 filter with request setup, key headers, and expected status codes so you can complete the challenge confidently.
next_challenge: /apichallenges/solutions/get/get-todos-200-filter-id-greater-than
concepts_learned: HTTP GET||200 OK||query parameters||filtering
concept_summary: Use this challenge to learn how query parameters filter a collection resource.
concept_reference_label: HTTP Methods and Verbs
concept_reference_url: /reference/http-verbs
concept_reference_label_2: HTTP Basics
concept_reference_url_2: /reference/http-basics
schema_howto_steps: Create a GET request to /todos||Include X-CHALLENGER so the challenge is tracked in your current session||Add the required query parameters and confirm the filtered todo results||Send the request and verify the response status is 200
showads: true
---


# How to complete the challenge `GET /todos (200) ? filter`

How to issue a GET request on a top level entity endpoint and use a query filter to receive a subset of data.

## GET /todos (200) ? filter

> 	Issue a GET request on the `/todos` end point with a query filter to get only todos which are 'done'. There must exist both 'done' and 'not done' todos, to pass this challenge.

- `GET` request will return all items from the `/todos` end point
- `200` is the success code meaning the request was accepted
- `?` means we need to add a URL Parameter
- `filter` means it will filter based on an attribute. In this case we are asked to filter on those which are 'done'. And this is represented by the `doneStatus` i.e. `doneStatus=true`
- We are using this request to `GET` a filter list of todo items
- Perform a `GET` first to see what the format of the message is
- Add a URL parameter to the request to repeat the `GET` and filter the list of todos e.g. `/todos?doneStatus=true`

## Basic Instructions

- Issue a `GET` request to end point "/todos"
    - `{{<ORIGIN_URL>}}/todos`
- The request should have an `X-CHALLENGER` header to track challenge completion
- Look at the returned format for todos

```js
    {
      "id": 41,
      "title": "create todo process payroll",
      "doneStatus": true,
      "description": ""
    },
```
- we want to use the `doneStatus` attribute as a URL parameter
- if you don't see any todos in the list with a `"doneStatus": true` then you will need to issue a `POST` request to create or amend a todo item. e.g. [challenge POST todos 201](/apichallenges/solutions/post-create/post-todos-201)
- Issue a `GET` request with a URL parameter `/todos?doneStatus=true`
- The response status code should be `200` because the request is accepted
- If you get a different response code, check the URL or headers of the message because you made have made a typo.
- If you don't see any todos returned then you may need to create one e.g. [challenge POST todos 201](/apichallenges/solutions/post-create/post-todos-201)

### Try it now

If you need fixture data, create one done todo and one not-done todo first. [See the solution](/apichallenges/solutions/post-create/post-todos-201).

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"done filter fixture","doneStatus":true,"description":"created for filtering"}' details="true" summary="POST /todos to create a done todo fixture">}}

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"not done comparison fixture","doneStatus":false,"description":"created for filtering"}' details="true" summary="POST /todos to create a not-done comparison todo">}}

Filter for done todos:

{{<api-live-request method="GET" path="/todos?doneStatus=true" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos?doneStatus=true to return only done todos" open="true">}}




## Example Request

~~~~~~~~
> GET /todos?doneStatus=true HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: application/json
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Connection: close
< Date: Tue, 15 Dec 2020 17:32:12 GMT
< Content-Type: application/json
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
< Server: Jetty(9.4.z-SNAPSHOT)
< Via: 1.1 vegur
~~~~~~~~

Returned body:

```json
{
  "todos": [
    {
      "id": 41,
      "title": "create todo process payroll",
      "doneStatus": true,
      "description": ""
    }
  ]
}
```

## Overview Video

{{<youtube-embed key="G-sLuhyPMuw" title="Solution to Get todos with query filter challenge">}}

[Patreon ad free version](https://www.patreon.com/posts/46603286)

## Lessons Learned

- `doneStatus=true` is an exact boolean filter over the todo collection.
- Filter challenges require fixture planning: there must be both matching and non-matching todos.
- A valid `200 OK` can still be a weak test if you do not assert every returned `doneStatus`.

## Suggested Experiments

- Create one `doneStatus=true` todo and one `doneStatus=false` todo, then confirm only the done item appears.
- Change the query to `doneStatus=false` and compare counts with the original done-only response.