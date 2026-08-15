package uk.co.compendiumdev.challenge;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.after;

import java.util.LinkedHashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;

public final class OpenApiDocsLandingPageLinks {

    public static final String LINK_TEXT =
            "Find OpenAPI file and OpenAPI Powered Client UIs like Swagger and Scalar here";

    private static final String OPENAPI_VERSION_32_TEXT = "OpenAPI v 3.2 JSON";
    private static final String UNORDERED_LIST_END = "</ul>";
    private static final Map<String, String> OPENAPI_FILE_PAGES = openApiFilePages();

    private OpenApiDocsLandingPageLinks() {}

    public static void install() {
        after(OpenApiDocsLandingPageLinks::replaceGeneratedDocsLinks);
    }

    static String replaceGeneratedDocsLinks(final String html, final String openApiFilePagePath) {

        if (html == null || html.isBlank()) {
            return html;
        }

        final int startIndex = generatedLinksStartIndex(html);
        if (startIndex < 0) {
            return html;
        }

        final int version32Index = html.indexOf(OPENAPI_VERSION_32_TEXT, startIndex);
        if (version32Index < 0) {
            return html;
        }

        final int unorderedListEndIndex = html.indexOf(UNORDERED_LIST_END, version32Index);
        if (unorderedListEndIndex < 0) {
            return html;
        }

        final int endIndex = unorderedListEndIndex + UNORDERED_LIST_END.length();
        return html.substring(0, startIndex)
                + openApiFilePageLink(openApiFilePagePath)
                + html.substring(endIndex);
    }

    private static void replaceGeneratedDocsLinks(
            final HttpServerRequest request, final HttpServerResponse response) {

        if (response.status() < 200 || response.status() >= 400) {
            return;
        }

        final String openApiFilePagePath = OPENAPI_FILE_PAGES.get(request.path());
        if (openApiFilePagePath == null) {
            return;
        }

        final String body = response.body();
        if (body == null) {
            return;
        }

        final String replacement = replaceGeneratedDocsLinks(body, openApiFilePagePath);
        if (!replacement.equals(body)) {
            response.body(replacement);
        }
    }

    private static int generatedLinksStartIndex(final String html) {
        final int swaggerLinkTextIndex = html.indexOf("Open Swagger UI");
        if (swaggerLinkTextIndex >= 0) {
            return html.lastIndexOf("<p", swaggerLinkTextIndex);
        }

        final int scalarLinkTextIndex = html.indexOf("Open Scalar UI");
        if (scalarLinkTextIndex >= 0) {
            return html.lastIndexOf("<p", scalarLinkTextIndex);
        }

        final int openApiListTextIndex = html.indexOf("OpenAPI v 3.0 JSON");
        if (openApiListTextIndex >= 0) {
            return html.lastIndexOf("<ul", openApiListTextIndex);
        }

        return -1;
    }

    private static String openApiFilePageLink(final String openApiFilePagePath) {
        return "<p><a href='" + openApiFilePagePath + "'>" + LINK_TEXT + "</a></p>\n";
    }

    private static Map<String, String> openApiFilePages() {
        final Map<String, String> pages = new LinkedHashMap<>();
        pages.put("/api/docs", "/apichallenges/openapi");
        pages.put("/simpleapi/docs", "/practice-modes/simpleapi-openapi");
        pages.put("/sim/docs", "/practice-modes/simulation-openapi");
        pages.put("/shop/docs", "/practice-modes/shoppingcart-openapi");
        pages.put("/mirror/docs", "/practice-modes/mirror");
        return pages;
    }
}
