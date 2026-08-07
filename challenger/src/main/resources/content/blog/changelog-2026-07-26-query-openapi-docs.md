---
date: 2026-07-26T09:00:00Z
lastmod: 2026-08-07
title: QUERY Method Support and Versioned OpenAPI Docs
seo_title: QUERY Method Support and Versioned OpenAPI Docs
description: A practical explanation of QUERY method support, versioned OpenAPI files, permissive specs, and downloadable API documentation.
seo_description: Understand the API Challenges QUERY method update, versioned OpenAPI 3.0, 3.1, and 3.2 docs, permissive specs, downloads, and testing value.
categories: Change Log||OpenAPI||HTTP Methods
tags: QUERY||OpenAPI||API Documentation||API Testing
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# QUERY Method Support and Versioned OpenAPI Docs

The 2026-07-26 update added `QUERY` support across API Challenges, Simple API, Simulation, and Mirror practice modes. It also expanded OpenAPI support with versioned documents and download options.

This update was useful because it combined a modern HTTP method with better documentation tooling. It gives learners a way to explore a less common method and gives testers more ways to compare OpenAPI versions, downloads, and permissive testing specifications.

## Why QUERY Is Interesting for Testers

`QUERY` is an HTTP method intended for safe, idempotent requests that can include query criteria in the request body. This is useful when query parameters become too limited or awkward for complex filtering.

For API testing, `QUERY` is valuable because it raises questions about tooling and standards support. Some clients make custom methods easy. Others hide them behind advanced settings or do not support them at all.

That makes `QUERY` a good tool-evaluation exercise. If a REST client cannot send a `QUERY` request, or if it treats `QUERY` like an unknown error, then the tester has learned something important about the tool. If automation code can send the method but the API documentation cannot describe it well, that is another useful discovery.

Adding `QUERY` to API Challenges, Simple API, Simulation, and Mirror means learners can compare the same method across different practice contexts:

- API Challenges for goal-driven challenge completion.
- Simple API for stateful practice.
- Simulation mode for controlled method behaviour.
- Mirror mode for observing what was actually sent.

## Versioned OpenAPI Documentation

The update added OpenAPI 3.0, 3.1, and 3.2 JSON endpoints, with `openapi.json` defaulting to OpenAPI 3.1. That lets learners and testers compare how tooling handles different OpenAPI versions.

The `?permissive` option helps create tester-friendly OpenAPI files. The `?download` option returns attachment headers so files can be saved directly and imported into desktop REST clients.

Versioned documentation is useful because OpenAPI tooling does not always support every version equally. A file that works in one client might fail in another. A newer OpenAPI version might document something more accurately, while an older version might import into more tools.

The download option helps with practical workflow. Testers often need to save an OpenAPI file, import it into a client, commit it for review, or compare it with a generated version. Returning attachment headers makes that flow more natural.

## Why Permissive Specs Help Testing

Strict OpenAPI files are useful for documentation and consumer guidance, but they can sometimes block exploratory testing. A tester may want to send an unsupported method, omit a required field, use a different content type, or deliberately test invalid data.

The `?permissive` option supports that style of work by producing a more tester-friendly view of the API. It does not mean the API accepts every request. It means the tooling is less likely to stop the tester before the request reaches the server.

## What This Update Adds for Learners

Start with [OpenAPI for API Testing](/reference/openapi) if you want to understand how an API description can support exploration, then try the [OpenAPI Converter](/tools/online-clients/openapi-converter) to see how a spec can be adjusted for testing.

Then try a `QUERY` request in more than one client. If one tool succeeds and another fails, investigate why. The learning is not only in the API response; it is also in understanding how your tools handle newer or less common HTTP behaviour.
