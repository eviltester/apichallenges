---
title: Scalar OpenAPI UI
seo_title: Scalar OpenAPI UI for API Reference and API Client Docs
description: An introduction to Scalar as an OpenAPI UI, API reference renderer, and API client.
lastmod: 2026-08-11
seo_description: Learn how Scalar supports OpenAPI and Swagger through API reference pages, a REST API client, hosted documentation, and related API tooling.
showads: true
---

# Scalar OpenAPI UI

[Scalar](https://github.com/scalar/scalar) is an open source API platform with OpenAPI support.

The project consists of the embedded API client and documentation viewer and a standalone API client, hosted documentation options, and other tools around OpenAPI documents.

We are demonstrating the Scalar 'Api Reference' which is the embedded client and documentation viewer.

## What Scalar Shows

Scalar can render OpenAPI descriptions as API reference documentation.

It is useful when you want a modern API reference experience and may also want related tooling such as an API client, hosted docs, SDK tooling, an editor, or an OpenAPI parser.

It adopts a side-bar endpoint list for easy navigation and then the main request specification on the right.

There is a `ctrl+k` shortcut key which shows a popup list of all endpoints, this might be useful on smaller screens when the side-bar disappears - this turns into an 'introduction' hamburger menu on smaller screens to make eaiser navigation on phones and tablets.

The Send Request feature uses a popop client that allows you amend headers and add additional query parameters that are not part of the OpenAPI specification file. Making the Scalar UI more suited for complex evaluation or to support more adhoc testing of the API.


## When To Consider Scalar

Scalar is worth evaluating when you want:

- the multi-language/multi-framework support,
- more fully featured send request editing,
- responsive support for smaller screens
- side-bar for navigation

If you use the interactive API console from a browser page, remember that browser security rules can affect whether requests are sent successfully. Use a REST client or proxy when you need more control over request construction and traffic evidence.

## Related OpenAPI Pages

Read [OpenAPI for API Testing](/reference/openapi) for the specification background, or compare it with [Swagger UI](/reference/open-api-uis/swagger), [OpenAPI Explorer](/reference/open-api-uis/openapi-explorer), [Stoplight Elements](/reference/open-api-uis/stoplight), [Zudoku](/reference/open-api-uis/zudoku), and [Redoc](/reference/open-api-uis/redoc).

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
