package uk.co.compendiumdev.challenge.challengehooks;

import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.DELETE;
import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.GET;
import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.PATCH;
import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.POST;
import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.PUT;
import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.QUERY;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProvider;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStoreProvider;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStoreProvider;

public class ChallengerApiResponseHookTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void textCalendarTodoChallengeCompletesForExistingTodoInstance(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance todo =
                    fixture.addTodo(
                            "summary, with; characters \\ and\nline",
                            "false",
                            "description, with; newline\r\nsecond \\ end");

            HttpApiResponse calendarResponse =
                    fixture.hook.run(
                            fixture.request("todos/" + todo.getPrimaryKeyValue(), GET)
                                    .addHeader("Accept", "text/calendar"),
                            fixture.apiResponse(406),
                            fixture.thingifier.apiConfig());

            Assertions.assertNotNull(calendarResponse);
            Assertions.assertEquals(200, calendarResponse.getStatusCode());
            Assertions.assertEquals(
                    "text/calendar", calendarResponse.getHeaders().get("Content-Type"));

            String body = calendarResponse.getBody();
            Assertions.assertTrue(body.contains("BEGIN:VCALENDAR"));
            Assertions.assertTrue(body.contains("BEGIN:VTODO"));
            Assertions.assertTrue(
                    body.contains("UID:todo-" + todo.getPrimaryKeyValue() + "@apichallenges"));
            Assertions.assertTrue(
                    body.contains("SUMMARY:summary\\, with\\; characters \\\\ and\\nline"));
            Assertions.assertTrue(
                    body.contains("DESCRIPTION:description\\, with\\; newline\\nsecond \\\\ end"));
            Assertions.assertTrue(body.contains("STATUS:NEEDS-ACTION"));
            Assertions.assertTrue(body.contains("END:VTODO"));
            Assertions.assertTrue(body.contains("END:VCALENDAR"));
            Assertions.assertTrue(fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODO));
            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODO_ACCEPT_TEXT_CALENDAR));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void textCalendarTodoResponseUsesCompletedStatusForDoneTodo(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance todo = fixture.addTodo("done todo", "true", "");

            HttpApiResponse calendarResponse =
                    fixture.hook.run(
                            fixture.request("todos/" + todo.getPrimaryKeyValue(), GET)
                                    .addHeader("Accept", "text/calendar"),
                            fixture.apiResponse(406),
                            fixture.thingifier.apiConfig());

            Assertions.assertNotNull(calendarResponse);
            Assertions.assertTrue(calendarResponse.getBody().contains("STATUS:COMPLETED"));
            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODO_ACCEPT_TEXT_CALENDAR));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void textCalendarTodoResponseHonoursAcceptQualityPreference(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance todo = fixture.addTodo("preferred calendar todo", "false", "");

            HttpApiResponse calendarResponse =
                    fixture.hook.run(
                            fixture.request("todos/" + todo.getPrimaryKeyValue(), GET)
                                    .addHeader(
                                            "Accept",
                                            "application/json;q=0.1, text/calendar;q=0.9"),
                            fixture.apiResponse(200),
                            fixture.thingifier.apiConfig());

            Assertions.assertNotNull(calendarResponse);
            Assertions.assertEquals(200, calendarResponse.getStatusCode());
            Assertions.assertEquals(
                    "text/calendar", calendarResponse.getHeaders().get("Content-Type"));
            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODO_ACCEPT_TEXT_CALENDAR));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void textCalendarTodoResponseIgnoresZeroQualityAccept(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance todo = fixture.addTodo("zero quality calendar todo", "false", "");

            HttpApiResponse calendarResponse =
                    fixture.hook.run(
                            fixture.request("todos/" + todo.getPrimaryKeyValue(), GET)
                                    .addHeader("Accept", "text/calendar;q=0, application/json"),
                            fixture.apiResponse(200),
                            fixture.thingifier.apiConfig());

            Assertions.assertNull(calendarResponse);
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODO_ACCEPT_TEXT_CALENDAR));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void textCalendarCollectionRequestRemainsUnsupportedAcceptChallenge(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            HttpApiResponse calendarResponse =
                    fixture.hook.run(
                            fixture.request("todos", GET).addHeader("Accept", "text/calendar"),
                            fixture.apiResponse(406),
                            fixture.thingifier.apiConfig());

            Assertions.assertNull(calendarResponse);
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODO_ACCEPT_TEXT_CALENDAR));
            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void advancedAcceptChallengesCompleteForExpectedNegotiationRules(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        for (AcceptCase acceptCase :
                List.of(
                        new AcceptCase(
                                CHALLENGE.GET_ACCEPT_XML_Q_PREFERRED,
                                "application/json;q=0.5, application/xml;q=1",
                                200,
                                "application/xml"),
                        new AcceptCase(
                                CHALLENGE.GET_ACCEPT_JSON_Q_PREFERRED,
                                "application/xml;q=0.5, application/json;q=1",
                                200,
                                "application/json"),
                        new AcceptCase(
                                CHALLENGE.GET_ACCEPT_Q_REJECTS_ALL_406,
                                "application/json;q=0, application/xml;q=0",
                                406,
                                "application/json"),
                        new AcceptCase(
                                CHALLENGE.GET_UNSUPPORTED_STRUCTURED_JSON_ACCEPT_406,
                                "application/problem+json",
                                406,
                                "application/json"),
                        new AcceptCase(CHALLENGE.GET_ACCEPT_TEXT_XML, "text/xml", 200, "text/xml"),
                        new AcceptCase(
                                CHALLENGE.GET_ACCEPT_VENDOR_XML,
                                "application/vnd.apichallenges.todo+xml",
                                200,
                                "application/vnd.apichallenges.todo+xml"),
                        new AcceptCase(
                                CHALLENGE.GET_ACCEPT_STRUCTURED_XML_WILDCARD,
                                "application/*+xml",
                                200,
                                "application/todo+xml"))) {

            try (HookFixture fixture = new HookFixture(providerFactory.get())) {
                HttpApiRequest request =
                        fixture.request("todos", GET).addHeader("Accept", acceptCase.accept);
                HttpApiResponse response = fixture.todoApiResponse(request, acceptCase.statusCode);

                fixture.hook.run(request, response, fixture.thingifier.apiConfig());

                Assertions.assertEquals(acceptCase.expectedResponseType, response.getType());
                Assertions.assertTrue(
                        fixture.challenger.statusOfChallenge(acceptCase.challenge),
                        acceptCase.challenge + " should complete for " + acceptCase.accept);
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void unsupportedStructuredJsonWildcardCompletesAdvancedAcceptChallenge(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            HttpApiRequest request =
                    fixture.request("todos", GET).addHeader("Accept", "application/*+json");

            fixture.hook.run(
                    request, fixture.todoApiResponse(request, 406), fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_UNSUPPORTED_STRUCTURED_JSON_ACCEPT_406));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void vendorXmlContentTypeChallengeCompletesForTodoXmlRequestBody(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            HttpApiRequest request =
                    fixture.request("todos", POST)
                            .addHeader("Content-Type", "application/vnd.apichallenges.todo+xml")
                            .addHeader("Accept", "application/json")
                            .setBody(
                                    "<todo><title>vendor xml</title><doneStatus>true</doneStatus></todo>");

            HttpApiResponse response = fixture.todoApiResponse(request, 201);

            fixture.hook.run(request, response, fixture.thingifier.apiConfig());

            Assertions.assertEquals("application/json", response.getType());
            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.POST_CREATE_VENDOR_XML));
            Assertions.assertFalse(fixture.challenger.statusOfChallenge(CHALLENGE.POST_CREATE_XML));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void missingTodoTextCalendarRequestReturns404WithoutCalendarChallenge(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            HttpApiResponse calendarResponse =
                    fixture.hook.run(
                            fixture.request("todos/999999", GET)
                                    .addHeader("Accept", "text/calendar"),
                            fixture.apiResponse(406),
                            fixture.thingifier.apiConfig());

            Assertions.assertNotNull(calendarResponse);
            Assertions.assertEquals(404, calendarResponse.getStatusCode());
            Assertions.assertEquals(
                    "application/json", calendarResponse.getHeaders().get("Content-Type"));
            Assertions.assertTrue(calendarResponse.getBody().contains("No such todo"));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODO_ACCEPT_TEXT_CALENDAR));
            Assertions.assertTrue(fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODO_404));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosChallengeCompletesWhenDoneAndNotDoneTodosExist(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");
            fixture.addTodo("not done", "false");

            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("doneStatus", "true")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosChallengeDoesNotCompleteWithoutMixedDoneStatusTodos(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");

            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("doneStatus", "true")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdGreaterThanChallengeCompletesForMatchingSubset(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id>2"),
                    fixture.apiResponseWithBody(200, todosJson(3, 4)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_GREATER_THAN));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdGreaterThanChallengeRequiresGreaterThanOperator(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id>=2"),
                    fixture.apiResponseWithBody(200, todosJson(2, 3, 4)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_GREATER_THAN));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdGreaterThanChallengeRequiresNonEmptyMatchingSubset(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id>2"),
                    fixture.apiResponseWithBody(200, todosJson(1, 3)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_GREATER_THAN));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdGreaterThanChallengeRequiresIntegerThreshold(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id>bob"),
                    fixture.apiResponseWithBody(200, todosJson(3, 4)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_GREATER_THAN));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdGreaterThanChallengeRequiresReturnedTodos(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id>2"),
                    fixture.apiResponseWithBody(200, todosJson()),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_GREATER_THAN));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdLessThanChallengeCompletesForMatchingSubset(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id<3"),
                    fixture.apiResponseWithBody(200, todosJson(1, 2)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_LESS_THAN));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdLessThanChallengeRequiresLessThanOperator(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id<=3"),
                    fixture.apiResponseWithBody(200, todosJson(1, 2, 3)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_LESS_THAN));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdLessThanChallengeRequiresMatchingSubset(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id<3"),
                    fixture.apiResponseWithBody(200, todosJson(1, 3)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_LESS_THAN));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdLessThanChallengeRequiresIntegerThreshold(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id<bob"),
                    fixture.apiResponseWithBody(200, todosJson(1, 2)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_LESS_THAN));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdSingleResultChallengeCompletesForOneTodoAmongMany(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(3);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id=2"),
                    fixture.apiResponseWithBody(200, todosJson(2)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_SINGLE_RESULT));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdSingleResultChallengeRequiresSingleReturnedTodo(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(3);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id=2"),
                    fixture.apiResponseWithBody(200, todosJson(2, 3)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_SINGLE_RESULT));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdSingleResultChallengeRequiresMultipleTodosInDatabase(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(1);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id=1"),
                    fixture.apiResponseWithBody(200, todosJson(1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_SINGLE_RESULT));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosIdSingleResultChallengeRequiresMatchingId(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(3);

            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("id=2"),
                    fixture.apiResponseWithBody(200, todosJson(3)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_ID_SINGLE_RESULT));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosDescriptionRegexChallengeCompletesForMatchingDescriptions(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setFilterableQueryParams("description~=.*fixture.*"),
                    fixture.apiResponseWithBody(
                            200, todosJsonWithDescription("regex fixture description", 1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_REGEX));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosDescriptionRegexChallengeRequiresRegexOperator(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setFilterableQueryParams("description*=*fixture*"),
                    fixture.apiResponseWithBody(
                            200, todosJsonWithDescription("regex fixture description", 1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_REGEX));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosDescriptionRegexChallengeRequiresMatchingDescriptions(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setFilterableQueryParams("description~=.*fixture.*"),
                    fixture.apiResponseWithBody(
                            200, todosJsonWithDescription("different description", 1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_REGEX));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosDescriptionRegexChallengeRequiresNonEmptyDescriptions(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("description~=.*"),
                    fixture.apiResponseWithBody(200, todosJson(1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_REGEX));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosDescriptionRegexChallengeRequiresValidRegex(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("description~=["),
                    fixture.apiResponseWithBody(
                            200, todosJsonWithDescription("regex fixture description", 1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_REGEX));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosDescriptionWildcardChallengeCompletesForMatchingDescriptions(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setFilterableQueryParams("description*=*fixture*"),
                    fixture.apiResponseWithBody(
                            200, todosJsonWithDescription("wildcard fixture description", 1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_WILDCARD));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosDescriptionWildcardChallengeRequiresWildcardOperator(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setFilterableQueryParams("description~=.*fixture.*"),
                    fixture.apiResponseWithBody(
                            200, todosJsonWithDescription("wildcard fixture description", 1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_WILDCARD));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosDescriptionWildcardChallengeRequiresMatchingDescriptions(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setFilterableQueryParams("description*=*fixture*"),
                    fixture.apiResponseWithBody(
                            200, todosJsonWithDescription("different description", 1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_WILDCARD));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosDescriptionWildcardChallengeRequiresNonEmptyDescriptions(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setFilterableQueryParams("description*=*"),
                    fixture.apiResponseWithBody(200, todosJson(1)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_WILDCARD));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void queryFilteredTodosChallengeCompletesWhenDoneAndNotDoneTodosExist(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");
            fixture.addTodo("not done", "false");

            fixture.hook.run(
                    fixture.request("todos", QUERY)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .setBody("doneStatus=true"),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.QUERY_TODOS_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void queryFilteredTodosChallengeDoesNotCompleteFromUrlFilterOnly(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");
            fixture.addTodo("not done", "false");

            fixture.hook.run(
                    fixture.request("todos", QUERY)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .setQueryParams(Map.of("doneStatus", "true")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.QUERY_TODOS_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void queryJsonPathFilteredTodosChallengeCompletesWhenOnlyDoneTodosReturned(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");
            fixture.addTodo("not done", "false");

            fixture.hook.run(
                    fixture.request("todos", QUERY)
                            .addHeader("Content-Type", "application/jsonpath")
                            .setBody("$.todos[?(@.doneStatus == true)]"),
                    fixture.apiResponseWithBody(
                            200,
                            "{\"todos\":[{\"id\":1,\"title\":\"done\",\"doneStatus\":true,\"description\":\"\"}]}"),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.QUERY_TODOS_JSONPATH_FILTERED));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.QUERY_TODOS_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void queryJsonPathFilteredTodosChallengeDoesNotCompleteWhenNotDoneTodosReturned(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");
            fixture.addTodo("not done", "false");

            fixture.hook.run(
                    fixture.request("todos", QUERY)
                            .addHeader("Content-Type", "application/jsonpath")
                            .setBody("$.todos"),
                    fixture.apiResponseWithBody(
                            200,
                            "{\"todos\":[{\"id\":1,\"title\":\"done\",\"doneStatus\":true,\"description\":\"\"},"
                                    + "{\"id\":2,\"title\":\"not done\",\"doneStatus\":false,\"description\":\"\"}]}"),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.QUERY_TODOS_JSONPATH_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void queryStructuredJsonFilteredTodosChallengeCompletesWhenOnlyDoneTodosReturned(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");
            fixture.addTodo("not done", "false");

            fixture.hook.run(
                    fixture.request("todos", QUERY)
                            .addHeader("Content-Type", "application/vnd.thingifier.query+json")
                            .setBody("{\"filter\":{\"doneStatus\":true}}"),
                    fixture.apiResponseWithBody(
                            200,
                            "{\"todos\":[{\"id\":1,\"title\":\"done\",\"doneStatus\":true,\"description\":\"\"}]}"),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.QUERY_TODOS_STRUCTURED_JSON_FILTERED));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.QUERY_TODOS_JSONPATH_FILTERED));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.QUERY_TODOS_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void queryStructuredJsonFilteredTodosChallengeDoesNotCompleteWhenNotDoneTodosReturned(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");
            fixture.addTodo("not done", "false");

            fixture.hook.run(
                    fixture.request("todos", QUERY)
                            .addHeader("Content-Type", "application/vnd.thingifier.query+json")
                            .setBody("{\"filter\":{\"doneStatus\":true}}"),
                    fixture.apiResponseWithBody(
                            200,
                            "{\"todos\":[{\"id\":1,\"title\":\"done\",\"doneStatus\":true,\"description\":\"\"},"
                                    + "{\"id\":2,\"title\":\"not done\",\"doneStatus\":false,\"description\":\"\"}]}"),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.QUERY_TODOS_STRUCTURED_JSON_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void queryStructuredJsonFilteredTodosChallengeRequiresStructuredJsonFilter(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");
            fixture.addTodo("not done", "false");

            fixture.hook.run(
                    fixture.request("todos", QUERY)
                            .addHeader("Content-Type", "application/vnd.thingifier.query+json")
                            .setBody("{\"sort\":[{\"field\":\"id\",\"direction\":\"desc\"}]}"),
                    fixture.apiResponseWithBody(
                            200,
                            "{\"todos\":[{\"id\":1,\"title\":\"done\",\"doneStatus\":true,\"description\":\"\"}]}"),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.QUERY_TODOS_STRUCTURED_JSON_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void sortedAscendingTodosChallengeCompletesForSingleTodoField(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_sortBy", "title")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_SORTED_ASCENDING));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_SORTED_DESCENDING));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_SORTED_MULTIPLE_FIELDS));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void sortedDescendingTodosChallengeCompletesForSingleTodoField(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_sortBy", "-id")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_SORTED_ASCENDING));
            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_SORTED_DESCENDING));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_SORTED_MULTIPLE_FIELDS));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void sortedMultipleFieldsTodosChallengeCompletesForCommaSeparatedTodoFields(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(Map.of("_sortBy", "+doneStatus,-id")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_SORTED_ASCENDING));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_SORTED_DESCENDING));
            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_SORTED_MULTIPLE_FIELDS));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredAndSortedTodosChallengeCompletesForFilterAndSort(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(Map.of("doneStatus", "false", "_sortBy", "-id")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_FILTERED_AND_SORTED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredAndSortedTodosChallengeRequiresFilterAndSort(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_sortBy", "-id")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_FILTERED_AND_SORTED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedLimitTodosChallengeCompletesForEightTodos(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(8);

            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_limit", "8")),
                    fixture.apiResponseWithBody(200, todosJson(1, 2, 3, 4, 5, 6, 7, 8)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_LIMIT));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedLimitTodosChallengeCompletesForShortCurrentPage(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_limit", "8")),
                    fixture.apiResponseWithBody(200, todosJson(1, 2, 3, 4)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_LIMIT));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedLimitTodosChallengeRequiresCurrentPageSize(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(4);

            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_limit", "8")),
                    fixture.apiResponseWithBody(200, todosJson(1, 2, 3, 4, 5)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_LIMIT));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedLimitTodosChallengeRequiresLimit(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET),
                    fixture.apiResponseWithBody(200, todosJson(1, 2, 3, 4, 5, 6, 7, 8)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_LIMIT));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedLimitTodosChallengeRequiresOkStatus(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_limit", "8")),
                    fixture.apiResponseWithBody(400, todosJson(1, 2, 3, 4, 5, 6, 7, 8)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_LIMIT));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedOffsetTodosChallengeCompletesForLimitAndOffset(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(10);

            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(Map.of("_limit", "5", "_offset", "5")),
                    fixture.apiResponseWithBody(200, todosJson(6, 7, 8, 9, 10)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_PAGINATED_LIMIT_OFFSET));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedOffsetTodosChallengeCompletesForShortCurrentPage(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(7);

            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(Map.of("_limit", "5", "_offset", "5")),
                    fixture.apiResponseWithBody(200, todosJson(6, 7)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_PAGINATED_LIMIT_OFFSET));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedOffsetTodosChallengeRequiresOkStatus(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(Map.of("_limit", "5", "_offset", "5")),
                    fixture.apiResponseWithBody(400, todosJson(6, 7, 8, 9, 10)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_PAGINATED_LIMIT_OFFSET));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedOffsetTodosChallengeRequiresOffset(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_limit", "5")),
                    fixture.apiResponseWithBody(200, todosJson(1, 2, 3, 4, 5)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_PAGINATED_LIMIT_OFFSET));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedLimitTooHighChallengeCompletesForBadRequest(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_limit", "21")),
                    fixture.apiResponse(400),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_PAGINATED_LIMIT_TOO_HIGH));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedLimitTooHighChallengeRequiresBadRequest(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_limit", "21")),
                    fixture.apiResponseWithBody(200, todosJson(1, 2, 3, 4, 5)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_PAGINATED_LIMIT_TOO_HIGH));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedSortedTodosChallengeCompletesForDescendingIdPage(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(10);

            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(
                                    Map.of("_sortBy", "-id", "_limit", "5", "_offset", "5")),
                    fixture.apiResponseWithBody(200, todosJson(15, 14, 13, 12, 11)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_SORTED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedSortedTodosChallengeRequiresSort(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(Map.of("_limit", "5", "_offset", "5")),
                    fixture.apiResponseWithBody(200, todosJson(15, 14, 13, 12, 11)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_SORTED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedSortedTodosChallengeRequiresOkStatus(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(
                                    Map.of("_sortBy", "-id", "_limit", "5", "_offset", "5")),
                    fixture.apiResponseWithBody(400, todosJson(15, 14, 13, 12, 11)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_SORTED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedFilteredTodosChallengeCompletesForFalseDoneStatusPage(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(3);

            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(
                                    Map.of("doneStatus", "false", "_limit", "2", "_offset", "1")),
                    fixture.apiResponseWithBody(200, todosJson(2, 3)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedFilteredTodosChallengeCompletesForShortCurrentPage(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodos(2);

            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(
                                    Map.of("doneStatus", "false", "_limit", "2", "_offset", "1")),
                    fixture.apiResponseWithBody(200, todosJson(2)),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedFilteredTodosChallengeRequiresOkStatus(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(
                                    Map.of("doneStatus", "false", "_limit", "2", "_offset", "1")),
                    fixture.apiResponseWithBody(400, todosJson(2, 3)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void paginatedFilteredTodosChallengeRequiresFilter(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET)
                            .setQueryParams(Map.of("_limit", "2", "_offset", "1")),
                    fixture.apiResponseWithBody(200, todosJson(2, 3)),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_PAGINATED_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void sortedTodosChallengesDoNotCompleteForUnknownFields(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("_sortBy", "missing")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_SORTED_ASCENDING));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_SORTED_DESCENDING));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.GET_TODOS_SORTED_MULTIPLE_FIELDS));
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_FILTERED_AND_SORTED));
        }
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("patchChallengeContentTypes")
    public void patchTodoChallengeCompletesForMatchingContentType(
            final String contentType, final CHALLENGE expectedChallenge) {

        try (HookFixture fixture = new HookFixture(new InMemoryThingStoreProvider())) {
            fixture.hook.run(
                    fixture.request("todos/1", PATCH).addHeader("Content-Type", contentType),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(fixture.challenger.statusOfChallenge(expectedChallenge));
            for (CHALLENGE challenge :
                    new CHALLENGE[] {
                        CHALLENGE.PATCH_TODOS_PARTIAL_200,
                        CHALLENGE.PATCH_TODOS_MERGE_PATCH_200,
                        CHALLENGE.PATCH_TODOS_JSON_PATCH_200
                    }) {
                if (challenge != expectedChallenge) {
                    Assertions.assertFalse(fixture.challenger.statusOfChallenge(challenge));
                }
            }
        }
    }

    private static Stream<Arguments> patchChallengeContentTypes() {
        return Stream.of(
                Arguments.of("application/json", CHALLENGE.PATCH_TODOS_PARTIAL_200),
                Arguments.of("application/merge-patch+json", CHALLENGE.PATCH_TODOS_MERGE_PATCH_200),
                Arguments.of("application/json-patch+json", CHALLENGE.PATCH_TODOS_JSON_PATCH_200));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void putTodoBodyIdChallengeCompletesForCollectionPut(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", PUT)
                            .setBody(
                                    "{\"id\":1,\"title\":\"put by body id\",\"doneStatus\":true,\"description\":\"done\"}"),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_BODY_ID_200));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void putTodoUrlIdNoBodyIdChallengeCompletesForInstancePut(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos/1", PUT).setBody("{\"title\":\"put by url id\"}"),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_ID_NO_BODY_ID_200));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void putTodoNoIdChallengeCompletesForCollectionPutWithoutIdentifier(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.hook.run(
                    fixture.request("todos", PUT).setBody("{\"title\":\"missing id\"}"),
                    fixture.apiResponse(422),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_NO_ID_422));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void putTodoMissingUrlIdWithoutBodyIdReturns404(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            HttpApiResponse amendedResponse =
                    fixture.hook.run(
                            fixture.request("todos/999999", PUT)
                                    .setBody("{\"title\":\"missing todo\"}"),
                            fixture.apiResponse(422),
                            fixture.thingifier.apiConfig());

            Assertions.assertNotNull(amendedResponse);
            Assertions.assertEquals(404, amendedResponse.getStatusCode());
            Assertions.assertTrue(amendedResponse.getBody().contains("No such todo"));
            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_ID_NOT_FOUND_404));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void putTodoMissingUrlIdWithMatchingBodyIdRemainsCreateValidation(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            HttpApiResponse amendedResponse =
                    fixture.hook.run(
                            fixture.request("todos/999999", PUT)
                                    .setBody(
                                            "{\"id\":999999,\"title\":\"create attempt\",\"doneStatus\":false,\"description\":\"\"}"),
                            fixture.apiResponse(422),
                            fixture.thingifier.apiConfig());

            Assertions.assertNull(amendedResponse);
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.PUT_TODOS_ID_NOT_FOUND_404));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void postMaxContentChallengeReadsCreatedTodoThroughRepository(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance todo =
                    fixture.addTodo(
                            "2*4*6*8*11*14*17*20*23*26*29*32*35*38*41*44*47*50*",
                            "true",
                            "*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*51*"
                                    + "54*57*60*63*66*69*72*75*78*81*84*87*90*93*96*100*"
                                    + "104*108*112*116*120*124*128*132*136*140*144*148*"
                                    + "152*156*160*164*168*172*176*180*184*188*192*196*200*");

            ApiResponse created =
                    new ApiResponse(201).setLocationHeader("/todos/" + todo.getPrimaryKeyValue());

            fixture.hook.run(
                    fixture.request("todos", POST),
                    fixture.apiResponse(created),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.POST_MAX_OUT_TITLE_DESCRIPTION_LENGTH));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void deleteAllTodosChallengeCompletesWhenRepositoryIsEmpty(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance todo = fixture.addTodo("delete me", "false");
            fixture.repository.entities().delete(todo);

            fixture.hook.run(
                    fixture.request("todos/" + todo.getPrimaryKeyValue(), DELETE),
                    fixture.apiResponse(204),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(fixture.challenger.statusOfChallenge(CHALLENGE.DELETE_ALL_TODOS));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void deleteAllTodosChallengeDoesNotCompleteWhenRepositoryStillHasTodos(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance deletedTodo = fixture.addTodo("delete me", "false");
            fixture.addTodo("keep me", "false");
            fixture.repository.entities().delete(deletedTodo);

            fixture.hook.run(
                    fixture.request("todos/" + deletedTodo.getPrimaryKeyValue(), DELETE),
                    fixture.apiResponse(204),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.DELETE_ALL_TODOS));
        }
    }

    private static String todosJson(final int... ids) {
        StringBuilder json = new StringBuilder("{\"todos\":[");

        for (int index = 0; index < ids.length; index++) {
            if (index > 0) {
                json.append(",");
            }
            json.append("{\"id\":")
                    .append(ids[index])
                    .append(",\"title\":\"todo ")
                    .append(ids[index])
                    .append("\",\"doneStatus\":false,\"description\":\"\"}");
        }

        json.append("]}");
        return json.toString();
    }

    private static String todosJsonWithDescription(final String description, final int... ids) {
        StringBuilder json = new StringBuilder("{\"todos\":[");

        for (int index = 0; index < ids.length; index++) {
            if (index > 0) {
                json.append(",");
            }
            json.append("{\"id\":")
                    .append(ids[index])
                    .append(",\"title\":\"todo ")
                    .append(ids[index])
                    .append("\",\"doneStatus\":false,\"description\":\"")
                    .append(description)
                    .append("\"}");
        }

        json.append("]}");
        return json.toString();
    }

    private static Stream<Arguments> repositoryProviders() {
        return Stream.of(
                Arguments.of(
                        "in-memory",
                        (Supplier<ThingStoreProvider>) InMemoryThingStoreProvider::new),
                Arguments.of(
                        "sqlite-memory",
                        (Supplier<ThingStoreProvider>) SqliteThingStoreProvider::inMemory));
    }

    private static class AcceptCase {
        private final CHALLENGE challenge;
        private final String accept;
        private final int statusCode;
        private final String expectedResponseType;

        AcceptCase(
                final CHALLENGE challenge,
                final String accept,
                final int statusCode,
                final String expectedResponseType) {
            this.challenge = challenge;
            this.accept = accept;
            this.statusCode = statusCode;
            this.expectedResponseType = expectedResponseType;
        }
    }

    private static class HookFixture implements AutoCloseable {
        private final Thingifier thingifier;
        private final Challengers challengers;
        private final ChallengerAuthData challenger;
        private final ChallengerApiResponseHook hook;
        private final EntityDefinition todo;
        private final ThingStore repository;

        HookFixture(final ThingStoreProvider provider) {
            thingifier = new Thingifier(new EntityRelModel(provider));
            todo = thingifier.defineThing("todo", "todos");
            todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            todo.addFields(
                    Field.is("title", FieldType.STRING),
                    Field.is("doneStatus", FieldType.BOOLEAN).withDefaultValue("false"),
                    Field.is("description", FieldType.STRING));

            challengers =
                    new Challengers(thingifier.getERmodel(), Arrays.asList(CHALLENGE.values()));
            challengers.setMultiPlayerMode();
            challenger = challengers.createNewChallenger();
            thingifier.ensureCreatedAndPopulatedInstanceDatabaseNamed(challenger.getXChallenger());
            repository = thingifier.getStore(challenger.getXChallenger());
            hook = new ChallengerApiResponseHook(challengers, thingifier);
        }

        EntityInstance addTodo(final String title, final String doneStatus) {
            return addTodo(title, doneStatus, "");
        }

        void addTodos(final int count) {
            for (int index = 1; index <= count; index++) {
                addTodo("todo " + index, "false");
            }
        }

        EntityInstance addTodo(
                final String title, final String doneStatus, final String description) {
            return repository
                    .entities()
                    .create(
                            EntityInstanceDraft.forEntity(todo)
                                    .withField("title", title)
                                    .withField("doneStatus", doneStatus)
                                    .withField("description", description));
        }

        HttpApiRequest request(final String path, final HttpApiRequest.VERB verb) {
            return new HttpApiRequest(path)
                    .setVerb(verb)
                    .addHeader("X-CHALLENGER", challenger.getXChallenger());
        }

        HttpApiResponse apiResponse(final int statusCode) {
            return apiResponse(new ApiResponse(statusCode));
        }

        HttpApiResponse apiResponseWithBody(final int statusCode, final String body) {
            ApiResponse response = new ApiResponse(statusCode);
            response.setBody(body);
            return apiResponse(response);
        }

        HttpApiResponse todoApiResponse(final HttpApiRequest request, final int statusCode) {
            return apiResponse(request, new ApiResponse(statusCode).resultContainsType(todo));
        }

        HttpApiResponse apiResponse(final HttpApiRequest request, final ApiResponse apiResponse) {
            return new HttpApiResponse(
                    request.getHeaders(),
                    apiResponse,
                    new JsonThing(thingifier.apiConfig().jsonOutput()),
                    thingifier.apiConfig());
        }

        HttpApiResponse apiResponse(final ApiResponse apiResponse) {
            return new HttpApiResponse(
                    new HttpHeadersBlock(),
                    apiResponse,
                    new JsonThing(thingifier.apiConfig().jsonOutput()),
                    thingifier.apiConfig());
        }

        @Override
        public void close() {
            thingifier.close();
        }
    }
}
