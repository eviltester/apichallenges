---
date:  2026-07-30T22:15:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos export CSV download
seo_title: Solution: GET todos export CSV download | API Challenges
description: How to solve API challenge GET todos export CSV download by using the format query parameter and checking the Content-Disposition response header.
seo_description: Export todos as CSV, verify the response is a file attachment, and check the Content-Disposition filename todos.csv to complete the challenge.
next_challenge: /apichallenges/solutions/content-disposition-header/get-todos-export-html
concepts_learned: HTTP GET||200 OK||Content-Disposition||download response
concept_summary: Use this challenge to learn how response headers describe a CSV file download.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /reference/testing-apis
schema_howto_steps: Create a GET request to /api/todos/export?format=csv||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200||Check the Content-Type starts with text/csv||Check the Content-Disposition header is attachment with filename todos.csv
showads: true
---


# How to complete the challenge `GET /api/todos/export (200) CSV download`

This challenge uses an export endpoint instead of the normal `GET /api/todos` endpoint. The response body is CSV and the response headers tell the client to treat the response as a downloadable file.

## GET /api/todos/export CSV download

> Issue a GET request on the `/api/todos/export?format=csv` end point and receive a CSV response with a `Content-Disposition` header for `todos.csv`.

- `GET` asks the API to return the todos.
- `/api/todos/export` uses a `format` query parameter to choose the representation.
- `format=csv` requests comma-separated values.
- `Content-Disposition: attachment` tells the client this can be treated as a file download.
- `filename="todos.csv"` suggests the download filename.
- Add the `X-CHALLENGER` header to track progress.

## Basic Instructions

- Issue a `GET` request to end point "/api/todos/export?format=csv"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/api/todos/export?format=csv`
- The request should have an `X-CHALLENGER` header to track challenge completion.
- The response status code should be `200`.
- Check the `Content-Type` response header starts with `text/csv`.
- Check the `Content-Disposition` response header is `attachment; filename="todos.csv"`.
- Check the response body contains CSV data.

The `format` query parameter controls the export type for this endpoint, so this challenge does not depend on the request `Accept` header.

### Try it now

{{<api-live-request method="GET" path="/api/todos/export?format=csv" expected-status="200" headers="Accept: text/csv" details="true" summary="GET /api/todos/export?format=csv to download todos as CSV" open="true">}}

## Example Request

~~~~~~~~
> GET /api/todos/export?format=csv HTTP/1.1
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

Open [todos.csv](/api/todos/export?format=csv) in your browser to request the CSV export directly.

Other `format` parameter values to try include [json](/api/todos/export?format=json), [xml](/api/todos/export?format=xml), [text](/api/todos/export?format=text), [html](/api/todos/export?format=html), [ndjson](/api/todos/export?format=ndjson), [jsonl](/api/todos/export?format=jsonl), [json-seq](/api/todos/export?format=json-seq), [tsv](/api/todos/export?format=tsv), and [tab-delimited](/api/todos/export?format=tab-delimited). The text aliases `txt` and `plain`, JSON sequence alias `jsonseq`, and tab aliases `tab`, `tabs`, and `tab-separated` are also supported.

Try changing the request `Accept` header to `application/json` while keeping `format=csv`. The export endpoint should still return CSV because the format is selected by the query parameter.

## Lessons Learned

- `Content-Disposition` turns a normal API response into a download-oriented response.
- `text/csv` should produce comma-separated rows that spreadsheet tools can import.
- Export endpoints need header checks and payload checks because the status alone says little about file usability.

## Suggested Experiments

- Save the `CSV` response and confirm the filename extension matches the `Content-Disposition` value.
- Compare a field containing commas or quotes, if available, to see how the `CSV` serializer escapes it.