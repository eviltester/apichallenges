package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import static uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpMethod.OPTIONS;

import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

final class ShoppingCartCorsHeadersResponseHook implements InternalHttpResponseHook {

    @Override
    public void run(final InternalHttpRequest request, final InternalHttpResponse response) {
        if (!request.getPath().startsWith("shop")) {
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
