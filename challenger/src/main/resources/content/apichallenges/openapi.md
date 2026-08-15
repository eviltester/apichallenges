---
title: API Challenges OpenAPI JSON Files
seo_title: OpenAPI JSON File for Practicing | API Challenges
description: Download the OpenAPI JSON files for the API Challenges.
lastmod: 2026-08-10
seo_description: Explore API Challenges OpenAPI JSON with practical guidance and actionable next steps designed to improve API testing skills through hands-on practice.
showads: true
---

# API Challenge OpenAPI Files

Download an OpenAPI JSON file to use in your REST Client.

## File Download Links

OpenAPI JSON files are available in the default format and in specific OpenAPI versions. The default `openapi.json` endpoint currently returns OpenAPI v 3.1.

- OpenAPI v 3.0 JSON [standard validation](/api/docs/openapi-3.0.json) [download](/api/docs/openapi-3.0.json?download) - [less validation](/api/docs/openapi-3.0.json?permissive) [download](/api/docs/openapi-3.0.json?permissive&download)
- OpenAPI v 3.1 JSON [standard validation](/api/docs/openapi-3.1.json) [download](/api/docs/openapi-3.1.json?download) - [less validation](/api/docs/openapi-3.1.json?permissive) [download](/api/docs/openapi-3.1.json?permissive&download)
- OpenAPI v 3.2 JSON [standard validation](/api/docs/openapi-3.2.json) [download](/api/docs/openapi-3.2.json?download) - [less validation](/api/docs/openapi-3.2.json?permissive) [download](/api/docs/openapi-3.2.json?permissive&download)

## Open OpenAPI 3.2 In Online UIs

Tool support for OpenAPI 3.2 is still emerging, so these links help compare how each UI handles the same file.

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <h3>Standard validation</h3>
    <p>Open the user-facing API Challenges OpenAPI 3.2 file.</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">Open in Swagger</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">Open in OpenAPI Explorer</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">Open in Scalar</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">Open in Stoplight Elements</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">Open in Zudoku</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">Open in Redoc</a>
    </p>
  </div>
  <div class="openapi-ui-launch-group">
    <h3>Less validation</h3>
    <p>Open the more permissive API Challenges OpenAPI 3.2 file for testing edge cases.</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Swagger</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in OpenAPI Explorer</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Scalar</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Stoplight Elements</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Zudoku</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Redoc</a>
    </p>
  </div>
</div>

## QUERY Method Support

OpenAPI 3.2 describes `QUERY` as a native method. OpenAPI 3.0 and 3.1 include `QUERY` details using a vendor extension so tools that do not yet understand OpenAPI 3.2 can still load the file.

Example `QUERY` request:

```http
QUERY /api/todos HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Accept: application/json

doneStatus=true
```

## About API Challenge's Normal OpenAPI File

The Normal OpenAPI File is intended for use as though you were a user.

It only lists endpoints that are valid to use, and has additional validation on the URL Parameters that you can enter.

When this type of file is loaded into a Swagger UI Generation application it makes it easy to USE the API but makes it harder to TEST the API.

- [OpenAPI v 3.0 JSON](/api/docs/openapi-3.0.json?download)
- [OpenAPI v 3.1 JSON](/api/docs/openapi-3.1.json?download)
- [OpenAPI v 3.2 JSON](/api/docs/openapi-3.2.json?download)

## About API Challenge's Permissive OpenAPI File

The Permissive OpenAPI File is intended for testing.

It lists all the end points with more Verbs i.e. even verbs that the API defines as not available.

The parameters are also possible to send as empty and type validation is not performed on the parameter values. This makes it possible to use Swagger UI applications to test more extreme situations.

- [OpenAPI v 3.0 JSON](/api/docs/openapi-3.0.json?permissive&download)
- [OpenAPI v 3.1 JSON](/api/docs/openapi-3.1.json?permissive&download)
- [OpenAPI v 3.2 JSON](/api/docs/openapi-3.2.json?permissive&download)
