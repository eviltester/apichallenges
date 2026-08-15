package uk.co.compendiumdev.challenge.httpserver;

import static uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpMethod.OPTIONS;

import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public final class CorsHeaders {

    private static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    private static final String REQUEST_HEADERS = "Access-Control-Request-Headers";
    private static final String REQUEST_METHOD = "Access-Control-Request-Method";
    private static final String WILDCARD = "*";

    private CorsHeaders() {}

    public static void allowOpenCrossOriginRequests(
            final InternalHttpRequest request, final InternalHttpResponse response) {
        response.setHeader("Access-Control-Allow-Origin", WILDCARD);
        response.setHeader(ALLOW_HEADERS, preflightHeaderOrDefault(request, REQUEST_HEADERS));
        response.setHeader(ALLOW_METHODS, preflightHeaderOrDefault(request, REQUEST_METHOD));
        response.setHeader(EXPOSE_HEADERS, WILDCARD);
    }

    private static String preflightHeaderOrDefault(
            final InternalHttpRequest request, final String requestHeaderName) {
        if (request.getVerb() != OPTIONS || !request.getHeaders().headerExists(requestHeaderName)) {
            return WILDCARD;
        }

        final String value = request.getHeader(requestHeaderName);
        return value.isBlank() ? WILDCARD : value;
    }
}
