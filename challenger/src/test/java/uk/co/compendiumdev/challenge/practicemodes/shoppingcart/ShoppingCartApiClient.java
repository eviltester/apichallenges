package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;

final class ShoppingCartApiClient {

    private final HttpMessageSender http;
    private final Gson gson = new Gson();

    ShoppingCartApiClient(final HttpMessageSender http) {
        this.http = http;
    }

    HttpResponseDetails register() {
        return http.send("/shop/register", "POST", jsonHeaders(), "");
    }

    RegisterResponse registerCart() {
        return gson.fromJson(register().body, RegisterResponse.class);
    }

    Products products() {
        return gson.fromJson(
                http.send("/shop/products", "GET", acceptJson(), "").body, Products.class);
    }

    Product productByCode(final String productCode) {
        return products().products.stream()
                .filter(product -> productCode.equals(product.productCode))
                .findFirst()
                .orElseThrow();
    }

    Product product(final int productId) {
        return gson.fromJson(
                http.send("/shop/products/" + productId, "GET", acceptJson(), "").body,
                Product.class);
    }

    HttpResponseDetails getCart(final int cartId) {
        return http.send("/shop/carts/" + cartId, "GET", acceptJson(), "");
    }

    HttpResponseDetails getCartItems(final int cartId) {
        return http.send("/shop/carts/" + cartId + "/items", "GET", acceptJson(), "");
    }

    HttpResponseDetails queryCarts() {
        final Map<String, String> headers = jsonHeaders();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return http.send("/shop/carts", "QUERY", headers, "state=open");
    }

    HttpResponseDetails addItem(
            final int cartId, final String token, final int productId, final int quantity) {
        return http.send(
                "/shop/carts/" + cartId + "/items",
                "POST",
                bearerJson(token),
                gson.toJson(Map.of("productId", productId, "quantity", quantity)));
    }

    HttpResponseDetails addItemRaw(final int cartId, final String token, final String body) {
        return http.send("/shop/carts/" + cartId + "/items", "POST", bearerJson(token), body);
    }

    CartItemResponse addItemOk(
            final int cartId, final String token, final int productId, final int quantity) {
        return gson.fromJson(
                addItem(cartId, token, productId, quantity).body, CartItemResponse.class);
    }

    HttpResponseDetails addItemWithCapturedValues(
            final int cartId,
            final String token,
            final int productId,
            final int quantity,
            final float unitPriceAtAdd,
            final int stockAtAdd) {
        return http.send(
                "/shop/carts/" + cartId + "/items",
                "POST",
                bearerJson(token),
                gson.toJson(
                        Map.of(
                                "productId",
                                productId,
                                "quantity",
                                quantity,
                                "unitPriceAtAdd",
                                unitPriceAtAdd,
                                "stockAtAdd",
                                stockAtAdd)));
    }

    HttpResponseDetails updateItem(
            final int cartId, final String token, final int itemId, final int quantity) {
        return http.send(
                "/shop/carts/" + cartId + "/items",
                "POST",
                bearerJson(token),
                gson.toJson(Map.of("id", itemId, "quantity", quantity)));
    }

    HttpResponseDetails deleteItem(final int cartId, final String token, final int itemId) {
        return http.send(
                "/shop/carts/" + cartId + "/items/" + itemId, "DELETE", bearerJson(token), "");
    }

    HttpResponseDetails deleteCart(final int cartId, final String token) {
        return http.send("/shop/carts/" + cartId, "DELETE", bearerJson(token), "");
    }

    HttpResponseDetails checkout(final int cartId, final String token) {
        return http.send("/shop/checkout/" + cartId, "POST", bearerJson(token), "");
    }

    CheckoutResponse checkoutOk(final int cartId, final String token) {
        return gson.fromJson(checkout(cartId, token).body, CheckoutResponse.class);
    }

    private Map<String, String> bearerJson(final String token) {
        final Map<String, String> headers = jsonHeaders();
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }

    private Map<String, String> jsonHeaders() {
        final Map<String, String> headers = acceptJson();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private Map<String, String> acceptJson() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        return headers;
    }

    static final class RegisterResponse {
        int cartId;
        String token;
    }

    static final class Products {
        java.util.List<Product> products;
    }

    static final class Product {
        int id;
        String productCode;
        String category;
        float unitPrice;
        int stock;
    }

    static final class Carts {
        java.util.List<Cart> carts;
    }

    static final class Cart {
        int id;
        String state;
        int createdTick;
        int updatedTick;
        int checkoutTick;
    }

    static final class CartItemResponse {
        int id;
        int cartId;
        int productId;
        int quantity;
        float unitPriceAtAdd;
        int stockAtAdd;
    }

    static final class CheckoutResponse {
        int cartId;
        String state;
        int itemCount;
        float total;
        java.util.List<StockChange> stockChanges;
    }

    static final class StockChange {
        int productId;
        String productCode;
        int before;
        int reducedBy;
        int after;
    }
}
