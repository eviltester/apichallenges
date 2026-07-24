package uk.co.compendiumdev.challenge.practicemodes.fromhell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FromHellEndpoint {

    private final String method;
    private final String path;
    private final int statusCode;
    private final String label;
    private final String documentation;
    private final String problem;
    private final String expectation;
    private final List<FromHellHeader> headers;
    private final String body;

    public FromHellEndpoint(
            final String method,
            final String path,
            final int statusCode,
            final String label,
            final String documentation,
            final String problem,
            final String expectation,
            final List<FromHellHeader> headers,
            final String body) {
        this.method = method == null ? "" : method.toUpperCase();
        this.path = path == null ? "" : path;
        this.statusCode = statusCode;
        this.label = valueOrEmpty(label);
        this.documentation = valueOrEmpty(documentation);
        this.problem = valueOrEmpty(problem);
        this.expectation = valueOrEmpty(expectation);
        this.headers = Collections.unmodifiableList(new ArrayList<>(headers));
        this.body = valueOrEmpty(body);
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public int statusCode() {
        return statusCode;
    }

    public String label() {
        return label;
    }

    public String documentation() {
        return documentation;
    }

    public String problem() {
        return problem;
    }

    public String expectation() {
        return expectation;
    }

    public List<FromHellHeader> headers() {
        return headers;
    }

    public String body() {
        return body;
    }

    public String contentType() {
        return getHeader("Content-Type");
    }

    public String getHeader(final String headerName) {
        return headers.stream()
                .filter(header -> headerName.equalsIgnoreCase(header.name()))
                .map(FromHellHeader::value)
                .findFirst()
                .orElse("");
    }

    private String valueOrEmpty(final String value) {
        return value == null ? "" : value;
    }
}
