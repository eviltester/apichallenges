---
title: API Testing Concepts and Coverage
seo_title: API Testing Concepts: Coverage, Data, Security and Risk
description: Reference guide for API testing coverage, risk, data, security, capacity, and evidence.
lastmod: 2026-08-04
seo_description: Learn API testing concepts for coverage, risk, data, security, capacity, documentation, and evidence before building practical REST API checks.
showads: true
---

# API Testing Concepts and Coverage

Testing an API is not just sending a few requests and checking that they return `200`.

An API is a system boundary. It accepts input, applies rules, changes or reads state, and returns a response. Good API testing helps us understand whether that boundary behaves correctly, consistently, securely, and usefully for the clients that depend on it.

And we have to use the API to learn if the functionality triggered by the API calls and the data resulting from the functionality is actually correct.

We use the API to get to the system. We are testing the system as well as the API. Never forget that. When we forget that, we often forget to chain requests to check state, run the backend processes to change state which we have to check, and some people will even 'mock' or stub the system and claim to have tested the API.

The API is an 'interface' which we use to test the system.

This reference page covers how to think about API testing. We will look at coverage, risk, data variation, architecture, security, capacity, documentation, and how to build a useful set of test ideas without depending on a single tool or checklist.

---

## Testing Different from Technology and Tooling

Tools help us send requests. Testing helps us decide if the requests have triggered and retrieved what they are supposed to.

It is easy to spend a lot of time learning a REST client, an automation library, a proxy, or an OpenAPI viewer and feel like we are testing because we are pressing buttons and seeing responses.

We need tooling to test. But using tools is not testing.

Testing starts when we ask questions:

- what could go wrong?
- what does the API promise to do?
- what inputs does it accept?
- what state does it depend on?
- what should happen when the caller is not authenticated?
- what happens when the client asks for something the API does not support?
- what evidence would convince us that the API is behaving as expected?
- etc.

Technology and tooling give us ways to perform checks and gather evidence. The testing thinking decides what evidence we need.

For example, a tool can send:

~~~~~~~~
POST /todos
~~~~~~~~

But testing asks:

- what is the smallest valid todo?
- what is the largest valid todo?
- what fields are required?
- what happens with malformed JSON?
- what happens when the title already exists?
- what happens when the caller is not authorised to create todos?
- etc.

The tool performs the request. The testing thinking designs the request for a purpose.

---

<a id="what-would-we-test"></a>

## What Would We Test?

- Risk
- Coverage
- Functionality and Outcomes

API testing can be organised around three related ideas.

`Risk` means what might fail, what might harm users, and what might harm the system.

`Coverage` means what parts of the API, data, behaviour, and architecture have we exercised.

`Functionality and Outcomes` means what the API is supposed to do, and whether the response and state changes match that expectation. I would also include - does it perform under stress and load? Is it secure? 'Outcomes' is a pretty broad category.

For example, if we test `POST /todos`, we might think about:

- Risk: duplicate todos, unauthorised creation, invalid data accepted, server crash
- Coverage: valid JSON, invalid JSON, missing fields, long fields, different users, different response formats
- Outcome: todo is created, status code is correct, response body is correct, stored data can be retrieved later, same outcome under load

The best API testing will mix these ideas.

A purely coverage-driven approach might touch every endpoint but miss the riskiest business rules.

A purely risk-driven approach might test important failures but miss simple documented behaviours.

A purely functionality-driven approach might confirm the happy path but miss invalid data, security checks, and state transitions.

And, those categories are a high level starting point for building a model, go beyond them.

---

<a id="coverage-driven-testing"></a>

## Coverage Driven Testing

REST APIs are a very pure system.

- Input  `->` Process  `->` Output
- Request  `->` Process  `->` Response

Most of the variation comes from:

- Input
- Current System State

With a UI we have to worry about variation like:

- which browser?
- exactly how I interact e.g. time between click and release of mouse, did I hold a key at the same time? etc.

With an API, the visible interaction is usually simpler. We send an HTTP request and receive an HTTP response.

That simplicity is powerful for testing because it lets us model the API as combinations of:

- verb
- endpoint
- headers
- query parameters
- path parameters
- request body
- authentication state
- existing server data
- concurrent load

For example:

~~~~~~~~
GET /todos?doneStatus=true
Accept: application/json
~~~~~~~~

The request has a verb, an endpoint, a query parameter, and a header. Each of those can be varied.

But the current system state also matters.

`GET /todos/999` means something different when todo `999` exists than when it does not exist.

`POST /todos` means something different when the database is empty than when it already contains the maximum number of todos.

Coverage-driven API testing is about deliberately varying these inputs and states so that we learn how the API behaves.

---

<a id="coverage-of-what"></a>

## Coverage of What?

- Verbs - have you used every verb with every endpoint?
- Endpoints - have you tried them all?
- Swagger - have you used the Swagger API document?
- Documentation - have you read the docs?
- Query Params - have you tried combinations?
- formats (content and accept) - have you varied XML, JSON, Text and others?
- State - Get when missing, Create when exists? etc.

Coverage is not just "we called every endpoint once."

A useful coverage model for API testing can include:

- every documented endpoint
- every supported HTTP verb for each endpoint
- unsupported verbs for important endpoints
- path parameters
- query parameters
- request headers
- response headers
- request body formats
- response body formats
- status codes
- authentication and authorisation states
- important data states
- known business rules
- etc.

Documentation can help us build this model.

If the API has an OpenAPI or Swagger document, use it to identify documented paths, methods, schemas, status codes, and security requirements.

Then compare the documentation with the actual behaviour.

For example:

- documentation says `POST /todos` can return `201`, but we also observe `422`
- documentation says `GET /todos` supports XML, but XML returns `406`
- documentation says `DELETE /todos/{id}` requires authentication, but it works without a token

These differences are useful findings. Sometimes the API is wrong. Sometimes the documentation is wrong. Either way, the team learns something.

And never, ever, trust the documentation to tell you what you can and can't do. Don't assume that it accurately tells ou what verbs are supported, or even what endpoints are available. The documentation tells you what it wants you to know, not what is actually available.

---

## What Are the Architecture Risks?

- Client  `->` Web Server  `->` App Server  `->` App
- Do we understand the architecture?

An API request often travels through more than one component before it reaches the application code.

For example:

~~~~~~~~
Client  `->` CDN  `->` Load Balancer  `->` Web Server  `->` App Server  `->` Database
~~~~~~~~

Or:

~~~~~~~~
Client  `->` API Gateway  `->` Authentication Service  `->` Application  `->` Message Queue  `->` Worker
~~~~~~~~

Architecture affects testing because each component can change behaviour.

A gateway might reject large headers before the application sees them.

A proxy might remove or add headers.

A load balancer might route requests to different versions of the application.

A cache might return an old response.

A background worker might process a request after the response has already been sent.

When testing an API, ask:

- what components handle the request?
- where is authentication checked?
- where are request size limits enforced?
- where are logs written?
- is there caching?
- are there asynchronous processes?
- can different servers have different data or configuration?
- etc.

Understanding the architecture helps us explain strange behaviour and design better test approaches.

---

## What Are the Capacity Risks?

- Performance?
- Load Testing?

When testing Capacity Risks asks whether the API can cope with the amount and shape of traffic it needs to handle.

This is not only about "how many requests per second?"

Useful capacity questions include:

- how quickly should common requests respond?
- what happens when many users create data at the same time?
- what happens when a query returns a large result set?
- are there pagination limits?
- are expensive filters or sorts protected?
- does the API rate limit clients?
- what happens when a downstream service is slow?
- does the API return useful errors under load?
- can the database be 'full'?
- etc.

Functional API testing often uses small amounts of data because we want fast feedback.

When testing Capacity we deliberately change the amount, timing, and concurrency of requests.

For example, a `GET /todos` test with 10 todos tells us something. A `GET /todos` test with 100,000 todos tells us a very different something.

Even if a dedicated performance team handles load testing, API testers can still spot capacity risks early by paying attention to large payloads, slow queries, missing pagination, and repeated requests.

---

## What Are the Security Risks?

- Authentication
- Authorisation
- Injection
- What headers are accepted? X-HTTP-Method-Override?

APIs are often directly accessible by other systems, scripts, and users with API clients. Security testing is very important.

Start with authentication and authorisation.

Authentication checks who the caller is. Authorisation checks what the caller is allowed to do.

For each important endpoint, try:

- no credentials
- invalid credentials
- valid credentials with insufficient permission
- valid credentials with the correct permission
- expired credentials
- credentials for a different tenant, account, or user

Then think about input attacks.

Injection risks appear when user supplied data is interpreted as a command, query, template, expression, or script.

For example:

- SQL-like input in a search field
- HTML or JavaScript in text fields
- path traversal strings in file names
- unexpected JSON structures in request bodies
- long strings that might trigger parsing or storage issues

Headers also matter.

Some systems accept headers such as:

~~~~~~~~
X-HTTP-Method-Override: DELETE
~~~~~~~~

If supported, this might allow a `POST` request to behave like a `DELETE` request. That can be useful for compatibility, but dangerous if not protected and documented.

Security testing can become a large specialist topic. At this level, the main goal is to remember that an API request is input, and all input needs to be treated with suspicion.

It is worth reading through both the OWasp Web Guidance and OWASP API Testing Guidance:

- [OWASP API Security Testing Framework](https://owasp.org/www-project-api-security-testing-framework/)
- [OWASP Web Security Testing Guide](https://owasp.org/www-project-web-security-testing-guide/) 

---

<a id="data-risks"></a>

## Data Risks

- minimum data in requests - missing fields, headers
- not enough data in requests
- wrong format data: json, xml, length, null, empty
- malformed data
- consistency? query params across requests?
- are defaults correct?
- duplicate data in payloads?
- headers: missing, malformed, too many, duplicate

Data is one of the richest sources of API test ideas.

For a request body, consider:

- required fields
- optional fields
- missing fields
- extra fields
- null values
- empty strings
- whitespace-only strings
- very long strings
- numbers at minimum and maximum values
- negative numbers
- booleans as true, false, strings, or numbers
- arrays with zero, one, many, and too many items
- duplicate values
- invalid enum values
- malformed JSON or XML

Consider the entire request as data, so that means the headers are also data we can vary.

For headers, consider:

- missing `Content-Type`
- incorrect `Content-Type`
- unsupported `Accept`
- duplicate headers
- very long header values
- malformed authentication headers
- custom headers with unexpected values

And the URL is data, we can vary the URL and Query Parameters.

For query parameters, consider:

- missing parameters
- unknown parameters
- duplicate parameters
- invalid sort fields
- invalid filter values
- pagination limits
- combinations of filters

Data risks are not only about invalid data.

We also need to test valid-but-interesting data:

- the smallest valid request
- the largest valid request
- Unicode text and different character encodings
- boundary values
- data that already exists
- data owned by another user
- data that has just been deleted

When an API has business rules, turn those rules into data ideas.

If a todo title has a maximum length of 50 characters, test 49, 50, and 51 and more.

If an item must have a unique ISBN, test a new ISBN and an existing ISBN.

If a field is generated by the server, test what happens when the client tries to provide it.

One benefit of API testing is that we are sending requests and these are essentially text messages. They are so easy to vary, amend, expand and send.

Don't just send 3 messages with max, min and just right. Automate sending 0 - max length (assuming it isn't too long). You don't have to do that all the time, but use data generation and manipulation tools and automated tooling to send more requests than you normally would manually. 

---

<a id="document-your-testing"></a>

## Document Your Testing

- How can you document your testing?
   - Mindmaps?
   - Text files?
   - Record all requests through an HTTP Proxy and store as a HAR file?

API testing can produce a lot of evidence very quickly.

We need to document what we tried, it very quickly becomes hard to know what we have covered and hard to reproduce a finding if you aren't tracking what you sent.

Documentation does not need to be heavy.

Useful lightweight options include:

- a checklist of endpoints and verbs
- a mind map of risks and coverage areas
- a text file with test ideas and notes
- saved requests in an API client
- automated checks in code
- proxy history exported as a HAR file
- screenshots or copied raw request and response messages for important findings

For exploratory testing, it can help to record:

- what you were trying to learn
- request sent
- response observed
- data state before and after
- questions raised
- bugs or documentation gaps found
- areas not yet tested

Proxy recordings are especially useful because they capture what was actually sent over HTTP.

Sometimes a REST client shows a tidy version of a request, but the proxy reveals extra headers, missing headers, redirects, cookies, or a different body than you expected.

---

<a id="common-api-issues"></a>

## Other Risks or Common Issues?

Every API has its own risks, but some issues appear often.

Common API issues include:

- documentation does not match implementation
- wrong status codes
- error responses are inconsistent
- validation differs between create and update
- unsupported verbs return `500` instead of `405`
- missing or incorrect `Content-Type`
- missing authentication checks
- authenticated users can access each other's data
- pagination is missing or inconsistent
- sorting and filtering behave differently together than separately
- `PUT` behaves like `PATCH`
- duplicate data is created accidentally
- deleted data can still be retrieved
- response bodies expose internal details
- server errors reveal stack traces
- rate limits are missing or undocumented
- headers are ignored
- headers are wrong

These common issues are not a replacement for understanding the product, but they are a useful starting list when you are learning a new API.

---

## Exercise: Think Through Testing

- Read the requirements
- Create some test ideas
- Look at the existing testing conducted
- Any ideas from that?
- Test
- Document and Track your Testing in a lightweight fashion
- Try different tools
- Run all your requests and responses through a Proxy and review the results - you might be surprised to see differences that your REST client tool did not reveal.
   - if you really want to test your REST Client then use the [API From Hell](https://apichallenges.eviltester.com/practice-modes/fromhell)

To practise API testing, choose one endpoint and build a small test model for it.

For example, choose:

~~~~~~~~
POST /todos
~~~~~~~~

Then ask:

- what does the documentation say this endpoint does?
- what fields are required?
- what fields are optional?
- what status code should a valid request return?
- what response body should be returned?
- what should happen if authentication is missing?
- what should happen if the body is malformed?
- what should happen if required fields are missing?
- what should happen if field values are too long?
- what should happen if the database already contains similar data?

Send requests, observe responses, and keep notes.

Then compare what you saw with the documentation and with your expectations from HTTP and REST conventions.

Try the same endpoint using more than one tool if you can. For example, use a REST client, `cURL`, automated code, and a proxy.

Different tools make different parts of the request visible, and that helps you learn.

---

## Practise This Concept

This page is about broadening your test thinking. Use the following pages when you want to move from coverage ideas into request examples and worked challenge evidence:

- [REST API Tutorial](/tutorials/rest-api-tutorial) is a concise refresher on the HTTP and REST pieces you will vary during testing.
- [How to Test REST APIs](/tutorials/rest-api-testing) gives a more procedural path for planning, executing, and recording exploratory API tests.
- [Simulation Mode](/practice-modes/simulation) is useful when you want stable responses while practising tool use and observation.
- [Simple API](/practice-modes/simpleapi) supports CRUD, duplicate data, validation, and state checks without auth setup.
- [API Challenge Solutions](/apichallenges/solutions) show how individual risks become concrete checks, expected statuses, and follow-up experiments.

---

## Summary

API testing is about understanding a system boundary. We send requests, observe responses, and check whether the API handled the input, state, security, and business rules correctly.

Always remember that we are testing the system behind the API, as well as the API interface to the system.

Good API testing combines coverage and risk. We vary verbs, endpoints, headers, query parameters, payloads, formats, authentication states, data states, and system conditions. We also think about architecture, capacity, security, and how the API behaves when clients make mistakes.

Tools help us send requests and collect evidence, but the testing value comes from the questions we ask and the observations we make.

Document your testing lightly as you go. Keep enough notes, saved requests, proxy captures, or automated checks so that you can explain what you covered, reproduce important findings, and decide what to test next.
