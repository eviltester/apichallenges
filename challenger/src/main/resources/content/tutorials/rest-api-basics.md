---
title: REST API - Tutorial
seo_title: Tutorial: REST API Tutorial for API Testing | API Challenges
description: Basic REST API tutorial to learn what is a REST API and how they work.
lastmod: 2026-08-01
seo_description: Learn REST API with practical examples and clear guidance you can apply immediately when creating requests, analyzing responses, and testing APIs.
showads: true
---

# REST API BASICS

- What is a REST API?
- CRUD and REST
- HTTP Verbs - HEAD, PUT, PATCH, QUERY
- Authentication and Authorisation

---

## What is a REST API?

- HTTP API - generic, anything goes
- REST API
    - the HTTP Verbs mean something specific e.g. should not Delete with a POST request
    - URI are 'nouns' and describe entities

A REST API is an API that conforms to the REST standards.

Many HTTP API's describe themselves as REST APIs but are inconsistent in how they interpret the standard.

When working with APIs, having an understanding of the REST API standards can provide you with a base level of expectation of how the API might work.

Often, REST API is simply used to mean HTTP API.


---

## REST Standards?

[Representational State Transfer](https://en.wikipedia.org/wiki/Representational_state_transfer)

- Loose standards
- Lots of disagreement on teams and online
- DISSERTATION: "Architectural Styles and the Design of Network-based Software Architectures" by [Roy Fielding](http://www.ics.uci.edu/~fielding/)
    - [ics.uci.edu/~fielding/pubs/dissertation/top.htm](https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm)

---

## Guidance

- Idempotent - same request, same result (on server, not necessarily in response)
- Stateless - server does not need to maintain state of client requests between requests e.g.
    - request 1: select these files,
    - request 2: delete files selected in previous request
- Cacheable - on the server side e.g. GET can be cacheable until entities in GET are updated
- Does it comply with HTTP Standard Guidance?

---

## CRUD

- Verbs are not as simple as Create, Read, Update, Delete

| CRUD Action | Verb |
|------|--------|
| Create | POST, PUT |
| Read | GET |
| Update | POST, PUT, PATCH |
| Delete |  DELETE |

---

## Endpoints vs URL

Very often when discussing REST APIs we talk about 'endpoints'.

Basically the 'path' part of the URL.

The following are the same Endpoint

- `/lists`
- `/lists?title="title"`

---

## Payloads vs Body

A Payload is the content of the body of the HTTP request.

- XML and JSON
- Tends not to be Form encoded
- Request defined by `content-type` header
- Response requested in `accept` header
- usually unmarshalled into an object in the application

---

## Requesting Formats

| Header        | Means |
|---------------|-------|
|Accept: application/json |	Please return JSON |
|Accept: application/xml | Please return XML |
|Content-Type: application/json	| This payload is JSON |
|Content-Type: application/xml | This payload is XML |

- XML might also be : `text/xml`
- The server might not support a particular format it might default to JSON or XML and ignore the header

---

## Authentication

If you make a request to a server and receive a 401 then you are not authenticated.

`WWW-Authenticate` header should challenge you with the authentication required.

- Generally avoid header sending by known authenticating information in request.
- Common bug is `WWW-Authenticate` not sent back in response.

---

## Common Authentication Approaches

- [Basic Auth Header](http://tools.ietf.org/html/7617)
    - `Authorization: Basic Ym9iOmRvYmJz`
    - base 64 encoded `username:password`
- Cookies
    - when 'login' server sends back a 'session cookie'
    - send 'session cookie' in future requests
- Custom Headers
    - API `secret codes`
    - e.g. `X-API-AUTH: thisismysecretapicode`

---

## Common Authentication Approaches

- URL authentication
    - `https://username:password@www.example.com/`
    - deprecated
    - used to be very common when automating web GUIs

Recommended reading [developer.mozilla.org/en-US/docs/Web/HTTP/Authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)

---

## Authentication vs Authorization

Authentication

- Are you authenticated?
- Does the system know who you are?
- Are your auth details correct?

Authorization

- you are authenticated
- do you have permission to access this endpoint?

---

## Real World vs Standards

Teams debate this all the time.

- Login? [stackoverflow.com/questions/13916620](https://stackoverflow.com/questions/13916620/rest-api-login-pattern)
- Put vs Post [stackoverflow.com/questions/630453](https://stackoverflow.com/questions/630453/put-vs-post-in-rest)
- see discussions on [restcookbook.com](http://restcookbook.com)

As a Tester:

- Refer to HTTP standards
    - headers, idempotency, response recommendations

Expect 'discussions' and 'debates' on a team.

---

### Verb - Head

- [HEAD](https://www.rfc-editor.org/rfc/rfc9110.html#name-head)
- same as GET but does not return a 'body'
- can be useful for checking 'existence' of an endpoint or entity
- see the [HTTP HEAD verb tutorial](/tutorials/http-verbs#http-head-verb)

---

### Verb - Put

- [PUT](https://www.rfc-editor.org/rfc/rfc9110.html#name-put) is usually used to create or replace the state of a resource
- PUT should be idempotent, so sending the same request repeatedly should leave the server in the same state
- Teams often choose one of three identifier styles for update requests:
    - `PUT /todos/{id}` with no id in the payload
    - `PUT /todos/{id}` with a matching id in the payload
    - `PUT /todos` with the id in the payload    
- Each style has tradeoffs:
    - URL ids make routing and logs easy to read
    - body ids can match object models and message contracts
        - but duplicating id in URL and body might feel like duplication
        - the API must reject mismatched ids clearly
    - ID only in the payload might not be considered RESTful because the URI (URL) no longer uniquely identifies the resource. It also makes routing and caching less straightforward.

For API testing, check which style the API supports, then test the edge cases:

- no id in either URL or payload
- id in the URL but not in the payload
- id in the payload but not in the URL
- id in both places and matching
- id in both places and different

---

### Verb - Patch

- [PATCH](https://www.rfc-editor.org/rfc/rfc5789) - An 'Update' method which provides a set of changes
- Contentious [see](http://williamdurand.fr/2014/02/14/please-do-not-patch-like-an-idiot/)
- Standard for [JSON Merge Patch format](https://www.rfc-editor.org/rfc/rfc7396)
- Standard for [JSON Patch format](https://www.rfc-editor.org/rfc/rfc6902)
- Proposed standard for [XML Patch Using XPath](https://tools.ietf.org/html/rfc5261)
- see the [HTTP PATCH verb tutorial](/tutorials/http-verbs#http-patch-verb)

Most web services just use `POST` or `PUT`


## General REST guidelines

Basics:

- URL `->` Which resource?
- HTTP method `->` What operation?
- Body `->` What should its state become?

General Recommendations:

For a new REST API I would recommend:

- `POST /items` `->` create a new item (server assigns or accepts an ID depending on your design)
- `GET /items/{id}` `->` retrieve an item
- `PUT /items/{id}` `->` replace/update an item
- `PATCH /items/{id}` `->` partially update an item
- `DELETE /items/{id}` `->` delete an item

For PUT and PATCH, make the URL authoritative for the resource ID. If your object model includes an id field in the payload, either ignore it or validate that it matches the URL and reject mismatches. This avoids ambiguity while remaining compatible with clients that serialize complete objects.

## Recommended Reading

Reading:

- read the REST Dissertation [ics.uci.edu/~fielding/pubs/dissertation/top.htm](https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm)
- Read the docs on authentication [developer.mozilla.org/en-US/docs/Web/HTTP/Authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)
- for real world 'discussions' see [restcookbook.com](http://restcookbook.com)
