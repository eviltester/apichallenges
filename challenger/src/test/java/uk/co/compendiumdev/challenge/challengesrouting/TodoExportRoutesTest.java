package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.serverstart.Environment;

public class TodoExportRoutesTest {

    private static HttpMessageSender http;

    @BeforeAll
    static void createHttp() {
        http = new HttpMessageSender(Environment.getBaseUri());
    }

    @Test
    void optionsOnTodoExportAllowsBrowserPreflight() {
        http.clearHeaders();
        http.setHeader("Origin", "https://example.test");
        http.setHeader("Access-Control-Request-Method", "GET");
        http.setHeader("Access-Control-Request-Headers", "X-CHALLENGER");

        final HttpResponseDetails response = http.send("/todos/export", "options");

        Assertions.assertEquals(204, response.statusCode);
        Assertions.assertEquals("GET, OPTIONS", response.getHeader("Allow"));
        Assertions.assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
        Assertions.assertEquals("X-CHALLENGER", response.getHeader("Access-Control-Allow-Headers"));
        Assertions.assertEquals("GET", response.getHeader("Access-Control-Allow-Methods"));
        Assertions.assertNull(response.getHeader("Access-Control-Allow-Credentials"));
    }

    @ParameterizedTest(name = "todo export route status {0} for {1}")
    @MethodSource("todoExportRoutingStatus")
    void todoExportRoutingStatus(final int statusCode, final String verb) {
        http.clearHeaders();

        final HttpResponseDetails response = http.send("/todos/export?format=csv", verb);

        Assertions.assertEquals(statusCode, response.statusCode);
    }

    private static Stream<Arguments> todoExportRoutingStatus() {
        return Stream.of(
                Arguments.of(200, "get"),
                Arguments.of(204, "options"),
                Arguments.of(405, "post"),
                Arguments.of(405, "put"),
                Arguments.of(405, "patch"),
                Arguments.of(405, "delete"),
                Arguments.of(405, "trace"));
    }
}
