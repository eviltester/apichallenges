---
title: REST API Tutorial: Learn REST by Using a LIVE API
seo_title: REST API Tutorial: Learn REST by Using a LIVE API
description: Learn REST API basics by sending read-only todo requests and CRUD requests to live APIs.
lastmod: 2026-08-06
seo_description: Learn REST by using live API examples with interactive requests for resources, URLs, HTTP methods, status codes, JSON, XML, headers, auth, OpenAPI, CRUD, and common REST mistakes.
showads: true
---

# REST API Tutorial: Learn REST by Using a LIVE API

This REST API tutorial introduces the main REST ideas by using live API examples. We provide a quick practical model of REST, and have embedded HTTP clients to let you send real requests from the page. When you want to learn a topic in more detail we have links to expanded reference material.

The read-only examples use the [API Challenges API](/apichallenges) `/todos` endpoint, and the CRUD examples use the [Simple API](/practice-modes/simpleapi) when we need to create, update, or delete data. 

By the end of this tutorial you should understand the basics of:

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

For more depth, read [REST API Basics](/tutorials/rest-api-basics).

---

## Resources

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

Learn more about resources in [REST API Basics](/tutorials/rest-api-basics).

---

## URLs

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

For more about HTTP request structure, read [HTTP Basics](/tutorials/http-basics).

---

## HTTP Methods

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

When you execute the first `GET /todos` request on this page, check that the response status is `200`.

You can also ask the API for something the route does not support. Execute this `DELETE /heartbeat` request and check that the response status is `405`.

{{<api-live-request method="DELETE" path="/heartbeat" expected-status="405" use-challenger="false" allowed-path-prefixes="/heartbeat" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="DELETE /heartbeat to see a 405 status code">}}

If you open the `Raw` response tab, the first line in the response shows the HTTP status returned by the server.

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

The API Challenges `/todos` API supports both JSON and XML for most requests. Try asking for XML:

{{<api-live-request method="GET" path="/todos" expected-status="200" use-challenger="false" allowed-path-prefixes="/todos" headers="Accept: application/xml" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /todos as XML">}}

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

Headers in the request describe the content e.g. `Content-Type` but they also set preferences for how the client wants the request to be processed and what the response should be like e.g. `Accept`.

Headers in the response describe the content and provide follow on information for the client e.g. `Content-Type`, `Location`.

Issue the request below and look at the headers in both the response and the request to see the difference.

{{<api-live-request method="GET" path="/todos" expected-status="200" use-challenger="false" allowed-path-prefixes="/todos" headers="Accept: application/json" editable="true" edit-mode="fixed" query-editable="false" body-editable="false" details="true" summary="GET /todos and inspect the headers in the response">}}

To see the actual headers sent in the Request you really need to open the Browser Dev Tools, and look in the Network tab. This will show every Request and Response and you'll see the full details of the request sent through to the server. The Browser will have added additional headers that are not listed in the HTTP Client headers field. Clients often add additional headers and it is worth being aware of that in case it impacts your testing or use of the API.

> **Exercise: Change the `Accept` Header**
>
> In our default request we asked for JSON.
> Use the editable exercise below. Keep the URL as `/todos`, but change the `Accept` header between:
>
> `Accept: application/json`
> 
> and
> 
> `Accept: application/xml`
>
> Then compare the response bodies.



Learn more about headers in [HTTP Basics](/tutorials/http-basics).

---

## Authentication and Authorization (Auth)

Authentication answers "who is making the request?

Authorization answers "what is that caller allowed to do?"

So you might be Authenticated and can login to the system, but you might not be Authorized to access all parts of the system.

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

Authentication is often the first step. The API asks "can you prove who you are?" The `GET /secret/token` endpoint uses Basic Auth to prove who you are, then returns the Authorization token to allow you access to private data.

Run this request first to get the Authorization token:

{{<api-live-request method="GET" path="/secret/token" expected-status="200" use-challenger="false" allowed-path-prefixes="/secret" headers="Authorization: Basic YWRtaW46cGFzc3dvcmQ=||Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /secret/token with Basic Auth to receive a read-only token">}}

The Authorization token is also shown in the response headers as the `X-AUTH-TOKEN` header.

> **Exercise - Amend Authorization Header**
>
> Amend the Authorization header so that the encoded credentials are incorrect and send the request.
> Delete a few characters from the credentials to make them invalid.
> You should see a 401 response, meaning that you are not Authenticated.

### Authorization

Authorization is the next step. The API asks "is this caller allowed to access this resource?" Use the `X-AUTH-TOKEN` value from the previous response as a bearer token Authorization header to read the protected secret note:

{{<api-live-request method="GET" path="/secret/note" expected-status="200" use-challenger="false" allowed-path-prefixes="/secret" headers="Authorization: Bearer {{authToken}}||Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /secret/note with Bearer Token to read the secret note">}}

> **Exercise - Amend Authorization Header**
>
> Amend the Authorization header so that the Bearer Token value is incorrect and send the request.
> Delete a few characters from the value to make it invalid.
> You should see a 403 response, meaning that you are Forbidden from accessing the content and are therefore not Authorized.

### Custom Headers

Custom HTTP Response and Request headers are often prefixed by `X-` and many APIs use this convention to have API keys which Authorize requests.

The API Challenges API uses the `X-AUTH-TOKEN` custom header for the API key, so we are also able to Authorize our requests using the `X-AUTH-TOKEN` from the previous response to read the protected secret note:

{{<api-live-request method="GET" path="/secret/note" expected-status="200" use-challenger="false" allowed-path-prefixes="/secret" headers="X-AUTH-TOKEN: {{authToken}}||Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /secret/note with Custom header X-AUTH-TOKEN to read the secret note">}}

> **Exercise - Amend X-AUTH-TOKEN Header**
>
> Amend the X-AUTH-TOKEN header so that the value is incorrect and send the request.
> Delete a few characters from the value to make it invalid.
> You should see a 403 response, meaning that you are Forbidden from accessing the content and are therefore not Authorized.

### More About Auth

For auth details and risks, read:

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

View the API Challenges OpenAPI file:

{{<api-live-request method="GET" path="/docs/openapi.json" expected-status="200" use-challenger="false" allowed-path-prefixes="/docs" headers="Accept: application/json" editable="false" query-editable="false" body-editable="false" details="true" summary="GET /docs/openapi.json to view the OpenAPI description">}}

OpenAPI files can be used in REST Client tools to create collections of requests that make it easier to test and use the API. We have reviews of many popular [REST Clients](/tools/clients), and most will import OpenAPI specifications.

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


For CRUD mutation practice, use the Simple API so the tutorial can create, update, and delete inventory items without challenger session tracking. Most API Update and Delete actions will require Authorization, to make it easy to practice testing APIs our Simple API does not require Authorization so you don't have to deal with that yet. Try the sequence below. Run the `POST` first. The built-in client remembers the created item id for the later requests.

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

You now have a practical understanding of the basics of REST APIs:

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
