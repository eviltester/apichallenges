---
title: HTTP PATCH Verb
seo_title: HTTP PATCH Method for REST API Testing and Requests
description: Learn how the HTTP PATCH method is commonly used in REST API testing.
lastmod: 2026-08-12
seo_description: Learn how the HTTP PATCH method partially updates REST API resources, how it differs from PUT, and what behaviour to check in tests.
showads: true
---

<a id="toc18"></a>
<a id="http-patch-verb"></a>
# HTTP PATCH Verb

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
PATCH /api/todos/1
~~~~~~~~

Might mean "change only the fields described in this request body."

This is useful when a resource has many fields and the client only wants to update one or two of them.

The important testing question with `PATCH` is "what format are the changes written in?"

Some APIs accept a partial JSON object. Some use [JSON Merge Patch](https://www.rfc-editor.org/rfc/rfc7396). Some use [JSON Patch](https://www.rfc-editor.org/rfc/rfc6902). These formats can look similar at a glance, but they have different rules.

`PATCH` is not automatically idempotent. A patch that says "set title to X" can be idempotent. A patch that says "increment count by 1" is not idempotent because sending it twice changes the result.

If two clients patch the same resource at the same time, one change can accidentally overwrite or conflict with another. APIs can reduce this risk with conditional requests, e.g. using an `ETag` value and an `If-Match` header.

---

## HTTP PATCH Send Example

~~~~~~~~
curl -X PATCH {{<ORIGIN_URL>}}/api/todos/3 ^
-H "Content-Type: application/json" ^
-H "Accept: application/json" ^
-d "{\"title\":\"patched title\"}"
~~~~~~~~

---

## HTTP PATCH Partial JSON Example

~~~~~~~~
PATCH {{<ORIGIN_URL>}}/api/todos/3 HTTP/1.1
User-Agent: rest-client
Host: localhost:4567
Content-Type: application/json
Accept: application/json

{"title":"patched title"}
~~~~~~~~

---

## HTTP PATCH JSON Merge Patch Example

~~~~~~~~
PATCH {{<ORIGIN_URL>}}/api/todos/3 HTTP/1.1
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

## HTTP PATCH JSON Patch Example

~~~~~~~~
PATCH {{<ORIGIN_URL>}}/api/todos/3 HTTP/1.1
User-Agent: rest-client
Host: localhost:4567
Content-Type: application/json-patch+json
Accept: application/json

[{"op":"replace","path":"/title","value":"patched title"}]
~~~~~~~~

---

## Common HTTP Status codes in response to a PATCH

- **200** - OK, resource was updated and a representation was returned
- **204** - OK, resource was updated and no body was returned
- **404** - target resource was not found
- **405** - method not allowed for this endpoint
- **409** - conflict applying the patch
- **415** - unsupported patch document content type
- **422** - patch content was understood but could not be applied


---
