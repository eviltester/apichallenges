package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation.UPDATE;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.api.validation.ApiOperationValidators;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;

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

    Thingifier get(
            final String pathPrefix, final SecretNoteAuth auth, final Challengers challengers) {
        final Thingifier secrets = new Thingifier();
        secrets.setDocumentation(
                "Secret Notes",
                "Authentication challenge support for secret token and note endpoints.");
        secrets.setDataGenerator(new SecretDataPopulator(challengers));

        final EntityDefinition note = secrets.defineThing(NOTE_ENTITY, NOTE_COLLECTION, 1);
        note.addAsPrimaryKeyField(Field.is("id", STRING).makeMandatory());
        note.addFields(
                Field.is("note", STRING)
                        .makeMandatory()
                        .withValidation(new MaximumLengthValidationRule(100)));
        note.defineView(NOTE_VIEW).hideFields("id");

        final EntityDefinition token = secrets.defineThing(TOKEN_ENTITY, TOKEN_COLLECTION, 1);
        token.addAsPrimaryKeyField(Field.is("id", STRING).makeMandatory());
        token.addFields(Field.is("token", STRING).makeMandatory());
        token.defineView(TOKEN_VIEW).hideFields("id");

        final ThingifierApiConfig config = new ThingifierApiConfig(pathPrefix);
        config.setSupportsMultipleDatabases(true);
        config.setReturnSingleGetItemsAsCollection(false);
        config.setDefaultContentTypeAsJson(true);
        config.writeMethods().entities().postCan(UPDATE);
        secrets.apiDefaults().setFrom(config);

        secrets.apiContract().entity(NOTE_ENTITY).defaultEntityView(NOTE_VIEW);
        secrets.apiContract().entity(TOKEN_ENTITY).defaultEntityView(TOKEN_VIEW);
        secrets.apiContract().disableEntityRoutes(NOTE_COLLECTION);
        secrets.apiContract().disableEntityRoutes(TOKEN_COLLECTION);
        secrets.apiContract().security().basic(BASIC_SCHEME, BASIC_REALM);
        secrets.apiContract().security().bearer(TOKEN_SCHEME);
        secrets.apiContract().security().apiKey(API_KEY_SCHEME, "X-AUTH-TOKEN");
        secrets.apiContract().authenticator(BASIC_SCHEME, auth::authenticateAdminPassword);
        secrets.apiContract().authenticator(TOKEN_SCHEME, auth::authenticateSecretToken);
        secrets.apiContract().authenticator(API_KEY_SCHEME, auth::authenticateSecretToken);
        final ThingifierApiRouteRule getSecretToken =
                secrets.apiContract()
                        .route(RoutingVerb.GET, "/secret/token")
                        .mapsToEntity(TOKEN_ENTITY)
                        .withFixedIdentifier(TOKEN_ID)
                        .defaultEntityView(TOKEN_VIEW)
                        .secureWithBasicAuth(BASIC_SCHEME);
        getSecretToken.onSuccess().addInstanceFieldAsHeader("X-AUTH-TOKEN", "token");

        final ThingifierApiRouteRule getSecretNote =
                secrets.apiContract()
                        .route(RoutingVerb.GET, "/secret/note")
                        .mapsToEntity(NOTE_ENTITY)
                        .withFixedIdentifier(NOTE_ID)
                        .defaultEntityView(NOTE_VIEW)
                        .secureWithAnyOf(TOKEN_SCHEME, API_KEY_SCHEME)
                        .authorizeWith(auth::authorizeSecretNote);
        getSecretNote.onError(406).suppressBody();

        final ThingifierApiRouteRule headSecretNote =
                secrets.apiContract()
                        .route(RoutingVerb.HEAD, "/secret/note")
                        .mapsToEntity(NOTE_ENTITY)
                        .withFixedIdentifier(NOTE_ID)
                        .defaultEntityView(NOTE_VIEW)
                        .secureWithAnyOf(TOKEN_SCHEME, API_KEY_SCHEME)
                        .authorizeWith(auth::authorizeSecretNote);
        headSecretNote.onError(406).suppressBody();

        final ThingifierApiRouteRule postSecretNote =
                secrets.apiContract()
                        .route(RoutingVerb.POST, "/secret/note")
                        .mapsToEntity(NOTE_ENTITY)
                        .withFixedIdentifier(NOTE_ID)
                        .defaultEntityView(NOTE_VIEW)
                        .entityCan(UPDATE)
                        .secureWithAnyOf(TOKEN_SCHEME, API_KEY_SCHEME)
                        .authorizeWith(auth::authorizeSecretNote)
                        .withApiOperationValidator(
                                "note-body-required",
                                ApiOperationValidators.requireBodyFields("note")
                                        .onMissing(422, "note is required"));
        postSecretNote.onError(406).suppressBody();

        fixedMethodNotAllowed(secrets, RoutingVerb.HEAD, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(secrets, RoutingVerb.POST, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(secrets, RoutingVerb.PUT, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(secrets, RoutingVerb.DELETE, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(secrets, RoutingVerb.PATCH, "/secret/token", TOKEN_ENTITY, TOKEN_ID);
        fixedMethodNotAllowed(secrets, RoutingVerb.TRACE, "/secret/token", TOKEN_ENTITY, TOKEN_ID);

        fixedMethodNotAllowed(secrets, RoutingVerb.PUT, "/secret/note", NOTE_ENTITY, NOTE_ID);
        fixedMethodNotAllowed(secrets, RoutingVerb.DELETE, "/secret/note", NOTE_ENTITY, NOTE_ID);
        fixedMethodNotAllowed(secrets, RoutingVerb.PATCH, "/secret/note", NOTE_ENTITY, NOTE_ID);
        fixedMethodNotAllowed(secrets, RoutingVerb.TRACE, "/secret/note", NOTE_ENTITY, NOTE_ID);

        secrets.generateData(
                uk.co.compendiumdev.thingifier.core.EntityRelModel.DEFAULT_DATABASE_NAME);

        return secrets;
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
}
