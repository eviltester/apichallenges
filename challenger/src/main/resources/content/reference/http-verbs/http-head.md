---
title: HTTP HEAD Verb
seo_title: HTTP HEAD Method for REST API Testing
description: Learn how the HTTP HEAD method is commonly used in REST API testing.
lastmod: 2026-08-12
seo_description: Learn how the HTTP HEAD method is commonly used in REST API testing.
showads: true
---

<a id="http-head-verb"></a>
# HTTP HEAD Verb

- [HEAD](https://www.rfc-editor.org/rfc/rfc9110.html#name-head) - request the same headers as GET without a response body
- HEAD is useful for checking whether a resource exists, checking caching headers, or validating metadata without transferring the representation
- A HEAD response should not include a body

`HEAD` is like asking "what would the headers look like if I sent a GET request here?"

The server should send the same response headers that it would send for `GET`, but without sending the response body.

This can be useful when we want to check whether a resource exists, inspect caching headers, check content type, or avoid downloading a large body.

For testing, `HEAD` is useful because some APIs accidentally route it differently from `GET`, or accidentally return a body when they should not.

---

## HTTP HEAD Verb Example

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

## Common HTTP Status codes in response to a HEAD

- **200** - OK, the resource exists
- **204** - OK, no content
- **404** - resource not found
- **405** - method not allowed for this endpoint
