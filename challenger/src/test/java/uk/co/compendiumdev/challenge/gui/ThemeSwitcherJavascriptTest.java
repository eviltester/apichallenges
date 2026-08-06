package uk.co.compendiumdev.challenge.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThemeSwitcherJavascriptTest {

    @Test
    void persistsOpenSideTocSectionsInSessionStorage() throws IOException {
        String javascript = themeSwitcherJavascript();

        Assertions.assertTrue(
                javascript.contains(
                        "const sideTocOpenSectionsKey = \"apichallenges-side-toc-open-sections\""));
        Assertions.assertTrue(javascript.contains("window.sessionStorage.getItem"));
        Assertions.assertTrue(javascript.contains("window.sessionStorage.setItem"));
        Assertions.assertTrue(javascript.contains(".side-toc-section[data-side-toc-section]"));
        Assertions.assertTrue(javascript.contains("section.dataset.sideTocSection"));
        Assertions.assertTrue(javascript.contains("JSON.stringify(openSections)"));
        Assertions.assertTrue(
                javascript.contains(
                        "section.addEventListener(\"toggle\", () => saveSideTocOpenSections"));
        Assertions.assertTrue(javascript.contains("restoreSideTocOpenSections();"));
    }

    @Test
    void learningPageForcesSideTocOpenWithoutPersistingState() throws IOException {
        String javascript = themeSwitcherJavascript();

        Assertions.assertTrue(javascript.contains("function shouldForceOpenSideTocSections()"));
        Assertions.assertTrue(javascript.contains("path === \"/learning\" || path === \"/learning/\""));
        Assertions.assertTrue(javascript.contains("if (shouldForceOpenSideTocSections())"));
        Assertions.assertTrue(javascript.contains("section.open = true;"));
        Assertions.assertTrue(
                javascript.contains(
                        "section.addEventListener(\"toggle\", () => saveSideTocOpenSections"));

        final int forceOpenBranch = javascript.indexOf("if (shouldForceOpenSideTocSections())");
        final int returnFromForceOpenBranch = javascript.indexOf("return;", forceOpenBranch);
        final int togglePersistenceListener =
                javascript.indexOf("section.addEventListener(\"toggle\"", forceOpenBranch);
        Assertions.assertTrue(forceOpenBranch > -1);
        Assertions.assertTrue(returnFromForceOpenBranch > forceOpenBranch);
        Assertions.assertTrue(togglePersistenceListener > returnFromForceOpenBranch);
    }

    private String themeSwitcherJavascript() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/public/js/theme-switcher.js")) {
            Assertions.assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
