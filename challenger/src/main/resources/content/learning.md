---
title: Learning Utilities and Resources
seo_title: Learning Utilities and Resources | API Challenges
description: A list of HTTP REST API learning tutorials and recommended books and practice sites for API Testing.
lastmod: 2026-08-09
seo_description: Start learning API testing with curated tutorials, practical resources, and a clear path from fundamentals to confident hands-on execution.
og_image: /images/hero/learning-zone-api-testing-path-1600x720.jpg
og_image_alt: Learning Zone hero image showing the live learning page, REST fundamentals, and the API testing learning path.
schema_image: /images/hero/learning-zone-api-testing-path-1600x720.jpg
showads: true
---

# Learning API Testing

<figure class="content-hero-figure learning-zone-hero-image">
  <img src="/images/hero/learning-zone-api-testing-path-1600x720.jpg" width="1600" height="720" loading="eager" decoding="async" alt="Learning Zone hero image showing the live learning page, REST fundamentals, and the API testing learning path.">
</figure>

## REST API Tutorial Syllabus

Use this as the main route through the Learning Zone. Each step builds enough vocabulary and practical evidence to make the next step easier:

1. [Interactive REST API Tutorial](/tutorials/rest-api-tutorial) - send real requests and see REST concepts in action.
2. [HTTP basics](/reference/http-basics) - learn how requests, responses, headers, bodies, and URLs fit together.
3. [REST basics](/reference/rest-api-basics) - connect resources, representations, statelessness, and CRUD with API design.
4. [HTTP methods](/reference/http-verbs) - understand what `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `HEAD`, and `OPTIONS` are expected to mean.
5. [Status codes](/reference/http-basics#toc7) - read response outcomes as testing evidence, not just pass/fail signals.
6. [OpenAPI](/reference/openapi) - use API descriptions to find endpoints, schemas, responses, and security expectations.
7. [How to Test REST APIs](/tutorials/rest-api-testing) - practise a step-by-step approach for exploring, checking, and reporting API behaviour.
8. [Interactive API Simulation](/tutorials/api-simulator-walkthrough) - use the simulator walkthrough to practise request and response handling safely.
9. [API Challenges](/apichallenges) - apply the ideas in a guided API challenge app.

## Reference

Use these when you want to revisit a specific concept in more detail:

- [Web Applications](/reference/web-basics)
- [HTTP Basics](/reference/http-basics)
- [HTTP Verbs and Methods](/reference/http-verbs)
- [REST API Basics](/reference/rest-api-basics)
- [API Testing Concepts and Coverage](/reference/testing-apis)
- [OpenAPI](/reference/openapi)
- [Swagger UI](/tools/online-clients/swagger/about)
- [Summary](/reference/summary)

## Practise with APIs

Move from syllabus reading to hands-on practice with APIs designed for learning:

- [Simulation Mode](/practice-modes/simulation)
- [Simple API](/practice-modes/simpleapi)
- [API Challenges overview](/apichallenges)

## Review Challenge Solutions

The challenge solutions are longer walkthroughs for specific API testing ideas. Use them after trying a challenge, or when you want to compare your reasoning with a worked example:

- [Read the API Challenge Solutions](/apichallenges/solutions)
- [Try the API Challenges app](/gui/challenges)
- [Read the API Challenges Documentation](/api/docs)

## Download Some Tools

- [HTTP/REST Clients](/tools/clients)
- [HTTP Proxies](/tools/proxies)

## Experiment with Other APIs

We have an extensive list of [Practice Sites](/practice-sites), which includes tips on how to use them.

We are in the process of creating longer tutorial guides with exercises for each of the listed sites.


## Understand Your REST Client

The Mirror mode is a good way to test out your tooling and see the details of your requests without using a proxy.

[Learn About the Mirror Mode Here](/practice-modes/mirror)

Also you want to learn the limits of your tooling and make sure it is accurately reporting your requests and the responses received. We create the API From Hell to help you stress your REST Client.

Using this API we've seen clients make malformed XML in the response look like we received perfect XML and responses that cannot be rendered. You really need to know what your API Client is hiding.

[Stress your REST API Client with the API From Hell](/practice-modes/fromhell)

## Experiment with our API Simulation Mode

The API has a simulation mode, it uses hard coded data in responses, but tries to mimic some conditions.

e.g. it expects you to post a specific JSON payload or XML payload and responds 'as if' you sent it. But... it also checks if you sent valid json, or valid xml, and responds based on your headers e.g. returning XML if you ask for it.

The simulator is stateless and does not track your usage, making it deterministic for multiple users.

The simulator is a good place to get started because it will respond nicely... unless you mess up the request syntax.

[Learn About the Simulator Here](/practice-modes/simulation)

## Automating and Testing a REST API Book

Alan Richardson wrote a book [Automating and Testing a REST API](https://www.eviltester.com/page/books/automating-testing-api-casestudy/)

Buying the book helps support this web site and application.

## Challenge Tutorials

The Challenges have full solution tutorials with key learning lessons and experiments to continue learning from. 

[Read the API Challenge Solutions](/apichallenges/solutions)

## Open Source Workshops

All the material from Alan Richardson's REST API Training workshops have been released to Github.

[Find the Workshop Material Here](https://www.eviltester.com/post/rest-api-workshops/)
