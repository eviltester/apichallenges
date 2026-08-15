---
title: Online Stoplight Elements
seo_title: Online Stoplight Elements UI for OpenAPI Documentation
description: A browser based Stoplight Elements page for opening OpenAPI files from a URL or local disk.
lastmod: 2026-08-12
layout: wide-tool
seo_description: Use Stoplight Elements online to load OpenAPI JSON or YAML from a URL or local file, inspect schemas, code samples, and API docs.
schema_type: WebPage
og_type: website
showads: true
---

# Online Stoplight Elements

Open an OpenAPI JSON or YAML file from a URL or from disk, then view it in Stoplight Elements.

<section class="online-openapi-ui-client" data-online-openapi-ui-client data-openapi-ui="stoplight" data-default-openapi-url="/api/docs/openapi.json">
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
  <p class="online-client-status" data-openapi-status role="status">Loading Stoplight Elements...</p>
  <div class="online-openapi-ui-wide-embed">
    <div id="online-stoplight-ui" class="online-openapi-ui-render" data-openapi-render-target></div>
  </div>
</section>

## Stoplight Elements In This Page

[Stoplight Elements](/tools/online-clients/stoplight/about) is an OpenAPI documentation toolkit built around embeddable components.

This page uses the Elements web component to render an OpenAPI file as interactive API reference documentation with navigation, schemas, request and response details, code samples, and a browser API console.

Because Stoplight Elements runs in the browser, URL loading and try-it requests are affected by CORS. If a URL is blocked, download the OpenAPI file and open it locally.

Compare this with the [Online Swagger UI](/tools/online-clients/swagger), [OpenAPI Explorer](/tools/online-clients/openapi-explorer), [Scalar](/tools/online-clients/scalar), [Zudoku](/tools/online-clients/zudoku), and [Redoc](/tools/online-clients/redoc) pages.

## Try Stoplight Elements With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Stoplight Elements with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fsimpleapi%2Fdocs%2Fopenapi.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fsim%2Fdocs%2Fopenapi.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fapi%2Fdocs%2Fopenapi.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fshop%2Fdocs%2Fopenapi.json">Buggy API</a>
    </p>
  </div>
</div>

<link rel="stylesheet" href="https://unpkg.com/@stoplight/elements@9.0.24/styles.min.css">
<link rel="stylesheet" href="/css/online-swagger-theme.css">
<script src="https://unpkg.com/@stoplight/elements@9.0.24/web-components.min.js"></script>
<script src="/js/vendor/js-yaml.min.js" defer></script>
<script src="/js/openapi-text-loader.js" defer></script>
<script src="/js/openapi-tool-controls.js" defer></script>
<script src="/js/online-openapi-ui-client.js" defer></script>
