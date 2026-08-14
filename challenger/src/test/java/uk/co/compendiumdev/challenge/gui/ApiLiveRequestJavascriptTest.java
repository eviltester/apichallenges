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
        Assertions.assertTrue(javascript.contains(".replace(/%7B/gi, '{')"));
        Assertions.assertTrue(javascript.contains(".replace(/%7D/gi, '}')"));
        Assertions.assertTrue(javascript.contains(".replace(/%7E/gi, '~')"));
        Assertions.assertTrue(javascript.contains(".replace(/%2A/gi, '*')"));
        Assertions.assertTrue(
                javascript.contains("return readableUrl(`${parsed.pathname}${parsed.search}`)"));
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
    void liveRequestWidgetsSupportSimpleApiTutorialPlaceholders() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("function randomSimpleApiIsbn()"));
        Assertions.assertTrue(javascript.contains("function createdSimpleApiItemIdFromResponse"));
        Assertions.assertTrue(javascript.contains("function createdSimpleApiItemIsbnFromRequest"));
        Assertions.assertTrue(javascript.contains("storeLastCreatedSimpleApiItemId"));
        Assertions.assertTrue(javascript.contains("storeLastCreatedSimpleApiItemIsbn"));
        Assertions.assertTrue(javascript.contains("randomSimpleApiIsbn: values[6]"));
        Assertions.assertTrue(javascript.contains("lastCreatedSimpleApiItemId: values[7]"));
        Assertions.assertTrue(javascript.contains("lastCreatedSimpleApiItemIsbn: values[8]"));
        Assertions.assertTrue(javascript.contains("!widgetState.request.hasDynamicValues"));
    }

    @Test
    void liveRequestWidgetsSupportReusableSimpleApiIsbnGenerator() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("function buildSimpleApiRandomIsbnDetails()"));
        Assertions.assertTrue(javascript.contains("function enhanceSimpleApiRandomIsbnDetails"));
        Assertions.assertTrue(javascript.contains("Generate Random SimpleAPI ISBN"));
        Assertions.assertTrue(javascript.contains("data-simpleapi-random-isbn"));
        Assertions.assertTrue(javascript.contains("randomSimpleApiIsbn().then(function (isbn)"));
        Assertions.assertTrue(javascript.contains("enhanceSimpleApiRandomIsbnDetailsAll();"));
        Assertions.assertTrue(
                javascript.contains("window.ApiChallengesSimpleApiRandomIsbn.buildDetails"));
        Assertions.assertTrue(
                javascript.contains("window.ApiChallengesSimpleApiRandomIsbn.enhanceAll"));
        Assertions.assertTrue(
                javascript.contains("window.ApiChallengesSimpleApiRandomIsbn.randomIsbn"));
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
        Assertions.assertTrue(javascript.contains("function dispatchChallengePassedEvent"));
        Assertions.assertTrue(
                javascript.contains(
                        "window.dispatchEvent(new CustomEvent('apiChallenges:challenge-passed'"));
        Assertions.assertTrue(javascript.contains("detail: { challengeId: String(challengeId) }"));
        Assertions.assertTrue(
                javascript.contains("dispatchChallengePassedEvent(request.challengeId)"));
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
        Assertions.assertTrue(
                javascript.contains("function urlWithPathOnly(path, allowedPathPrefixes, fallbackUrl)"));
        Assertions.assertTrue(
                javascript.contains("pathMatchesPrefix(parsed.pathname, prefix)"));
        Assertions.assertTrue(
                javascript.contains("return `${window.location.origin}${fallback.pathname}${fallback.search}`"));
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
        Assertions.assertTrue(javascript.contains("function methodAllowsBody(method, request)"));
        Assertions.assertTrue(javascript.contains("request && request.bodyMethods === 'all'"));
        Assertions.assertTrue(javascript.contains("function requestBodyAllowed(request)"));
        Assertions.assertTrue(javascript.contains("function bodyForRequest(request)"));
        Assertions.assertTrue(javascript.contains("bodyLabel.hidden = !showBody"));
        Assertions.assertTrue(javascript.contains("syncBodyControlVisibility();"));
    }

    @Test
    void liveRequestWidgetsCanOptIntoCustomMethods() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(
                javascript.contains("customMethod: placeholder.dataset.customMethod === 'true'"));
        Assertions.assertTrue(
                javascript.contains("bodyMethods: (placeholder.dataset.bodyMethods || '').trim()"));
        Assertions.assertTrue(javascript.contains("const CUSTOM_METHOD_VALUE = '__custom__';"));
        Assertions.assertTrue(javascript.contains("appendDefaultMethodOptions(methodSelect);"));
        Assertions.assertTrue(javascript.contains("customOption.textContent = 'Custom...'"));
        Assertions.assertTrue(javascript.contains("methodInput.type = 'text'"));
        Assertions.assertTrue(javascript.contains("methodInput.placeholder = 'CUSTOM'"));
        Assertions.assertTrue(
                javascript.contains(
                        "methodInput.setAttribute('aria-label', 'Custom HTTP method')"));
        Assertions.assertTrue(javascript.contains("methodInput.hidden = !customSelected"));
        Assertions.assertTrue(javascript.contains("syncMethodControlState(selectedCustomMethod)"));
        Assertions.assertTrue(javascript.contains("methodInput.addEventListener('blur'"));
    }

    @Test
    void liveRequestWidgetsSupportConfiguredFieldsAndResponseCopyActions() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(
                javascript.contains(
                        "queryEditable: placeholder.dataset.queryEditable !== 'false'"));
        Assertions.assertTrue(javascript.contains("'path'"));
        Assertions.assertTrue(
                javascript.contains("function urlWithPathOnly(path, allowedPathPrefixes, fallbackUrl)"));
        Assertions.assertTrue(javascript.contains("request.editMode === 'path'"));
        Assertions.assertTrue(
                javascript.contains(
                        "request.editMode === 'path' && request.customMethod"));
        Assertions.assertTrue(javascript.contains("pathInput.className = 'sim-live-edit-path'"));
        Assertions.assertTrue(javascript.contains("pathInput.type = 'text'"));
        Assertions.assertTrue(javascript.contains("pathInput.value = pathAndQueryFromUrl"));
        Assertions.assertTrue(
                javascript.contains(
                        "request.url = urlWithPathOnly(pathInput.value, request.allowedPathPrefixes, request.url)"));
        Assertions.assertTrue(
                javascript.contains("controls.pathInput.value = pathAndQueryFromUrl(request.url)"));
        Assertions.assertTrue(
                javascript.contains(
                        "widgetState.controls.pathInput.value = pathAndQueryFromUrl(widgetState.request.url)"));
        Assertions.assertTrue(javascript.contains("pathInput.addEventListener('blur'"));
        Assertions.assertTrue(javascript.contains("if (request.queryEditable)"));
        Assertions.assertTrue(javascript.contains("return 'apichallenges.readonly.xAuthToken'"));
        Assertions.assertTrue(
                javascript.contains("function authTokenStorageKeyForRequest(request)"));
        Assertions.assertTrue(javascript.contains("authToken: currentAuthToken(request)"));
        Assertions.assertTrue(javascript.contains("storeAuthToken(responseAuthToken, request)"));
        Assertions.assertTrue(
                javascript.contains("responseActions.className = 'sim-live-response-actions'"));
        Assertions.assertTrue(
                javascript.contains("responseBodyCopyButton.textContent = 'Copy body'"));
        Assertions.assertTrue(
                javascript.contains("responseHeadersCopyButton.textContent = 'Copy headers'"));
        Assertions.assertTrue(javascript.contains("rawTab.textContent = 'Raw'"));
        Assertions.assertTrue(
                javascript.contains("responseRawCopyButton.textContent = 'Copy raw'"));
        Assertions.assertTrue(
                javascript.contains("function rawResponseToText(response, bodyText)"));
        Assertions.assertTrue(
                javascript.contains("responseActions.appendChild(responseBodyCopyButton)"));
        Assertions.assertTrue(
                javascript.contains("responseActions.appendChild(responseHeadersCopyButton)"));
        Assertions.assertTrue(
                javascript.contains("responseActions.appendChild(responseRawCopyButton)"));
        Assertions.assertTrue(
                javascript.contains("copyText(bodyPanel.textContent, responseBodyCopyButton)"));
        Assertions.assertTrue(
                javascript.contains(
                        "copyText(headersPanel.textContent, responseHeadersCopyButton)"));
        Assertions.assertTrue(
                javascript.contains("copyText(rawPanel.textContent, responseRawCopyButton)"));
        Assertions.assertTrue(
                javascript.contains(
                        "responseArea.rawPanel.textContent = rawResponseToText(response, text)"));
        Assertions.assertTrue(javascript.contains("'.sim-live-response-panel'"));
        final int responseElements = javascript.indexOf("elements: [");
        final int bodyPanel = javascript.indexOf("bodyPanel,", responseElements);
        final int headersPanel = javascript.indexOf("headersPanel,", responseElements);
        final int rawPanel = javascript.indexOf("rawPanel,", responseElements);
        final int responseActions = javascript.indexOf("responseActions,", responseElements);
        Assertions.assertTrue(responseElements >= 0);
        Assertions.assertTrue(bodyPanel < headersPanel);
        Assertions.assertTrue(headersPanel < rawPanel);
        Assertions.assertTrue(rawPanel < responseActions);
    }

    @Test
    void publicRenderAllRendersLateInjectedWidgets() throws IOException {
        String javascript = apiLiveRequestJavascript();

        Assertions.assertTrue(
                javascript.contains(
                        "window.ApiChallengesLiveRequest = window.ApiChallengesLiveRequest || {};"));
        Assertions.assertTrue(
                javascript.contains("window.ApiChallengesLiveRequest.renderAll = renderAll;"));
        Assertions.assertTrue(
                javascript.contains(
                        "window.ApiChallengesLiveRequest.showFireworks = showChallengeFireworks;"));
        Assertions.assertTrue(javascript.contains("onReady(renderAll);"));
    }

    @Test
    void docsEndpointExperimentsRenderBeforeRequestDefinitionLists() throws IOException {
        String javascript = apiDocsLiveRequestJavascript();

        Assertions.assertTrue(javascript.contains("function insertBeforeFirstEndpointList"));
        Assertions.assertTrue(javascript.contains("if (sibling.tagName === 'UL')"));
        Assertions.assertTrue(javascript.contains("parent.insertBefore(details, sibling);"));
        Assertions.assertTrue(javascript.contains("insertBeforeFirstEndpointList("));
        Assertions.assertTrue(javascript.contains("simpleApiRandomIsbn: true"));
        Assertions.assertFalse(javascript.contains("function buildSimpleApiRandomIsbnDetails()"));
        Assertions.assertTrue(javascript.contains("function buildEndpointPracticeTools"));
        Assertions.assertTrue(
                javascript.contains(
                        "fragment.appendChild(buildLiveRequestDetails(endpointPath, method, config))"));
        Assertions.assertTrue(
                javascript.contains("window.ApiChallengesSimpleApiRandomIsbn.buildDetails"));
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
