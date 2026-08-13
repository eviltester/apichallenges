---
title: HTTP POST Verb
seo_title: HTTP POST Method for REST API Testing and Requests
description: Learn how the HTTP POST method is commonly used in REST API testing.
lastmod: 2026-08-12
seo_description: Learn how the HTTP POST method submits data to REST APIs, creates resources or actions, and what request and response behaviour to test.
showads: true
---

<a id="http-post-verb"></a>
# HTTP POST Verb

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

## HTTP POST Verb Send Example

~~~~~~~~
curl -X POST {{<ORIGIN_URL>}}/lists ^
-H "accept: application/xml" ^
-H content-type:application/json ^
-H "Authorization: Basic dXNlcjpwYXNzd29yZA==" ^
-d "{title:'a list title'}" ^
--proxy 127.0.0.1:8888
~~~~~~~~

---

## HTTP POST Verb Request Example

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

## HTTP POST Verb Response Example

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

## Common HTTP Status codes in response to a POST

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
