---
title: About Swagger UI
seo_title: About Swagger UI for API Testing: OpenAPI Docs and Requests
description: A review-style introduction to Swagger tooling, Swagger UI, and Swagger Editor for working with OpenAPI files.
lastmod: 2026-08-12
seo_description: Learn how Swagger UI and Swagger Editor use OpenAPI files to render browser-based API documentation and request forms.
showads: true
---

# About Swagger UI

Swagger is a family of tools that work with OpenAPI files.

OpenAPI is the standard specification. Swagger is tooling that can read OpenAPI and make it easier for people to view, edit, and use an API.

The most common Swagger tools are:

- Swagger UI, a browser interface for exploring and trying an API
- Swagger Editor, an editor for viewing and changing an OpenAPI file

Use our [Online Swagger UI](/tools/online-clients/swagger) page when you want to load an OpenAPI file from a URL or from disk and try it directly on this site.

## Swagger UI

Swagger UI renders an OpenAPI file as interactive API documentation.

It can show endpoints, supported HTTP methods, request bodies, response formats, status codes, and authentication details. It can also create request forms so you can send API calls from the browser.

Examples on this site:

- [Simple API Swagger UI](/simpleapi/docs/swagger-ui)
- [API Challenges Swagger UI](/api/docs/swagger-ui)
- [API Simulator Swagger UI](/sim/docs/swagger-ui)

On our Test Pages site we also have:

- [Shopping Cart Swagger UI](https://testpages.eviltester.com/apps/basiccart/swagger/)
- [Simple Calculator Swagger UI](https://testpages.eviltester.com/apps/calculator-api/swagger/)

## Swagger Editor

Swagger Editor can load an OpenAPI file and show the editable specification on one side with a rendered interface on the other.

- [swagger.io/tools/swagger-editor](https://swagger.io/tools/swagger-editor/)
- [editor.swagger.io](https://editor.swagger.io/)

You can import an OpenAPI file from a URL or from a downloaded JSON or YAML file.

Try these URLs in the Swagger Editor:

- `https://apichallenges.eviltester.com/simpleapi/docs/openapi.json`
- `https://apichallenges.eviltester.com/api/docs/openapi.json`
- `https://api.practicesoftwaretesting.com/docs/api-docs.json`

## Running Swagger Editor Locally

I prefer to run the Swagger Editor locally and do so using Docker.

```shell
docker run -p 1235:8080 --rm swaggerapi/swagger-editor
```

The above command runs the Swagger Editor UI on port `1235` and deletes the container when I exit the command line.

I can then access the editor by visiting `http://localhost:1235`.

Exercise:

- run the editor locally using Docker
- load `https://apichallenges.eviltester.com/simpleapi/docs/openapi.json`
- try some requests
- edit some of the OpenAPI content and see what changes in the rendered Swagger UI

## CORS And Browser Security

Swagger UI and Swagger Editor make requests from browser JavaScript.

Browsers use Cross-Origin Resource Sharing, known as CORS, to control whether JavaScript from one origin can send requests to another origin.

- [MDN CORS documentation](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)

This means a Swagger UI hosted on one site might not be able to call an API hosted on another site unless the API sends suitable CORS headers.

The API Challenges APIs use relaxed CORS headers to make browser-based API tooling easier to use.

Exercise:

- open [petstore.swagger.io](https://petstore.swagger.io/)
- enter `https://apichallenges.eviltester.com/simpleapi/docs/openapi.json`
- click Explore
- issue a request
- inspect the response headers to see the CORS headers

## Designed For Usage More Than Testing

Swagger UI is very convenient, but it is usually designed to help people use an API successfully.

That means it may guide you toward valid requests and hide invalid combinations. For example, if an endpoint does not document `DELETE`, then Swagger UI will not show a `DELETE` operation for that endpoint.

When testing, you often want to try unsupported methods, invalid payloads, missing fields, long values, or malformed input. A less-validating OpenAPI file can make Swagger UI more useful for that style of testing, but a dedicated REST client may still be more flexible.

Exercise:

- compare the [standard Simple API OpenAPI file](/simpleapi/docs/openapi.json)
- with the [less-validating Simple API OpenAPI file](/simpleapi/docs/openapi.json?permissive)
- notice how the more permissive file makes it easier to send exploratory or negative test requests

## Related OpenAPI Pages

Read [OpenAPI as a specification](/reference/openapi) when you want to understand the standard file format that Swagger tools consume.

Compare Swagger UI with [OpenAPI Explorer](/tools/online-clients/openapi-explorer/about), [Scalar](/tools/online-clients/scalar/about), [Stoplight Elements](/tools/online-clients/stoplight/about), [Zudoku](/tools/online-clients/zudoku/about), and [Redoc](/tools/online-clients/redoc/about).

## Try Swagger With Our APIs

<div class="openapi-ui-launch-panel">
  <div class="openapi-ui-launch-group">
    <p>Try Swagger with our APIs:</p>
    <p class="openapi-ui-launch-links">
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fsimpleapi%2Fdocs%2Fopenapi.json">Simple API</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fsim%2Fdocs%2Fopenapi.json">API Simulator</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fapi%2Fdocs%2Fopenapi.json">API Challenges</a>
      <a class="openapi-ui-launch-link" href="/tools/online-clients/swagger?url=%2Fshop%2Fdocs%2Fopenapi.json">Buggy API</a>
    </p>
  </div>
</div>
