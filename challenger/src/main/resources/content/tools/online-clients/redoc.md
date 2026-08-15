---
title: Online Redoc OpenAPI Viewer
seo_title: Online Redoc OpenAPI Viewer for API Reference Docs
description: A browser based Redoc page for opening OpenAPI files from a URL or local disk.
lastmod: 2026-08-12
layout: wide-tool
seo_description: Use Redoc online to load OpenAPI JSON or YAML from a URL or local file and inspect readable API reference documentation in the browser.
schema_type: WebPage
og_type: website
showads: true
---

# Online Redoc OpenAPI Viewer

Open an OpenAPI JSON or YAML file from a URL or from disk, then view it in Redoc.

<section class="online-openapi-ui-client" data-online-openapi-ui-client data-openapi-ui="redoc" data-default-openapi-url="/api/docs/openapi.json">
  <form class="online-swagger-controls" data-openapi-url-form>
    <label>
      OpenAPI or Swagger URL
      <input data-openapi-url type="text" value="/api/docs/openapi.json" placeholder="https://example.com/openapi.json">
    </label>
    <button type="submit">Open URL</button>
  </form>
  <div class="online-swagger-file-row">
    <label>
      Open local JSON or YAML file
      <input data-openapi-file type="file" accept=".json,.yaml,.yml,application/json,text/yaml,application/yaml">
    </label>
  </div>
  <p class="online-client-status" data-openapi-status role="status">Loading Redoc...</p>
  <div class="online-openapi-ui-wide-embed">
    <div id="online-redoc-ui" class="online-openapi-ui-render" data-openapi-render-target></div>
  </div>
</section>

## Redoc In This Page

[Redoc](/tools/online-clients/redoc/about) is an open source tool for rendering API reference documentation from OpenAPI and Swagger descriptions.

The open source Redoc experience is primarily a documentation viewer. It is good for reading, navigating, and reviewing an API description, but it is not a request-sending API client in the same way as Swagger UI or a REST client.

Use this page when you want to inspect the documented structure, schemas, examples, and response details. Use the [Basic Client](/tools/online-clients/basic-client), [Online Swagger UI](/tools/online-clients/swagger), or a desktop REST client when you need to send exploratory requests.

Compare this with the [OpenAPI Explorer](/tools/online-clients/openapi-explorer), [Scalar](/tools/online-clients/scalar), [Stoplight Elements](/tools/online-clients/stoplight), and [Zudoku](/tools/online-clients/zudoku) pages.

## Try Redoc With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Redoc with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fsim%2Fdocs%2Fopenapi-3.2.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Buggy API</a>
    </p>
  </div>
</div>

<link rel="stylesheet" href="/css/online-swagger-theme.css">
<script src="https://cdn.jsdelivr.net/npm/redoc@2.5.3/bundles/redoc.standalone.js"></script>
<script src="/js/vendor/js-yaml.min.js" defer></script>
<script src="/js/openapi-text-loader.js" defer></script>
<script src="/js/openapi-tool-controls.js" defer></script>
<script src="/js/online-openapi-ui-client.js" defer></script>
