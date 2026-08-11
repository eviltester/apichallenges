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
        String themeCss = themeExperimentsCss();

        String collapsedLinksRule =
                ruleBody(css, ".side-toc-section:not([open]) > .side-toc-section-links");
        Assertions.assertTrue(collapsedLinksRule.contains("display: none"));

        String openDescriptionRule =
                ruleBody(
                        css,
                        ".side-toc-section[open] > .side-toc-section-summary .side-toc-section-description");
        Assertions.assertTrue(openDescriptionRule.contains("display: none"));

        Assertions.assertTrue(css.contains("a.side-toc-section-title"));
        Assertions.assertTrue(css.contains("a.side-toc-syllabus-title"));
        Assertions.assertTrue(css.contains(".content-centered-cta"));
        Assertions.assertTrue(themeCss.contains("html[data-theme] a.side-toc-section-title"));
        Assertions.assertTrue(themeCss.contains("html[data-theme] a.side-toc-syllabus-title"));
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
    void wideToolPagesUseNormalChromeClosableNavAndWideEmbedOnly() throws IOException {
        String contentCss = contentCss();
        String themeCss = themeExperimentsCss();
        String onlineCss = onlineSwaggerThemeCss();

        Assertions.assertFalse(contentCss.contains("doc-columns-wide-tool"));
        Assertions.assertFalse(contentCss.contains("content-wide-tool"));
        Assertions.assertFalse(contentCss.contains("is-wide-tool-drawer"));
        Assertions.assertFalse(themeCss.contains("content-wide-tool"));
        Assertions.assertFalse(themeCss.contains("wide-tool-main"));
        Assertions.assertFalse(themeCss.contains(".left-column.is-wide-tool-drawer"));

        String wideToolColumnsRule = ruleBody(contentCss, "body.wide-tool-page .doc-columns");
        Assertions.assertTrue(wideToolColumnsRule.contains("display: flex"));
        Assertions.assertTrue(wideToolColumnsRule.contains("flex-direction: column"));

        String wideToolSideNavLayerRule =
                ruleBody(
                        themeCss,
                        "html[data-theme] body.wide-tool-page .left-column.is-collapsible");
        Assertions.assertTrue(wideToolSideNavLayerRule.contains("position: relative"));
        Assertions.assertTrue(wideToolSideNavLayerRule.contains("z-index: 45"));

        String closedSideNavRule =
                ruleBody(contentCss, "body.wide-tool-page .left-column.is-collapsible .side-toc");
        Assertions.assertTrue(closedSideNavRule.contains("display: none"));

        String openSideNavRule =
                ruleBody(
                        contentCss,
                        "body.wide-tool-page .left-column.is-collapsible.is-open .side-toc");
        Assertions.assertTrue(openSideNavRule.contains("display: block"));

        Assertions.assertTrue(contentCss.contains("@media (min-width: 1180px)"));
        Assertions.assertTrue(
                contentCss.contains(
                        "grid-template-columns: minmax(13rem, 0.85fr) minmax(20rem, 1.4fr) "
                                + "minmax(13rem, 0.9fr)"));
        Assertions.assertTrue(contentCss.contains("body.wide-tool-page .wide-tool-side-toc-grid"));
        Assertions.assertTrue(
                contentCss.contains(
                        "body.wide-tool-page .wide-tool-side-toc-column + "
                                + ".wide-tool-side-toc-column"));
        Assertions.assertTrue(
                contentCss.contains("body.wide-tool-page .wide-tool-side-toc-support"));
        Assertions.assertTrue(
                contentCss.contains(
                        "body.wide-tool-page .wide-tool-side-toc-column > "
                                + ".side-toc-root > li{\n    font-weight: bold"));
        Assertions.assertTrue(
                contentCss.contains(
                        "body.wide-tool-page .wide-tool-side-toc-column > "
                                + ".side-toc-root > li > ul li"));
        Assertions.assertFalse(contentCss.contains("li:nth-child(8)"));
        Assertions.assertTrue(
                themeCss.contains(
                        "html[data-theme] body.wide-tool-page "
                                + ".wide-tool-side-toc-support > p a"));
        Assertions.assertTrue(themeCss.contains(".wide-tool-side-toc-grid"));
        Assertions.assertTrue(themeCss.contains("gap: clamp(1rem, 2vw, 1.75rem)"));
        Assertions.assertTrue(themeCss.contains("display: grid"));

        String wideToolNavButtonRule =
                ruleBody(
                        themeCss,
                        "html[data-theme] body.wide-tool-page .mobile-content-nav-toggle");
        Assertions.assertTrue(wideToolNavButtonRule.contains("display: inline-flex"));

        String wideToolMainRule = ruleBody(themeCss, "html[data-theme] body.wide-tool-page main");
        Assertions.assertTrue(wideToolMainRule.contains("background: transparent"));
        Assertions.assertTrue(wideToolMainRule.contains("border: 0"));

        String copyBlockRule =
                ruleBody(themeCss, "html[data-theme] body.wide-tool-page .wide-tool-copy-block");
        Assertions.assertTrue(copyBlockRule.contains("border: 1px solid var(--border)"));
        Assertions.assertTrue(copyBlockRule.contains("max-width: var(--reading-max)"));

        String breakoutRule =
                ruleBody(
                        themeCss,
                        "html[data-theme] body.wide-tool-page .wide-tool-client-breakout");
        Assertions.assertTrue(
                breakoutRule.contains("inline-size: calc(100vw - clamp(1rem, 4vw, 3rem))"));

        String wideToolTocRule = ruleBody(themeCss, "html[data-theme] body.wide-tool-page #toc");
        Assertions.assertTrue(wideToolTocRule.contains("float: none"));
        Assertions.assertTrue(wideToolTocRule.contains("position: static"));

        Assertions.assertTrue(
                onlineCss.contains(
                        "html[data-theme] body.wide-tool-page .online-openapi-ui-wide-embed"));
        Assertions.assertFalse(onlineCss.contains("inline-size: min(64rem"));
        Assertions.assertFalse(onlineCss.contains("inline-size: min(48rem"));
        Assertions.assertTrue(onlineCss.contains("inline-size: 100%"));

        String wideClientRule =
                ruleBody(
                        onlineCss,
                        "html[data-theme] body.wide-tool-page .online-swagger-client,\n"
                                + "html[data-theme] body.wide-tool-page .online-openapi-ui-client");
        Assertions.assertTrue(wideClientRule.contains("background: transparent"));
        Assertions.assertTrue(wideClientRule.contains("border: 0"));
        Assertions.assertTrue(wideClientRule.contains("box-shadow: none"));

        Assertions.assertTrue(onlineCss.contains("html[data-theme] .online-openapi-ui-render"));
        String wideRenderRule =
                ruleBody(
                        onlineCss,
                        "html[data-theme] body.wide-tool-page .online-openapi-ui-wide-embed"
                                + " .online-openapi-ui-render,\n"
                                + "html[data-theme] body.wide-tool-page"
                                + " .online-openapi-ui-wide-embed #online-swagger-ui");
        Assertions.assertTrue(wideRenderRule.contains("height: auto"));
        Assertions.assertTrue(
                wideRenderRule.contains("min-height: max(34rem, calc(100vh - 8rem))"));
        Assertions.assertTrue(wideRenderRule.contains("overflow: visible"));
        Assertions.assertFalse(wideRenderRule.contains("overflow: auto"));
        Assertions.assertTrue(onlineCss.contains("body.wide-tool-page .scalar-app-exit"));
        Assertions.assertTrue(onlineCss.contains("width: auto !important"));
        String scalarRenderedMainRule =
                ruleBody(
                        onlineCss,
                        "html[data-theme] body.wide-tool-page #online-scalar-ui"
                                + " main.references-rendered");
        Assertions.assertTrue(scalarRenderedMainRule.contains("margin: 0"));
        Assertions.assertTrue(scalarRenderedMainRule.contains("max-width: none"));
        Assertions.assertTrue(scalarRenderedMainRule.contains("padding: 0"));
        Assertions.assertTrue(scalarRenderedMainRule.contains("width: auto"));
        Assertions.assertTrue(onlineCss.contains("--scalar-background-1: var(--surface)"));
        Assertions.assertTrue(onlineCss.contains("#online-scalar-ui button"));
        Assertions.assertTrue(onlineCss.contains("background: transparent !important"));
        Assertions.assertTrue(onlineCss.contains("#online-scalar-ui .client-libraries__active"));
        Assertions.assertTrue(
                onlineCss.contains(
                        "html[data-theme=\"learning-platform\"] body.wide-tool-page"
                                + " #online-scalar-ui .client-libraries__active"));
        Assertions.assertTrue(onlineCss.contains("color: var(--text) !important"));
        Assertions.assertTrue(onlineCss.contains("#online-scalar-ui .download-button"));
        Assertions.assertTrue(onlineCss.contains("#online-scalar-ui .parameter-item-trigger"));
        Assertions.assertTrue(onlineCss.contains("#online-scalar-ui .request-body-required"));
        Assertions.assertTrue(onlineCss.contains("#online-scalar-ui .scalar-code-block"));
        Assertions.assertTrue(
                onlineCss.contains("#online-scalar-ui .scalar-code-block .custom-scroll"));
        Assertions.assertTrue(onlineCss.contains("#online-openapi-explorer-ui openapi-explorer"));
        Assertions.assertTrue(
                onlineCss.contains("--api-challenges-openapi-surface: var(--surface)"));
        Assertions.assertTrue(onlineCss.contains("--api-challenges-openapi-nav-bg"));
        Assertions.assertTrue(onlineCss.contains("--api-challenges-openapi-nav-text"));
        Assertions.assertTrue(onlineCss.contains("#online-stoplight-ui .sl-bg-canvas"));
        Assertions.assertTrue(onlineCss.contains("--color-canvas: var(--surface)"));
        Assertions.assertTrue(onlineCss.contains("--color-canvas-tint: var(--surface-soft)"));
        Assertions.assertTrue(onlineCss.contains("#online-stoplight-ui .sl-bg-canvas-tint"));
        Assertions.assertTrue(onlineCss.contains("#online-stoplight-ui button.sl-button"));
        Assertions.assertTrue(onlineCss.contains("padding-block: 0.25rem !important"));
        Assertions.assertTrue(onlineCss.contains("#online-stoplight-ui button.sl-bg-primary"));
        Assertions.assertTrue(onlineCss.contains("#online-stoplight-ui button.sl-bg-transparent"));
        Assertions.assertTrue(
                onlineCss.contains("#online-stoplight-ui button.sl-border-transparent"));
        Assertions.assertTrue(onlineCss.contains("#online-stoplight-ui .SendButtonHolder"));
        Assertions.assertTrue(onlineCss.contains("flex-wrap: wrap !important"));
        Assertions.assertTrue(onlineCss.contains("#online-stoplight-ui .sl-code-highlight"));
        Assertions.assertTrue(onlineCss.contains("#online-stoplight-ui .token.string"));
        Assertions.assertTrue(onlineCss.contains("#online-stoplight-ui .token.property"));
        Assertions.assertTrue(
                onlineCss.contains("#online-stoplight-ui .sl-bg-success.sl-text-on-primary"));
        Assertions.assertTrue(
                onlineCss.contains(
                        "#online-stoplight-ui .sl-text-warning:not(.sl-text-on-primary)"));
        Assertions.assertTrue(
                onlineCss.contains("#online-stoplight-ui .sl-bg-danger.sl-text-on-primary"));
        Assertions.assertTrue(
                onlineCss.contains(
                        "html[data-theme=\"learning-platform\"] body.wide-tool-page"
                                + " #online-stoplight-ui .sl-text-primary"));
        Assertions.assertTrue(
                onlineCss.contains("#online-stoplight-ui .ElementsTableOfContentsItem"));
        Assertions.assertTrue(
                onlineCss.contains(
                        "#online-stoplight-ui .ElementsTableOfContentsItem .sl-text-success"));
        Assertions.assertTrue(
                onlineCss.contains(
                        "#online-stoplight-ui .ElementsTableOfContentsItem .sl-text-danger"));
        Assertions.assertTrue(
                onlineCss.contains(
                        "#online-stoplight-ui .sl-text-lg.sl-font-semibold.sl-uppercase.sl-px-2\\.5"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui .redoc-wrap"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui .menu-content"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui .scrollbar-container.ps"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui [role=\"tab\"]"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui .react-tabs__tab--selected"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui button:not(.collapser)"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui button.collapser"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui .http-verb.get"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui .operation-type.options"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui table span"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui code span"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui .redoc-json"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui .redoc-json code"));
        Assertions.assertTrue(onlineCss.contains("#online-redoc-ui .redoc-json ul"));
        Assertions.assertTrue(onlineCss.contains("overflow-x: auto !important"));
        Assertions.assertTrue(
                onlineCss.contains(
                        "html[data-theme] body.wide-tool-page .online-openapi-ui-wide-embed #online-swagger-ui"));
    }

    @Test
    void openApiFilePagesHaveThemedLaunchButtons() throws IOException {
        String contentCss = contentCss();
        String themeCss = themeExperimentsCss();

        Assertions.assertTrue(contentCss.contains(".openapi-ui-launch-panel"));
        Assertions.assertTrue(contentCss.contains(".openapi-ui-launch-links"));
        Assertions.assertTrue(contentCss.contains("a.openapi-ui-launch-link"));

        String baseLinkRule = ruleBody(contentCss, "a.openapi-ui-launch-link");
        Assertions.assertTrue(baseLinkRule.contains("display: inline-flex"));
        Assertions.assertTrue(baseLinkRule.contains("min-height: 2.25rem"));
        Assertions.assertTrue(baseLinkRule.contains("text-decoration: none"));

        String themedGroupRule = ruleBody(themeCss, "html[data-theme] .openapi-ui-launch-group");
        Assertions.assertTrue(themedGroupRule.contains("background: var(--surface-soft)"));
        Assertions.assertTrue(themedGroupRule.contains("border-color: var(--border)"));

        String themedLinkRule = ruleBody(themeCss, "html[data-theme] a.openapi-ui-launch-link");
        Assertions.assertTrue(themedLinkRule.contains("background: var(--cta-bg)"));
        Assertions.assertTrue(themedLinkRule.contains("color: var(--cta-text)"));
    }

    @Test
    void onlineSwaggerThemeOverridesSwaggerUiAfterCdnStyles() throws IOException {
        String css = onlineSwaggerThemeCss();

        Assertions.assertTrue(css.contains("html[data-theme] #online-swagger-ui"));
        Assertions.assertTrue(
                css.contains("html[data-theme=\"dark-lab\"] #online-swagger-ui .swagger-ui"));
        Assertions.assertTrue(
                css.contains(
                        "html[data-theme]:not([data-theme=\"dark-lab\"]) #online-swagger-ui .swagger-ui"));
        Assertions.assertTrue(css.contains(".responses-table"));
        Assertions.assertTrue(css.contains(".opblock.opblock-get"));
        Assertions.assertTrue(css.contains(".opblock.opblock-post"));
        Assertions.assertTrue(css.contains(".opblock.opblock-put"));
        Assertions.assertTrue(css.contains(".opblock.opblock-patch"));
        Assertions.assertTrue(css.contains(".opblock.opblock-query"));
        Assertions.assertTrue(css.contains(".opblock.opblock-delete"));
        Assertions.assertTrue(css.contains(".download-url-wrapper .download-url-button"));
        Assertions.assertTrue(css.contains("background: #0f766e !important"));
        Assertions.assertTrue(css.contains(".opblock-summary-path"));
        Assertions.assertTrue(css.contains(".opblock-summary-control:hover"));
        Assertions.assertTrue(css.contains("background: transparent !important"));
        Assertions.assertTrue(css.contains(".btn.try-out__btn"));
        Assertions.assertTrue(css.contains(".swagger-ui .btn:hover"));
        Assertions.assertTrue(css.contains("opacity: 1 !important"));
        Assertions.assertTrue(css.contains(".swagger-ui section.models"));
        Assertions.assertTrue(css.contains(".json-schema-2020-12"));
        Assertions.assertTrue(css.contains(".json-schema-2020-12__title"));
        Assertions.assertTrue(
                css.contains("section.models .json-schema-2020-12-expand-deep-button"));
        Assertions.assertTrue(css.contains(".opblock-summary-options .opblock-summary-method"));
        Assertions.assertTrue(css.contains(".opblock-summary-head .opblock-summary-method"));
        Assertions.assertTrue(css.contains(".opblock-summary-query .opblock-summary-method"));
        Assertions.assertTrue(css.contains(".opblock-summary-trace .opblock-summary-method"));
        Assertions.assertTrue(css.contains("color-scheme: dark"));
        Assertions.assertTrue(css.contains("color-scheme: light"));
        Assertions.assertTrue(css.contains(".opblock-summary-method"));
        Assertions.assertTrue(css.contains("text-shadow: none"));
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
