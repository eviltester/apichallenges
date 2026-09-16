package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiFinalResponse;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationContext;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

interface ChallengerApiExchange {
    String verb();

    String path();

    String requestHeader(String name);

    default boolean hasRequestHeader(final String name) {
        return requestHeader(name) != null && !requestHeader(name).isEmpty();
    }

    String requestBody();

    Map<String, String> rawQueryParams();

    QueryFilterParams queryParams();

    int statusCode();

    String responseContentType();

    default boolean responseTypeIs(final String expectedType) {
        return responseContentType() != null && responseContentType().contentEquals(expectedType);
    }

    String responseBody();

    String responseHeader(String name);

    Collection<String> errorMessages();

    ThingifierApiConfig config();

    ThingStore store();
}

final class RouteChallengerApiExchange implements ChallengerApiExchange {
    private final ThingifierApiOperationContext context;
    private final ThingifierApiFinalResponse response;
    private final Map<String, String> rawQueryParams;

    RouteChallengerApiExchange(
            final ThingifierApiOperationContext context,
            final ThingifierApiFinalResponse response) {
        this.context = context;
        this.response = response;
        this.rawQueryParams = rawQueryParamsFrom(context.queryParams());
    }

    @Override
    public String verb() {
        return context.verb().name();
    }

    @Override
    public String path() {
        return withoutLeadingSlash(context.internalPath());
    }

    @Override
    public String requestHeader(final String name) {
        return context.requestHeaders().get(name);
    }

    @Override
    public String requestBody() {
        return context.rawRequestBody() == null ? "" : context.rawRequestBody();
    }

    @Override
    public Map<String, String> rawQueryParams() {
        return rawQueryParams;
    }

    @Override
    public QueryFilterParams queryParams() {
        return context.queryParams();
    }

    @Override
    public int statusCode() {
        return response.statusCode();
    }

    @Override
    public String responseContentType() {
        return response.contentType();
    }

    @Override
    public String responseBody() {
        return response.body().orElse("");
    }

    @Override
    public String responseHeader(final String name) {
        return response.headers().get(name);
    }

    @Override
    public Collection<String> errorMessages() {
        return response.apiResponse().getErrorMessages();
    }

    @Override
    public ThingifierApiConfig config() {
        return context.apiConfig();
    }

    @Override
    public ThingStore store() {
        return context.store();
    }

    private static Map<String, String> rawQueryParamsFrom(final QueryFilterParams queryParams) {
        Map<String, String> rawQueryParams = new LinkedHashMap<>();
        if (queryParams == null) {
            return rawQueryParams;
        }

        for (FilterBy filterBy : queryParams.toList()) {
            rawQueryParams.put(filterBy.fieldName, filterBy.fieldValue);
        }
        return rawQueryParams;
    }

    private static String withoutLeadingSlash(final String path) {
        if (path == null) {
            return "";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
