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
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.ChallengerConfig;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.challenges.ChallengeSection;
import uk.co.compendiumdev.challenge.challenges.ChallengeSolutionLink;

public class SolutionNextChallengeChainTest {

    private static final Pattern NEXT_CHALLENGE_PATTERN =
            Pattern.compile("(?m)^next_challenge:\\s*(\\S+)\\s*$");
    private static final Pattern API_LIVE_REQUEST_PATTERN =
            Pattern.compile("\\{\\{<api-live-request\\s+([\\s\\S]*?)>}}");
    private static final Pattern DETAILS_TRUE_PATTERN =
            Pattern.compile("\\bdetails=(\"true\"|'true')", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPEN_TRUE_PATTERN =
            Pattern.compile("\\bopen=(\"true\"|'true')", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUMMARY_PATTERN =
            Pattern.compile("\\bsummary=(\"([^\"]+)\"|'([^']+)')", Pattern.CASE_INSENSITIVE);

    @Test
    void solutionNextChallengeFrontMatterMatchesChallengeListOrder() throws IOException {

        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();

        List<String> orderedSolutionPages = orderedSolutionPages(config);
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

    @Test
    void everyChallengeHasItsOwnSolutionPageAcrossActiveModes() {
        List<ChallengerConfig> configs = new ArrayList<>();

        configs.add(new ChallengerConfig());

        ChallengerConfig multiPlayerNoStorage = new ChallengerConfig();
        multiPlayerNoStorage.setToMultiPlayerMode();
        multiPlayerNoStorage.setToNoPersistenceMode();
        configs.add(multiPlayerNoStorage);

        ChallengerConfig multiPlayerLocalStorage = new ChallengerConfig();
        multiPlayerLocalStorage.setToMultiPlayerMode();
        configs.add(multiPlayerLocalStorage);

        List<String> duplicatePages = new ArrayList<>();
        for (ChallengerConfig config : configs) {
            Set<String> pagesSeen = new LinkedHashSet<>();
            for (String solutionPage : orderedSolutionPages(config)) {
                if (!pagesSeen.add(solutionPage)) {
                    duplicatePages.add(solutionPage);
                }
            }
        }

        Assertions.assertTrue(
                duplicatePages.isEmpty(),
                "Each challenge should link to its own solution page. Duplicate pages: "
                        + String.join(", ", duplicatePages));
    }

    @Test
    void solutionIndexMatchesChallengeSectionsAndOrder() throws IOException {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();

        Assertions.assertEquals(
                String.join("\n", expectedSolutionIndexSectionsAndLinks(config)),
                String.join("\n", actualSolutionIndexSectionsAndLinks()),
                "The solution index should use the same sections, order, names, and solution pages"
                        + " as the challenge list.");
    }

    @Test
    void solutionPagesUseDetailsSummariesForApiLiveRequestWidgets() throws IOException {
        List<String> pagesWithInvalidWidgets = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(contentRoot().resolve("apichallenges/solutions"))) {
            paths.filter(path -> path.toString().endsWith(".md"))
                    .forEach(
                            path -> {
                                try {
                                    String content = Files.readString(path);
                                    validateApiLiveRequestWidgets(path, content)
                                            .ifPresent(pagesWithInvalidWidgets::add);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        }

        Assertions.assertTrue(
                pagesWithInvalidWidgets.isEmpty(),
                "Each solution API live request should have a semantic details summary, and each"
                        + " solution page should have one default-open main request:\n"
                        + String.join("\n", pagesWithInvalidWidgets));
    }

    private List<String> orderedSolutionPages(final ChallengerConfig config) {
        List<String> orderedPages = new ArrayList<>();

        for (ChallengeDefinitionData challenge : new ChallengeDefinitions(config).getChallenges()) {
            firstSolutionPageFor(challenge).ifPresent(orderedPages::add);
        }

        return orderedPages;
    }

    private List<String> expectedSolutionIndexSectionsAndLinks(final ChallengerConfig config) {
        List<String> indexEntries = new ArrayList<>();

        for (ChallengeSection section : new ChallengeDefinitions(config).getChallengeSections()) {
            List<String> sectionEntries = new ArrayList<>();
            for (ChallengeDefinitionData challenge : section.getChallenges()) {
                firstSolutionPageFor(challenge)
                        .ifPresent(
                                solutionPage ->
                                        sectionEntries.add(
                                                "- ["
                                                        + challenge.name
                                                        + "]("
                                                        + solutionPage
                                                        + ")"));
            }

            if (!sectionEntries.isEmpty()) {
                indexEntries.add("## " + section.getTitle());
                indexEntries.addAll(sectionEntries);
            }
        }

        return indexEntries;
    }

    private List<String> actualSolutionIndexSectionsAndLinks() throws IOException {
        List<String> indexEntries = new ArrayList<>();
        boolean foundFirstSection = false;

        for (String line :
                Files.readAllLines(contentRoot().resolve("apichallenges/solutions.md"))) {
            if (line.startsWith("## ")) {
                foundFirstSection = true;
                indexEntries.add(line);
            } else if (foundFirstSection
                    && line.startsWith("- [")
                    && line.contains("](/apichallenges/solutions/")) {
                indexEntries.add(line);
            }
        }

        return indexEntries;
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

    private Optional<String> validateApiLiveRequestWidgets(final Path path, final String content) {
        int widgetCount = 0;
        int openWidgetCount = 0;
        List<String> errors = new ArrayList<>();
        Matcher matcher = API_LIVE_REQUEST_PATTERN.matcher(content);
        while (matcher.find()) {
            widgetCount++;
            final String attributes = matcher.group(1);
            if (!DETAILS_TRUE_PATTERN.matcher(attributes).find()) {
                errors.add("missing details=true");
            }
            Matcher summaryMatcher = SUMMARY_PATTERN.matcher(attributes);
            if (!summaryMatcher.find()) {
                errors.add("missing summary");
            } else {
                String summary =
                        summaryMatcher.group(2) == null
                                ? summaryMatcher.group(3)
                                : summaryMatcher.group(2);
                if (summary.contains("Main request:")) {
                    errors.add("summary uses generic Main request label");
                }
                if (summary.contains("{{") || summary.contains("}}")) {
                    errors.add("summary exposes template placeholder");
                }
            }
            if (OPEN_TRUE_PATTERN.matcher(attributes).find()) {
                openWidgetCount++;
            }
        }

        if (widgetCount == 0) {
            return Optional.empty();
        }

        if (openWidgetCount != 1) {
            errors.add("expected one open=true main widget but found " + openWidgetCount);
        }

        if (errors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(path + ": " + String.join(", ", errors));
    }
}
