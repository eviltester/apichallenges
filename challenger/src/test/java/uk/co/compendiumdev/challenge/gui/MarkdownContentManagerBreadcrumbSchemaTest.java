package uk.co.compendiumdev.challenge.gui;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class MarkdownContentManagerBreadcrumbSchemaTest {

    @Test
    void homepageDoesNotEmitIndexBreadcrumbUrl() {

        final MarkdownContentManager contentManager = contentManager();
        final String html =
                contentManager.getHtmlVersionOfMarkdownContent("site", "/index", Map.of());

        Assertions.assertFalse(html.contains("\"@type\":\"BreadcrumbList\""));
        Assertions.assertFalse(html.contains("https://apichallenges.eviltester.com/index"));
    }

    @Test
    void authorPageBreadcrumbSkipsMissingAuthorIndexPage() {

        final MarkdownContentManager contentManager = contentManager();
        final String html =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/author/alan-richardson", Map.of());

        Assertions.assertTrue(html.contains("\"@type\":\"BreadcrumbList\""));
        Assertions.assertFalse(
                html.contains("\"item\":\"https://apichallenges.eviltester.com/author\""));
        Assertions.assertTrue(
                html.contains(
                        "\"item\":\"https://apichallenges.eviltester.com/author/alan-richardson\""));
    }

    @Test
    void solutionPageBreadcrumbSkipsMissingSectionFolderPages() {

        final MarkdownContentManager contentManager = contentManager();
        final String html =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/apichallenges/solutions/get/get-todos-200", Map.of());

        Assertions.assertTrue(html.contains("\"@type\":\"BreadcrumbList\""));
        Assertions.assertTrue(
                html.contains("\"item\":\"https://apichallenges.eviltester.com/apichallenges\""));
        Assertions.assertTrue(
                html.contains(
                        "\"item\":\"https://apichallenges.eviltester.com/apichallenges/solutions\""));
        Assertions.assertFalse(
                html.contains(
                        "\"item\":\"https://apichallenges.eviltester.com/apichallenges/solutions/get\""));
        Assertions.assertTrue(
                html.contains(
                        "\"item\":\"https://apichallenges.eviltester.com/apichallenges/solutions/get/get-todos-200\""));
    }

    private MarkdownContentManager contentManager() {
        final ResourceContentScanner contentScanner = new ResourceContentScanner();
        final List<String> pathsToFileContent =
                contentScanner.scanForFullPathsOfExtensionsIn("content/", "md");
        return new MarkdownContentManager(pathsToFileContent, new DefaultGUIHTML(), null);
    }
}
