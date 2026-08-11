package uk.co.compendiumdev.challenge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ThirdPartyCdnDependencyTest {

    private static final Pattern CDN_URL_PATTERN =
            Pattern.compile(
                    "https://(?:unpkg\\.com|cdn\\.jsdelivr\\.net|cdn\\.redoc\\.ly|cdn\\.zudoku\\.dev|fonts\\.gstatic\\.com)[^\\\\\"'\\s<>)]*");
    private static final String SEMVER = "\\d+\\.\\d+\\.\\d+(?:[-+][A-Za-z0-9._-]+)?";
    private static final Pattern UNPKG_PINNED_NPM_PACKAGE =
            Pattern.compile(
                    "^https://unpkg\\.com/(?:@[^/]+/[^/@]+|[^/@/]+)@" + SEMVER + "(?:/.*)?$");
    private static final Pattern JSDELIVR_PINNED_NPM_PACKAGE =
            Pattern.compile(
                    "^https://cdn\\.jsdelivr\\.net/npm/(?:@[^/]+/[^/@]+|[^/@/]+)@"
                            + SEMVER
                            + "(?:/.*)?$");
    private static final Pattern ZUDOKU_PINNED_PACKAGE =
            Pattern.compile("^https://cdn\\.zudoku\\.dev/" + SEMVER + "/.*$");
    private static final Pattern GOOGLE_FONT_PINNED_PACKAGE =
            Pattern.compile("^https://fonts\\.gstatic\\.com/s/[^/]+/v\\d+/.*$");

    private static final Set<String> PINNED_CDN_DEPENDENCY_INVENTORY =
            Set.of(
                    "https://cdn.jsdelivr.net/npm/@justinribeiro/lite-youtube@1.5.0/lite-youtube.js",
                    "https://cdn.jsdelivr.net/npm/@scalar/api-reference@1.64.1",
                    "https://cdn.jsdelivr.net/npm/redoc@2.5.3/bundles/redoc.standalone.js",
                    "https://cdn.zudoku.dev/0.83.0/main.js",
                    "https://cdn.zudoku.dev/0.83.0/zudoku.css",
                    "https://fonts.gstatic.com/s/galindo/v24/HI_KiYMeLqVKqwyuQ5HiRp-dhpQ.ttf",
                    "https://unpkg.com/@stoplight/elements@9.0.24/styles.min.css",
                    "https://unpkg.com/@stoplight/elements@9.0.24/web-components.min.js",
                    "https://unpkg.com/openapi-explorer@2.4.820/dist/browser/openapi-explorer.min.js",
                    "https://unpkg.com/swagger-ui-dist@5.32.12/swagger-ui-bundle.js",
                    "https://unpkg.com/swagger-ui-dist@5.32.12/swagger-ui-standalone-preset.js",
                    "https://unpkg.com/swagger-ui-dist@5.32.12/swagger-ui.css");

    @Test
    void thirdPartyCdnUrlsArePinnedAndInventoried() throws IOException {
        final Set<String> discoveredUrls = discoverMainSourceCdnUrls();

        Assertions.assertEquals(
                new TreeSet<>(PINNED_CDN_DEPENDENCY_INVENTORY),
                discoveredUrls,
                "Update this inventory when adding or changing third-party CDN dependencies.");

        discoveredUrls.forEach(this::assertExplicitlyPinned);
    }

    private void assertExplicitlyPinned(final String url) {
        Assertions.assertFalse(url.contains("/latest"), url);
        Assertions.assertFalse(url.contains("@latest"), url);

        if (url.startsWith("https://unpkg.com/")) {
            Assertions.assertTrue(UNPKG_PINNED_NPM_PACKAGE.matcher(url).matches(), url);
            return;
        }

        if (url.startsWith("https://cdn.jsdelivr.net/npm/")) {
            Assertions.assertTrue(JSDELIVR_PINNED_NPM_PACKAGE.matcher(url).matches(), url);
            return;
        }

        if (url.startsWith("https://cdn.zudoku.dev/")) {
            Assertions.assertTrue(ZUDOKU_PINNED_PACKAGE.matcher(url).matches(), url);
            return;
        }

        if (url.startsWith("https://fonts.gstatic.com/")) {
            Assertions.assertTrue(GOOGLE_FONT_PINNED_PACKAGE.matcher(url).matches(), url);
            return;
        }

        Assertions.fail("Unexpected CDN host: " + url);
    }

    private Set<String> discoverMainSourceCdnUrls() throws IOException {
        final Path projectRoot = projectRoot();
        final Set<String> urls = new TreeSet<>();
        for (final Path root :
                Set.of(
                        projectRoot.resolve("src/main/resources"),
                        projectRoot.resolve("src/main/java"))) {
            if (!Files.exists(root)) {
                continue;
            }

            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .filter(this::isTextSource)
                        .forEach(path -> urls.addAll(cdnUrlsIn(path)));
            }
        }
        return urls;
    }

    private Path projectRoot() {
        final Path currentDirectory = Path.of("").toAbsolutePath();
        if (Files.exists(currentDirectory.resolve("src/main/resources"))) {
            return currentDirectory;
        }
        return currentDirectory.resolve("challenger");
    }

    private boolean isTextSource(final Path path) {
        final String fileName = path.getFileName().toString();
        return fileName.endsWith(".java")
                || fileName.endsWith(".md")
                || fileName.endsWith(".html")
                || fileName.endsWith(".js")
                || fileName.endsWith(".css")
                || fileName.endsWith(".txt");
    }

    private Set<String> cdnUrlsIn(final Path path) {
        try {
            final String content = Files.readString(path, StandardCharsets.UTF_8);
            final Matcher matcher = CDN_URL_PATTERN.matcher(content);
            final Set<String> urls = new TreeSet<>();
            while (matcher.find()) {
                urls.add(matcher.group());
            }
            return urls;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }
}
