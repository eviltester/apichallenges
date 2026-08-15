---
title: Online Scalar OpenAPI UI
seo_title: Online Scalar OpenAPI UI for API Reference Testing
description: A browser based Scalar API reference page for opening OpenAPI files from a URL or local disk.
lastmod: 2026-08-12
layout: wide-tool
seo_description: Use Scalar online to load OpenAPI JSON or YAML from a URL or local file, inspect API reference docs, and explore request examples.
schema_type: WebPage
og_type: website
showads: true
---

# Online Scalar OpenAPI UI

Open an OpenAPI JSON or YAML file from a URL or from disk, then view it in Scalar.

<section class="online-openapi-ui-client" data-online-openapi-ui-client data-openapi-ui="scalar" data-default-openapi-url="/api/docs/openapi.json">
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
  <p class="online-client-status" data-openapi-status role="status">Loading Scalar...</p>
  <div class="online-openapi-ui-wide-embed">
    <div id="online-scalar-ui" class="online-openapi-ui-render" data-openapi-render-target></div>
  </div>
</section>

## Scalar In This Page

[Scalar](/tools/online-clients/scalar/about) is an open source API platform with OpenAPI and Swagger support.

This page uses Scalar as an API reference renderer for OpenAPI files. It is useful when you want a modern reference layout with request examples, navigation, schemas, and API client style workflows.

The Scalar viewer runs in your browser. Loading external OpenAPI URLs and making browser requests can be limited by CORS, so open a downloaded file from disk if a public URL is blocked.

Compare this with the [Online Swagger UI](/tools/online-clients/swagger), [OpenAPI Explorer](/tools/online-clients/openapi-explorer), [Stoplight Elements](/tools/online-clients/stoplight), [Zudoku](/tools/online-clients/zudoku), and [Redoc](/tools/online-clients/redoc) pages.

## Try Scalar With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Scalar with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fsim%2Fdocs%2Fopenapi-3.2.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Buggy API</a>
    </p>
  </div>
</div>

<link rel="stylesheet" href="/css/online-swagger-theme.css">
<script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference@1.64.1"></script>
<script src="/js/vendor/js-yaml.min.js" defer></script>
<script src="/js/openapi-text-loader.js" defer></script>
<script src="/js/openapi-tool-controls.js" defer></script>
<script src="/js/online-openapi-ui-client.js" defer></script>
