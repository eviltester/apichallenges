package uk.co.compendiumdev.challenge.challengesrouting;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.SimpleHttpRouteCreator;
import uk.co.compendiumdev.thingifier.adapter.httpserver.routehandlers.HttpApiRequestResponseHandler;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.RestApiGetHandler;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public class TodoExportRoutes {

    public void configure(
            final Thingifier thingifier, final ThingifierApiDocumentationDefn apiDefn) {
        configure(thingifier, apiDefn, "");
    }

    public void configure(
            final Thingifier thingifier,
            final ThingifierApiDocumentationDefn apiDefn,
            final String pathPrefix) {

        final String endpoint = ApiChallengeRoutePath.withPrefix(pathPrefix, "/todos/export");

        get(
                endpoint,
                (request, result) ->
                        new HttpApiRequestResponseHandler(request, result, thingifier)
                                .validateRequestSyntax(false)
                                .usingHandler(
                                        apiRequest ->
                                                exportTodos(
                                                        apiRequest,
                                                        request.queryParam("format"),
                                                        thingifier))
                                .handle());

        SimpleHttpRouteCreator.addHandler(
                endpoint,
                "options",
                (request, result) -> {
                    result.status(204);
                    result.header("Allow", "GET, OPTIONS");
                    return "";
                });

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.GET, endpoint, RoutingStatus.returnedFromCall(), null)
                        .addDocumentation(
                                "Export todos using a format query parameter. Supported values are: "
                                        + TodoExportFormat.supportedShortNames())
                        .addRequestUrlParam(
                                Field.is("format", STRING)
                                        .withDescription(
                                                "Export format short name, e.g. csv, html, tsv,"
                                                        + " json, xml, text-xml, text, ndjson,"
                                                        + " jsonl, or json-seq"))
                        .addResponseHeader(
                                "Content-Disposition", "attachment; filename=\"todos.{extension}\"")
                        .addPossibleStatuses(200, 400, 431));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                                RoutingVerb.OPTIONS, endpoint, RoutingStatus.returnValue(204), null)
                        .addDocumentation("CORS preflight options for exporting todos")
                        .addResponseHeader("Allow", "GET, OPTIONS")
                        .addPossibleStatuses(204, 431));

        SimpleHttpRouteCreator.routeStatusWhenNot(405, endpoint, List.of("get", "options"));
    }

    private ApiResponse exportTodos(
            final HttpApiRequest request, final String rawFormat, final Thingifier thingifier) {

        Optional<TodoExportFormat> maybeFormat = TodoExportFormat.fromShortName(rawFormat);
        if (maybeFormat.isEmpty()) {
            return ApiResponse.error(
                    400,
                    "Unsupported export format. Use one of: "
                            + TodoExportFormat.supportedShortNames());
        }

        TodoExportFormat format = maybeFormat.get();
        request.addHeader("Accept", format.mediaType());

        ApiResponse response =
                new RestApiGetHandler(thingifier)
                        .handle("todos", new QueryFilterParams(), request.getHeaders());

        if (response.getStatusCode() == 200) {
            response.setHeader("Content-Disposition", format.contentDisposition());
        }

        return response;
    }
}
