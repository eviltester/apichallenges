package uk.co.compendiumdev.challenge.challenges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerConfig;

public class ChallengeDefinitionsTest {

    @Test
    void allSinglePlayerChallengesHaveHints() {
        assertAllChallengesHaveHints(new ChallengerConfig());
    }

    @Test
    void allMultiPlayerNoStorageChallengesHaveHints() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();

        assertAllChallengesHaveHints(config);
    }

    @Test
    void allMultiPlayerLocalStorageChallengesHaveHints() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();

        assertAllChallengesHaveHints(config);
    }

    @Test
    void textCalendarTodoInstanceChallengeIsInAcceptSectionAfterUnsupportedAccept() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();
        ChallengeDefinitions definitions = new ChallengeDefinitions(config);

        List<ChallengeDefinitionData> challenges = new ArrayList<>(definitions.getChallenges());
        ChallengeDefinitionData calendarChallenge =
                challengeNamed(challenges, "GET /todos/{id} (200) text/calendar");

        Assertions.assertEquals(
                CHALLENGE.GET_TODO_ACCEPT_TEXT_CALENDAR,
                definitions.getChallenge("GET /todos/{id} (200) text/calendar"));
        Assertions.assertTrue(
                indexOfChallenge(challenges, "GET /todos (406)")
                        < indexOfChallenge(challenges, "GET /todos/{id} (200) text/calendar"));
        Assertions.assertTrue(
                indexOfChallenge(challenges, "GET /todos/{id} (200) text/calendar")
                        < indexOfChallenge(challenges, "POST /todos XML"));
        Assertions.assertTrue(
                calendarChallenge.description.contains("Accept` header of `text/calendar`"));
        Assertions.assertTrue(
                calendarChallenge.solutions.stream()
                        .anyMatch(
                                solution ->
                                        solution.linkData.equals(
                                                "/apichallenges/solutions/accept-header/get-todos-id-200-calendar")));
    }

    @Test
    void collectionQueryChallengesMentionJsonResponseRequirement() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();
        ChallengeDefinitions definitions = new ChallengeDefinitions(config);
        List<ChallengeDefinitionData> challenges = new ArrayList<>(definitions.getChallenges());

        for (String challengeName :
                List.of(
                        "GET /todos (200) ?filter",
                        "GET /todos (200) ?filter id greater than",
                        "GET /todos (200) ?filter id less than",
                        "GET /todos (200) ?filter id single result",
                        "GET /todos (200) ?filter description regex",
                        "GET /todos (200) ?filter description wildcard",
                        "GET /todos (200) ?_sortBy ascending",
                        "GET /todos (200) ?_sortBy descending",
                        "GET /todos (200) ?_sortBy multiple",
                        "GET /todos (200) ?filter&_sortBy",
                        "GET /todos (200) ?_limit",
                        "GET /todos (200) ?_limit&_offset",
                        "GET /todos (200) ?_sortBy&_limit&_offset",
                        "GET /todos (200) ?filter&_limit&_offset")) {
            ChallengeDefinitionData challenge = challengeNamed(challenges, challengeName);

            Assertions.assertTrue(
                    challenge.description.contains("JSON format"),
                    challengeName + " should mention JSON response format");
            Assertions.assertTrue(
                    challenge.hints.stream()
                            .anyMatch(hint -> hint.hintText.contains("Accept: application/json")),
                    challengeName + " should include the Accept JSON hint");
        }
    }

    @Test
    void queryBodyChallengesAreInQuerySectionBeforePatchChallenges() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();
        ChallengeDefinitions definitions = new ChallengeDefinitions(config);

        List<ChallengeDefinitionData> challenges = new ArrayList<>(definitions.getChallenges());
        ChallengeDefinitionData jsonPathChallenge =
                challengeNamed(challenges, "QUERY /todos (200) JSONPath");
        ChallengeDefinitionData structuredJsonChallenge =
                challengeNamed(challenges, "QUERY /todos (200) Structured JSON");

        Assertions.assertEquals(
                CHALLENGE.QUERY_TODOS_JSONPATH_FILTERED,
                definitions.getChallenge("QUERY /todos (200) JSONPath"));
        Assertions.assertEquals(
                CHALLENGE.QUERY_TODOS_STRUCTURED_JSON_FILTERED,
                definitions.getChallenge("QUERY /todos (200) Structured JSON"));
        Assertions.assertTrue(
                indexOfChallenge(challenges, "QUERY /todos (200)")
                        < indexOfChallenge(challenges, "QUERY /todos (200) JSONPath"));
        Assertions.assertTrue(
                indexOfChallenge(challenges, "QUERY /todos (200) JSONPath")
                        < indexOfChallenge(challenges, "QUERY /todos (200) Structured JSON"));
        Assertions.assertTrue(
                indexOfChallenge(challenges, "QUERY /todos (200) Structured JSON")
                        < indexOfChallenge(challenges, "PATCH /todos/{id} (200) partial"));
        Assertions.assertTrue(jsonPathChallenge.description.contains("JSONPath query body"));
        Assertions.assertTrue(
                structuredJsonChallenge.description.contains("Structured JSON query body"));
        Assertions.assertTrue(
                jsonPathChallenge.hints.stream()
                        .anyMatch(hint -> hint.hintText.contains("application/jsonpath")));
        Assertions.assertTrue(
                structuredJsonChallenge.hints.stream()
                        .anyMatch(
                                hint ->
                                        hint.hintText.contains(
                                                "application/vnd.apichallenges.todo-query+json")));
        Assertions.assertTrue(
                structuredJsonChallenge.hints.stream()
                        .anyMatch(
                                hint ->
                                        hint.hintLink.equals(
                                                "/reference/http-verbs/http-query#http-query-structured-json-body")));
        Assertions.assertTrue(
                jsonPathChallenge.solutions.stream()
                        .anyMatch(
                                solution ->
                                        solution.linkData.equals(
                                                "/apichallenges/solutions/query/query-todos-200-jsonpath")));
        Assertions.assertTrue(
                structuredJsonChallenge.solutions.stream()
                        .anyMatch(
                                solution ->
                                        solution.linkData.equals(
                                                "/apichallenges/solutions/query/query-todos-200-structured-json")));
    }

    private void assertAllChallengesHaveHints(final ChallengerConfig config) {
        Collection<ChallengeDefinitionData> challenges =
                new ChallengeDefinitions(config).getChallenges();
        List<String> missingHints = new ArrayList<>();

        for (ChallengeDefinitionData challenge : challenges) {
            if (!challenge.hasHints()) {
                missingHints.add(challenge.id + " " + challenge.name);
            }
        }

        Assertions.assertTrue(
                missingHints.isEmpty(),
                "Challenges missing hints: " + String.join(", ", missingHints));
    }

    private ChallengeDefinitionData challengeNamed(
            final List<ChallengeDefinitionData> challenges, final String name) {
        for (ChallengeDefinitionData challenge : challenges) {
            if (challenge.name.equals(name)) {
                return challenge;
            }
        }
        Assertions.fail("Missing challenge " + name);
        return null;
    }

    private int indexOfChallenge(
            final List<ChallengeDefinitionData> challenges, final String name) {
        for (int index = 0; index < challenges.size(); index++) {
            if (challenges.get(index).name.equals(name)) {
                return index;
            }
        }
        Assertions.fail("Missing challenge " + name);
        return -1;
    }
}
