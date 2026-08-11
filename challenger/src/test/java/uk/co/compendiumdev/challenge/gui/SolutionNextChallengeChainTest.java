package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private static final Pattern CHALLENGE_REQUEST_TRUE_PATTERN =
            Pattern.compile("\\bchallenge-request=(\"true\"|'true')", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUMMARY_PATTERN =
            Pattern.compile("\\bsummary=(\"([^\"]+)\"|'([^']+)')", Pattern.CASE_INSENSITIVE);
    private static final Set<String> ALLOWED_CONCEPT_REFERENCE_URLS =
            Set.of(
                    "/reference/web-basics",
                    "/reference/http-basics",
                    "/reference/http-verbs",
                    "/reference/rest-api-basics",
                    "/reference/testing-apis",
                    "/reference/openapi",
                    "/reference/open-api-uis/swagger");
    private static final List<String> LEARNING_SECTION_INLINE_CODE_TERMS =
            List.of(
                    "Accept",
                    "Content-Type",
                    "Content-Disposition",
                    "X-CHALLENGER",
                    "X-AUTH-TOKEN",
                    "X-HTTP-Method-Override",
                    "Authorization",
                    "Accept-Query",
                    "Allow",
                    "application/json",
                    "application/xml",
                    "application/gzip",
                    "application/*+json",
                    "application/problem+json",
                    "application/vnd.api+json",
                    "application/x-www-form-urlencoded",
                    "application/merge-patch+json",
                    "application/json-patch+json",
                    "text/calendar",
                    "text/csv",
                    "text/html",
                    "text/tab-separated-values",
                    "GET",
                    "HEAD",
                    "OPTIONS",
                    "QUERY",
                    "POST",
                    "PUT",
                    "PATCH",
                    "DELETE",
                    "TRACE",
                    "/todos",
                    "/todo",
                    "/heartbeat",
                    "/secret/token",
                    "/secret/note",
                    "/challenges",
                    "/challenger",
                    "?doneStatus=false",
                    "?doneStatus=true",
                    "_limit",
                    "_offset",
                    "_sortBy",
                    "JSON",
                    "XML",
                    "CSV",
                    "TSV",
                    "HTML",
                    "HTTP");

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
                        + " solution page should have one main challenge request:\n"
                        + String.join("\n", pagesWithInvalidWidgets));
    }

    @Test
    void activeSolutionPagesHaveConceptLearnedMetadata() throws IOException {

        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();

        List<String> metadataErrors = new ArrayList<>();
        Map<String, String> learningBulletsSeen = new HashMap<>();

        for (String solutionPage : orderedSolutionPages(config)) {
            Path markdownFile = markdownFileFor(solutionPage);
            String content = Files.readString(markdownFile);

            String conceptsLearned = headerValue(content, "concepts_learned").orElse("");
            if (conceptsLearned.trim().isEmpty()) {
                metadataErrors.add(solutionPage + " missing concepts_learned");
            } else if (conceptsLearned.split("\\|\\|").length < 2) {
                metadataErrors.add(solutionPage + " should list multiple learned concepts");
            }

            String conceptSummary = headerValue(content, "concept_summary").orElse("");
            if (conceptSummary.trim().isEmpty()) {
                metadataErrors.add(solutionPage + " missing concept_summary");
            }

            if (headerValue(content, "lessons_learned").isPresent()) {
                metadataErrors.add(solutionPage + " should keep lessons in markdown content");
            }
            if (headerValue(content, "suggested_experiments").isPresent()) {
                metadataErrors.add(
                        solutionPage + " should keep suggested experiments in markdown content");
            }

            validateMarkdownLearningSection(
                    solutionPage, content, "Lessons Learned", metadataErrors);
            validateMarkdownLearningSection(
                    solutionPage, content, "Suggested Experiments", metadataErrors);
            validateUniqueLearningBullets(
                    solutionPage, content, learningBulletsSeen, metadataErrors);

            if (learningSectionIndex(content, "Lessons Learned")
                    > learningSectionIndex(content, "Suggested Experiments")) {
                metadataErrors.add(
                        solutionPage
                                + " should render Lessons Learned before Suggested Experiments");
            }

            validateConceptReference(
                    solutionPage,
                    content,
                    "concept_reference_label",
                    "concept_reference_url",
                    true,
                    metadataErrors);
            validateConceptReference(
                    solutionPage,
                    content,
                    "concept_reference_label_2",
                    "concept_reference_url_2",
                    false,
                    metadataErrors);

            if (content.contains("concept_reference_url: /tutorials/rest-api-tutorial")
                    || content.contains("concept_reference_url_2: /tutorials/rest-api-tutorial")) {
                metadataErrors.add(solutionPage + " links concept block to interactive tutorial");
            }
        }

        Assertions.assertTrue(
                metadataErrors.isEmpty(),
                "Active solution pages should have valid Concept learned metadata:\n"
                        + String.join("\n", metadataErrors));
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

    private Optional<String> headerValue(final String content, final String headerName) {
        Matcher matcher =
                Pattern.compile("(?m)^" + Pattern.quote(headerName) + ":\\s*(.*)$")
                        .matcher(content);
        if (matcher.find()) {
            return Optional.of(matcher.group(1).trim());
        }
        return Optional.empty();
    }

    private void validateMarkdownLearningSection(
            final String solutionPage,
            final String content,
            final String heading,
            final List<String> metadataErrors) {

        final Optional<String> section = markdownSection(content, heading);
        if (section.isEmpty()) {
            metadataErrors.add(solutionPage + " missing markdown section ## " + heading);
            return;
        }

        final long bulletCount =
                section.get().lines().filter(line -> line.startsWith("- ")).count();
        if (bulletCount < 2) {
            metadataErrors.add(
                    solutionPage
                            + " should list multiple bullets in markdown section ## "
                            + heading);
        }

        section.get()
                .lines()
                .filter(line -> line.startsWith("- "))
                .forEach(
                        line -> {
                            final long inlineCodeDelimiterCount =
                                    line.chars().filter(character -> character == '`').count();
                            if (inlineCodeDelimiterCount % 2 != 0) {
                                metadataErrors.add(
                                        solutionPage
                                                + " has unbalanced inline code in ## "
                                                + heading
                                                + ": "
                                                + line);
                            }

                            final String proseWithoutCodeSpans = line.replaceAll("`[^`]*`", "");
                            for (String term : LEARNING_SECTION_INLINE_CODE_TERMS) {
                                if (proseWithoutCodeSpans.contains(term)) {
                                    metadataErrors.add(
                                            solutionPage
                                                    + " should code-format "
                                                    + term
                                                    + " in ## "
                                                    + heading
                                                    + ": "
                                                    + line);
                                    break;
                                }
                            }
                        });
    }

    private void validateUniqueLearningBullets(
            final String solutionPage,
            final String content,
            final Map<String, String> learningBulletsSeen,
            final List<String> metadataErrors) {

        String currentHeading = "";
        for (String line : markdownBody(content).lines().toList()) {
            if ("## Lessons Learned".equals(line)) {
                currentHeading = "Lessons Learned";
                continue;
            }
            if ("## Suggested Experiments".equals(line)) {
                currentHeading = "Suggested Experiments";
                continue;
            }
            if (!currentHeading.isEmpty() && line.startsWith("## ")) {
                currentHeading = "";
                continue;
            }
            if (currentHeading.isEmpty() || !line.startsWith("- ")) {
                continue;
            }

            final String duplicateKey = currentHeading + "\t" + line.trim();
            final String previousSolution =
                    learningBulletsSeen.putIfAbsent(duplicateKey, solutionPage);
            if (previousSolution != null) {
                metadataErrors.add(
                        solutionPage
                                + " repeats "
                                + currentHeading
                                + " bullet from "
                                + previousSolution
                                + ": "
                                + line.trim());
            }
        }
    }

    private int learningSectionIndex(final String content, final String heading) {
        return markdownBody(content).indexOf("## " + heading);
    }

    private Optional<String> markdownSection(final String content, final String heading) {
        final Matcher matcher =
                Pattern.compile(
                                "(?ms)^## "
                                        + Pattern.quote(heading)
                                        + "\\s*$([\\s\\S]*?)(?=^##\\s+|\\z)")
                        .matcher(markdownBody(content));
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    private String markdownBody(final String content) {
        final Matcher matcher = Pattern.compile("(?s)\\A---\\R.*?\\R---\\R?").matcher(content);
        if (matcher.find()) {
            return content.substring(matcher.end());
        }
        return content;
    }

    private void validateConceptReference(
            final String solutionPage,
            final String content,
            final String labelKey,
            final String urlKey,
            final boolean required,
            final List<String> metadataErrors) {

        final Optional<String> label = headerValue(content, labelKey);
        final Optional<String> url = headerValue(content, urlKey);

        if (required && (label.isEmpty() || label.get().isBlank())) {
            metadataErrors.add(solutionPage + " missing " + labelKey);
        }
        if (required && (url.isEmpty() || url.get().isBlank())) {
            metadataErrors.add(solutionPage + " missing " + urlKey);
        }

        if (label.isPresent() != url.isPresent()) {
            metadataErrors.add(solutionPage + " has incomplete " + labelKey + "/" + urlKey);
            return;
        }

        if (url.isEmpty()) {
            return;
        }

        if (!ALLOWED_CONCEPT_REFERENCE_URLS.contains(url.get())) {
            metadataErrors.add(solutionPage + " has unsupported reference URL " + url.get());
        }

        final Path referencePage = contentRoot().resolve(url.get().substring(1) + ".md");
        if (!Files.exists(referencePage)) {
            metadataErrors.add(solutionPage + " reference URL does not exist: " + url.get());
        }
    }

    private Optional<String> validateApiLiveRequestWidgets(final Path path, final String content) {
        int widgetCount = 0;
        int mainWidgetCount = 0;
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
            if (OPEN_TRUE_PATTERN.matcher(attributes).find()
                    || CHALLENGE_REQUEST_TRUE_PATTERN.matcher(attributes).find()) {
                mainWidgetCount++;
            }
        }

        if (widgetCount == 0) {
            return Optional.empty();
        }

        if (mainWidgetCount != 1) {
            errors.add(
                    "expected one open=true or challenge-request=true main widget but found "
                            + mainWidgetCount);
        }

        if (errors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(path + ": " + String.join(", ", errors));
    }
}
