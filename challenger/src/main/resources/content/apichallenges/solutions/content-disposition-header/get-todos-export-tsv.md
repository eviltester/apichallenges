---
date:  2026-07-30T22:17:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - GET todos export tab-delimited download
seo_title: Solution: GET todos export tab-delimited download | API Challenges
description: How to solve API challenge GET todos export tab-delimited download by using format=tsv and checking the Content-Disposition response header.
seo_description: Export todos as tab-separated values, verify the response is a file attachment, and check the Content-Disposition filename todos.tsv to complete the challenge.
next_challenge: /apichallenges/solutions/manage-session/get-challenger-guid-existing-x-challenger-200
concepts_learned: HTTP GET||200 OK||Content-Disposition||download response
concept_summary: Use this challenge to learn how response headers describe a TSV file download.
concept_reference_label: HTTP Basics
concept_reference_url: /reference/http-basics
concept_reference_label_2: API Testing Concepts and Coverage
concept_reference_url_2: /reference/testing-apis
schema_howto_steps: Create a GET request to /todos/export?format=tsv||Include X-CHALLENGER so the challenge is tracked in your current session||Send the request and verify the response status is 200||Check the Content-Type starts with text/tab-separated-values||Check the Content-Disposition header is attachment with filename todos.tsv
showads: true
---


# How to complete the challenge `GET /todos/export (200) tab-delimited download`

This challenge exports todos as tab-delimited data. Tab-delimited content is often called TSV, which means tab-separated values. The endpoint accepts both `format=tsv` and `format=tab-delimited`.

## GET /todos/export tab-delimited download

> Issue a GET request on the `/todos/export?format=tsv` end point and receive a tab-delimited response with a `Content-Disposition` header for `todos.tsv`.

- `GET` asks the API to return the todos.
- `/todos/export` uses a `format` query parameter to choose the representation.
- `format=tsv` requests tab-separated values.
- `format=tab-delimited` is an alias for the same export.
- `Content-Disposition: attachment` tells the client this can be treated as a file download.
- `filename="todos.tsv"` suggests the download filename.
- Add the `X-CHALLENGER` header to track progress.

## Basic Instructions

- Issue a `GET` request to end point "/todos/export?format=tsv"
    - if running locally that endpoint would be
        - `{{<ORIGIN_URL>}}/todos/export?format=tsv`
- The request should have an `X-CHALLENGER` header to track challenge completion.
- The response status code should be `200`.
- Check the `Content-Type` response header starts with `text/tab-separated-values`.
- Check the `Content-Disposition` response header is `attachment; filename="todos.tsv"`.
- Check the response body contains tab-delimited data.

The `format` query parameter controls the export type for this endpoint. For this challenge, `format=tsv` and `format=tab-delimited` should both produce the same response type and filename.

### Try it now

{{<api-live-request method="GET" path="/todos/export?format=tsv" expected-status="200" headers="Accept: text/tab-separated-values" details="true" summary="GET /todos/export?format=tsv to download todos as TSV" open="true">}}

## Example Request

~~~~~~~~
> GET /todos/export?format=tsv HTTP/1.1
> Host: {{<HOST_URL>}}
> User-Agent: rest-client
> X-CHALLENGER: x-challenger-guid
> Accept: text/tab-separated-values
~~~~~~~~

## Example Response

~~~~~~~~
< HTTP/1.1 200 OK
< Content-Type: text/tab-separated-values
< Content-Disposition: attachment; filename="todos.tsv"
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Headers: *
< X-Challenger: x-challenger-guid
~~~~~~~~

Example Response body:

```text
id	title	doneStatus	description
1	scan paperwork	false	
2	file paperwork	true	
```

## Extra Experiment

Open [todos.tsv](/todos/export?format=tsv) in your browser to request the tab-delimited export directly.

Other `format` parameter values to try include [json](/todos/export?format=json), [xml](/todos/export?format=xml), [csv](/todos/export?format=csv), [text](/todos/export?format=text), [html](/todos/export?format=html), [ndjson](/todos/export?format=ndjson), [jsonl](/todos/export?format=jsonl), [json-seq](/todos/export?format=json-seq), and [tab-delimited](/todos/export?format=tab-delimited). The text aliases `txt` and `plain`, JSON sequence alias `jsonseq`, and tab aliases `tab`, `tabs`, and `tab-separated` are also supported.

Repeat the request with `/todos/export?format=tab-delimited`. It should still return `Content-Type: text/tab-separated-values` and `Content-Disposition: attachment; filename="todos.tsv"`.

## Lessons Learned

- `text/tab-separated-values` is useful when commas in data would make `CSV` assertions noisy.
- A `TSV` export should use tab delimiters while preserving the same todo fields.
- Header validation matters because downloaded text formats can look similar at a glance.

## Suggested Experiments

- Inspect the raw payload and confirm fields are separated by tab characters rather than commas.
- Import the `TSV` into a spreadsheet and compare column alignment with the `CSV` export.