---
title: HTTP PUT Verb
seo_title: HTTP PUT Method for REST API Testing
description: Learn how the HTTP PUT method is commonly used in REST API testing.
lastmod: 2026-08-12
seo_description: Learn how the HTTP PUT method is commonly used in REST API testing.
showads: true
---

<a id="http-put-verb"></a>
# HTTP PUT Verb

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

## HTTP PUT Send Example

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

## HTTP PUT Request Example

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

## HTTP PUT Response Example

~~~~~~~~
HTTP/1.1 201 Created
Date: Thu, 17 Aug 2017 13:41:46 GMT
Content-Type: application/json
Server: Jetty(9.4.4.v20170414)
Content-Length: 0
~~~~~~~~

---

## Common HTTP Status codes in response to a PUT

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

