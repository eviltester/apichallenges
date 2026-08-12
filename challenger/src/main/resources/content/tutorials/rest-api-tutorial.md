---
title: REST API Tutorial: Learn REST by Using a Live API
seo_title: REST API Tutorial: Learn REST by Using a Live API
description: Learn REST API basics by sending real GET, HEAD, POST, PATCH, and DELETE requests to live APIs.
lastmod: 2026-08-09
seo_description: Learn REST API basics with live HTTP requests. Try resources, URLs, methods, status codes, JSON, XML, headers, auth, OpenAPI, CRUD, and common REST mistakes.
showads: true
---

# REST API Tutorial: Learn REST by Using a Live API

This REST API tutorial teaches the main REST concepts by sending real HTTP requests to live APIs. You will try `GET`, `HEAD`, `POST`, `PATCH`, and `DELETE`, inspect status codes and headers, switch between JSON and XML, use auth tokens, and run a small CRUD flow from the page.

The read-only examples use the [API Challenges API](/apichallenges) `/todos` endpoint, and the CRUD examples use the [Simple API](/practice-modes/simpleapi) when we need to create, update, or delete data. When you want to learn a topic in more detail, follow the links to the expanded reference material.

By the end of this tutorial you should understand the basics of:

- what REST means in everyday API work
- how resources and URLs fit together
- why HTTP methods matter
- how status codes, headers, JSON, and XML affect API behaviour
- how CRUD maps onto common REST-style APIs
- where OpenAPI documentation helps
- common mistakes to avoid

Useful REST API reference pages:

- [REST API Basics](/reference/rest-api-basics)
- [HTTP Basics](/reference/http-basics)
- [HTTP Methods and Verbs](/reference/http-verbs)
- [OpenAPI](/reference/openapi)
- [Swagger UI](/tools/online-clients/swagger/about)
- [API Testing Concepts and Coverage](/reference/testing-apis)

---

## What Is a REST API?

REST stands for Representational State Transfer. In practical API work, people usually use "REST API" to mean an HTTP API that is organised around resources and uses standard HTTP methods such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.

REST is an architectural style and set of guidelines.

REST was introduced by Roy Fielding in his thesis, [Architectural Styles and the Design of Network-based Software Architectures](https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm), which describes REST as an architectural style for networked applications.

A high level starting point is:

- a URL identifies a resource
  - Example: `/todos` identifies the collection of todo resources.
  - Example: `/todos/1` identifies a single todo resource, the one with `id=1`.
- an HTTP method describes what you want to do
  - Example: `GET /todos` asks to read the todo collection and retrieve todo details.
- headers describe message details
  - Example: `Accept: application/json` asks for a JSON response.
- a body contains a representation such as JSON or XML
  - Example: `{"title":"learn REST","doneStatus":false}` is a JSON representation sent in a request.
- a status code reports what happened
  - Example: `200 OK` means the API returned the requested resource.

For a deeper explanation of REST concepts, read [REST API Basics](/reference/rest-api-basics).

---

## REST API Resources

A resource is something the API exposes. In the API Challenges API, a todo item is a resource.

There is a collection resource:

~~~~~~~~
/todos
~~~~~~~~

And there are individual item resources:

~~~~~~~~
/todos/1
/todos/2
~~~~~~~~

Try listing the todo collection:

{{<api-live-request method="GET" path="/todos" expected-status="200" use-challenger="false" allowed-path-prefixes="/todos" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /todos to list todo resources">}}

When you execute the request, look for the collection in the response body. Each todo has fields such as `id`, `title`, `doneStatus`, and `description`.

Learn more about REST resources in [REST API Basics](/reference/rest-api-basics).

---

## REST API URLs and Endpoints

In a REST-style API, URLs are usually noun-like. They identify the thing you want to work with.

For example:

| URL | Meaning |
|-----|---------|
| `/todos` | the collection of todos |
| `/todos/1` | todo `1` |
| `/docs/openapi.json` | the OpenAPI description for API Challenges |

Try reading a single todo resource:

{{<api-live-request method="GET" path="/todos/1" expected-status="200" use-challenger="false" allowed-path-prefixes="/todos" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /todos/1 to read one todo resource">}}

The URL does not usually contain the action. Instead of `/deleteItem/1`, a REST-style API would usually combine an HTTP method (e.g. `DELETE`) with a resource URL (e.g. `/items/1`). In the CRUD section below, the Simple API uses this request format:

~~~~~~~~
DELETE /simpleapi/items/1
~~~~~~~~

For more about HTTP request structure, read [HTTP Basics](/reference/http-basics).

---

## HTTP Methods in REST APIs

The HTTP method (also known as HTTP verb) tells the server what kind of operation the client wants.

Common REST-style method usage:

| Method | Common Meaning |
|--------|----------------|
| `GET` | read a resource |
| `HEAD` | read response status and headers without the response body |
| `POST` | create a resource or submit data |
| `PUT` | replace a resource |
| `PATCH` | partially update a resource |
| `DELETE` | remove a resource |

`HEAD` is similar to `GET`, but the server should only return the response status and headers. This is useful when a client wants to check whether a resource exists, or inspect metadata, without downloading the response body.

{{<api-live-request method="HEAD" path="/todos/1" expected-status="200" use-challenger="false" allowed-path-prefixes="/todos" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="HEAD /todos/1 to get Headers Only">}}

> **Exercise: Compare `HEAD` and `GET`**
>
> The earlier `GET /todos/1` request returned a response body.
> After sending the `HEAD /todos/1` request, compare the status and headers with the `GET` response. The `HEAD` response should not contain a body, but the headers should be mostly the same.



These are conventions, not enforced mandatory rules that each API must follow. Individual teams decide what the API will implement and what methods each route supports, and the documentation should explain what to use.

For example, many APIs do not implement `PATCH` and rely on `POST` for partial updates. Some APIs do not use `PUT` and again rely on `POST` to do the work.

There are standards available for the body format of `PATCH` requests, but again, not all teams use these.

When testing, we work with the API we've got, not the API we think the standards and guidelines describe.

Learn more about REST API methods in [HTTP Methods and Verbs](/reference/http-verbs).

---

## REST API Status Codes

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

When you execute the first `GET /todos` request on this page, check that the response status is `200`.

You can also ask the API for something the route does not support. Execute this `DELETE /heartbeat` request and check that the response status is `405`.

{{<api-live-request method="DELETE" path="/heartbeat" expected-status="405" use-challenger="false" allowed-path-prefixes="/heartbeat" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="DELETE /heartbeat to see a 405 status code">}}

If you open the `Raw` response tab, the first line in the response shows the HTTP status returned by the server.

Learn more about HTTP status codes in [HTTP Basics](/reference/http-basics).

---

## JSON and XML in REST APIs

REST APIs usually send a representation of a resource in the response body.

JSON is common:

~~~~~~~~
Accept: application/json
~~~~~~~~

XML is also used by some APIs:

~~~~~~~~
Accept: application/xml
~~~~~~~~

The API Challenges `/todos` API supports both JSON and XML for most requests. Try asking for XML:

{{<api-live-request method="GET" path="/todos" expected-status="200" use-challenger="false" allowed-path-prefixes="/todos" headers="Accept: application/xml" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /todos as XML">}}

The resource is the same, but the representation format changes.

Learn more about HTTP request and response bodies in [HTTP Basics](/reference/http-basics).

---

## REST API Headers

Headers are metadata for the HTTP message.

Important API headers include:

- `Accept`: the response format the client prefers
- `Content-Type`: the format of the request body
- `Authorization`: credentials or tokens used for authentication
- `Location`: often returned after creating a resource

Request headers describe the content, for example `Content-Type`, and can also set preferences for how the client wants the request to be processed, for example `Accept`.

Response headers describe the returned content and provide follow-on information for the client, for example `Content-Type` and `Location`.

Issue the request below and look at the headers in both the response and the request to see the difference.

{{<api-live-request method="GET" path="/todos" expected-status="200" use-challenger="false" allowed-path-prefixes="/todos" headers="Accept: application/json" editable="true" edit-mode="fixed" query-editable="false" body-editable="false" details="true" summary="GET /todos and inspect the headers in the response">}}

To see the actual headers sent in the request, open the browser Dev Tools and look in the Network tab. This shows the full request and response details sent to the server. The browser may add headers that are not listed in the embedded HTTP client, and those extra headers can sometimes affect API testing.

> **Exercise: Change the `Accept` Header**
>
> In our default request we asked for JSON.
> Use the editable request above. Keep the URL as `/todos`, but change the `Accept` header between:
>
> `Accept: application/json`
> 
> and
> 
> `Accept: application/xml`
>
> Then compare the response bodies.



Learn more about REST API headers in [HTTP Basics](/reference/http-basics).

---

## REST API Authentication and Authorization

Authentication answers "who is making the request?"

Authorization answers "what is that caller allowed to do?"

So you might be authenticated and able to log in to the system, but not authorized to access every resource.

APIs often use headers for auth, for example:

~~~~~~~~
Authorization: Bearer token-value
Authorization: Basic encoded-credentials
~~~~~~~~

### Authentication

Basic Auth is a simple HTTP authentication scheme. The client sends an `Authorization` header that starts with `Basic`, followed by the username and password joined with a colon and Base64 encoded. For example, `admin:password` becomes `YWRtaW46cGFzc3dvcmQ=`, so the request sends:

~~~~~~~~
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
~~~~~~~~

Base64 is only an encoding, not encryption. Anyone who can see the header can decode it, so Basic Auth should be sent over HTTPS in real systems.

Authentication is often the first step. The API asks "can you prove who you are?" The `GET /secret/token` endpoint uses Basic Auth to prove who you are, then returns an auth token to allow access to private data.

Run this request first to get the auth token:

{{<api-live-request method="GET" path="/secret/token" expected-status="200" use-challenger="false" allowed-path-prefixes="/secret" headers="Authorization: Basic YWRtaW46cGFzc3dvcmQ=||Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /secret/token with Basic Auth to receive a read-only token">}}

The auth token is shown in the response body and in the `X-AUTH-TOKEN` response header.

> **Exercise - Amend Authorization Header**
>
> Amend the Authorization header so that the encoded credentials are incorrect and send the request.
> Delete a few characters from the credentials to make them invalid.
> You should see a `401` response, meaning that authentication failed.

### Authorization

Authorization is the next step. The API asks "is this caller allowed to access this resource?" Use the token from the previous response as a Bearer token in the standard `Authorization` request header to read the protected secret note:

{{<api-live-request method="GET" path="/secret/note" expected-status="200" use-challenger="false" allowed-path-prefixes="/secret" headers="Authorization: Bearer {{authToken}}||Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /secret/note with Bearer Token to read the secret note">}}

> **Exercise - Amend Authorization Header**
>
> Amend the `Authorization` header so that the Bearer token value is incorrect and send the request.
> Delete a few characters from the value to make it invalid.
> You should see a `403` response, meaning that the request is forbidden because the token is not authorized.

### Custom Headers

Custom HTTP request and response headers are sometimes prefixed by `X-`. Some APIs use custom headers to pass API keys or auth tokens.

The API Challenges API also accepts the token in the custom `X-AUTH-TOKEN` request header, so the same protected note can be read this way:

{{<api-live-request method="GET" path="/secret/note" expected-status="200" use-challenger="false" allowed-path-prefixes="/secret" headers="X-AUTH-TOKEN: {{authToken}}||Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /secret/note with Custom header X-AUTH-TOKEN to read the secret note">}}

> **Exercise - Amend X-AUTH-TOKEN Header**
>
> Amend the `X-AUTH-TOKEN` header so that the value is incorrect and send the request.
> Delete a few characters from the value to make it invalid.
> You should see a `403` response, meaning that the request is forbidden because the token is not authorized.

### More About Auth

For auth details and risks, read:

- [REST API Basics: Authentication](/reference/rest-api-basics#toc11)
- [HTTP Basics: Basic Authentication](/reference/http-basics#toc29)
- [API Testing Concepts and Coverage](/reference/testing-apis)

---

## OpenAPI Documentation for REST APIs

OpenAPI is a structured way to describe an HTTP API.

An OpenAPI file can describe:

- URLs and methods
- request bodies
- response bodies
- status codes
- headers
- authentication rules

View the API Challenges OpenAPI file:

{{<api-live-request method="GET" path="/docs/openapi.json" expected-status="200" use-challenger="false" allowed-path-prefixes="/docs" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /docs/openapi.json to view the OpenAPI description">}}

OpenAPI files can be used in REST Client tools to create collections of requests that make it easier to test and use the API. We have reviews of many popular [REST Clients](/tools/clients), and most will import OpenAPI specifications.

OpenAPI is useful, but it is still documentation. The running API is the source of behaviour you experience when you send requests.

Learn more about REST API documentation in [OpenAPI](/reference/openapi) and [Swagger UI](/tools/online-clients/swagger/about).

---

## CRUD Operations in REST APIs

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


For CRUD mutation practice, use the Simple API so the tutorial can create, update, and delete inventory items without challenger session tracking. Many real API update and delete actions require authorization; the Simple API does not, so you can focus on the REST and HTTP basics first. Try the sequence below. Run the `POST` first. The built-in client remembers the created item id for the later requests.

{{<api-live-request method="POST" path="/simpleapi/items" expected-status="201" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Content-Type: application/json||Accept: application/json" editable="false" query-editable="false" body='{"type":"book","isbn13":"{{randomSimpleApiIsbn}}","price":2.00,"numberinstock":3}' details="true" summary="POST /simpleapi/items to create an item">}}

{{<api-live-request method="GET" path="/simpleapi/items/{{lastCreatedSimpleApiItemId}}" expected-status="200" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /simpleapi/items/{id} to read the created item">}}

{{<api-live-request method="PATCH" path="/simpleapi/items/{{lastCreatedSimpleApiItemId}}" expected-status="200" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Content-Type: application/json||Accept: application/json" editable="false" query-editable="false" body='{"price":9.99}' details="true" summary="PATCH /simpleapi/items/{id} to update the price">}}

{{<api-live-request method="DELETE" path="/simpleapi/items/{{lastCreatedSimpleApiItemId}}" expected-status="204" use-challenger="false" allowed-path-prefixes="/simpleapi" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="DELETE /simpleapi/items/{id} to delete the item">}}

Learn more about CRUD and REST in [REST API Basics](/reference/rest-api-basics) and [HTTP Methods and Verbs](/reference/http-verbs).

---

## Common REST API Mistakes

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

For deeper coverage ideas, read [API Testing Concepts and Coverage](/reference/testing-apis).

---

## REST API FAQ

### What is a REST API?

A REST API is usually an HTTP API that exposes resources through URLs and lets clients use HTTP methods such as `GET`, `POST`, `PATCH`, and `DELETE`.

### What is a REST API resource?

A REST API resource is something the API exposes, such as a todo item, a user, an order, or a product.

### What is the difference between `GET` and `HEAD`?

`GET` asks for the resource representation, including the response body. `HEAD` asks for the status and headers only, without the response body.

### What is CRUD in a REST API?

CRUD means create, read, update, and delete. REST-style APIs commonly map CRUD actions to `POST`, `GET`, `PUT` or `PATCH`, and `DELETE`.

### What is the difference between authentication and authorization?

Authentication checks who is making the request. Authorization checks whether that caller is allowed to access the requested resource.

### Is OpenAPI the same as REST?

No. REST is an API style. OpenAPI is a documentation format that can describe REST-style HTTP APIs.

---

## Next Steps

You now have a practical understanding of the basics of REST APIs:

- resources are the things exposed by the API
- URLs identify resources
- HTTP methods describe the operation
- headers and bodies shape the message
- status codes describe the outcome
- OpenAPI documents expected behaviour

Use the reference pages when one part of the tutorial needs more depth:

- [REST API Basics](/reference/rest-api-basics)
- [HTTP Basics](/reference/http-basics)
- [HTTP Methods and Verbs](/reference/http-verbs)
- [How to Test REST APIs](/tutorials/rest-api-testing)
- [OpenAPI](/reference/openapi)
- [Swagger UI](/tools/online-clients/swagger/about)

When you want more hands-on repetition, move from the guided examples here into the practice areas:

- [Simulation Mode](/practice-modes/simulation)
- [Simple API](/practice-modes/simpleapi)
- [API Challenges overview](/apichallenges)
- [API Challenge Solutions](/apichallenges/solutions)
