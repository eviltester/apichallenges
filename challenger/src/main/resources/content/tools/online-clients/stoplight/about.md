---
title: About Stoplight Elements
seo_title: About Stoplight Elements OpenAPI UI for API Documentation
description: A review-style introduction to Stoplight Elements as an OpenAPI and Markdown powered documentation UI.
lastmod: 2026-08-12
seo_description: Learn how Stoplight Elements uses OpenAPI and Markdown components to build interactive API reference documentation and developer portal pages.
showads: true
---

# About Stoplight Elements

[Stoplight Elements](https://docs.stoplight.io/) is an OpenAPI documentation toolkit from Stoplight.

Elements can be used through React components or web components to build API reference documentation and developer documentation pages powered by OpenAPI and Markdown.

Use our [Online Stoplight Elements](/tools/online-clients/stoplight) page when you want to load an OpenAPI file from a URL or from disk and see it rendered on this site.

## What Stoplight Elements Shows

Stoplight Elements can render OpenAPI descriptions as interactive API reference documentation.

It is designed for teams that want embeddable documentation components, often as part of a wider developer portal or documentation site.

It adopts the side-bar approach to show all the endpoints on the left with the detailed documentation on the right. There are more code generation options present than `cURL`, so this makes it very suitable for putting in front of programmers, although this also makes the UI a little harder to understand at first.

The interface to send a request is simple, with configured fields accessible above the "Send API Request" button and the response shown inline.

The interface does not seem as responsive on smaller screens as other embedded clients that we tried.

## When To Consider Stoplight Elements

Stoplight Elements is worth evaluating when you want:

- multi-language and multi-framework support
- a simple sending interface
- side-bar navigation
- embeddable documentation components

As with any browser-based OpenAPI UI, requests made from the rendered documentation may still be limited by CORS or other browser restrictions. Use a desktop REST client or proxy when you need to verify exactly what traffic was sent.

## Related OpenAPI Pages

Read [OpenAPI for API Testing](/reference/openapi) for the specification background, or compare Stoplight Elements with [Swagger UI](/tools/online-clients/swagger/about), [OpenAPI Explorer](/tools/online-clients/openapi-explorer/about), [Scalar](/tools/online-clients/scalar/about), [Zudoku](/tools/online-clients/zudoku/about), and [Redoc](/tools/online-clients/redoc/about).

## Try Stoplight Elements With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Stoplight Elements with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fsim%2Fdocs%2Fopenapi-3.2.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Buggy API</a>
    </p>
  </div>
</div>
