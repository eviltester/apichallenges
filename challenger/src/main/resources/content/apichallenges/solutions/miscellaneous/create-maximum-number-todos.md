---
date:  2025-01-01T15:26:00Z
lastmod: 2026-08-06
title: API Challenges Solution For - POST /api/todos (409) max todos
seo_title: Solution: POST /api/todos (409) max todos | API Challenges
description: How to solve API challenges to Create maximum number of todos
seo_description: Use this walkthrough to solve POST /api/todos (409) max todos with request setup, key headers, and expected status codes so you can complete the challenge.
next_challenge: /gui/challenges
concepts_learned: HTTP POST||boundary testing||API test data||state management
concept_summary: Use this challenge to learn how creating maximum data helps test API limits and state handling.
concept_reference_label: API Testing Concepts and Coverage
concept_reference_url: /reference/testing-apis
concept_reference_label_2: HTTP Methods and Verbs
concept_reference_url_2: /reference/http-verbs
schema_howto_steps: Create todos repeatedly with POST /api/todos until the maximum limit is reached||Use valid JSON payloads and include X-CHALLENGER for every create request||Track successful creations so you can confirm the max-count boundary||Verify the final allowed create response and behavior at the limit||Check challenge status and confirm maximum todo creation is complete
showads: true
---


# How to complete the Create Maximum Number of Todos

This challenge requires you to max out the number of todos in the system.

The API Documentation says that `A maximum of 20 todos is allowed.`

There are many ways to do this and some automated execution is probably required but since there are only 20 allowed, you could do this easily in a REST Client.


## Automated With Java RestAssured Example

I have an automated Java execution using Rest Assured to complete this:

- [C059AddMaximumNumberOfTodosTest.java](https://github.com/eviltester/apichallenges/blob/main/challengerAuto/src/test/java/uk/co/compendiumdev/challenger/restassured/_19_misc_challenges/C059AddMaximumNumberOfTodosTest.java)

This uses a bunch of abstractions to keep the code simple but the basic process is:

- `GET` the `/api/todos` and find out how many there are already
- Issue as many `POST` requests to create a todo as necessary to max it out

```

TodosApi todos = new TodosApi();
List<Todo> currentTodos = todos.getTodos();

int todosToCreate = 20 - currentTodos.size();

while( todosToCreate > 0 ){
    Todo aTodo = todos.createTodo("my title " + 
       todosToCreate, "description", true
       );
    idsToDelete.add(aTodo.id);
    todosToCreate--;
};

// create a to do to throw it over the edge
Todo createMe = new Todo();
createMe.title = "my title";
createMe.description = "my description";
```

## Completing Challenge Using Client Tools

It is possible to complete this challenge manually by issuing all the requests by hand. This is easy to do because a TODO can be created with a single `POST`

`POST` to `/api/todos` with a simple payload `{"title":"not unique"}`

Resending this request would eventually result in a `409 Conflict`:

```json
{
  "errorMessages": [
    "ERROR: Cannot add instance, maximum limit of 20 reached"
  ]
}
```

## Completing Challenge Using Client Tool Features

Some tools have the ability to issue Data Driven requests, so if you can parse the GET response and create a list of ids then you could use the data driven feature of the tool.

- [Bruno Data Driven Testing](https://docs.usebruno.com/testing/tests/data-driven-testing)
- [Postman Data Driven Community Posts](https://community.postman.com/tag/data-driven)

Most of the API client tools also have the ability to create scripts to achieve this.
### Try it now

{{<api-live-request method="POST" path="/api/todos" expected-status="409" headers="Content-Type: application/json||Accept: application/json" body='{"title":"solution widget todo","doneStatus":true,"description":"created from the solution page"}' details="true" summary="POST /api/todos after maxing out todos to trigger 409" open="true">}}

## Lessons Learned

- The max-todos challenge is about system state limits, not a single request field.
- To reproduce the limit, tests must control setup by counting or clearing existing todos.
- `409 Conflict` can signal capacity constraints when the request is valid in isolation.

## Suggested Experiments

- Delete all todos, then create them until the first `409 Conflict` to identify the exact limit.
- After reaching the limit, delete one todo and confirm a new create request succeeds.