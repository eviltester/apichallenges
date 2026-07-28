package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ShoppingCartDataPopulator implements RepositoryDataPopulator {

    @Override
    public void populate(final ERSchema schema, final ThingStore store) {
        final EntityDefinition product = schema.getEntityDefinitionNamed("product");
        ShoppingCartCatalogue.PRODUCTS.stream()
                .limit(12)
                .forEach(seed -> createProduct(store, product, seed));
    }

    static void createProduct(
            final ThingStore store,
            final EntityDefinition product,
            final ShoppingCartCatalogue.ProductSeed seed) {
        store.entities()
                .create(
                        EntityInstanceDraft.forEntity(product)
                                .withField("productCode", seed.productCode())
                                .withField("category", seed.category())
                                .withField("unitPrice", String.valueOf(seed.unitPrice()))
                                .withField("stock", String.valueOf(seed.stock())));
    }
}
