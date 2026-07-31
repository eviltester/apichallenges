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
        Assertions.assertTrue(urls.containsKey("tutorials/openapi"));
    }
}
