package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class MiscChallenges {

    public static ChallengeDefinitionData postAllTodos409(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "POST /todos (409) max todos",
                        "Issue as many POST requests as it takes to exceed the maximum number of TODOS allowed for a user. The maximum number should be listed in the documentation.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/miscellaneous/create-maximum-number-todos");

        return aChallenge;
    }

    public static ChallengeDefinitionData deleteAllTodos204(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "DELETE /todos/{id} (204) all",
                        "Issue a DELETE request to successfully delete the last todo in system so that there are no more todos in the system");

        aChallenge.addHint(
                "After deleting the last todo, there will be no todos left in the application");
        aChallenge.addHint(
                "Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint(
                "You have to delete all the todo items in the system to complete this challenge");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/miscellaneous/delete-all-todos");

        return aChallenge;
    }
}
