package uk.co.compendiumdev.challenge.gui;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.ChallengerConfig;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.challenges.ChallengeSolutionLink;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class MarkdownContentManagerChallengeStatusTest {

    @Test
    void solutionPagesAssociateOnlyTheSolvingLiveRequestWithTheChallengeId() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();
        ChallengeDefinitions challengeDefinitions = new ChallengeDefinitions(config);

        ResourceContentScanner contentScanner = new ResourceContentScanner();
        List<String> pathsToFileContent =
                contentScanner.scanForFullPathsOfExtensionsIn("content/", "md");
        MarkdownContentManager contentManager =
                new MarkdownContentManager(
                        pathsToFileContent, new DefaultGUIHTML(), challengeDefinitions);

        for (ChallengeDefinitionData challenge : challengeDefinitions.getChallenges()) {
            firstSolutionPageFor(challenge)
                    .ifPresent(
                            solutionPage -> {
                                String html =
                                        contentManager.getResourceMarkdownFileAsHtml(
                                                "content", solutionPage, Map.of());
                                String expectedChallengeId =
                                        "data-challenge-id=\"" + challenge.id + "\"";

                                Assertions.assertEquals(
                                        2,
                                        countOccurrences(html, "data-challenge-id="),
                                        solutionPage
                                                + " should mark the page banner and the solving"
                                                + " API live request");
                                Assertions.assertEquals(
                                        1,
                                        countMatches(
                                                html,
                                                "<div class=\"api-live-request\"[^>]*"
                                                        + Pattern.quote(expectedChallengeId)),
                                        solutionPage
                                                + " should mark only the solving API live request");
                                Assertions.assertTrue(
                                        html.contains(
                                                "<aside class=\"solution-challenge-completed\" "
                                                        + expectedChallengeId),
                                        solutionPage + " should render a hidden completed banner");
                                Assertions.assertTrue(
                                        html.indexOf("solution-challenge-completed")
                                                < html.indexOf("<h1"),
                                        solutionPage
                                                + " should render the completed banner above the"
                                                + " H1");
                                Assertions.assertTrue(
                                        html.contains(expectedChallengeId),
                                        solutionPage + " should use challenge id " + challenge.id);
                            });
        }
    }

    private Optional<String> firstSolutionPageFor(final ChallengeDefinitionData challenge) {
        for (ChallengeSolutionLink solution : challenge.solutions) {
            if ("HREF".equals(solution.linkType)
                    && solution.linkData.startsWith("/apichallenges/solutions/")) {
                return Optional.of(solution.linkData);
            }
        }

        return Optional.empty();
    }

    private int countOccurrences(final String value, final String substring) {
        return value.split(Pattern.quote(substring), -1).length - 1;
    }

    private int countMatches(final String value, final String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(value);
        int matches = 0;
        while (matcher.find()) {
            matches++;
        }
        return matches;
    }
}
