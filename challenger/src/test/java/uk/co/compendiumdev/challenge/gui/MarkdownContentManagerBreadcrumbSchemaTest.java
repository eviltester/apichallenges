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
        Assertions.assertFalse(html.contains("https://apichallenges.com/index"));
    }

    @Test
    void authorPageBreadcrumbSkipsMissingAuthorIndexPage() {

        final MarkdownContentManager contentManager = contentManager();
        final String html =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/author/alan-richardson", Map.of());

        Assertions.assertTrue(html.contains("\"@type\":\"BreadcrumbList\""));
        Assertions.assertFalse(html.contains("\"item\":\"https://apichallenges.com/author\""));
        Assertions.assertTrue(
                html.contains("\"item\":\"https://apichallenges.com/author/alan-richardson\""));
    }

    @Test
    void solutionPageBreadcrumbSkipsMissingSectionFolderPages() {

        final MarkdownContentManager contentManager = contentManager();
        final String html =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/apichallenges/solutions/get/get-todos-200", Map.of());

        Assertions.assertTrue(html.contains("\"@type\":\"BreadcrumbList\""));
        Assertions.assertTrue(
                html.contains("\"item\":\"https://apichallenges.com/apichallenges\""));
        Assertions.assertTrue(
                html.contains("\"item\":\"https://apichallenges.com/apichallenges/solutions\""));
        Assertions.assertFalse(
                html.contains(
                        "\"item\":\"https://apichallenges.com/apichallenges/solutions/get\""));
        Assertions.assertTrue(
                html.contains(
                        "\"item\":\"https://apichallenges.com/apichallenges/solutions/get/get-todos-200\""));
    }

    @Test
    void sectionIndexPagesLeadBreadcrumbsForSubPages() {

        final MarkdownContentManager contentManager = contentManager();

        final String referenceHtml =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/reference/http-basics", Map.of());
        Assertions.assertTrue(referenceHtml.contains("<a href=\"/reference\">reference</a>"));
        Assertions.assertTrue(
                referenceHtml.contains("\"item\":\"https://apichallenges.com/reference\""));

        final String tutorialsHtml =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/tutorials/rest-api-testing", Map.of());
        Assertions.assertTrue(tutorialsHtml.contains("<a href=\"/tutorials\">tutorials</a>"));
        Assertions.assertTrue(
                tutorialsHtml.contains("\"item\":\"https://apichallenges.com/tutorials\""));

        final String practiceModesHtml =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/practice-modes/simpleapi", Map.of());
        Assertions.assertTrue(
                practiceModesHtml.contains("<a href=\"/practice-modes\">practice-modes</a>"));
        Assertions.assertTrue(
                practiceModesHtml.contains(
                        "\"item\":\"https://apichallenges.com/practice-modes\""));

        final String toolsHtml =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/tools/clients/bruno", Map.of());
        Assertions.assertTrue(toolsHtml.contains("<a href=\"/tools\">tools</a>"));
        Assertions.assertTrue(toolsHtml.contains("<a href=\"/tools/clients\">clients</a>"));
        Assertions.assertTrue(toolsHtml.contains("\"item\":\"https://apichallenges.com/tools\""));

        final String practiceSitesHtml =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/practice-sites/httpbin", Map.of());
        Assertions.assertTrue(
                practiceSitesHtml.contains("<a href=\"/practice-sites\">practice-sites</a>"));
        Assertions.assertTrue(
                practiceSitesHtml.contains(
                        "\"item\":\"https://apichallenges.com/practice-sites\""));
    }

    @Test
    void onlineOpenApiUiToolAboutPagesUseToolAsBreadcrumbParent() {

        final MarkdownContentManager contentManager = contentManager();
        final String html =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/tools/online-clients/openapi-explorer/about", Map.of());

        assertOpenApiUiToolAboutBreadcrumb(html, "OpenAPI Explorer UI", "openapi-explorer");
    }

    @Test
    void onlineOpenApiUiToolPagesUseToolsAsBreadcrumbParent() {

        final MarkdownContentManager contentManager = contentManager();
        final String html =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content", "/tools/online-clients/scalar", Map.of());

        assertOnlineOpenApiUiToolBreadcrumb(html, "Scalar", "/tools/online-clients/scalar");
    }

    private void assertOpenApiUiToolAboutBreadcrumb(
            final String html, final String toolName, final String clientPath) {

        Assertions.assertTrue(
                html.contains("<nav class=\"breadcrumb\" aria-label=\"Breadcrumb\">"));
        Assertions.assertTrue(html.contains("<li><a href=\"/tools\">Tools</a></li>"));
        Assertions.assertTrue(
                html.contains("<li><a href=\"/tools/online-clients\">Online Clients</a></li>"));
        Assertions.assertTrue(
                html.contains(
                        "<li><a href=\"/tools/online-clients/"
                                + clientPath
                                + "\">"
                                + toolName
                                + "</a></li>"));
        Assertions.assertTrue(html.contains("<li aria-current=\"page\">About</li>"));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":2,\"name\":\"Tools\",\"item\":\"https://apichallenges.com/tools\""));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":3,\"name\":\"Online Clients\",\"item\":\"https://apichallenges.com/tools/online-clients\""));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":4,\"name\":\""
                                + toolName
                                + "\",\"item\":\"https://apichallenges.com/tools/online-clients/"
                                + clientPath
                                + "\""));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":5,\"name\":\"About\",\"item\":\"https://apichallenges.com/tools/online-clients/"
                                + clientPath
                                + "/about"
                                + "\""));
    }

    private void assertOnlineOpenApiUiToolBreadcrumb(
            final String html, final String toolName, final String currentPath) {

        Assertions.assertTrue(
                html.contains("<nav class=\"breadcrumb\" aria-label=\"Breadcrumb\">"));
        Assertions.assertTrue(html.contains("<li><a href=\"/tools\">Tools</a></li>"));
        Assertions.assertTrue(
                html.contains("<li><a href=\"/tools/online-clients\">Online Clients</a></li>"));
        Assertions.assertTrue(html.contains("<li aria-current=\"page\">" + toolName + "</li>"));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":2,\"name\":\"Tools\",\"item\":\"https://apichallenges.com/tools\""));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":3,\"name\":\"Online Clients\",\"item\":\"https://apichallenges.com/tools/online-clients\""));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":4,\"name\":\""
                                + toolName
                                + "\",\"item\":\"https://apichallenges.com"
                                + currentPath
                                + "\""));
    }

    private MarkdownContentManager contentManager() {
        final ResourceContentScanner contentScanner = new ResourceContentScanner();
        final List<String> pathsToFileContent =
                contentScanner.scanForFullPathsOfExtensionsIn("content/", "md");
        return new MarkdownContentManager(pathsToFileContent, new DefaultGUIHTML(), null);
    }
}
