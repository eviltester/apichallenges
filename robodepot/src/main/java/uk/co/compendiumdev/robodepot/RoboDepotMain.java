package uk.co.compendiumdev.robodepot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.MainImplementation;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProviderConfig;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class RoboDepotMain {

    private static final Logger LOG = LoggerFactory.getLogger(RoboDepotMain.class);

    static MainImplementation app;
    static RoboDepotRoutes routes;

    public static void main(final String[] args) {
        LOG.info("Starting RoboDepot");

        app = new MainImplementation();
        ThingStoreProviderConfig repositoryConfig = ThingStoreProviderConfig.fromArgs(args);
        LOG.info("Using Thingifier repository {}", repositoryConfig.describe());

        Thingifier roboDepot =
                new WarehouseRobotThingifier().get(repositoryConfig.createProvider());
        app.registerModel("robodepot", roboDepot);
        app.setDefaultModelName("robodepot");
        app.setDefaultsFromArgs(args);
        app.configurePortAndDefaultRoutes();
        app.setupBuiltInConfigurableRoutes();
        app.chooseThingifier();
        app.configureThingifierWithProfile();

        DefaultGUIHTML gui = app.getGuiManagement();
        configureGui(gui);
        configureDocumentation(app.getApiDefn(), roboDepot);

        String bugMode = RoboDepotBugMode.fromArgs(args).argumentValue();
        routes = new RoboDepotRoutes(roboDepot, gui, bugMode);
        routes.configureSupportRoutes();

        ThingifierHttpApiRoutings restServer = app.startRestServer();
        routes.addHooks(restServer);
        app.addBuiltInArgConfiguredHooks();
    }

    public static void stop() {
        if (app != null) {
            app.close();
        }
        app = null;
        routes = null;
    }

    private static void configureGui(final DefaultGUIHTML gui) {
        gui.appendToCustomHeadContent(
                """
                <link rel="icon" href="data:,">
                <link rel="stylesheet" href="/css/robodepot.css">
                """);
        gui.setActualMenuHtml(
                """
                <nav class="robodepot-nav" aria-label="RoboDepot menu">
                  <a href="/">RoboDepot</a>
                  <a href="/robodepot/docs">API Docs</a>
                  <a href="/robodepot/docs/swagger-ui">Swagger UI</a>
                  <a href="/robodepot/gui/entities">Data Explorer</a>
                  <a href="/robodepot/docs/openapi.json">OpenAPI JSON</a>
                </nav>
                """);
        gui.setFooter(
                """
                <footer class="robodepot-footer">
                  <p>RoboDepot standalone API. Public, in-memory, deliberately buggy by default.</p>
                </footer>
                """);
    }

    private static void configureDocumentation(
            final ThingifierApiDocumentationDefn apiDocDefn, final Thingifier roboDepot) {
        apiDocDefn.setThingifier(roboDepot);
        apiDocDefn.setTitle("RoboDepot");
        apiDocDefn.setDescription("A constrained warehouse robot API for API testing practice.");
        apiDocDefn.setVersion("1.0.0");
        apiDocDefn.setPathPrefix("/robodepot");
        apiDocDefn.setSeoTitle("RoboDepot API Documentation");
        apiDocDefn.setSwaggerUiTitle("RoboDepot API - Swagger UI");
        apiDocDefn.setSeoDescription(
                "RoboDepot endpoint documentation for constrained warehouse robot API testing.");
        apiDocDefn.setMetaRobots("noindex,follow");
        apiDocDefn.setOgType("website");
        apiDocDefn.setTwitterCard("summary_large_image");
        apiDocDefn.addServer("http://localhost:4567", "local execution");
    }
}
