package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

final class ShoppingCartCatalogue {

    static final List<ProductSeed> PRODUCTS =
            List.of(
                    new ProductSeed("BOOK_API", "book", 8.99f, 12),
                    new ProductSeed("DVD_BUGS", "dvd", 14.99f, 15),
                    new ProductSeed("CD_STATUS", "cd", 6.50f, 2),
                    new ProductSeed("BLURAY_HTTP", "blu-ray", 19.99f, 8),
                    new ProductSeed("GAME_JSON", "game", 29.99f, 11),
                    new ProductSeed("PUZZLE_CACHE", "game", 12.00f, 7),
                    new ProductSeed("NOTEBOOK_GRID", "stationery", 4.75f, 18),
                    new ProductSeed("PEN_BLUE", "stationery", 1.25f, 40),
                    new ProductSeed("MUG_TESTER", "homeware", 9.95f, 13),
                    new ProductSeed("STICKER_PACK", "stationery", 2.50f, 30),
                    new ProductSeed("USB_CABLE", "electronics", 7.25f, 16),
                    new ProductSeed("POSTER_API", "art", 5.99f, 10),
                    new ProductSeed("BOOK_REST", "book", 10.50f, 0),
                    new ProductSeed("DVD_STATUS", "dvd", 11.25f, 0),
                    new ProductSeed("CD_HEADERS", "cd", 7.75f, 0),
                    new ProductSeed("GAME_TOKENS", "game", 24.00f, 0),
                    new ProductSeed("PUZZLE_PROXY", "game", 13.50f, 0),
                    new ProductSeed("NOTEBOOK_LOGS", "stationery", 5.25f, 0),
                    new ProductSeed("PEN_RED", "stationery", 1.25f, 0),
                    new ProductSeed("MUG_CLIENT", "homeware", 9.95f, 0),
                    new ProductSeed("STICKER_HTTP", "stationery", 2.50f, 0),
                    new ProductSeed("USB_HUB", "electronics", 17.25f, 0),
                    new ProductSeed("POSTER_BUG", "art", 5.99f, 0),
                    new ProductSeed("BOOK_TESTS", "book", 12.99f, 0));

    private ShoppingCartCatalogue() {}

    static List<String> productCodes() {
        return PRODUCTS.stream().map(ProductSeed::productCode).toList();
    }

    static List<String> categories() {
        return PRODUCTS.stream().map(ProductSeed::category).distinct().toList();
    }

    static Optional<ProductSeed> nextUnusedProduct(final List<String> usedCodes) {
        return PRODUCTS.stream()
                .filter(product -> !usedCodes.contains(product.productCode()))
                .findFirst();
    }

    static ProductSeed product(final String productCode) {
        return PRODUCTS.stream()
                .filter(product -> product.productCode().equals(productCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown product " + productCode));
    }

    static boolean isKnownProductCode(final String productCode) {
        return PRODUCTS.stream().anyMatch(product -> product.productCode().equals(productCode));
    }

    static String[] productCodeExamples() {
        return productCodes().toArray(String[]::new);
    }

    static String[] categoryExamples() {
        return categories().toArray(String[]::new);
    }

    static boolean containsProductCode(final String productCode, final String... candidates) {
        return Arrays.asList(candidates).contains(productCode);
    }

    record ProductSeed(String productCode, String category, float unitPrice, int stock) {}
}
