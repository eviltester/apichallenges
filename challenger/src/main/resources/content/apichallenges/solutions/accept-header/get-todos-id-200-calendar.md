---
date: 2026-08-01T10:00:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos id text calendar 200
seo_title: Solution: GET todo id text calendar 200 | API Challenges
description: How to solve API challenge GET todos id 200 text/calendar by requesting an existing todo as a calendar VTODO.
seo_description: Request an existing todo with Accept text/calendar and verify the VTODO response so you can complete the API Challenges content negotiation challenge.
next_challenge: /apichallenges/solutions/content-type-header/post-todos-xml
concepts_learned: HTTP GET||200 OK||Accept header||content negotiation
concept_summary: Use this challenge to learn how the Accept header changes the response format for text/calendar.
concept_reference_label: HTTP Basics
concept_reference_url: /tutorials/http-basics
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /tutorials/http-verbs
schema_howto_steps: Create or identify an existing todo||Create a GET request to /todos/{id}||Include X-CHALLENGER so the challenge is tracked in your current session||Set Accept to text/calendar||Send the request and verify the response status is 200 and Content-Type is text/calendar
showads: true
---


# How to complete the challenge `GET /todos/{id} (200) text/calendar`

This challenge asks for one existing todo in calendar format. The API returns a minimal `VCALENDAR` containing a single `VTODO`.

## GET /todos/{id} (200) text/calendar

> Issue a GET request on the `/todos/{id}` end point with an `Accept` header of `text/calendar` to receive the todo as a VTODO.

- Use a real todo id, not the literal text `{id}`
- Add the `Accept: text/calendar` header
- Add the `X-CHALLENGER` header to track progress
- The response status should be `200`
- The response `Content-Type` should be `text/calendar`

## Basic Instructions

- Find an existing todo by calling `GET /todos`
- If there are no todos, create one with `POST /todos`
- Issue a `GET` request to `/todos/{id}`
- Send `Accept: text/calendar`

### Try it now

If you don't know what todos are available, list them first.

{{<api-live-request method="GET" path="/todos" expected-status="200" headers="Accept: application/json" details="true" summary="GET /todos to see what todos are available now">}}

If there are no todos, create one.

{{<api-live-request method="POST" path="/todos" expected-status="201" headers="Content-Type: application/json||Accept: application/json" body='{"title":"calendar todo","doneStatus":false,"description":"created for the text/calendar solution"}' details="true" summary="POST /todos to create a todo for the calendar request">}}

{{<api-live-request method="GET" path="/todos/{{firstTodoId}}" expected-status="200" headers="Accept: text/calendar" details="true" summary="GET /todos/{id} as text/calendar" open="true">}}

## Example Request

~~~~~~~~
> GET /todos/1 HTTP/1.1
> Host: {{<HOST_URL>}}
> X-CHALLENGER: x-challenger-guid
> Accept: text/calendar
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: text/calendar
< X-Challenger: x-challenger-guid
~~~~~~~~

Example Response body:

```text
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//EvilTester//API Challenges//EN
BEGIN:VTODO
UID:todo-1@apichallenges
SUMMARY:calendar todo
DESCRIPTION:created for the text/calendar solution
STATUS:NEEDS-ACTION
END:VTODO
END:VCALENDAR
```

## Lessons Learned

- `text/calendar` demonstrates that a single todo can be represented as a `VTODO`, not only as `JSON` or `XML`.
- Collection and single-resource endpoints may support different media types.
- Content negotiation should verify both `Content-Type` and domain-specific body structure.

## Suggested Experiments

- Request `GET /todos/{id}` with `Accept: text/calendar` and then with `Accept: application/json` to compare fields.
- Try `Accept: text/calendar` on `/todos` and observe whether the collection supports the calendar representation.