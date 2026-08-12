---
title: HTTP Methods for REST API Testing
seo_title: HTTP Methods for REST API Testing: GET, POST, PUT, PATCH, DELETE
description: Basic HTTP Verbs and Methods tutorial what they do and how to use them.
lastmod: 2026-08-12
seo_description: Learn HTTP Verbs with practical examples and clear guidance you can apply immediately when creating requests, analyzing responses, and testing APIs.
showads: true
---

# HTTP Methods and Verbs for REST API Testing

HTTP verbs, or methods, tell the server what type of action the client wants to perform against a resource.

The resource is usually represented by the URL path. For example, `/todos` might represent a collection of todos, and `/todos/1` might represent one specific todo item.

The verb and the URL work together:

~~~~~~~~
GET /todos
POST /todos
GET /todos/1
PUT /todos/1
PATCH /todos/1
DELETE /todos/1
~~~~~~~~

Those requests all look similar, but they ask the server to do different things.

We need to use the full scope of verbs when testing APIs because we are checking that the API responds correctly to each supported verb, rejects unsupported verbs, uses suitable status codes, and changes server state only when the verb is supposed to change state.

---

## Common Verb Conventions

The common REST-style convention is to use nouns in the URL and verbs in the HTTP method.

For example, `/customers` is a noun-like collection URL and `/customers/12345` is a noun-like item URL. The HTTP verb tells the server whether we want to retrieve, create, replace, update, or delete.

| **Verb** | **Common Use** | **Collection URL e.g. `/customers`** | **Item URL e.g. `/customers/12345`** |
|----------|----------------|--------------------------------------|--------------------------------------|
| [`GET`](/reference/http-verbs/http-get) | Read | Return a list, often with filtering, sorting, or pagination. | Return a single item, or `404` if it does not exist. |
| [`POST`](/reference/http-verbs/http-post) | Create or process | Create a new item in the collection, often returning `201 Created` and a `Location` header. | Sometimes used for item-specific actions, but not usually for replacing the item. |
| [`PUT`](/reference/http-verbs/http-put) | Create or replace | Rare for a whole collection unless the API explicitly supports replacing the collection. | Replace the item using the supplied representation, or create it if client-chosen ids are supported. |
| [`PATCH`](/reference/http-verbs/http-patch) | Modify | Rare for a whole collection unless the API supports collection-level changes. | Apply selected changes to the item. |
| [`DELETE`](/reference/http-verbs/http-delete) | Delete | Rare and dangerous for a whole collection unless explicitly supported. | Delete the item, often returning `204 No Content`. |
| [`HEAD`](/reference/http-verbs/http-head) | Read metadata | Return headers for the collection without a body. | Return headers for the item without a body. |
| [`OPTIONS`](/reference/http-verbs/http-options) | Discover options | Return allowed methods or communication options. | Return allowed methods or communication options. |
| [`QUERY`](/reference/http-verbs/http-query) | Read with body content | Return matching resources using query criteria in the request body. | Less common for a specific item, but possible if documented. |
| [`TRACE`](/reference/http-verbs/http-trace) | Diagnostic loop-back | Rare for APIs and often disabled. | Rare for APIs and often disabled. |
| [`CONNECT`](/reference/http-verbs/http-connect) | Create a tunnel | Usually proxy or gateway behaviour, not REST resource behaviour. | Usually proxy or gateway behaviour, not REST resource behaviour. |

These are conventions rather than automatic behaviour.

The server application still decides what each route supports. If an API does not support `DELETE /customers/12345` then it should reject the request, even though `DELETE` is a valid HTTP verb.

---

## Safe and Idempotent Verbs

Two useful words appear often when people discuss HTTP verbs: `safe` and `idempotent`.

A `safe` request is intended for reading information. It should not ask the server to change the resource. `GET`, `HEAD`, `OPTIONS`, `TRACE`, and `QUERY` are intended to be safe.

This does not mean the server does absolutely nothing. A server might log the request, update analytics, or refresh a cache. The important point is that the client did not ask to change the resource being addressed. The user shouldn't really expect any side-effects.

An `idempotent` request is one where sending the same request once, twice, or several times has the same intended effect on the resource.

For example, if `PUT /todos/1` replaces todo `1` with the same supplied JSON each time, then the todo ends in the same state each time.

`POST` is usually not idempotent. If you send the same `POST /todos` request twice, the API might create two todo items.

When testing and automating retry behaviour is safer with idempotent methods. Retrying a failed `GET` or `PUT` is usually less risky than retrying a `POST` that might create another item.

| **Verb** | **Safe?** | **Usually Idempotent?** | **Why it Matters** |
|----------|-----------|-------------------------|--------------------|
| [`GET`](/reference/http-verbs/http-get) | Yes | Yes | Repeating a read should not change the resource. |
| [`HEAD`](/reference/http-verbs/http-head) | Yes | Yes | Like `GET`, but without the response body. |
| [`OPTIONS`](/reference/http-verbs/http-options) | Yes | Yes | Used to ask what is allowed. |
| [`TRACE`](/reference/http-verbs/http-trace) | Yes | Yes | Used for diagnostic loop-back checks, but often disabled. |
| [`QUERY`](/reference/http-verbs/http-query) | Yes | Yes | Used to retrieve data with query content. |
| [`POST`](/reference/http-verbs/http-post) | No | No | Repeating it may create or process more than once. |
| [`PUT`](/reference/http-verbs/http-put) | No | Yes | Repeating the same replacement should leave the same final state. |
| [`PATCH`](/reference/http-verbs/http-patch) | No | Sometimes | Depends on the patch format and what the patch does. |
| [`DELETE`](/reference/http-verbs/http-delete) | No | Yes | Repeating it leaves the resource deleted, though later responses may be `404`. |
| [`CONNECT`](/reference/http-verbs/http-connect) | No | No | Establishes a tunnel rather than reading or changing a normal resource representation. |

When the API documentation says a route behaves differently from these conventions, test the documented behaviour. When there is no documentation, these conventions give us a useful starting point for exploration.

---

## HTTP Verb Reference Pages

Use the overview above for the common conventions, then go to a verb page for the examples and status code notes.

<a id="http-get-verb"></a>

- [HTTP GET Verb](/reference/http-verbs/http-get) - retrieve data.

<a id="http-head-verb"></a>

- [HTTP HEAD Verb](/reference/http-verbs/http-head) - retrieve headers without a response body.

<a id="http-options-verb"></a>

- [HTTP OPTIONS Verb](/reference/http-verbs/http-options) - discover supported methods or communication options.

<a id="toc8"></a>
<a id="http-query-verb"></a>
<a id="http-query-structured-json-body"></a>

- [HTTP QUERY Verb](/reference/http-verbs/http-query) - retrieve data using body content, including [JSONPath and Structured JSON request bodies](/reference/http-verbs/http-query#http-query-structured-json-body).

<a id="http-post-verb"></a>

- [HTTP POST Verb](/reference/http-verbs/http-post) - create resources or ask the server to process a request body.

<a id="http-put-verb"></a>

- [HTTP PUT Verb](/reference/http-verbs/http-put) - create or replace from full information.

<a id="toc18"></a>
<a id="http-patch-verb"></a>

- [HTTP PATCH Verb](/reference/http-verbs/http-patch) - apply selected changes to an existing resource.

<a id="http-delete-verb"></a>

- [HTTP DELETE Verb](/reference/http-verbs/http-delete) - remove a resource.

<a id="http-trace-verb"></a>

- [HTTP TRACE Verb](/reference/http-verbs/http-trace) - perform diagnostic loop-back checks.

<a id="http-connect-verb"></a>

- [HTTP CONNECT Verb](/reference/http-verbs/http-connect) - establish a network tunnel, usually through a proxy.

---

## Practise This Concept

Methods become clearer when you send different verbs to similar resources and watch how the API responds. These links help you move from definitions to behaviour:

- [REST API Tutorial](/tutorials/rest-api-tutorial) gives a short live introduction before you go deep on each method.
- [REST API Basics](/reference/rest-api-basics) explains how verbs and resource URLs combine to express intent.
- [How to Test REST APIs](/tutorials/rest-api-testing) shows where method checks fit inside a broader API test flow.
- [Simple API](/practice-modes/simpleapi) lets you try `GET`, `POST`, `PUT`, `PATCH`, and `DELETE` against data you can safely change.
- [API Challenge Solutions](/apichallenges/solutions) include method-specific walkthroughs for GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE, and QUERY.

---

## Summary

HTTP verbs describe the intent of a request. The URL identifies the resource, and the verb tells the server whether the client wants to read, create, replace, update, delete, or inspect what is allowed.

The common REST-style pattern is to use noun-like URLs such as `/todos` and `/todos/1`, then use verbs such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE` to describe the action.

Some verbs are intended to be safe, such as `GET`, `HEAD`, `OPTIONS`, `TRACE`, and `QUERY`, because they are used to retrieve information rather than change resources. Some verbs are intended to be idempotent, such as `PUT` and `DELETE`, because sending the same request again should leave the resource in the same final state.

For API testing, each verb gives us different risks to check. We can test that supported verbs do the right thing, unsupported verbs are rejected, status codes match the behaviour, request bodies are interpreted correctly, and server state only changes when the verb and endpoint say it should.
