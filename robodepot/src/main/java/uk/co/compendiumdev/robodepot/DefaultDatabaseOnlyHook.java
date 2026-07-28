package uk.co.compendiumdev.robodepot;

import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public final class DefaultDatabaseOnlyHook implements HttpApiRequestHook {

    @Override
    public HttpApiResponse run(final HttpApiRequest request, final ThingifierApiConfig config) {
        if (request.getHeaders().headerExists(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME)) {
            request.getHeaders().put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "");
        }
        return null;
    }
}
