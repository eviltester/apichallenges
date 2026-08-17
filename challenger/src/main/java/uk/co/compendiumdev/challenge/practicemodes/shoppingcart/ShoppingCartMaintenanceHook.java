package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

final class ShoppingCartMaintenanceHook implements InternalHttpResponseHook {

    private final ShoppingCartMaintenance maintenance;

    ShoppingCartMaintenanceHook(final ShoppingCartMaintenance maintenance) {
        this.maintenance = maintenance;
    }

    @Override
    public void run(final InternalHttpRequest request, final InternalHttpResponse response) {
        maintenance.run();
    }
}
