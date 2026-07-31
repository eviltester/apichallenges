---
date:  2026-07-30T22:16:00Z
lastmod: 2026-07-30
title: API Challenges Solution For - GET todos export HTML download
seo_title: Solution: GET todos export HTML download | API Challenges
description: How to solve API challenge GET todos export HTML download by using format=html and checking the Content-Disposition response header.
seo_description: Export todos as HTML, verify the response is a file attachment, and check the Content-Disposition filename todos.html to complete the challenge.
next_challenge: /apichallenges/solutions/content-disposition-header/get-todos-export-tsv
schema_howto_steps: Create a GET request to /todos/export?format=html||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200||Check the Content-Type starts with text/html||Check the Content-Disposition header is attachment with filename todos.html
showads: true
---

# How to complete the challenge `GET /todos/export (200) HTML download`

This challenge asks for an HTML export of the todos. The important part is not only the HTML body; the response should also include a `Content-Disposition` header that marks it as a downloadable attachment.

## GET /todos/export HTML download

> Issue a GET request on the `/todos/export?format=html` end point and receive an HTML response with a `Content-Disposition` header for `todos.html`.

- `GET` asks the API to return the todos.
- `/todos/export` uses a `format` query parameter to choose the representation.
- `format=html` requests an HTML representation.
- `Content-Disposition: attachment` tells the client this can be treated as a file download.
- `filename="todos.html"` suggests the download filename.
- Add the `X-CHALLENGER` header to track progress.

## Basic Instructions

- Issue a `GET` request to end point "/todos/export?format=html"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos/export?format=html`
- The request should have an `X-CHALLENGER` header to track challenge completion.
- The response status code should be `200`.
- Check the `Content-Type` response header starts with `text/html`.
- Check the `Content-Disposition` response header is `attachment; filename="todos.html"`.
- Check the response body contains an HTML table.

The `format` query parameter controls the export type for this endpoint. You can send an `Accept` header, but this challenge is solved by requesting `format=html` and checking the response headers.

### Try it now

{{<api-live-request method="GET" path="/todos/export?format=html" expected-status="200" headers="Accept: text/html" details="true" summary="GET /todos/export?format=html to download todos as HTML" open="true">}}

## Example Request

~~~~~~~~
> GET /todos/export?format=html HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: text/html
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: text/html
< Content-Disposition: attachment; filename="todos.html"
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
~~~~~~~~

Example Response body:

```html
<table>
  <tr><th>id</th><th>title</th><th>doneStatus</th><th>description</th></tr>
  <tr><td>1</td><td>scan paperwork</td><td>false</td><td></td></tr>
</table>
```

## Extra Experiment

Open [todos.html](/todos/export?format=html) in a browser or save the response body as `todos.html`. The same response can be inspected as data or treated as a downloadable HTML file because of the response headers.

Other `format` parameter values to try include [json](/todos/export?format=json), [xml](/todos/export?format=xml), [csv](/todos/export?format=csv), [text](/todos/export?format=text), [ndjson](/todos/export?format=ndjson), [jsonl](/todos/export?format=jsonl), [json-seq](/todos/export?format=json-seq), [tsv](/todos/export?format=tsv), and [tab-delimited](/todos/export?format=tab-delimited). The text aliases `txt` and `plain`, JSON sequence alias `jsonseq`, and tab aliases `tab`, `tabs`, and `tab-separated` are also supported.
