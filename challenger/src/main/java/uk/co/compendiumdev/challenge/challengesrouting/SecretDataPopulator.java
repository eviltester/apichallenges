package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.core.EntityRelModel.DEFAULT_DATABASE_NAME;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class SecretDataPopulator implements RepositoryDataPopulator {
    private final Challengers challengers;

    SecretDataPopulator(final Challengers challengers) {
        this.challengers = challengers;
    }

    @Override
    public void populate(final ERSchema schema, final ThingStore store) {
        final SecretSeedData seedData = seedDataFor(store.databaseKey());
        if (seedData == null) {
            return;
        }

        upsertFixedInstance(
                store,
                schema.getEntityDefinitionNamed(SecretThingifier.NOTE_ENTITY),
                SecretThingifier.NOTE_ID,
                "note",
                seedData.note());
        upsertFixedInstance(
                store,
                schema.getEntityDefinitionNamed(SecretThingifier.TOKEN_ENTITY),
                SecretThingifier.TOKEN_ID,
                "token",
                seedData.token());
    }

    private SecretSeedData seedDataFor(final String databaseKey) {
        if (DEFAULT_DATABASE_NAME.equals(databaseKey)) {
            return new SecretSeedData(
                    AuthRoutes.READ_ONLY_SECRET_NOTE, AuthRoutes.READ_ONLY_AUTH_TOKEN);
        }

        final ChallengerAuthData challenger = challengers.getChallenger(databaseKey);
        if (challenger == null) {
            return null;
        }

        return new SecretSeedData(challenger.getNote(), challenger.getXAuthToken());
    }

    private void upsertFixedInstance(
            final ThingStore store,
            final EntityDefinition entity,
            final String id,
            final String valueFieldName,
            final String value) {
        final EntityInstanceDraft draft =
                EntityInstanceDraft.forEntity(entity)
                        .withField("id", id)
                        .withField(valueFieldName, value == null ? "" : value);
        final EntityInstance existing = store.entityQueries().findByPrimaryKey(entity, id);

        if (existing == null) {
            store.entities().create(draft);
        } else {
            store.entities().replace(existing, draft);
        }
    }

    private record SecretSeedData(String note, String token) {}
}
