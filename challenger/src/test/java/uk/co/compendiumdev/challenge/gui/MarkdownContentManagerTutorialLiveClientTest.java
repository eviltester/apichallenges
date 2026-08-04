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
        Assertions.assertEquals(13, countOccurrences(html, "data-allowed-path-prefixes=\"/simpleapi\""));
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
                html.contains(
                        "Updated <time datetime='2026-08-04'>2026-08-04</time>"));
        Assertions.assertTrue(
                html.contains("<nav class='side-toc' aria-label='Learning and reference links'>"));
    }

    private String renderRestApiTestingTutorial() {
        ResourceContentScanner contentScanner = new ResourceContentScanner();
        List<String> pathsToFileContent =
                contentScanner.scanForFullPathsOfExtensionsIn("content/", "md");
        MarkdownContentManager contentManager =
                new MarkdownContentManager(pathsToFileContent, new DefaultGUIHTML(), null);

        return contentManager.getResourceMarkdownFileAsHtml(
                "content",
                "/tutorials/rest-api-testing",
                Map.of(
                        "ORIGIN_URL",
                        "http://localhost:4567",
                        "HOST_URL",
                        "localhost:4567"));
    }

    private int countOccurrences(final String value, final String substring) {
        return value.split(java.util.regex.Pattern.quote(substring), -1).length - 1;
    }
}
