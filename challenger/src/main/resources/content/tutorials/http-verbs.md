---
title: HTTP Verbs - Tutorial
seo_title: Tutorial: HTTP Verbs Tutorial | API Challenges
description: Basic HTTP Verbs and Methods tutorial what they do and how to use them.
lastmod: 2026-08-04
seo_description: Learn HTTP Verbs with practical examples and clear guidance you can apply immediately when creating requests, analyzing responses, and testing APIs.
showads: true
---

# HTTP Verbs Overview

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
| `GET` | Read | Return a list, often with filtering, sorting, or pagination. | Return a single item, or `404` if it does not exist. |
| `POST` | Create or process | Create a new item in the collection, often returning `201 Created` and a `Location` header. | Sometimes used for item-specific actions, but not usually for replacing the item. |
| `PUT` | Create or replace | Rare for a whole collection unless the API explicitly supports replacing the collection. | Replace the item using the supplied representation, or create it if client-chosen ids are supported. |
| `PATCH` | Modify | Rare for a whole collection unless the API supports collection-level changes. | Apply selected changes to the item. |
| `DELETE` | Delete | Rare and dangerous for a whole collection unless explicitly supported. | Delete the item, often returning `204 No Content`. |
| `HEAD` | Read metadata | Return headers for the collection without a body. | Return headers for the item without a body. |
| `OPTIONS` | Discover options | Return allowed methods or communication options. | Return allowed methods or communication options. |
| `QUERY` | Read with body content | Return matching resources using query criteria in the request body. | Less common for a specific item, but possible if documented. |

These are conventions rather than automatic behaviour.

The server application still decides what each route supports. If an API does not support `DELETE /customers/12345` then it should reject the request, even though `DELETE` is a valid HTTP verb.

---

## Safe and Idempotent Verbs

Two useful words appear often when people discuss HTTP verbs: `safe` and `idempotent`.

A `safe` request is intended for reading information. It should not ask the server to change the resource. `GET`, `HEAD`, `OPTIONS`, and `QUERY` are intended to be safe.

This does not mean the server does absolutely nothing. A server might log the request, update analytics, or refresh a cache. The important point is that the client did not ask to change the resource being addressed. The user shouldn't really expect any side-effects.

An `idempotent` request is one where sending the same request once, twice, or several times has the same intended effect on the resource.

For example, if `PUT /todos/1` replaces todo `1` with the same supplied JSON each time, then the todo ends in the same state each time.

`POST` is usually not idempotent. If you send the same `POST /todos` request twice, the API might create two todo items.

When testing and automating retry behaviour is safer with idempotent methods. Retrying a failed `GET` or `PUT` is usually less risky than retrying a `POST` that might create another item.

| **Verb** | **Safe?** | **Usually Idempotent?** | **Why it Matters** |
|----------|-----------|-------------------------|--------------------|
| `GET` | Yes | Yes | Repeating a read should not change the resource. |
| `HEAD` | Yes | Yes | Like `GET`, but without the response body. |
| `OPTIONS` | Yes | Yes | Used to ask what is allowed. |
| `QUERY` | Yes | Yes | Used to retrieve data with query content. |
| `POST` | No | No | Repeating it may create or process more than once. |
| `PUT` | No | Yes | Repeating the same replacement should leave the same final state. |
| `PATCH` | No | Sometimes | Depends on the patch format and what the patch does. |
| `DELETE` | No | Yes | Repeating it leaves the resource deleted, though later responses may be `404`. |

When the API documentation says a route behaves differently from these conventions, test the documented behaviour. When there is no documentation, these conventions give us a useful starting point for exploration.

---

## HTTP GET Verb

- [GET](https://www.rfc-editor.org/rfc/rfc9110.html#name-get) - retrieve data
- GET verbs can be issued by a browser
    - click on link
    - visit a site
- GET `https://apichallenges.eviltester.com/mirror/raw`
- Important Headers
    - User-Agent - tells server app type
    - Accept - what format response you prefer

`GET` is the verb we use most often when we want to read information.

When you type a URL into a browser, click a link, load an image, or fetch data from JavaScript, there is a good chance a `GET` request is being sent.

In a REST-style API, `GET /todos` would usually return a list of todo items, and `GET /todos/1` would usually return one todo item.

`GET` requests should not be used to create, edit, or delete resources. If a `GET` request changes important server state then it can cause problems with browser prefetching, search engine crawlers, caching, retries, and automated checks.

The server often responds differently based on the `User-Agent` and you might have to control the `User-Agent` when testing to trigger mobile behaviour or possibly even bypass controls that restrict automated tooling.

The `Accept` header is often important with `GET` because the client may ask for a preferred response format, e.g. `application/json` or `application/xml`.

---

### HTTP GET Verb Example

~~~~~~~~
curl {{<ORIGIN_URL>}}/heartbeat ^
-H "accept: application/xml" ^
--proxy 127.0.0.1:8888
~~~~~~~~

~~~~~~~~
GET {{<ORIGIN_URL>}}/heartbeat HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Connection: Keep-Alive
accept: application/xml
~~~~~~~~

---

### Common HTTP Status codes in response to a GET

- **200** - OK, found the url, returned contents
- **301, 307, 308** - content has moved, new url in `location` header
- **404** - url not found
- **401** - authentication is required, see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it

For a collection URL, a successful `GET` might return an empty list rather than `404`.

For a specific item URL, `404` is common when the id does not match any resource.


---

## HTTP HEAD Verb

- [HEAD](https://www.rfc-editor.org/rfc/rfc9110.html#name-head) - request the same headers as GET without a response body
- HEAD is useful for checking whether a resource exists, checking caching headers, or validating metadata without transferring the representation
- A HEAD response should not include a body

`HEAD` is like asking "what would the headers look like if I sent a GET request here?"

The server should send the same response headers that it would send for `GET`, but without sending the response body.

This can be useful when we want to check whether a resource exists, inspect caching headers, check content type, or avoid downloading a large body.

For testing, `HEAD` is useful because some APIs accidentally route it differently from `GET`, or accidentally return a body when they should not.

---

### HTTP HEAD Verb Example

~~~~~~~~
curl -I {{<ORIGIN_URL>}}/todos
~~~~~~~~

~~~~~~~~
HEAD {{<ORIGIN_URL>}}/todos HTTP/1.1
User-Agent: curl/8.0.0
Host: localhost:4567
Accept: */*
~~~~~~~~

---

### Common HTTP Status codes in response to a HEAD

- **200** - OK, the resource exists
- **204** - OK, no content
- **404** - resource not found
- **405** - method not allowed for this endpoint


---

## HTTP QUERY Verb

- [QUERY](https://www.rfc-editor.org/rfc/rfc10008.html) - safely retrieve data using query content in the request body
- QUERY is intended for safe, read-only requests where the query content is too large, complex, or structured for a URI query string
- A server can advertise supported QUERY request content types with `Accept-Query`
- API Challenges supports `QUERY /todos` with `Content-Type: application/x-www-form-urlencoded`

`QUERY` is newer and less widely supported than the traditional verbs.

It exists for the situation where we want the read-only behaviour of a `GET`, but the query is too large or too structured to fit comfortably in a URL query string.

For example, a simple filter might fit neatly into a URL:

~~~~~~~~
GET /todos?doneStatus=true
~~~~~~~~

But a more complex query might be easier to send in a request body. That is the type of use case `QUERY` is designed for.

Because `QUERY` is not as widely supported as `GET` and `POST`, always check whether your client, proxy, gateway, and server framework support it before relying on it in a real API.

---

### HTTP QUERY Verb Example

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

### Common HTTP Status codes in response to a QUERY

- **200** - OK, returned the matching representation
- **400** - invalid query content
- **405** - method not allowed for this endpoint
- **415** - unsupported query content type


---

## HTTP POST Verb

- [POST](https://www.rfc-editor.org/rfc/rfc9110.html#name-post) - amend/create from partial information

- send a body and describe the body format in the `Content-Type` header
- usually used to create data or ask the server to process data
- browser will usually send a POST request when submitting a form

`POST` is the most general purpose of the common HTTP verbs.

It tells the server "process this request body using whatever behaviour this endpoint defines."

In REST-style APIs, `POST` is commonly used to create a new resource inside a collection:

~~~~~~~~
POST /todos
~~~~~~~~

The client sends the representation in the request body. The server validates it, creates the new resource, assigns an id, and often returns `201 Created`.

When the server creates a new resource, a `Location` header is often used to tell the client the URL of the new resource:

~~~~~~~~
Location: /todos/42
~~~~~~~~

`POST` can also be used for operations that do not fit cleanly into create, read, update, or delete. For example, a search endpoint, an upload, or a command-style operation might use `POST`.

Because `POST` is usually not idempotent, be careful with retries. If the first request created a resource but the response was lost, sending the same request again might create a duplicate unless the API has duplicate detection or idempotency keys.

---

### HTTP POST Verb Send Example

~~~~~~~~
curl -X POST {{<ORIGIN_URL>}}/lists ^
-H "accept: application/xml" ^
-H content-type:application/json ^
-H "Authorization: Basic dXNlcjpwYXNzd29yZA==" ^
-d "{title:'a list title'}" ^
--proxy 127.0.0.1:8888
~~~~~~~~

---

### HTTP POST Verb Request Example

~~~~~~~~
POST {{<ORIGIN_URL>}}/lists HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Connection: Keep-Alive
accept: application/json
content-type: application/json
Authorization: Basic dXNlcjpwYXNzd29yZA==
Content-Length: 22

{title:'a list title'}

~~~~~~~~

---

### HTTP POST Verb Response Example

~~~~~~~~
HTTP/1.1 201 Created
Date: Thu, 17 Aug 2017 12:11:12 GMT
Content-Type: application/json
Location: /lists/f8134dd6-a573-4cf5-a6c6-9d556118ed0b
Server: Jetty(9.4.4.v20170414)
Content-Length: 171

{"lists":[{
"guid":"f8134dd6-a573-4cf5-a6c6-9d556118ed0b",
"title":"a list title",
"description":"",
"createdDate":"2017-08-17-13-11-12",
"amendedDate":"2017-08-17-13-11-12"}]}
~~~~~~~~

---

### Common HTTP Status codes in response to a POST

- **200** - OK, request was processed and a response body may have been returned
- **201** - created a new resource
- **202** - request was accepted for later processing
- **204** - request was processed and no response body was returned
- **400** - request was malformed or invalid
- **404** - target url was not found
- **401** - authentication is required, see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it
- **409** - request conflicts with existing server state
- **415** - unsupported request body content type
- **422** - request body was understood but failed validation
- **500** - server error while processing the request


---

## HTTP PUT Verb

- [PUT](https://www.rfc-editor.org/rfc/rfc9110.html#name-put) - create or replace from full information

`PUT` is usually used when the client is sending a complete replacement representation for a resource.

For example:

~~~~~~~~
PUT /todos/1
~~~~~~~~

Means "replace todo `1` with the representation in this request body."

This is different from `POST /todos`, where the server usually decides the new id.

With `PUT`, the URL often identifies the exact resource being created or replaced. If the API supports client-chosen ids, then `PUT /todos/123` might create todo `123` when it does not already exist. If the API does not support creation by `PUT`, then the same request might return `404` or `405`.

Full information means the request body should contain the representation needed to replace the resource. If a field is missing, the server might remove it, reset it to a default, reject the request, or follow an application-specific rule. The API documentation should make this clear.

`PUT` should be idempotent. If we send the same `PUT` request again, the resource should end up in the same state as it did after the first request.

---

### HTTP PUT Send Example

~~~~~~~~
curl -X PUT {{<ORIGIN_URL>}}/lists ^
-H "Authorization: Basic dXNlcjpwYXNzd29yZA==" ^
--proxy 127.0.0.1:8888 ^
-d @createlistwithput.txt
~~~~~~~~

where `createlistwithput.txt` file contains

~~~~~~~~
{"title":"title added with put",
"description":"list description",
"guid": "guidcreatedwithput201708171440",
"createdDate": "2017-08-17-14-40-34",
"amendedDate": "2017-08-17-14-40-34"}
~~~~~~~~

---

### HTTP PUT Request Example

~~~~~~~~
PUT {{<ORIGIN_URL>}}/lists HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Accept: */*
Connection: Keep-Alive
Authorization: Basic dXNlcjpwYXNzd29yZA==
Content-Length: 180
Content-Type: application/json

{"title":"title added with put",
"description":"list description",
"guid": "guidcreatedwithput201708171440",
"createdDate": "2017-08-17-14-40-34",
"amendedDate": "2017-08-17-14-40-34"}
~~~~~~~~

---

### HTTP PUT Response Example

~~~~~~~~
HTTP/1.1 201 Created
Date: Thu, 17 Aug 2017 13:41:46 GMT
Content-Type: application/json
Server: Jetty(9.4.4.v20170414)
Content-Length: 0
~~~~~~~~

---

### Common HTTP Status codes in response to a PUT

- **200** - OK, resource was replaced and a representation was returned
- **201** - resource was created
- **204** - resource was replaced and no response body was returned
- **400** - request body or URL was invalid
- **401** - need authorisation see `WWW-Authenticate` header
- **403** - resource probably exists but you are not allowed to replace it
- **404** - target resource was not found, and create-by-PUT is not supported
- **409** - request conflicts with the current resource state
- **415** - unsupported request body content type
- **422** - request body was understood but failed validation

---

## HTTP PATCH Verb

- [PATCH](https://www.rfc-editor.org/rfc/rfc5789) - apply a set of changes to an existing resource
- PATCH is often used when a client wants to update selected fields without replacing the whole resource
- Servers can advertise supported PATCH request content types with `Accept-Patch`
- Common JSON PATCH styles:
    - [JSON Merge Patch](https://www.rfc-editor.org/rfc/rfc7396) with `Content-Type: application/merge-patch+json`
    - [JSON Patch](https://www.rfc-editor.org/rfc/rfc6902) with `Content-Type: application/json-patch+json`
- Some APIs also support partial JSON object updates with `Content-Type: application/json`

`PATCH` is used when the client wants to change part of an existing resource rather than replace the whole thing.

For example:

~~~~~~~~
PATCH /todos/1
~~~~~~~~

Might mean "change only the fields described in this request body."

This is useful when a resource has many fields and the client only wants to update one or two of them.

The important testing question with `PATCH` is "what format are the changes written in?"

Some APIs accept a partial JSON object. Some use [JSON Merge Patch](https://www.rfc-editor.org/rfc/rfc7396). Some use [JSON Patch](https://www.rfc-editor.org/rfc/rfc6902). These formats can look similar at a glance, but they have different rules.

`PATCH` is not automatically idempotent. A patch that says "set title to X" can be idempotent. A patch that says "increment count by 1" is not idempotent because sending it twice changes the result.

If two clients patch the same resource at the same time, one change can accidentally overwrite or conflict with another. APIs can reduce this risk with conditional requests, e.g. using an `ETag` value and an `If-Match` header.

---

### HTTP PATCH Send Example

~~~~~~~~
curl -X PATCH {{<ORIGIN_URL>}}/todos/3 ^
-H "Content-Type: application/json" ^
-H "Accept: application/json" ^
-d "{\"title\":\"patched title\"}"
~~~~~~~~

---

### HTTP PATCH Partial JSON Example

~~~~~~~~
PATCH {{<ORIGIN_URL>}}/todos/3 HTTP/1.1
User-Agent: rest-client
Host: localhost:4567
Content-Type: application/json
Accept: application/json

{"title":"patched title"}
~~~~~~~~

---

### HTTP PATCH JSON Merge Patch Example

~~~~~~~~
PATCH {{<ORIGIN_URL>}}/todos/3 HTTP/1.1
User-Agent: rest-client
Host: localhost:4567
Content-Type: application/merge-patch+json
Accept: application/json

{"description":"patched description"}
~~~~~~~~

The merge-patch may look similar to the `application/json` patch. But the main difference is the standard handling.

An API can use whatever conventions it wants to handle an `application/json` patch. APIs might choose to ignore a field you try to set as `null` or they might throw an error. It is their choice.

But in a `merge-patch+json` standard, a `null` is a delete, so if you try to delete a field value then it must be `nullable` and if not you'll see an error. So it is important when you test an application that supports `merge-patch+json` that you double check the processing against the standard. [JSON Merge Patch](https://www.rfc-editor.org/rfc/rfc7396)

---

### HTTP PATCH JSON Patch Example

~~~~~~~~
PATCH {{<ORIGIN_URL>}}/todos/3 HTTP/1.1
User-Agent: rest-client
Host: localhost:4567
Content-Type: application/json-patch+json
Accept: application/json

[{"op":"replace","path":"/title","value":"patched title"}]
~~~~~~~~

---

### Common HTTP Status codes in response to a PATCH

- **200** - OK, resource was updated and a representation was returned
- **204** - OK, resource was updated and no body was returned
- **404** - target resource was not found
- **405** - method not allowed for this endpoint
- **409** - conflict applying the patch
- **415** - unsupported patch document content type
- **422** - patch content was understood but could not be applied


---

## HTTP DELETE Verb

- [DELETE](https://www.rfc-editor.org/rfc/rfc9110.html#name-delete) - delete items

`DELETE` asks the server to remove the resource identified by the URL.

For example:

~~~~~~~~
DELETE /todos/1
~~~~~~~~

Means "delete todo `1`."

Deleting a specific item is common. Deleting an entire collection is possible only if the API explicitly supports it, and it is usually protected carefully because it is a high impact action.

A successful `DELETE` often returns `204 No Content` because the resource has been removed and there is nothing else to return. Some APIs return `200 OK` with a response body describing what was deleted.

`DELETE` is considered idempotent because, after the first successful delete, the resource is gone. Sending the same delete again leaves it gone. The second response might be `404 Not Found`, but the final state is still that the resource does not exist.

---

### HTTP DELETE Send Example

~~~~~~~~
curl -X DELETE {{<ORIGIN_URL>}}/lists/{guid} ^
-H "Authorization: Basic YWRtaW46cGFzc3dvcmQ=" ^
--proxy 127.0.0.1:8888
~~~~~~~~

---

### HTTP DELETE Request Example

~~~~~~~~
DELETE {{<ORIGIN_URL>}}/lists/{guid} HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Accept: */*
Connection: Keep-Alive
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
~~~~~~~~

---

### HTTP DELETE Response Example

~~~~~~~~
HTTP/1.1 204 No Content
Date: Thu, 17 Aug 2017 12:20:35 GMT
Content-Type: application/json
Server: Jetty(9.4.4.v20170414)
~~~~~~~~


---

### Common HTTP Status codes in response to a DELETE

- **200** - resource was deleted and a response body was returned
- **202** - delete request was accepted for later processing
- **204** - resource was deleted and no response body was returned
- **404** - target resource was not found
- **401** - authentication is required, see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it
- **405** - method not allowed for this endpoint
- **500** - server error while processing the request


---

## HTTP OPTIONS Verb

- [OPTIONS](https://www.rfc-editor.org/rfc/rfc9110.html#name-options) - shows the verbs available on this url
- returns an `Allow` header describing the allowed HTTP Verbs

`OPTIONS` asks the server which communication options are available for the target URL.

In API testing we most often look for the `Allow` header, which should list the methods the server says are valid for that resource.

For example:

~~~~~~~~
Allow: GET, POST, PUT
~~~~~~~~

This can help us discover supported methods, but we should still test them. An API can have incomplete or misleading `OPTIONS` responses, and proxies or frameworks can add their own behaviour.

---

### HTTP OPTIONS Send Example

~~~~~~~~
curl -X OPTIONS {{<ORIGIN_URL>}}/lists ^
--proxy 127.0.0.1:8888
~~~~~~~~

---

### HTTP OPTIONS Request Example

~~~~~~~~
OPTIONS {{<ORIGIN_URL>}}/lists HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Accept: */*
Connection: Keep-Alive
~~~~~~~~

---

### HTTP OPTIONS Response Example

~~~~~~~~
HTTP/1.1 200 OK
Date: Thu, 17 Aug 2017 12:24:39 GMT
Allow: GET, POST, PUT
Content-Type: text/html;charset=utf-8
Server: Jetty(9.4.4.v20170414)
Content-Length: 0
~~~~~~~~

---

### Common HTTP Status codes in response to an OPTIONS

- **200** - options were returned, often including an `Allow` header
- **204** - options were returned with no response body
- **404** - target url was not found

---

## Summary

HTTP verbs describe the intent of a request. The URL identifies the resource, and the verb tells the server whether the client wants to read, create, replace, update, delete, or inspect what is allowed.

The common REST-style pattern is to use noun-like URLs such as `/todos` and `/todos/1`, then use verbs such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE` to describe the action.

Some verbs are intended to be safe, such as `GET`, `HEAD`, `OPTIONS`, and `QUERY`, because they are used to retrieve information rather than change resources. Some verbs are intended to be idempotent, such as `PUT` and `DELETE`, because sending the same request again should leave the resource in the same final state.

For API testing, each verb gives us different risks to check. We can test that supported verbs do the right thing, unsupported verbs are rejected, status codes match the behaviour, request bodies are interpreted correctly, and server state only changes when the verb and endpoint say it should.
