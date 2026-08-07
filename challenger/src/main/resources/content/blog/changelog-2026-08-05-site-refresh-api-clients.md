---
date: 2026-08-05T09:00:00Z
lastmod: 2026-08-07
title: API Challenges Site Refresh and Hosted API Clients
seo_title: API Challenges Site Refresh and Hosted API Clients
description: A fuller explanation of the API Challenges site refresh, progress tracking improvements, hosted API clients, and tester OpenAPI tools.
seo_description: Explore the API Challenges site refresh with improved navigation, progress tracking, hosted REST clients, Swagger UI controls, and OpenAPI conversion tools.
categories: Change Log||API Testing||Tools
tags: API Challenges||Swagger UI||OpenAPI Converter||API Client
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# API Challenges Site Refresh and Hosted API Clients

The 2026-08-05 update was a broad refresh across API Challenges. It improved the learning experience, added hosted browser API clients, expanded progress tracking, and made OpenAPI files easier to use for exploratory testing.

This was a site-wide learning update rather than a single feature. The goal was to make the site easier to navigate, easier to practise with, and more useful for people moving from API concepts into hands-on testing.

## Better Learning Flow

The tutorials and reference pages were reorganised so learners can move from concepts to practice more smoothly. The goal was to make the site easier to scan: start with learning material, move into practice modes, then use challenge solutions for review.

API testing has many overlapping ideas: HTTP messages, REST resources, methods, authentication, schemas, and tool behaviour. Clear navigation helps learners build those concepts in a sensible order.

The refresh made the learning path more explicit. A reader can start with the [Learning Zone](/learning), learn core REST ideas, use reference pages for depth, practise in the simulator or Simple API, and then use challenge solutions to compare their thinking with worked examples.

## Buggy API Practice Mode

The update also added the Buggy API practice mode. This gives testers a deliberate place to practise against flawed behaviour rather than only clean examples.

That is important because real API testing is not only about confirming happy path behaviour. Testers also need to notice inconsistencies, unclear error handling, missing validation, and behaviour that differs from documentation. A buggy practice mode gives learners permission to look for problems rather than assume the API is always correct.

## Browser-Based API Clients

The update added hosted online API client pages for a [Basic Client](/tools/online-clients/basic-client) and [Online Swagger UI](/tools/online-clients/swagger). These pages help learners make requests without installing a desktop tool first.

The [OpenAPI Converter](/tools/online-clients/openapi-converter) was also added so testers can convert OpenAPI files into more permissive specifications. That is useful when a strict schema blocks exploratory requests that a tester intentionally wants to try.

Hosted clients are useful for onboarding. A learner can send a request immediately, which keeps the focus on API concepts. Later, they can move to Bruno, cURL, Postman, Insomnia, or another desktop client with a better understanding of what the tool is doing.

The Swagger UI profile controls also help testers compare different levels of strictness. An original spec may document intended usage. A practical or aggressive tester-friendly spec can support exploration beyond the documented happy path.

## Progress Tracking and Practice

API Challenges progress tracking was improved so challenge state and solution flow are clearer. Server-side storage was re-enabled to make restored challenge progress more reliable.

For a practical route through the site, start with the [Learning Zone](/learning), try the [Interactive REST API Tutorial](/tutorials/rest-api-tutorial), and then move into [API Challenges](/apichallenges).

Progress tracking matters because challenge-based learning benefits from continuity. Learners need to know what they have completed, what remains, and where to resume. Clearer progress helps reduce friction and makes the practice experience feel more coherent.

## What This Update Adds for API Testing Learners

The combined benefit is a stronger learning loop:

1. Read a concept.
2. Try a request in the browser.
3. Practise in a mode designed for that skill.
4. Track progress through challenges.
5. Use solutions and references to review.

Now you can route into deeper practice rather than just a static explanation.
