package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimLiveRequestJavascriptTest {

    @Test
    void liveRequestWidgetsKeepFilterOperatorsReadableInDisplay() throws IOException {
        String javascript = simLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("function readableUrl(url)"));
        Assertions.assertTrue(javascript.contains(".replace(/%3E/gi, '>')"));
        Assertions.assertTrue(javascript.contains(".replace(/%3C/gi, '<')"));
        Assertions.assertTrue(javascript.contains(".replace(/%7E/gi, '~')"));
        Assertions.assertTrue(javascript.contains(".replace(/%2A/gi, '*')"));
        Assertions.assertTrue(javascript.contains("url.textContent = readableUrl(request.url)"));
        Assertions.assertTrue(javascript.contains("urlInput.value = readableUrl(request.url)"));
        Assertions.assertTrue(javascript.contains("\"${readableUrl(request.url)}\""));
        Assertions.assertTrue(javascript.contains("return fetch(request.url, options);"));
    }

    private String simLiveRequestJavascript() throws IOException {
        try (InputStream stream =
                getClass().getResourceAsStream("/public/js/sim-live-request.js")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
