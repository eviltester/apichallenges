---
date: 2026-07-17T09:00:00Z
lastmod: 2026-08-07
title: API Challenges Repo Move and Swagger UI Updates
seo_title: API Challenges Repo Move and Swagger UI Updates
description: Learn why the API Challenges GitHub repo move, inline simulator widgets, and Swagger UI pages improve API testing practice.
seo_description: Learn how the API Challenges GitHub repo move, inline API simulator widgets, and Swagger UI documentation pages improve API testing practice and learning.
categories: Change Log||OpenAPI||API Testing
tags: GitHub||Swagger UI||API Simulator||OpenAPI
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# API Challenges Repo Move and Swagger UI Updates

The 2026-07-17 update moved the project to the [API Challenges GitHub repo](https://github.com/eviltester/apichallenges), added inline HTTP request widgets to the simulator instructions, and added Swagger UI for Simple API and API Challenges.

This update improved both transparency and hands-on learning. The GitHub repo gives people a place to inspect the project, while the inline widgets and Swagger UI pages make it easier to move from reading about an API to interacting with it.

## Why the Repo Move Helps

Putting the project in a public GitHub repo makes the application easier to inspect, learn from, and contribute to. For learners, it also creates a route from using an API to studying how a practice API is implemented.

That matters because API testing improves when testers understand how systems are built. You do not need to read all the code to benefit from the repo. Even a quick look can help you understand routing, challenge definitions, test coverage, and how practice APIs are assembled.

For people using API Challenges as a teaching tool, the repo also makes changes easier to track and discuss. A public repository turns the site from a black-box practice app into a study resource.

## Inline Simulator Requests

Inline HTTP request widgets make tutorial pages more active. A learner can read an explanation, send the request, and compare the response without switching tools.

That does not replace external clients. It gives beginners a low-friction way to understand request structure before moving to a desktop client.

The inline simulator requests are particularly helpful because the API Simulator is designed for controlled exploration. Learners can try common methods, unsupported methods, missing records, and headers-only requests. Seeing those examples on the page helps them connect the explanation with evidence.

## Swagger UI for Practice APIs

Swagger UI pages were added for [API Challenges](/docs/swagger-ui) and [Simple API](/simpleapi/docs/swagger-ui). These pages make the OpenAPI descriptions visible and interactive.

Use [Swagger UI and Tools](/reference/swagger) to understand where Swagger UI helps and where exploratory testing still needs a more flexible REST client.

Swagger UI is useful because it gives readers a quick way to discover paths, schemas, and example requests. It also lets learners make requests from a documentation-first view. That is a different experience from starting in a REST client, and both perspectives are useful.

## What This Update Adds for API Testing Practice

This update added three complementary routes into the same learning material:

- source-level learning through GitHub,
- guided experimentation through inline request widgets,
- documentation-driven exploration through Swagger UI.

Together, those routes support a stronger API testing workflow. Read the docs, try the request, inspect the response, and when needed, look behind the scenes to understand why the practice API behaves the way it does.
