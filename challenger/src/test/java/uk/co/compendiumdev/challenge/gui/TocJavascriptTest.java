package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TocJavascriptTest {

    @Test
    void emptyTableOfContentsPlaceholderIsRemoved() throws IOException {
        String javascript = tocJavascript();

        Assertions.assertTrue(javascript.contains("var headings = [].slice.call"));
        Assertions.assertTrue(javascript.contains("if (!headings.length)"));
        Assertions.assertTrue(javascript.contains("toc.remove();"));
        Assertions.assertTrue(javascript.contains("showTableOfContentsProgress"));
    }

    private String tocJavascript() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/public/js/toc.js")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
