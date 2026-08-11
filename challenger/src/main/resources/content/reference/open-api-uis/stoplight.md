---
title: Stoplight Elements OpenAPI UI
seo_title: Stoplight Elements OpenAPI UI for API Documentation
description: An introduction to Stoplight Elements as an OpenAPI and Markdown powered documentation UI.
lastmod: 2026-08-11
seo_description: Learn how Stoplight Elements uses OpenAPI and Markdown components to build interactive API reference documentation and developer portal pages.
showads: true
---

# Stoplight Elements OpenAPI UI

[Stoplight Elements](https://docs.stoplight.io/) is an OpenAPI documentation toolkit from Stoplight.

Elements can be used through React components or web components to build API reference documentation and developer documentation pages powered by OpenAPI and Markdown.

## What Stoplight Elements Shows

Stoplight Elements can render OpenAPI descriptions as interactive API reference documentation.

It is designed for teams that want embeddable documentation components, often as part of a wider developer portal or documentation site.

It adopts the side-bar approach to show all the endpoints on the left with the detailed documentation on the right. There more code generation options present than `cURL` so this makes it very suitable for putting in front of programmers, although this also makes the UI a little harder to understand at first.

The interface to send a request is pretty simple with all configured fields accessible above the "Send API Request" button and the response is shown inline.

The interface doesn't seem as responsive on smaller screens as other embedded clients that we tried.

## When To Consider Stoplight Elements

Scalar is worth evaluating when you want:

- the multi-language/multi-framework support,
- simple sending interface,
- side-bar for navigation

As with any browser-based OpenAPI UI, requests made from the rendered documentation may still be limited by CORS or other browser restrictions. Use a desktop REST client or proxy when you need to verify exactly what traffic was sent.


## Related OpenAPI Pages

Read [OpenAPI for API Testing](/reference/openapi) for the specification background, or compare it with [Swagger UI](/reference/open-api-uis/swagger), [OpenAPI Explorer](/reference/open-api-uis/openapi-explorer), [Scalar](/reference/open-api-uis/scalar), [Zudoku](/reference/open-api-uis/zudoku), and [Redoc](/reference/open-api-uis/redoc).

## Try Stoplight Elements With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Stoplight Elements with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fsim%2Fdocs%2Fopenapi-3.2.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fdocs%2Fopenapi-3.2.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/stoplight?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Buggy API</a>
    </p>
  </div>
</div>
