package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

final class SecretNoteModelSupport {
    private final Thingifier secretThingifier;
    private final Challengers challengers;
    private final EntityDefinition secretNote;

    SecretNoteModelSupport(final Thingifier secretThingifier, final Challengers challengers) {
        this.secretThingifier = secretThingifier;
        this.challengers = challengers;
        this.secretNote = secretThingifier.getDefinitionNamed(SecretThingifier.NOTE_ENTITY);
    }

    HttpApiResponseHook responseHook() {
        return this::syncSuccessfulPost;
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private HttpApiResponse syncSuccessfulPost(
            final HttpApiRequest request,
            final HttpApiResponse response,
            final ThingifierApiConfig config) {
        addSecretNoteAuthenticationChallenge(request, response);

        if (request.getVerb() != HttpApiRequest.VERB.POST || response.getStatusCode() != 200) {
            return null;
        }

        final ChallengerAuthData challenger =
                challengers.getChallenger(request.getHeader("X-CHALLENGER"));
        if (challenger == null) {
            return null;
        }

        final EntityInstance note = noteInstance(challenger.getXChallenger());
        if (note != null) {
            challenger.setNote(note.getFieldValue("note").asString());
        }
        return null;
    }

    private void addSecretNoteAuthenticationChallenge(
            final HttpApiRequest request, final HttpApiResponse response) {
        if (response.getStatusCode() != 401
                || response.getHeaders().get("WWW-Authenticate") != null
                || !isSecretNotePath(normalizedPath(request.getPath()))) {
            return;
        }

        final String authToken = request.getHeader("X-AUTH-TOKEN");
        final String challengerId = request.getHeader("X-CHALLENGER");
        if (authToken == null || challengerIsUnknown(challengerId)) {
            response.getHeaders().put("WWW-Authenticate", "Bearer");
        }
    }

    private boolean challengerIsUnknown(final String challengerId) {
        return challengerId != null
                && !challengerId.isBlank()
                && challengers.getChallenger(challengerId) == null;
    }

    private String normalizedPath(final String path) {
        if (path == null || path.isBlank()) {
            return "";
        }

        final String normalized = path.trim().replace('\\', '/');
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private boolean isSecretNotePath(final String path) {
        return "/secret/note".equals(path) || path.endsWith("/secret/note");
    }

    private EntityInstance noteInstance(final String databaseName) {
        return secretThingifier
                .getStore(databaseName)
                .entityQueries()
                .findByPrimaryKey(secretNote, SecretThingifier.NOTE_ID);
    }
}
