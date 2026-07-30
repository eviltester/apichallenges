package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.*;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProvider;

public final class ShoppingCartThingifier {

    public static final int MAX_PRODUCTS = 24;
    public static final int MAX_CARTS = 100;
    public static final int MAX_CART_ITEMS = 400;

    public Thingifier get() {
        return get(null);
    }

    public Thingifier get(final ThingStoreProvider storeProvider) {
        final EntityRelModel model =
                storeProvider == null ? new EntityRelModel() : new EntityRelModel(storeProvider);
        final Thingifier shop = new Thingifier(model);

        shop.setDocumentation(
                "Buggy API", "A deliberately buggy shopping cart API for API testing practice.");

        final EntityDefinition product =
                shop.defineThing("product", "products", MAX_PRODUCTS)
                        .withDescription(
                                "A safe fixed catalogue item with internally maintained stock.");
        product.addAsPrimaryKeyField(autoIncrement("id", "Unique product identifier."));
        product.addFields(
                enumField(
                                "productCode",
                                "Safe catalogue code for the product.",
                                "BOOK_API",
                                ShoppingCartCatalogue.productCodeExamples())
                        .makeMandatory()
                        .setMustBeUnique(true),
                enumField(
                                "category",
                                "Safe category for the product.",
                                "book",
                                ShoppingCartCatalogue.categoryExamples())
                        .makeMandatory(),
                Field.is("unitPrice", FLOAT)
                        .withDescription("Current unit price used for new cart lines.")
                        .withMinMaxValues(0.01f, 500.00f)
                        .withDefaultValue("1.00")
                        .makeMandatory(),
                Field.is("stock", INTEGER)
                        .withDescription("Current available stock count maintained by the API.")
                        .withMinMaxValues(-1000, 9999)
                        .withDefaultValue("0")
                        .makeMandatory());

        final EntityDefinition cart =
                shop.defineThing("cart", "carts", MAX_CARTS)
                        .withDescription("A user's shopping cart, created through registration.");
        cart.addAsPrimaryKeyField(autoIncrement("id", "Unique cart identifier."));
        cart.addFields(
                Field.is("token", AUTO_GUID)
                        .withDescription("Protected bearer token for cart mutation."),
                enumField(
                                "state",
                                "Lifecycle state of the cart.",
                                "open",
                                "open",
                                "closed",
                                "abandoned")
                        .makeMandatory(),
                Field.is("createdTick", INTEGER)
                        .withDescription("Internal tick when the cart was created.")
                        .withMinMaxValues(0, 999999)
                        .withDefaultValue("0"),
                Field.is("updatedTick", INTEGER)
                        .withDescription("Internal tick when the cart was last changed.")
                        .withMinMaxValues(0, 999999)
                        .withDefaultValue("0"),
                Field.is("checkoutTick", INTEGER)
                        .withDescription("Internal tick when checkout last completed.")
                        .withMinMaxValues(0, 999999)
                        .withDefaultValue("0"));
        cart.defineView("PublicCart")
                .hideRequestFields("token")
                .hideResponseFields("token")
                .disallowInputFields("token");
        shop.guiConfig().dataExplorer().responseView("cart", "PublicCart");

        final EntityDefinition cartItem =
                shop.defineThing("cartitem", "cartitems", MAX_CART_ITEMS)
                        .withDescription("A product line in a shopping cart.");
        cartItem.addAsPrimaryKeyField(autoIncrement("id", "Unique cart item identifier."));
        cartItem.addFields(
                Field.is("productId", INTEGER)
                        .withDescription("Product identifier requested for this cart line.")
                        .withMinMaxValues(1, MAX_PRODUCTS)
                        .withDefaultValue("1")
                        .makeMandatory(),
                Field.is("quantity", INTEGER)
                        .withDescription("Requested quantity for this cart line.")
                        .withMinMaxValues(-1000, 9999)
                        .withDefaultValue("1")
                        .makeMandatory(),
                Field.is("unitPriceAtAdd", FLOAT)
                        .withDescription("Product unit price captured when the line was added.")
                        .withMinMaxValues(0.0f, 500.00f)
                        .withDefaultValue("0.00"),
                Field.is("stockAtAdd", INTEGER)
                        .withDescription("Product stock captured when the line was added.")
                        .withMinMaxValues(-1000, 9999)
                        .withDefaultValue("0"));
        cartItem.defineView("AddedCartItem")
                .hideRequestFields("unitPriceAtAdd", "stockAtAdd")
                .allowInputFields("unitPriceAtAdd", "stockAtAdd");

        shop.defineRelationship(cart, cartItem, "items", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "cart");
        shop.defineRelationship(product, cartItem, "cartitems", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "product");

        shop.setDataGenerator(new ShoppingCartDataPopulator());
        shop.generateData(EntityRelModel.DEFAULT_DATABASE_NAME);

        final ThingifierApiConfigProfile profile =
                shop.apiConfigProfiles().createDefaultProfile("v1", "Buggy API");
        configurePublicApi(profile.apiConfig());
        shop.apiConfig().setFrom(profile.apiConfig());
        configurePublicApiSpec(shop);

        return shop;
    }

    private static Field autoIncrement(final String name, final String description) {
        return Field.is(name, AUTO_INCREMENT).withDescription(description);
    }

    private static Field enumField(
            final String name,
            final String description,
            final String defaultValue,
            final String... examples) {
        Field field =
                Field.is(name, ENUM).withDescription(description).withDefaultValue(defaultValue);
        for (String example : examples) {
            field.withExample(example);
        }
        return field;
    }

    private static void configurePublicApi(final ThingifierApiConfig config) {
        config.setFrom(new ThingifierApiConfig("/shop"));
        config.forParams().setAllowPagingThroughUrlParams(false);
        config.setReturnSingleGetItemsAsCollection(false);
        config.setApiToEnforceDeclaredTypesInInput(true);
        config.setApiToShowPrimaryKeyHeaderInResponse(true);
        config.setApiToAllowXmlForResponses(false);
        config.setApiToAllowXmlForContentType(false);
        config.setDefaultContentTypeAsJson(true);
        config.jsonOutput().setCompressRelationships(true);
    }

    private static void configurePublicApiSpec(final Thingifier shop) {
        shop.apiSpec()
                .route(RoutingVerb.POST, "/carts/{cartId}/items")
                .entityView("AddedCartItem")
                .secureWithBearerAuth()
                .addDocumentation("add a product line to a cart using the cart bearer token")
                .requestPayload("create_AddedCartItem");
        shop.apiSpec()
                .route(RoutingVerb.DELETE, "/carts/{cartId}/items/{itemId}")
                .secureWithBearerAuth()
                .addDocumentation("delete a product line from a cart using the cart bearer token");
        shop.apiSpec()
                .route(RoutingVerb.DELETE, "/carts/{cartId}")
                .secureWithBearerAuth()
                .addDocumentation("delete or abandon a cart using the cart bearer token");

        shop.apiSpec().route(RoutingVerb.POST, "/products").disable();
        shop.apiSpec().route(RoutingVerb.POST, "/products/{productId}").disable();
        shop.apiSpec().route(RoutingVerb.PUT, "/products/{productId}").disable();
        shop.apiSpec().route(RoutingVerb.DELETE, "/products/{productId}").disable();
        shop.apiSpec().disableRelationshipRoutes("/products", "cartitems");

        shop.apiSpec().route(RoutingVerb.POST, "/carts").disable();
        shop.apiSpec().route(RoutingVerb.POST, "/carts/{cartId}").hide();
        shop.apiSpec().route(RoutingVerb.PUT, "/carts/{cartId}").hide();

        shop.apiSpec().disableEntityRoutes("/cartitems");
        shop.apiSpec().disableRelationshipRoutes("/cartitems", "cart");
        shop.apiSpec().disableRelationshipRoutes("/cartitems", "product");
    }
}
