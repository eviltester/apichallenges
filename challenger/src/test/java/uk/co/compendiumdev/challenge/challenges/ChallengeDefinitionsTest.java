package uk.co.compendiumdev.challenge.challenges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.ChallengerConfig;

public class ChallengeDefinitionsTest {

    @Test
    void allSinglePlayerChallengesHaveHints() {
        assertAllChallengesHaveHints(new ChallengerConfig());
    }

    @Test
    void allMultiPlayerNoStorageChallengesHaveHints() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();
        config.setToNoPersistenceMode();

        assertAllChallengesHaveHints(config);
    }

    @Test
    void allMultiPlayerLocalStorageChallengesHaveHints() {
        ChallengerConfig config = new ChallengerConfig();
        config.setToMultiPlayerMode();

        assertAllChallengesHaveHints(config);
    }

    private void assertAllChallengesHaveHints(final ChallengerConfig config) {
        Collection<ChallengeDefinitionData> challenges =
                new ChallengeDefinitions(config).getChallenges();
        List<String> missingHints = new ArrayList<>();

        for (ChallengeDefinitionData challenge : challenges) {
            if (!challenge.hasHints()) {
                missingHints.add(challenge.id + " " + challenge.name);
            }
        }

        Assertions.assertTrue(
                missingHints.isEmpty(),
                "Challenges missing hints: " + String.join(", ", missingHints));
    }
}
