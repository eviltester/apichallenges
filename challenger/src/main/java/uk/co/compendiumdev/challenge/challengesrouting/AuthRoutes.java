package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.*;

import java.util.List;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteHandler;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.adapter.httpserver.SimpleHttpRouteCreator;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.HttpServerRequestToInternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.InternalHttpResponseToHttpServer;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.HttpApiResponseToInternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.InternalHttpRequestToHttpApiRequest;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.*;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BasicAuthHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BearerAuthHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.domain.instances.validation.EntityInstanceStateValidator;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

// TODO: This should be using a Thingifier to do the work of XML JSON etc... like the simulation
public class AuthRoutes {
    private static final String LIVE_WIDGET_HEADER = "X-API-Challenges-Live-Widget";
    private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final String BEARER_CHALLENGE = "Bearer";
    static final String READ_ONLY_AUTH_TOKEN = "00000000-0000-4000-8000-000000000000";
    static final String READ_ONLY_SECRET_NOTE = "This is the read-only secret note.";

    private Thingifier secretNoteStore;
    private EntityDefinition secretNote;
    private ThingifierHttpApi httpApi;
    private JsonThing jsonThing;
    private final EntityInstanceStateValidator stateValidator = new EntityInstanceStateValidator();

    public void configure(
            final Challengers challengers, final ThingifierApiDocumentationDefn apiDefn) {
        configure(challengers, apiDefn, "");
    }

    public void configure(
            final Challengers challengers,
            final ThingifierApiDocumentationDefn apiDefn,
            final String pathPrefix) {

        final String secretTokenPath =
                ApiChallengeRoutePath.withPrefix(pathPrefix, "/secret/token");
        final String secretNotePath = ApiChallengeRoutePath.withPrefix(pathPrefix, "/secret/note");

        // authentication and authorisation
        // - create a 'secret' note which can be stored against session using an auth token

        this.secretNoteStore = new Thingifier();

        this.secretNote = this.secretNoteStore.defineThing("secretnote", "secretnotes");

        this.secretNote.addFields(
                Field.is("note", FieldType.STRING)
                        .makeMandatory()
                        .withValidation(new MaximumLengthValidationRule(100))
                        .withDefaultValue(""));

        this.httpApi = new ThingifierHttpApi(this.secretNoteStore);
        this.jsonThing = new JsonThing(this.secretNoteStore.apiConfig().jsonOutput());

        SimpleHttpRouteCreator.addHandler(
                secretTokenPath,
                "options",
                (request, result) -> {
                    result.status(204);
                    // disallow DELETE, PATCH, TRACE
                    result.header("Allow", "GET, POST, OPTIONS");
                    return "";
                });

        // TODO: this still feels tightly coupled to HTTP routing; route handling should delegate
        // to an internal auth use case.

        // GET /secret/token with basic auth returns a token for read-only tutorial examples.
        get(
                secretTokenPath,
                (request, result) -> {
                    if (!requestHasValidBasicAuth(request, result)) {
                        return "";
                    }

                    ChallengerAuthData challenger =
                            challengers.getChallenger(request.header("X-CHALLENGER"));
                    if (challenger == null && hasXChallengerHeader(request)) {
                        result.status(401);
                        XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
                        return "";
                    }

                    final String authToken =
                            challenger == null ? READ_ONLY_AUTH_TOKEN : challenger.getXAuthToken();

                    result.header("X-AUTH-TOKEN", authToken);
                    result.header("Content-Type", "application/json");
                    result.status(200);
                    return "{\"token\":\"" + authToken + "\"}";
                });

        // POST /secret/token with basic auth to get a session token to use as X-AUTH-TOKEN header
        // todo: or {username, password} payload
        post(
                secretTokenPath,
                (request, result) -> {
                    if (!requestHasValidBasicAuth(request, result)) {
                        return "";
                    }

                    ChallengerAuthData challenger =
                            challengers.getChallenger(request.header("X-CHALLENGER"));

                    if (challenger == null) {
                        result.status(401);
                        XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
                        return "";
                    }

                    // if no header X-AUTH-TOKEN then grant one
                    result.header("X-AUTH-TOKEN", challenger.getXAuthToken());
                    result.status(201);
                    return "";
                });

        SimpleHttpRouteCreator.routeStatusWhenNot(
                405, secretTokenPath, List.of("get", "post", "options"));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST,
                                secretTokenPath,
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "POST %s with basic auth to get a secret token to use as X-AUTH-TOKEN header, to allow access to the %s end points."
                                        .formatted(secretTokenPath, secretNotePath))
                        .addPossibleStatuses(201, 401)
                        .secureWithBasicAuth());

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET,
                                secretTokenPath,
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "GET %s with basic auth to get an X-AUTH-TOKEN header for read-only access to %s."
                                        .formatted(secretTokenPath, secretNotePath))
                        .addPossibleStatuses(200, 401)
                        .secureWithBasicAuth());

        // POST /secret/note GET /secret/note - limit note to 100 chars
        // no auth token will receive a 403
        // auth token which does not match the session will receive a 401
        // header X-AUTH-TOKEN: token given - if token not found (then) 401

        SimpleHttpRouteCreator.addHandler(
                secretNotePath,
                "options",
                (request, result) -> {
                    result.status(204);
                    // disallow POST, DELETE, PATCH, TRACE
                    result.header("Allow", "GET, HEAD, POST, OPTIONS");
                    return "";
                });

        HttpRouteHandler getSecretNote =
                (request, result) -> {
                    String authToken = authTokenFromRequest(request);

                    result.header("Content-Type", "application/json");

                    ChallengerAuthData challenger =
                            challengers.getChallenger(request.header("X-CHALLENGER"));

                    if (challenger == null) {
                        if (hasXChallengerHeader(request)) {
                            XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
                            return unauthorizedSecretNote(result);
                        }
                        return getReadOnlySecretNote(request, result, authToken);
                    }

                    if (authToken == null || authToken.isEmpty()) {
                        return unauthorizedSecretNote(result);
                    }

                    if (!authToken.contentEquals(challenger.getXAuthToken())) {
                        result.status(403); // given token is not allowed to access anything
                        return "";
                    }

                    AcceptHeaderParser acceptHeaderParser =
                            new AcceptHeaderParser(request.header("ACCEPT"));
                    if (!acceptHeaderParser.missingAcceptHeader()
                            && !acceptHeaderParser.isSupportedHeader()) {
                        result.status(406);
                        return "";
                    }

                    return renderSecretNoteResponse(request, result, challenger.getNote());

                    // return resultBasedOnAcceptHeader(result, request.header("ACCEPT"),
                    // challenger.getNote());
                };

        get(
                secretNotePath,
                (request, result) -> {
                    return getSecretNote.handle(request, result);
                });

        head(
                secretNotePath,
                (request, result) -> {
                    getSecretNote.handle(request, result);
                    return "";
                });

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET,
                                secretNotePath,
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "GET %s with X-AUTH-TOKEN to return the secret note for the user."
                                        .formatted(secretNotePath))
                        .addPossibleStatuses(200, 401, 403)
                        .addCustomHeader("X-AUTH-TOKEN", "string"));

        post(
                secretNotePath,
                (request, result) -> {
                    final String authorization = request.header("Authorization");
                    String authToken = request.header("X-AUTH-TOKEN");

                    AcceptHeaderParser acceptHeaderParser =
                            new AcceptHeaderParser(request.header("ACCEPT"));
                    if (!acceptHeaderParser.missingAcceptHeader()
                            && !acceptHeaderParser.isSupportedHeader()) {
                        result.status(406);
                        return "";
                    }

                    ContentTypeHeaderParser contentTypeParser =
                            new ContentTypeHeaderParser(request.header("CONTENT-TYPE"));
                    if (!contentTypeParser.isJSON() && !contentTypeParser.isXML()) {
                        result.status(415);
                        return "";
                    }

                    // todo: if no X-CHALLENGER provided then, search memory for authToken and use
                    // associated
                    //       challenger
                    ChallengerAuthData challenger =
                            challengers.getChallenger(request.header("X-CHALLENGER"));

                    if (challenger == null) {
                        XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
                        return unauthorizedSecretNote(result);
                    }

                    result.header("X-CHALLENGER", challenger.getXChallenger());
                    // set content-type header for error responses
                    if (acceptHeaderParser.hasAPreferenceForXml()) {
                        result.header("Content-Type", "application/xml");
                    } else {
                        result.header("Content-Type", "application/json");
                    }

                    // authorization bearer token will take precedence over X-AUTH-HEADER
                    if (authorization != null && !authorization.isEmpty()) {
                        final BearerAuthHeaderParser bearerToken =
                                new BearerAuthHeaderParser(authorization);
                        if (bearerToken.isBearerToken() && bearerToken.isValid()) {
                            authToken = bearerToken.getToken();
                        }
                    }

                    if (authToken == null || authToken.isEmpty()) {
                        return unauthorizedSecretNote(result);
                    }

                    if (!authToken.contentEquals(challenger.getXAuthToken())) {
                        result.status(403); // given token is not allowed to access anything
                        return "";
                    }

                    if (!acceptHeaderParser.missingAcceptHeader()
                            && !acceptHeaderParser.isSupportedHeader()) {
                        result.status(406);
                        return "";
                    }

                    final InternalHttpRequest internalRequest =
                            HttpServerRequestToInternalHttpRequest.convert(request);
                    final HttpApiRequest myRequest =
                            InternalHttpRequestToHttpApiRequest.convert(internalRequest);
                    HttpApiResponse httpApiResponse =
                            this.httpApi.validateRequestSyntax(
                                    myRequest, ThingifierHttpApi.HttpVerb.POST);

                    // TODO: this should be simpler to use by apps building on thingifier
                    if (httpApiResponse == null) {

                        ApiResponse response =
                                this.secretNoteStore
                                        .api()
                                        .post(
                                                "secretnote",
                                                new BodyParser(
                                                        myRequest,
                                                        this.secretNoteStore.getThingNames()),
                                                myRequest.getHeaders());
                        if (!response.isErrorResponse()) {

                            EntityInstance returnedInstance = response.getReturnedInstance();
                            final List<String> protectedFieldNames =
                                    returnedInstance
                                            .getEntity()
                                            .getFieldNamesOfType(
                                                    FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
                            ValidationReport validity =
                                    stateValidator.validateFields(
                                            returnedInstance, protectedFieldNames, false);
                            validity.combine(
                                    secretNoteStore
                                            .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                                            .relationships()
                                            .validate(returnedInstance));

                            this.secretNoteStore.deleteThing(
                                    response.getReturnedInstance(),
                                    EntityRelModel.DEFAULT_DATABASE_NAME);

                            if (!validity.isValid()) {
                                response =
                                        ApiResponse.error(400, validity.getCombinedErrorMessages());
                            } else {
                                final EntityInstance postedThing = response.getReturnedInstance();
                                response = ApiResponse.success().returnSingleInstance(postedThing);
                                challenger.setNote(
                                        response.getReturnedInstance()
                                                .getFieldValue("note")
                                                .asString());
                            }
                        }

                        httpApiResponse =
                                new HttpApiResponse(
                                        myRequest.getHeaders(),
                                        response,
                                        jsonThing,
                                        this.secretNoteStore.apiConfig());
                    }

                    return InternalHttpResponseToHttpServer.convert(
                            HttpApiResponseToInternalHttpResponse.convert(httpApiResponse), result);
                });

        SimpleHttpRouteCreator.routeStatusWhenNot(
                405, secretNotePath, List.of("get", "post", "head", "options"));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.POST,
                                secretNotePath,
                                RoutingStatus.returnedFromCall(),
                                null)
                        .addDocumentation(
                                "POST %s with X-AUTH-TOKEN, and a payload of `{'note':'contents of note'}` to amend the contents of the secret note."
                                        .formatted(secretNotePath))
                        .addPossibleStatuses(200, 400, 401, 403)
                        .addCustomHeader("X-AUTH-TOKEN", "string"));
    }

    private boolean isLiveWidgetRequest(final HttpServerRequest request) {
        return "true".equalsIgnoreCase(request.header(LIVE_WIDGET_HEADER));
    }

    private boolean hasXChallengerHeader(final HttpServerRequest request) {
        final String challenger = request.header("X-CHALLENGER");
        return challenger != null && !challenger.trim().isEmpty();
    }

    private boolean requestHasValidBasicAuth(
            final HttpServerRequest request, final HttpServerResponse result) {

        BasicAuthHeaderParser basicAuth =
                new BasicAuthHeaderParser(request.header("Authorization"));

        // admin/password as default username:password
        if (!basicAuth.matches("admin", "password")) {
            if (!isLiveWidgetRequest(request)) {
                result.header(WWW_AUTHENTICATE_HEADER, "Basic realm=\"User Visible Realm\"");
            }
            result.status(401);
            return false;
        }

        return true;
    }

    private String authTokenFromRequest(final HttpServerRequest request) {
        String authToken = request.header("X-AUTH-TOKEN");
        final String authorization = request.header("Authorization");

        // authorization bearer token will take precedence over X-AUTH-HEADER
        if (authorization != null && !authorization.isEmpty()) {
            final BearerAuthHeaderParser bearerToken = new BearerAuthHeaderParser(authorization);
            if (bearerToken.isBearerToken() && bearerToken.isValid()) {
                authToken = bearerToken.getToken();
            }
        }

        return authToken;
    }

    private String getReadOnlySecretNote(
            final HttpServerRequest request,
            final HttpServerResponse result,
            final String authToken) {

        if (authToken == null || authToken.isEmpty()) {
            return unauthorizedSecretNote(result);
        }

        if (!READ_ONLY_AUTH_TOKEN.contentEquals(authToken)) {
            result.status(403);
            return "";
        }

        AcceptHeaderParser acceptHeaderParser = new AcceptHeaderParser(request.header("ACCEPT"));
        if (!acceptHeaderParser.missingAcceptHeader() && !acceptHeaderParser.isSupportedHeader()) {
            result.status(406);
            return "";
        }

        return renderSecretNoteResponse(request, result, READ_ONLY_SECRET_NOTE);
    }

    private String unauthorizedSecretNote(final HttpServerResponse result) {
        result.header(WWW_AUTHENTICATE_HEADER, BEARER_CHALLENGE);
        result.status(401);
        return "";
    }

    private String renderSecretNoteResponse(
            final HttpServerRequest request, final HttpServerResponse result, final String note) {

        final InternalHttpRequest internalRequest =
                HttpServerRequestToInternalHttpRequest.convert(request);
        final HttpApiRequest myRequest =
                InternalHttpRequestToHttpApiRequest.convert(internalRequest);

        final ApiResponse response =
                ApiResponse.success()
                        .returnSingleDraft(
                                EntityInstanceDraft.forEntity(secretNote).withField("note", note));

        final HttpApiResponse httpApiResponse =
                new HttpApiResponse(
                        myRequest.getHeaders(),
                        response,
                        jsonThing,
                        this.secretNoteStore.apiConfig());

        return InternalHttpResponseToHttpServer.convert(
                HttpApiResponseToInternalHttpResponse.convert(httpApiResponse), result);
    }
}
