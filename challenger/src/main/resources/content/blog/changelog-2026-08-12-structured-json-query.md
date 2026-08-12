---
date: 2026-08-12T13:00:00Z
lastmod: 2026-08-12
title: Structured JSON QUERY Request Body Support
seo_title: Structured JSON QUERY Request Body Support for API Testing
description: API Challenges now supports Structured JSON request bodies for the HTTP QUERY method.
seo_description: Learn what Structured JSON query bodies are, how they differ from JSONPath, and how to use them with QUERY in API Challenges and Simple API.
categories: Change Log||API Testing||HTTP Methods
tags: Structured JSON||QUERY||Simple API||JSONPath||API Testing
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# Structured JSON QUERY Request Body Support

API Challenges now supports Structured JSON in `QUERY` request bodies.

This means `QUERY` endpoints can advertise and accept:

```text
Content-Type: application/vnd.apichallenges.todo-query+json
```

The existing form-encoded and JSONPath options still work:

```text
Content-Type: application/x-www-form-urlencoded
Content-Type: application/jsonpath
```

With all three formats enabled, an API can use the `Accept-Query` response header to show clients what query body formats are supported:

```text
Accept-Query: application/x-www-form-urlencoded, application/jsonpath, application/vnd.apichallenges.todo-query+json
```

## What is Structured JSON?

Structured JSON is a JSON document designed to describe a query.

It is not the JSON representation of the resource you want to create or update. It is a read-only query document with known members such as `filter`, `sort`, `limit`, and `offset`.

For example, a Simple API item might be represented as:

```json
{
  "type": "book",
  "price": 9.99,
  "numberinstock": 3,
  "isbn13": "123-4-56-789012-3"
}
```

A Structured JSON query for books is shaped differently:

```json
{
  "filter": {
    "type": "book"
  }
}
```

The first JSON document is data. The second JSON document is a query about data.

## Structured JSON and JSONPath are Different

JSONPath is an expression language for selecting values from a JSON document. A JSONPath query usually starts from `$`, the root of a JSON document:

```text
$.items[?(@.type == 'book')]
```

That expression says: start at the root, select the `items` array, and keep the items whose `type` is `book`.

Structured JSON uses a normal JSON object instead:

```json
{
  "filter": {
    "type": "book"
  }
}
```

Both examples can return books, but they express the query differently.

Use JSONPath when you want an expression that selects from a response-shaped JSON document. Use Structured JSON when you want a predictable request shape that names query features directly.

## Why Use Structured JSON in QUERY?

`QUERY` is intended for safe read requests where the query belongs in the request body.

Structured JSON works well with `QUERY` because:

- the HTTP method still communicates that this is a read;
- the URL can stay focused on the collection, such as `/simpleapi/items`;
- each query option has a clear JSON shape;
- generated clients and forms can model `filter`, `sort`, `limit`, and `offset`;
- exact JSON values preserve type, so booleans and numbers do not have to become strings;
- the `Content-Type` header documents the query document format.

## Examples of Structured JSON

Return books:

```json
{
  "filter": {
    "type": "book"
  }
}
```

Return items in stock:

```json
{
  "filter": {
    "numberinstock": {
      "greaterThan": 0
    }
  }
}
```

Return cheaper items:

```json
{
  "filter": {
    "price": {
      "lessThan": 10
    }
  }
}
```

Return matching ISBN text:

```json
{
  "filter": {
    "isbn13": {
      "contains": "123"
    }
  }
}
```

Sort and page a result set:

```json
{
  "sort": [
    {
      "field": "price",
      "direction": "asc"
    }
  ],
  "limit": 5,
  "offset": 0
}
```

## Using Structured JSON With Simple API

The [Simple API](/practice-modes/simpleapi) has an `/simpleapi/items` collection with fields such as `type`, `price`, `numberinstock`, and `isbn13`.

To return books:

```http
QUERY /simpleapi/items HTTP/1.1
Content-Type: application/vnd.apichallenges.todo-query+json
Accept: application/json

{"filter":{"type":"book"}}
```

To return CDs:

```json
{"filter":{"type":"cd"}}
```

To return items with stock:

```json
{"filter":{"numberinstock":{"greaterThan":0}}}
```

To return books sorted by price:

```json
{"filter":{"type":"book"},"sort":[{"field":"price","direction":"asc"}]}
```

For API Challenges todos, the equivalent completed-todo query is:

```http
QUERY /todos HTTP/1.1
Content-Type: application/vnd.apichallenges.todo-query+json
Accept: application/json

{"filter":{"doneStatus":true}}
```

## Try the New Challenge

There is now an API Challenge for Structured JSON query bodies:

- [QUERY /todos Structured JSON solution](/apichallenges/solutions/query/query-todos-200-structured-json)

And the reference material has been expanded:

- [HTTP Methods and Verbs: QUERY Structured JSON](/reference/http-verbs/http-query#http-query-structured-json-body)

Try the Simple API examples first, then use the new challenge to prove that your request body is doing exactly what you think it is doing.
