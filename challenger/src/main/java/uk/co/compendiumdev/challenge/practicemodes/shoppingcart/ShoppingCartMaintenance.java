package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class ShoppingCartMaintenance {

    static final int MIN_STOCKED_PRODUCTS = 10;
    static final int RESTOCK_QUANTITY = 12;

    private final Thingifier thingifier;

    ShoppingCartMaintenance(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    void run() {
        final ThingStore store = ShoppingCartSupport.store(thingifier);
        if (store == null) {
            return;
        }

        pruneOldCarts(store);
        maintainProducts(store);
    }

    void pruneBeforeCartCreate() {
        final ThingStore store = ShoppingCartSupport.store(thingifier);
        if (store == null) {
            return;
        }

        final List<EntityInstance> carts =
                ShoppingCartSupport.sortedById(ShoppingCartSupport.list(thingifier, store, "cart"));
        if (carts.size() < ShoppingCartThingifier.MAX_CARTS) {
            return;
        }

        deleteCartAndItems(store, carts.get(0));
    }

    private void pruneOldCarts(final ThingStore store) {
        List<EntityInstance> carts =
                ShoppingCartSupport.list(thingifier, store, "cart").stream()
                        .sorted(
                                (left, right) ->
                                        Integer.compare(
                                                ShoppingCartSupport.intValue(left, "createdTick"),
                                                ShoppingCartSupport.intValue(right, "createdTick")))
                        .toList();

        while (carts.size() > ShoppingCartThingifier.MAX_CARTS) {
            deleteCartAndItems(store, carts.get(0));
            carts = carts.subList(1, carts.size());
        }
    }

    void deleteCartAndItems(final ThingStore store, final EntityInstance cart) {
        for (EntityInstance item : ShoppingCartSupport.related(store, cart, "items")) {
            store.relationships().removeAll(item);
            store.entities().delete(item);
        }
        store.relationships().removeAll(cart);
        store.entities().delete(cart);
    }

    private void maintainProducts(final ThingStore store) {
        int stockedCount = stockedProducts(store).size();
        if (stockedCount >= MIN_STOCKED_PRODUCTS) {
            return;
        }

        final List<EntityInstance> products =
                ShoppingCartSupport.sortedById(
                        ShoppingCartSupport.list(thingifier, store, "product"));

        for (EntityInstance product : products) {
            if (stockedCount >= MIN_STOCKED_PRODUCTS) {
                return;
            }
            if (ShoppingCartSupport.intValue(product, "stock") <= 0) {
                ShoppingCartSupport.patch(
                        store, product, "stock", String.valueOf(RESTOCK_QUANTITY));
                stockedCount++;
            }
        }

        while (stockedCount < MIN_STOCKED_PRODUCTS
                && products.size() < ShoppingCartThingifier.MAX_PRODUCTS) {
            final List<String> usedCodes =
                    ShoppingCartSupport.list(thingifier, store, "product").stream()
                            .map(product -> ShoppingCartSupport.stringValue(product, "productCode"))
                            .toList();
            final ShoppingCartCatalogue.ProductSeed next =
                    ShoppingCartCatalogue.nextUnusedProduct(usedCodes).orElse(null);
            if (next == null) {
                return;
            }
            final EntityDefinition productDefinition =
                    ShoppingCartSupport.definition(thingifier, "product");
            ShoppingCartDataPopulator.createProduct(store, productDefinition, next);
            stockedCount++;
        }
    }

    private List<EntityInstance> stockedProducts(final ThingStore store) {
        return ShoppingCartSupport.list(thingifier, store, "product").stream()
                .filter(product -> ShoppingCartSupport.intValue(product, "stock") > 0)
                .toList();
    }
}
