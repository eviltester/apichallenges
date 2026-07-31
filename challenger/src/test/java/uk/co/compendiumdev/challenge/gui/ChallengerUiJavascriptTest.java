package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChallengerUiJavascriptTest {

    @Test
    void usesCombinedLocalSaveControls() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertTrue(javascript.contains("Save Locally"));
        Assertions.assertTrue(javascript.contains("Saved Locally"));
        Assertions.assertTrue(javascript.contains("${doneCount} / ${totalCount} Challenges"));
        Assertions.assertTrue(javascript.contains("saveCurrentChallengerToLocalStorage"));
        Assertions.assertFalse(javascript.contains("Save Your Progress"));
        Assertions.assertFalse(javascript.contains("Save Your Todos"));
    }

    @Test
    void inputChallengerGuidUsesInPageDialog() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertFalse(javascript.contains("prompt("));
        Assertions.assertTrue(javascript.contains("ensureChallengerGuidDialog"));
        Assertions.assertTrue(javascript.contains("document.createElement('dialog')"));
        Assertions.assertTrue(javascript.contains("challenger-guid-input"));
        Assertions.assertTrue(javascript.contains("challenger-guid-form"));
        Assertions.assertTrue(javascript.contains("useInputChallengerGuid"));
        Assertions.assertTrue(javascript.contains("closeChallengerGuidDialog"));
        Assertions.assertTrue(javascript.contains("dialog.showModal()"));
        Assertions.assertTrue(javascript.contains("ensureChallengerGuidDialogStyles"));
        Assertions.assertTrue(javascript.contains("width: min(92vw, 36rem)"));
        Assertions.assertTrue(javascript.contains("text-align: center"));
        Assertions.assertTrue(javascript.contains("width: 100%"));
        Assertions.assertTrue(javascript.contains("font-size: 1.1rem"));
        Assertions.assertTrue(javascript.contains("challenger-guid-actions"));
        Assertions.assertTrue(javascript.contains("#challenger-guid-actions button"));
        Assertions.assertTrue(javascript.contains("font-size: 2rem"));
        Assertions.assertTrue(javascript.contains("padding: 1rem 1.5rem"));
    }

    @Test
    void movesManualRestoreIntoManageChallengerGuidsList() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertTrue(javascript.contains("Manage Challenger GUIDs"));
        Assertions.assertTrue(javascript.contains("Input a Challenger GUID to use"));
        Assertions.assertTrue(javascript.contains("restoreLocalChallenger"));
        Assertions.assertTrue(javascript.contains("restoreStatusElementId"));
        Assertions.assertTrue(javascript.contains("restore unavailable"));
        Assertions.assertTrue(javascript.contains("canRestoreGuid(myguid)"));
        Assertions.assertTrue(javascript.contains("forgetGuid"));
        Assertions.assertFalse(javascript.contains("Previously Used Challenger GUIDs"));
        Assertions.assertFalse(javascript.contains("Restore Locally Saved Progress"));
        Assertions.assertFalse(javascript.contains("Restore Locally Saved Todos"));
    }

    @Test
    void autoRestoresUnknownGuidWithSessionStorageGuard() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertTrue(javascript.contains("Restoring locally saved session"));
        Assertions.assertTrue(javascript.contains("autoRestoreLocalChallenger"));
        Assertions.assertTrue(javascript.contains("sessionStorage.getItem"));
        Assertions.assertTrue(javascript.contains("sessionStorage.setItem"));
        Assertions.assertTrue(javascript.contains(".auto-restore-attempted"));
        Assertions.assertTrue(javascript.contains("Saved todos found"));
    }

    @Test
    void combinedRestoreRestoresProgressBeforeTodos() throws IOException {
        String javascript = challengerUiJavascript();

        int progressRestore =
                javascript.indexOf("restoreChallengerProgressInSystem(sanitizedGuid)");
        int rememberGuid = javascript.indexOf("rememberChallengerGuid(sanitizedGuid)");
        int todosRestore = javascript.indexOf("restoreTodosInSystem(sanitizedGuid)");

        Assertions.assertTrue(progressRestore > 0);
        Assertions.assertTrue(rememberGuid > progressRestore);
        Assertions.assertTrue(todosRestore > rememberGuid);
    }

    @Test
    void manualRestoreRendersStatusUnderGuidAndResetsButtonOnFailure() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertTrue(javascript.contains("restoreLocalChallenger('${myguid}', this)"));
        Assertions.assertTrue(
                javascript.contains("<br><span id='${restoreStatusElementId(myguid)}'></span>"));
        Assertions.assertTrue(
                javascript.contains("setLocalRestoreMessage(error.message, sanitizedGuid)"));
        Assertions.assertTrue(
                javascript.contains("setRestoreButtonState(button, \"restore\", false)"));
        Assertions.assertTrue(javascript.contains("parsedBody.errorMessages"));
    }

    @Test
    void previousGuidListForgetsUnrestorableGuidsButKeepsSinglePlayer() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertTrue(javascript.contains("cleanUnrestorableGuids"));
        Assertions.assertTrue(javascript.contains("shouldKeepGuidInPreviousList"));
        Assertions.assertTrue(javascript.contains("isProtectedSinglePlayerGuid"));
        Assertions.assertTrue(
                javascript.contains(
                        "const SINGLE_PLAYER_CHALLENGER_GUID = \"rest-api-challenges-single-player\""));
        Assertions.assertTrue(javascript.contains("removeSavedGuidData(myguid)"));
        Assertions.assertTrue(
                javascript.contains("localStorage.removeItem(`${sanitizedGuid}.data`)"));
        Assertions.assertTrue(
                javascript.contains("localStorage.removeItem(`${sanitizedGuid}.progress`)"));
        Assertions.assertTrue(javascript.contains("if(isProtectedSinglePlayerGuid(aguid)){"));
        Assertions.assertTrue(javascript.contains("if(!isProtectedSinglePlayerGuid(myguid)){"));
    }

    @Test
    void previousGuidListDefaultsToFiveEntries() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertTrue(javascript.contains("const PREVIOUS_CHALLENGER_GUIDS_MAX = 5"));
        Assertions.assertTrue(javascript.contains("capPreviousGuidArray"));
        Assertions.assertTrue(
                javascript.contains("PREVIOUS_CHALLENGER_GUIDS_MAX - protectedGuids.length"));
        Assertions.assertTrue(
                javascript.contains(
                        "normalGuids.slice(Math.max(normalGuids.length - maxNormalGuids, 0))"));
        Assertions.assertTrue(javascript.contains("guidsArray = capPreviousGuidArray(guidsArray)"));
    }

    @Test
    void definesCompactProgressAchievements() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertTrue(javascript.contains("const ACHIEVEMENT_DEFINITIONS"));
        Assertions.assertTrue(javascript.contains("A New Challenger"));
        Assertions.assertTrue(javascript.contains("You Got This"));
        Assertions.assertTrue(javascript.contains("In the Race"));
        Assertions.assertTrue(javascript.contains("Reward Unlocked: Server session storage"));
        Assertions.assertTrue(javascript.contains("Completist"));
        Assertions.assertTrue(javascript.contains("challengeKey: \"CREATE_NEW_CHALLENGER\""));
        Assertions.assertTrue(javascript.contains("challengeKey: \"GET_CHALLENGES\""));
        Assertions.assertTrue(javascript.contains("threshold: 60"));
        Assertions.assertTrue(javascript.contains("allChallenges: true"));
        Assertions.assertTrue(javascript.contains("function showAchievements()"));
        Assertions.assertTrue(javascript.contains("achievement-rail-panel"));
        Assertions.assertTrue(javascript.contains("addEventListener(\"mouseenter\""));
        Assertions.assertTrue(javascript.contains("addEventListener(\"click\""));
    }

    @Test
    void successfulRestoreRefreshesLocalProgressBeforeRedirect() throws IOException {
        String javascript = challengerUiJavascript();

        int progressRestore =
                javascript.indexOf("restoreChallengerProgressInSystem(sanitizedGuid)");
        int progressRefresh =
                javascript.indexOf("refreshLocalChallengerProgressFromSystem(sanitizedGuid)");
        int localProgressSave =
                javascript.indexOf("saveChallengerProgressToLocalStorage(challenger)");
        int redirect =
                javascript.indexOf(
                        "location.href = `/gui/challenges/${sanitizedGuid}${location.hash || \"\"}`");

        Assertions.assertTrue(progressRestore > 0);
        Assertions.assertTrue(progressRefresh > progressRestore);
        Assertions.assertTrue(localProgressSave > 0);
        Assertions.assertTrue(redirect > progressRefresh);
    }

    private String challengerUiJavascript() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/public/js/challengerui.js")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
