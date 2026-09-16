package uk.co.compendiumdev.challenge.challengesrouting;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

class TodoExportDocumentationTest {

    @Test
    void documentsFormatAsQueryParameter() {
        final Thingifier thingifier = new Thingifier();
        final ThingifierApiDocumentationDefn apiDefn =
                new ThingifierApiDocumentationDefn().setThingifier(thingifier);

        new TodoExportRoutes().configure(thingifier, apiDefn, "/api");

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();
        final Operation get = openApi.getPaths().get("/api/todos/export").getGet();
        final Parameter format = operationParameter(get, "format");

        Assertions.assertEquals("query", format.getIn());
        Assertions.assertFalse(Boolean.TRUE.equals(format.getRequired()));
        Assertions.assertEquals("string", format.getSchema().getType());
    }

    @Test
    void documentsExportResponseContentByFormat() {
        final Thingifier thingifier = new Thingifier();
        final ThingifierApiDocumentationDefn apiDefn =
                new ThingifierApiDocumentationDefn().setThingifier(thingifier);

        new TodoExportRoutes().configure(thingifier, apiDefn, "/api");

        final OpenAPI openApi = new Swaggerizer(apiDefn).swagger();
        final Operation get = openApi.getPaths().get("/api/todos/export").getGet();
        final Content successContent = get.getResponses().get("200").getContent();

        for (TodoExportFormat format : TodoExportFormat.values()) {
            Assertions.assertTrue(successContent.containsKey(format.mediaType()));
        }
        Assertions.assertTrue(
                successContent
                        .get(TodoExportFormat.JSON.mediaType())
                        .getSchema()
                        .getProperties()
                        .containsKey("todos"));
        Assertions.assertEquals(
                "string",
                successContent.get(TodoExportFormat.CSV.mediaType()).getSchema().getType());
        Assertions.assertTrue(
                get.getResponses()
                        .get("400")
                        .getContent()
                        .get("application/json")
                        .getSchema()
                        .getProperties()
                        .containsKey("errorMessages"));
    }

    private Parameter operationParameter(final Operation operation, final String parameterName) {
        return operation.getParameters().stream()
                .filter(parameter -> parameterName.equals(parameter.getName()))
                .findFirst()
                .orElseThrow(
                        () -> new AssertionError("OpenAPI parameter not found: " + parameterName));
    }
}
