---
title: HTTP Verbs - Tutorial
seo_title: Tutorial: HTTP Verbs Tutorial | API Challenges
description: Basic HTTP Verbs and Methods tutorial what they do and how to use them.
lastmod: 2026-07-30
seo_description: Learn HTTP Verbs with practical examples and clear guidance you can apply immediately when creating requests, analyzing responses, and testing APIs.
showads: true
---

# HTTP Verbs Overview

---

## HTTP GET Verb

- [GET](https://www.rfc-editor.org/rfc/rfc9110.html#name-get) - retrieve data
- GET verbs can be issued by a browser
    - click on link
    - visit a site
- GET `http://compendiumdev.co.uk/apps/api/mock/reflect`
- Important Headers
    - User-Agent - tells server app type
    - Accept - what format response you prefer


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
- **401** - you need to give me authorisation details see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it


---

## HTTP HEAD Verb

- [HEAD](https://www.rfc-editor.org/rfc/rfc9110.html#name-head) - request the same headers as GET without a response body
- HEAD is useful for checking whether a resource exists, checking caching headers, or validating metadata without transferring the representation
- A HEAD response should not include a body

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

- send a 'body' format of content in the 'content-type' header
- usually used to create or amend data
- browser will usually send a POST request when submitting a form

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

- **200** - OK, did whatever I was supposed to
- **201** - OK created new items
- **202** - OK, I'll do that later
- **204** - OK, I have no more information to give you
- **400** - what? that request made no sense
- **404** - I can't post to that url it is not found
- **401** - need authorisation see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it
- **409** - can't do that, already exists
- **500** - your request made me crash


---

## HTTP PUT Verb

- [PUT](https://www.rfc-editor.org/rfc/rfc9110.html#name-put) - create or replace from full information

Full information means it should be idempotent - send it again and get exactly the same request

Demo

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

## HTTP PATCH Verb

- [PATCH](https://www.rfc-editor.org/rfc/rfc5789) - apply a set of changes to an existing resource
- PATCH is often used when a client wants to update selected fields without replacing the whole resource
- Servers can advertise supported PATCH request content types with `Accept-Patch`
- Common JSON PATCH styles:
    - [JSON Merge Patch](https://www.rfc-editor.org/rfc/rfc7396) with `Content-Type: application/merge-patch+json`
    - [JSON Patch](https://www.rfc-editor.org/rfc/rfc6902) with `Content-Type: application/json-patch+json`
- Some APIs also support partial JSON object updates with `Content-Type: application/json`

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

Demo

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

- **200** - OK, did whatever I was supposed to
- **202** - OK, I'll do that later
- **204** - OK, I have no more information to give you
- **404** - I can't post to that url it is not found
- **401** - you need to give me authorisation details see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it
- **500** - your request made me crash


---

## HTTP OPTIONS Verb

- [OPTIONS](https://www.rfc-editor.org/rfc/rfc9110.html#name-options) - shows the verbs available on this url
- returns an `Allow` header describing the allowed HTTP Verbs

---

### HTTP OPTIONS Send Example

~~~~~~~~
curl -X OPTIONS {{<ORIGIN_URL>}}/lists ^
--proxy 127.0.0.1:8888
~~~~~~~~

Demo

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

### Common HTTP Status codes in response to a OPTIONS

- **200** - OK, did whatever I was supposed to
- **404** - I can't post to that url it is not found

