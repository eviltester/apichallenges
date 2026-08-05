package uk.co.compendiumdev.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AssetVersionTest {

    @Test
    void versionedPathAddsAnAssetVersion() {
        final String versionedPath = AssetVersion.versionedPath("/css/online-swagger-theme.css");

        assertTrue(
                versionedPath.matches(
                        "/css/online-swagger-theme\\.css\\?v=[A-Za-z0-9._-]+"),
                versionedPath);
    }

    @Test
    void localAssetVersionAddsATimestampWhenTheResourceExists() {
        final String version = AssetVersion.localAssetVersion(
                "/css/online-swagger-theme.css", "dev");

        assertTrue(version.matches("dev-[0-9]+"), version);
    }

    @Test
    void versionedPathDoesNotRewriteAlreadyVersionedPaths() {
        assertEquals(
                "/css/online-swagger-theme.css?v=already",
                AssetVersion.versionedPath("/css/online-swagger-theme.css?v=already"));
    }

    @Test
    void versionHtmlAssetReferencesVersionsLocalCssAndJavascript() {
        final String html =
                "<link rel=\"stylesheet\" href=\"/css/online-swagger-theme.css\">"
                        + "<script src='/js/openapi-tool-controls.js'></script>"
                        + "<script src=\"https://example.com/app.js\"></script>";

        final String versionedHtml = AssetVersion.versionHtmlAssetReferences(html);

        assertTrue(
                versionedHtml.matches(
                        "(?s).*href=\"/css/online-swagger-theme\\.css\\?v=[^\"]+\".*"),
                versionedHtml);
        assertTrue(
                versionedHtml.matches(
                        "(?s).*src='/js/openapi-tool-controls\\.js\\?v=[^']+'.*"),
                versionedHtml);
        assertTrue(versionedHtml.contains("src=\"https://example.com/app.js\""));
    }

    @Test
    void resolveVersionPrefersRailwayCommitThenConfiguredVersion() {
        assertEquals(
                "0123456789ab",
                AssetVersion.resolveVersion(
                        Map.of(
                                "RAILWAY_GIT_COMMIT_SHA",
                                "0123456789abcdef",
                                "APICHALLENGES_ASSET_VERSION",
                                "configured")));

        assertEquals(
                "configured-1",
                AssetVersion.resolveVersion(Map.of("APICHALLENGES_ASSET_VERSION", "configured-1")));
    }

    @Test
    void devAssetVersionFallsBackWhenTheLocalResourceIsUnknown() {
        assertEquals("dev", AssetVersion.localAssetVersion("/css/not-a-real-file.css", "dev"));
    }
}
