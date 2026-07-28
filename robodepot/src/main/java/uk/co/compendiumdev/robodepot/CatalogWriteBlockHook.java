package uk.co.compendiumdev.robodepot;

import java.util.List;
import java.util.Set;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public final class CatalogWriteBlockHook implements HttpApiRequestHook {

    private static final Set<String> CATALOG_ROUTES = Set.of("robotmodels", "skus");

    @Override
    public HttpApiResponse run(final HttpApiRequest request, final ThingifierApiConfig config) {
        if (!isWriteVerb(request)) {
            return null;
        }

        List<String> segments = ApiPath.segments(request.getPath());
        if (segments.size() == 1 || segments.size() == 2) {
            if (CATALOG_ROUTES.contains(segments.get(0))) {
                return HookResponses.error(request, config, 405, "Catalog resources are read-only");
            }
        }

        return null;
    }

    private boolean isWriteVerb(final HttpApiRequest request) {
        return request.getVerb() == HttpApiRequest.VERB.POST
                || request.getVerb() == HttpApiRequest.VERB.PUT
                || request.getVerb() == HttpApiRequest.VERB.DELETE;
    }
}
