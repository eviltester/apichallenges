---
date: 2026-08-20T10:00:00Z
lastmod: 2026-08-20
title: API From Hell and other fun updates
seo_title: API From Hell has standalone implementations in multiple frameworks
description: Standalone API From Hell implementations are now available for local REST client and proxy testing, with conformance checks and new practice site links.
seo_description: Run API From Hell locally with standalone Node, Python, Java, Flask, Express, and Mockoon versions; use conformance checks through proxies and try two new practice sites.
categories: Change Log
tags: API From Hell
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# Updated API From Hell And Fun Stuff

We have added updated standalone API From Hell versions, which can be found on [Github Standalone API From Hell Implementations](https://github.com/eviltester/apichallenges/tree/main/api-from-hell).

This is a set of standalone versions of the [API From Hell](https://apichallenges.com/practice-modes/fromhell) that you can run locally.

The standalone implementations include:

- Node.js native HTTP server
- Node.js Express
- Python native HTTP server
- Python Flask
- Java raw HTTP server
- Mockoon generated environment

## Fun Stuff

In the list of [Practice Sites](/practice-sites) we've added links to

- [API Sleuth](https://beinghumantester.com/projects/api-sleuth/) an API Testing and Automating Quiz
- [Decrypt The Narrative](https://dcrypt.run/) a deliberately buggy API Game

## What is the API From Hell?

The API From Hell is a set of 70+ endpoints, most of which are errors or problematic in some way. The few that are not issues are designed to help you see how your REST Client handles different format representations like HTML, CSV, JSON, XML etc.

There are multiple categories like malformed JSON, malformed XML. And there are also 'problematic' payloads like JSON with duplicate id fields.

There is an Open API file which you can load into your REST Client and then start making requests.

## Why Run The API From Hell Locally?

The Internet Infrastructure gets in the way. CDNs, web servers, proxies... all of these want to protect you from malformed HTTP. The Web Server that we host the API Challenges on strips out the body content from our deliberately bad `204` responses with content.

If you want to experience the full 100% hellish version of the API and really stress test your tool you need to run it locally.

And to make that easier we split out the API From Hell from the main API Challenges app so you can run, just the API From Hell either directly from source or from the Docker versions.

- [Github Standalone API From Hell Implementations](https://github.com/eviltester/apichallenges/tree/main/api-from-hell)

There are also a set of tests which check each endpoint so if you want to test a Proxy then:

- start an API From Hell server
- start your proxy
- run the conformance coverage through a Proxy

The conformance coverage is currently written in Python but full instructions are in the Readme file.


