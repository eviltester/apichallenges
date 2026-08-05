package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChallengerUiCssTest {

    @Test
    void achievementHoverUsesAnimatedRingWithoutRepaintingMedal() throws IOException {
        String css = themeExperimentsCss();

        Assertions.assertTrue(css.contains("html[data-theme] .achievement-medal::after"));
        Assertions.assertTrue(css.contains("@keyframes achievement-ring-pulse"));
        Assertions.assertTrue(
                css.contains(
                        "html[data-theme] .achievement-medal:hover::after,\n"
                                + "html[data-theme] .achievement-medal:focus-visible::after"));

        String hoverRule =
                ruleBody(
                        css,
                        "html[data-theme] .achievement-medal:hover,\n"
                                + "html[data-theme] .achievement-medal:focus-visible");
        Assertions.assertFalse(hoverRule.contains("background:"));
        Assertions.assertFalse(hoverRule.contains("color:"));

        String hoverRingRule =
                ruleBody(
                        css,
                        "html[data-theme] .achievement-medal:hover::after,\n"
                                + "html[data-theme] .achievement-medal:focus-visible::after");
        Assertions.assertTrue(hoverRingRule.contains("animation: achievement-ring-pulse"));
        Assertions.assertTrue(hoverRingRule.contains("opacity: 1"));
        Assertions.assertTrue(hoverRingRule.contains("transform: scale(1)"));
    }

    @Test
    void nextAchievementUsesUnderlineInsteadOfCircularOutline() throws IOException {
        String css = themeExperimentsCss();

        Assertions.assertTrue(css.contains("html[data-theme] .achievement-medal.is-next::before"));
        Assertions.assertTrue(css.contains("@keyframes achievement-next-underline"));

        String nextRule = ruleBody(css, "html[data-theme] .achievement-medal.is-next");
        Assertions.assertTrue(nextRule.contains("opacity: 0.86"));
        Assertions.assertFalse(nextRule.contains("outline:"));
        Assertions.assertFalse(nextRule.contains("outline-offset:"));

        String underlineRule = ruleBody(css, "html[data-theme] .achievement-medal.is-next::before");
        Assertions.assertTrue(underlineRule.contains("bottom: -0.45rem"));
        Assertions.assertTrue(underlineRule.contains("height: 0.16rem"));
        Assertions.assertTrue(underlineRule.contains("width: 72%"));
        Assertions.assertTrue(underlineRule.contains("animation: achievement-next-underline"));

        String selectedNextRule =
                ruleBody(css, "html[data-theme] .achievement-medal.is-selected.is-next");
        Assertions.assertTrue(selectedNextRule.contains("background: var(--surface-strong)"));
        Assertions.assertTrue(selectedNextRule.contains("color: var(--muted)"));
        Assertions.assertTrue(selectedNextRule.contains("outline: none"));
    }

    @Test
    void sideTocShowsDescriptionsOnlyWhenSectionsAreCollapsed() throws IOException {
        String css = contentCss();

        String collapsedLinksRule =
                ruleBody(css, ".side-toc-section:not([open]) > .side-toc-section-links");
        Assertions.assertTrue(collapsedLinksRule.contains("display: none"));

        String openDescriptionRule =
                ruleBody(
                        css,
                        ".side-toc-section[open] > .side-toc-section-summary .side-toc-section-description");
        Assertions.assertTrue(openDescriptionRule.contains("display: none"));
    }

    @Test
    void emptyTableOfContentsDoesNotFloatBesideContent() throws IOException {
        String css = tocCss();

        String emptyTocRule = ruleBody(css, "div#toc:empty");
        Assertions.assertTrue(emptyTocRule.contains("display: none"));
        Assertions.assertTrue(css.contains(".main-text-content:has(> #toc:not(:empty))"));
        Assertions.assertFalse(css.contains(".main-text-content:has(> #toc)"));
    }

    @Test
    void onlineSwaggerThemeOverridesSwaggerUiAfterCdnStyles() throws IOException {
        String css = onlineSwaggerThemeCss();

        Assertions.assertTrue(css.contains("html[data-theme] #online-swagger-ui"));
        Assertions.assertTrue(
                css.contains(
                        "html[data-theme=\"dark-lab\"] #online-swagger-ui .swagger-ui"));
        Assertions.assertTrue(
                css.contains(
                        "html[data-theme]:not([data-theme=\"dark-lab\"]) #online-swagger-ui .swagger-ui"));
        Assertions.assertTrue(css.contains(".responses-table"));
        Assertions.assertTrue(css.contains(".opblock-summary-options .opblock-summary-method"));
        Assertions.assertTrue(css.contains(".opblock-summary-head .opblock-summary-method"));
        Assertions.assertTrue(css.contains(".opblock-summary-trace .opblock-summary-method"));
        Assertions.assertTrue(css.contains("color-scheme: dark"));
        Assertions.assertTrue(css.contains("color-scheme: light"));
        Assertions.assertTrue(css.contains(".opblock-summary-method"));
        Assertions.assertTrue(css.contains("var(--surface)"));
        Assertions.assertTrue(css.contains("var(--text)"));
        Assertions.assertTrue(css.contains("var(--code-bg)"));
    }

    private String ruleBody(final String css, final String selector) {
        Pattern pattern =
                Pattern.compile(Pattern.quote(selector) + "\\s*\\{([^}]*)}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(css);
        Assertions.assertTrue(matcher.find(), selector);
        return matcher.group(1);
    }

    private String themeExperimentsCss() throws IOException {
        try (InputStream stream =
                getClass().getResourceAsStream("/public/css/theme-experiments.css")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String contentCss() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/public/css/content.css")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String tocCss() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/public/css/toc.css")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String onlineSwaggerThemeCss() throws IOException {
        try (InputStream stream =
                getClass().getResourceAsStream("/public/css/online-swagger-theme.css")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
