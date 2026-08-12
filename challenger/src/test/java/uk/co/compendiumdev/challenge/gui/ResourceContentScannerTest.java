package uk.co.compendiumdev.challenge.gui;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceContentScannerTest {

    @Test
    void scanForUrlsWithDatesExcludesNoindexAndExplicitlyExcludedPages() {

        final ResourceContentScanner scanner = new ResourceContentScanner();
        final Map<String, LocalDate> urls = scanner.scanForUrlsWithDates("content/", "md");

        Assertions.assertFalse(urls.containsKey("seo-metadata-test-page"));
        Assertions.assertFalse(urls.containsKey("practice-modes/shoppingcart-bugs"));
        Assertions.assertTrue(urls.containsKey("reference/openapi"));
        Assertions.assertFalse(urls.containsKey("reference/swagger"));
        Assertions.assertFalse(urls.containsKey("reference/open-api-uis/swagger"));
        Assertions.assertFalse(urls.containsKey("reference/open-api-uis/openapi-explorer"));
        Assertions.assertFalse(urls.containsKey("reference/open-api-uis/scalar"));
        Assertions.assertFalse(urls.containsKey("reference/open-api-uis/stoplight"));
        Assertions.assertFalse(urls.containsKey("reference/open-api-uis/zudoku"));
        Assertions.assertFalse(urls.containsKey("reference/open-api-uis/redoc"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/openapi-explorer"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/scalar"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/stoplight"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/zudoku"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/redoc"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/swagger/about"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/openapi-explorer/about"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/scalar/about"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/stoplight/about"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/zudoku/about"));
        Assertions.assertTrue(urls.containsKey("tools/online-clients/redoc/about"));
    }
}
