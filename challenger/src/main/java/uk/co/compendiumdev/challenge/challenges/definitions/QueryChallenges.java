package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class QueryChallenges {

    public static ChallengeDefinitionData queryTodosFiltered200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "QUERY /todos (200)",
                        "Issue a QUERY request on the `/todos` end point with form-encoded query content to get only todos which are 'done'. There must exist both 'done' and 'not done' todos, to pass this challenge.");

        aChallenge.addHint(
                "QUERY is a safe, read-only HTTP method that can send query content in the request body.");
        aChallenge.addHint(
                "Use `Content-Type: application/x-www-form-urlencoded` for the QUERY body.");
        aChallenge.addHint("Filter on completed todos with a request body of `doneStatus=true`.");
        aChallenge.addHint("Make sure there are todos which are done, and not yet done.");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/query/query-todos-200");
        return aChallenge;
    }

    public static ChallengeDefinitionData queryTodosJsonPathFiltered200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "QUERY /todos (200) JSONPath",
                        "Issue a QUERY request on the `/todos` end point with a JSONPath query body to get only todos which are 'done'. There must exist both 'done' and 'not done' todos, to pass this challenge.");

        aChallenge.addHint(
                "Use `Content-Type: application/jsonpath` to tell the API the QUERY body is a JSONPath expression.");
        aChallenge.addHint(
                "A JSONPath expression such as `$.todos[?(@.doneStatus == true)]` filters the todo collection.");
        aChallenge.addHint("Add `Accept: application/json` so you can inspect the returned todos.");
        aChallenge.addHint("Make sure there are todos which are done, and not yet done.");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/query/query-todos-200-jsonpath");
        return aChallenge;
    }
}
