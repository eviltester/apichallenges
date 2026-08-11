---
title: Buggy API OpenAPI JSON Files
seo_title: Buggy API OpenAPI JSON Downloads | API Challenges
description: Download OpenAPI JSON files for the Buggy API.
lastmod: 2026-08-10
seo_description: Download Buggy API OpenAPI JSON files for REST clients, Swagger UI imports, schema exploration, and deliberately buggy checkout practice.
showads: true
---

# Buggy API OpenAPI Files

Download an OpenAPI JSON file to use in your REST Client.

The default `openapi.json` endpoint currently returns OpenAPI v 3.1.

- OpenAPI v 3.0 JSON [standard validation](/shop/docs/openapi-3.0.json) [download](/shop/docs/openapi-3.0.json?download) - [less validation](/shop/docs/openapi-3.0.json?permissive) [download](/shop/docs/openapi-3.0.json?permissive&download)
- OpenAPI v 3.1 JSON [standard validation](/shop/docs/openapi-3.1.json) [download](/shop/docs/openapi-3.1.json?download) - [less validation](/shop/docs/openapi-3.1.json?permissive) [download](/shop/docs/openapi-3.1.json?permissive&download)
- OpenAPI v 3.2 JSON [standard validation](/shop/docs/openapi-3.2.json) [download](/shop/docs/openapi-3.2.json?download) - [less validation](/shop/docs/openapi-3.2.json?permissive) [download](/shop/docs/openapi-3.2.json?permissive&download)

## Open OpenAPI 3.2 In Online UIs

Tool support for OpenAPI 3.2 is still emerging, so these links help compare how each UI handles the same file.

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <h3>Standard validation</h3>
    <p>Open the user-facing Buggy API OpenAPI 3.2 file.</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Open in Swagger</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Open in OpenAPI Explorer</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Open in Scalar</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Open in Stoplight Elements</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Open in Zudoku</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Open in Redoc</a>
    </p>
  </div>
  <div class="openapi-ui-launch-group">
    <h3>Less validation</h3>
    <p>Open the more permissive Buggy API OpenAPI 3.2 file for testing edge cases.</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Swagger</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in OpenAPI Explorer</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Scalar</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Stoplight Elements</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Zudoku</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json%3Fpermissive">Open in Redoc</a>
    </p>
  </div>
</div>

The API is intentionally buggy by default. Start the app with `-shopbugs=none` when you want the clean business rules for comparison.
