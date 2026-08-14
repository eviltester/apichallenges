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
    void advancedAcceptChallengesAreInTheirOwnSectionBeforeContentType() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();
        ChallengeDefinitions definitions = new ChallengeDefinitions(config);

        List<ChallengeDefinitionData> challenges = new ArrayList<>(definitions.getChallenges());
        ChallengeSection advancedAccept =
                sectionNamed(definitions.getChallengeSections(), "Advanced Accept Challenges");

        List<String> expectedNames =
                List.of(
                        "GET /todos (200) q XML preferred",
                        "GET /todos (200) q JSON preferred",
                        "GET /todos (406) q rejects all",
                        "GET /todos (406) unsupported +json",
                        "GET /todos (200) text/xml",
                        "GET /todos (200) vendor XML",
                        "GET /todos (200) structured XML wildcard");

        Assertions.assertEquals(
                expectedNames,
                advancedAccept.getChallenges().stream().map(challenge -> challenge.name).toList());
        Assertions.assertTrue(
                indexOfChallenge(challenges, "GET /todos/{id} (200) text/calendar")
                        < indexOfChallenge(challenges, "GET /todos (200) q XML preferred"));
        Assertions.assertTrue(
                indexOfChallenge(challenges, "GET /todos (200) structured XML wildcard")
                        < indexOfChallenge(challenges, "POST /todos XML"));

        Assertions.assertEquals(
                CHALLENGE.GET_ACCEPT_XML_Q_PREFERRED,
                definitions.getChallenge("GET /todos (200) q XML preferred"));
        Assertions.assertEquals(
                CHALLENGE.GET_ACCEPT_JSON_Q_PREFERRED,
                definitions.getChallenge("GET /todos (200) q JSON preferred"));
        Assertions.assertEquals(
                CHALLENGE.GET_ACCEPT_Q_REJECTS_ALL_406,
                definitions.getChallenge("GET /todos (406) q rejects all"));
        Assertions.assertEquals(
                CHALLENGE.GET_UNSUPPORTED_STRUCTURED_JSON_ACCEPT_406,
                definitions.getChallenge("GET /todos (406) unsupported +json"));
        Assertions.assertEquals(
                CHALLENGE.GET_ACCEPT_TEXT_XML,
                definitions.getChallenge("GET /todos (200) text/xml"));
        Assertions.assertEquals(
                CHALLENGE.GET_ACCEPT_VENDOR_XML,
                definitions.getChallenge("GET /todos (200) vendor XML"));
        Assertions.assertEquals(
                CHALLENGE.GET_ACCEPT_STRUCTURED_XML_WILDCARD,
                definitions.getChallenge("GET /todos (200) structured XML wildcard"));

        assertChallengeMentionsAndLinks(
                challengeNamed(challenges, "GET /todos (200) q XML preferred"),
                "application/json;q=0.5, application/xml;q=1",
                "/apichallenges/solutions/accept-header/get-todos-200-q-xml-preferred");
        assertChallengeMentionsAndLinks(
                challengeNamed(challenges, "GET /todos (406) unsupported +json"),
                "application/problem+json",
                "/apichallenges/solutions/accept-header/get-todos-406-unsupported-json-suffix");
        assertChallengeMentionsAndLinks(
                challengeNamed(challenges, "GET /todos (200) vendor XML"),
                "application/vnd.apichallenges.todo+xml",
                "/apichallenges/solutions/accept-header/get-todos-200-vendor-xml");
        assertChallengeMentionsAndLinks(
                challengeNamed(challenges, "GET /todos (200) structured XML wildcard"),
                "application/*+xml",
                "/apichallenges/solutions/accept-header/get-todos-200-structured-xml-wildcard");
    }

    @Test
    void vendorXmlContentTypeChallengeIsBeforeUnsupportedContentType() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();
        ChallengeDefinitions definitions = new ChallengeDefinitions(config);

        List<ChallengeDefinitionData> challenges = new ArrayList<>(definitions.getChallenges());
        ChallengeDefinitionData vendorXmlChallenge =
                challengeNamed(challenges, "POST /todos vendor XML");

        Assertions.assertEquals(
                CHALLENGE.POST_CREATE_VENDOR_XML,
                definitions.getChallenge("POST /todos vendor XML"));
        Assertions.assertTrue(
                indexOfChallenge(challenges, "POST /todos JSON")
                        < indexOfChallenge(challenges, "POST /todos vendor XML"));
        Assertions.assertTrue(
                indexOfChallenge(challenges, "POST /todos vendor XML")
                        < indexOfChallenge(challenges, "POST /todos (415)"));
        assertChallengeMentionsAndLinks(
                vendorXmlChallenge,
                "application/vnd.apichallenges.todo+xml",
                "/apichallenges/solutions/content-type-header/post-todos-vendor-xml");
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
                                                "application/vnd.thingifier.query+json")));
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

    private void assertChallengeMentionsAndLinks(
            final ChallengeDefinitionData challenge,
            final String expectedHintText,
            final String expectedSolutionLink) {

        Assertions.assertTrue(
                challenge.hints.stream().anyMatch(hint -> hint.hintText.contains(expectedHintText)),
                challenge.name + " should mention " + expectedHintText + " in a hint");
        Assertions.assertTrue(
                challenge.solutions.stream()
                        .anyMatch(solution -> solution.linkData.equals(expectedSolutionLink)),
                challenge.name + " should link to " + expectedSolutionLink);
    }

    private ChallengeSection sectionNamed(
            final Collection<ChallengeSection> sections, final String sectionTitle) {
        for (ChallengeSection section : sections) {
            if (section.getTitle().equals(sectionTitle)) {
                return section;
            }
        }
        Assertions.fail("Missing challenge section " + sectionTitle);
        return null;
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
