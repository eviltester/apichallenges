---
title: About Zudoku
seo_title: About Zudoku OpenAPI UI for Interactive API Documentation
description: A review-style introduction to Zudoku as an OpenAPI powered API documentation framework.
lastmod: 2026-08-12
seo_description: Learn how Zudoku uses OpenAPI documents to build customizable developer documentation, API references, and self-hosted developer portals.
showads: true
---

# About Zudoku

[Zudoku](https://github.com/zuplo/zudoku) is an open source framework for building API documentation around OpenAPI documents.

It is created by Zuplo and is designed for customizable developer experiences, API references, and developer portals.

Use our [Online Zudoku OpenAPI UI](/tools/online-clients/zudoku) page when you want to load an OpenAPI file from a URL or from disk and preview it on this site.

## What Zudoku Shows

Zudoku can build OpenAPI powered API references and broader documentation sites.

It supports use cases such as standalone documentation websites, interactive API references, internal documentation, and developer portals with authentication-aware documentation flows.

Zudoku uses a side-bar to show the endpoints, with the main request documented on the right. The Send Request mechanism is a popup client which allows header and parameter amendment to go beyond the confines of the OpenAPI specification supplied by the API.

Zudoku supports multiple programming language examples, but the framework for each language is not configurable, so the language support is not as extensive as [Scalar](/tools/online-clients/scalar/about) or [Stoplight Elements](/tools/online-clients/stoplight/about).

Zudoku uses a slide-in side-bar for endpoints on smaller screens, which works, but can be surprising at first sight.

Zudoku is normally designed to be used as its own docs site or app, often with a project and build setup. In that mode, it owns the route structure, theme, navigation, and React app directly. On this site we are embedding it in a non-React environment, so the showcase page uses an iframe.

## When To Consider Zudoku

Zudoku is worth evaluating when you want a flexible embedded client, but do not want the user overloaded with frameworks and mainly want to show examples in their familiar coding language.

- multi-language examples with a fixed framework choice
- a flexible and advanced sending interface
- a responsive client
- side-bar navigation
- a documentation framework that can own the docs site rather than a small embedded widget

For API testing, treat any browser request console as a convenient starter tool and still verify important traffic with a REST client or proxy.

## Related OpenAPI Pages

Read [OpenAPI for API Testing](/reference/openapi) for the specification background, or compare Zudoku with [Swagger UI](/tools/online-clients/swagger/about), [OpenAPI Explorer](/tools/online-clients/openapi-explorer/about), [Scalar](/tools/online-clients/scalar/about), [Stoplight Elements](/tools/online-clients/stoplight/about), and [Redoc](/tools/online-clients/redoc/about).

## Try Zudoku With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Zudoku with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fsim%2Fdocs%2Fopenapi-3.2.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fapi%2Fdocs%2Fopenapi-3.2.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Buggy API</a>
    </p>
  </div>
</div>
