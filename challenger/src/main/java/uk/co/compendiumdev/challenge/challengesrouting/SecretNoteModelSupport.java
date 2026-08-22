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

    private EntityInstance noteInstance(final String databaseName) {
        return secretThingifier
                .getStore(databaseName)
                .entityQueries()
                .findByPrimaryKey(secretNote, SecretThingifier.NOTE_ID);
    }
}
