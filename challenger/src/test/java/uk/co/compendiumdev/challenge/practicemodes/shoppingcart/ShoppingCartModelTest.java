package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

class ShoppingCartModelTest {

    @Test
    void modelDefinesExpectedEntitiesFieldsLimitsAndRelationships() {
        final Thingifier shop = new ShoppingCartThingifier().get();

        final EntityDefinition product = shop.getDefinitionNamed("product");
        final EntityDefinition cart = shop.getDefinitionNamed("cart");
        final EntityDefinition cartItem = shop.getDefinitionNamed("cartitem");

        Assertions.assertEquals(ShoppingCartThingifier.MAX_PRODUCTS, product.getMaxInstanceLimit());
        Assertions.assertEquals(ShoppingCartThingifier.MAX_CARTS, cart.getMaxInstanceLimit());
        Assertions.assertEquals(
                ShoppingCartThingifier.MAX_CART_ITEMS, cartItem.getMaxInstanceLimit());

        Assertions.assertEquals(FieldType.AUTO_INCREMENT, product.getField("id").getType());
        Assertions.assertEquals(FieldType.ENUM, product.getField("productCode").getType());
        Assertions.assertEquals(FieldType.ENUM, product.getField("category").getType());
        Assertions.assertEquals(FieldType.FLOAT, product.getField("unitPrice").getType());
        Assertions.assertEquals(FieldType.INTEGER, product.getField("stock").getType());

        Assertions.assertEquals(FieldType.AUTO_GUID, cart.getField("token").getType());
        Assertions.assertEquals(FieldType.ENUM, cart.getField("state").getType());
        Assertions.assertEquals(FieldType.INTEGER, cart.getField("createdTick").getType());
        Assertions.assertEquals(FieldType.INTEGER, cart.getField("updatedTick").getType());
        Assertions.assertEquals(FieldType.INTEGER, cart.getField("checkoutTick").getType());
        Assertions.assertTrue(cart.hasViewNamed("PublicCart"));
        Assertions.assertEquals(
                "PublicCart", shop.guiConfig().dataExplorer().responseViewNameFor(cart).orElse(""));
        Assertions.assertEquals(
                "PublicCart", shop.apiSpec().defaultRequestEntityViewFor(cart).orElse(""));
        Assertions.assertEquals(
                "PublicCart", shop.apiSpec().defaultResponseEntityViewFor(cart).orElse(""));
        final EntityViewDefinition publicCart = cart.getViewNamed("PublicCart");
        Assertions.assertTrue(publicCart.isResponseVisible("id"));
        Assertions.assertTrue(publicCart.isResponseVisible("state"));
        Assertions.assertTrue(publicCart.isResponseVisible("createdTick"));
        Assertions.assertTrue(publicCart.isResponseVisible("updatedTick"));
        Assertions.assertTrue(publicCart.isResponseVisible("checkoutTick"));
        Assertions.assertFalse(publicCart.isResponseVisible("token"));
        Assertions.assertFalse(publicCart.isRequestVisible("token"));
        Assertions.assertFalse(publicCart.isInputAllowed("token"));

        Assertions.assertEquals(FieldType.INTEGER, cartItem.getField("productId").getType());
        Assertions.assertEquals(FieldType.INTEGER, cartItem.getField("quantity").getType());
        Assertions.assertEquals(FieldType.FLOAT, cartItem.getField("unitPriceAtAdd").getType());
        Assertions.assertEquals(FieldType.INTEGER, cartItem.getField("stockAtAdd").getType());
        Assertions.assertTrue(cartItem.hasViewNamed("AddedCartItem"));
        final EntityViewDefinition addedCartItem = cartItem.getViewNamed("AddedCartItem");
        Assertions.assertTrue(addedCartItem.isRequestVisible("productId"));
        Assertions.assertTrue(addedCartItem.isRequestVisible("quantity"));
        Assertions.assertFalse(addedCartItem.isRequestVisible("unitPriceAtAdd"));
        Assertions.assertFalse(addedCartItem.isRequestVisible("stockAtAdd"));
        Assertions.assertTrue(addedCartItem.isResponseVisible("unitPriceAtAdd"));
        Assertions.assertTrue(addedCartItem.isResponseVisible("stockAtAdd"));
        Assertions.assertTrue(addedCartItem.isInputAllowed("unitPriceAtAdd"));
        Assertions.assertTrue(addedCartItem.isInputAllowed("stockAtAdd"));

        Assertions.assertTrue(shop.hasRelationshipNamed("items"));
        Assertions.assertTrue(shop.hasRelationshipNamed("cartitems"));
        Assertions.assertTrue(shop.hasRelationshipNamed("cart"));
        Assertions.assertTrue(shop.hasRelationshipNamed("product"));
    }

    @Test
    void mutableFieldsDoNotUseFreeTextStrings() {
        final Thingifier shop = new ShoppingCartThingifier().get();

        for (String entityName : List.of("product", "cart", "cartitem")) {
            final EntityDefinition definition = shop.getDefinitionNamed(entityName);
            for (String fieldName : definition.getFieldNames()) {
                Assertions.assertNotEquals(
                        FieldType.STRING,
                        definition.getField(fieldName).getType(),
                        entityName + "." + fieldName);
            }
        }
    }

    @Test
    void seedDataStartsBelowCapsAndHasAtLeastTenStockedProducts() {
        final Thingifier shop = new ShoppingCartThingifier().get();
        final ThingStore store = shop.getERmodel().getStore(EntityRelModel.DEFAULT_DATABASE_NAME);

        final List<EntityInstance> products = ShoppingCartSupport.list(shop, store, "product");
        final long stockedProducts =
                products.stream()
                        .filter(product -> ShoppingCartSupport.intValue(product, "stock") > 0)
                        .count();

        Assertions.assertTrue(products.size() <= ShoppingCartThingifier.MAX_PRODUCTS);
        Assertions.assertTrue(stockedProducts >= ShoppingCartMaintenance.MIN_STOCKED_PRODUCTS);
        Assertions.assertEquals(0, ShoppingCartSupport.list(shop, store, "cart").size());
        Assertions.assertEquals(0, ShoppingCartSupport.list(shop, store, "cartitem").size());
    }
}
