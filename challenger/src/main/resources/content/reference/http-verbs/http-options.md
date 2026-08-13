---
title: HTTP OPTIONS Verb
seo_title: HTTP OPTIONS Method for REST API Testing and Requests
description: Learn how the HTTP OPTIONS method is commonly used in REST API testing.
lastmod: 2026-08-12
seo_description: Learn how the HTTP OPTIONS method advertises supported methods, how Allow headers work, and what to check when testing REST API endpoints.
showads: true
---

<a id="http-options-verb"></a>
# HTTP OPTIONS Verb

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

## HTTP OPTIONS Send Example

~~~~~~~~
curl -X OPTIONS {{<ORIGIN_URL>}}/lists ^
--proxy 127.0.0.1:8888
~~~~~~~~

---

## HTTP OPTIONS Request Example

~~~~~~~~
OPTIONS {{<ORIGIN_URL>}}/lists HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Accept: */*
Connection: Keep-Alive
~~~~~~~~

---

## HTTP OPTIONS Response Example

~~~~~~~~
HTTP/1.1 200 OK
Date: Thu, 17 Aug 2017 12:24:39 GMT
Allow: GET, POST, PUT
Content-Type: text/html;charset=utf-8
Server: Jetty(9.4.4.v20170414)
Content-Length: 0
~~~~~~~~

---

## Common HTTP Status codes in response to an OPTIONS

- **200** - options were returned, often including an `Allow` header
- **204** - options were returned with no response body
- **404** - target url was not found

---
