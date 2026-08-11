---
title: Zudoku OpenAPI UI
seo_title: Zudoku OpenAPI UI for Interactive API Documentation
description: An introduction to Zudoku as an OpenAPI powered API documentation framework.
lastmod: 2026-08-11
seo_description: Learn how Zudoku uses OpenAPI documents to build customizable developer documentation, API references, and self-hosted developer portals.
showads: true
---

# Zudoku OpenAPI UI

[Zudoku](https://github.com/zuplo/zudoku) is an open source framework for building API documentation around OpenAPI documents.

It is created by Zuplo and is designed for customizable developer experiences, API references, and developer portals.

## What Zudoku Shows

Zudoku can build OpenAPI powered API references and broader documentation sites.

It supports use cases such as standalone documentation websites, interactive API references, internal documentation, and developer portals with authentication-aware documentation flows.

Zudoku uses a side-bar to show the endpoints, with the main request documented on the right. The Send Request mechanism is a popup client which allows header and parameter amendment to go beyond the confines of the OpenAPI specification supplied by the API.

Zudoku supports multiple programming language examples but the framework for each language is not configurable so not as extensive language support ass [Scalar](/reference/open-api-uis/scalar) or [Stoplight](/reference/open-api-uis/stoplight)

Zudoku uses a slide in side-bar for endpoints on smaller screens which works, but is surprising at first sight.

Zudoku is normally designed to be used as its own docs site or app, often with a project/build setup. In that mode, it owns the route structure, theme, navigation, and React app directly. On this site we are embedding it in a non-React environment so we are having to use an iframe.

## When To Consider Zudoku

Zudoku is worth evaluating when you want a flexible embedded client but don't want the user to be overloaded with frameworks and just wants to see examples in their familiar coding language.

- the multi-language (non-configurable framework) support,
- flexible and advanced sending interface,
- responsive client,
- side-bar for navigation,
- you want Zudoku to 'own' the docs - because it is a framework more than an embedded client widget

For API testing, treat any browser request console as a convenient starter tool and still verify important traffic with a REST client or proxy.

## Related OpenAPI Pages

Read [OpenAPI for API Testing](/reference/openapi) for the specification background, or compare it with [Swagger UI](/reference/open-api-uis/swagger), [OpenAPI Explorer](/reference/open-api-uis/openapi-explorer), [Scalar](/reference/open-api-uis/scalar), [Stoplight Elements](/reference/open-api-uis/stoplight), and [Redoc](/reference/open-api-uis/redoc).

## Try Zudoku With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Zudoku with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fsim%2Fdocs%2Fopenapi-3.2.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fdocs%2Fopenapi-3.2.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/zudoku?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Buggy API</a>
    </p>
  </div>
</div>
