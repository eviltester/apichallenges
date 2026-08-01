package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApiLiveRequestJavascriptTest {

    @Test
    void liveRequestWidgetsKeepFilterOperatorsReadableInDisplay() throws IOException {
        String javascript = apiLiveRequestJavascript();

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
        String javascript = apiLiveRequestJavascript();

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
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(
                javascript.contains("editActions.className = 'sim-live-edit-actions'"));
        Assertions.assertTrue(
                javascript.indexOf("editActions.appendChild(resetButton)")
                        < javascript.indexOf("editActions.appendChild(prettyPrintButton)"));
    }

    @Test
    void solvingRequestWidgetsCheckChallengeStatusAndShowTemporaryFeedback() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("placeholder.dataset.challengeId || ''"));
        Assertions.assertTrue(javascript.contains("function checkChallengePassed(request)"));
        Assertions.assertTrue(javascript.contains("/gui/challenge-status/"));
        Assertions.assertTrue(javascript.contains("'X-CHALLENGER': challenger"));
        Assertions.assertTrue(javascript.contains("Challenge Passed"));
        Assertions.assertTrue(javascript.contains("Challenge Not Passed Yet"));
        Assertions.assertTrue(javascript.contains("function updateChallengeCompletedBanners()"));
        Assertions.assertTrue(javascript.contains("function showChallengeCompletedBanner"));
        Assertions.assertTrue(javascript.contains("function showChallengeFireworks()"));
        Assertions.assertTrue(javascript.contains("prefers-reduced-motion: reduce"));
        Assertions.assertTrue(javascript.contains(".sim-live-fireworks"));
        Assertions.assertTrue(javascript.contains("sim-live-firework-ring"));
        Assertions.assertTrue(javascript.contains("sim-live-firework-confetti"));
        Assertions.assertTrue(javascript.contains("for (let index = 0; index < 90; index += 1)"));
        Assertions.assertTrue(javascript.contains("}, 4600);"));
        Assertions.assertTrue(
                javascript.contains(".solution-challenge-completed[data-challenge-id]"));
        Assertions.assertTrue(javascript.contains("updateChallengeCompletedBanners();"));
        Assertions.assertTrue(javascript.contains("}, 10000);"));
        Assertions.assertTrue(javascript.contains("let wasChallengePassedBeforeRequest = false"));
        Assertions.assertTrue(javascript.contains("wasChallengePassedBeforeRequest ="));
        Assertions.assertTrue(javascript.contains("if (!wasChallengePassedBeforeRequest)"));
        Assertions.assertTrue(javascript.contains("if (!requestWasSent || !request.challengeId)"));
    }

    @Test
    void restrictedClientRequestsValidateOriginAndAllowedPathPrefixes() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("function validateRequestTarget(request)"));
        Assertions.assertTrue(javascript.contains("parsed.origin !== window.location.origin"));
        Assertions.assertTrue(javascript.contains("function pathMatchesPrefix(path, prefix)"));
        Assertions.assertTrue(
                javascript.contains("return path === prefix || path.indexOf(`${prefix}/`) === 0;"));
        Assertions.assertTrue(javascript.contains("hasUnresolvedPathParameter(parsed.pathname)"));
        Assertions.assertTrue(javascript.contains("request.allowedPathPrefixes.some"));
    }

    @Test
    void restrictedClientCommandsAreBlockedWhenTargetValidationFails() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(
                javascript.contains("function buildRestrictedCommand(request, builder)"));
        Assertions.assertTrue(
                javascript.contains("Fix the request target before copying a command."));
        Assertions.assertTrue(
                javascript.contains("buildRestrictedCommand(request, buildCurlCommand)"));
        Assertions.assertTrue(
                javascript.contains("buildRestrictedCommand(request, buildWgetCommand)"));
        Assertions.assertTrue(
                javascript.contains("responseArea.status.textContent = 'Request blocked'"));
    }

    @Test
    void bodyEditorAndRequestBodiesAreLimitedToBodyMethods() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(
                javascript.contains("const BODY_METHODS = ['POST', 'PUT', 'PATCH', 'QUERY'];"));
        Assertions.assertTrue(javascript.contains("function methodAllowsBody(method)"));
        Assertions.assertTrue(javascript.contains("function requestBodyAllowed(request)"));
        Assertions.assertTrue(javascript.contains("function bodyForRequest(request)"));
        Assertions.assertTrue(javascript.contains("bodyLabel.hidden = !showBody"));
    }

    @Test
    void publicRenderAllRendersLateInjectedWidgets() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(
                javascript.contains(
                        "window.ApiChallengesLiveRequest = window.ApiChallengesLiveRequest || {};"));
        Assertions.assertTrue(
                javascript.contains("window.ApiChallengesLiveRequest.renderAll = renderAll;"));
        Assertions.assertTrue(javascript.contains("onReady(renderAll);"));
    }

    @Test
    void docsEndpointExperimentsRenderBeforeRequestDefinitionLists() throws IOException {
        String javascript = apiDocsLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("function insertBeforeFirstEndpointList"));
        Assertions.assertTrue(javascript.contains("if (sibling.tagName === 'UL')"));
        Assertions.assertTrue(javascript.contains("parent.insertBefore(details, sibling);"));
        Assertions.assertTrue(javascript.contains("insertBeforeFirstEndpointList("));
    }

    private String apiLiveRequestJavascript() throws IOException {
        try (InputStream stream =
                getClass().getResourceAsStream("/public/js/api-live-request.js")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String apiDocsLiveRequestJavascript() throws IOException {
        try (InputStream stream =
                getClass().getResourceAsStream("/public/js/api-docs-live-request.js")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
