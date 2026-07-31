package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimLiveRequestJavascriptTest {

    @Test
    void liveRequestWidgetsKeepFilterOperatorsReadableInDisplay() throws IOException {
        String javascript = simLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("function readableUrl(url)"));
        Assertions.assertTrue(javascript.contains(".replace(/%3E/gi, '>')"));
        Assertions.assertTrue(javascript.contains(".replace(/%3C/gi, '<')"));
        Assertions.assertTrue(javascript.contains(".replace(/%7E/gi, '~')"));
        Assertions.assertTrue(javascript.contains(".replace(/%2A/gi, '*')"));
        Assertions.assertTrue(javascript.contains("url.textContent = readableUrl(request.url)"));
        Assertions.assertTrue(javascript.contains("urlInput.value = readableUrl(request.url)"));
        Assertions.assertTrue(javascript.contains("\"${readableUrl(request.url)}\""));
        Assertions.assertTrue(javascript.contains("return fetch(request.url, options);"));
    }

    @Test
    void liveRequestWidgetsSupportRestoredChallengerPlaceholders() throws IOException {
        String javascript = simLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("function restoredChallenger()"));
        Assertions.assertTrue(
                javascript.contains("function currentChallengerJsonForRestoredChallenger()"));
        Assertions.assertTrue(javascript.contains("json.xChallenger = restored"));
        Assertions.assertTrue(javascript.contains("restoredChallenger: restoredChallenger()"));
        Assertions.assertTrue(
                javascript.contains("currentChallengerJsonForRestoredChallenger: values[5]"));
    }

    @Test
    void editableRequestControlsPlaceResetBeforePrettyPrintInSharedActionRow() throws IOException {
        String javascript = simLiveRequestJavascript();

        Assertions.assertTrue(
                javascript.contains("editActions.className = 'sim-live-edit-actions'"));
        Assertions.assertTrue(
                javascript.indexOf("editActions.appendChild(resetButton)")
                        < javascript.indexOf("editActions.appendChild(prettyPrintButton)"));
    }

    @Test
    void solvingRequestWidgetsCheckChallengeStatusAndShowTemporaryFeedback() throws IOException {
        String javascript = simLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("placeholder.dataset.challengeId || ''"));
        Assertions.assertTrue(javascript.contains("function checkChallengePassed(request)"));
        Assertions.assertTrue(javascript.contains("/gui/challenge-status/"));
        Assertions.assertTrue(javascript.contains("'X-CHALLENGER': challenger"));
        Assertions.assertTrue(javascript.contains("Challenge Passed"));
        Assertions.assertTrue(javascript.contains("Challenge Not Passed Yet"));
        Assertions.assertTrue(javascript.contains("function updateChallengeCompletedBanners()"));
        Assertions.assertTrue(javascript.contains("function showChallengeCompletedBanner"));
        Assertions.assertTrue(
                javascript.contains(".solution-challenge-completed[data-challenge-id]"));
        Assertions.assertTrue(javascript.contains("updateChallengeCompletedBanners();"));
        Assertions.assertTrue(javascript.contains("}, 10000);"));
        Assertions.assertTrue(javascript.contains("if (!requestWasSent || !request.challengeId)"));
    }

    private String simLiveRequestJavascript() throws IOException {
        try (InputStream stream =
                getClass().getResourceAsStream("/public/js/sim-live-request.js")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
