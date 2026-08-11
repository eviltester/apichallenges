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
    private static final String SWAGGER_UI_DIST_VERSION = "5.32.12";
    private static final String UNPKG_CDN_ROOT = "https://" + "unpkg.com/";
    private static final String SWAGGER_UI_DIST_CDN_ROOT = UNPKG_CDN_ROOT + "swagger-ui-dist";
    private static final String PINNED_SWAGGER_UI_DIST_CDN_ROOT =
            UNPKG_CDN_ROOT + "swagger-ui-dist@" + SWAGGER_UI_DIST_VERSION;
    private static final Map<String, String> PINNED_CDN_REPLACEMENTS =
            Map.of(
                    SWAGGER_UI_DIST_CDN_ROOT + "/swagger-ui.css",
                    PINNED_SWAGGER_UI_DIST_CDN_ROOT + "/swagger-ui.css",
                    SWAGGER_UI_DIST_CDN_ROOT + "/swagger-ui-bundle.js",
                    PINNED_SWAGGER_UI_DIST_CDN_ROOT + "/swagger-ui-bundle.js",
                    SWAGGER_UI_DIST_CDN_ROOT + "/swagger-ui-standalone-preset.js",
                    PINNED_SWAGGER_UI_DIST_CDN_ROOT + "/swagger-ui-standalone-preset.js");

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
        if (response.status() < 200 || response.status() >= 400) {
            return;
        }

        final String path = request.path();
        if (isNoindexFollowHtmlPagePath(path)) {
            response.header("X-Robots-Tag", "noindex, follow");
            return;
        }

        if (isNoindexApiResourcePath(path)) {
            response.header("X-Robots-Tag", "noindex");
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
        final String versionedBody =
                AssetVersion.versionHtmlAssetReferences(pinThirdPartyCdnHtmlAssets(body));
        if (versionedBody != null && !versionedBody.equals(body)) {
            response.body(versionedBody);
        }
    }

    static String pinThirdPartyCdnHtmlAssets(final String body) {
        String pinnedBody = body;
        for (Map.Entry<String, String> replacement : PINNED_CDN_REPLACEMENTS.entrySet()) {
            pinnedBody = pinnedBody.replace(replacement.getKey(), replacement.getValue());
        }
        return pinnedBody;
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
                || path.equals("/blog/feed.xml")
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

    private static boolean isNoindexFollowHtmlPagePath(final String path) {
        return isSwaggerUiPath(path)
                || path.equals("/gui/challenges")
                || path.startsWith("/gui/challenges/")
                || path.equals("/gui/entities")
                || path.startsWith("/gui/entities/")
                || path.equals("/gui/instances")
                || path.startsWith("/gui/instances/")
                || path.startsWith("/gui/instance/")
                || path.matches("^/(apichallenges|simpleapi|shop)/client$")
                || path.equals("/simpleapi/gui")
                || path.startsWith("/simpleapi/gui/")
                || path.equals("/shop/gui")
                || path.startsWith("/shop/gui/")
                || path.matches("^/(sim|mirror)/docs$");
    }

    private static boolean isNoindexApiResourcePath(final String path) {
        return path.equals("/docs/openapi.json")
                || path.equals("/docs/swagger")
                || path.matches("^/docs/openapi-3\\.[0-9]\\.json$")
                || path.matches("^/(simpleapi|sim|shop|mirror|fromhell)/docs/swagger$")
                || path.matches(
                        "^/(simpleapi|sim|shop|mirror|fromhell)/docs/(openapi|openapi-3\\.[0-9])\\.json$")
                || path.equals("/gui/challenge-status")
                || path.startsWith("/gui/challenge-status/")
                || path.equals("/mirror/request")
                || path.startsWith("/mirror/request/")
                || path.equals("/mirror/raw")
                || path.startsWith("/mirror/raw/")
                || path.equals("/challenger")
                || path.startsWith("/challenger/")
                || path.equals("/secret")
                || path.startsWith("/secret/")
                || path.equals("/todos")
                || path.startsWith("/todos/")
                || path.equals("/challenges")
                || path.equals("/heartbeat")
                || isPracticeApiResourcePath(path);
    }

    private static boolean isPracticeApiResourcePath(final String path) {
        return isApiResourceUnderPrefix(path, "/simpleapi")
                || isApiResourceUnderPrefix(path, "/shop")
                || isApiResourceUnderPrefix(path, "/sim")
                || isApiResourceUnderPrefix(path, "/fromhell");
    }

    private static boolean isApiResourceUnderPrefix(final String path, final String prefix) {
        return path.startsWith(prefix + "/")
                && !path.startsWith(prefix + "/docs")
                && !path.equals(prefix + "/client")
                && !path.startsWith(prefix + "/client/")
                && !path.equals(prefix + "/gui")
                && !path.startsWith(prefix + "/gui/");
    }

    private static boolean isContentDocumentationPath(final String path) {
        return path.startsWith("/apichallenges")
                || path.startsWith("/author")
                || path.startsWith("/blog")
                || path.startsWith("/learning")
                || path.startsWith("/practice-modes")
                || path.startsWith("/tools")
                || path.startsWith("/tutorials")
                || path.equals("/seo-metadata-test-page")
                || path.equals("/seo-metadata-minimal-page")
                || path.equals("/seo-metadata-video-page");
    }
}
