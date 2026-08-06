---
title: REST API Tutorial: Learn REST by Using a LIVE API
seo_title: REST API Tutorial: Learn REST by Using a LIVE API
description: Learn REST API basics by sending requests to the live Simple API.
lastmod: 2026-08-06
seo_description: Learn REST by using a live API with interactive examples for resources, URLs, HTTP methods, status codes, JSON, XML, headers, auth, OpenAPI, CRUD, and common REST mistakes.
showads: true
---

# REST API Tutorial: Learn REST by Using a LIVE API

This REST API tutorial introduces the main REST ideas by using the live [Simple API](/practice-modes/simpleapi). We provide a quick practical model of REST, and have embedded HTTP clients to let you send real requests from the page. When you want to learn a topic in more detail we have links to expanded reference material.

By the end you should understand the basics of:

- what REST means in everyday API work
- how resources and URLs fit together
- why HTTP methods matter
- how status codes, headers, JSON, and XML affect API behaviour
- how CRUD maps onto common REST-style APIs
- where OpenAPI documentation helps
- common mistakes to avoid

Useful reference pages:

- [REST API Basics](/tutorials/rest-api-basics)
- [HTTP Basics](/tutorials/http-basics)
- [HTTP Methods and Verbs](/tutorials/http-verbs)
- [OpenAPI](/tutorials/openapi)
- [Swagger](/tutorials/swagger)
- [API Testing Concepts and Coverage](/tutorials/testing-apis)

---

## What REST Is

REST stands for Representational State Transfer. In practical API work, people usually use "REST API" to mean an HTTP API that is organised around resources and uses standard HTTP methods such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.

REST is an architectural style, not just a URL naming convention. For learning and day to day use, the most useful starting point is:

- a URL identifies a resource
- an HTTP method describes what you want to do
- headers describe message details
- a body contains a representation such as JSON or XML
- a status code reports what happened

For more depth, read [REST API Basics](/tutorials/rest-api-basics).

---

## Resources

A resource is something the API exposes. In the Simple API, an inventory item is a resource.

There is a collection resource:

~~~~~~~~
/simpleapi/items
~~~~~~~~

And there are individual item resources:

~~~~~~~~
/simpleapi/items/1
/simpleapi/items/2
~~~~~~~~

Try listing the item collection:

{{<api-live-request method="GET" path="/simpleapi/items" expected-status="200" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /simpleapi/items to list item resources">}}

When you execute the request, look for the collection in the response body. Each item has fields such as `id`, `type`, `isbn13`, `price`, and `numberinstock`.

Learn more about resources in [REST API Basics](/tutorials/rest-api-basics).

---

## URLs

In a REST-style API, URLs are usually noun-like. They identify the thing you want to work with.

For example:

| URL | Meaning |
|-----|---------|
| `/simpleapi/items` | the collection of items |
| `/simpleapi/items/1` | item `1` |
| `/simpleapi/docs/openapi.json` | the OpenAPI description for the Simple API |

The URL does not usually contain the action. Instead of `/deleteItem/1`, a REST-style API would usually use:

~~~~~~~~
DELETE /simpleapi/items/1
~~~~~~~~

For more about HTTP request structure, read [HTTP Basics](/tutorials/http-basics).

---

## HTTP Methods

The HTTP method tells the server what kind of operation the client wants.

Common REST-style method usage:

| Method | Common Meaning |
|--------|----------------|
| `GET` | read a resource |
| `POST` | create a resource or submit data |
| `PUT` | replace a resource |
| `PATCH` | partially update a resource |
| `DELETE` | remove a resource |

These are conventions, not magic. The server decides what each route supports, and the documentation should explain it.

Learn more in [HTTP Methods and Verbs](/tutorials/http-verbs).

---

## Status Codes

The status code gives a quick summary of what happened.

Common examples:

| Status | Meaning |
|--------|---------|
| `200 OK` | the request succeeded |
| `201 Created` | a new resource was created |
| `204 No Content` | the request succeeded and there is no response body |
| `400 Bad Request` | the request could not be understood |
| `401 Unauthorized` | authentication is missing or failed |
| `403 Forbidden` | the caller is known but not allowed |
| `404 Not Found` | the resource was not found |
| `422 Unprocessable Content` | the body was understood but failed validation |
| `500 Internal Server Error` | the server failed unexpectedly |

When you execute the first `GET /simpleapi/items` request on this page, check that the response status is `200`.

Learn more about status codes in [HTTP Basics](/tutorials/http-basics).

---

## JSON and XML

REST APIs usually send a representation of a resource in the response body.

JSON is common:

~~~~~~~~
Accept: application/json
~~~~~~~~

XML is also used by some APIs:

~~~~~~~~
Accept: application/xml
~~~~~~~~

The Simple API supports both JSON and XML for some requests. Try asking for XML:

{{<api-live-request method="GET" path="/simpleapi/items" expected-status="200" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Accept: application/xml" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /simpleapi/items as XML">}}

The resource is the same, but the representation format changes.

Learn more about request and response bodies in [HTTP Basics](/tutorials/http-basics).

---

## Headers

Headers are metadata for the HTTP message.

Important API headers include:

- `Accept`: the response format the client prefers
- `Content-Type`: the format of the request body
- `Authorization`: credentials or tokens used for authentication
- `Location`: often returned after creating a resource

### Exercise: Change the `Accept` Header

Use the editable exercise below. Keep the URL as `/simpleapi/items`, but change the `Accept` header between:

~~~~~~~~
Accept: application/json
Accept: application/xml
~~~~~~~~

Then compare the response bodies.

{{<api-live-request method="GET" path="/simpleapi/items" expected-status="200" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Accept: application/json" editable="true" edit-mode="fixed" query-editable="false" body-editable="false" details="true" summary="Exercise: change the Accept header for /simpleapi/items">}}

Learn more about headers in [HTTP Basics](/tutorials/http-basics).

---

## Auth

Authentication answers "who is making the request?" Authorisation answers "what is that caller allowed to do?"

APIs often use headers for auth, for example:

~~~~~~~~
Authorization: Bearer token-value
Authorization: Basic encoded-credentials
~~~~~~~~

The Simple API does not require auth, so the examples on this page can focus on REST concepts. For auth details and risks, read:

- [REST API Basics: Authentication](/tutorials/rest-api-basics#toc11)
- [HTTP Basics: Basic Authentication](/tutorials/http-basics#toc29)
- [API Testing Concepts and Coverage](/tutorials/testing-apis)

---

## OpenAPI

OpenAPI is a structured way to describe an HTTP API.

An OpenAPI file can describe:

- URLs and methods
- request bodies
- response bodies
- status codes
- headers
- authentication rules

View the Simple API OpenAPI file:

{{<api-live-request method="GET" path="/simpleapi/docs/openapi.json" expected-status="200" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /simpleapi/docs/openapi.json to view the OpenAPI description">}}

OpenAPI is useful, but it is still documentation. The running API is the source of behaviour you experience when you send requests.

Learn more in [OpenAPI](/tutorials/openapi) and [Swagger](/tutorials/swagger).

---

## CRUD

CRUD means:

- Create
- Read
- Update
- Delete

A common REST-style CRUD flow looks like this:

| CRUD Action | HTTP Request |
|-------------|--------------|
| Create | `POST /simpleapi/items` |
| Read | `GET /simpleapi/items/{id}` |
| Update | `PATCH /simpleapi/items/{id}` or `PUT /simpleapi/items/{id}` |
| Delete | `DELETE /simpleapi/items/{id}` |

Try the sequence below. Run the `POST` first. The built-in client remembers the created item id for the later requests.

{{<api-live-request method="POST" path="/simpleapi/items" expected-status="201" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Content-Type: application/json||Accept: application/json" editable="false" query-editable="false" body='{"type":"book","isbn13":"{{randomSimpleApiIsbn}}","price":2.00,"numberinstock":3}' details="true" summary="POST /simpleapi/items to create an item">}}

{{<api-live-request method="GET" path="/simpleapi/items/{{lastCreatedSimpleApiItemId}}" expected-status="200" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /simpleapi/items/{id} to read the created item">}}

{{<api-live-request method="PATCH" path="/simpleapi/items/{{lastCreatedSimpleApiItemId}}" expected-status="200" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Content-Type: application/json||Accept: application/json" editable="false" query-editable="false" body='{"price":9.99}' details="true" summary="PATCH /simpleapi/items/{id} to update the price">}}

{{<api-live-request method="DELETE" path="/simpleapi/items/{{lastCreatedSimpleApiItemId}}" expected-status="204" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="DELETE /simpleapi/items/{id} to delete the item">}}

Learn more about CRUD and REST in [REST API Basics](/tutorials/rest-api-basics) and [HTTP Methods and Verbs](/tutorials/http-verbs).

---

## Common Mistakes

REST API mistakes often come from mixing up the parts of the HTTP message.

Common examples:

- using action words in URLs instead of resource names
- using `GET` for operations that change data
- returning `200` for every outcome, including errors
- ignoring `Content-Type` and `Accept`
- accepting malformed JSON or wrong field types
- documenting one behaviour in OpenAPI while the live API behaves differently
- forgetting auth checks on update or delete operations
- treating `PUT` and `PATCH` as if they always mean the same thing

Try sending a body with the wrong data type. `numberinstock` should be a number, not a string:

{{<api-live-request method="POST" path="/simpleapi/items" expected-status="422" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Content-Type: application/json||Accept: application/json" editable="false" query-editable="false" body='{"type":"book","isbn13":"{{randomSimpleApiIsbn}}","price":2.00,"numberinstock":"3"}' details="true" summary="POST /simpleapi/items with an invalid field type">}}

The expected result is a client error because the server understood the request body but rejected the value.

For deeper coverage ideas, read [API Testing Concepts and Coverage](/tutorials/testing-apis).

---

## Next Steps

You now have a practical map of REST:

- resources are the things exposed by the API
- URLs identify resources
- HTTP methods describe the operation
- headers and bodies shape the message
- status codes describe the outcome
- OpenAPI documents expected behaviour

To continue, read:

- [How to Test REST APIs](/tutorials/rest-api-testing)
- [REST API Basics](/tutorials/rest-api-basics)
- [HTTP Basics](/tutorials/http-basics)
- [HTTP Methods and Verbs](/tutorials/http-verbs)
- [OpenAPI](/tutorials/openapi)
