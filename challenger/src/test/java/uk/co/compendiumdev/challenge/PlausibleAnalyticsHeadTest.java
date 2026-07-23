package uk.co.compendiumdev.challenge;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class PlausibleAnalyticsHeadTest {

    @Test
    public void disabledWhenEnabledFlagMissing() {
        final String head = new PlausibleAnalyticsHead(Map.of()).asHtml();
        Assertions.assertEquals("", head);
    }

    @Test
    public void disabledWhenEnabledFlagIsNotTrue() {
        final String head =
                new PlausibleAnalyticsHead(Map.of(PlausibleAnalyticsHead.ENABLED_ENV, "false"))
                        .asHtml();

        Assertions.assertEquals("", head);
    }

    @Test
    public void enabledUsesDefaultScriptUrlWhenCustomUrlMissing() {
        final String head =
                new PlausibleAnalyticsHead(Map.of(PlausibleAnalyticsHead.ENABLED_ENV, "true"))
                        .asHtml();

        Assertions.assertTrue(head.contains("Privacy-friendly analytics by Plausible"));
        Assertions.assertTrue(head.contains(PlausibleAnalyticsHead.DEFAULT_SCRIPT_URL));
        Assertions.assertTrue(head.contains("window.plausible=window.plausible"));
        Assertions.assertTrue(head.contains("plausible.init()"));
    }

    @Test
    public void enabledUsesCustomScriptUrlWhenProvided() {
        final String customUrl = "https://plausible.example.com/js/script.js";
        final String head =
                new PlausibleAnalyticsHead(
                                Map.of(
                                        PlausibleAnalyticsHead.ENABLED_ENV,
                                        "true",
                                        PlausibleAnalyticsHead.SCRIPT_URL_ENV,
                                        customUrl))
                        .asHtml();

        Assertions.assertTrue(head.contains(customUrl));
        Assertions.assertFalse(head.contains(PlausibleAnalyticsHead.DEFAULT_SCRIPT_URL));
    }

    @Test
    public void unsafeCustomScriptUrlFallsBackToDefaultScriptUrl() {
        final String head =
                new PlausibleAnalyticsHead(
                                Map.of(
                                        PlausibleAnalyticsHead.ENABLED_ENV,
                                        "true",
                                        PlausibleAnalyticsHead.SCRIPT_URL_ENV,
                                        "javascript:alert(1)"))
                        .asHtml();

        Assertions.assertTrue(head.contains(PlausibleAnalyticsHead.DEFAULT_SCRIPT_URL));
        Assertions.assertFalse(head.contains("javascript:alert"));
    }

    @Test
    public void generatedPageHeadDoesNotContainPlausibleByDefault() {
        final DefaultGUIHTML gui = new DefaultGUIHTML();

        final String html = gui.getPageStart("Page", "", "");

        Assertions.assertFalse(html.contains("plausible"));
    }

    @Test
    public void configuredSnippetCanBeAppendedToGeneratedPageHead() {
        final DefaultGUIHTML gui = new DefaultGUIHTML();
        gui.appendToCustomHeadContent(
                new PlausibleAnalyticsHead(Map.of(PlausibleAnalyticsHead.ENABLED_ENV, "true"))
                        .asHtml());

        final String html = gui.getPageStart("Page", "", "");

        Assertions.assertTrue(html.contains(PlausibleAnalyticsHead.DEFAULT_SCRIPT_URL));
        Assertions.assertTrue(html.contains("window.plausible=window.plausible"));
    }
}
