package uk.co.compendiumdev.challenge;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;

public class CdnCachePolicyTest {

    @Test
    void htmlAppPagesDeclareNoindexFollowRobotsHeader() {

        for (String path :
                List.of(
                        "/docs/swagger-ui",
                        "/simpleapi/docs/swagger-ui",
                        "/sim/docs/swagger-ui",
                        "/shop/docs/swagger-ui",
                        "/mirror/docs/swagger-ui",
                        "/fromhell/docs/swagger-ui",
                        "/gui/challenges",
                        "/gui/challenges/current",
                        "/gui/entities",
                        "/gui/entities/todo",
                        "/gui/instances",
                        "/gui/instances/todo",
                        "/gui/instance/todo/1",
                        "/apichallenges/client",
                        "/simpleapi/client",
                        "/shop/client",
                        "/simpleapi/gui",
                        "/simpleapi/gui/entities",
                        "/shop/gui",
                        "/shop/gui/entities",
                        "/sim/docs",
                        "/mirror/docs")) {
            final TestResponse response = new TestResponse();

            CdnCachePolicy.applyRobotsPolicy(new TestRequest(path), response);

            Assertions.assertEquals("noindex, follow", response.headers().get("X-Robots-Tag"));
        }
    }

    @Test
    void apiResourcesDeclareNoindexRobotsHeader() {
        for (String path :
                List.of(
                        "/docs/openapi.json",
                        "/docs/openapi-3.0.json",
                        "/docs/openapi-3.1.json",
                        "/docs/openapi-3.2.json",
                        "/docs/swagger",
                        "/simpleapi/docs/openapi.json",
                        "/simpleapi/docs/openapi-3.1.json",
                        "/simpleapi/docs/swagger",
                        "/sim/docs/openapi.json",
                        "/shop/docs/openapi.json",
                        "/mirror/docs/openapi.json",
                        "/fromhell/docs/openapi.json",
                        "/gui/challenge-status",
                        "/gui/challenge-status/current",
                        "/mirror/request",
                        "/mirror/request/anything",
                        "/mirror/raw",
                        "/mirror/raw/anything",
                        "/challenger",
                        "/challenger/current",
                        "/secret",
                        "/secret/token",
                        "/todos",
                        "/todos/1",
                        "/challenges",
                        "/heartbeat",
                        "/simpleapi/items",
                        "/simpleapi/randomisbn",
                        "/shop/products",
                        "/shop/register",
                        "/sim/entities",
                        "/fromhell/status/200")) {
            final TestResponse response = new TestResponse();

            CdnCachePolicy.applyRobotsPolicy(new TestRequest(path), response);

            Assertions.assertEquals("noindex", response.headers().get("X-Robots-Tag"), path);
        }
    }

    @Test
    void indexableDocsAndSeoPagesDoNotDeclareNoindexRobotsHeader() {

        for (String path :
                List.of(
                        "/docs",
                        "/simpleapi/docs",
                        "/shop/docs",
                        "/learning",
                        "/practice-modes/simpleapi",
                        "/tools/online-clients",
                        "/tools/online-clients/basic-client",
                        "/tools/online-clients/swagger",
                        "/tools/online-clients/openapi-explorer",
                        "/tools/online-clients/scalar",
                        "/tools/online-clients/stoplight",
                        "/tools/online-clients/zudoku",
                        "/tools/online-clients/redoc",
                        "/tools/online-clients/openapi-converter")) {
            final TestResponse response = new TestResponse();

            CdnCachePolicy.applyRobotsPolicy(new TestRequest(path), response);

            Assertions.assertFalse(response.containsHeader("X-Robots-Tag"), path);
        }
    }

    @Test
    void generatedSwaggerUiCdnUrlsArePinnedToExplicitVersions() {
        final String generatedHtml =
                """
                <link rel='stylesheet' href='https://unpkg.com/swagger-ui-dist/swagger-ui.css'>
                <script src='https://unpkg.com/swagger-ui-dist/swagger-ui-bundle.js'></script>
                <script src='https://unpkg.com/swagger-ui-dist/swagger-ui-standalone-preset.js'></script>
                """;

        final String pinnedHtml = CdnCachePolicy.pinThirdPartyCdnHtmlAssets(generatedHtml);

        Assertions.assertTrue(
                pinnedHtml.contains("https://unpkg.com/swagger-ui-dist@5.32.12/swagger-ui.css"));
        Assertions.assertTrue(
                pinnedHtml.contains(
                        "https://unpkg.com/swagger-ui-dist@5.32.12/swagger-ui-bundle.js"));
        Assertions.assertTrue(
                pinnedHtml.contains(
                        "https://unpkg.com/swagger-ui-dist@5.32.12/swagger-ui-standalone-preset.js"));
        Assertions.assertFalse(
                pinnedHtml.contains("https://unpkg.com/swagger-ui-dist/swagger-ui.css"));
        Assertions.assertFalse(
                pinnedHtml.contains("https://unpkg.com/swagger-ui-dist/swagger-ui-bundle.js"));
        Assertions.assertFalse(
                pinnedHtml.contains(
                        "https://unpkg.com/swagger-ui-dist/swagger-ui-standalone-preset.js"));
    }

    private record TestRequest(String path) implements HttpServerRequest {

        @Override
        public Object attribute(final String name) {
            return null;
        }

        @Override
        public void attribute(final String name, final Object value) {}

        @Override
        public String body() {
            return "";
        }

        @Override
        public String contentLength() {
            return "";
        }

        @Override
        public String cookie(final String name) {
            return "";
        }

        @Override
        public String header(final String name) {
            return "";
        }

        @Override
        public Set<String> headerNames() {
            return Collections.emptySet();
        }

        @Override
        public String host() {
            return "";
        }

        @Override
        public String ip() {
            return "";
        }

        @Override
        public String method() {
            return "GET";
        }

        @Override
        public String pathInfo() {
            return path;
        }

        @Override
        public String protocol() {
            return "HTTP/1.1";
        }

        @Override
        public String queryParam(final String name) {
            return "";
        }

        @Override
        public Set<String> queryParamNames() {
            return Collections.emptySet();
        }

        @Override
        public List<String> queryParams(final String name) {
            return Collections.emptyList();
        }

        @Override
        public Map<String, List<String>> queryParamMap() {
            return Collections.emptyMap();
        }

        @Override
        public String queryString() {
            return "";
        }

        @Override
        public String scheme() {
            return "http";
        }

        @Override
        public String splat() {
            return "";
        }

        @Override
        public String[] splatValues() {
            return new String[0];
        }

        @Override
        public String url() {
            return "http://localhost:4567" + path;
        }

        @Override
        public Map<String, String> urlParams() {
            return Collections.emptyMap();
        }
    }

    private static class TestResponse implements HttpServerResponse {
        private final Map<String, String> headers = new HashMap<>();
        private String body = "";
        private int status = 200;
        private String type = "";

        @Override
        public String body() {
            return body;
        }

        @Override
        public void body(final String body) {
            this.body = body;
        }

        @Override
        public void forceBody(final String body) {
            this.body = body;
        }

        @Override
        public boolean containsHeader(final String name) {
            return headers.containsKey(name);
        }

        @Override
        public void header(final String name, final String value) {
            headers.put(name, value);
        }

        @Override
        public Map<String, String> headers() {
            return headers;
        }

        @Override
        public void redirect(final String location) {}

        @Override
        public void redirect(final String location, final int statusCode) {
            status = statusCode;
        }

        @Override
        public int status() {
            return status;
        }

        @Override
        public void status(final int statusCode) {
            status = statusCode;
        }

        @Override
        public void suppressContentType() {
            type = "";
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public void type(final String contentType) {
            type = contentType;
        }
    }
}
