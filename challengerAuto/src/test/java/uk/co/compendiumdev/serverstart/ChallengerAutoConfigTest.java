package uk.co.compendiumdev.serverstart;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChallengerAutoConfigTest {

    @Test
    void defaultsToOwnedLocalSqliteMemoryMultiplayerOnAutoPort() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(Collections.emptyMap(), Collections.emptyMap());

        Assertions.assertEquals(ChallengerAutoConfig.Target.LOCAL, config.getTarget());
        Assertions.assertEquals(
                ChallengerAutoConfig.Repository.SQLITE_MEMORY, config.getLocalRepository());
        Assertions.assertEquals(ChallengerAutoConfig.PlayerMode.MULTI, config.getLocalPlayerMode());
        Assertions.assertTrue(config.isAutoPort());
        Assertions.assertTrue(config.shouldRunFullSuite());
        Assertions.assertEquals(ChallengerAutoConfig.ApiRouteMode.API, config.getApiRouteMode());
    }

    @Test
    void systemPropertiesOverrideEnvironmentValues() {
        Map<String, String> properties =
                Map.of(
                        ChallengerAutoConfig.PROPERTY_TARGET, "existing",
                        ChallengerAutoConfig.PROPERTY_BASE_URL, "http://localhost:4567");
        Map<String, String> environment =
                Map.of(
                        "CHALLENGER_AUTO_TARGET", "live",
                        "CHALLENGER_AUTO_BASE_URL", "https://example.invalid");

        ChallengerAutoConfig config = ChallengerAutoConfig.from(properties, environment);

        Assertions.assertEquals(ChallengerAutoConfig.Target.EXISTING, config.getTarget());
        Assertions.assertEquals("http://localhost:4567", config.getBaseUrl());
    }

    @Test
    void existingDefaultsToLocalhostWhenBaseUrlNotProvided() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(ChallengerAutoConfig.PROPERTY_TARGET, "existing"),
                        Collections.emptyMap());

        Assertions.assertEquals("http://localhost:4567", config.getBaseUrl());
    }

    @Test
    void liveDefaultsToPublicApiChallengesHost() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(ChallengerAutoConfig.PROPERTY_TARGET, "live"),
                        Collections.emptyMap());

        Assertions.assertEquals("https://apichallenges.eviltester.com", config.getBaseUrl());
        Assertions.assertFalse(config.shouldRunFullSuite());
    }

    @Test
    void liveCanOverrideBaseUrlForRailwayOrOtherRemoteEnvironments() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_TARGET, "live",
                                ChallengerAutoConfig.PROPERTY_BASE_URL, "https://railway.example"),
                        Collections.emptyMap());

        Assertions.assertEquals(ChallengerAutoConfig.Target.LIVE, config.getTarget());
        Assertions.assertEquals("https://railway.example", config.getBaseUrl());
    }

    @Test
    void externalFullOptInAllowsMutatingSuiteForExternalTargets() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_TARGET, "live",
                                ChallengerAutoConfig.PROPERTY_EXTERNAL_FULL, "true"),
                        Collections.emptyMap());

        Assertions.assertTrue(config.shouldRunFullSuite());
    }

    @Test
    void aliasesAreAcceptedForRepositoryAndPlayerMode() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_LOCAL_REPOSITORY, "in_memory",
                                ChallengerAutoConfig.PROPERTY_LOCAL_PLAYER_MODE, "multi-user"),
                        Collections.emptyMap());

        Assertions.assertEquals(
                ChallengerAutoConfig.Repository.MEMORY, config.getLocalRepository());
        Assertions.assertEquals(ChallengerAutoConfig.PlayerMode.MULTI, config.getLocalPlayerMode());
    }

    @Test
    void invalidTargetIsRejected() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        ChallengerAutoConfig.from(
                                Map.of(ChallengerAutoConfig.PROPERTY_TARGET, "somewhere"),
                                Collections.emptyMap()));
    }

    @Test
    void invalidRepositoryIsRejected() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        ChallengerAutoConfig.from(
                                Map.of(ChallengerAutoConfig.PROPERTY_LOCAL_REPOSITORY, "postgres"),
                                Collections.emptyMap()));
    }

    @Test
    void invalidPlayerModeIsRejected() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        ChallengerAutoConfig.from(
                                Map.of(ChallengerAutoConfig.PROPERTY_LOCAL_PLAYER_MODE, "co-op"),
                                Collections.emptyMap()));
    }

    @Test
    void invalidPortIsRejected() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        ChallengerAutoConfig.from(
                                Map.of(ChallengerAutoConfig.PROPERTY_LOCAL_PORT, "not-a-port"),
                                Collections.emptyMap()));
    }

    @Test
    void baseUrlHasTrailingSlashesRemoved() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(
                                ChallengerAutoConfig.PROPERTY_TARGET, "existing",
                                ChallengerAutoConfig.PROPERTY_BASE_URL, "http://localhost:4567///"),
                        Collections.emptyMap());

        Assertions.assertEquals("http://localhost:4567", config.getBaseUrl());
    }

    @Test
    void environmentCanProvideGeneralBaseUrl() {
        Map<String, String> env = new HashMap<>();
        env.put("CHALLENGER_AUTO_TARGET", "existing");
        env.put("CHALLENGER_AUTO_BASE_URL", "http://localhost:5678");

        ChallengerAutoConfig config = ChallengerAutoConfig.from(Collections.emptyMap(), env);

        Assertions.assertEquals(ChallengerAutoConfig.Target.EXISTING, config.getTarget());
        Assertions.assertEquals("http://localhost:5678", config.getBaseUrl());
    }

    @Test
    void apiRouteModePrefixesOnlyApiChallengesPaths() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(ChallengerAutoConfig.PROPERTY_API_ROUTE_MODE, "canonical"),
                        Collections.emptyMap());

        Assertions.assertEquals("/api/todos", config.routePath("/todos"));
        Assertions.assertEquals("/api/todos/1", config.routePath("/todos/1"));
        Assertions.assertEquals(
                "/api/todos?doneStatus=false", config.routePath("/todos?doneStatus=false"));
        Assertions.assertEquals("/api/challenger", config.routePath("/challenger"));
        Assertions.assertEquals("/api/challenges", config.routePath("/challenges"));
        Assertions.assertEquals("/api/heartbeat", config.routePath("/heartbeat"));
        Assertions.assertEquals("/api/secret/token", config.routePath("/secret/token"));
        Assertions.assertEquals("/api/docs/openapi.json", config.routePath("/docs/openapi.json"));

        Assertions.assertEquals("/sim/entities", config.routePath("/sim/entities"));
        Assertions.assertEquals("/simpleapi/randomisbn", config.routePath("/simpleapi/randomisbn"));
        Assertions.assertEquals("/shop/cart", config.routePath("/shop/cart"));
        Assertions.assertEquals(
                "/fromhell/status-code/200", config.routePath("/fromhell/status-code/200"));
    }

    @Test
    void apiRouteModeDoesNotDoublePrefixCanonicalPaths() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(ChallengerAutoConfig.PROPERTY_API_ROUTE_MODE, "api"),
                        Collections.emptyMap());

        Assertions.assertEquals("/api/todos", config.routePath("/api/todos"));
        Assertions.assertEquals("/api/challenger", config.routePath("/api/challenger"));
    }

    @Test
    void legacyRouteModeLeavesApiChallengesPathsAtRoot() {
        ChallengerAutoConfig config =
                ChallengerAutoConfig.from(
                        Map.of(ChallengerAutoConfig.PROPERTY_API_ROUTE_MODE, "legacy"),
                        Collections.emptyMap());

        Assertions.assertEquals(ChallengerAutoConfig.ApiRouteMode.LEGACY, config.getApiRouteMode());
        Assertions.assertEquals("/todos", config.routePath("/todos"));
        Assertions.assertEquals("/challenger", config.routePath("/challenger"));
        Assertions.assertEquals("/docs/openapi.json", config.routePath("/docs/openapi.json"));
    }

    @Test
    void invalidApiRouteModeIsRejected() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->
                        ChallengerAutoConfig.from(
                                Map.of(ChallengerAutoConfig.PROPERTY_API_ROUTE_MODE, "sideways"),
                                Collections.emptyMap()));
    }
}
