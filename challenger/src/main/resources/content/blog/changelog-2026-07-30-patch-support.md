---
date: 2026-07-30T09:00:00Z
lastmod: 2026-08-07
title: PATCH Support for API Challenges and Simple API
seo_title: PATCH Support for API Challenges and Simple API
description: Learn what PATCH support adds to API Challenges and Simple API, including partial JSON, JSON Merge Patch, and JSON Patch styles.
seo_description: Learn what PATCH support adds to API Challenges and Simple API, including partial JSON updates, JSON Merge Patch, JSON Patch, and API testing coverage.
categories: Change Log||HTTP Methods||API Testing
tags: PATCH||JSON Patch||JSON Merge Patch||REST API
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# PATCH Support for API Challenges and Simple API

The 2026-07-30 update added `PATCH` support for API Challenges todo instance routes and Simple API item instance routes. It also added challenge coverage and solution walkthroughs for `PATCH /api/todos/{id}`.

This update added an important missing piece to the REST practice path. Learners could already practise create, read, replace, and delete behaviour. Adding `PATCH` lets them practise partial updates, media type differences, and more precise state-change assertions.

## Why PATCH Is Useful

`PATCH` is used when a client wants to change part of a resource rather than replace the whole representation. That makes it different from `PUT`, which is commonly treated as a complete update.

For API testing, `PATCH` creates useful questions:

- What happens when only one field is sent?
- Are omitted fields preserved or removed?
- Does the API support a documented patch media type?
- Are invalid patch operations rejected clearly?

Those questions are useful because partial updates are common in real APIs. A user might change a price, a status, a description, or a flag without resending the whole resource. Tests need to check that the intended field changed and that unrelated fields did not.

## Patch Styles Added

This update added support for partial JSON, JSON Merge Patch, and JSON Patch update styles. These styles look similar from a distance because they all update resources, but they have different rules.

JSON Merge Patch usually sends a partial object. JSON Patch sends a list of operations such as replace, add, or remove. A tester needs to know which style the API expects because the same endpoint can behave very differently depending on `Content-Type`.

This gives learners a practical reason to care about headers. A request body alone is not enough. The `Content-Type` tells the server how to interpret that body. A JSON object used as a merge patch and a JSON Patch operation list are different contracts.

## What the New PATCH Challenges Add

The new `PATCH /api/todos/{id} (200)` challenges give learners a guided way to practise three styles of partial update. That is better than only adding the endpoint because it also teaches expected behaviour and common request setup.

The solution walkthroughs help learners compare:

- a simple partial JSON update,
- a JSON Merge Patch update,
- a JSON Patch operation list,
- the expected status code,
- the expected state after the request.

This turns `PATCH` from a definition into an observable behaviour.

## What This Update Adds for API Testing Practice

Use [HTTP Methods and Verbs](/reference/http-verbs) to compare `PATCH`, `PUT`, and `POST`, then practise state changes in [Simple API](/practice-modes/simpleapi) or the [API Challenges app](/apichallenges).

When testing `PATCH`, do not only assert that the request returns `200`. Read the resource back and check that only the intended fields changed. Then try invalid patch documents, unsupported content types, missing resources, and repeated patch requests. That is where the useful API testing lessons appear.
