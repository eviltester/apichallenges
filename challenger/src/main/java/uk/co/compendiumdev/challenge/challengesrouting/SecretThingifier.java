package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
import static uk.co.compendiumdev.thingifier.core.EntityRelModel.DEFAULT_DATABASE_NAME;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.api.validation.ApiOperationValidators;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class SecretThingifier {
    static final String NOTE_ENTITY = "secretnote";
    static final String NOTE_COLLECTION = "secretnotes";
    static final String NOTE_ID = "note";
    static final String NOTE_VIEW = "SecretNoteResponse";
    static final String TOKEN_ENTITY = "secrettoken";
    static final String TOKEN_COLLECTION = "secrettokens";
    static final String TOKEN_ID = "token";
    static final String TOKEN_VIEW = "SecretTokenResponse";
    static final String TOKEN_SCHEME = "secretNoteToken";
    static final String API_KEY_SCHEME = "secretNoteApiKey";
    static final String BASIC_SCHEME = "secretTokenBasic";
    static final String BASIC_REALM = "User Visible Realm";

    void configure(
            final Thingifier thingifier,
            final SecretNoteAuth auth,
            final SecretDataPopulator secretDataPopulator) {
        final EntityDefinition note = thingifier.defineThing(NOTE_ENTITY, NOTE_COLLECTION, 1);
        note.addAsPrimaryKeyField(Field.is("id", STRING).makeMandatory());
        note.addFields(
                Field.is("note", STRING)
                        .makeMandatory()
                        .withValidation(new MaximumLengthValidationRule(100)));
        note.defineView(NOTE_VIEW).hideFields("id");

        final EntityDefinition token = thingifier.defineThing(TOKEN_ENTITY, TOKEN_COLLECTION, 1);
        token.addAsPrimaryKeyField(Field.is("id", STRING).makeMandatory());
        token.addFields(Field.is("token", STRING).makeMandatory());
        token.defineView(TOKEN_VIEW).hideFields("id");

        thingifier.setDataGenerator(
                new CompositeRepositoryDataPopulator(
                        thingifier.getDefaultDataPopulator(), secretDataPopulator));
        secretDataPopulator.populate(thingifier, DEFAULT_DATABASE_NAME);

        thingifier.apiContract().entity(NOTE_ENTITY).defaultEntityView(NOTE_VIEW);
        thingifier.apiContract().entity(TOKEN_ENTITY).defaultEntityView(TOKEN_VIEW);
        thingifier.apiContract().disableEntityRoutes(NOTE_COLLECTION);
        thingifier.apiContract().disableEntityRoutes(TOKEN_COLLECTION);
        thingifier.apiContract().security().basic(BASIC_SCHEME, BASIC_REALM);
        thingifier.apiContract().security().bearer(TOKEN_SCHEME);
        thingifier.apiContract().security().apiKey(API_KEY_SCHEME, "X-AUTH-TOKEN");
        thingifier.apiContract().authenticator(BASIC_SCHEME, auth::authenticateAdminPassword);
        thingifier.apiContract().authenticator(TOKEN_SCHEME, auth::authenticateSecretToken);
        thingifier.apiContract().authenticator(API_KEY_SCHEME, auth::authenticateSecretToken);
        final ThingifierApiRouteRule getSecretToken =
                thingifier
                        .apiContract()
                        .route(RoutingVerb.GET, "/secret/token")
                        .mapsToEntity(TOKEN_ENTITY)
                        .withFixedIdentifier(TOKEN_ID)
                        .defaultEntityView(TOKEN_VIEW)
                        .secureWithBasicAuth(BASIC_SCHEME)
                        .addDocumentation(
                                "GET /api/secret/token with basic auth to get an X-AUTH-TOKEN header and token response body for access to /api/secret/note.");
        getSecretToken.onSuccess().addInstanceFieldAsHeader("X-AUTH-TOKEN", "token");

        final ThingifierApiRouteRule getSecretNote =
                thingifier
                        .apiContract()
                        .route(RoutingVerb.GET, "/secret/note")
                        .mapsToEntity(NOTE_ENTITY)
                        .withFixedIdentifier(NOTE_ID)
                        .defaultEntityView(NOTE_VIEW)
                        .secureWithAnyOf(TOKEN_SCHEME, API_KEY_SCHEME)
                        .authorizeWith(auth::authorizeSecretNote)
                        .addDocumentation(
                                "GET /api/secret/note with X-AUTH-TOKEN to return the secret note for the user.");
        getSecretNote.onError(406).suppressBody();

        final ThingifierApiRouteRule headSecretNote =
                thingifier
                        .apiContract()
                        .route(RoutingVerb.HEAD, "/secret/note")
                        .mapsToEntity(NOTE_ENTITY)
                        .withFixedIdentifier(NOTE_ID)
                        .defaultEntityView(NOTE_VIEW)
                        .secureWithAnyOf(TOKEN_SCHEME, API_KEY_SCHEME)
                        .authorizeWith(auth::authorizeSecretNote);
        headSecretNote.onError(406).suppressBody();

        final ThingifierApiRouteRule postSecretNote =
                thingifier
                        .apiContract()
                        .route(RoutingVerb.POST, "/secret/note")
                        .mapsToEntity(NOTE_ENTITY)
                        .withFixedIdentifier(NOTE_ID)
                        .defaultEntityView(NOTE_VIEW)
                        .entityCan(UPDATE)
                        .secureWithAnyOf(TOKEN_SCHEME, API_KEY_SCHEME)
                        .authorizeWith(auth::authorizeSecretNote)
                        .addDocumentation(
                                "POST /api/secret/note with X-AUTH-TOKEN, and a payload of `{'note':'contents of note'}` to amend the contents of the secret note.")
                        .withApiOperationValidator(
                                "note-body-required",
                                ApiOperationValidators.requireBodyFields("note")
                                        .onMissing(422, "note is required"));
        postSecretNote.onError(406).suppressBody();

        fixedMethodNotAllowed(
                thingifier, RoutingVerb.HEAD, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(
                thingifier, RoutingVerb.POST, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(thingifier, RoutingVerb.PUT, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(
                thingifier, RoutingVerb.DELETE, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(
                thingifier, RoutingVerb.PATCH, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(
                thingifier, RoutingVerb.TRACE, "/secret/token", TOKEN_ENTITY, TOKEN_ID);

        fixedMethodNotAllowed(thingifier, RoutingVerb.PUT, "/secret/note", NOTE_ENTITY, NOTE_ID);
        fixedMethodNotAllowed(thingifier, RoutingVerb.DELETE, "/secret/note", NOTE_ENTITY, NOTE_ID);
        fixedMethodNotAllowed(thingifier, RoutingVerb.PATCH, "/secret/note", NOTE_ENTITY, NOTE_ID);
        fixedMethodNotAllowed(thingifier, RoutingVerb.TRACE, "/secret/note", NOTE_ENTITY, NOTE_ID);
    }

    private void fixedMethodNotAllowed(
            final Thingifier secrets,
            final RoutingVerb verb,
            final String path,
            final String entityName,
            final String identifier) {
        secrets.apiContract()
                .route(verb, path)
                .mapsToEntity(entityName)
                .withFixedIdentifier(identifier)
                .methodNotAllowed()
                .hide();
    }

    private static final class CompositeRepositoryDataPopulator implements RepositoryDataPopulator {
        private final RepositoryDataPopulator first;
        private final RepositoryDataPopulator second;

        private CompositeRepositoryDataPopulator(
                final RepositoryDataPopulator first, final RepositoryDataPopulator second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void populate(final ERSchema schema, final ThingStore store) {
            if (first != null) {
                first.populate(schema, store);
            }
            if (second != null) {
                second.populate(schema, store);
            }
        }
    }
}
