package uk.co.compendiumdev.robodepot;

import static uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpMethod.OPTIONS;

import java.util.List;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public class RoboDepotCorsHeadersResponseHook implements InternalHttpResponseHook {

    @Override
    public void run(final InternalHttpRequest request, final InternalHttpResponse response) {

        List<String> validEndpointPrefixesToRunAgainst = List.of("robodepot");
        String[] pathSegments = request.getPath().split("/");
        if (!validEndpointPrefixesToRunAgainst.contains(pathSegments[0])) {
            return;
        }

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        if (request.getVerb() == OPTIONS
                && request.getHeaders().headerExists("Access-Control-Allow-Methods")) {
            response.setHeader(
                    "Access-Control-Allow-Methods",
                    request.getHeader("Access-Control-Allow-Methods"));
        }
    }
}
