package uk.co.compendiumdev.challenge.gui;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class MarkdownContentManagerTutorialLiveClientTest {

    @Test
    void restApiTestingTutorialRendersSimpleApiLiveClients() {

        String html = renderRestApiTestingTutorial();

        Assertions.assertEquals(13, countOccurrences(html, "class=\"api-live-request\""));
        Assertions.assertEquals(13, countOccurrences(html, "data-use-challenger=\"false\""));
        Assertions.assertEquals(
                13, countOccurrences(html, "data-allowed-path-prefixes=\"/simpleapi\""));
        Assertions.assertEquals(12, countOccurrences(html, "data-query-editable=\"false\""));
        Assertions.assertEquals(7, countOccurrences(html, "data-body-editable=\"false\""));
        Assertions.assertEquals(1, countOccurrences(html, "data-edit-mode=\"adhoc\""));
        Assertions.assertTrue(html.contains("data-path=\"/simpleapi/items\""));
        Assertions.assertTrue(html.contains("data-path=\"/simpleapi/randomisbn\""));
        Assertions.assertTrue(
                html.contains("data-path=\"/simpleapi/items/{{lastCreatedSimpleApiItemId}}\""));
        Assertions.assertTrue(html.contains("{{randomSimpleApiIsbn}}"));
        Assertions.assertTrue(html.contains("{{lastCreatedSimpleApiItemIsbn}}"));
    }

    @Test
    void restApiTestingTutorialRendersMainContentBeforeSidebarLinks() {

        String html = renderRestApiTestingTutorial();

        final int section = html.indexOf("<section class='doc-columns'>");
        final int rightColumn = html.indexOf("<div class='right-column'>");
        final int main = html.indexOf("<main id='maincontentstartshere'>");
        final int h1 = html.indexOf("<h1>How to Test REST APIs Step by Step</h1>");
        final int byline = html.indexOf("<p class='article-byline'>");
        final int toc = html.indexOf("<div id='toc'>");
        final int leftColumn = html.indexOf("<aside class='left-column'");
        final int sideToc = html.indexOf("<nav class='side-toc'");

        Assertions.assertTrue(section >= 0);
        Assertions.assertTrue(section < rightColumn);
        Assertions.assertTrue(rightColumn < main);
        Assertions.assertTrue(main < h1);
        Assertions.assertTrue(h1 < byline);
        Assertions.assertTrue(byline < toc);
        Assertions.assertTrue(h1 < leftColumn);
        Assertions.assertTrue(leftColumn < sideToc);
        Assertions.assertTrue(
                html.contains(
                        "<a href='/author/alan-richardson' rel='author'>Alan Richardson</a>"));
        Assertions.assertTrue(
                html.contains("Updated <time datetime='2026-08-04'>2026-08-04</time>"));
        Assertions.assertTrue(
                html.contains("<nav class='side-toc' aria-label='Learning and reference links'>"));
    }

    @Test
    void sideTocRendersCollapsibleSectionsWithDescriptions() {

        String html = renderRestApiTestingTutorial();

        Assertions.assertTrue(
                html.contains(
                        "<details class=\"side-toc-section\" data-side-toc-section=\"tools\">"));
        Assertions.assertTrue(
                html.contains(
                        "<details class=\"side-toc-section\" data-side-toc-section=\"reference-tutorials\">"));
        Assertions.assertTrue(html.contains("<span class=\"side-toc-section-title\">Tools</span>"));
        Assertions.assertTrue(
                html.contains(
                        "Tool reviews for REST HTTP clients, proxies, and online API clients to help with API testing."));
        Assertions.assertTrue(
                html.contains(
                        "Reference tutorials covering HTTP basics, REST APIs, OpenAPI, Swagger, and practical API testing concepts."));
        assertContainsInOrder(
                html,
                "<a href=\"/tools/clients\">REST/HTTP Clients</a>",
                "<li><a href=\"/tools/proxies\">Proxies</a></li>",
                "Online Clients",
                "<li><a href=\"/tools/online-clients/basic-client\">Basic Client</a></li>",
                "<li><a href=\"/tools/online-clients/swagger\">Swagger</a></li>");
    }

    @Test
    void onlineClientContentPagesRenderToolsAndSidebarLinks() {

        String html = renderContentPage("/tools/online-clients/basic-client");

        Assertions.assertTrue(
                html.contains(
                        "<title>Free Online REST API Client for Testing HTTP Requests</title>"));
        Assertions.assertTrue(html.contains("class=\"sim-live-request\""));
        Assertions.assertTrue(html.contains("data-custom-method=\"true\""));
        Assertions.assertTrue(html.contains("data-body-methods=\"all\""));
        Assertions.assertFalse(html.contains("data-allowed-path-prefixes"));
        Assertions.assertTrue(html.contains("limited by CORS"));
        Assertions.assertTrue(html.contains("Use Browser Dev Tools To Help Test REST APIs"));
        Assertions.assertTrue(html.contains("HAR file"));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/basic-client\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/rest-api-testing\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simulation\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/http-basics\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/http-verbs\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/rest-api-basics\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/summary-reviews\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/bruno\""));
        Assertions.assertTrue(html.contains("href=\"/tools/proxies\""));

        html = renderContentPage("/tools/online-clients/swagger");

        Assertions.assertTrue(
                html.contains(
                        "<title>Online Swagger UI: Open OpenAPI Files from URL or Disk</title>"));
        Assertions.assertTrue(html.contains("data-online-swagger-client"));
        Assertions.assertTrue(html.contains("data-openapi-url"));
        Assertions.assertTrue(html.contains("data-openapi-file"));
        Assertions.assertTrue(html.contains("https://unpkg.com/swagger-ui-dist"));
        Assertions.assertTrue(html.contains("https://cdn.jsdelivr.net/npm/js-yaml@4"));
        Assertions.assertTrue(html.contains("src=\"/js/online-swagger-client.js\""));
        Assertions.assertTrue(
                html.contains("Open OpenAPI And Swagger Files From URL Or Disk"));
        Assertions.assertTrue(html.contains("How To Use Swagger UI For REST API Testing"));
        Assertions.assertTrue(html.contains("CORS Limits For Browser Swagger UI"));
        Assertions.assertTrue(html.contains("When To Use A REST Client Instead Of Swagger UI"));
        Assertions.assertTrue(html.contains("less-validating, permissive file"));
        Assertions.assertTrue(html.contains("limited by CORS"));
        Assertions.assertTrue(html.contains("href=\"/tutorials/swagger\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/rest-api-testing\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simulation\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/basic-client\""));
        Assertions.assertTrue(html.contains("href=\"/apichallenges/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simpleapi-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/shoppingcart-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/summary-reviews\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/insomnia\""));
        Assertions.assertTrue(html.contains("href=\"/tools/proxies\""));
    }

    private String renderRestApiTestingTutorial() {
        return renderContentPage("/tutorials/rest-api-testing");
    }

    private String renderContentPage(final String contentPath) {
        ResourceContentScanner contentScanner = new ResourceContentScanner();
        List<String> pathsToFileContent =
                contentScanner.scanForFullPathsOfExtensionsIn("content/", "md");
        MarkdownContentManager contentManager =
                new MarkdownContentManager(pathsToFileContent, new DefaultGUIHTML(), null);

        return contentManager.getResourceMarkdownFileAsHtml(
                "content",
                contentPath,
                Map.of("ORIGIN_URL", "http://localhost:4567", "HOST_URL", "localhost:4567"));
    }

    private int countOccurrences(final String value, final String substring) {
        return value.split(java.util.regex.Pattern.quote(substring), -1).length - 1;
    }

    private void assertContainsInOrder(final String value, final String... substrings) {
        int previousIndex = -1;
        for (String substring : substrings) {
            final int index = value.indexOf(substring);
            Assertions.assertTrue(index >= 0, "Missing expected text: " + substring);
            Assertions.assertTrue(
                    index > previousIndex,
                    "Expected text to appear later than previous entry: " + substring);
            previousIndex = index;
        }
    }
}
