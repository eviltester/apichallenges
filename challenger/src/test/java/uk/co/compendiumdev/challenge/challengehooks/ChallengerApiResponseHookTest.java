package uk.co.compendiumdev.challenge.challengehooks;

import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.GET;
import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.PUT;

import java.util.Arrays;
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
    public void textCalendarCollectionRequestIsNotHandledByResponseHook(
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
            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406));
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

    private static Stream<Arguments> repositoryProviders() {
        return Stream.of(
                Arguments.of(
                        "in-memory",
                        (Supplier<ThingStoreProvider>) InMemoryThingStoreProvider::new),
                Arguments.of(
                        "sqlite-memory",
                        (Supplier<ThingStoreProvider>) SqliteThingStoreProvider::inMemory));
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
