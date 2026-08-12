---
date: 2026-08-11T16:30:00Z
lastmod: 2026-08-11
title: JSONPath QUERY Request Body Support
seo_title: JSONPath QUERY Request Body Support for API Testing
description: API Challenges now supports JSONPath request bodies for the HTTP QUERY method.
seo_description: Learn what JSONPath is, why it helps API testing, and how to use JSONPath request bodies with the HTTP QUERY method in API Challenges.
categories: Change Log||API Testing||HTTP Methods
tags: JSONPath||QUERY||Simple API||API Testing
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# JSONPath QUERY Request Body Support

API Challenges now supports JSONPath expressions in `QUERY` request bodies.

This means endpoints powered by Thingifier can advertise and accept:

```text
Content-Type: application/jsonpath
```

The original `QUERY` support used form-encoded content, for example:

```text
type=book
```

That still works. JSONPath adds another way to describe read-only selection criteria in the body of a `QUERY` request.

## What is JSONPath?

JSONPath is a query syntax for JSON documents. It lets us select values from a JSON structure in a similar spirit to how XPath selects values from XML.

The standard is [RFC 9535](https://datatracker.ietf.org/doc/html/rfc9535), published in February 2024. The RFC defines JSONPath as a syntax for selecting and extracting JSON values from a JSON value.

A JSONPath expression usually starts with `$`, which means the root of the JSON document.

For a response like this:

```json
{
  "items": [
    {
      "id": 1,
      "type": "book",
      "price": 9.99,
      "numberinstock": 3,
      "isbn13": "123-4-56-789012-3"
    }
  ]
}
```

The expression:

```text
$.items[?(@.type == 'book')]
```

selects every item where the `type` field is `book`.

## Why Use JSONPath?

JSONPath is useful when the query is more structured than a simple field/value filter.

A URL query string works well for simple examples:

```text
/simpleapi/items?type=book
```

But a request body can be easier to read when the query has multiple conditions or when you want a syntax that already describes JSON data.

For example:

```text
$.items[?(@.type == 'book' && @.numberinstock > 0)]
```

That expression reads as "from the items collection, return books where the number in stock is greater than zero."

## Examples Using Simple API

The [Simple API](/practice-modes/simpleapi) has an `/simpleapi/items` collection with fields such as `type`, `price`, `numberinstock`, and `isbn13`.

To return only books:

```http
QUERY /simpleapi/items HTTP/1.1
Content-Type: application/jsonpath
Accept: application/json

$.items[?(@.type == 'book')]
```

To return CDs:

```text
$.items[?(@.type == 'cd')]
```

To return out-of-stock items:

```text
$.items[?(@.numberinstock == 0)]
```

To return books that have stock:

```text
$.items[?(@.type == 'book' && @.numberinstock > 0)]
```

To return all items:

```text
$.items
```

For API Challenges todos, the equivalent completed-todo query is:

```http
QUERY /todos HTTP/1.1
Content-Type: application/jsonpath
Accept: application/json

$.todos[?(@.doneStatus == true)]
```

## Why Use JSONPath With QUERY?

`QUERY` is intended for safe read requests where the query content belongs in the request body rather than the URL.

JSONPath pairs well with `QUERY` because:

- the HTTP method still communicates "this is a read";
- the URL can stay focused on the collection resource;
- the request body can contain a structured query expression;
- the `Content-Type` header documents the query language;
- clients can discover support through the `Accept-Query` response header.

This gives testers another useful behavior to explore. Does your REST client support custom methods? Does it allow a request body with `QUERY`? Does it preserve `Content-Type: application/jsonpath`? Does generated OpenAPI documentation show the content type clearly?

## Try the New Challenge

There is now an API Challenge for JSONPath query bodies:

- [QUERY /todos JSONPath solution](/apichallenges/solutions/query/query-todos-200-jsonpath)

And the reference material has been expanded:

- [HTTP Methods and Verbs: QUERY](/reference/http-verbs#http-query-verb)

Try the Simple API examples first, then use the challenge to prove that your request is doing what you think it is doing.
