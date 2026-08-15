package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;
import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.head;

import uk.co.compendiumdev.challenge.challengehooks.ChallengerApiRequestHook;
import uk.co.compendiumdev.challenge.challengehooks.ChallengerApiResponseHook;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;

public final class ApiChallengeLegacyPaths {

    private static final int MOVED_PERMANENTLY = 301;

    private final Challengers challengers;
    private final boolean singlePlayerMode;
    private final Thingifier thingifier;
    private final PersistenceLayer persistenceLayer;
    private final ChallengeDefinitions challengeDefinitions;

    public ApiChallengeLegacyPaths(
            final Challengers challengers,
            final boolean singlePlayerMode,
            final Thingifier thingifier,
            final PersistenceLayer persistenceLayer,
            final ChallengeDefinitions challengeDefinitions) {
        this.challengers = challengers;
        this.singlePlayerMode = singlePlayerMode;
        this.thingifier = thingifier;
        this.persistenceLayer = persistenceLayer;
        this.challengeDefinitions = challengeDefinitions;
    }

    public void configure() {
        final ThingifierApiDocumentationDefn legacyApiDefn = legacyDocumentation();

        new ChallengerTrackingRoutes()
                .configure(
                        challengers,
                        singlePlayerMode,
                        legacyApiDefn,
                        persistenceLayer,
                        thingifier,
                        challengeDefinitions,
                        "");
        new ChallengesRoutes()
                .configure(challengers, singlePlayerMode, legacyApiDefn, challengeDefinitions, "");
        new HeartBeatRoutes().configure(legacyApiDefn, "");
        new TodoExportRoutes().configure(thingifier, legacyApiDefn, "");
        new AuthRoutes().configure(challengers, legacyApiDefn, "");

        configureLegacyDocumentationRedirects();

        final ApiChallengeCanonicalThingifierRoutes legacyTodoRoutes =
                new ApiChallengeCanonicalThingifierRoutes(thingifier, "", "").configure();
        legacyTodoRoutes.registerHttpApiRequestHook(new ChallengerApiRequestHook(challengers));
        legacyTodoRoutes.registerHttpApiResponseHook(
                new ChallengerApiResponseHook(challengers, thingifier));
    }

    private ThingifierApiDocumentationDefn legacyDocumentation() {
        final ThingifierApiDocumentationDefn legacyApiDefn = new ThingifierApiDocumentationDefn();
        legacyApiDefn.setThingifier(thingifier);
        legacyApiDefn.setPathPrefix("");
        legacyApiDefn.setSeoTitle("API Challenges API Documentation | API Challenges");
        legacyApiDefn.setSwaggerUiTitle("API Challenges - Swagger UI");
        legacyApiDefn.setSeoDescription(
                "Explore API Challenges endpoint documentation with request formats, payload examples, and expected responses for practical API testing.");
        legacyApiDefn.setMetaRobots("index,follow");
        legacyApiDefn.setOgType("website");
        legacyApiDefn.setTwitterCard("summary_large_image");
        legacyApiDefn.addServer("https://apichallenges.com", "cloud hosted version");
        legacyApiDefn.addServer("http://localhost:4567", "local execution");
        legacyApiDefn.setVersion("1.0.0");
        return legacyApiDefn;
    }

    private void configureLegacyDocumentationRedirects() {
        get("/docs", this::redirectToCanonicalDocumentation);
        get("/docs/*", this::redirectToCanonicalDocumentation);
        head("/docs", this::redirectToCanonicalDocumentation);
        head("/docs/*", this::redirectToCanonicalDocumentation);
    }

    private String redirectToCanonicalDocumentation(
            final HttpServerRequest request, final HttpServerResponse response) {
        response.redirect(canonicalDocumentationPath(request), MOVED_PERMANENTLY);
        return "";
    }

    private String canonicalDocumentationPath(final HttpServerRequest request) {
        final String query = request.queryString();
        final String querySuffix = query == null || query.isBlank() ? "" : "?" + query;
        return "/api" + request.path() + querySuffix;
    }
}
