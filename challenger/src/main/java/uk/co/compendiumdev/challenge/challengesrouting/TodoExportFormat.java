package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;

public enum TodoExportFormat {
    JSON(AcceptHeaderParser.ACCEPT_TYPE.JSON, "json", "json"),
    XML(AcceptHeaderParser.ACCEPT_TYPE.XML, "xml", "xml"),
    TEXT_XML(AcceptHeaderParser.ACCEPT_TYPE.TEXT_XML, "xml", "text-xml", "txml"),
    CSV(AcceptHeaderParser.ACCEPT_TYPE.CSV, "csv", "csv"),
    TEXT(AcceptHeaderParser.ACCEPT_TYPE.TEXT, "txt", "text", "txt", "plain"),
    HTML(AcceptHeaderParser.ACCEPT_TYPE.HTML, "html", "html"),
    NDJSON(AcceptHeaderParser.ACCEPT_TYPE.NDJSON, "ndjson", "ndjson"),
    JSONL(AcceptHeaderParser.ACCEPT_TYPE.JSONL, "jsonl", "jsonl"),
    JSON_SEQ(AcceptHeaderParser.ACCEPT_TYPE.JSON_SEQ, "json-seq", "json-seq", "jsonseq"),
    TSV(
            AcceptHeaderParser.ACCEPT_TYPE.TSV,
            "tsv",
            "tsv",
            "tab",
            "tabs",
            "tab-delimited",
            "tab-separated");

    private final AcceptHeaderParser.ACCEPT_TYPE acceptType;
    private final String fileExtension;
    private final List<String> shortNames;

    TodoExportFormat(
            final AcceptHeaderParser.ACCEPT_TYPE acceptType,
            final String fileExtension,
            final String... shortNames) {
        this.acceptType = acceptType;
        this.fileExtension = fileExtension;
        this.shortNames = List.of(shortNames);
    }

    public AcceptHeaderParser.ACCEPT_TYPE acceptType() {
        return acceptType;
    }

    public String mediaType() {
        return acceptType.mediaType();
    }

    public String filename() {
        return "todos." + fileExtension;
    }

    public String contentDisposition() {
        return "attachment; filename=\"" + filename() + "\"";
    }

    public List<String> shortNames() {
        return shortNames;
    }

    public static Optional<TodoExportFormat> fromShortName(final String rawFormat) {
        if (rawFormat == null || rawFormat.trim().isEmpty()) {
            return Optional.empty();
        }

        String format = rawFormat.trim().toLowerCase(Locale.ROOT);
        for (TodoExportFormat exportFormat : values()) {
            if (exportFormat.shortNames.contains(format)) {
                return Optional.of(exportFormat);
            }
        }
        return Optional.empty();
    }

    public static String supportedShortNames() {
        List<String> names = new ArrayList<>();
        for (TodoExportFormat exportFormat : values()) {
            names.add(exportFormat.shortNames.get(0));
        }
        return String.join(", ", names);
    }
}
