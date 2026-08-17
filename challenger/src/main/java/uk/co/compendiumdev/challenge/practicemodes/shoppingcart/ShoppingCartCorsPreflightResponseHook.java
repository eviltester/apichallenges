package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

final class ShoppingCartCorsPreflightResponseHook implements InternalHttpResponseHook {

    @Override
    public void run(final InternalHttpRequest request, final InternalHttpResponse response) {
        if (request.getHeaders().headerExists("Access-Control-Allow-Methods")) {
            response.setHeader(
                    "Access-Control-Allow-Methods",
                    request.getHeader("Access-Control-Allow-Methods"));
        }
    }
}
