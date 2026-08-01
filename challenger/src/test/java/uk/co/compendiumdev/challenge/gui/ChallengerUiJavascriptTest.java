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
        Assertions.assertTrue(javascript.contains("Clearance Granted"));
        Assertions.assertTrue(
                javascript.contains("Complete Authentication and Authorization challenges 72-81"));
        Assertions.assertTrue(javascript.contains("Misc Mastery"));
        Assertions.assertTrue(javascript.contains("Complete Miscellaneous challenges 82 and 83"));
        Assertions.assertTrue(javascript.contains("Completist"));
        Assertions.assertTrue(javascript.contains("challengeKey: \"CREATE_NEW_CHALLENGER\""));
        Assertions.assertTrue(javascript.contains("challengeKey: \"GET_CHALLENGES\""));
        Assertions.assertTrue(javascript.contains("challengeKeys: ["));
        Assertions.assertTrue(javascript.contains("\"CREATE_SECRET_TOKEN_401\""));
        Assertions.assertTrue(javascript.contains("\"POST_SECRET_NOTE_BEARER_200\""));
        Assertions.assertTrue(javascript.contains("\"DELETE_ALL_TODOS\""));
        Assertions.assertTrue(javascript.contains("\"POST_ALL_TODOS\""));
        Assertions.assertTrue(javascript.contains("threshold: 60"));
        Assertions.assertTrue(javascript.contains("function areChallengeKeysComplete"));
        Assertions.assertTrue(javascript.contains("challengeKeys.every"));
        Assertions.assertTrue(javascript.contains("allChallenges: true"));
        Assertions.assertTrue(javascript.contains("function showAchievements()"));
        Assertions.assertTrue(javascript.contains("achievement-rail-panel"));
        Assertions.assertTrue(javascript.contains("function selectAchievementMedal"));
        Assertions.assertTrue(javascript.contains("function setSelectedAchievementDetails"));
        Assertions.assertTrue(
                javascript.contains(
                        "addEventListener(\"mouseenter\", function(){ setAchievementDetails(medal, false); })"));
        Assertions.assertTrue(
                javascript.contains(
                        "addEventListener(\"mouseleave\", function(){ setSelectedAchievementDetails(panel); })"));
        Assertions.assertTrue(javascript.contains("addEventListener(\"click\""));
        Assertions.assertTrue(javascript.contains("setAchievementDetails(medal, true)"));

        Assertions.assertTrue(javascript.indexOf("Better than the Best") > 0);
        Assertions.assertTrue(
                javascript.indexOf("Clearance Granted")
                        > javascript.indexOf("Better than the Best"));
        Assertions.assertTrue(
                javascript.indexOf("Misc Mastery") > javascript.indexOf("Clearance Granted"));
        Assertions.assertTrue(
                javascript.indexOf("Completist") > javascript.indexOf("Misc Mastery"));
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

    @Test
    void progressRefreshUsesXhrAndUpdatesExistingPageContent() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertTrue(
                javascript.contains("const CHALLENGE_PROGRESS_REFRESH_INTERVAL_MS = 60000"));
        Assertions.assertTrue(
                javascript.contains("const CHALLENGE_PROGRESS_AUTO_MONITOR_MS = 30 * 60 * 1000"));
        Assertions.assertTrue(javascript.contains("function fetchChallengeProgressStatus()"));
        Assertions.assertTrue(javascript.contains("fetch(\"/gui/challenge-status\""));
        Assertions.assertTrue(javascript.contains("headers[\"X-CHALLENGER\"] = challenger"));
        Assertions.assertTrue(javascript.contains("progress.known!==true"));
        Assertions.assertTrue(javascript.contains("function applyChallengeProgressStatus"));
        Assertions.assertTrue(javascript.contains("function doneCountFromChallengeProgress"));
        Assertions.assertTrue(javascript.contains("function currentProgressDoneCount"));
        Assertions.assertTrue(
                javascript.contains("document.challengerData = progress.challengerData || {}"));
        Assertions.assertTrue(
                javascript.contains("document.databaseData = progress.databaseData || {}"));
        Assertions.assertTrue(javascript.contains("function updateChallengeRows"));
        Assertions.assertTrue(javascript.contains("tr[data-challenge-id]"));
        Assertions.assertTrue(javascript.contains(".challenge-done-status"));
        Assertions.assertTrue(javascript.contains("challenge-status-newly-completed"));
        Assertions.assertTrue(javascript.contains("function updateCurrentStatus"));
        Assertions.assertTrue(javascript.contains("function updateAchievements"));
        Assertions.assertTrue(
                javascript.contains(
                        "<button type='button' id='refresh-challenge-status'>Refresh Status</button>"));
        Assertions.assertFalse(
                javascript.contains("<button onclick=location.reload()>Refresh Status"));
    }

    @Test
    void progressAutoMonitorStopsOnFailuresAndChallengePassedEventsRefresh() throws IOException {
        String javascript = challengerUiJavascript();

        Assertions.assertTrue(javascript.contains("id='auto-monitor-challenge-progress'"));
        Assertions.assertTrue(
                javascript.contains("Auto monitor challenge progress for 30 minutes"));
        Assertions.assertTrue(javascript.contains("function startChallengeProgressAutoMonitor()"));
        Assertions.assertTrue(
                javascript.contains("challengeProgressAutoMonitorIntervalId = window.setInterval"));
        Assertions.assertTrue(javascript.contains("}, CHALLENGE_PROGRESS_REFRESH_INTERVAL_MS);"));
        Assertions.assertTrue(
                javascript.contains("challengeProgressAutoMonitorTimeoutId = window.setTimeout"));
        Assertions.assertTrue(javascript.contains("}, CHALLENGE_PROGRESS_AUTO_MONITOR_MS);"));
        Assertions.assertTrue(javascript.contains("function stopChallengeProgressAutoMonitor"));
        Assertions.assertTrue(javascript.contains("checkbox.checked = false"));
        Assertions.assertTrue(javascript.contains("Auto monitor stopped: ${message}"));
        Assertions.assertTrue(
                javascript.contains("isAuto || isChallengeProgressAutoMonitorActive()"));
        Assertions.assertTrue(
                javascript.contains("const previousDoneCount = currentProgressDoneCount();"));
        Assertions.assertTrue(javascript.contains("auto: isAuto"));
        Assertions.assertTrue(javascript.contains("previousDoneCount: previousDoneCount"));
        Assertions.assertTrue(
                javascript.contains(
                        "const shouldShowFireworksOnProgressIncrease =\n"
                                + "        isAuto || (options && options.fireworksOnProgressIncrease"
                                + " === true);"));
        Assertions.assertTrue(
                javascript.contains(
                        "fireworksOnProgressIncrease:" + " shouldShowFireworksOnProgressIncrease"));
        Assertions.assertTrue(
                javascript.contains(
                        "options.fireworksOnProgressIncrease === true &&\n"
                                + "            newDoneCount > options.previousDoneCount"));
        Assertions.assertTrue(
                javascript.contains("function showProgressRefreshChallengeFireworks"));
        Assertions.assertTrue(
                javascript.contains("window.ApiChallengesLiveRequest.showFireworks();"));
        Assertions.assertTrue(
                javascript.contains(
                        "refreshChallengeProgress({auto: false,"
                                + " fireworksOnProgressIncrease: true});"));
        Assertions.assertTrue(
                javascript.contains("window.addEventListener(\"apiChallenges:challenge-passed\""));
        Assertions.assertTrue(javascript.contains("refreshChallengeProgress({auto: false});"));
    }

    private String challengerUiJavascript() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/public/js/challengerui.js")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
