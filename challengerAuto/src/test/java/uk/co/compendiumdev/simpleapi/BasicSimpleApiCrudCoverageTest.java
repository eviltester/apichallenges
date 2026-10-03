package uk.co.compendiumdev.simpleapi;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.serverstart.Environment;
import uk.co.compendiumdev.simpleapi.payloads.Item;
import uk.co.compendiumdev.simpleapi.payloads.Items;

public class BasicSimpleApiCrudCoverageTest {

    private static final int MINIMUM_SHARED_ITEMS = 5;
    private static final int SIMPLE_API_ITEM_LIMIT = 100;
    private static final int RESERVED_CREATION_SLOTS = 5;
    private static final int MAXIMUM_SHARED_ITEMS = SIMPLE_API_ITEM_LIMIT - RESERVED_CREATION_SLOTS;

    private final Set<Integer> createdItemIds = new LinkedHashSet<>();

    @BeforeAll
    static void logRestAssuredCalls() {
        Assumptions.assumeTrue(Environment.shouldRunFullSuite(), Environment.fullSuiteSkipReason());
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @BeforeEach
    void prepareSharedInventory() {
        // The live Simple API is shared, so leave room for this suite and concurrent users.
        for (int attempt = 0; attempt < 3; attempt++) {
            Items items = getAllItems();
            int itemCount = items.items.size();

            if (itemCount >= MINIMUM_SHARED_ITEMS && itemCount <= MAXIMUM_SHARED_ITEMS) {
                return;
            }

            if (itemCount > MAXIMUM_SHARED_ITEMS) {
                removeExcessItems(items, itemCount - MAXIMUM_SHARED_ITEMS);
            } else {
                addMissingItems(MINIMUM_SHARED_ITEMS - itemCount);
            }
        }

        int finalItemCount = getAllItems().items.size();
        Assertions.assertTrue(
                finalItemCount >= MINIMUM_SHARED_ITEMS && finalItemCount <= MAXIMUM_SHARED_ITEMS,
                () ->
                        "Could not prepare the shared Simple API inventory: expected between "
                                + MINIMUM_SHARED_ITEMS
                                + " and "
                                + MAXIMUM_SHARED_ITEMS
                                + " items but found "
                                + finalItemCount);
    }

    @AfterEach
    void removeItemsCreatedByTest() {
        List<Integer> itemsNotRemoved = new ArrayList<>();

        for (Integer itemId : new ArrayList<>(createdItemIds)) {
            int statusCode = deleteItem(itemId);
            if (statusCode != 204 && statusCode != 404) {
                itemsNotRemoved.add(itemId);
            }
        }

        createdItemIds.clear();
        Assertions.assertTrue(
                itemsNotRemoved.isEmpty(),
                () -> "Could not remove Simple API test items " + itemsNotRemoved);
    }

    @Test
    void canGetAllItems() {
        Item createdItem = createTrackedItem(newTestItem());

        Items response = getAllItems();

        Assertions.assertTrue(
                response.items.stream().anyMatch(item -> item.id.equals(createdItem.id)));
    }

    @Test
    void canCreateAnItemWithPost() {
        Item anItem = newTestItem();
        Item response = createTrackedItem(anItem);

        Assertions.assertEquals(anItem.isbn13, response.isbn13);
        Assertions.assertEquals(anItem.type, response.type);
        Assertions.assertEquals(anItem.price, response.price);
    }

    @Test
    void canGetACreatedItem() {
        Item anItem = newTestItem();
        Item response = createTrackedItem(anItem);

        Item getResponse =
                RestAssured.given()
                        .accept("application/json")
                        .get(apiPath("/items/" + response.id))
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .extract()
                        .response()
                        .body()
                        .as(Item.class);

        Assertions.assertEquals(response.id, getResponse.id);
        Assertions.assertEquals(anItem.isbn13, getResponse.isbn13);
        Assertions.assertEquals(anItem.type, getResponse.type);
        Assertions.assertEquals(anItem.price, getResponse.price);
    }

    @Test
    void canDeleteAnItem() {
        Item createdItem = createTrackedItem(newTestItem());

        RestAssured.given()
                .accept("application/json")
                .delete(apiPath("/items/" + createdItem.id))
                .then()
                .statusCode(204);
        createdItemIds.remove(createdItem.id);

        RestAssured.given()
                .accept("application/json")
                .get(apiPath("/items/" + createdItem.id))
                .then()
                .statusCode(404);
    }

    private Items getAllItems() {
        return RestAssured.given()
                .accept("application/json")
                .get(apiPath("/items"))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response()
                .body()
                .as(Items.class);
    }

    private void removeExcessItems(Items items, int itemsToRemove) {
        for (int itemIndex = 0; itemIndex < itemsToRemove; itemIndex++) {
            int statusCode = deleteItem(items.items.get(itemIndex).id);
            Assertions.assertTrue(
                    statusCode == 204 || statusCode == 404,
                    () -> "Unexpected status " + statusCode + " while making creation space");
        }
    }

    private void addMissingItems(int itemsToAdd) {
        for (int itemIndex = 0; itemIndex < itemsToAdd; itemIndex++) {
            createTrackedItem(newTestItem());
        }
    }

    private Item createTrackedItem(Item item) {
        Item response =
                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(item)
                        .accept("application/json")
                        .post(apiPath("/items"))
                        .then()
                        .statusCode(201)
                        .contentType(ContentType.JSON)
                        .extract()
                        .response()
                        .body()
                        .as(Item.class);

        Assertions.assertTrue(response.id > 0);
        createdItemIds.add(response.id);
        return response;
    }

    private int deleteItem(Integer itemId) {
        return RestAssured.given()
                .accept("application/json")
                .delete(apiPath("/items/" + itemId))
                .statusCode();
    }

    private Item newTestItem() {
        Item item = new Item();
        item.isbn13 = TimestampToIsbn13.currentIsbn();
        item.numberinstock = 23;
        item.type = "dvd";
        item.price = new BigDecimal("51.29");
        return item;
    }

    private String apiPath(String postfix) {
        return Environment.getBaseUri() + "/simpleapi" + postfix;
    }
}
