---
title: REST API Basics for API Testing
seo_title: REST API Basics for Testers: Resources, Verbs, CRUD, Auth
description: Basic REST API tutorial to learn what is a REST API and how they work.
lastmod: 2026-08-04
seo_description: Learn REST API with practical examples and clear guidance you can apply immediately when creating requests, analyzing responses, and testing APIs.
showads: true
---

# REST API Basics for Testers

- What is a REST API?
- CRUD and REST
- HTTP Verbs - HEAD, PUT, PATCH, QUERY
- Authentication and Authorisation

REST APIs are HTTP APIs that follow a set of architectural ideas about resources, representations, stateless requests, and standard HTTP behaviour.

In practice, REST gives us a useful way to design and test APIs. The URL identifies a resource, the HTTP verb describes the action, headers describe the message, and the request or response body contains a representation of the data.

This page explains the basic REST ideas that help when reading API documentation, creating requests, and deciding what behaviour to test.

---

## What is a REST API?

- HTTP API - generic, anything goes
- REST API
    - the HTTP verbs mean something specific e.g. should not `DELETE` with a `POST` request
    - URIs are nouns and describe entities

A REST API is an HTTP API that is designed around resources.

A resource is something the API exposes. For example:

- a collection of todos: `/todos`
- a specific todo: `/todos/1`
- a collection of users: `/users`
- a specific user: `/users/123`

The resource is identified by the URI, often the path part of the URL.

The HTTP verb then describes what we want to do with that resource:

~~~~~~~~
GET /todos
POST /todos
GET /todos/1
PUT /todos/1
PATCH /todos/1
DELETE /todos/1
~~~~~~~~

This is different from an HTTP API where "anything goes". An HTTP API might use `POST` for every action, or use action-based URLs such as `/deleteTodo` or `/createUser`.

Many HTTP APIs describe themselves as REST APIs but are inconsistent in how they interpret the REST style. That is common in the real world.

When working with APIs, having an understanding of REST gives us a base level of expectation for how the API might work. It also gives us a shared vocabulary for discussing design choices and testing risks.

Often, REST API is simply used to mean HTTP API. When testing, it is worth checking what the team means by REST rather than assuming everyone means exactly the same thing.

---

## REST Standards?

[Representational State Transfer](https://en.wikipedia.org/wiki/Representational_state_transfer), or REST, was described by Roy Fielding in his dissertation "Architectural Styles and the Design of Network-based Software Architectures".

- "Architectural Styles and the Design of Network-based Software Architectures" by [Roy Fielding](http://www.ics.uci.edu/~fielding/)
    - [ics.uci.edu/~fielding/pubs/dissertation/top.htm](https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm)

REST is an architectural style rather than a checklist of endpoint naming rules.

This is one reason there is so much disagreement online and in teams. People often use "REST" to mean slightly different things:

- "uses HTTP and JSON"
- "uses nouns in URLs"
- "uses `GET`, `POST`, `PUT`, `PATCH`, and `DELETE` consistently"
- "follows the architectural constraints described by Fielding"

For day to day API testing, we do not usually need to debate the whole dissertation before we can make progress.

We can use the most practical parts of REST as testing guidance:

- resources should be identifiable by URLs
- HTTP verbs should have consistent meaning
- status codes should match the outcome
- requests should contain all the information needed to process them
- representations should be described by headers such as `Content-Type` and `Accept`

But ultimately, an API is what the team and company decide it is. Even if it ignores the standards and guidelines. We have to test for risks and issues that are consistent with the API and the agreed usage. We can't test APIs against standards and guidelines if no-one no the team has accepted the standard and guidelines as requirements.

---

## Guidance

- Idempotent - same request, same result on the server, not necessarily in the response
- Stateless - server does not need to maintain state of client requests between requests
- Cacheable - responses should make clear whether they can be cached
- Does it comply with HTTP Standard Guidance?

REST guidance helps us ask better questions about an API.

`Idempotent` means that sending the same request more than once should leave the server in the same final state.

For example, sending the same `PUT /todos/1` request twice should still leave todo `1` with the same values. Sending the same `DELETE /todos/1` request twice should still leave todo `1` deleted, even if the second response is different.

`POST` is usually not idempotent. Sending the same `POST /todos` request twice might create two todo items.

`Stateless` means that each request should contain the information the server needs to process it.

For example, this is not a stateless API design:

- request 1: select these files
- request 2: delete the files selected in the previous request

The second request relies on remembered state from the first request. A stateless design would `GET` a list of files, then list the files to delete in the `DELETE` request itself.

`Cacheable` means that responses should include enough information for clients, browsers, or proxies to know whether the response can be reused. `GET` responses are often candidates for caching. `POST`, `PUT`, `PATCH`, and `DELETE` usually need more care because they are associated with changing server state.

When testing, these ideas help us spot risks:

- will retrying a request create duplicate data?
- does the server rely on hidden state from a previous request?
- do cache headers allow stale data to be shown?
- does the API follow the HTTP standards it claims to follow?

---

## CRUD

CRUD means:

- Create
- Read
- Update
- Delete

CRUD is a common way to describe data operations, but HTTP verbs are not exactly the same thing as CRUD actions.

| CRUD Action | Common HTTP Verb(s) |
|-------------|---------------------|
| Create | `POST`, sometimes `PUT` |
| Read | `GET`, sometimes `HEAD` or `QUERY` |
| Update | `PUT`, `PATCH`, sometimes `POST` |
| Delete | `DELETE` |

The mapping depends on the API design.

For example, a common REST-style design is:

~~~~~~~~
POST /todos       -> create a todo
GET /todos/1      -> read a todo
PUT /todos/1      -> replace a todo
PATCH /todos/1    -> partially update a todo
DELETE /todos/1   -> delete a todo
~~~~~~~~

But real APIs vary.

Some APIs use `POST` for update because HTML forms historically only supported `GET` and `POST`. Some APIs use `PUT` to create resources when the client chooses the id. Some APIs do not support `PATCH` at all.

For testing, avoid assuming that CRUD maps perfectly to one verb. Read the documentation, then test the documented behaviour and the edge cases around unsupported verbs.

---

## Endpoints vs URL

Very often when discussing REST APIs we talk about endpoints or even 'routing'.

Basically, an endpoint is the path part of the URL, sometimes combined with the HTTP verb. 'Routing' means how the application routes the request for processing.

For example:

~~~~~~~~
GET /lists
POST /lists
GET /lists/123
~~~~~~~~

In casual conversation, people might call `/lists` an endpoint.

For testing, it is usually clearer to include the verb as well:

~~~~~~~~
GET /lists
POST /lists
~~~~~~~~

These are different API operations even though the path is the same.

The following URLs have the same endpoint path:

- `/lists`
- `/lists?title=title`

The query string changes the request, but the route or endpoint path is still `/lists`.

We have to pay attention to this because a problem might be caused by:

- the wrong HTTP verb
- the wrong path
- a missing path parameter
- a query string that changes filtering, sorting, or pagination

---

## Payloads vs Body

A payload is the content in the body of the HTTP request or response.

In API testing, people often use `payload`, `body`, and `message body` to mean similar things.

The body is the actual content sent after the headers.

Common API payload formats include:

- JSON
- XML
- form encoded data
- plain text
- files or multipart form data

REST APIs often use JSON or XML because those formats can be parsed into objects by the application.

The request body format is described by the `Content-Type` header:

~~~~~~~~
Content-Type: application/json
~~~~~~~~

The response format we would prefer is requested with the `Accept` header:

~~~~~~~~
Accept: application/json
~~~~~~~~

The server might not support every format. If we send XML to an endpoint that only supports JSON, we might receive `415 Unsupported Media Type`. If we ask for XML but the server only returns JSON, we might receive `406 Not Acceptable`, or the server might ignore the preference depending on the API contract.

---

## Requesting Formats

| Header | Means |
|--------|-------|
| `Accept: application/json` | Please return JSON |
| `Accept: application/xml` | Please return XML |
| `Content-Type: application/json` | This payload is JSON |
| `Content-Type: application/xml` | This payload is XML |

`Accept` and `Content-Type` are easy to mix up.

`Accept` is about the response:

~~~~~~~~
Accept: application/json
~~~~~~~~

This tells the server "I would like JSON back."

`Content-Type` is about the request body:

~~~~~~~~
Content-Type: application/json
~~~~~~~~

This tells the server "I am sending JSON."

XML might also be sent as `text/xml`, depending on the API.

The server might not support a particular format. It might default to JSON or XML and ignore the header. Or it might reject the request with a clear status code. The API documentation should tell us what to expect.

For testing, try the normal supported formats first, then try unsupported or missing headers to see whether the API fails clearly.

---

## Authentication

If you make a request to a server and receive a `401`, then you are not authenticated.

Authentication means the system does not know who you are, or does not accept the credentials you supplied.

The response should often include a `WWW-Authenticate` header to challenge the client with the authentication required.

For example:

~~~~~~~~
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Basic realm="api"
~~~~~~~~

Common authentication bugs include:

- `WWW-Authenticate` not sent back in the response
- valid credentials rejected
- invalid credentials accepted
- authentication required in documentation but not enforced
- different behaviour between browser, API client, and automation requests

When testing authentication, use known valid and invalid credentials, and check both the status code and response headers.

---

## Common Authentication Approaches

There are several common ways that APIs and Web Applications identify the caller.

### Basic Auth Header

[Basic Auth](http://tools.ietf.org/html/7617) sends credentials in the `Authorization` header.

~~~~~~~~
Authorization: Basic Ym9iOmRvYmJz
~~~~~~~~

The value after `Basic` is base64 encoded `username:password`.

This is not encryption. It is just encoding. Basic Auth should be used over `https` so the connection is encrypted.

### Cookies

Cookies are common in browser-based applications.

The usual flow is:

- user logs in
- server sends back a session cookie
- browser sends the session cookie on future requests

APIs can use cookies too, especially when the API is used by a browser front end.

### Custom Headers

Some APIs use custom headers for secret codes, API keys, tenant ids, or other application-specific information.

For example:

~~~~~~~~
X-API-AUTH: thisismysecretapicode
~~~~~~~~

Custom headers are common, but they need clear documentation because the HTTP standard will not tell a client what they mean.

### URL Authentication

URL authentication puts credentials in the URL:

~~~~~~~~
https://username:password@www.example.com/
~~~~~~~~

This is deprecated and should generally be avoided. Credentials in URLs can leak through logs, browser history, screenshots, analytics tools, and proxies.

Recommended reading [developer.mozilla.org/en-US/docs/Web/HTTP/Authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)

---

## Authentication vs Authorization

Authentication asks:

- Are you authenticated?
- Does the system know who you are?
- Are your auth details correct?

Authorization asks:

- You are authenticated, but do you have permission to access this endpoint?
- Are you allowed to perform this action?
- Are you allowed to access this specific resource?

The difference should also be reflected in status codes.

A `401 Unauthorized` response normally means "you are not authenticated."

A `403 Forbidden` response normally means "the system knows who you are, but you are not allowed to do this."

For example:

- no token when requesting `/admin/users` might return `401`
- a valid non-admin token when requesting `/admin/users` might return `403`

For API testing, check both cases. It is common for APIs to handle missing authentication but forget to check permissions after authentication succeeds.

---

## Real World vs Standards

Teams debate this all the time.

- Login? [stackoverflow.com/questions/13916620](https://stackoverflow.com/questions/13916620/rest-api-login-pattern)
- Put vs Post [stackoverflow.com/questions/630453](https://stackoverflow.com/questions/630453/put-vs-post-in-rest)
- see discussions on [restcookbook.com](http://restcookbook.com)

REST gives us guidance, but real systems are messy.

Some APIs are called REST APIs even though they use `POST` for everything. Some APIs use action-based endpoints because they are easier for a team to understand. Some APIs inherit constraints from old clients, frameworks, gateways, or security layers.

As a tester, our job is not to win a terminology argument. Our job is to understand the intended behaviour, test the risks, and communicate clearly.

Use standards and documentation as support:

- HTTP standards for verbs, headers, status codes, idempotency, and response recommendations
- API documentation for what this system says it should do
- observed behaviour from requests and responses
- team agreements when the documentation is incomplete

Expect discussions and debates on a team. When they happen, concrete examples help:

~~~~~~~~
I sent PUT /todos/1 twice with the same body.
The first response was 200.
The second response created a duplicate todo.
That does not match the usual idempotent expectation for PUT.
~~~~~~~~

This is more useful than simply saying "this is not RESTful."

---

### Verb - Head

- [HEAD](https://www.rfc-editor.org/rfc/rfc9110.html#name-head)
- same as GET but does not return a body
- can be useful for checking existence of an endpoint or entity
- see the [HTTP HEAD verb tutorial](/tutorials/http-verbs#http-head-verb)

`HEAD` is useful when we want to inspect metadata without downloading the full response body.

For example, a `HEAD` request might tell us whether a resource exists, what `Content-Type` it would return, or whether a cached copy is still valid.

When testing REST APIs, check that `HEAD` behaves consistently with `GET` for the same resource. If `GET /todos/1` returns `200`, then `HEAD /todos/1` would usually return `200` with the same kind of response headers and no body.

---

### Verb - Put

- [PUT](https://www.rfc-editor.org/rfc/rfc9110.html#name-put) is usually used to create or replace the state of a resource
- PUT should be idempotent, so sending the same request repeatedly should leave the server in the same state

Teams often choose one of three identifier styles for update requests:

- `PUT /todos/{id}` with no id in the payload
- `PUT /todos/{id}` with a matching id in the payload
- `PUT /todos` with the id in the payload

Each style has tradeoffs:

- URL ids make routing and logs easy to read
- body ids can match object models and message contracts
- duplicating id in URL and body can feel like duplication
- if the id appears in both places, the API must reject mismatched ids clearly
- id only in the payload might not be considered RESTful because the URI no longer uniquely identifies the resource
- id only in the payload can make routing, logging, and caching less straightforward

For API testing, check which style the API supports, then test the edge cases:

- no id in either URL or payload
- id in the URL but not in the payload
- id in the payload but not in the URL
- id in both places and matching
- id in both places and different

Also check whether `PUT` replaces the full resource or behaves more like a partial update.

If the API says `PUT` is a full replacement, then omitting a field should have a documented outcome. It might remove the field, reset it, reject the request, or preserve the old value. Different APIs make different choices, and those choices need tests.

---

### Verb - Patch

- [PATCH](https://www.rfc-editor.org/rfc/rfc5789) - an update method which provides a set of changes
- Standard for [JSON Merge Patch format](https://www.rfc-editor.org/rfc/rfc7396)
- Standard for [JSON Patch format](https://www.rfc-editor.org/rfc/rfc6902)
- Proposed standard for [XML Patch Using XPath](https://tools.ietf.org/html/rfc5261)
- see the [HTTP PATCH verb tutorial](/tutorials/http-verbs#http-patch-verb)

`PATCH` is intended for partial updates.

Instead of sending a full replacement representation, the client sends the changes it wants to apply.

The main testing question is "what kind of patch document does this endpoint accept?"

Common options include:

- a partial JSON object with `Content-Type: application/json`
- JSON Merge Patch with `Content-Type: application/merge-patch+json`
- JSON Patch with `Content-Type: application/json-patch+json`

These formats are not the same. They have different rules for arrays, missing fields, `null` values, and operations.

Most web services historically used `POST` or `PUT` instead of `PATCH`, so do not assume `PATCH` is supported just because an API is called REST.

When testing `PATCH`, check:

- valid partial updates
- unknown fields
- fields set to `null`
- invalid patch formats
- patches against missing resources
- conflicts when the resource has changed since the client last read it

---

## General REST Guidelines

Basics:

- URL `->` Which resource?
- HTTP method `->` What operation?
- Body `->` What should its state become?

For a new REST API, a simple pattern is:

- `POST /items` `->` create a new item
- `GET /items/{id}` `->` retrieve an item
- `PUT /items/{id}` `->` replace an item
- `PATCH /items/{id}` `->` partially update an item
- `DELETE /items/{id}` `->` delete an item

For `PUT` and `PATCH`, make the URL authoritative for the resource id.

If your object model includes an id field in the payload, either ignore it or validate that it matches the URL and reject mismatches. This avoids ambiguity while remaining compatible with clients that serialise complete objects.

For testing, start with the happy path for each supported operation, then vary one important detail at a time:

- missing id
- unknown id
- malformed body
- unsupported `Content-Type`
- unsupported `Accept`
- missing authentication
- authenticated but not authorised
- duplicate create
- repeated idempotent request
- unsupported verb
- etc. _(this is not a complete list, **in fact any list you see... ever... add 'etc.' to the end**)_

The aim is not to prove that the API is "perfectly RESTful". The aim is to understand and test the contract the API claims to offer.

---

## Recommended Reading

Reading:

- read the REST Dissertation [ics.uci.edu/~fielding/pubs/dissertation/top.htm](https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm)
- Read the docs on authentication [developer.mozilla.org/en-US/docs/Web/HTTP/Authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)
- for real world discussions see [restcookbook.com](http://restcookbook.com)
- review [HTTP Basics](/tutorials/http-basics) for requests, responses, headers, status codes, and message bodies
- review [HTTP Verbs](/tutorials/http-verbs) for examples of each verb and common testing checks

---

## Summary

REST APIs use HTTP to expose resources. The URL identifies the resource, the HTTP verb describes the operation, headers describe the message, and the body contains a representation of data or changes.

REST gives us useful expectations: requests should be stateless, responses should use meaningful status codes, verbs should be used consistently, and repeated idempotent requests should leave the server in the same final state.

Real APIs vary, so testing needs both standards awareness and product awareness. Read the documentation, inspect the actual HTTP requests and responses, and test the behaviour that matters: resource routing, verbs, payloads, formats, authentication, authorisation, status codes, and state changes.
