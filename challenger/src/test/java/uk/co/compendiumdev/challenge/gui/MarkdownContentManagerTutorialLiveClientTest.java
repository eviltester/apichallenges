package uk.co.compendiumdev.challenge.gui;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class MarkdownContentManagerTutorialLiveClientTest {

    @Test
    void restApiTestingTutorialRendersSimpleApiLiveClients() {

        ResourceContentScanner contentScanner = new ResourceContentScanner();
        List<String> pathsToFileContent =
                contentScanner.scanForFullPathsOfExtensionsIn("content/", "md");
        MarkdownContentManager contentManager =
                new MarkdownContentManager(pathsToFileContent, new DefaultGUIHTML(), null);

        String html =
                contentManager.getResourceMarkdownFileAsHtml(
                        "content",
                        "/tutorials/rest-api-testing",
                        Map.of(
                                "ORIGIN_URL",
                                "http://localhost:4567",
                                "HOST_URL",
                                "localhost:4567"));

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

    private int countOccurrences(final String value, final String substring) {
        return value.split(java.util.regex.Pattern.quote(substring), -1).length - 1;
    }
}
