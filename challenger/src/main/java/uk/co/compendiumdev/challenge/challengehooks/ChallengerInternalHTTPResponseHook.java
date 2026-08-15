package uk.co.compendiumdev.challenge.challengehooks;

import static uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpMethod.*;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.ChallengerState;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challengesrouting.XChallengerHeader;
import uk.co.compendiumdev.challenge.httpserver.CorsHeaders;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BearerAuthHeaderParser;

public class ChallengerInternalHTTPResponseHook implements InternalHttpResponseHook {

    private final Challengers challengers;

    public ChallengerInternalHTTPResponseHook(final Challengers challengers) {
        this.challengers = challengers;
    }

    @Override
    public void run(final InternalHttpRequest request, final InternalHttpResponse response) {

        if (!ApiChallengeHookPath.isApiChallengesEndpoint(request.getPath())) {
            return;
        }

        // allow cross origin requests
        // and swagger
        // https://support.smartbear.com/swaggerhub/docs/en/edit-apis/cors-requirements-for--try-it-out-.html
        CorsHeaders.allowOpenCrossOriginRequests(request, response);

        final String path = ApiChallengeHookPath.normalize(request.getPath());

        ChallengerAuthData challenger =
                challengers.getChallenger(request.getHeader("X-CHALLENGER"));

        // we can complete a challenge while the user is null - creating the user
        if (request.getVerb() == POST
                && path.contentEquals("challenger")
                && response.getStatusCode() == 201) {
            // challenger did not exist so we need to find it to pass the challenge

            String challengerId = response.getHeader("X-Challenger");
            challenger = challengers.getChallenger(challengerId);
            if (challenger != null && challenger.getState() == ChallengerState.NEW) {
                challengers.pass(challenger, CHALLENGE.CREATE_NEW_CHALLENGER);
            }
            if (challenger.getXChallenger().equals(Challengers.SINGLE_PLAYER_GUID)) {
                // single player pass is simulated
                challengers.pass(challenger, CHALLENGE.CREATE_NEW_CHALLENGER);
            }
        }

        if (request.getVerb() == GET
                && path.startsWith("challenger/")
                && response.getStatusCode() == 200) {

            String challengerId = response.getHeader("X-Challenger");
            challenger = challengers.getChallenger(challengerId);
            if (challenger != null
                    && challenger.getState() == ChallengerState.LOADED_FROM_PERSISTENCE) {
                challengers.pass(challenger, CHALLENGE.GET_RESTORE_EXISTING_CHALLENGER);
            }
        }

        if (request.getVerb() == POST
                && path.startsWith("challenger")
                && response.getStatusCode() == 200) {

            String givenChallengerId = request.getHeader("X-Challenger");
            String challengerId = response.getHeader("X-Challenger");
            challenger = challengers.getChallenger(challengerId);
            if (challenger != null
                    && givenChallengerId.equals(challengerId)
                    && challenger.getState() == ChallengerState.LOADED_FROM_PERSISTENCE) {
                challengers.pass(challenger, CHALLENGE.POST_RESTORE_EXISTING_CHALLENGER);
            }
        }

        if (request.getVerb() == PUT
                && path.startsWith("challenger/")
                && (response.getStatusCode() == 200)) {

            String challengerId = response.getHeader("X-Challenger");
            challenger = challengers.getChallenger(challengerId);
            if (challenger != null) {
                challengers.pass(challenger, CHALLENGE.PUT_RESTORABLE_CHALLENGER_PROGRESS_STATUS);
            }
        }

        if (request.getVerb() == PUT
                && path.startsWith("challenger/")
                && (response.getStatusCode() == 409)
                && response.getBody().contains("URL GUID does not match payload X-CHALLENGER")) {

            String challengerId = response.getHeader("X-Challenger");
            challenger = challengers.getChallenger(challengerId);
            if (challenger != null) {
                challengers.pass(challenger, CHALLENGE.PUT_CHALLENGER_GUID_MISMATCH_409);
            }
        }

        if (request.getVerb() == PUT
                && path.startsWith("challenger/")
                && (response.getStatusCode() == 201)) {

            String challengerId = response.getHeader("X-Challenger");
            challenger = challengers.getChallenger(challengerId);
            if (challenger != null) {
                challengers.pass(challenger, CHALLENGE.PUT_NEW_RESTORED_CHALLENGER_PROGRESS_STATUS);
            }
        }

        if (request.getVerb() == GET
                && path.startsWith("challenger/database/")
                && (response.getStatusCode() == 200)) {

            String challengerId = response.getHeader("X-Challenger");
            challenger = challengers.getChallenger(challengerId);
            if (challenger != null) {
                challengers.pass(challenger, CHALLENGE.GET_RESTORABLE_TODOS);
            }
        }

        if (request.getVerb() == PUT
                && path.startsWith("challenger/database/")
                && (response.getStatusCode() == 204)) {

            String challengerId = response.getHeader("X-Challenger");
            challenger = challengers.getChallenger(challengerId);
            if (challenger != null) {
                challengers.pass(challenger, CHALLENGE.PUT_RESTORABLE_TODOS);
            }
        }

        if (challenger == null) {
            if (!path.contentEquals("challenger")
                    && !path.contains(
                            "mirror/r") // exclude mirror endpoints from adding a challenger
            ) {

                if (!response.getHeaders().headerExists("X-CHALLENGER")) {
                    XChallengerHeader.setResultHeaderBasedOnChallenger(response, challenger);
                }
            }
            // cannot track challenges
            if (response.getStatusCode() == 404
                    && (response.getBody() == null || response.getBody().isEmpty())) {
                setResponseAs404(path, request, response);
            }
            return;
        }

        if (challenger != null) {
            if (!response.getHeaders().headerExists("X-CHALLENGER")) {
                if (!path.contains("mirror/r")) {
                    // exclude mirror endpoints from adding a challenger
                    XChallengerHeader.setResultHeaderBasedOnChallenger(response, challenger);
                }
            }
        }

        // No endpoint defined so this 404 created by HTTP server routing
        if (request.getVerb() == GET
                && path.contentEquals("todo")
                && response.getStatusCode() == 404) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_NOT_PLURAL_404);
        }

        if (request.getVerb() == OPTIONS
                && path.contentEquals("todos")
                && response.getStatusCode() == 204) {
            // hack for backwards compatibility with initial solutions
            response.setStatus(200);
            challengers.pass(challenger, CHALLENGE.OPTIONS_TODOS);
        }

        if (request.getVerb() == GET
                && path.contentEquals("todos/export")
                && response.getStatusCode() == 200) {
            String contentDisposition = response.getHeader("Content-Disposition");
            if (contentDisposition.contains("attachment")
                    && contentDisposition.contains("filename=\"todos.csv\"")) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_EXPORT_CSV_CONTENT_DISPOSITION);
            }
            if (contentDisposition.contains("attachment")
                    && contentDisposition.contains("filename=\"todos.html\"")) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_EXPORT_HTML_CONTENT_DISPOSITION);
            }
            if (contentDisposition.contains("attachment")
                    && contentDisposition.contains("filename=\"todos.tsv\"")) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_EXPORT_TSV_CONTENT_DISPOSITION);
            }
        }

        if (request.getVerb() == PUT
                && path.matches("todos/.*")
                && response.getStatusCode() == 422) {
            if (response.getBody().contains("Cannot create todo with PUT due to Auto fields id")) {
                challengers.pass(challenger, CHALLENGE.PUT_TODOS_422);
            }
        }

        if (request.getVerb() == PUT
                && path.matches("todos/.*")
                && response.getStatusCode() == 200) {
            if (request.getBody().toLowerCase().contains("donestatus")
                    && request.getBody().toLowerCase().contains("description")) {
                challengers.pass(challenger, CHALLENGE.PUT_TODOS_FULL_200);
            }
        }

        if (request.getVerb() == PUT
                && path.matches("todos/.*")
                && response.getStatusCode() == 422) {
            if (response.getBody().contains("title : field is mandatory")) {
                challengers.pass(challenger, CHALLENGE.PUT_TODOS_MISSING_TITLE_422);
            }
        }

        if (request.getVerb() == PUT
                && path.matches("todos/.*")
                && response.getStatusCode() == 200) {
            if (!request.getBody().toLowerCase().contains("donestatus")
                    && !request.getBody().toLowerCase().contains("description")) {
                challengers.pass(challenger, CHALLENGE.PUT_TODOS_PARTIAL_200);
            }
        }

        if (request.getVerb() == PUT
                && path.matches("todos/.*")
                && response.getStatusCode() == 422) {
            if (response.getBody().contains("Can not amend id from")) {
                challengers.pass(challenger, CHALLENGE.PUT_TODOS_422_NO_AMEND_ID);
            }
        }

        if (request.getVerb() == POST
                && path.contentEquals("secret/token")
                && request.getHeaders().headerExists("Authorization")
                && request.getHeader("Authorization").length() > 10
                && response.getStatusCode() == 401) {
            challengers.pass(challenger, CHALLENGE.CREATE_SECRET_TOKEN_401);
        }

        if (request.getVerb() == POST
                && path.contentEquals("secret/token")
                && request.getHeaders().headerExists("Authorization")
                && request.getHeader("Authorization").length() > 10
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.CREATE_SECRET_TOKEN_201);
        }

        if (request.getVerb() == GET
                && path.contentEquals("secret/note")
                && request.getHeaders().headerExists("X-AUTH-TOKEN")
                && request.getHeader("X-AUTH-TOKEN").length() > 1
                && response.getStatusCode() == 403) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_NOTE_403);
        }

        if (request.getVerb() == GET
                && path.contentEquals("secret/note")
                && !request.getHeaders().headerExists("X-AUTH-TOKEN")
                && response.getStatusCode() == 401) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_NOTE_401);
        }

        if (request.getVerb() == POST
                && path.contentEquals("secret/note")
                && request.getHeaders().headerExists("X-AUTH-TOKEN")
                && request.getHeader("X-AUTH-TOKEN").length() > 1
                && request.getBody().contains("\"note\"")
                && response.getStatusCode() == 403) {
            challengers.pass(challenger, CHALLENGE.POST_SECRET_NOTE_403);
        }

        if (request.getVerb() == POST
                && path.contentEquals("secret/note")
                && !request.getHeaders().headerExists("X-AUTH-TOKEN")
                && request.getBody().contains("\"note\"")
                && response.getStatusCode() == 401) {
            challengers.pass(challenger, CHALLENGE.POST_SECRET_NOTE_401);
        }

        if (request.getVerb() == POST
                && path.contentEquals("secret/note")
                && request.getHeaders().headerExists("X-AUTH-TOKEN")
                && request.getBody().contains("\"note\"")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.POST_SECRET_NOTE_200);
        }

        if (request.getVerb() == GET
                && path.contentEquals("secret/note")
                && request.getHeaders().headerExists("X-AUTH-TOKEN")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_NOTE_200);
        }

        if (request.getVerb() == GET
                && path.contentEquals("secret/note")
                && request.getHeaders().headerExists("Authorization")
                && new BearerAuthHeaderParser(request.getHeader("Authorization")).isValid()
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_SECRET_NOTE_BEARER_200);
        }

        if (request.getVerb() == POST
                && path.contentEquals("secret/note")
                && request.getHeaders().headerExists("Authorization")
                && new BearerAuthHeaderParser(request.getHeader("Authorization")).isValid()
                && request.getBody().contains("\"note\"")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.POST_SECRET_NOTE_BEARER_200);
        }

        if (response.getStatusCode() == 404
                && (response.getBody() == null || response.getBody().isEmpty())) {
            setResponseAs404(path, request, response);
        }
    }

    private void setResponseAs404(
            final String path,
            final InternalHttpRequest request,
            final InternalHttpResponse response) {

        if (request.getAcceptHeader() != null && !request.getAcceptHeader().isEmpty()) {
            if (request.getAcceptHeader().contains("html")) {
                // treat as a GUI request and redirect
                response.setStatus(307);
                response.setHeader("Location", "/gui/404/" + path);
                return;
            }
            if (request.getAcceptHeader().startsWith("application/")) {
                if (request.getAcceptHeader().endsWith("xml")) {
                    response.setType("application/xml");
                    response.setBody(
                            "<errorMessages><message>404 resource Unknown</message></errorMessages>");
                    return;
                }
                if (request.getAcceptHeader().endsWith("json")) {
                    response.setType("application/json");
                    response.setBody("{\"errorMessages\":[\"404 resource Unknown\"]}");
                    return;
                }
            }
        }
    }
}
