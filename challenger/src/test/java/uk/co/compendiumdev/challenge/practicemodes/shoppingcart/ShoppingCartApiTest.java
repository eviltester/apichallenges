package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartApiClient.CartItemResponse;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartApiClient.Carts;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartApiClient.CheckoutResponse;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartApiClient.Product;
import uk.co.compendiumdev.challenge.practicemodes.shoppingcart.ShoppingCartApiClient.RegisterResponse;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.serverstart.Environment;

class ShoppingCartApiTest {

    private HttpMessageSender http;
    private ShoppingCartApiClient api;

    @AfterEach
    void stopApp() {
        Environment.stop();
    }

    @Test
    void registrationAuthAndTokenStrippingWork() {
        startApp("-shopbugs=none");

        final RegisterResponse cart = api.registerCart();
        Assertions.assertTrue(cart.cartId > 0);
        Assertions.assertTrue(
                cart.token.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));

        final Product product = api.productByCode("MUG_TESTER");
        Assertions.assertEquals(401, api.addItem(cart.cartId, "", product.id, 1).statusCode);
        Assertions.assertEquals(
                403,
                api.addItem(cart.cartId, "00000000-0000-4000-8000-000000000000", product.id, 1)
                        .statusCode);
        Assertions.assertEquals(
                201, api.addItem(cart.cartId, cart.token, product.id, 1).statusCode);

        final HttpResponseDetails getCart = api.getCart(cart.cartId);
        Assertions.assertEquals(200, getCart.statusCode);
        Assertions.assertFalse(getCart.body.contains("token"));
        Assertions.assertFalse(getCart.body.contains(cart.token));

        final HttpResponseDetails queryCarts = api.queryCarts();
        Assertions.assertEquals(200, queryCarts.statusCode);
        Assertions.assertFalse(queryCarts.body.contains("token"));
        Assertions.assertFalse(queryCarts.body.contains(cart.token));
    }

    @Test
    void cleanModeEnforcesCartStockCheckoutAndTokenRules() {
        startApp("-shopbugs=none");

        final RegisterResponse cart = api.registerCart();
        final Product cd = api.productByCode("CD_STATUS");

        Assertions.assertEquals(422, api.addItem(cart.cartId, cart.token, cd.id, -1).statusCode);
        Assertions.assertEquals(422, api.addItem(cart.cartId, cart.token, cd.id, 0).statusCode);
        Assertions.assertEquals(
                409, api.addItem(cart.cartId, cart.token, cd.id, cd.stock + 1).statusCode);

        final Product dvd = api.productByCode("DVD_BUGS");
        final CartItemResponse item = api.addItemOk(cart.cartId, cart.token, dvd.id, 3);

        final RegisterResponse otherCart = api.registerCart();
        Assertions.assertEquals(
                403, api.updateItem(cart.cartId, otherCart.token, item.id, 4).statusCode);
        Assertions.assertEquals(
                403, api.deleteItem(cart.cartId, otherCart.token, item.id).statusCode);

        final CartItemResponse deleteItem = api.addItemOk(cart.cartId, cart.token, dvd.id, 1);
        Assertions.assertEquals(
                200, api.deleteItem(cart.cartId, cart.token, deleteItem.id).statusCode);

        final CheckoutResponse checkout = api.checkoutOk(cart.cartId, cart.token);
        Assertions.assertEquals("closed", checkout.state);
        Assertions.assertEquals(3, checkout.stockChanges.get(0).reducedBy);
        final int stockAfterCheckout = api.product(dvd.id).stock;
        Assertions.assertEquals(dvd.stock - 3, stockAfterCheckout);

        Assertions.assertEquals(409, api.checkout(cart.cartId, cart.token).statusCode);
        Assertions.assertEquals(stockAfterCheckout, api.product(dvd.id).stock);
        Assertions.assertEquals(409, api.addItem(cart.cartId, cart.token, cd.id, 1).statusCode);

        final RegisterResponse cartToDelete = api.registerCart();
        Assertions.assertEquals(
                403, api.deleteCart(cartToDelete.cartId, otherCart.token).statusCode);
        Assertions.assertEquals(
                200, api.deleteCart(cartToDelete.cartId, cartToDelete.token).statusCode);
        Assertions.assertEquals(404, api.getCart(cartToDelete.cartId).statusCode);
    }

    @Test
    void cleanModeRejectsBadQuantityUpdates() {
        startApp("-shopbugs=none");

        final RegisterResponse cart = api.registerCart();
        final Product mug = api.productByCode("MUG_TESTER");
        final CartItemResponse item = api.addItemOk(cart.cartId, cart.token, mug.id, 1);

        Assertions.assertEquals(
                422, api.updateItem(cart.cartId, cart.token, item.id, -1).statusCode);
        Assertions.assertEquals(
                422, api.updateItem(cart.cartId, cart.token, item.id, 0).statusCode);
        Assertions.assertEquals(
                409, api.updateItem(cart.cartId, cart.token, item.id, mug.stock + 1).statusCode);
    }

    @Test
    void cleanModeCheckoutUsesCurrentStockNotStockAtAdd() {
        startApp("-shopbugs=none");

        final Product book = api.productByCode("BOOK_API");
        final RegisterResponse firstCart = api.registerCart();
        final RegisterResponse secondCart = api.registerCart();

        Assertions.assertEquals(
                201, api.addItem(firstCart.cartId, firstCart.token, book.id, 10).statusCode);
        Assertions.assertEquals(
                201, api.addItem(secondCart.cartId, secondCart.token, book.id, 8).statusCode);
        Assertions.assertEquals(200, api.checkout(secondCart.cartId, secondCart.token).statusCode);

        final Product afterSecondCheckout = api.product(book.id);
        Assertions.assertEquals(book.stock - 8, afterSecondCheckout.stock);
        Assertions.assertEquals(409, api.checkout(firstCart.cartId, firstCart.token).statusCode);
    }

    @Test
    void cleanModeCheckoutTotalIncludesEveryLine() {
        startApp("-shopbugs=none");

        final RegisterResponse cart = api.registerCart();
        final Product mug = api.productByCode("MUG_TESTER");
        final Product cable = api.productByCode("USB_CABLE");

        api.addItemOk(cart.cartId, cart.token, mug.id, 1);
        api.addItemOk(cart.cartId, cart.token, cable.id, 1);

        final CheckoutResponse checkout = api.checkoutOk(cart.cartId, cart.token);
        Assertions.assertEquals(mug.unitPrice + cable.unitPrice, checkout.total, 0.001f);
    }

    @Test
    void cleanModeIgnoresClientSuppliedCapturedCartItemValues() {
        startApp("-shopbugs=none");

        final RegisterResponse cart = api.registerCart();
        final Product mug = api.productByCode("MUG_TESTER");
        final HttpResponseDetails response =
                api.addItemWithCapturedValues(cart.cartId, cart.token, mug.id, 1, 0.01f, 999);

        Assertions.assertEquals(201, response.statusCode);
        final CartItemResponse item =
                ShoppingCartSupport.GSON.fromJson(response.body, CartItemResponse.class);
        Assertions.assertEquals(mug.unitPrice, item.unitPriceAtAdd, 0.001f);
        Assertions.assertEquals(mug.stock, item.stockAtAdd);
    }

    @Test
    void defaultBugModeAllowsCartAndCartItemReadsWithoutBearerToken() {
        startApp();

        final RegisterResponse cart = api.registerCart();
        final Product mug = api.productByCode("MUG_TESTER");
        Assertions.assertEquals(201, api.addItem(cart.cartId, cart.token, mug.id, 1).statusCode);

        Assertions.assertEquals(200, api.getCart(cart.cartId).statusCode);
        Assertions.assertEquals(200, api.getCartItems(cart.cartId).statusCode);
    }

    @Test
    void defaultBugModeAllowsCreateAndUpdateQuantitiesGreaterThanStock() {
        startApp();

        final Product cd = api.productByCode("CD_STATUS");
        final RegisterResponse overstockCreateCart = api.registerCart();
        Assertions.assertEquals(
                201,
                api.addItem(
                                overstockCreateCart.cartId,
                                overstockCreateCart.token,
                                cd.id,
                                cd.stock + 5)
                        .statusCode);

        final Product mug = api.productByCode("MUG_TESTER");
        final RegisterResponse overstockUpdateCart = api.registerCart();
        final CartItemResponse item =
                api.addItemOk(overstockUpdateCart.cartId, overstockUpdateCart.token, mug.id, 1);
        Assertions.assertEquals(
                200,
                api.updateItem(
                                overstockUpdateCart.cartId,
                                overstockUpdateCart.token,
                                item.id,
                                mug.stock + 1)
                        .statusCode);
    }

    @Test
    void defaultBugModeAllowsCreateAndUpdateNegativeQuantities() {
        startApp();

        final Product mug = api.productByCode("MUG_TESTER");
        final RegisterResponse negativeCreateCart = api.registerCart();
        Assertions.assertEquals(
                201, api.addItem(negativeCreateCart.cartId, negativeCreateCart.token, mug.id, -3)
                        .statusCode);
        final CheckoutResponse negativeCheckout =
                api.checkoutOk(negativeCreateCart.cartId, negativeCreateCart.token);
        Assertions.assertTrue(negativeCheckout.total < 0);

        final RegisterResponse negativeUpdateCart = api.registerCart();
        final CartItemResponse item =
                api.addItemOk(negativeUpdateCart.cartId, negativeUpdateCart.token, mug.id, 1);
        Assertions.assertEquals(
                200,
                api.updateItem(negativeUpdateCart.cartId, negativeUpdateCart.token, item.id, -3)
                        .statusCode);
    }

    @Test
    void defaultBugModeAllowsCreateAndUpdateZeroQuantities() {
        startApp();

        final Product mug = api.productByCode("MUG_TESTER");
        final RegisterResponse zeroCreateCart = api.registerCart();
        Assertions.assertEquals(
                201,
                api.addItem(zeroCreateCart.cartId, zeroCreateCart.token, mug.id, 0).statusCode);

        final RegisterResponse zeroUpdateCart = api.registerCart();
        final CartItemResponse item =
                api.addItemOk(zeroUpdateCart.cartId, zeroUpdateCart.token, mug.id, 1);
        Assertions.assertEquals(
                200,
                api.updateItem(zeroUpdateCart.cartId, zeroUpdateCart.token, item.id, 0)
                        .statusCode);
    }

    @Test
    void defaultBugModeAcceptsHiddenCapturedCartItemValues() {
        startApp();

        final RegisterResponse cart = api.registerCart();
        final Product mug = api.productByCode("MUG_TESTER");
        final HttpResponseDetails response =
                api.addItemWithCapturedValues(cart.cartId, cart.token, mug.id, 1, 0.01f, 999);

        Assertions.assertEquals(201, response.statusCode);
        final CartItemResponse item =
                ShoppingCartSupport.GSON.fromJson(response.body, CartItemResponse.class);
        Assertions.assertEquals(0.01f, item.unitPriceAtAdd, 0.001f);
        Assertions.assertEquals(999, item.stockAtAdd);
    }

    @Test
    void defaultBugModeUsesStockAtAddForBookApiCheckout() {
        startApp();

        final Product book = api.productByCode("BOOK_API");
        final RegisterResponse staleCart = api.registerCart();
        final RegisterResponse stockReducerCart = api.registerCart();

        Assertions.assertEquals(201, api.addItem(staleCart.cartId, staleCart.token, book.id, 10)
                .statusCode);
        Assertions.assertEquals(
                201, api.addItem(stockReducerCart.cartId, stockReducerCart.token, book.id, 8)
                        .statusCode);
        Assertions.assertEquals(
                200, api.checkout(stockReducerCart.cartId, stockReducerCart.token).statusCode);

        final int stockBeforeStaleCheckout = api.product(book.id).stock;
        Assertions.assertEquals(200, api.checkout(staleCart.cartId, staleCart.token).statusCode);
        Assertions.assertEquals(stockBeforeStaleCheckout - 10, api.product(book.id).stock);
    }

    @Test
    void defaultBugModeReducesDvdStockByOnePerLine() {
        startApp();

        final Product dvd = api.productByCode("DVD_BUGS");
        final RegisterResponse cart = api.registerCart();
        Assertions.assertEquals(201, api.addItem(cart.cartId, cart.token, dvd.id, 5).statusCode);

        final CheckoutResponse checkout = api.checkoutOk(cart.cartId, cart.token);
        Assertions.assertEquals(1, checkout.stockChanges.get(0).reducedBy);
        Assertions.assertEquals(dvd.stock - 1, api.product(dvd.id).stock);
    }

    @Test
    void defaultBugModeAllowsCdStockToBecomeNegative() {
        startApp();

        final Product cd = api.productByCode("CD_STATUS");
        final RegisterResponse cart = api.registerCart();
        Assertions.assertEquals(201, api.addItem(cart.cartId, cart.token, cd.id, cd.stock + 5)
                .statusCode);

        Assertions.assertEquals(200, api.checkout(cart.cartId, cart.token).statusCode);
        Assertions.assertTrue(api.product(cd.id).stock < 0);
    }

    @Test
    void defaultBugModeAllowsClosedCartModification() {
        startApp();

        final Product mug = api.productByCode("MUG_TESTER");
        final RegisterResponse cart = api.registerCart();
        Assertions.assertEquals(201, api.addItem(cart.cartId, cart.token, mug.id, 1).statusCode);
        Assertions.assertEquals(200, api.checkout(cart.cartId, cart.token).statusCode);

        Assertions.assertEquals(201, api.addItem(cart.cartId, cart.token, mug.id, 1).statusCode);
    }

    @Test
    void defaultBugModeAllowsDoubleCheckoutToReduceStockAgain() {
        startApp();

        final Product mug = api.productByCode("MUG_TESTER");
        final RegisterResponse cart = api.registerCart();
        Assertions.assertEquals(201, api.addItem(cart.cartId, cart.token, mug.id, 1).statusCode);

        Assertions.assertEquals(200, api.checkout(cart.cartId, cart.token).statusCode);
        final int stockAfterFirstCheckout = api.product(mug.id).stock;
        Assertions.assertEquals(200, api.checkout(cart.cartId, cart.token).statusCode);
        Assertions.assertEquals(stockAfterFirstCheckout - 1, api.product(mug.id).stock);
    }

    @Test
    void defaultBugModeCheckoutTotalIgnoresFinalLine() {
        startApp();

        final RegisterResponse cart = api.registerCart();
        final Product mug = api.productByCode("MUG_TESTER");
        final Product cable = api.productByCode("USB_CABLE");
        api.addItemOk(cart.cartId, cart.token, mug.id, 1);
        api.addItemOk(cart.cartId, cart.token, cable.id, 1);

        final CheckoutResponse checkout = api.checkoutOk(cart.cartId, cart.token);
        Assertions.assertEquals(mug.unitPrice, checkout.total, 0.001f);
    }

    @Test
    void defaultBugModeAllowsAnotherCartsBearerTokenToUpdateExistingItem() {
        startApp();

        final Product mug = api.productByCode("MUG_TESTER");
        final RegisterResponse cart = api.registerCart();
        final RegisterResponse otherCart = api.registerCart();
        final CartItemResponse item = api.addItemOk(cart.cartId, cart.token, mug.id, 1);

        Assertions.assertEquals(
                200, api.updateItem(cart.cartId, otherCart.token, item.id, 2).statusCode);
    }

    @Test
    void maintenanceKeepsStockedProductsAndPrunesOldCarts() {
        startApp();

        long stocked =
                api.products().products.stream().filter(product -> product.stock > 0).count();
        Assertions.assertTrue(stocked >= ShoppingCartMaintenance.MIN_STOCKED_PRODUCTS);

        final List<RegisterResponse> carts = new ArrayList<>();
        for (int index = 0; index < 105; index++) {
            carts.add(api.registerCart());
        }

        final HttpResponseDetails cartsResponse =
                http.send("/shop/carts", "GET", java.util.Map.of("Accept", "application/json"), "");
        Assertions.assertEquals(200, cartsResponse.statusCode);
        final Carts visibleCarts =
                ShoppingCartSupport.GSON.fromJson(cartsResponse.body, Carts.class);
        Assertions.assertTrue(visibleCarts.carts.size() <= ShoppingCartThingifier.MAX_CARTS);
        Assertions.assertFalse(cartsResponse.body.contains(carts.get(0).token));
        Assertions.assertFalse(cartsResponse.body.contains("token"));
    }

    @Test
    void pagesDocsAndHiddenBugNotesAreRoutedCorrectly() {
        startApp();

        assertPage("/practice-modes/shoppingcart", "Buggy API");
        assertPage("/practice-modes/shoppingcart-openapi", "Buggy API OpenAPI Files");
        assertPage("/practice-modes/shoppingcart-bugs", "Buggy API Deliberate Bugs");
        assertPage("/shop/docs", "Buggy API");
        assertPage("/shop/docs/swagger-ui", "Buggy API - Swagger UI");
        final HttpResponseDetails openApi = http.send("/shop/docs/openapi.json", "GET");
        Assertions.assertEquals(200, openApi.statusCode);
        Assertions.assertTrue(openApi.body.contains("/shop/carts/{id}/items"));
        Assertions.assertFalse(openApi.body.contains("/shop/cartitems"));
        Assertions.assertFalse(openApi.body.contains("/shop/products/{id}/cartitems"));
        Assertions.assertTrue(openApi.body.contains("\"bearerAuth\""));
        Assertions.assertTrue(openApi.body.contains("\"scheme\" : \"bearer\""));
        Assertions.assertTrue(openApi.body.contains("\"securitySchemes\""));
        Assertions.assertTrue(openApi.body.contains("requestBody"));
        Assertions.assertTrue(openApi.body.contains("\"name\" : \"id\""));
        Assertions.assertTrue(openApi.body.contains("\"name\" : \"relatedId\""));
        Assertions.assertTrue(openApi.body.contains("\"productId\""));
        assertAddedCartItemViewSchemas(openApi.body);
        Assertions.assertEquals(200, http.send("/shop/gui/entities", "GET").statusCode);

        final HttpResponseDetails home = http.send("/", "GET");
        Assertions.assertTrue(home.body.contains("href=\"/practice-modes/shoppingcart\""));
        Assertions.assertTrue(home.body.contains("href=\"/shop/docs/swagger-ui\""));
        Assertions.assertFalse(home.body.contains("shoppingcart-bugs"));

        final HttpResponseDetails sitemap = http.send("/sitemap.xml", "GET");
        Assertions.assertTrue(sitemap.body.contains("/practice-modes/shoppingcart</loc>"));
        Assertions.assertFalse(sitemap.body.contains("shoppingcart-bugs"));
    }

    private void assertAddedCartItemViewSchemas(final String openApiBody) {
        final JsonObject schemas =
                JsonParser.parseString(openApiBody)
                        .getAsJsonObject()
                        .getAsJsonObject("components")
                        .getAsJsonObject("schemas");

        final JsonObject requestProperties =
                schemas.getAsJsonObject("create_AddedCartItem").getAsJsonObject("properties");
        Assertions.assertTrue(requestProperties.has("productId"));
        Assertions.assertTrue(requestProperties.has("quantity"));
        Assertions.assertFalse(requestProperties.has("unitPriceAtAdd"));
        Assertions.assertFalse(requestProperties.has("stockAtAdd"));

        final JsonObject responseProperties =
                schemas.getAsJsonObject("AddedCartItem").getAsJsonObject("properties");
        Assertions.assertTrue(responseProperties.has("id"));
        Assertions.assertTrue(responseProperties.has("productId"));
        Assertions.assertTrue(responseProperties.has("quantity"));
        Assertions.assertTrue(responseProperties.has("unitPriceAtAdd"));
        Assertions.assertTrue(responseProperties.has("stockAtAdd"));
    }

    @Test
    void directWritesToInternalThingifierCollectionsAreBlocked() {
        startApp();

        Assertions.assertEquals(
                404,
                http.send(
                                "/shop/products",
                                "POST",
                                java.util.Map.of(
                                        "Content-Type",
                                        "application/json",
                                        "Accept",
                                        "application/json"),
                                "{}")
                        .statusCode);
        Assertions.assertEquals(
                404,
                http.send(
                                "/shop/carts",
                                "POST",
                                java.util.Map.of(
                                        "Content-Type",
                                        "application/json",
                                        "Accept",
                                        "application/json"),
                                "{}")
                        .statusCode);
        Assertions.assertEquals(
                404,
                http.send(
                                "/shop/cartitems",
                                "POST",
                                java.util.Map.of(
                                        "Content-Type",
                                        "application/json",
                                        "Accept",
                                        "application/json"),
                                "{}")
                        .statusCode);
        Assertions.assertEquals(
                404,
                http.send(
                                "/shop/cartitems/1",
                                "PUT",
                                java.util.Map.of(
                                        "Content-Type",
                                        "application/json",
                                        "Accept",
                                        "application/json"),
                                "{\"quantity\":2}")
                        .statusCode);
        final Product product = api.productByCode("MUG_TESTER");
        Assertions.assertEquals(
                404,
                http.send(
                                "/shop/products/" + product.id + "/cartitems",
                                "POST",
                                java.util.Map.of(
                                        "Content-Type",
                                        "application/json",
                                        "Accept",
                                        "application/json"),
                                "{\"id\":1}")
                        .statusCode);
    }

    private void assertPage(final String path, final String expectedText) {
        final HttpResponseDetails response = http.send(path, "GET");
        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(response.body.contains(expectedText));
    }

    private void startApp(final String... extraArgs) {
        Environment.stop();
        final List<String> args = new ArrayList<>();
        args.add("-multiplayer");
        args.add("-nostorage");
        args.addAll(List.of(extraArgs));
        ChallengeMain.main(args.toArray(String[]::new));
        Environment.waitTillRunningStatus(true);
        http = new HttpMessageSender("http://localhost:4567");
        api = new ShoppingCartApiClient(http);
    }
}
