---
title: HTTP CONNECT Verb
seo_title: HTTP CONNECT Method for REST API Testing
description: Learn how the HTTP CONNECT method is commonly used for tunnels and why REST APIs rarely expose it.
lastmod: 2026-08-12
seo_description: Learn how the HTTP CONNECT method is commonly used for tunnels and why REST APIs rarely expose it.
showads: true
---

<a id="http-connect-verb"></a>

# HTTP CONNECT Verb

- [CONNECT](https://www.rfc-editor.org/rfc/rfc9110.html#name-connect) - establish a tunnel to the target server
- CONNECT is most often seen when an HTTP client talks to a proxy before using HTTPS
- CONNECT is rarely part of normal REST API resource behaviour

`CONNECT` asks an intermediary, usually a proxy, to open a tunnel to another server.

For API testing, `CONNECT` normally appears when you are testing proxies, gateways, network controls, or client tooling. A normal application API will usually reject `CONNECT` because it does not map neatly to reading or changing a resource.

---

## HTTP CONNECT Request Example

~~~~~~~~
CONNECT example.com:443 HTTP/1.1
Host: example.com:443
~~~~~~~~

---

## Common HTTP Status codes in response to a CONNECT

- **200** - tunnel established
- **400** - invalid CONNECT target or request
- **403** - tunnel request forbidden
- **405** - method not allowed for this endpoint
- **407** - proxy authentication is required
- **501** - method not implemented by the server

