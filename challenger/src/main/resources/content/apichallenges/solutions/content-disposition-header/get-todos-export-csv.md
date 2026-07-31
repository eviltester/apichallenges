---
date:  2026-07-30T22:15:00Z
lastmod: 2026-07-30
title: API Challenges Solution For - GET todos export CSV download
seo_title: Solution: GET todos export CSV download | API Challenges
description: How to solve API challenge GET todos export CSV download by using the format query parameter and checking the Content-Disposition response header.
seo_description: Export todos as CSV, verify the response is a file attachment, and check the Content-Disposition filename todos.csv to complete the challenge.
next_challenge: /apichallenges/solutions/content-disposition-header/get-todos-export-html
schema_howto_steps: Create a GET request to /todos/export?format=csv||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200||Check the Content-Type starts with text/csv||Check the Content-Disposition header is attachment with filename todos.csv
showads: true
---

# How to complete the challenge `GET /todos/export (200) CSV download`

This challenge uses an export endpoint instead of the normal `GET /todos` endpoint. The response body is CSV and the response headers tell the client to treat the response as a downloadable file.

## GET /todos/export CSV download

> Issue a GET request on the `/todos/export?format=csv` end point and receive a CSV response with a `Content-Disposition` header for `todos.csv`.

- `GET` asks the API to return the todos.
- `/todos/export` uses a `format` query parameter to choose the representation.
- `format=csv` requests comma-separated values.
- `Content-Disposition: attachment` tells the client this can be treated as a file download.
- `filename="todos.csv"` suggests the download filename.
- Add the `X-CHALLENGER` header to track progress.

## Basic Instructions

- Issue a `GET` request to end point "/todos/export?format=csv"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos/export?format=csv`
- The request should have an `X-CHALLENGER` header to track challenge completion.
- The response status code should be `200`.
- Check the `Content-Type` response header starts with `text/csv`.
- Check the `Content-Disposition` response header is `attachment; filename="todos.csv"`.
- Check the response body contains CSV data.

The `format` query parameter controls the export type for this endpoint, so this challenge does not depend on the request `Accept` header.

### Try it now

{{<api-live-request method="GET" path="/todos/export?format=csv" expected-status="200" headers="Accept: text/csv" details="true" summary="GET /todos/export?format=csv to download todos as CSV" open="true">}}

## Example Request

~~~~~~~~
> GET /todos/export?format=csv HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: text/csv
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: text/csv
< Content-Disposition: attachment; filename="todos.csv"
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
~~~~~~~~

Example Response body:

```csv
id,title,doneStatus,description
1,scan paperwork,false,
2,file paperwork,true,
```

## Extra Experiment

Open [todos.csv](/todos/export?format=csv) in your browser to request the CSV export directly.

Other `format` parameter values to try include [json](/todos/export?format=json), [xml](/todos/export?format=xml), [text](/todos/export?format=text), [html](/todos/export?format=html), [ndjson](/todos/export?format=ndjson), [jsonl](/todos/export?format=jsonl), [json-seq](/todos/export?format=json-seq), [tsv](/todos/export?format=tsv), and [tab-delimited](/todos/export?format=tab-delimited). The text aliases `txt` and `plain`, JSON sequence alias `jsonseq`, and tab aliases `tab`, `tabs`, and `tab-separated` are also supported.

Try changing the request `Accept` header to `application/json` while keeping `format=csv`. The export endpoint should still return CSV because the format is selected by the query parameter.
