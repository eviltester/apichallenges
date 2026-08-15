package uk.co.compendiumdev.challenge.practicemodes.simpleapi;

import java.util.List;
import uk.co.compendiumdev.challenge.httpserver.CorsHeaders;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public class SimpleApiCorsHeadersResponseHook implements InternalHttpResponseHook {

    @Override
    public void run(final InternalHttpRequest request, final InternalHttpResponse response) {

        // TODO: hooks should only apply to a specific routing set and this should not be necessary
        List<String> validEndpointPrefixesToRunAgainst = List.of("simpleapi");
        String[] pathSegments = request.getPath().split("/");
        if (!validEndpointPrefixesToRunAgainst.contains(pathSegments[0])) {
            return;
        }

        // allow cross origin requests
        // and swagger
        // https://support.smartbear.com/swaggerhub/docs/en/edit-apis/cors-requirements-for--try-it-out-.html
        CorsHeaders.allowOpenCrossOriginRequests(request, response);
    }
}
