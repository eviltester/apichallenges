---
title: Redoc OpenAPI UI
seo_title: Redoc OpenAPI UI for API Reference Documentation
description: An introduction to Redoc as an open source OpenAPI reference documentation viewer.
lastmod: 2026-08-11
seo_description: Learn how open source Redoc renders OpenAPI and Swagger descriptions as API reference documentation, and why it is a viewer rather than a client.
showads: true
---

# Redoc OpenAPI UI

[Redoc](https://redocly.com/redoc) is an open source tool for rendering API reference documentation from OpenAPI and Swagger descriptions.

The open source Redoc experience is primarily a documentation viewer. It helps people read and navigate an API description, but it is not a request-sending API client in the same way as Swagger UI or a REST client.

Redoc does support sending requests but only in the paid plan. The Open Source version is view only.

- [Redoc Github Repo](https://github.com/Redocly/redoc)
- [Redoc Official Demo](https://redocly.github.io/redoc/) 

## What Redoc Shows

Redoc renders OpenAPI documentation with navigation, detailed reference content, schemas, and request and response examples.

The side-bar makes it easy to navigate through all the requests and see full scope of endpoints and operations available.

It is often used when a team wants a polished API reference page that can be published as documentation and browsed by API consumers.

Like all tools offered by commercial vendors, Redoc has a non-configurable link back to the vendors site.

Redoc is supported natively in frameworks such as Docusaurus.

## When To Consider Redoc

Redoc is a strong fit when your main goal is readable API reference documentation.

For exploratory requests and testing, Redoc can still be useful in combination with REST API tools because it makes the documented API structure easier to inspect.

## Related OpenAPI Pages

Read [OpenAPI for API Testing](/reference/openapi) for the specification background, or compare it with [Swagger UI](/reference/open-api-uis/swagger), [OpenAPI Explorer](/reference/open-api-uis/openapi-explorer), [Scalar](/reference/open-api-uis/scalar), [Stoplight Elements](/reference/open-api-uis/stoplight), and [Zudoku](/reference/open-api-uis/zudoku).

## Try Redoc With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Redoc with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fsim%2Fdocs%2Fopenapi-3.2.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fdocs%2Fopenapi-3.2.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/redoc?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Buggy API</a>
    </p>
  </div>
</div>
