package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;

class TodoExportFormatTest {

    @Test
    void supportsAllThingifierResponseMediaTypes() {
        Set<AcceptHeaderParser.ACCEPT_TYPE> mappedTypes =
                Arrays.stream(TodoExportFormat.values())
                        .map(TodoExportFormat::acceptType)
                        .collect(Collectors.toSet());

        Assertions.assertEquals(
                Set.copyOf(AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes()), mappedTypes);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("formatMappings")
    void mapsShortNamesToMediaTypeFilenameAndContentDisposition(
            final String shortName,
            final String mediaType,
            final String filename,
            final TodoExportFormat expectedFormat) {

        TodoExportFormat format = TodoExportFormat.fromShortName(shortName).orElseThrow();

        Assertions.assertEquals(expectedFormat, format);
        Assertions.assertEquals(mediaType, format.mediaType());
        Assertions.assertEquals(filename, format.filename());
        Assertions.assertEquals(
                "attachment; filename=\"" + filename + "\"", format.contentDisposition());
    }

    @Test
    void unsupportedFormatIsNotMapped() {
        Assertions.assertTrue(TodoExportFormat.fromShortName("pdf").isEmpty());
        Assertions.assertTrue(TodoExportFormat.fromShortName("").isEmpty());
        Assertions.assertTrue(TodoExportFormat.fromShortName(null).isEmpty());
    }

    private static Stream<Arguments> formatMappings() {
        return Stream.of(
                Arguments.of("json", "application/json", "todos.json", TodoExportFormat.JSON),
                Arguments.of("xml", "application/xml", "todos.xml", TodoExportFormat.XML),
                Arguments.of("text-xml", "text/xml", "todos.xml", TodoExportFormat.TEXT_XML),
                Arguments.of("txml", "text/xml", "todos.xml", TodoExportFormat.TEXT_XML),
                Arguments.of("csv", "text/csv", "todos.csv", TodoExportFormat.CSV),
                Arguments.of("text", "text/plain", "todos.txt", TodoExportFormat.TEXT),
                Arguments.of("txt", "text/plain", "todos.txt", TodoExportFormat.TEXT),
                Arguments.of("html", "text/html", "todos.html", TodoExportFormat.HTML),
                Arguments.of(
                        "ndjson", "application/x-ndjson", "todos.ndjson", TodoExportFormat.NDJSON),
                Arguments.of("jsonl", "application/jsonl", "todos.jsonl", TodoExportFormat.JSONL),
                Arguments.of(
                        "json-seq",
                        "application/json-seq",
                        "todos.json-seq",
                        TodoExportFormat.JSON_SEQ),
                Arguments.of(
                        "jsonseq",
                        "application/json-seq",
                        "todos.json-seq",
                        TodoExportFormat.JSON_SEQ),
                Arguments.of("tsv", "text/tab-separated-values", "todos.tsv", TodoExportFormat.TSV),
                Arguments.of(
                        "tab-delimited",
                        "text/tab-separated-values",
                        "todos.tsv",
                        TodoExportFormat.TSV));
    }
}
