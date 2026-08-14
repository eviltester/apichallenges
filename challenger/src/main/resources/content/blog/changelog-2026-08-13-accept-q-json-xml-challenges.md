---
date: 2026-08-13T10:00:00Z
lastmod: 2026-08-13
title: Accept q Values, JSON, XML, And New API Challenges
seo_title: Accept q Values, JSON, XML, And New API Challenges
description: API Challenges now includes richer Accept header negotiation for q values, JSON, XML, and structured media type variations.
seo_description: Learn how Accept q values affect JSON and XML response negotiation, and practise the behaviour with new API Challenges.
categories: Change Log||API Testing||HTTP Headers
tags: Accept Header||Content Negotiation||JSON||XML||API Challenges
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# Accept q Values, JSON, XML, And New API Challenges

We have expanded the Accept content negotiation handling used by API Challenges and the practice APIs.

This means you can now test more realistic `Accept` header behaviour for:

- quality values, usually written as `q`;
- JSON response negotiation;
- XML response negotiation;
- structured media type suffixes such as `+json` and `+xml`;
- unsupported media types returning `406 Not Acceptable`.

There are also new API Challenges so you can practise these concepts against the `/todos` API rather than only reading about them.

## Why Accept q Values Matter

Many clients can handle more than one response format.

A client might prefer XML, but still accept JSON:

```http
Accept: application/xml;q=1, application/json;q=0.5
```

Or it might prefer JSON, but accept XML as a fallback:

```http
Accept: application/xml;q=0.5, application/json;q=1
```

The `Accept` header is not just a list of formats. It can describe preference, fallback, and exclusion. If an API ignores those details then clients may receive a response they did not ask for, or a less useful error when a fallback response was possible.

## What Accept q Means

The `q` value is a quality value. It gives each media type in the `Accept` header a preference weight.

The main rules are:

- a missing `q` value means `q=1.0`;
- higher values are preferred over lower values;
- `q=0` means "do not send me this media type";
- if the most preferred media type is not supported, a lower-priority supported media type can still be used.

For example:

```http
Accept: application/problem+json;q=1, application/json;q=0.5
```

If `application/problem+json` is not supported as a normal representation of the requested resource, the API can still respond with `application/json` because the client listed it as an acceptable fallback.

But this request rejects both JSON and XML:

```http
Accept: application/json;q=0, application/xml;q=0
```

When the API has no acceptable representation left to send, a `406 Not Acceptable` response is the expected observable outcome.

## JSON Accept Variations

The normal JSON representation for a resource is:

```http
Accept: application/json
```

Structured JSON-looking media types are different media types. For example:

```http
Accept: application/problem+json
Accept: application/*+json
```

Those do not automatically mean `application/json`.

`application/problem+json` is normally used for problem details, not as the ordinary representation of a todo item or todo collection. `application/*+json` asks for a supported structured `+json` media type. It does not automatically match plain `application/json`.

If your client can process the normal JSON representation, include it explicitly as a fallback:

```http
Accept: application/problem+json;q=1, application/json;q=0.5
```

This gives the API a clear way to return normal JSON when the more specific structured JSON media type is not available.

## XML Accept Variations

The XML side now has more useful variation to explore.

You can request the ordinary XML representation:

```http
Accept: application/xml
```

You can also try `text/xml`:

```http
Accept: text/xml
```

And the API Challenges todo endpoints now support explicit and wildcard structured XML negotiation:

```http
Accept: application/vnd.apichallenges.todo+xml
Accept: application/*+xml
```

This is useful because `+xml` media types often describe a specific XML representation, not just "any XML". Testing the explicit vendor media type and the structured XML wildcard helps you check whether the API chooses a representation consistently.

## New API Challenges

The new challenges cover the content negotiation edge cases that are easy to miss in everyday API testing.

The advanced `Accept` challenges include:

- preferring XML over JSON with `q` values;
- preferring JSON over XML with `q` values;
- rejecting all available representations with `q=0`;
- requesting unsupported structured JSON;
- requesting `text/xml`;
- requesting vendor XML with `application/vnd.apichallenges.todo+xml`;
- requesting structured XML with `application/*+xml`.

There is also a new `Content-Type` challenge for posting a todo using:

```http
Content-Type: application/vnd.apichallenges.todo+xml
```

Together, these challenges make it easier to practise content negotiation as observable API behaviour: send a request, inspect the status code, inspect the `Content-Type`, and check whether the body format matches the negotiated representation.

## Where To Try It

Read the expanded [HTTP Basics Accept header reference](/reference/http-basics#accept-header) for the rules and examples.

Then open the [API Challenges progress page](/gui/challenges) and work through the Accept and Advanced Accept challenge groups.

If you want a lower-pressure area for broader experimentation, the [Simple API experiments page](/practice-modes/simpleapi/experiments) includes content negotiation exercises and inline clients for trying different headers against `/simpleapi/items`.
