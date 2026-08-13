---
title: HTTP QUERY Verb
seo_title: HTTP QUERY Method for REST API Testing
description: Learn how the HTTP QUERY method uses request body query content, JSONPath, and Structured JSON.
lastmod: 2026-08-12
seo_description: Learn how the HTTP QUERY method uses request body query content, JSONPath, and Structured JSON.
showads: true
---

<a id="toc8"></a>
<a id="http-query-verb"></a>
# HTTP QUERY Verb

- [QUERY](https://www.rfc-editor.org/rfc/rfc10008.html) - safely retrieve data using query content in the request body
- QUERY is intended for safe, read-only requests where the query content is too large, complex, or structured for a URI query string
- A server can advertise supported QUERY request content types with `Accept-Query`
- API Challenges supports `QUERY /todos` with `Content-Type: application/x-www-form-urlencoded`, `Content-Type: application/jsonpath`, and `Content-Type: application/vnd.apichallenges.todo-query+json`

`QUERY` is newer and less widely supported than the traditional verbs.

It exists for the situation where we want the read-only behaviour of a `GET`, but the query is too large or too structured to fit comfortably in a URL query string.

For example, a simple filter might fit neatly into a URL:

~~~~~~~~
GET /todos?doneStatus=true
~~~~~~~~

But a more complex query might be easier to send in a request body. That is the type of use case `QUERY` is designed for.

Because `QUERY` is not as widely supported as `GET` and `POST`, always check whether your client, proxy, gateway, and server framework support it before relying on it in a real API.

---

## HTTP QUERY Form Body Example

~~~~~~~~
curl -X QUERY {{<ORIGIN_URL>}}/todos ^
-H "Content-Type: application/x-www-form-urlencoded" ^
-H "Accept: application/json" ^
-d "doneStatus=true"
~~~~~~~~

~~~~~~~~
QUERY {{<ORIGIN_URL>}}/todos HTTP/1.1
User-Agent: curl/8.0.0
Host: localhost:4567
Content-Type: application/x-www-form-urlencoded
Accept: application/json
Content-Length: 15

doneStatus=true
~~~~~~~~

---

## HTTP QUERY JSONPath Body

`QUERY` can also use a structured query language in the request body. API Challenges supports JSONPath query bodies using:

~~~~~~~~
Content-Type: application/jsonpath
~~~~~~~~

JSONPath is defined by [RFC 9535](https://datatracker.ietf.org/doc/html/rfc9535). The standard describes JSONPath as a string syntax for selecting and extracting values from a JSON value. In practice, it gives us a compact way to describe which parts of a JSON document we want to select.

JSONPath expressions usually start at the root with `$`. For API Challenges, `QUERY /todos` evaluates the expression against a JSON document shaped like the normal collection response:

```json
{
  "todos": [
    {
      "id": 1,
      "title": "learn QUERY",
      "doneStatus": true,
      "description": "try JSONPath"
    }
  ]
}
```

To return only completed todos, send:

~~~~~~~~
$.todos[?(@.doneStatus == true)]
~~~~~~~~

That expression means:

- `$` starts at the root of the JSON document.
- `.todos` selects the `todos` array.
- `[?(...)]` filters the array.
- `@` refers to each todo being tested by the filter.
- `@.doneStatus == true` keeps only todos whose `doneStatus` value is `true`.

JSONPath `QUERY` requests should select complete resource objects from the collection. For example, `$.todos[?(@.doneStatus == true)]` is valid because it selects todo objects. A projection such as `$.todos[*].title` selects only field values, not complete todos, so API Challenges rejects it as unprocessable.

---

## HTTP QUERY JSONPath Example

~~~~~~~~
curl -X QUERY {{<ORIGIN_URL>}}/todos ^
-H "Content-Type: application/jsonpath" ^
-H "Accept: application/json" ^
-d "$.todos[?(@.doneStatus == true)]"
~~~~~~~~

~~~~~~~~
QUERY {{<ORIGIN_URL>}}/todos HTTP/1.1
User-Agent: curl/8.0.0
Host: localhost:4567
Content-Type: application/jsonpath
Accept: application/json
Content-Length: 31

$.todos[?(@.doneStatus == true)]
~~~~~~~~

Useful JSONPath expressions to experiment with:

| **Goal** | **JSONPath Body** |
|----------|-------------------|
| Return every todo | `$.todos` |
| Return done todos | `$.todos[?(@.doneStatus == true)]` |
| Return not-done todos | `$.todos[?(@.doneStatus == false)]` |
| Return todos with a specific title | `$.todos[?(@.title == 'learn QUERY')]` |
| Return todos with non-empty descriptions | `$.todos[?(@.description != '')]` |

JSONPath is useful with `QUERY` because the URL can stay focused on the resource collection, while the request body carries a more expressive read-only selection.

---

<a id="http-query-structured-json-body"></a>

## HTTP QUERY Structured JSON Body

`QUERY` can also send a JSON object that describes the query criteria. API Challenges calls this a Structured JSON query body and supports it using:

~~~~~~~~
Content-Type: application/vnd.apichallenges.todo-query+json
~~~~~~~~

Structured JSON means that the request body is JSON, but it is not the normal resource representation. It is a query document with known top-level members such as `filter`, `sort`, `limit`, and `offset`.

Structured JSON is a documented API Challenges query format, not a general web standard. JSON itself is standardized: [RFC 8259](https://datatracker.ietf.org/doc/html/rfc8259). JSONPath is also standardized: [RFC 9535](https://datatracker.ietf.org/doc/html/rfc9535). The media type reinforces that it is vendor/API-specific:

```text
application/vnd.apichallenges.todo-query+json
```

The `vnd.` part is the vendor tree for media types, described by [RFC 6838](https://datatracker.ietf.org/doc/html/rfc6838). The `+json` suffix says the payload syntax is JSON, but the semantics are defined by the API, not by a general web standard.

For example, a todo representation might look like this:

```json
{
  "title": "learn QUERY",
  "doneStatus": true,
  "description": "try Structured JSON"
}
```

A Structured JSON query body instead describes how to search the collection:

```json
{
  "filter": {
    "doneStatus": true
  }
}
```

The body is JSON, but it doesn't represent the `todo` it represents a structured set of read-only query instructions.

Different APIs will support the representation in different ways, so check your API documentation to find the correct representation, assuming that your API supports structured JSON queries.

API Challenges Structured JSON supports these members:

| **Member** | **Purpose** | **Example** |
|------------|-------------|-------------|
| `filter` | Match fields by exact values or supported operators. | `{"filter":{"doneStatus":true}}` |
| `sort` | Sort the returned collection by one or more fields. | `{"sort":[{"field":"id","direction":"desc"}]}` |
| `limit` | Limit the number of returned items. | `{"limit":5}` |
| `offset` | Skip items before returning a page. | `{"offset":5}` |

Inside `filter`, exact values match field values:

```json
{
  "filter": {
    "doneStatus": false
  }
}
```

Operator objects can express richer criteria:

```json
{
  "filter": {
    "id": {
      "greaterThan": 1,
      "lessThan": 5
    }
  }
}
```

```json
{
  "filter": {
    "title": {
      "contains": "query"
    }
  }
}
```

Sorting and paging can be added alongside filtering:

```json
{
  "filter": {
    "doneStatus": false
  },
  "sort": [
    {
      "field": "title",
      "direction": "asc"
    }
  ],
  "limit": 5,
  "offset": 0
}
```

`Structured JSON` differs from `JSONPath`:

- `JSONPath` is an expression language for selecting values from a JSON document, e.g. `$.todos[?(@.doneStatus == true)]`.
- Structured JSON is a query criteria object that the API understands, e.g. `{"filter":{"doneStatus":true}}`.
- JSONPath starts from a response-shaped document, while Structured JSON uses named query members such as `filter`, `sort`, `limit`, and `offset`.
- Structured JSON can be easier for generated clients and forms because each query option has a predictable JSON shape.

Use the Structured JSON media type when you want body-based query criteria without introducing a separate expression language.

---

## HTTP QUERY Structured JSON Example

~~~~~~~~
curl -X QUERY {{<ORIGIN_URL>}}/todos ^
-H "Content-Type: application/vnd.apichallenges.todo-query+json" ^
-H "Accept: application/json" ^
-d "{\"filter\":{\"doneStatus\":true}}"
~~~~~~~~

~~~~~~~~
QUERY {{<ORIGIN_URL>}}/todos HTTP/1.1
User-Agent: curl/8.0.0
Host: localhost:4567
Content-Type: application/vnd.apichallenges.todo-query+json
Accept: application/json
Content-Length: 30

{"filter":{"doneStatus":true}}
~~~~~~~~

Useful Structured JSON bodies to experiment with:

| **Goal** | **Structured JSON Body** |
|----------|--------------------------|
| Return done todos | `{"filter":{"doneStatus":true}}` |
| Return not-done todos | `{"filter":{"doneStatus":false}}` |
| Return ids greater than 1 and less than 5 | `{"filter":{"id":{"greaterThan":1,"lessThan":5}}}` |
| Return titles containing text | `{"filter":{"title":{"contains":"query"}}}` |
| Return sorted and paged results | `{"sort":[{"field":"id","direction":"desc"}],"limit":5,"offset":0}` |

---

## Common HTTP Status codes in response to a QUERY

- **200** - OK, returned the matching representation
- **400** - invalid query content
- **405** - method not allowed for this endpoint
- **415** - unsupported query content type
- **422** - query content was valid syntax but the fields, operators, or selected data could not be processed


---

