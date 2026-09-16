package uk.co.compendiumdev.challenge.challengesrouting;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.api.http.UrlQueryParamParser;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class RouteChallengeTestExchange implements ChallengerApiExchange {
    private final Map<String, String> requestHeaders = new LinkedHashMap<>();
    private final Map<String, String> responseHeaders = new LinkedHashMap<>();
    private final Map<String, String> rawQueryParams = new LinkedHashMap<>();
    private final Collection<String> errors = new java.util.ArrayList<>();
    private String verb = "GET";
    private String path = "todos";
    private String requestBody = "";
    private String responseBody = "";
    private String responseContentType = "";
    private int statusCode = 200;
    private QueryFilterParams queryParams = new QueryFilterParams();
    private ThingifierApiConfig config;
    private ThingStore store;

    RouteChallengeTestExchange withRequestHeader(final String name, final String value) {
        requestHeaders.put(name, value);
        return this;
    }

    RouteChallengeTestExchange withRequestBody(final String body) {
        requestBody = body;
        return this;
    }

    RouteChallengeTestExchange withResponseBody(final String body) {
        responseBody = body;
        return this;
    }

    RouteChallengeTestExchange withResponseHeader(final String name, final String value) {
        responseHeaders.put(name, value);
        return this;
    }

    RouteChallengeTestExchange withErrorMessage(final String message) {
        errors.add(message);
        return this;
    }

    RouteChallengeTestExchange withResponseContentType(final String contentType) {
        responseContentType = contentType;
        return this;
    }

    RouteChallengeTestExchange withStatusCode(final int code) {
        statusCode = code;
        return this;
    }

    RouteChallengeTestExchange withQuery(final String query) {
        rawQueryParams.clear();
        queryParams = new UrlQueryParamParser().parse(query);
        for (String pair : query.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int equals = pair.indexOf("=");
            String name = equals == -1 ? pair : pair.substring(0, equals);
            String value = equals == -1 ? "" : pair.substring(equals + 1);
            rawQueryParams.put(urlDecode(name), urlDecode(value));
        }
        return this;
    }

    RouteChallengeTestExchange withConfig(final ThingifierApiConfig apiConfig) {
        config = apiConfig;
        return this;
    }

    RouteChallengeTestExchange withStore(final ThingStore thingStore) {
        store = thingStore;
        return this;
    }

    @Override
    public String verb() {
        return verb;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String requestHeader(final String name) {
        return requestHeaders.get(name);
    }

    @Override
    public String requestBody() {
        return requestBody;
    }

    @Override
    public Map<String, String> rawQueryParams() {
        return rawQueryParams;
    }

    @Override
    public QueryFilterParams queryParams() {
        return queryParams;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String responseContentType() {
        return responseContentType;
    }

    @Override
    public String responseBody() {
        return responseBody;
    }

    @Override
    public String responseHeader(final String name) {
        return responseHeaders.get(name);
    }

    @Override
    public Collection<String> errorMessages() {
        return errors;
    }

    @Override
    public ThingifierApiConfig config() {
        return config;
    }

    @Override
    public ThingStore store() {
        return store;
    }

    private static String urlDecode(final String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
