---
title: About Scalar
seo_title: About Scalar OpenAPI UI for API Reference and API Client Docs
description: A review-style introduction to Scalar as an OpenAPI UI, API reference renderer, and API client.
lastmod: 2026-08-12
seo_description: Learn how Scalar supports OpenAPI and Swagger through API reference pages, a REST API client, hosted documentation, and related API tooling.
showads: true
---

# About Scalar

[Scalar](https://github.com/scalar/scalar) is an open source API platform with OpenAPI support.

The project consists of the embedded API client and documentation viewer, a standalone API client, hosted documentation options, and other tools around OpenAPI documents.

We are demonstrating the Scalar API Reference, which is the embedded client and documentation viewer.

Use our [Online Scalar OpenAPI UI](/tools/online-clients/scalar) page when you want to load an OpenAPI file from a URL or from disk and see it rendered on this site.

## What Scalar Shows

Scalar can render OpenAPI descriptions as API reference documentation.

It is useful when you want a modern API reference experience and may also want related tooling such as an API client, hosted docs, SDK tooling, an editor, or an OpenAPI parser.

It adopts a side-bar endpoint list for easy navigation and then the main request specification on the right.

There is a `ctrl+k` shortcut key which shows a popup list of all endpoints. This can be useful on smaller screens when the side-bar disappears; it turns into an introduction menu on smaller screens to make navigation easier on phones and tablets.

The Send Request feature uses a popup client that lets you amend headers and add additional query parameters that are not part of the OpenAPI specification file. This can make the Scalar UI more suited for complex evaluation or more ad hoc testing of the API.

## When To Consider Scalar

Scalar is worth evaluating when you want:

- multi-language and multi-framework support
- more fully featured send request editing
- responsive support for smaller screens
- side-bar navigation

If you use the interactive API console from a browser page, remember that browser security rules can affect whether requests are sent successfully. Use a REST client or proxy when you need more control over request construction and traffic evidence.

## Related OpenAPI Pages

Read [OpenAPI for API Testing](/reference/openapi) for the specification background, or compare Scalar with [Swagger UI](/tools/online-clients/swagger/about), [OpenAPI Explorer](/tools/online-clients/openapi-explorer/about), [Stoplight Elements](/tools/online-clients/stoplight/about), [Zudoku](/tools/online-clients/zudoku/about), and [Redoc](/tools/online-clients/redoc/about).

## Try Scalar With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Scalar with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fsimpleapi%2Fdocs%2Fopenapi-3.2.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fsim%2Fdocs%2Fopenapi-3.2.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fdocs%2Fopenapi-3.2.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/scalar?url=%2Fshop%2Fdocs%2Fopenapi-3.2.json">Buggy API</a>
    </p>
  </div>
</div>
