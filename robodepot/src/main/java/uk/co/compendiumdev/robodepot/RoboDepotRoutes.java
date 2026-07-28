package uk.co.compendiumdev.robodepot;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.post;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class RoboDepotRoutes {

    private final DefaultGUIHTML guiTemplates;
    private final String bugMode;
    private final RoboDepotSimulation simulation;
    private final Thingifier roboDepot;

    public RoboDepotRoutes(
            final Thingifier roboDepot, final DefaultGUIHTML guiTemplates, final String bugMode) {
        this.roboDepot = roboDepot;
        this.guiTemplates = guiTemplates;
        this.bugMode = bugMode;
        this.simulation = new RoboDepotSimulation(roboDepot, bugMode);
    }

    public void configureSupportRoutes() {
        new RoboDepotHomePage(guiTemplates).configureRoutes();
        new RoboDepotGuiRoutings(roboDepot, guiTemplates).configureRoutes("/robodepot/gui");
        configureHiddenSimulationRoutes();
    }

    public void addHooks(final ThingifierHttpApiRoutings roboDepotHttpRouting) {
        RoboDepotHookConfigurator.registerSafetyAndBugHooks(
                roboDepotHttpRouting, roboDepot, bugMode);
        roboDepotHttpRouting.registerInternalHttpResponseHook(
                new RoboDepotCorsHeadersResponseHook());
    }

    private void configureHiddenSimulationRoutes() {
        post(
                "/robodepot/tick-forward",
                (request, response) -> {
                    response.type("application/json");

                    if (!request.queryParamNames().isEmpty()) {
                        response.status(400);
                        return "{\"errorMessages\":[\"tick-forward does not accept query parameters\"]}";
                    }

                    if (request.body() != null && !request.body().trim().isEmpty()) {
                        response.status(400);
                        return "{\"errorMessages\":[\"tick-forward does not accept a request body\"]}";
                    }

                    RoboDepotTickResult result = simulation.tick(headersFrom(request));
                    response.status(result.statusCode());
                    if (result.isRateLimited()) {
                        response.header("Retry-After", String.valueOf(result.retryAfterSeconds()));
                    }
                    return result.asJson();
                });
    }

    private HttpHeadersBlock headersFrom(
            final uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest request) {
        HttpHeadersBlock headers = new HttpHeadersBlock();
        for (String name : request.headerNames()) {
            headers.put(name, request.header(name));
        }
        return headers;
    }
}
