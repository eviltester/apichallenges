---
title: HTTP CONNECT Verb
seo_title: HTTP CONNECT Method for REST API Testing and Requests
description: Learn how the HTTP CONNECT method is commonly used for tunnels and why REST APIs rarely expose it.
lastmod: 2026-08-12
seo_description: Learn how the HTTP CONNECT method starts tunnels through proxies, how to test its behaviour, and why normal REST APIs usually reject it.
showads: true
---

<a id="http-connect-verb"></a>

# HTTP CONNECT Verb

- [CONNECT](https://www.rfc-editor.org/rfc/rfc9110.html#name-connect) - establish a tunnel to the target server
- CONNECT is most often seen when an HTTP client talks to a proxy before using HTTPS
- CONNECT is rarely part of normal REST API resource behaviour

`CONNECT` asks an intermediary, usually a proxy, to open a tunnel to another server.

For API testing, `CONNECT` normally appears when you are testing proxies, gateways, network controls, or client tooling. A normal application API will usually reject `CONNECT` because it does not map neatly to reading or changing a resource.

You may find that you're HTTP Client doesn't support sending `CONNECT` requests because it is normally used to initiate a connection rather than trigger functionality but if you need to send it you can always use `cURL` with `-X CONNECT`:


```
curl -i -X CONNECT https://apichallenges.com/heartbeat
```

## HTTP CONNECT Request Example

~~~~~~~~
CONNECT example.com:443 HTTP/1.1
Host: example.com:443
~~~~~~~~


## How to Test `CONNECT` Request

This is not how we would really test a connection though.

`CONNECT` asks a proxy to create a tunnel, then the client normally starts TLS through that tunnel, then HTTPS happens inside it so that we can then start communicating using HTTPS.

So `CONNECT` is just an initiating action (normally through a proxy).

If we see a success message:

```
HTTP/1.1 200 Connection Established
```

Then it is the follow on requests through that connection that tell us if it worked.

### Normally we expect `CONNECT` to fail

For most APIs you would not expect them to respond with success.

`CONNECT` should normally be unsupported, blocked by the web server/proxy, or return an error status like `405 Method Not Allowed`with an `Allow` header listing the methods the endpoint supports, or `501 Not Implemented`.

A normal JSON/REST API generally has no reason to accept `CONNECT`.

If an API responds with success, it might mean:

- The backend/router has a catch-all handler that treats unknown methods like normal requests.
- The framework automatically routes CONNECT to an endpoint by mistake.

Both of the above are usually unintended coding errors that should be resolved.

The most Risky situation is that `CONNECT` doesn't just respond with success, it actually creates a tunnel which could expose it as an unintended proxy. This would be a security issue.

If accepted, the backend may create a tunnel from the public-facing server into restricted infrastructure.

It could allow attackers to tunnel traffic through your server to reach places they should not be allowed to access e.g.:

- Internal network services
- Cloud metadata endpoints
- Databases or admin panels not exposed publicly
- Third-party targets, making your server look like the source of the traffic
- Ports and hosts that should be blocked from the internet

If a normal API accepts `CONNECT` and tunnels traffic, it may create an open-proxy or SSRF-style risk.

OWASP describes SSRF as a vulnerability where an application can be made to send requests to unexpected destinations, including internal services that are not directly reachable by users. See [OWASP API7:2023 Server Side Request Forgery](https://owasp.org/API-Security/editions/2023/en/0xa7-server-side-request-forgery/) and the [OWASP SSRF Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html).

A normal API should reject CONNECT unless it is explicitly designed to be a proxy. In some instances it is appropriate to prevent `CONNECT` requests ever reaching the application and have them blocked at the edge/load balancer.


---

## Common HTTP Status codes in response to a CONNECT

- **200** - tunnel established
- **400** - invalid CONNECT target or request
- **403** - tunnel request forbidden
- **405** - method not allowed for this endpoint
- **407** - proxy authentication is required
- **501** - method not implemented by the server
