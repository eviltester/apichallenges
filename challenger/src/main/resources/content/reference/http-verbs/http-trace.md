---
title: HTTP TRACE Verb
seo_title: HTTP TRACE Method for REST API Testing
description: Learn how the HTTP TRACE method is commonly used and why APIs often disable it.
lastmod: 2026-08-12
seo_description: Learn how the HTTP TRACE method is commonly used and why APIs often disable it.
showads: true
---

<a id="http-trace-verb"></a>

# HTTP TRACE Verb

- [TRACE](https://www.rfc-editor.org/rfc/rfc9110.html#name-trace) - perform a message loop-back test along the request path
- TRACE is mainly a diagnostic method
- TRACE is rarely enabled for application APIs and is often blocked by servers, gateways, or security policy

`TRACE` asks the server to send back the request message it received.

For API testing, `TRACE` is useful mostly as a discovery and security check. Many APIs do not implement it, and that is usually expected. API Challenges includes `TRACE /heartbeat` so you can practise checking unsupported or unimplemented methods.

---

## HTTP TRACE Send Example

~~~~~~~~
curl -X TRACE {{<ORIGIN_URL>}}/heartbeat
~~~~~~~~

---

## HTTP TRACE Request Example

~~~~~~~~
TRACE {{<ORIGIN_URL>}}/heartbeat HTTP/1.1
User-Agent: curl/8.0.0
Host: localhost:4567
Accept: */*
~~~~~~~~

---

## Common HTTP Status codes in response to a TRACE

- **200** - OK, the server performed the message loop-back
- **405** - method not allowed for this endpoint
- **501** - method not implemented by the server

