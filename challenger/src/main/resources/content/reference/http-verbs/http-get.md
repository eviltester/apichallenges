---
title: HTTP GET Verb
seo_title: HTTP GET Method for REST API Testing
description: Learn how the HTTP GET method is commonly used in REST API testing.
lastmod: 2026-08-12
seo_description: Learn how the HTTP GET method is commonly used in REST API testing.
showads: true
---

<a id="http-get-verb"></a>
# HTTP GET Verb

- [GET](https://www.rfc-editor.org/rfc/rfc9110.html#name-get) - retrieve data
- GET verbs can be issued by a browser
    - click on link
    - visit a site
- GET `https://apichallenges.eviltester.com/mirror/raw`
- Important Headers
    - User-Agent - tells server app type
    - Accept - what format response you prefer

`GET` is the verb we use most often when we want to read information.

When you type a URL into a browser, click a link, load an image, or fetch data from JavaScript, there is a good chance a `GET` request is being sent.

In a REST-style API, `GET /todos` would usually return a list of todo items, and `GET /todos/1` would usually return one todo item.

`GET` requests should not be used to create, edit, or delete resources. If a `GET` request changes important server state then it can cause problems with browser prefetching, search engine crawlers, caching, retries, and automated checks.

The server often responds differently based on the `User-Agent` and you might have to control the `User-Agent` when testing to trigger mobile behaviour or possibly even bypass controls that restrict automated tooling.

The `Accept` header is often important with `GET` because the client may ask for a preferred response format, e.g. `application/json` or `application/xml`.

---

## HTTP GET Verb Example

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

## Common HTTP Status codes in response to a GET

- **200** - OK, found the url, returned contents
- **301, 307, 308** - content has moved, new url in `location` header
- **404** - url not found
- **401** - authentication is required, see `WWW-Authenticate` header
- **403** - url probably exists but you are not allowed to access it

For a collection URL, a successful `GET` might return an empty list rather than `404`.

For a specific item URL, `404` is common when the id does not match any resource.


---

