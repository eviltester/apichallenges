package uk.co.compendiumdev.challenge;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AssetVersion {

    private static final Pattern LOCAL_ASSET_ATTRIBUTE =
            Pattern.compile("(\\b(?:href|src)=['\"])(/(?:css|js)/[^'\"?#]+)(['\"])");
    private static final String VERSION = resolveVersion(System.getenv());

    private AssetVersion() {}

    public static String versionedPath(final String path) {
        if (path == null || path.isEmpty() || path.contains("v=")) {
            return path;
        }

        final String separator = path.contains("?") ? "&" : "?";
        return path + separator + "v=" + versionForPath(path);
    }

    public static String versionHtmlAssetReferences(final String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        final Matcher matcher = LOCAL_ASSET_ATTRIBUTE.matcher(html);
        final StringBuffer versionedHtml = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(
                    versionedHtml,
                    Matcher.quoteReplacement(
                            matcher.group(1) + versionedPath(matcher.group(2)) + matcher.group(3)));
        }
        matcher.appendTail(versionedHtml);
        return versionedHtml.toString();
    }

    static String resolveVersion(final Map<String, String> environment) {
        final String railwayCommit = sanitized(environment.get("RAILWAY_GIT_COMMIT_SHA"));
        if (!railwayCommit.isEmpty()) {
            return railwayCommit.length() > 12 ? railwayCommit.substring(0, 12) : railwayCommit;
        }

        final String configuredVersion = sanitized(environment.get("APICHALLENGES_ASSET_VERSION"));
        if (!configuredVersion.isEmpty()) {
            return configuredVersion;
        }

        final Package packageInfo = AssetVersion.class.getPackage();
        if (packageInfo != null) {
            final String implementationVersion = sanitized(packageInfo.getImplementationVersion());
            if (!implementationVersion.isEmpty()) {
                return implementationVersion;
            }
        }

        return "dev";
    }

    static String devAssetVersion(final String path) {
        return localAssetVersion(path, VERSION);
    }

    static String localAssetVersion(final String path, final String fallbackVersion) {
        final URL assetUrl = AssetVersion.class.getResource("/public" + path);
        if (assetUrl == null) {
            return fallbackVersion;
        }

        try {
            final URLConnection connection = assetUrl.openConnection();
            connection.setUseCaches(false);
            final long lastModified = connection.getLastModified();
            return lastModified > 0 ? fallbackVersion + "-" + lastModified : fallbackVersion;
        } catch (IOException e) {
            return fallbackVersion;
        }
    }

    private static String versionForPath(final String path) {
        if (!"dev".equals(VERSION)) {
            return VERSION;
        }

        return devAssetVersion(path);
    }

    private static String sanitized(final String rawVersion) {
        if (rawVersion == null) {
            return "";
        }

        return rawVersion.trim().replaceAll("[^A-Za-z0-9._-]", "");
    }
}
