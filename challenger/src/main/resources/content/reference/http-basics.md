---
title: HTTP Basics for API Testing
seo_title: HTTP Basics for API Testing: Requests, Responses, Headers
description: Basic HTTP tutorial and overview of key HTTP terminology and status codes.
lastmod: 2026-08-04
seo_description: Learn HTTP Basics with practical examples and clear guidance you can apply immediately when creating requests, analyzing responses, and testing APIs.
showads: true
---

# HTTP Basics for API Testing

This tutorial introduces the core HTTP concepts you need when testing Web Applications and APIs. We will look at how requests are built, how responses report success or failure, what common verbs and status codes mean, how JSON and XML bodies are represented, and how URLs, query strings, and headers influence the way a server processes a request.

---

## Overview of Section - HTTP Requests and Responses

- HTTP Verbs - GET, POST, DELETE
- Headers
- Responses
    - Status Codes - e.g. 200, 404, 500
- This is the foundation for most web, HTTP, REST testing and automating.

HTTP is the message format used by browsers, API clients, proxies, servers, and many test tools when they communicate over the web.

If you can read an HTTP request and an HTTP response then you can understand a lot of what is happening in a Web Application or API.

You do not need to memorise every header or every status code. The useful skill is learning how the message is structured, knowing which parts are important, and knowing where to look up the details when you need them.


---

## `cURL` vs Browsers

The most common way we've seen of sending HTTP requests is by using a Web Browser, but you're basically limited to `GET` requests and then the requests that the application send to the server.

If we want to send custom requests then we need to use a different tool e.g. `cURL` or [one of the tools from the tool list](/tools/clients).

`cURL` is a command line tool for issuing HTTP Requests and it is installed by default on most operating systems. Although Windows has a custom version of `cURL` that uses slightly different commands so you might want to install `cURL` from the official site, and then run it using `curl.exe`

- [cURL official site id curl.se](https://curl.se/)


---

## HTTP GET Request sent from `cURL`

Command:

~~~~~~~~
curl {{<ORIGIN_URL>}}/heartbeat ^
-H "accept: application/xml" ^
--proxy 127.0.0.1:8888
~~~~~~~~

The command above uses `cURL` to send a request to the `/heartbeat` endpoint.

By default, `cURL` sends a `GET` request when you provide a URL and no other command to use a different verb.

The `-H` option adds a header to the request. In this example we are asking the server to send back `application/xml` if it can.

The `--proxy` option routes the request through a proxy running on `127.0.0.1:8888`. We often do this when testing so that we can inspect the raw HTTP messages in a proxy tool.

---

This command sends a GET Request:

~~~~~~~~
GET {{<ORIGIN_URL>}}/heartbeat HTTP/1.1
User-Agent: curl/7.39.0
Host: {{<HOST_URL>}}
Connection: Keep-Alive
accept: application/xml
~~~~~~~~

A raw HTTP request is mostly a text message with a standard layout.

The first line is the request line:

~~~~~~~~
GET {{<ORIGIN_URL>}}/heartbeat HTTP/1.1
~~~~~~~~

This tells us:

- `GET` is the HTTP verb, or method
- `{{<ORIGIN_URL>}}/heartbeat` is the resource being requested
- `HTTP/1.1` is the HTTP version being used

After the request line we see the headers:

- `User-Agent` describes the client making the request
- `Host` identifies the host the request is being sent to
- `Connection` gives connection handling information
- `accept` asks for a preferred response format

In testing, this gives us several places to investigate problems. The URL might be wrong, the verb might be wrong, the `Host` might be different from what we expected, or the `Accept` header might be asking for a response format that the server does not support.

---

## HTTP Response to GET /heartbeat request

~~~~~~~~
HTTP/1.1 204 No Content
Date: Thu, 17 Aug 2017 10:34:32 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Server: Jetty(9.4.4.v20170414)
~~~~~~~~

An HTTP response has a similar structure to a request.

The first line is the status line:

~~~~~~~~
HTTP/1.1 204 No Content
~~~~~~~~

This tells us:

- `HTTP/1.1` is the HTTP version used for the response
- `204` is the status code
- `No Content` is the reason phrase, a short human-readable description

The status code is the first part that your automation will usually assert on. The reason phrase is useful when reading, but it is not a reliable thing to automate against because some servers customise it.

The headers describe the response:

- `Date` tells us when the response was produced
- `Content-Type` tells us the media type of the body, when there is a body
- `Transfer-Encoding` describes how the message body is transferred
- `Server` describes the server software

For `/heartbeat`, a `204` status code means the server handled the request successfully and has no response body to send back.

If the same request is sent with `accept: application/xml`, a server that supports XML might respond with an XML `Content-Type`. If it cannot satisfy the requested format then the response may be different, for example `406 Not Acceptable`.

We need to learn to read both response and request. To make sure we are sending valid requests, and make sure that the response is accurate e.g. sometimes we might see a response with a `Content-Length: 7243` (meaning 7234 bytes of content) but actually no content was added. Testing has to be able to spot this type of issue.

---

## Raw HTTP Requests and Responses

We need to be able to read raw HTTP requests and responses, but we rarely have to create them by hand.

Most of the time a browser, `cURL`, Postman, Insomnia, Rest Assured, Playwright, or another client library will create the raw message for us. Our job is to understand what was sent and what came back.

When you see a header you do not recognise, look it up. Some headers are used by browsers, some are for proxies, some are for caching, some are for the application, and some are simply descriptive.

Useful references:

- [MDN HTTP headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers)
- [HTTP field name registry](https://www.iana.org/assignments/http-fields/http-fields.xhtml)
- [List of HTTP header fields](https://en.wikipedia.org/wiki/List_of_HTTP_header_fields)

When testing, pay attention to headers that affect behaviour:

- `Accept` can change the response format
- `Content-Type` can change how the server parses the request body
- `Authorization` can change what the client is allowed to do
- `Cookie` can identify a browser session
- custom headers can switch on application-specific features

---

## Basic HTTP Verbs

The HTTP verb, or method, tells the server what type of action the client wants to perform.

The verb does not do the work by itself. The application code on the server decides what happens. But the verb gives us a shared convention, and API documentation will usually describe behaviour in terms of verb plus URL.

- [GET](https://www.rfc-editor.org/rfc/rfc9110.html#name-get) - retrieve a representation of a resource
- [HEAD](https://www.rfc-editor.org/rfc/rfc9110.html#name-head) - retrieve the headers from a resource representation - often used to check if a resource exists
- [POST](https://www.rfc-editor.org/rfc/rfc9110.html#name-post) - send information for the server to process, often to create or amend something
- [PUT](https://www.rfc-editor.org/rfc/rfc9110.html#name-put) - create or replace a resource using the supplied representation
- [DELETE](https://www.rfc-editor.org/rfc/rfc9110.html#name-delete) - request that a resource is deleted
- [OPTIONS](https://www.rfc-editor.org/rfc/rfc9110.html#name-options) - ask what communication options are available for a resource

Additional verbs:

- [PATCH](https://www.rfc-editor.org/info/rfc5789) - partially update a resource
   - Standard formats for `PATCH` body are available:
      - [JSON Patch](https://www.rfc-editor.org/info/rfc6902)
      - [JSON Merge Patch](https://www.rfc-editor.org/info/rfc7396/)
- [QUERY](https://www.rfc-editor.org/info/rfc10008/) - retrieve a resource based on the query defined in the body

For example, these two requests use the same URL but have different intent:

~~~~~~~~
GET /todos/1 HTTP/1.1
DELETE /todos/1 HTTP/1.1
~~~~~~~~

The first request asks to retrieve the todo item. The second asks to delete it.

This is why API tests need to check the verb as well as the URL. A route that works with `GET` might correctly reject `DELETE`, and a route that allows `DELETE` probably needs authentication or authorisation checks.

---

## References

HTTP has formal standards and many approachable learning resources.

When you are learning, the approachable resources help you move quickly. When you are testing a real system and reporting a defect, the standards help you explain what you observed in a precise way.

- [HTTP Semantics standard](https://www.rfc-editor.org/rfc/rfc9110) is the main reference for what HTTP methods, status codes, fields, and message semantics mean.
- [MDN HTTP documentation](https://developer.mozilla.org/en-US/docs/Web/HTTP) is a practical reference when you want examples and browser-oriented explanations.
- [HTTP status code registry](https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml) lists the officially registered status codes.
- [W3C RFC 2616 section 9](https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html) is an older HTTP/1.1 reference. You may still see it linked from older articles, but the newer RFCs should be preferred for current work.
- [HTTP Verbs](/reference/http-verbs) explains common method conventions with examples from API testing.

In practice, use these references to support your testing language.

Instead of saying "this endpoint is broken", you can say "I sent a `PUT` request with a full replacement representation and observed a `200` response, but a subsequent `GET` showed the existing fields were not replaced." That is a much clearer defect report.

---

## HTTP Status Codes

Every HTTP response starts with a status code.

The status code is a three digit number that gives the client a summary of the result. It does not tell the whole story, but it is the first thing we usually look at.

- `1xx` Informational - the server has received the request and the conversation is continuing
- `2xx` Success - the request was understood and accepted
- `3xx` Redirection - the client needs to take another action, often by following a different URL
- `4xx` Client Error - the request has a problem from the server's point of view
- `5xx` Server Error - the server failed while trying to handle a request

Status codes are not the same as test results.

A `404` can be correct if you are testing that a missing resource is reported as missing. A `500` is usually a sign that the server crashed or failed internally, but even then you still need to read the response body and logs to understand the cause.


---

## Common HTTP Status Codes

| **Status Code** | **Common Meaning** | **Testing Note** |
|-----------------|--------------------|------------------|
| 200 OK | The request succeeded. | Common for successful `GET`, `POST`, `PUT`, and `PATCH` requests. |
| 201 Created | A new resource was created. | Often returned from `POST` requests. Check for a `Location` header when the API documents one. |
| 204 No Content | The request succeeded and there is no body. | Common for `DELETE` and heartbeat endpoints. Do not expect a JSON or XML response body. |
| 301 Moved Permanently | The resource has a new permanent URL. | Clients may cache this. Tests should check the `Location` header. |
| 307 Temporary Redirect | The client should repeat the request at another URL using the same method. | Useful to know because the method should not change from `POST` to `GET`. |
| 400 Bad Request | The server could not understand or accept the request. | Often caused by malformed JSON, invalid query parameters, or missing required data. |
| 401 Unauthorized | Authentication is required or failed. | Despite the name, this usually means "not authenticated". |
| 403 Forbidden | The client is authenticated but not allowed to perform the action. | This is commonly an authorisation failure. |
| 404 Not Found | The resource was not found. | Can be correct behaviour when requesting an unknown id. |
| 405 Method Not Allowed | The URL exists but does not support this verb. | Check whether the `Allow` header documents valid methods. |
| 409 Conflict | The request conflicts with current server state. | Common when updating stale data or creating something that already exists. |
| 415 Unsupported Media Type | The server does not support the request `Content-Type`. | Useful when testing JSON, XML, form data, or invalid content types. |
| 422 Unprocessable Content | The request body is syntactically valid but fails validation. | Common for business rule or field validation failures. |
| 500 Internal Server Error | The server failed unexpectedly. | Usually worth investigating because client input should not crash the server. |
| 501 Not Implemented | The server does not support the functionality needed for the request. | Often seen when a method is recognised but not implemented. |
| 502 Bad Gateway | A gateway or proxy received an invalid response from an upstream server. | Common in distributed systems when one service depends on another. |
| 503 Service Unavailable | The server is temporarily unable to handle the request. | Often caused by overload, maintenance, or a dependency being unavailable. |
| 504 Gateway Timeout | A gateway or proxy did not receive a response in time. | Useful for testing timeout and retry behaviour. |

The exact status code to expect should come from the API documentation, the product requirements, or the behaviour agreed by the team.

When those sources disagree, standards and observed behaviour help you have a more precise conversation.

---

## HTTP Status code references

Status code references are useful when you want to quickly check the meaning of a code, but the application documentation should still be your main source for expected behaviour.

- [HTTP Semantics - Status Codes](https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes)
- [MDN HTTP response status codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status)
- [IANA HTTP status code registry](https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml)
- [httpstatuses.com](https://httpstatuses.com/)

The standard explains what the code means generally. The application documentation explains what the code means for your system.

---

## HTTP Message Body Format - JSON

- JSON - JavaScript Object Notation
- a common format for sending structured data
- very common in REST APIs
- can be used in request bodies and response bodies
- can be validated with JSON Schema

JSON looks like JavaScript object syntax, but you will see it used from many programming languages.

An API might return JSON when you send:

~~~~~~~~
Accept: application/json
~~~~~~~~

And an API might expect JSON in the request body when you send:

~~~~~~~~
Content-Type: application/json
~~~~~~~~

Useful references:

- [JSON](https://www.json.org/)
- [JSON on MDN](https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Scripting/JSON)
- [JSON Schema](https://json-schema.org/)

---

### JSON Example Explained

~~~~~~~~
{
 "lists":
  [
   {
    "guid":"f8134dd6-a573-4cf5-a6c6-9d556118ed0b",
    "title":"a list title",
    "description":"",
    "createdDate":"2017-08-17-13-11-12",
    "amendedDate":"2017-08-17-13-11-12"
   }
  ]
}
~~~~~~~~

This JSON starts with an object. We can tell because the first non-whitespace character is `{`.

Inside that object is a field called `lists`.

The value of `lists` is an array. We can tell because it starts with `[`.

The array contains one object with these fields:

- `guid`
- `title`
- `description`
- `createdDate`
- `amendedDate`

In this example all the field values are strings.

When testing JSON responses, we often check:

- the response has the expected fields
- the values have the expected types
- the array has the expected number of items
- required fields are present
- unexpected fields are not present, if the API contract says they should not be returned

---

## HTTP Message Body Format - XML

- XML - Extensible Markup Language
- another common format for structured data
- uses elements and tags rather than braces and brackets
- can be used in request bodies and response bodies
- can be validated against an XML schema

XML is less common than JSON in many modern REST APIs, but it is still used in many systems.

An API might return XML when you send:

~~~~~~~~
Accept: application/xml
~~~~~~~~

And an API might expect XML in the request body when you send:

~~~~~~~~
Content-Type: application/xml
~~~~~~~~

Useful references:

- [XML on MDN](https://developer.mozilla.org/en-US/docs/Web/XML/Guides/XML_introduction)
- [XML Schema](https://www.w3.org/XML/Schema)

---

## XML Example Explained

~~~~~~~~
<?xml version="1.0" encoding="UTF-8" ?>
<lists>
  <list>
    <guid>f8134dd6-a573-4cf5-a6c6-9d556118ed0b</guid>
    <title>a list title</title>
    <description></description>
    <createdDate>2017-08-17-13-11-12</createdDate>
    <amendedDate>2017-08-17-13-11-12</amendedDate>
  </list>
</lists>
~~~~~~~~

This XML has a root element called `lists`.

Inside `lists` is a `list` element.

Inside `list` are nested elements:

- `guid`
- `title`
- `description`
- `createdDate`
- `amendedDate`

The opening and closing tags describe the structure:

~~~~~~~~
<title>a list title</title>
~~~~~~~~

The value is the text between the opening tag and the closing tag.

When testing XML responses, we often check the same concepts as JSON, but with XML tooling:

- the expected elements exist
- required elements are present
- element values are correct
- the document is valid against a schema, if a schema is part of the contract

---

## URI - Uniform Resource Identifier

`scheme:[//[user[:password]@]host[:port]][/path][?query][#fragment]`

- `https://apichallenges.eviltester.com/mirror/raw`
    - scheme = `https`
    - host = `apichallenges.eviltester.com`
    - path = `mirror/raw`

A URI identifies a resource.

For day to day API testing, the resource is usually something the server can route to:

- a collection such as `/todos`
- a specific item such as `/todos/1`
- an action-like endpoint such as `/heartbeat`

The path matters because it is part of how the application decides which code should handle the request.

A URL is a URI.

[wikipedia.org/wiki/Uniform_Resource_Identifier](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier)

---

### URI vs URL vs URN

- URI - Uniform Resource Identifier
    - generic representation - might not include the scheme
    - `https://apichallenges.eviltester.com/mirror/raw`
    - `apichallenges.eviltester.com/mirror/raw`
    - `/mirror/raw`
- URL - Uniform Resource Locator
    - `https://apichallenges.eviltester.com/mirror/raw`
    - defines how to locate the identified resource
- URN - [Uniform Resource Name](https://en.wikipedia.org/wiki/Uniform_Resource_Name)
    - not often used - uses scheme `urn`

---

### Scheme(s)

The scheme appears at the start of a URI and describes how the resource should be accessed or interpreted.

- http
- https
- ftp
- mailto
- file

For API testing, `http` and `https` are the schemes we usually use.

The `https` scheme means HTTP is being sent over a secure TLS connection. This is why a browser shows a secure connection for `https://` URLs.

---

### Query Strings

~~~~~~~~
GET /lists/{guid}?without=title,description
GET {{<ORIGIN_URL>}}/lists/f13?without=title,description
~~~~~~~~

Query String:

~~~~~~~~
?without=title,description
~~~~~~~~

A query string is the part of the URL after the `?`.

It is commonly used to send small pieces of information that affect what the server returns.

For example:

~~~~~~~~
?without=title,description
~~~~~~~~

This might mean "return the list but leave out the `title` and `description` fields", depending on how the application has implemented this endpoint.

Query string parameters are commonly written as `name=value` pairs:

~~~~~~~~
?status=active&sort=title
~~~~~~~~

The first parameter starts after the `?`. Additional parameters are separated with `&`.

---

### More About Query Strings

~~~~~~~~
GET /lists/{guid}?without=title,description
~~~~~~~~

Query strings are conventions that the application has to parse.

HTTP defines the general structure of the URI, but your application decides what `without`, `status`, `sort`, `limit`, or `offset` actually mean.

Query strings can be used with any verb, but they are most commonly seen with `GET` requests because a `GET` request normally does not have a request body.

Common query string uses include:

- filtering, e.g. `?doneStatus=true`
- sorting, e.g. `?sort=title`
- pagination, e.g. `?limit=10&offset=20`
- field selection, e.g. `?without=description`

If a query value contains spaces or reserved characters then it may need URL encoding. For example, a space is often encoded as `%20`.

[en.wikipedia.org/wiki/Query_string](https://en.wikipedia.org/wiki/Query_string)

---

## HTTP Standards?

HTTP is defined in RFC documents.

You do not need to read them from start to finish before you can test an API. They are most useful when you need a precise definition or when a team is disagreeing about expected behaviour.

- RFC 9110 [HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110)
- RFC 9112 [HTTP/1.1](https://www.rfc-editor.org/rfc/rfc9112)
- RFC 7230 [(HTTP/1.1): Message Syntax and Routing](https://tools.ietf.org/html/rfc7230) - older reference you may still see linked

---

## How to test with this information

HTTP knowledge helps us test below the visible user interface.

For example, in a browser you might only see "something went wrong". In the Network tab, a proxy, or an API client, you might see:

- the request was sent to the wrong URL
- the request used the wrong verb
- the `Content-Type` was missing
- the server returned `401` because the authentication token was missing
- the server returned `500` when it should have returned a validation error

When testing with HTTP:

- read the API documentation for the expected verb, URL, headers, body, and status code
- send a valid request and check the success response
- vary one important part of the request at a time
- check invalid methods, missing headers, malformed bodies, unsupported media types, and unauthorised access
- read standards for verbs and status codes when you need to explain an observation precisely

Projects often argue about interpretations. Some of the standards are exact enough that it is possible to say "I observed X and it does not match the standard." Include links and short quotes from standards when that helps your report.

---

## HTTP Headers

- Headers are `Key: value` pair attributes in the request
- Headers are metadata, not the message body
- Headers help the sender describe the message
- Headers help the receiver decide how to process the message

Headers appear after the request line or status line.

For example:

~~~~~~~~
Accept: application/xml
Content-Type: application/json
Authorization: Basic dXNlcjpwYXNzd29yZA==
~~~~~~~~

Header names are case-insensitive, so `Accept`, `accept`, and `ACCEPT` refer to the same header field.

A `GET` request with an `Accept: application/xml` header is asking the server to respond with XML if it can.

A `POST` request with a `Content-Type: application/json` header is telling the server that the body of the request should be parsed as JSON.

Headers are a common source of API testing issues because missing or incorrect headers can completely change how the server handles the request.

---

### User-Agent Header

The `User-Agent` header describes the software making the request.

Browsers usually send long `User-Agent` values. API clients and automated tests often send shorter ones, or sometimes none at all.

~~~~~~~~
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
AppleWebKit/537.36 (KHTML, like Gecko)
Chrome/60.0.3112.90 Safari/537.36
~~~~~~~~

Some servers use the `User-Agent` to vary behaviour, block requests, record analytics, or work around browser differences.

As a tester, it is worth knowing whether an API behaves differently when called from a browser, from `cURL`, or from your automation library.

---

### Accept Header

The `Accept` header tells the server which response formats the client is willing to receive.

~~~~~~~~
Accept: text/html,application/xhtml+xml,application/xml;
q=0.9,image/webp,image/apng,*/*;q=0.8
~~~~~~~~

Common values:

- `text/html`
- `application/json`
- `application/xml`

If a request says:

~~~~~~~~
Accept: application/xml
~~~~~~~~

Then the client is asking for XML.

If the API supports XML for that endpoint, it may return:

~~~~~~~~
Content-Type: application/xml
~~~~~~~~

If the API does not support XML, it may ignore the preference, return a default format, or return an error such as `406 Not Acceptable`. The expected behaviour should be defined by the API contract.

The `q=` values in longer `Accept` headers are quality values. They let a client express preferences when it can accept several formats.

---

### Content-Type Header

The `Content-Type` header describes the format of the message body being sent.

For example:

~~~~~~~~
Content-Type: application/json
~~~~~~~~

This tells the server to parse the request body as JSON.

Common values include:

- `application/json`
- `application/xml`
- `text/plain`
- `application/x-www-form-urlencoded`
- `multipart/form-data`

`Accept` and `Content-Type` are easy to confuse:

- `Accept` means "what response formats I can accept"
- `Content-Type` means "what format I am sending"

For a `POST` request that sends JSON and expects JSON back, you might use both:

~~~~~~~~
Accept: application/json
Content-Type: application/json
~~~~~~~~

---

### Basic Auth Header

An application might use Basic Authentication to control access to an API.

Basic Authentication uses the `Authorization` header.

e.g. `Authorization: Basic dXNlcjpwYXNzd29yZA==`

`dXNlcjpwYXNzd29yZA==` is base64 encoded "user:password"

This is not encryption. It is just an encoding of the username and password. Basic Authentication should be used over `https` so that the connection itself is encrypted.

In `cURL` you can add the header yourself, or use `cURL`'s authentication options.

In tools like Postman and Insomnia, you will usually use the Authorization or Auth tab and let the tool build the header.

When testing authentication:

- check a valid username and password
- check missing credentials
- check invalid credentials
- check whether the same endpoint behaves differently when authenticated and unauthenticated

see [base64decode.org](https://www.base64decode.org)

---

## Practise This Concept

HTTP basics are easiest to learn when you keep looking at the actual request and response. These pages give you more chances to inspect the message, vary one part, and explain the result:

- [REST API Tutorial](/tutorials/rest-api-tutorial) lets you see headers, status codes, JSON, XML, and auth in live examples.
- [HTTP Methods and Verbs](/reference/http-verbs) narrows the focus to what changes when the method changes.
- [How to Test REST APIs](/tutorials/rest-api-testing) shows how to turn HTTP evidence into useful test notes and assertions.
- [Simulation Mode](/practice-modes/simulation) gives repeatable responses while you learn to read the message details.
- [API Challenge Solutions](/apichallenges/solutions) show specific cases where a header, status code, or body format decides the outcome.

---

## Summary

HTTP is built around requests and responses. A client sends a request with a verb, URL, headers, and sometimes a body. The server sends back a response with a status code, headers, and sometimes a body.

The verb tells the server what type of action the client wants. The status code gives us a quick summary of what happened. Headers add useful metadata, such as what format the client accepts, what format is being sent, and whether authentication details are included.

Request and response bodies usually contain structured data, often JSON or XML. URLs identify resources, and query strings add extra filtering, sorting, pagination, or other request information.

For API testing, these details are the evidence. When we can read the raw request and response, we can understand what was actually sent, what the server actually returned, and where behaviour differs from the documentation, standards, or our expectations.
