---
title: HTTP TRACE Verb
seo_title: HTTP TRACE Method for REST API Testing
description: Learn how the HTTP TRACE method is commonly used, how to test it, and why APIs often disable it.
lastmod: 2026-08-13
seo_description: Learn how the HTTP TRACE method is commonly used, how to test it, and why APIs often disable it.
showads: true
---

<a id="http-trace-verb"></a>

# HTTP TRACE Verb

- [TRACE](https://www.rfc-editor.org/rfc/rfc9110.html#name-trace) - perform a message loop-back test along the request path
- TRACE is mainly a diagnostic method
- TRACE is rarely enabled for application APIs and is often blocked by servers, gateways, or security policy

`TRACE` asks the server to send back, or loop back, the request message it received.

For API testing, `TRACE` is useful mostly as a discovery and security check. Many APIs do not implement it, and that is usually expected. API Challenges includes `TRACE /heartbeat` so you can practise checking unsupported or unimplemented methods.

Unlike `CONNECT`, `TRACE` does not establish a tunnel. It is a diagnostic request. If it succeeds, the important evidence is the echoed request in the response body.

---

## HTTP TRACE Send Example

~~~~~~~~
curl -i -v -X TRACE {{<ORIGIN_URL>}}/heartbeat
~~~~~~~~

Some browser-based HTTP clients cannot send `TRACE` requests. If your API client does not support `TRACE`, use `cURL` with `-X TRACE` or use a dedicated testing proxy/tool that allows uncommon methods.

When testing `TRACE`, avoid sending real authentication tokens, session cookies, API keys, or other secrets unless you are working in a controlled security test. A successful `TRACE` response may echo the request headers back to you.

---

## HTTP TRACE Request Example

~~~~~~~~
TRACE {{<ORIGIN_URL>}}/heartbeat HTTP/1.1
User-Agent: curl/8.0.0
Host: localhost:4567
Accept: */*
~~~~~~~~

---

## How to Test `TRACE` Request

`TRACE` is not normally a functional API test.

`TRACE` is normally used to answer questions like:

- Does the server, gateway, or load balancer allow `TRACE` at all?
- Does the response echo the request line and headers?
- Are any sensitive headers reflected in the response body?
- Is the method blocked consistently across HTTP and HTTPS?
- Does a `POST` using a `X-HTTP-Method-Override: TRACE` header unexpectedly allow `TRACE` behaviour?

You can add a harmless diagnostic header to make the loop-back easier to see:

~~~~~~~~
curl -i -v -X TRACE {{<ORIGIN_URL>}}/heartbeat ^
  -H "X-Trace-Test: apichallenges"
~~~~~~~~

If the server performs the loop-back, the response body may contain the request line and headers it received, including the `X-Trace-Test` header.

For API Challenges, `TRACE /heartbeat` is deliberately used as an unsupported-method challenge and returns `501 Not Implemented`.

### Normally we expect `TRACE` to fail

For most APIs you would not expect `TRACE` to respond with success.

`TRACE` should normally be unsupported, blocked by the web server/proxy, or return an error status like `405 Method Not Allowed` with an `Allow` header listing the methods the endpoint supports, or `501 Not Implemented`.

A normal JSON/REST API generally has no reason to echo requests back to callers.

If an API responds with success, it might mean:

- The backend/router has a catch-all handler that treats unknown methods like normal requests.
- The framework automatically routes `TRACE` to an endpoint by mistake.
- A web server, proxy, gateway, or load balancer has diagnostic `TRACE` behaviour enabled.

A `200 OK` response with a normal JSON API body usually points to loose method routing. A `200 OK` response that echoes the request line and headers means `TRACE` loop-back behaviour is enabled.

Both are usually unintended for a public API and should be reviewed.

### Security Considerations

The main concern with `TRACE` is that it reflects request data back to the client. Historically, this was associated with Cross-Site Tracing (XST), where `TRACE` could be combined with browser or cross-domain weaknesses to expose headers such as cookies or authentication data.

Modern browsers commonly block script-based `TRACE` requests, which reduces the old XST attack.

OWASP describes Cross-Site Tracing as involving the `TRACE` or `TRACK` methods, and the OWASP Web Security Testing Guide includes testing for HTTP methods and XST. See [OWASP Cross Site Tracing](https://owasp.org/www-community/attacks/Cross_Site_Tracing) and [OWASP WSTG: Test HTTP Methods](https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/02-Configuration_and_Deployment_Management_Testing/06-Test_HTTP_Methods).

A normal API should reject `TRACE` unless it is explicitly required for diagnostic use. In many deployments it is appropriate to prevent `TRACE` requests from reaching the application and have them blocked at the edge/load balancer.

---

## Common HTTP Status codes in response to a TRACE

- **200** - OK, the server performed the message loop-back
- **405** - method not allowed for this endpoint
- **501** - method not implemented by the server
