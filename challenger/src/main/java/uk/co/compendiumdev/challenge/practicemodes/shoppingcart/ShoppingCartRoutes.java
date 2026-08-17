package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.*;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.INTEGER;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.adapter.httpserver.SimpleHttpRouteCreator;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierAutoDocGenRouting;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.routing.DefaultGuiRoutings;

public final class ShoppingCartRoutes {
    private static final List<String> SHOP_API_HOOK_ENDPOINTS =
            List.of(
                    "/shop/products",
                    "/shop/products/:productId",
                    "/shop/carts",
                    "/shop/carts/:cartId",
                    "/shop/carts/:cartId/items",
                    "/shop/carts/:cartId/items/:itemId",
                    "/shop/cartitems",
                    "/shop/cartitems/:itemId",
                    "/shop/register",
                    "/shop/checkout/:cartId");

    private final DefaultGUIHTML guiTemplates;
    private final ShoppingCartBugMode bugMode;
    private final ShoppingCartState state;
    private final ShoppingCartMaintenance maintenance;
    private final Thingifier shoppingCart;

    public ShoppingCartRoutes(
            final DefaultGUIHTML guiTemplates, final ShoppingCartBugMode bugMode) {
        this.guiTemplates = guiTemplates;
        this.bugMode = bugMode;
        this.state = new ShoppingCartState();
        this.shoppingCart = new ShoppingCartThingifier().get();
        this.maintenance = new ShoppingCartMaintenance(shoppingCart);
    }

    public void configure() {
        new DefaultGuiRoutings(shoppingCart, guiTemplates).configureRoutes("/shop/gui");

        final ThingifierApiDocumentationDefn apiDocDefn = documentation();
        addCustomRouteDocumentation(apiDocDefn);

        redirect.get("/shop", "/practice-modes/shoppingcart");
        registerCartCreationRoute();
        registerCheckoutRouteWithBookDvdCdAndDoubleCheckoutBugs();

        new ThingifierAutoDocGenRouting(shoppingCart, apiDocDefn, guiTemplates);

        final ThingifierHttpApiRoutings shopRouting =
                new ThingifierHttpApiRoutings(shoppingCart, apiDocDefn);
        registerBadQuantityHiddenFieldClosedCartAndCrossCartTokenBugsForPostCartItems(shopRouting);
        registerClosedCartCanStillDeleteCartItemBug(shopRouting);
        registerCartDeleteHook(shopRouting);
        registerReadOnlyRouteWriteRejectionHooks(shopRouting);
        registerTokenStrippingFromCartResponses(shopRouting);
        registerMaintenanceAfterShopApiResponses(shopRouting);
        registerBuggyCorsHeadersThatAllowCredentialsForWildcardOrigin(shopRouting);
        registerBuggyCorsPreflightThatReadsAllowMethodsRequestHeader(shopRouting);
    }

    Thingifier thingifier() {
        return shoppingCart;
    }

    private void registerBadQuantityHiddenFieldClosedCartAndCrossCartTokenBugsForPostCartItems(
            final ThingifierHttpApiRoutings shopRouting) {
        registerWriteHook(
                shopRouting,
                "/carts/:cartId/items",
                ShoppingCartWriteHook.cartItemWriteBugsForPostCartItems(
                        shoppingCart, bugMode, state, maintenance),
                RoutingVerb.POST);
    }

    private void registerClosedCartCanStillDeleteCartItemBug(
            final ThingifierHttpApiRoutings shopRouting) {
        registerWriteHook(
                shopRouting,
                "/carts/:cartId/items/:itemId",
                ShoppingCartWriteHook.closedCartCanStillDeleteCartItemBug(
                        shoppingCart, bugMode, state, maintenance),
                RoutingVerb.DELETE);
    }

    private void registerCartDeleteHook(final ThingifierHttpApiRoutings shopRouting) {
        registerWriteHook(
                shopRouting,
                "/carts/:cartId",
                ShoppingCartWriteHook.deleteCartAndItsItems(
                        shoppingCart, bugMode, state, maintenance),
                RoutingVerb.DELETE);
    }

    private void registerReadOnlyRouteWriteRejectionHooks(
            final ThingifierHttpApiRoutings shopRouting) {
        registerWriteHook(
                shopRouting,
                "/carts",
                ShoppingCartWriteHook.rejectWritesToReadOnlyShoppingCartRoutes(
                        shoppingCart, bugMode, state, maintenance),
                RoutingVerb.POST,
                RoutingVerb.PUT,
                RoutingVerb.DELETE);
        registerWriteHook(
                shopRouting,
                "/carts/:cartId",
                ShoppingCartWriteHook.rejectWritesToReadOnlyShoppingCartRoutes(
                        shoppingCart, bugMode, state, maintenance),
                RoutingVerb.POST,
                RoutingVerb.PUT);
        registerWriteHook(
                shopRouting,
                "/carts/:cartId/items",
                ShoppingCartWriteHook.rejectWritesToReadOnlyShoppingCartRoutes(
                        shoppingCart, bugMode, state, maintenance),
                RoutingVerb.PUT,
                RoutingVerb.DELETE);
        registerWriteHook(
                shopRouting,
                "/carts/:cartId/items/:itemId",
                ShoppingCartWriteHook.rejectWritesToReadOnlyShoppingCartRoutes(
                        shoppingCart, bugMode, state, maintenance),
                RoutingVerb.POST,
                RoutingVerb.PUT);
    }

    private void registerTokenStrippingFromCartResponses(
            final ThingifierHttpApiRoutings shopRouting) {
        final ShoppingCartTokenStripResponseHook tokenStripHook =
                new ShoppingCartTokenStripResponseHook();
        shopRouting.registerInternalHttpResponseHook("/shop/carts", tokenStripHook);
        shopRouting.registerInternalHttpResponseHook("/shop/carts/:cartId", tokenStripHook);
        shopRouting.registerInternalHttpResponseHook("/shop/carts/:cartId/items", tokenStripHook);
        shopRouting.registerInternalHttpResponseHook(
                "/shop/carts/:cartId/items/:itemId", tokenStripHook);
    }

    private void registerMaintenanceAfterShopApiResponses(
            final ThingifierHttpApiRoutings shopRouting) {
        registerShopResponseHook(shopRouting, new ShoppingCartMaintenanceHook(maintenance));
    }

    private void registerBuggyCorsHeadersThatAllowCredentialsForWildcardOrigin(
            final ThingifierHttpApiRoutings shopRouting) {
        registerShopResponseHook(shopRouting, new ShoppingCartCorsHeadersResponseHook());
    }

    private void registerBuggyCorsPreflightThatReadsAllowMethodsRequestHeader(
            final ThingifierHttpApiRoutings shopRouting) {
        registerShopResponseHook(
                shopRouting, new ShoppingCartCorsPreflightResponseHook(), RoutingVerb.OPTIONS);
    }

    private void registerWriteHook(
            final ThingifierHttpApiRoutings shopRouting,
            final String endpoint,
            final HttpApiRequestHook hook,
            final RoutingVerb... verbs) {
        shopRouting.registerHttpApiRequestHook(HookScope.endpointAndVerbs(endpoint, verbs), hook);
    }

    private void registerShopResponseHook(
            final ThingifierHttpApiRoutings shopRouting, final InternalHttpResponseHook hook) {
        for (String endpoint : SHOP_API_HOOK_ENDPOINTS) {
            shopRouting.registerInternalHttpResponseHook(endpoint, hook);
        }
    }

    private void registerShopResponseHook(
            final ThingifierHttpApiRoutings shopRouting,
            final InternalHttpResponseHook hook,
            final RoutingVerb... verbs) {
        for (String endpoint : SHOP_API_HOOK_ENDPOINTS) {
            shopRouting.registerInternalHttpResponseHook(
                    HookScope.endpointAndVerbs(endpoint, verbs), hook);
        }
    }

    private ThingifierApiDocumentationDefn documentation() {
        final ThingifierApiDocumentationDefn apiDocDefn = new ThingifierApiDocumentationDefn();
        apiDocDefn.addServer("https://apichallenges.com", "cloud hosted version");
        apiDocDefn.addServer("http://localhost:4567", "local execution");
        apiDocDefn.setVersion("1.0.0");
        apiDocDefn.setThingifier(shoppingCart);
        apiDocDefn.setTitle("Buggy API");
        apiDocDefn.setDescription(
                "The Buggy API is a small public Shopping Cart practice API mounted at /shop.");
        apiDocDefn.setPathPrefix("/shop");
        apiDocDefn.setSeoTitle("Buggy API Documentation | API Challenges");
        apiDocDefn.setSwaggerUiTitle("Buggy API - Swagger UI");
        apiDocDefn.setSeoDescription(
                "Read Buggy API endpoint documentation for practising auth, stock, checkout, and business-rule testing.");
        apiDocDefn.setMetaRobots("index,follow");
        apiDocDefn.setOgType("website");
        apiDocDefn.setTwitterCard("summary_large_image");

        shoppingCart
                .apidocsconfig()
                .setHeaderSectionOverride(
                        """
                        <p>The Buggy API is a small public Shopping Cart practice API mounted at
                        <code>/shop</code>.</p>
                        <p>It is deliberately buggy by default. Start the app with
                        <code>-shopbugs=none</code> to run the clean business rules.</p>
                        <p>Use <code>POST /shop/register</code> to create a cart and receive a bearer token.
                        Product catalogue data is read-only; cart item writes happen through
                        <code>/shop/carts/{cartId}/items</code>.</p>
                        """
                                .stripIndent());

        return apiDocDefn;
    }

    private void addCustomRouteDocumentation(final ThingifierApiDocumentationDefn apiDocDefn) {
        apiDocDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST,
                                "/shop/register",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation("create a new open cart and return its bearer token")
                        .addPossibleStatuses(201)
                        .returnPayload(201, "register"));

        apiDocDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST,
                                "/shop/checkout/:cartId",
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "checkout a cart using its bearer token in the Authorization header")
                        .addRequestUrlParam(cartIdField())
                        .secureWithBearerAuth()
                        .addPossibleStatuses(200, 401, 403, 404, 409)
                        .returnPayload(200, "checkout"));
    }

    private Field cartIdField() {
        return Field.is("cartId", INTEGER)
                .withDescription("Cart identifier returned by POST /shop/register.")
                .withMinMaxValues(1, ShoppingCartThingifier.MAX_CARTS)
                .withExample("1");
    }

    private void registerCartCreationRoute() {
        post(
                "/shop/register",
                (request, response) -> {
                    maintenance.pruneBeforeCartCreate();
                    final ThingStore store =
                            shoppingCart
                                    .getERmodel()
                                    .getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
                    final int tick = state.nextTick();
                    final EntityInstance cart =
                            store.entities()
                                    .create(
                                            EntityInstanceDraft.forEntity(
                                                            shoppingCart.getDefinitionNamed("cart"))
                                                    .withField("state", "open")
                                                    .withField("createdTick", String.valueOf(tick))
                                                    .withField("updatedTick", String.valueOf(tick))
                                                    .withField("checkoutTick", "0"));

                    response.status(201);
                    response.type("application/json");
                    response.header("Location", "/shop/carts/" + cart.getPrimaryKeyValue());
                    return ShoppingCartSupport.GSON.toJson(
                            ShoppingCartSupport.body(
                                    "cartId", Integer.parseInt(cart.getPrimaryKeyValue()),
                                    "token", ShoppingCartSupport.stringValue(cart, "token")));
                });

        options(
                "/shop/register",
                (request, response) -> {
                    response.status(204);
                    response.header("Allow", "POST, OPTIONS");
                    return "";
                });

        new SimpleHttpRouteCreator("/shop/register")
                .status(
                        405,
                        true,
                        List.of("get", "head", "put", "delete", "patch", "trace", "query"));
    }

    private void registerCheckoutRouteWithBookDvdCdAndDoubleCheckoutBugs() {
        final ShoppingCartCheckout checkout =
                new ShoppingCartCheckout(shoppingCart, bugMode, state);
        post("/shop/checkout/:cartId", checkout::checkout);

        options(
                "/shop/checkout/:cartId",
                (request, response) -> {
                    response.status(204);
                    response.header("Allow", "POST, OPTIONS");
                    return "";
                });

        new SimpleHttpRouteCreator("/shop/checkout/:cartId")
                .status(
                        405,
                        true,
                        List.of("get", "head", "put", "delete", "patch", "trace", "query"));
    }
}
