package uk.co.compendiumdev.challenge.gui;

import java.util.List;

final class BreadcrumbHtml {

    private BreadcrumbHtml() {}

    static Item link(final String label, final String href) {
        return new Item(label, href);
    }

    static Item current(final String label) {
        return new Item(label, "");
    }

    static String render(final List<Item> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<nav class=\"breadcrumb\" aria-label=\"Breadcrumb\">\n");
        html.append("<ol>\n");

        for (int index = 0; index < items.size(); index++) {
            final Item item = items.get(index);
            final boolean currentPage = index == items.size() - 1;

            html.append("<li");
            if (currentPage) {
                html.append(" aria-current=\"page\"");
            }
            html.append(">");

            if (!currentPage && item.href() != null && !item.href().isBlank()) {
                html.append("<a href=\"")
                        .append(escapeHtml(item.href()))
                        .append("\">")
                        .append(escapeHtml(item.label()))
                        .append("</a>");
            } else {
                html.append(escapeHtml(item.label()));
            }

            html.append("</li>\n");
        }

        html.append("</ol>\n");
        html.append("</nav>\n\n");
        return html.toString();
    }

    private static String escapeHtml(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    record Item(String label, String href) {}
}
