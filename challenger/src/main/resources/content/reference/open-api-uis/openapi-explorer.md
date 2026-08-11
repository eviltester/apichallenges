---
title: OpenAPI Explorer UI
seo_title: OpenAPI Explorer UI for OpenAPI Documentation
description: An introduction to OpenAPI Explorer as an OpenAPI documentation web component.
lastmod: 2026-08-11
seo_description: Learn how OpenAPI Explorer uses a web component to render OpenAPI documentation, show resources and models, and support browser API calls.
showads: true
---

# OpenAPI Explorer UI

[OpenAPI Explorer](https://github.com/Authress-Engineering/openapi-explorer) is an open source web component for viewing OpenAPI specifications in a browser.

It is designed to be embedded in a site or application as a custom element. The project also describes automatic integration options for React and Vue.

## What OpenAPI Explorer Shows

OpenAPI Explorer can render an OpenAPI file as an API explorer and console.

It can help people:

- view resources and models from the OpenAPI document
- make API calls from the rendered interface
- see generated client or SDK code samples
- style the component to fit the host site

## When To Consider OpenAPI Explorer

OpenAPI Explorer is a good fit when you want an embeddable component instead of a hosted documentation platform.

Because it is a browser UI, API calls made from the page are still affected by browser security rules such as CORS. For exploratory API testing, compare the requests it sends with a REST client or an HTTP proxy when you need stronger evidence.

## Related OpenAPI Pages

Read [OpenAPI for API Testing](/reference/openapi) for the specification background, or compare it with [Swagger UI](/reference/open-api-uis/swagger), [Scalar](/reference/open-api-uis/scalar), [Stoplight Elements](/reference/open-api-uis/stoplight), [Zudoku](/reference/open-api-uis/zudoku), and [Redoc](/reference/open-api-uis/redoc).

## Try OpenAPI Explorer UI With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try OpenAPI Explorer UI with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fsim%2Fdocs%2Fopenapi-3.2.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fdocs%2Fopenapi-3.2.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/openapi-explorer?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Buggy API</a>
    </p>
  </div>
</div>
