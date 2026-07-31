package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.ChallengerConfig;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.challenges.ChallengeSolutionLink;

public class SolutionNextChallengeChainTest {

    private static final Pattern NEXT_CHALLENGE_PATTERN =
            Pattern.compile("(?m)^next_challenge:\\s*(\\S+)\\s*$");

    @Test
    void solutionNextChallengeFrontMatterMatchesChallengeListOrder() throws IOException {

        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();

        List<String> orderedSolutionPages = orderedDistinctSolutionPages(config);
        List<String> mismatches = new ArrayList<>();

        for (int index = 0; index < orderedSolutionPages.size(); index++) {
            String solutionPage = orderedSolutionPages.get(index);
            String expectedNext =
                    index + 1 < orderedSolutionPages.size()
                            ? orderedSolutionPages.get(index + 1)
                            : "/gui/challenges";

            Path markdownFile = markdownFileFor(solutionPage);
            if (!Files.exists(markdownFile)) {
                mismatches.add(solutionPage + " is missing markdown file " + markdownFile);
                continue;
            }

            Optional<String> actualNext = nextChallengeFrom(markdownFile);
            if (actualNext.isEmpty()) {
                mismatches.add(solutionPage + " is missing next_challenge front matter");
                continue;
            }

            if (!expectedNext.equals(actualNext.get())) {
                mismatches.add(
                        solutionPage
                                + " expected next_challenge "
                                + expectedNext
                                + " but found "
                                + actualNext.get());
            }
        }

        Assertions.assertTrue(
                mismatches.isEmpty(),
                "Solution next_challenge front matter should match the challenge list order:\n"
                        + String.join("\n", mismatches));
    }

    private List<String> orderedDistinctSolutionPages(final ChallengerConfig config) {
        Set<String> orderedPages = new LinkedHashSet<>();

        for (ChallengeDefinitionData challenge : new ChallengeDefinitions(config).getChallenges()) {
            firstSolutionPageFor(challenge).ifPresent(orderedPages::add);
        }

        return new ArrayList<>(orderedPages);
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

    private Path markdownFileFor(final String solutionPage) {
        return contentRoot().resolve(solutionPage.substring(1) + ".md");
    }

    private Path contentRoot() {
        Path moduleRelative = Paths.get("src/main/resources/content");
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }

        return Paths.get("challenger/src/main/resources/content");
    }

    private Optional<String> nextChallengeFrom(final Path markdownFile) throws IOException {
        Matcher matcher = NEXT_CHALLENGE_PATTERN.matcher(Files.readString(markdownFile));
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }
}
