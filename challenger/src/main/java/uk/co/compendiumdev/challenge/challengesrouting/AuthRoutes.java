package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;

public class AuthRoutes {
    static final String READ_ONLY_AUTH_TOKEN = "00000000-0000-4000-8000-000000000000";
    static final String READ_ONLY_SECRET_NOTE = "This is the read-only secret note.";

    public void configure(final Thingifier thingifier, final Challengers challengers) {
        final SecretDataPopulator secretDataPopulator = new SecretDataPopulator(challengers);
        final SecretNoteAuth secretNoteAuth =
                new SecretNoteAuth(challengers, thingifier, secretDataPopulator);
        new SecretThingifier()
                .configure(thingifier, secretNoteAuth, secretDataPopulator, challengers);
    }
}
