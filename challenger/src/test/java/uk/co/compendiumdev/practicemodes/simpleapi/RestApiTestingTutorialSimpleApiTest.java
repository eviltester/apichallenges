package uk.co.compendiumdev.practicemodes.simpleapi;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.practicemodes.simpleapi.testabstractions.Item;
import uk.co.compendiumdev.practicemodes.simpleapi.testabstractions.Items;
import uk.co.compendiumdev.serverstart.Environment;
import uk.co.compendiumdev.serverstart.Port;

public class RestApiTestingTutorialSimpleApiTest {

    private static final Gson GSON = new Gson();
    private static HttpMessageSender http;

    @BeforeAll
    static void createHttp() {
        if (Port.inUse("localhost", 4567)) {
            http = new HttpMessageSender("http://localhost:4567");
        } else {
            http = new HttpMessageSender(Environment.getBaseUri());
        }
    }

    @Test
    public void restApiTestingTutorialRequestsWorkWithTheSimpleApi() {

        ensureRoomForTutorialItem();

        List<Integer> createdItemIds = new ArrayList<>();

        try {
            final HttpResponseDetails listResponse = sendAcceptJson("/simpleapi/items", "GET");
            Assertions.assertEquals(200, listResponse.statusCode);
            Assertions.assertEquals("application/json", listResponse.getHeader("Content-Type"));
            Assertions.assertNotNull(GSON.fromJson(listResponse.body, Items.class).items);

            final String isbn = uniqueRandomIsbnFromTutorialEndpoint();
            final HttpResponseDetails createResponse =
                    sendJson(
                            "/simpleapi/items",
                            "POST",
                            """
                            {
                              "type": "book",
                              "isbn13": "%s",
                              "price": 2.00,
                              "numberinstock": 3
                            }
                            """
                                    .formatted(isbn)
                                    .stripIndent());

            Assertions.assertEquals(201, createResponse.statusCode, createResponse.body);
            Assertions.assertEquals("application/json", createResponse.getHeader("Content-Type"));
            Assertions.assertTrue(
                    createResponse.getHeader("Location").startsWith("/simpleapi/items/"));

            final Item createdItem = GSON.fromJson(createResponse.body, Item.class);
            createdItemIds.add(createdItem.id);
            assertItem(createdItem, "book", isbn, 2.00F, 3);

            final HttpResponseDetails getCreatedResponse =
                    sendAcceptJson("/simpleapi/items/" + createdItem.id, "GET");
            Assertions.assertEquals(200, getCreatedResponse.statusCode);
            Assertions.assertEquals(
                    "application/json", getCreatedResponse.getHeader("Content-Type"));
            assertItem(GSON.fromJson(getCreatedResponse.body, Item.class), "book", isbn, 2.00F, 3);

            final HttpResponseDetails duplicateIsbnResponse =
                    sendJson(
                            "/simpleapi/items",
                            "POST",
                            """
                            {
                              "type": "book",
                              "isbn13": "%s",
                              "price": 2.00,
                              "numberinstock": 3
                            }
                            """
                                    .formatted(isbn)
                                    .stripIndent());

            Assertions.assertEquals(422, duplicateIsbnResponse.statusCode);
            Assertions.assertTrue(
                    duplicateIsbnResponse.body.contains("Field isbn13 Value is not unique"),
                    duplicateIsbnResponse.body);

            final String invalidRequestIsbn = uniqueRandomIsbnFromTutorialEndpoint();
            final HttpResponseDetails invalidTypeResponse =
                    sendJson(
                            "/simpleapi/items",
                            "POST",
                            """
                            {
                              "type": "book",
                              "isbn13": "%s",
                              "price": 2.00,
                              "numberinstock": "3"
                            }
                            """
                                    .formatted(invalidRequestIsbn)
                                    .stripIndent());

            Assertions.assertEquals(422, invalidTypeResponse.statusCode);
            Assertions.assertTrue(
                    invalidTypeResponse.body.contains(
                            "numberinstock should be INTEGER but was STRING"),
                    invalidTypeResponse.body);

            final HttpResponseDetails putResponse =
                    sendJson(
                            "/simpleapi/items/" + createdItem.id,
                            "PUT",
                            """
                            {
                              "type": "dvd",
                              "isbn13": "%s",
                              "price": 4.56,
                              "numberinstock": 8
                            }
                            """
                                    .formatted(isbn)
                                    .stripIndent());

            Assertions.assertEquals(200, putResponse.statusCode, putResponse.body);
            assertItem(GSON.fromJson(putResponse.body, Item.class), "dvd", isbn, 4.56F, 8);

            final HttpResponseDetails patchResponse =
                    sendJson(
                            "/simpleapi/items/" + createdItem.id,
                            "PATCH",
                            """
                            {
                              "price": 9.99
                            }
                            """
                                    .stripIndent());

            Assertions.assertEquals(200, patchResponse.statusCode, patchResponse.body);
            assertItem(GSON.fromJson(patchResponse.body, Item.class), "dvd", isbn, 9.99F, 8);

            final HttpResponseDetails unsupportedDeleteResponse =
                    sendAcceptJson("/simpleapi/items", "DELETE");
            Assertions.assertEquals(405, unsupportedDeleteResponse.statusCode);

            final HttpResponseDetails deleteResponse =
                    sendAcceptJson("/simpleapi/items/" + createdItem.id, "DELETE");
            Assertions.assertEquals(204, deleteResponse.statusCode);
            createdItemIds.remove(createdItem.id);

            final HttpResponseDetails getDeletedResponse =
                    sendAcceptJson("/simpleapi/items/" + createdItem.id, "GET");
            Assertions.assertEquals(404, getDeletedResponse.statusCode);
        } finally {
            for (Integer createdItemId : createdItemIds) {
                sendAcceptJson("/simpleapi/items/" + createdItemId, "DELETE");
            }
        }
    }

    private String uniqueRandomIsbnFromTutorialEndpoint() {

        final Set<String> existingIsbns = existingNormalisedIsbns();

        for (int attempts = 0; attempts < 10; attempts++) {
            final HttpResponseDetails randomIsbnResponse =
                    http.send("/simpleapi/randomisbn", "GET", Map.of(), "");

            Assertions.assertEquals(200, randomIsbnResponse.statusCode);

            final String isbn = randomIsbnResponse.body.trim();
            Assertions.assertTrue(isbn.replace("-", "").matches("\\d{13}"), isbn);

            if (!existingIsbns.contains(normaliseIsbn(isbn))) {
                return isbn;
            }
        }

        Assertions.fail("Could not generate a unique ISBN from /simpleapi/randomisbn");
        return "";
    }

    private Set<String> existingNormalisedIsbns() {

        final Items items = getItems();
        final Set<String> isbns = new HashSet<>();

        for (Item item : items.items) {
            isbns.add(normaliseIsbn(item.isbn13));
        }

        return isbns;
    }

    private void ensureRoomForTutorialItem() {

        final Items items = getItems();
        final int itemsToDelete = Math.max(0, items.items.size() - 98);

        for (int index = 0; index < itemsToDelete; index++) {
            sendAcceptJson("/simpleapi/items/" + items.items.get(index).id, "DELETE");
        }
    }

    private Items getItems() {

        final HttpResponseDetails response = sendAcceptJson("/simpleapi/items", "GET");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertEquals("application/json", response.getHeader("Content-Type"));

        return GSON.fromJson(response.body, Items.class);
    }

    private HttpResponseDetails sendAcceptJson(final String path, final String verb) {
        return http.send(path, verb, Map.of("Accept", "application/json"), "");
    }

    private HttpResponseDetails sendJson(
            final String path, final String verb, final String requestBody) {
        return http.send(
                path,
                verb,
                Map.of("Content-Type", "application/json", "Accept", "application/json"),
                requestBody);
    }

    private void assertItem(
            final Item item,
            final String expectedType,
            final String expectedIsbn,
            final Float expectedPrice,
            final Integer expectedStock) {

        Assertions.assertEquals(expectedType, item.type);
        Assertions.assertEquals(expectedIsbn, item.isbn13);
        Assertions.assertEquals(expectedPrice, item.price);
        Assertions.assertEquals(expectedStock, item.numberinstock);
    }

    private String normaliseIsbn(final String isbn) {
        return isbn.replace("-", "");
    }
}
