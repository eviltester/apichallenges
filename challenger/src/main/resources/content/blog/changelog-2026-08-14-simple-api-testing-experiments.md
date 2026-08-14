---
date: 2026-08-14T10:00:00Z
lastmod: 2026-08-14
title: Simple API Testing Experiments For API Test Planning
seo_title: Simple API Testing Experiments, API Test Plan and Test Approach Ideas
description: Use the Simple API experiments page as a practical source of API test plan, test approach, and exploratory testing ideas.
seo_description: Use Simple API experiments as practical API test plan, API test approach, exploratory testing, and API test coverage ideas.
categories: Change Log||API Testing||REST API Tutorial
tags: Simple API||API Test Plan||API Test Approach||Exploratory Testing||API Experiments
hide_sidebar: true
schema_type: BlogPosting
showads: true
---

# Simple API Testing Experiments For API Test Planning

We have added a new [Simple API Testing Experiments](/practice-modes/simpleapi/experiments) page.

This is a hands-on page for people who want to practise API testing without following a fixed challenge list. It is written as a set of testing experiments, so it can be used as:

- an API test plan starter;
- an API test approach guide;
- a list of API exploratory test ideas;
- a source of API test coverage reminders;
- a practical way to learn by sending real requests.

## Why API Experiments List Exists

Formal test plans are often unnecessary, it is possible to test from a set of ideas.

When people create test plans they often vary from too detailed to, not enough detail. Whatever we are faced with, we have to be able to come up with new ideas to try next when sitting in front of an API client.

The Simple API experiments page tries to help you develop those thought processes.

We present a Simple API test plan as a set of experiments:

- what request can I send?
- what response should I observe?
- what state should change?
- what data should I vary?
- what errors should I try to trigger?
- what documentation claim should I compare against the real API?
- what else should I do?

You can use this for exploratory API testing. We've tried not to be too prescriptive and encourage you to observe and think, because it keeps the focus on learning. 

## Why Use Simple API For Experiments?

The Simple API has a small `/simpleapi/items` resource model, which makes it a good place to practise API testing techniques.

The API is large enough to support useful coverage ideas:

- creating items;
- reading collections and individual items;
- filtering;
- replacing and amending data;
- deleting data;
- checking invalid values;
- trying duplicate identifiers;
- varying request headers;
- comparing behaviour with documentation;
- testing unsupported methods and error responses.

But it is still small enough that the test data is easy to understand. That makes it a good API for learning how to build an API test approach before moving on to larger systems.

## How To Experiment with an API

Open the [Simple API Testing Experiments](/practice-modes/simpleapi/experiments) page and work through one experiment at a time.

For each experiment:

1. Read the idea.
2. Send the request with the inline API client.
3. Inspect the status code, headers, and response body.
4. Vary the path, data, headers, or method where the client allows it.
5. Write down what you learned and what you want to try next.

The page is not intended to be a rigid checklist that you would use every time you come to test an API, use it as a trigger for experimentation and thinking of new ideas.

For example, a create-item experiment might lead you into validation checks, duplicate ISBN checks, update checks, delete checks, and state verification. A content negotiation experiment might lead you into `Accept` headers, JSON, XML, and unsupported media types.

## API Test Plan And API Test Approach Ideas

If you are building an API test plan, you could use the experiments as initial coverage ideas.

You can group the ideas into a simple API test approach:

- happy path workflows;
- CRUD lifecycle coverage;
- field validation and boundary data;
- duplicate and conflicting data;
- state changes across multiple requests;
- filtering and query behaviour;
- headers and content negotiation;
- unsupported methods and error responses;
- documentation comparison;
- multi-user and data lifecycle risks.

Eventually you'll learn the underpinning HTTP Standards and realise that 'everything is just data' and 'data can be varied'.

## API Exploratory Test Ideas

When using this for exploratory testing, do not stop when one request succeeds.

Ask:

- what happens if I vary one field?
- what happens if I omit a field?
- what happens if I reuse a value?
- what happens if I use a different item id?
- what happens if I change the `Accept` header?
- what happens if I send the right request to the wrong path?
- what happens if the docs and the implementation disagree?

Asking questions is a key part of testing and opens up entrance to the infinite world of Software Testing coverage ideas.

## Supporting Reference

The [API Testing Concepts and Coverage](/reference/testing-apis) reference page explains the broader concepts underpinning this: coverage, risk, data variation, documentation, evidence, and how to design useful API tests.

Use the reference page for the testing concepts, then use the [Simple API Testing Experiments](/practice-modes/simpleapi/experiments) page to practise the ideas against a real API.
