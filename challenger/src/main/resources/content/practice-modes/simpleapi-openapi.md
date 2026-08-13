---
title: Simple API OpenAPI JSON Files
seo_title: Simple API OpenAPI JSON Downloads | API Practice Mode
description: Download the OpenAPI JSON files for the Simple API.
lastmod: 2026-08-12
seo_description: Use Simple API OpenAPI JSON to practice safely, understand request-response behavior, and build confidence with guided exercises before advanced.
showads: true
---

# Simple API OpenAPI Files

Download an OpenAPI JSON file to use in your REST Client.

## File Download Links

OpenAPI JSON files are available in the default format and in specific OpenAPI versions. The default `openapi.json` endpoint currently returns OpenAPI v 3.1.

- OpenAPI v 3.0 JSON [standard validation](/simpleapi/docs/openapi-3.0.json) [download](/simpleapi/docs/openapi-3.0.json?download) - [less validation](/simpleapi/docs/openapi-3.0.json?permissive) [download](/simpleapi/docs/openapi-3.0.json?permissive&download)
- OpenAPI v 3.1 JSON [standard validation](/simpleapi/docs/openapi-3.1.json) [download](/simpleapi/docs/openapi-3.1.json?download) - [less validation](/simpleapi/docs/openapi-3.1.json?permissive) [download](/simpleapi/docs/openapi-3.1.json?permissive&download)
- OpenAPI v 3.2 JSON [standard validation](/simpleapi/docs/openapi-3.2.json) [download](/simpleapi/docs/openapi-3.2.json?download) - [less validation](/simpleapi/docs/openapi-3.2.json?permissive) [download](/simpleapi/docs/openapi-3.2.json?permissive&download)

## Open OpenAPI 3.2 In Online UIs

Tool support for OpenAPI 3.2 is still emerging, so these links help compare how each UI handles the same file.

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <h3>Standard validation</h3>
    <p>Open the user-facing Simple API OpenAPI 3.2 file.</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Open in Swagger</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Open in OpenAPI Explorer</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Open in Scalar</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Open in Stoplight Elements</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Open in Zudoku</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Open in Redoc</a>
    </p>
  </div>
  <div class="openapi-ui-launch-group">
    <h3>Less validation</h3>
    <p>Open the more permissive Simple API OpenAPI 3.2 file for testing edge cases.</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Swagger</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in OpenAPI Explorer</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Scalar</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Stoplight Elements</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Zudoku</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Redoc</a>
    </p>
  </div>
</div>

OpenAPI 3.2 describes `QUERY` as a native method. OpenAPI 3.0 and 3.1 include `QUERY` details using a vendor extension so tools that do not yet understand OpenAPI 3.2 can still load the file.

Example `QUERY` request:

```http
QUERY /simpleapi/items HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Accept: application/json

type=book
```

The same endpoint also supports JSONPath QUERY bodies:

```http
QUERY /simpleapi/items HTTP/1.1
Content-Type: application/jsonpath
Accept: application/json

$.items[?(@.type == 'book')]
```

Useful JSONPath experiments include `$.items`, `$.items[?(@.type == 'cd')]`, `$.items[?(@.numberinstock == 0)]`, and `$.items[?(@.type == 'book' && @.numberinstock > 0)]`.

The same endpoint also supports Thingifier Structured JSON QUERY bodies:

```http
QUERY /simpleapi/items HTTP/1.1
Content-Type: application/vnd.thingifier.query+json
Accept: application/json

{"filter":{"type":"book"}}
```

Useful Structured JSON experiments include:

- `{"filter":{"type":"cd"}}`
- `{"filter":{"numberinstock":{"greaterThan":0}}}`
- `{"filter":{"price":{"lessThan":10}}}`
- `{"filter":{"isbn13":{"contains":"123"}}}`
- `{"filter":{"type":"book"},"sort":[{"field":"price","direction":"asc"}]}`

Read [HTTP Methods and Verbs](/reference/http-verbs/http-query#http-query-structured-json-body) for more about `QUERY` request bodies, JSONPath, and Structured JSON.

## Normal OpenAPI File Explained

The Normal OpenAPI File is intended for use as though you were a user.

It only lists endpoints that are valid to use, and has additional validation on the URL Parameters that you can enter.

When this type of file is loaded into a Swagger UI Generation application it makes it easy to USE the API but makes it harder to TEST the API.

- [OpenAPI v 3.0 JSON](/simpleapi/docs/openapi-3.0.json?download)
- [OpenAPI v 3.1 JSON](/simpleapi/docs/openapi-3.1.json?download)
- [OpenAPI v 3.2 JSON](/simpleapi/docs/openapi-3.2.json?download)

## Permissive OpenAPI File Explained

The Permissive OpenAPI File is intended for testing.

It lists all the end points with more Verbs i.e. even verbs that the API defines as not available.

The parameters are also possible to send as empty and type validation is not performed on the parameter values. This makes it possible to use Swagger UI applications to test more extreme situations.

- [OpenAPI v 3.0 JSON](/simpleapi/docs/openapi-3.0.json?permissive&download)
- [OpenAPI v 3.1 JSON](/simpleapi/docs/openapi-3.1.json?permissive&download)
- [OpenAPI v 3.2 JSON](/simpleapi/docs/openapi-3.2.json?permissive&download)
