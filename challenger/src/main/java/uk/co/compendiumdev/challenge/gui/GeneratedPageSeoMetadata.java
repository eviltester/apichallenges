package uk.co.compendiumdev.challenge.gui;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.after;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;

public final class GeneratedPageSeoMetadata {

    private static final String CANONICAL_HOST = "https://apichallenges.com";
    private static final String SITE_NAME = "API Challenges";
    private static final String DEFAULT_OG_IMAGE =
            CANONICAL_HOST + "/images/hero/apichallenges-whole-site-gauntlet-1600x720.jpg";
    private static final String META_MARKER = "<!-- generated-page-seo-metadata -->";
    private static final Pattern DESCRIPTION_META =
            Pattern.compile("<meta\\s+name=['\"]description['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROBOTS_META =
            Pattern.compile("<meta\\s+name=['\"]robots['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern JSON_LD_SCRIPT =
            Pattern.compile(
                    "<script\\s+type=['\"]application/ld\\+json['\"]", Pattern.CASE_INSENSITIVE);
    private static final Map<String, PageMetadata> PAGES = pageMetadata();

    private GeneratedPageSeoMetadata() {}

    public static void install() {
        after(GeneratedPageSeoMetadata::inject);
    }

    private static void inject(final HttpServerRequest request, final HttpServerResponse response) {

        if (response.status() < 200 || response.status() >= 400) {
            return;
        }

        final PageMetadata metadata = PAGES.get(request.path());
        if (metadata == null) {
            return;
        }

        final String body = response.body();
        if (body == null || !body.contains("</head>") || body.contains(META_MARKER)) {
            return;
        }

        final String headContent = metadata.headContent(body);
        if (headContent.isEmpty()) {
            return;
        }

        response.body(body.replace("</head>", headContent + "</head>"));
    }

    private static Map<String, PageMetadata> pageMetadata() {
        final Map<String, PageMetadata> pages = new LinkedHashMap<>();
        pages.put(
                "/sim/docs/swagger-ui",
                new PageMetadata(
                        "API Simulator - Swagger UI",
                        "Use Swagger UI to inspect the API Simulator OpenAPI definition and compare deterministic request behaviour, payload formats, and guided practice responses.",
                        "/sim/docs/swagger-ui"));
        pages.put(
                "/simpleapi/docs/swagger-ui",
                new PageMetadata(
                        "Simple API - Swagger UI",
                        "Use Swagger UI to explore the Simple API OpenAPI documentation, schemas, examples, and response expectations for hands-on HTTP testing practice.",
                        "/simpleapi/docs/swagger-ui"));
        pages.put(
                "/api/docs/swagger-ui",
                new PageMetadata(
                        "API Challenges - Swagger UI",
                        "Use Swagger UI to explore and try the API Challenges OpenAPI documentation, request formats, payload examples, and expected responses in the browser.",
                        "/api/docs/swagger-ui"));
        pages.put(
                "/shop/docs/swagger-ui",
                new PageMetadata(
                        "Buggy API - Swagger UI",
                        "Use Swagger UI to explore the Buggy API OpenAPI documentation for practising auth, stock, checkout, and business-rule testing workflows.",
                        "/shop/docs/swagger-ui"));
        pages.put(
                "/apichallenges/client",
                new PageMetadata(
                        "API Challenges Client",
                        "Send browser-based requests to the API Challenges practice API, inspect JSON responses, and experiment with challenge endpoints from one simple client.",
                        "/apichallenges/client"));
        pages.put(
                "/simpleapi/client",
                new PageMetadata(
                        "Simple API Client",
                        "Send browser-based requests to the Simple API, generate test ISBNs, inspect JSON responses, and practise create, read, update, and delete calls.",
                        "/simpleapi/client"));
        pages.put(
                "/shop/client",
                new PageMetadata(
                        "Buggy API Client",
                        "Send browser-based requests to the Buggy API shopping cart, inspect JSON responses, and practise product, cart, auth, and checkout workflows.",
                        "/shop/client"));
        return pages;
    }

    private record PageMetadata(String title, String description, String path) {

        private static final String ROBOTS = "noindex,follow";

        private String headContent(final String existingHtml) {
            final StringBuilder html = new StringBuilder(META_MARKER);
            if (!DESCRIPTION_META.matcher(existingHtml).find()) {
                html.append("<meta name='description' content='")
                        .append(escapeHtmlAttribute(description))
                        .append("'>");
            }
            if (!ROBOTS_META.matcher(existingHtml).find()) {
                html.append("<meta name='robots' content='")
                        .append(escapeHtmlAttribute(ROBOTS))
                        .append("'>");
            }
            appendOpenGraphAndTwitterMetadata(html);
            if (!JSON_LD_SCRIPT.matcher(existingHtml).find()) {
                html.append(jsonLd());
            }
            return html.toString();
        }

        private void appendOpenGraphAndTwitterMetadata(final StringBuilder html) {
            final String canonicalUrl = canonicalUrl();
            html.append("<meta property='og:title' content='")
                    .append(escapeHtmlAttribute(title))
                    .append("'>");
            html.append("<meta property='og:description' content='")
                    .append(escapeHtmlAttribute(description))
                    .append("'>");
            html.append("<meta property='og:type' content='website'>");
            html.append("<meta property='og:url' content='")
                    .append(escapeHtmlAttribute(canonicalUrl))
                    .append("'>");
            html.append("<meta property='og:site_name' content='")
                    .append(escapeHtmlAttribute(SITE_NAME))
                    .append("'>");
            html.append("<meta property='og:image' content='")
                    .append(escapeHtmlAttribute(DEFAULT_OG_IMAGE))
                    .append("'>");
            html.append("<meta property='og:image:alt' content='")
                    .append(escapeHtmlAttribute(title))
                    .append("'>");
            html.append("<meta name='twitter:card' content='summary_large_image'>");
            html.append("<meta name='twitter:title' content='")
                    .append(escapeHtmlAttribute(title))
                    .append("'>");
            html.append("<meta name='twitter:description' content='")
                    .append(escapeHtmlAttribute(description))
                    .append("'>");
            html.append("<meta name='twitter:image' content='")
                    .append(escapeHtmlAttribute(DEFAULT_OG_IMAGE))
                    .append("'>");
        }

        private String jsonLd() {
            return "<script type='application/ld+json'>"
                    + "{\"@context\":\"https://schema.org\","
                    + "\"@type\":\"WebPage\","
                    + "\"name\":\""
                    + escapeJsonValue(title)
                    + "\","
                    + "\"description\":\""
                    + escapeJsonValue(description)
                    + "\","
                    + "\"url\":\""
                    + escapeJsonValue(canonicalUrl())
                    + "\","
                    + "\"isPartOf\":{\"@type\":\"WebSite\",\"name\":\""
                    + escapeJsonValue(SITE_NAME)
                    + "\",\"url\":\""
                    + escapeJsonValue(CANONICAL_HOST)
                    + "\"},"
                    + "\"publisher\":{\"@type\":\"Organization\",\"name\":\"Compendium Developments Ltd\",\"url\":\"https://compendiumdev.co.uk\"}}"
                    + "</script>";
        }

        private String canonicalUrl() {
            return CANONICAL_HOST + path;
        }
    }

    private static String escapeHtmlAttribute(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String escapeJsonValue(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
