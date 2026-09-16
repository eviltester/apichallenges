package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.Collection;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;

final class ApiChallengeMediaTypes {
    static final String APPLICATION_JSON = "application/json";
    static final String APPLICATION_XML = "application/xml";
    static final String TEXT_XML = "text/xml";
    static final String TODO_VENDOR_XML = "application/vnd.apichallenges.todo+xml";
    static final String TODO_STRUCTURED_XML = "application/todo+xml";
    static final String STRUCTURED_JSON_QUERY = "application/vnd.thingifier.query+json";
    static final Collection<String> TODO_XML_ENTITY_NAMES = List.of("todo", "todos");

    private ApiChallengeMediaTypes() {}

    static boolean requestContentTypeIs(
            final ChallengerApiExchange exchange, final String expectedType) {
        return new ContentTypeHeaderParser(exchange.requestHeader("Content-Type"))
                .isMediaType(expectedType);
    }

    static boolean acceptHeaderEquals(
            final ChallengerApiExchange exchange, final String expectedValue) {
        return normalizedHeaderValue(exchange.requestHeader("Accept")).equals(expectedValue);
    }

    static boolean isTodoXmlResponseType(final String contentType) {
        return APPLICATION_XML.equals(contentType)
                || TEXT_XML.equals(contentType)
                || TODO_VENDOR_XML.equals(contentType)
                || TODO_STRUCTURED_XML.equals(contentType);
    }

    static String normalizedHeaderValue(final String headerValue) {
        if (headerValue == null) {
            return "";
        }

        int parameterIndex = headerValue.indexOf(";");
        String mediaType =
                parameterIndex == -1 ? headerValue : headerValue.substring(0, parameterIndex);
        return mediaType.trim().toLowerCase();
    }
}
