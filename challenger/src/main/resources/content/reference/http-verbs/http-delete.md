---
title: HTTP DELETE Verb
seo_title: HTTP DELETE Method for REST API Testing
description: Learn how the HTTP DELETE method is commonly used in REST API testing.
lastmod: 2026-08-12
seo_description: Learn how the HTTP DELETE method is commonly used in REST API testing.
showads: true
---

<a id="http-delete-verb"></a>
# HTTP DELETE Verb

- [DELETE](https://www.rfc-editor.org/rfc/rfc9110.html#name-delete) - delete items

`DELETE` asks the server to remove the resource identified by the URL.

For example:

~~~~~~~~
DELETE /todos/1
~~~~~~~~

Means "delete todo `1`."

Deleting a specific item is common. Deleting an entire collection is possible only if the API explicitly supports it, and it is usually protected carefully because it is a high impact action.

A successful `DELETE` often returns `204 No Content` because the resource has been removed and there is nothing else to return. Some APIs return `200 OK` with a response body describing what was deleted.

`DELETE` is considered idempotent because, after the first successful delete, the resource is gone. Sending the same delete again leaves it gone. The second response might be `404 Not Found`, but the final state is still that the resource does not exist.

---

## HTTP DELETE Send Example

~~~~~~~~
curl -X DELETE {{<ORIGIN_URL>}}/lists/{guid} ^
-H "Authorization: Basic YWRtaW46cGFzc3dvcmQ=" ^
--proxy 127.0.0.1:8888
~~~~~~~~

---

## HTTP DELETE Request Example

~~~~~~~~
DELETE {{<ORIGIN_URL>}}/lists/{guid} HTTP/1.1
User-Agent: curl/7.39.0
Host: localhost:4567
Accept: */*
Connection: Keep-Alive
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
~~~~~~~~

---

## HTTP DELETE Response Example

~~~~~~~~
HTTP/1.1 204 No Content
Date: Thu, 17 Aug 2017 12:20:35 GMT
Content-Type: application/json
Server: Jetty(9.4.4.v20170414)
~~~~~~~~


---

## Common HTTP Status codes in response to a DELETE

- **200** - resource was deleted and a response body was returned
- **202** - delete request was accepted for later processing
- **204** - resource was deleted and no response body was returned
- **404** - target resource was not found
- **401** - authentication is required, see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it
- **405** - method not allowed for this endpoint
- **500** - server error while processing the request


---

