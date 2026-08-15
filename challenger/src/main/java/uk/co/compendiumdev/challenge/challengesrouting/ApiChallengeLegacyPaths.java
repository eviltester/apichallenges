package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.challengehooks.ChallengerApiRequestHook;
import uk.co.compendiumdev.challenge.challengehooks.ChallengerApiResponseHook;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierAutoDocGenRouting;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public final class ApiChallengeLegacyPaths {

    private final Challengers challengers;
    private final boolean singlePlayerMode;
    private final Thingifier thingifier;
    private final PersistenceLayer persistenceLayer;
    private final ChallengeDefinitions challengeDefinitions;
    private final DefaultGUIHTML guiTemplates;

    public ApiChallengeLegacyPaths(
            final Challengers challengers,
            final boolean singlePlayerMode,
            final Thingifier thingifier,
            final PersistenceLayer persistenceLayer,
            final ChallengeDefinitions challengeDefinitions,
            final DefaultGUIHTML guiTemplates) {
        this.challengers = challengers;
        this.singlePlayerMode = singlePlayerMode;
        this.thingifier = thingifier;
        this.persistenceLayer = persistenceLayer;
        this.challengeDefinitions = challengeDefinitions;
        this.guiTemplates = guiTemplates;
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

        new ThingifierAutoDocGenRouting(thingifier, legacyApiDefn, guiTemplates);

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
}
