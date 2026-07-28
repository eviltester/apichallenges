package uk.co.compendiumdev.robodepot;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;

import java.util.HashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGuiHtmlPages;

public class RoboDepotGuiRoutings {

    private final DefaultGUIHTML templates;
    private final Thingifier thingifier;
    private DefaultGuiHtmlPages htmlPages;

    public RoboDepotGuiRoutings(final Thingifier thingifier, final DefaultGUIHTML defaultGui) {
        this.templates = defaultGui;
        this.thingifier = thingifier;
    }

    public RoboDepotGuiRoutings configureRoutes(final String urlPrefixPath) {
        this.htmlPages = new DefaultGuiHtmlPages(templates, thingifier, urlPrefixPath);

        get(
                "%s".formatted(urlPrefixPath),
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);

                    return htmlPages.getHomePageHtml("GUI", "", urlPrefixPath);
                });

        get(
                "%s/entities".formatted(urlPrefixPath),
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);

                    return htmlPages.getEntitiesListPage(EntityRelModel.DEFAULT_DATABASE_NAME);
                });

        get(
                "%s/instances".formatted(urlPrefixPath),
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);

                    String entityName = request.queryParam("entity");

                    return htmlPages.getInstancesListPage(
                            EntityRelModel.DEFAULT_DATABASE_NAME, entityName);
                });

        get(
                "%s/instance".formatted(urlPrefixPath),
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);

                    String entityName = "";
                    for (String queryParam : request.queryParamNames()) {
                        if (queryParam.contentEquals("entity")) {
                            entityName = request.queryParam("entity");
                        }
                    }

                    Map<String, String> instanceQueryParams = new HashMap<>();

                    for (String queryParam : request.queryParamNames()) {
                        if (!queryParam.equals("entity") && !queryParam.equals("database")) {
                            instanceQueryParams.put(queryParam, request.queryParam(queryParam));
                        }
                    }

                    return htmlPages.getInstanceDetailsPage(
                            EntityRelModel.DEFAULT_DATABASE_NAME, entityName, instanceQueryParams);
                });

        return this;
    }
}
