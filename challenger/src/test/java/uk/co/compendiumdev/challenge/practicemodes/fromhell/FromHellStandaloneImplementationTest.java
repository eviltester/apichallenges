package uk.co.compendiumdev.challenge.practicemodes.fromhell;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FromHellStandaloneImplementationTest {

    @Test
    public void apiChallengesPackagesTheSharedCatalog() throws IOException {
        final String sharedCatalog =
                Files.readString(
                        apiFromHellRoot().resolve("catalog").resolve("fromhell-catalog.json"),
                        StandardCharsets.UTF_8);

        final InputStream packagedCatalog =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(FromHellCatalog.RESOURCE_PATH);

        Assertions.assertNotNull(packagedCatalog);
        Assertions.assertEquals(
                sharedCatalog, new String(packagedCatalog.readAllBytes(), StandardCharsets.UTF_8));
        Assertions.assertEquals(77, FromHellCatalog.loadDefault().endpoints().size());
    }

    @Test
    public void standaloneImplementationFoldersAreCatalogDriven() {
        final Path root = apiFromHellRoot();

        Assertions.assertTrue(
                root.resolve("implementations/node-native-http/server.js").toFile().isFile());
        Assertions.assertTrue(
                root.resolve("implementations/node-express/server.js").toFile().isFile());
        Assertions.assertTrue(
                root.resolve("implementations/python-native-http/server.py").toFile().isFile());
        Assertions.assertTrue(
                root.resolve("implementations/python-flask/server.py").toFile().isFile());
        Assertions.assertTrue(
                root.resolve("implementations/java-javalin/pom.xml").toFile().isFile());
        Assertions.assertTrue(
                root.resolve("implementations/mockoon/fromhell.generated.json").toFile().isFile());

        Assertions.assertTrue(
                fileContains(
                        root.resolve("implementations/node-native-http/server.js"),
                        "fromhell-catalog.json"));
        Assertions.assertTrue(
                fileContains(
                        root.resolve("implementations/node-express/server.js"),
                        "FROMHELL_CATALOG"));
        Assertions.assertTrue(
                fileContains(
                        root.resolve("implementations/python-native-http/server.py"),
                        "FROMHELL_CATALOG"));
        Assertions.assertTrue(
                fileContains(
                        root.resolve("implementations/python-flask/server.py"),
                        "FROMHELL_CATALOG"));
        Assertions.assertTrue(
                fileContains(
                        root.resolve(
                                "implementations/java-javalin/src/main/java/dev/eviltester/fromhell/ApiFromHellJavalinMain.java"),
                        "FROMHELL_CATALOG"));
    }

    @Test
    public void sharedToolingIsPresent() {
        final Path root = apiFromHellRoot();

        Assertions.assertTrue(
                root.resolve("catalog/fromhell-catalog.schema.json").toFile().isFile());
        Assertions.assertTrue(root.resolve("tooling/fromhelllib.py").toFile().isFile());
        Assertions.assertTrue(
                root.resolve("tooling/catalog-validator/validate_catalog.py").toFile().isFile());
        Assertions.assertTrue(
                root.resolve("tooling/openapi-generator/generate_openapi.py").toFile().isFile());
        Assertions.assertTrue(
                root.resolve("tooling/mockoon-generator/generate_mockoon.py").toFile().isFile());
        Assertions.assertTrue(
                root.resolve("tooling/conformance-tests/run_conformance.py").toFile().isFile());
    }

    private static boolean fileContains(final Path path, final String expected) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8).contains(expected);
        } catch (IOException e) {
            throw new AssertionError("Could not read " + path, e);
        }
    }

    private static Path apiFromHellRoot() {
        final Path fromRepositoryRoot = Path.of("api-from-hell");
        if (Files.exists(fromRepositoryRoot)) {
            return fromRepositoryRoot;
        }
        return Path.of("..", "api-from-hell");
    }
}
