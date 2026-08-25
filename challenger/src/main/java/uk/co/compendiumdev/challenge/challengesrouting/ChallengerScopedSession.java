package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.api.security.DataScopeCreationPolicy.ENSURE_EXISTS;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiScopedSessionContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiScopedSessionResult;

public final class ChallengerScopedSession {
    static final String SESSION_NAME = "challengerSession";
    private static final String HEADER_NAME = "X-CHALLENGER";
    private static final String MISSING_CREDENTIAL_MESSAGE =
            "Cannot amend details. Missing a valid X-CHALLENGER header.";
    private static final String INVALID_CREDENTIAL_MESSAGE = MISSING_CREDENTIAL_MESSAGE;

    public void configure(
            final Thingifier thingifier,
            final Challengers challengers,
            final boolean singlePlayerMode) {
        final var scopedSession =
                thingifier
                        .apiContract()
                        .scopedSession(SESSION_NAME)
                        .fromHeader(HEADER_NAME)
                        .authenticateWith(context -> authenticate(challengers, context));

        if (singlePlayerMode) {
            scopedSession
                    .allowAnonymousReadsUsingDataScope(
                            Challengers.SINGLE_PLAYER_GUID, ENSURE_EXISTS)
                    .allowAnonymousWritesUsingDataScope(
                            Challengers.SINGLE_PLAYER_GUID, ENSURE_EXISTS);
        } else {
            scopedSession.allowAnonymousDefaultScopeForReads().requireAuthenticatedScopeForWrites();
        }

        scopedSession
                .onMissingRequiredCredential(401, MISSING_CREDENTIAL_MESSAGE)
                .onInvalidCredential(401, INVALID_CREDENTIAL_MESSAGE);
    }

    private ThingifierApiScopedSessionResult authenticate(
            final Challengers challengers, final ThingifierApiScopedSessionContext context) {
        final ChallengerAuthData challenger = challengers.getChallenger(context.credential());
        if (challenger == null) {
            return ThingifierApiScopedSessionResult.unauthenticated();
        }

        challenger.touch();
        challengers.purgeOldAuthData();
        return ThingifierApiScopedSessionResult.authenticated(challenger)
                .useDataScope(challenger.getXChallenger(), ENSURE_EXISTS);
    }
}
