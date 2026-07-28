package uk.co.compendiumdev.robodepot;

import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

final class HookResponses {

    private HookResponses() {}

    static HttpApiResponse error(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final int status,
            final String message) {
        return new HttpApiResponse(
                request.getHeaders(),
                ApiResponse.error(status, message),
                new JsonThing(config.jsonOutput()),
                config);
    }

    static HttpApiResponse noContent(
            final HttpApiRequest request, final ThingifierApiConfig config) {
        return new HttpApiResponse(
                request.getHeaders(),
                ApiResponse.noContent(),
                new JsonThing(config.jsonOutput()),
                config);
    }
}
