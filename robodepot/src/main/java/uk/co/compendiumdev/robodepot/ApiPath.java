package uk.co.compendiumdev.robodepot;

import java.util.ArrayList;
import java.util.List;

final class ApiPath {

    private ApiPath() {}

    static List<String> segments(final String path) {
        List<String> segments = new ArrayList<>();
        if (path == null || path.trim().isEmpty()) {
            return segments;
        }

        for (String segment : path.split("/")) {
            if (!segment.trim().isEmpty()) {
                segments.add(segment.trim());
            }
        }
        return segments;
    }
}
