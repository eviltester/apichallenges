package uk.co.compendiumdev.challenge;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.after;

import java.util.Locale;
import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;

public final class CdnCachePolicy {

    public static final String STATIC_ASSET_CACHE_CONTROL = "public, max-age=31536000, immutable";
    public static final String DOCS_CACHE_CONTROL =
            "public, max-age=300, s-maxage=86400, stale-while-revalidate=604800, stale-if-error=604800";
    public static final String NO_STORE_CACHE_CONTROL = "no-store";

    private CdnCachePolicy() {}

    public static void install() {
        System.setProperty("thingifier.static.cache-control", STATIC_ASSET_CACHE_CONTROL);
        after(
                (request, response) -> {
                    versionLocalHtmlAssets(response);
                    applyRobotsPolicy(request, response);
                    apply(request, response);
                });
    }

    static void applyRobotsPolicy(
            final HttpServerRequest request, final HttpServerResponse response) {
        if (response.status() >= 200
                && response.status() < 400
                && isSwaggerUiPath(request.path())) {
            response.header("X-Robots-Tag", "noindex, follow");
        }
    }

    static void apply(final HttpServerRequest request, final HttpServerResponse response) {
        final String path = request.path();

        if (isStaticAssetPath(path)) {
            response.header("Cache-Control", STATIC_ASSET_CACHE_CONTROL);
            return;
        }

        if (hasCacheControl(response)) {
            return;
        }

        if (!isCacheableMethod(request.method())
                || response.status() < 200
                || response.status() >= 400) {
            response.header("Cache-Control", NO_STORE_CACHE_CONTROL);
            return;
        }

        if (isDocsPath(path)) {
            response.header("Cache-Control", DOCS_CACHE_CONTROL);
            return;
        }

        response.header("Cache-Control", NO_STORE_CACHE_CONTROL);
    }

    private static void versionLocalHtmlAssets(final HttpServerResponse response) {
        if (response.status() < 200 || response.status() >= 400) {
            return;
        }

        final String contentType = headerValue(response, "Content-Type");
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("text/html")) {
            return;
        }

        final String body = response.body();
        final String versionedBody = AssetVersion.versionHtmlAssetReferences(body);
        if (versionedBody != null && !versionedBody.equals(body)) {
            response.body(versionedBody);
        }
    }

    private static boolean hasCacheControl(final HttpServerResponse response) {
        return headerValue(response, "Cache-Control") != null;
    }

    private static String headerValue(final HttpServerResponse response, final String headerName) {
        for (Map.Entry<String, String> header : response.headers().entrySet()) {
            if (header.getKey().equalsIgnoreCase(headerName)) {
                return header.getValue();
            }
        }
        return null;
    }

    private static boolean isCacheableMethod(final String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }

    private static boolean isStaticAssetPath(final String path) {
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/favicon/")
                || path.startsWith("/images/")
                || path.equals("/robots.txt")
                || path.equals("/sitemap.bak");
    }

    private static boolean isDocsPath(final String path) {
        return path.equals("/")
                || path.equals("/sitemap.xml")
                || path.equals("/docs")
                || path.startsWith("/docs/")
                || path.matches("^/(simpleapi|sim|shop|mirror|fromhell)/docs($|/.*)")
                || isContentDocumentationPath(path);
    }

    private static boolean isSwaggerUiPath(final String path) {
        return path.equals("/docs/swagger-ui")
                || path.matches("^/(simpleapi|sim|shop|mirror|fromhell)/docs/swagger-ui$");
    }

    private static boolean isContentDocumentationPath(final String path) {
        return path.startsWith("/apichallenges")
                || path.startsWith("/author")
                || path.startsWith("/learning")
                || path.startsWith("/practice-modes")
                || path.startsWith("/tools")
                || path.startsWith("/tutorials")
                || path.equals("/seo-metadata-test-page")
                || path.equals("/seo-metadata-minimal-page")
                || path.equals("/seo-metadata-video-page");
    }
}
