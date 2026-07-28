package uk.co.compendiumdev.robodepot;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

class RoboDepotApiTest {

    @Test
    void modelContainsConstrainedEntitiesFieldsCapsAndRelationships() {
        Thingifier thingifier = new WarehouseRobotThingifier().get();

        assertEntity(
                thingifier, "robotmodel", "robotmodels", WarehouseRobotThingifier.MAX_ROBOT_MODELS);
        assertEntity(thingifier, "sku", "skus", WarehouseRobotThingifier.MAX_SKUS);
        assertEntity(thingifier, "zone", "zones", WarehouseRobotThingifier.MAX_ZONES);
        assertEntity(thingifier, "robot", "robots", WarehouseRobotThingifier.MAX_ROBOTS);
        assertEntity(thingifier, "job", "jobs", WarehouseRobotThingifier.MAX_JOBS);
        assertEntity(thingifier, "stock", "stock", WarehouseRobotThingifier.MAX_STOCK);

        for (EntityDefinition entity : thingifier.getERmodel().getEntityDefinitions()) {
            for (String fieldName : entity.getFieldNames()) {
                Assertions.assertNotEquals(FieldType.STRING, entity.getField(fieldName).getType());
                Assertions.assertTrue(
                        entity.getField(fieldName).hasDescription(),
                        entity.getName() + "." + fieldName + " should have a description");
            }
        }
        Assertions.assertFalse(thingifier.apiConfig().supportsMultipleDatabases());
        Assertions.assertEquals("/robodepot", thingifier.apiConfig().getApiEndPointPrefix());

        EntityDefinition robotModel = thingifier.getDefinitionNamed("robotmodel");
        EntityDefinition sku = thingifier.getDefinitionNamed("sku");
        EntityDefinition zone = thingifier.getDefinitionNamed("zone");
        EntityDefinition robot = thingifier.getDefinitionNamed("robot");
        EntityDefinition job = thingifier.getDefinitionNamed("job");
        EntityDefinition stock = thingifier.getDefinitionNamed("stock");

        assertEntityDescription(robotModel, WarehouseRobotThingifier.ROBOT_MODEL_DESCRIPTION);
        assertEntityDescription(sku, WarehouseRobotThingifier.SKU_DESCRIPTION);
        assertEntityDescription(zone, WarehouseRobotThingifier.ZONE_DESCRIPTION);
        assertEntityDescription(robot, WarehouseRobotThingifier.ROBOT_DESCRIPTION);
        assertEntityDescription(job, WarehouseRobotThingifier.JOB_DESCRIPTION);
        assertEntityDescription(stock, WarehouseRobotThingifier.STOCK_DESCRIPTION);

        Assertions.assertNotNull(robotModel.getNamedRelationshipTo("robots", robot));
        Assertions.assertEquals(
                "1", robot.getNamedRelationshipTo("model", robotModel).getCardinality().right());
        Assertions.assertEquals(
                String.valueOf(WarehouseRobotThingifier.MAX_ROBOTS_PER_ZONE),
                zone.getNamedRelationshipTo("robots", robot).getCardinality().right());
        Assertions.assertEquals(
                String.valueOf(WarehouseRobotThingifier.MAX_JOBS_PER_ROBOT),
                robot.getNamedRelationshipTo("jobs", job).getCardinality().right());
        Assertions.assertNotNull(zone.getNamedRelationshipTo("pickupJobs", job));
        Assertions.assertNotNull(zone.getNamedRelationshipTo("dropoffJobs", job));
        Assertions.assertEquals(
                String.valueOf(WarehouseRobotThingifier.MAX_STOCK_PER_ZONE),
                zone.getNamedRelationshipTo("stock", stock).getCardinality().right());
        Assertions.assertNotNull(sku.getNamedRelationshipTo("stock", stock));
    }

    @Test
    void seedDataStaysBelowCapsAndHasValidRelationships() {
        Thingifier thingifier = new WarehouseRobotThingifier().get();
        ThingStore store = thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);

        Assertions.assertEquals(4, count(thingifier, "robotmodel"));
        Assertions.assertEquals(3, count(thingifier, "sku"));
        Assertions.assertEquals(4, count(thingifier, "zone"));
        Assertions.assertEquals(6, count(thingifier, "robot"));
        Assertions.assertEquals(6, count(thingifier, "job"));
        Assertions.assertEquals(7, count(thingifier, "stock"));

        Assertions.assertTrue(
                count(thingifier, "robotmodel") < WarehouseRobotThingifier.MAX_ROBOT_MODELS);
        Assertions.assertTrue(count(thingifier, "sku") < WarehouseRobotThingifier.MAX_SKUS);
        Assertions.assertTrue(count(thingifier, "zone") < WarehouseRobotThingifier.MAX_ZONES);
        Assertions.assertTrue(count(thingifier, "robot") < WarehouseRobotThingifier.MAX_ROBOTS);
        Assertions.assertTrue(count(thingifier, "job") < WarehouseRobotThingifier.MAX_JOBS);
        Assertions.assertTrue(count(thingifier, "stock") < WarehouseRobotThingifier.MAX_STOCK);

        Assertions.assertFalse(
                store.relationships()
                        .listRelated(findById(thingifier, "robotmodel", "1"), "robots")
                        .isEmpty());
        Assertions.assertFalse(
                store.relationships()
                        .listRelated(findById(thingifier, "zone", "1"), "robots")
                        .isEmpty());
        Assertions.assertFalse(
                store.relationships()
                        .listRelated(findById(thingifier, "robot", "1"), "jobs")
                        .isEmpty());
    }

    @Test
    void singleTargetRelationshipReadsReturnSingleObjects() {
        Thingifier thingifier = new WarehouseRobotThingifier().get();
        ThingifierHttpApi api = api(thingifier);

        HttpApiResponse modelResponse = get(api, "/robots/1/model");
        Assertions.assertEquals(200, modelResponse.getStatusCode());
        Assertions.assertFalse(modelResponse.apiResponse().isCollection());
        Assertions.assertTrue(modelResponse.getBody().contains("\"modelCode\""));
        Assertions.assertFalse(modelResponse.getBody().contains("\"robotmodels\""));

        HttpApiResponse robotsResponse = get(api, "/robotmodels/1/robots");
        Assertions.assertEquals(200, robotsResponse.getStatusCode());
        Assertions.assertTrue(robotsResponse.apiResponse().isCollection());
        Assertions.assertTrue(robotsResponse.getBody().contains("\"robots\""));
    }

    @Test
    void mutableEntitiesSupportCrud() {
        Thingifier thingifier = new WarehouseRobotThingifier().get();

        assertCrud(
                thingifier,
                "zones",
                "{\"zoneType\":\"pack\",\"capacity\":9,\"temperatureBand\":\"ambient\",\"closed\":false}",
                "{\"closed\":true}");
        assertCrud(
                thingifier,
                "robots",
                "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":55}",
                "{\"status\":\"charging\"}");
        assertCrud(
                thingifier,
                "jobs",
                "{\"priority\":2,\"state\":\"queued\",\"payloadType\":\"small-bin\",\"createdTick\":400}",
                "{\"state\":\"in-progress\"}");
        assertCrud(
                thingifier,
                "stock",
                "{\"quantity\":4,\"condition\":\"sealed\"}",
                "{\"condition\":\"held\"}");
    }

    @Test
    void safetyRulesRejectUnsafePayloadsAndCatalogWrites() {
        Thingifier thingifier = new WarehouseRobotThingifier().get();
        ThingifierHttpApi api = api(thingifier, new CatalogWriteBlockHook());

        Assertions.assertNotEquals(
                201,
                post(
                                api,
                                "/robots",
                                "{\"color\":\"purple\",\"status\":\"idle\",\"batteryLevel\":55}")
                        .getStatusCode());
        Assertions.assertNotEquals(
                201,
                post(api, "/robots", "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":101}")
                        .getStatusCode());
        Assertions.assertNotEquals(
                201,
                post(
                                api,
                                "/zones",
                                "{\"zoneType\":\"pick\",\"capacity\":5,\"temperatureBand\":\"ambient\",\"closed\":false,\"label\":\"bad\"}")
                        .getStatusCode());

        Assertions.assertEquals(
                413,
                post(api, "/stock", "{\"condition\":\"" + "x".repeat(5000) + "\"}")
                        .getStatusCode());

        Assertions.assertEquals(
                405,
                post(
                                api,
                                "/robotmodels",
                                "{\"modelCode\":\"rb100\",\"payloadClass\":\"standard\",\"driveType\":\"wheeled\",\"maxBatteryHours\":8}")
                        .getStatusCode());

        String robotId =
                create(
                        api,
                        "/robots",
                        "{\"color\":\"blue\",\"status\":\"idle\",\"batteryLevel\":80}");
        Assertions.assertEquals(
                201,
                post(api, "/robotmodels/1/robots", "{\"id\":" + robotId + "}").getStatusCode());
    }

    @Test
    void maxInstanceCapsAreEnforced() {
        Thingifier thingifier = new WarehouseRobotThingifier().get();
        ThingifierHttpApi api = api(thingifier);

        assertCapacity(
                thingifier,
                api,
                "robotmodel",
                "/robotmodels",
                WarehouseRobotThingifier.MAX_ROBOT_MODELS,
                "{\"modelCode\":\"rb100\",\"payloadClass\":\"standard\",\"driveType\":\"wheeled\",\"maxBatteryHours\":8}");
        assertCapacity(
                thingifier,
                api,
                "sku",
                "/skus",
                WarehouseRobotThingifier.MAX_SKUS,
                "{\"skuCode\":\"crate-a\",\"sizeClass\":\"small\",\"fragile\":false,\"storageRequirement\":\"ambient\"}");
        assertCapacity(
                thingifier,
                api,
                "zone",
                "/zones",
                WarehouseRobotThingifier.MAX_ZONES,
                "{\"zoneType\":\"pick\",\"capacity\":5,\"temperatureBand\":\"ambient\",\"closed\":false}");
        assertCapacity(
                thingifier,
                api,
                "robot",
                "/robots",
                WarehouseRobotThingifier.MAX_ROBOTS,
                "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":50}");
        assertCapacity(
                thingifier,
                api,
                "job",
                "/jobs",
                WarehouseRobotThingifier.MAX_JOBS,
                "{\"priority\":2,\"state\":\"queued\",\"payloadType\":\"small-bin\",\"createdTick\":1}");
        assertCapacity(
                thingifier,
                api,
                "stock",
                "/stock",
                WarehouseRobotThingifier.MAX_STOCK,
                "{\"quantity\":1,\"condition\":\"sealed\"}");
    }

    @Test
    void relationshipCapsAreEnforced() {
        Thingifier thingifier = emptyRoboDepot();
        ThingifierHttpApi api = api(thingifier);

        String robotId =
                create(
                        api,
                        "/robots",
                        "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":90}");
        String firstModelId =
                create(
                        api,
                        "/robotmodels",
                        "{\"modelCode\":\"rb100\",\"payloadClass\":\"standard\",\"driveType\":\"wheeled\",\"maxBatteryHours\":8}");
        String secondModelId =
                create(
                        api,
                        "/robotmodels",
                        "{\"modelCode\":\"rb200\",\"payloadClass\":\"standard\",\"driveType\":\"wheeled\",\"maxBatteryHours\":8}");
        Assertions.assertEquals(
                201,
                post(api, "/robotmodels/" + firstModelId + "/robots", "{\"id\":" + robotId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                422,
                post(api, "/robotmodels/" + secondModelId + "/robots", "{\"id\":" + robotId + "}")
                        .getStatusCode());

        String firstZoneId = createZone(api);
        String secondZoneId = createZone(api);
        String secondRobotId =
                create(
                        api,
                        "/robots",
                        "{\"color\":\"blue\",\"status\":\"idle\",\"batteryLevel\":80}");
        Assertions.assertEquals(
                201,
                post(api, "/zones/" + firstZoneId + "/robots", "{\"id\":" + secondRobotId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                422,
                post(api, "/zones/" + secondZoneId + "/robots", "{\"id\":" + secondRobotId + "}")
                        .getStatusCode());

        String stockId = create(api, "/stock", "{\"quantity\":1,\"condition\":\"sealed\"}");
        String firstSkuId =
                create(
                        api,
                        "/skus",
                        "{\"skuCode\":\"crate-a\",\"sizeClass\":\"small\",\"fragile\":false,\"storageRequirement\":\"ambient\"}");
        String secondSkuId =
                create(
                        api,
                        "/skus",
                        "{\"skuCode\":\"crate-b\",\"sizeClass\":\"small\",\"fragile\":false,\"storageRequirement\":\"ambient\"}");
        Assertions.assertEquals(
                201,
                post(api, "/skus/" + firstSkuId + "/stock", "{\"id\":" + stockId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                422,
                post(api, "/skus/" + secondSkuId + "/stock", "{\"id\":" + stockId + "}")
                        .getStatusCode());

        assertRobotJobCap(api, robotId);
        assertZoneStockCap(api, firstZoneId);
    }

    @Test
    void sessionHeadersUseTheDefaultDatabase() {
        Thingifier thingifier = new WarehouseRobotThingifier().get();
        ThingifierHttpApi api = api(thingifier);

        int defaultZoneCount = count(thingifier, "zone");
        Assertions.assertEquals(
                201,
                post(
                                api,
                                "/zones",
                                "{\"zoneType\":\"pack\",\"capacity\":7,\"temperatureBand\":\"ambient\",\"closed\":false}")
                        .getStatusCode());
        Assertions.assertEquals(defaultZoneCount + 1, count(thingifier, "zone"));

        HttpApiResponse headerCreate =
                post(
                        api,
                        request(
                                "/zones",
                                HttpApiRequest.VERB.POST,
                                "{\"zoneType\":\"cold\",\"capacity\":4,\"temperatureBand\":\"chilled\",\"closed\":false}",
                                "alpha"));
        Assertions.assertEquals(201, headerCreate.getStatusCode());
        Assertions.assertEquals(defaultZoneCount + 2, count(thingifier, "zone"));
        Assertions.assertFalse(thingifier.getERmodel().getDatabaseNames().contains("alpha"));

        HttpApiResponse spacedHeader =
                get(api, request("/zones", HttpApiRequest.VERB.GET, null, "bad session"));
        Assertions.assertEquals(200, spacedHeader.getStatusCode());
        Assertions.assertFalse(thingifier.getERmodel().getDatabaseNames().contains("bad session"));
    }

    @Test
    void bugHooksAreOptInAndDeterministic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        ThingifierHttpApi cleanApi =
                api(
                        cleanThingifier,
                        new RoboDepotBusinessRulesHook(cleanThingifier, RoboDepotBugMode.NONE));
        String cleanJobId =
                create(
                        cleanApi,
                        "/jobs",
                        "{\"priority\":2,\"state\":\"queued\",\"payloadType\":\"small-bin\",\"createdTick\":1}");
        Assertions.assertEquals(204, delete(cleanApi, "/jobs/" + cleanJobId).getStatusCode());
        Assertions.assertEquals(0, count(cleanThingifier, "job"));

        Thingifier classicThingifier = emptyRoboDepot();
        ThingifierHttpApi classicApi =
                api(
                        classicThingifier,
                        new RoboDepotBusinessRulesHook(
                                classicThingifier, RoboDepotBugMode.CLASSIC));
        String classicJobId =
                create(
                        classicApi,
                        "/jobs",
                        "{\"priority\":2,\"state\":\"queued\",\"payloadType\":\"small-bin\",\"createdTick\":1}");
        Assertions.assertEquals(204, delete(classicApi, "/jobs/" + classicJobId).getStatusCode());
        Assertions.assertEquals(0, count(classicThingifier, "job"));
    }

    @Test
    void businessRulesAreCleanUnlessClassicBugBundleIsEnabled() {
        assertActiveJobRelationshipDeleteOnlyBlockedInClassic();
        assertHeldStockRelationshipDeleteOnlyBlockedInClassic();
        assertClosedZoneRelationshipsOnlyAllowedInClassic();
        assertFragileFrozenStockOnlyAllowedInClassic();
        assertZoneCapacityOffByOneOnlyHappensInClassic();
        assertValidEnumValuesOnlyRejectedInClassic();
        assertStockPutOnlyIncrementsInClassic();
        assertPayloadMismatchOnlyAllowedInClassic();
    }

    @Test
    void tickForwardAdvancesStateAndRateLimits() {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-27T12:00:00Z"));
        Thingifier thingifier = new WarehouseRobotThingifier().get();
        RoboDepotSimulation simulation =
                new RoboDepotSimulation(thingifier, RoboDepotBugMode.NONE, clock);

        RoboDepotTickResult result = simulation.tick(new HttpHeadersBlock());

        Assertions.assertEquals(200, result.statusCode());
        Assertions.assertTrue(result.asJson().contains("\"tick\":126"));
        Assertions.assertTrue(result.asJson().contains("\"ticksAdvanced\":1"));
        Assertions.assertEquals("completed", field(thingifier, "job", "2", "state"));
        Assertions.assertEquals("idle", field(thingifier, "robot", "2", "status"));
        Assertions.assertEquals("17", field(thingifier, "stock", "3", "quantity"));

        result = simulation.tick(new HttpHeadersBlock());

        Assertions.assertEquals(429, result.statusCode());
        Assertions.assertEquals(20, result.retryAfterSeconds());

        clock.advanceSeconds(RoboDepotSimulation.SECONDS_BETWEEN_TICKS);
        result = simulation.tick(new HttpHeadersBlock());

        Assertions.assertEquals(200, result.statusCode());
        Assertions.assertTrue(result.asJson().contains("\"tick\":127"));
    }

    @Test
    void simulationBugsAreTriggeredByDefaultAndCleanModeUsesExpectedRules() {
        assertPriorityInversionOnlyHappensInClassic();
        assertLowBatteryRobotOnlyWorksInClassic();
        assertChargingRobotOnlyGetsAssignedInClassic();
        assertPayloadMismatchTickOnlyAllowedInClassic();
        assertOfflineRobotOnlyMovesInClassic();
        assertDamagedStockOnlyGetsPickedInClassic();
        assertWrongSkuOnlyGetsAdjustedInClassic();
        assertStockShortageOnlyCompletesInClassic();
        assertRobotStatusOnlyStaysAssignedInClassic();
    }

    private void assertActiveJobRelationshipDeleteOnlyBlockedInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        ThingifierHttpApi cleanApi =
                api(
                        cleanThingifier,
                        new RoboDepotBusinessRulesHook(cleanThingifier, RoboDepotBugMode.NONE));
        String cleanRobotId =
                create(
                        cleanApi,
                        "/robots",
                        "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":90}");
        String cleanJobId =
                create(
                        cleanApi,
                        "/jobs",
                        "{\"priority\":2,\"state\":\"in-progress\",\"payloadType\":\"small-bin\",\"createdTick\":1}");
        Assertions.assertEquals(
                201,
                post(cleanApi, "/robots/" + cleanRobotId + "/jobs", "{\"id\":" + cleanJobId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                204,
                delete(cleanApi, "/robots/" + cleanRobotId + "/jobs/" + cleanJobId)
                        .getStatusCode());

        Thingifier classicThingifier = emptyRoboDepot();
        ThingifierHttpApi classicApi =
                api(
                        classicThingifier,
                        new RoboDepotBusinessRulesHook(
                                classicThingifier, RoboDepotBugMode.CLASSIC));
        String classicRobotId =
                create(
                        classicApi,
                        "/robots",
                        "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":90}");
        String classicJobId =
                create(
                        classicApi,
                        "/jobs",
                        "{\"priority\":2,\"state\":\"in-progress\",\"payloadType\":\"small-bin\",\"createdTick\":1}");
        Assertions.assertEquals(
                201,
                post(
                                classicApi,
                                "/robots/" + classicRobotId + "/jobs",
                                "{\"id\":" + classicJobId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                409,
                delete(classicApi, "/robots/" + classicRobotId + "/jobs/" + classicJobId)
                        .getStatusCode());
    }

    private void assertHeldStockRelationshipDeleteOnlyBlockedInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        ThingifierHttpApi cleanApi =
                api(
                        cleanThingifier,
                        new RoboDepotBusinessRulesHook(cleanThingifier, RoboDepotBugMode.NONE));
        String cleanZoneId = createZone(cleanApi);
        String cleanStockId = create(cleanApi, "/stock", "{\"quantity\":1,\"condition\":\"held\"}");

        Assertions.assertEquals(
                201,
                post(cleanApi, "/zones/" + cleanZoneId + "/stock", "{\"id\":" + cleanStockId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                204,
                delete(cleanApi, "/zones/" + cleanZoneId + "/stock/" + cleanStockId)
                        .getStatusCode());
        Assertions.assertEquals(0, relatedCount(cleanThingifier, "zone", cleanZoneId, "stock"));

        Thingifier classicThingifier = emptyRoboDepot();
        ThingifierHttpApi classicApi =
                api(
                        classicThingifier,
                        new RoboDepotBusinessRulesHook(
                                classicThingifier, RoboDepotBugMode.CLASSIC));
        String zoneId = createZone(classicApi);
        String stockId = create(classicApi, "/stock", "{\"quantity\":1,\"condition\":\"held\"}");

        Assertions.assertEquals(
                201,
                post(classicApi, "/zones/" + zoneId + "/stock", "{\"id\":" + stockId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                409, delete(classicApi, "/zones/" + zoneId + "/stock/" + stockId).getStatusCode());
        Assertions.assertEquals(1, relatedCount(classicThingifier, "zone", zoneId, "stock"));
    }

    private void assertClosedZoneRelationshipsOnlyAllowedInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        ThingifierHttpApi cleanApi =
                api(
                        cleanThingifier,
                        new RoboDepotBusinessRulesHook(cleanThingifier, RoboDepotBugMode.NONE));
        String cleanZoneId =
                create(
                        cleanApi,
                        "/zones",
                        "{\"zoneType\":\"pick\",\"capacity\":2,\"temperatureBand\":\"ambient\",\"closed\":true}");
        String cleanRobotId =
                create(
                        cleanApi,
                        "/robots",
                        "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":90}");
        Assertions.assertEquals(
                409,
                post(cleanApi, "/zones/" + cleanZoneId + "/robots", "{\"id\":" + cleanRobotId + "}")
                        .getStatusCode());
        String cleanStockId =
                create(cleanApi, "/stock", "{\"quantity\":1,\"condition\":\"sealed\"}");
        Assertions.assertEquals(
                409,
                post(cleanApi, "/zones/" + cleanZoneId + "/stock", "{\"id\":" + cleanStockId + "}")
                        .getStatusCode());

        Thingifier classicThingifier = emptyRoboDepot();
        ThingifierHttpApi classicApi =
                api(
                        classicThingifier,
                        new RoboDepotBusinessRulesHook(
                                classicThingifier, RoboDepotBugMode.CLASSIC));
        String classicZoneId =
                create(
                        classicApi,
                        "/zones",
                        "{\"zoneType\":\"pick\",\"capacity\":2,\"temperatureBand\":\"ambient\",\"closed\":true}");
        String classicRobotId =
                create(
                        classicApi,
                        "/robots",
                        "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":90}");
        Assertions.assertEquals(
                201,
                post(
                                classicApi,
                                "/zones/" + classicZoneId + "/robots",
                                "{\"id\":" + classicRobotId + "}")
                        .getStatusCode());
        String classicStockId =
                create(classicApi, "/stock", "{\"quantity\":1,\"condition\":\"sealed\"}");
        Assertions.assertEquals(
                201,
                post(
                                classicApi,
                                "/zones/" + classicZoneId + "/stock",
                                "{\"id\":" + classicStockId + "}")
                        .getStatusCode());
    }

    private void assertFragileFrozenStockOnlyAllowedInClassic() {
        Thingifier cleanZoneFirstThingifier = emptyRoboDepot();
        ThingifierHttpApi cleanZoneFirstApi =
                api(
                        cleanZoneFirstThingifier,
                        new RoboDepotBusinessRulesHook(
                                cleanZoneFirstThingifier, RoboDepotBugMode.NONE));
        String cleanFrozenZoneId = createZone(cleanZoneFirstApi, "cold", 5, "frozen", false);
        String cleanFragileSkuId =
                createSku(cleanZoneFirstApi, "fragile-a", "medium", true, "ambient");
        String cleanStockId =
                create(cleanZoneFirstApi, "/stock", "{\"quantity\":1,\"condition\":\"sealed\"}");
        Assertions.assertEquals(
                201,
                post(
                                cleanZoneFirstApi,
                                "/skus/" + cleanFragileSkuId + "/stock",
                                "{\"id\":" + cleanStockId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                409,
                post(
                                cleanZoneFirstApi,
                                "/zones/" + cleanFrozenZoneId + "/stock",
                                "{\"id\":" + cleanStockId + "}")
                        .getStatusCode());

        Thingifier cleanSkuFirstThingifier = emptyRoboDepot();
        ThingifierHttpApi cleanSkuFirstApi =
                api(
                        cleanSkuFirstThingifier,
                        new RoboDepotBusinessRulesHook(
                                cleanSkuFirstThingifier, RoboDepotBugMode.NONE));
        String cleanPrelinkedZoneId = createZone(cleanSkuFirstApi, "cold", 5, "frozen", false);
        String cleanPrelinkedSkuId =
                createSku(cleanSkuFirstApi, "fragile-a", "medium", true, "ambient");
        String cleanPrelinkedStockId =
                create(cleanSkuFirstApi, "/stock", "{\"quantity\":1,\"condition\":\"sealed\"}");
        Assertions.assertEquals(
                201,
                post(
                                cleanSkuFirstApi,
                                "/zones/" + cleanPrelinkedZoneId + "/stock",
                                "{\"id\":" + cleanPrelinkedStockId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                409,
                post(
                                cleanSkuFirstApi,
                                "/skus/" + cleanPrelinkedSkuId + "/stock",
                                "{\"id\":" + cleanPrelinkedStockId + "}")
                        .getStatusCode());

        Thingifier classicThingifier = emptyRoboDepot();
        ThingifierHttpApi classicApi =
                api(
                        classicThingifier,
                        new RoboDepotBusinessRulesHook(
                                classicThingifier, RoboDepotBugMode.CLASSIC));
        String classicFrozenZoneId = createZone(classicApi, "cold", 5, "frozen", false);
        String classicFragileSkuId = createSku(classicApi, "fragile-a", "medium", true, "ambient");
        String classicStockId =
                create(classicApi, "/stock", "{\"quantity\":1,\"condition\":\"sealed\"}");
        Assertions.assertEquals(
                201,
                post(
                                classicApi,
                                "/skus/" + classicFragileSkuId + "/stock",
                                "{\"id\":" + classicStockId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                201,
                post(
                                classicApi,
                                "/zones/" + classicFrozenZoneId + "/stock",
                                "{\"id\":" + classicStockId + "}")
                        .getStatusCode());
    }

    private void assertZoneCapacityOffByOneOnlyHappensInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        ThingifierHttpApi cleanApi =
                api(
                        cleanThingifier,
                        new RoboDepotBusinessRulesHook(cleanThingifier, RoboDepotBugMode.NONE));
        String cleanZoneId =
                create(
                        cleanApi,
                        "/zones",
                        "{\"zoneType\":\"pick\",\"capacity\":2,\"temperatureBand\":\"ambient\",\"closed\":false}");
        String firstCleanRobotId =
                create(
                        cleanApi,
                        "/robots",
                        "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":90}");
        String secondCleanRobotId =
                create(
                        cleanApi,
                        "/robots",
                        "{\"color\":\"blue\",\"status\":\"idle\",\"batteryLevel\":90}");
        Assertions.assertEquals(
                201,
                post(
                                cleanApi,
                                "/zones/" + cleanZoneId + "/robots",
                                "{\"id\":" + firstCleanRobotId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                201,
                post(
                                cleanApi,
                                "/zones/" + cleanZoneId + "/robots",
                                "{\"id\":" + secondCleanRobotId + "}")
                        .getStatusCode());

        Thingifier classicThingifier = emptyRoboDepot();
        ThingifierHttpApi classicApi =
                api(
                        classicThingifier,
                        new RoboDepotBusinessRulesHook(
                                classicThingifier, RoboDepotBugMode.CLASSIC));
        String classicZoneId =
                create(
                        classicApi,
                        "/zones",
                        "{\"zoneType\":\"pick\",\"capacity\":2,\"temperatureBand\":\"ambient\",\"closed\":false}");
        String firstClassicRobotId =
                create(
                        classicApi,
                        "/robots",
                        "{\"color\":\"red\",\"status\":\"idle\",\"batteryLevel\":90}");
        String secondClassicRobotId =
                create(
                        classicApi,
                        "/robots",
                        "{\"color\":\"blue\",\"status\":\"idle\",\"batteryLevel\":90}");
        Assertions.assertEquals(
                201,
                post(
                                classicApi,
                                "/zones/" + classicZoneId + "/robots",
                                "{\"id\":" + firstClassicRobotId + "}")
                        .getStatusCode());
        Assertions.assertEquals(
                409,
                post(
                                classicApi,
                                "/zones/" + classicZoneId + "/robots",
                                "{\"id\":" + secondClassicRobotId + "}")
                        .getStatusCode());
    }

    private void assertValidEnumValuesOnlyRejectedInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        ThingifierHttpApi cleanApi =
                api(
                        cleanThingifier,
                        new RoboDepotBusinessRulesHook(cleanThingifier, RoboDepotBugMode.NONE));
        Assertions.assertEquals(
                201,
                post(
                                cleanApi,
                                "/robots",
                                "{\"color\":\"yellow\",\"status\":\"idle\",\"batteryLevel\":90}")
                        .getStatusCode());
        Assertions.assertEquals(
                201,
                post(
                                cleanApi,
                                "/jobs",
                                "{\"priority\":2,\"state\":\"cancelled\",\"payloadType\":\"small-bin\",\"createdTick\":1}")
                        .getStatusCode());

        Thingifier classicThingifier = emptyRoboDepot();
        ThingifierHttpApi classicApi =
                api(
                        classicThingifier,
                        new RoboDepotBusinessRulesHook(
                                classicThingifier, RoboDepotBugMode.CLASSIC));
        Assertions.assertEquals(
                422,
                post(
                                classicApi,
                                "/robots",
                                "{\"color\":\"yellow\",\"status\":\"idle\",\"batteryLevel\":90}")
                        .getStatusCode());
        Assertions.assertEquals(
                422,
                post(
                                classicApi,
                                "/jobs",
                                "{\"priority\":2,\"state\":\"cancelled\",\"payloadType\":\"small-bin\",\"createdTick\":1}")
                        .getStatusCode());
    }

    private void assertStockPutOnlyIncrementsInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        ThingifierHttpApi cleanApi =
                api(
                        cleanThingifier,
                        new RoboDepotBusinessRulesHook(cleanThingifier, RoboDepotBugMode.NONE));
        String cleanStockId =
                create(cleanApi, "/stock", "{\"quantity\":5,\"condition\":\"sealed\"}");
        Assertions.assertEquals(
                200,
                put(cleanApi, "/stock/" + cleanStockId, "{\"quantity\":3,\"condition\":\"sealed\"}")
                        .getStatusCode());
        Assertions.assertEquals("3", field(cleanThingifier, "stock", cleanStockId, "quantity"));

        Thingifier classicThingifier = emptyRoboDepot();
        ThingifierHttpApi classicApi =
                api(
                        classicThingifier,
                        new RoboDepotBusinessRulesHook(
                                classicThingifier, RoboDepotBugMode.CLASSIC));
        String classicStockId =
                create(classicApi, "/stock", "{\"quantity\":5,\"condition\":\"sealed\"}");
        Assertions.assertEquals(
                200,
                put(
                                classicApi,
                                "/stock/" + classicStockId,
                                "{\"quantity\":3,\"condition\":\"sealed\"}")
                        .getStatusCode());
        Assertions.assertEquals("8", field(classicThingifier, "stock", classicStockId, "quantity"));
    }

    private void assertPayloadMismatchOnlyAllowedInClassic() {
        Thingifier cleanThingifier = new WarehouseRobotThingifier().get();
        ThingifierHttpApi cleanApi =
                api(
                        cleanThingifier,
                        new RoboDepotBusinessRulesHook(cleanThingifier, RoboDepotBugMode.NONE));
        String cleanJobId =
                create(
                        cleanApi,
                        "/jobs",
                        "{\"priority\":2,\"state\":\"queued\",\"payloadType\":\"oversize\",\"createdTick\":300}");
        Assertions.assertEquals(
                409,
                post(cleanApi, "/robots/3/jobs", "{\"id\":" + cleanJobId + "}").getStatusCode());

        Thingifier classicThingifier = new WarehouseRobotThingifier().get();
        ThingifierHttpApi classicApi =
                api(
                        classicThingifier,
                        new RoboDepotBusinessRulesHook(
                                classicThingifier, RoboDepotBugMode.CLASSIC));
        String classicJobId =
                create(
                        classicApi,
                        "/jobs",
                        "{\"priority\":2,\"state\":\"queued\",\"payloadType\":\"oversize\",\"createdTick\":300}");
        Assertions.assertEquals(
                201,
                post(classicApi, "/robots/3/jobs", "{\"id\":" + classicJobId + "}")
                        .getStatusCode());
    }

    private void assertPriorityInversionOnlyHappensInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        PriorityScenario cleanScenario = createPriorityScenario(cleanThingifier);

        tickOnce(cleanThingifier, RoboDepotBugMode.NONE);

        Assertions.assertEquals(
                "in-progress",
                field(cleanThingifier, "job", cleanScenario.highPriorityJobId(), "state"));
        Assertions.assertEquals(
                "blocked",
                field(cleanThingifier, "job", cleanScenario.lowPriorityJobId(), "state"));

        Thingifier classicThingifier = emptyRoboDepot();
        PriorityScenario classicScenario = createPriorityScenario(classicThingifier);

        tickOnce(classicThingifier, RoboDepotBugMode.CLASSIC);

        Assertions.assertEquals(
                "blocked",
                field(classicThingifier, "job", classicScenario.highPriorityJobId(), "state"));
        Assertions.assertEquals(
                "in-progress",
                field(classicThingifier, "job", classicScenario.lowPriorityJobId(), "state"));
    }

    private void assertLowBatteryRobotOnlyWorksInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        RobotJobScenario cleanScenario =
                createQueuedJobScenario(cleanThingifier, "idle", 10, "standard", "standard-tote");

        tickOnce(cleanThingifier, RoboDepotBugMode.NONE);

        Assertions.assertEquals(
                "blocked", field(cleanThingifier, "job", cleanScenario.jobId(), "state"));
        Assertions.assertEquals(
                "idle", field(cleanThingifier, "robot", cleanScenario.robotId(), "status"));

        Thingifier classicThingifier = emptyRoboDepot();
        RobotJobScenario classicScenario =
                createQueuedJobScenario(classicThingifier, "idle", 10, "standard", "standard-tote");

        tickOnce(classicThingifier, RoboDepotBugMode.CLASSIC);

        Assertions.assertEquals(
                "in-progress", field(classicThingifier, "job", classicScenario.jobId(), "state"));
        Assertions.assertEquals(
                "assigned", field(classicThingifier, "robot", classicScenario.robotId(), "status"));
    }

    private void assertChargingRobotOnlyGetsAssignedInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        RobotJobScenario cleanScenario =
                createQueuedJobScenario(
                        cleanThingifier, "charging", 40, "standard", "standard-tote");

        tickOnce(cleanThingifier, RoboDepotBugMode.NONE);

        Assertions.assertEquals(
                "blocked", field(cleanThingifier, "job", cleanScenario.jobId(), "state"));
        Assertions.assertEquals(
                "charging", field(cleanThingifier, "robot", cleanScenario.robotId(), "status"));
        Assertions.assertEquals(
                "50", field(cleanThingifier, "robot", cleanScenario.robotId(), "batteryLevel"));

        Thingifier classicThingifier = emptyRoboDepot();
        RobotJobScenario classicScenario =
                createQueuedJobScenario(
                        classicThingifier, "charging", 40, "standard", "standard-tote");

        tickOnce(classicThingifier, RoboDepotBugMode.CLASSIC);

        Assertions.assertEquals(
                "in-progress", field(classicThingifier, "job", classicScenario.jobId(), "state"));
        Assertions.assertEquals(
                "assigned", field(classicThingifier, "robot", classicScenario.robotId(), "status"));
        Assertions.assertEquals(
                "50", field(classicThingifier, "robot", classicScenario.robotId(), "batteryLevel"));
    }

    private void assertPayloadMismatchTickOnlyAllowedInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        RobotJobScenario cleanScenario =
                createQueuedJobScenario(cleanThingifier, "idle", 90, "light", "oversize");

        tickOnce(cleanThingifier, RoboDepotBugMode.NONE);

        Assertions.assertEquals(
                "blocked", field(cleanThingifier, "job", cleanScenario.jobId(), "state"));

        Thingifier classicThingifier = emptyRoboDepot();
        RobotJobScenario classicScenario =
                createQueuedJobScenario(classicThingifier, "idle", 90, "light", "oversize");

        tickOnce(classicThingifier, RoboDepotBugMode.CLASSIC);

        Assertions.assertEquals(
                "in-progress", field(classicThingifier, "job", classicScenario.jobId(), "state"));
    }

    private void assertOfflineRobotOnlyMovesInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        RobotJobScenario cleanScenario =
                createInProgressScenarioWithStock(
                        cleanThingifier,
                        "offline",
                        80,
                        "standard",
                        "standard-tote",
                        "crate-a",
                        "medium",
                        false,
                        "ambient",
                        "sealed",
                        1);

        tickOnce(cleanThingifier, RoboDepotBugMode.NONE);

        Assertions.assertEquals(
                "blocked", field(cleanThingifier, "job", cleanScenario.jobId(), "state"));
        Assertions.assertEquals(
                cleanScenario.pickupZoneId(),
                relatedId(cleanThingifier, "robot", cleanScenario.robotId(), "zone"));
        Assertions.assertEquals(
                "1", field(cleanThingifier, "stock", cleanScenario.stockId(), "quantity"));

        Thingifier classicThingifier = emptyRoboDepot();
        RobotJobScenario classicScenario =
                createInProgressScenarioWithStock(
                        classicThingifier,
                        "offline",
                        80,
                        "standard",
                        "standard-tote",
                        "crate-a",
                        "medium",
                        false,
                        "ambient",
                        "sealed",
                        1);

        tickOnce(classicThingifier, RoboDepotBugMode.CLASSIC);

        Assertions.assertEquals(
                "completed", field(classicThingifier, "job", classicScenario.jobId(), "state"));
        Assertions.assertEquals(
                classicScenario.dropoffZoneId(),
                relatedId(classicThingifier, "robot", classicScenario.robotId(), "zone"));
        Assertions.assertEquals(
                "0", field(classicThingifier, "stock", classicScenario.stockId(), "quantity"));
    }

    private void assertDamagedStockOnlyGetsPickedInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        RobotJobScenario cleanScenario =
                createInProgressScenarioWithStock(
                        cleanThingifier,
                        "assigned",
                        80,
                        "standard",
                        "standard-tote",
                        "crate-a",
                        "medium",
                        false,
                        "ambient",
                        "damaged",
                        1);

        tickOnce(cleanThingifier, RoboDepotBugMode.NONE);

        Assertions.assertEquals(
                "blocked", field(cleanThingifier, "job", cleanScenario.jobId(), "state"));
        Assertions.assertEquals(
                "1", field(cleanThingifier, "stock", cleanScenario.stockId(), "quantity"));

        Thingifier classicThingifier = emptyRoboDepot();
        RobotJobScenario classicScenario =
                createInProgressScenarioWithStock(
                        classicThingifier,
                        "assigned",
                        80,
                        "standard",
                        "standard-tote",
                        "crate-a",
                        "medium",
                        false,
                        "ambient",
                        "damaged",
                        1);

        tickOnce(classicThingifier, RoboDepotBugMode.CLASSIC);

        Assertions.assertEquals(
                "completed", field(classicThingifier, "job", classicScenario.jobId(), "state"));
        Assertions.assertEquals(
                "0", field(classicThingifier, "stock", classicScenario.stockId(), "quantity"));
    }

    private void assertWrongSkuOnlyGetsAdjustedInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        WrongSkuScenario cleanScenario = createWrongSkuScenario(cleanThingifier);

        tickOnce(cleanThingifier, RoboDepotBugMode.NONE);

        Assertions.assertEquals(
                "completed", field(cleanThingifier, "job", cleanScenario.jobId(), "state"));
        Assertions.assertEquals(
                "3", field(cleanThingifier, "stock", cleanScenario.wrongStockId(), "quantity"));
        Assertions.assertEquals(
                "4", field(cleanThingifier, "stock", cleanScenario.correctStockId(), "quantity"));

        Thingifier classicThingifier = emptyRoboDepot();
        WrongSkuScenario classicScenario = createWrongSkuScenario(classicThingifier);

        tickOnce(classicThingifier, RoboDepotBugMode.CLASSIC);

        Assertions.assertEquals(
                "completed", field(classicThingifier, "job", classicScenario.jobId(), "state"));
        Assertions.assertEquals(
                "2", field(classicThingifier, "stock", classicScenario.wrongStockId(), "quantity"));
        Assertions.assertEquals(
                "5",
                field(classicThingifier, "stock", classicScenario.correctStockId(), "quantity"));
    }

    private void assertStockShortageOnlyCompletesInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        RobotJobScenario cleanScenario =
                createInProgressScenarioWithoutStock(
                        cleanThingifier, "assigned", 80, "standard", "standard-tote");

        tickOnce(cleanThingifier, RoboDepotBugMode.NONE);

        Assertions.assertEquals(
                "blocked", field(cleanThingifier, "job", cleanScenario.jobId(), "state"));

        Thingifier classicThingifier = emptyRoboDepot();
        RobotJobScenario classicScenario =
                createInProgressScenarioWithoutStock(
                        classicThingifier, "assigned", 80, "standard", "standard-tote");

        tickOnce(classicThingifier, RoboDepotBugMode.CLASSIC);

        Assertions.assertEquals(
                "completed", field(classicThingifier, "job", classicScenario.jobId(), "state"));
    }

    private void assertRobotStatusOnlyStaysAssignedInClassic() {
        Thingifier cleanThingifier = emptyRoboDepot();
        RobotJobScenario cleanScenario =
                createInProgressScenarioWithStock(
                        cleanThingifier,
                        "assigned",
                        80,
                        "standard",
                        "standard-tote",
                        "crate-a",
                        "medium",
                        false,
                        "ambient",
                        "sealed",
                        1);

        tickOnce(cleanThingifier, RoboDepotBugMode.NONE);

        Assertions.assertEquals(
                "completed", field(cleanThingifier, "job", cleanScenario.jobId(), "state"));
        Assertions.assertEquals(
                "idle", field(cleanThingifier, "robot", cleanScenario.robotId(), "status"));

        Thingifier classicThingifier = emptyRoboDepot();
        RobotJobScenario classicScenario =
                createInProgressScenarioWithStock(
                        classicThingifier,
                        "assigned",
                        80,
                        "standard",
                        "standard-tote",
                        "crate-a",
                        "medium",
                        false,
                        "ambient",
                        "sealed",
                        1);

        tickOnce(classicThingifier, RoboDepotBugMode.CLASSIC);

        Assertions.assertEquals(
                "completed", field(classicThingifier, "job", classicScenario.jobId(), "state"));
        Assertions.assertEquals(
                "assigned", field(classicThingifier, "robot", classicScenario.robotId(), "status"));
    }

    @Test
    void bugModeParsingDefaultsToClassicAndOnlyArgsNoneDisablesBundle() {
        Assertions.assertEquals(RoboDepotBugMode.CLASSIC, RoboDepotBugMode.fromValue(null));
        Assertions.assertEquals(RoboDepotBugMode.CLASSIC, RoboDepotBugMode.fromValue("surprise"));
        Assertions.assertEquals(
                RoboDepotBugMode.CLASSIC, RoboDepotBugMode.fromArgs(new String[] {}));
        Assertions.assertEquals(
                RoboDepotBugMode.CLASSIC, RoboDepotBugMode.fromArgs(new String[] {"-bugs=none"}));
        Assertions.assertEquals(
                RoboDepotBugMode.CLASSIC,
                RoboDepotBugMode.fromArgs(
                        new String[] {"-robodepotbugs=active-job-unassign-blocked"}));
        Assertions.assertEquals(
                RoboDepotBugMode.NONE,
                RoboDepotBugMode.fromArgs(new String[] {"-robodepotbugs=none"}));
        Assertions.assertTrue(
                RoboDepotBugMode.CLASSIC.enables(RoboDepotBugMode.ACTIVE_JOB_UNASSIGN_BLOCKED));
        Assertions.assertTrue(
                RoboDepotBugMode.CLASSIC.enables(RoboDepotBugMode.STOCK_PUT_INCREMENTS));
        Assertions.assertTrue(
                RoboDepotBugMode.CLASSIC.enables(RoboDepotBugMode.ROBOT_STATUS_STALE));
        for (RoboDepotBugMode mode : RoboDepotBugMode.values()) {
            if (mode != RoboDepotBugMode.NONE && mode != RoboDepotBugMode.CLASSIC) {
                Assertions.assertTrue(RoboDepotBugMode.CLASSIC.enables(mode));
            }
        }
    }

    private PriorityScenario createPriorityScenario(final Thingifier thingifier) {
        ThingifierHttpApi api = api(thingifier);
        String modelId = createModel(api, "standard");
        String zoneId = createZone(api);
        String robotId = createRobot(api, "red", "idle", 90);
        connect(api, "/robotmodels/" + modelId + "/robots", robotId);
        connect(api, "/zones/" + zoneId + "/robots", robotId);

        for (int index = 0; index < WarehouseRobotThingifier.MAX_JOBS_PER_ROBOT - 1; index++) {
            String fillerJobId = createJob(api, 1, "completed", "small-bin", index);
            connect(api, "/robots/" + robotId + "/jobs", fillerJobId);
        }

        String highPriorityJobId = createJob(api, 5, "queued", "small-bin", 100);
        String lowPriorityJobId = createJob(api, 1, "queued", "small-bin", 101);
        return new PriorityScenario(highPriorityJobId, lowPriorityJobId);
    }

    private RobotJobScenario createQueuedJobScenario(
            final Thingifier thingifier,
            final String robotStatus,
            final int batteryLevel,
            final String payloadClass,
            final String payloadType) {
        ThingifierHttpApi api = api(thingifier);
        String modelId = createModel(api, payloadClass);
        String pickupZoneId = createZone(api, "pick", 5, "ambient", false);
        String dropoffZoneId = createZone(api, "pack", 5, "ambient", false);
        String robotId = createRobot(api, "red", robotStatus, batteryLevel);
        String jobId = createJob(api, 3, "queued", payloadType, 1);

        connect(api, "/robotmodels/" + modelId + "/robots", robotId);
        connect(api, "/zones/" + pickupZoneId + "/robots", robotId);
        connect(api, "/zones/" + pickupZoneId + "/pickupJobs", jobId);
        connect(api, "/zones/" + dropoffZoneId + "/dropoffJobs", jobId);

        return new RobotJobScenario(robotId, jobId, pickupZoneId, dropoffZoneId, null);
    }

    private RobotJobScenario createInProgressScenarioWithoutStock(
            final Thingifier thingifier,
            final String robotStatus,
            final int batteryLevel,
            final String payloadClass,
            final String payloadType) {
        ThingifierHttpApi api = api(thingifier);
        String modelId = createModel(api, payloadClass);
        String pickupZoneId = createZone(api, "pick", 5, "ambient", false);
        String dropoffZoneId = createZone(api, "pack", 5, "ambient", false);
        String robotId = createRobot(api, "red", robotStatus, batteryLevel);
        String jobId = createJob(api, 3, "in-progress", payloadType, 1);

        connect(api, "/robotmodels/" + modelId + "/robots", robotId);
        connect(api, "/zones/" + pickupZoneId + "/robots", robotId);
        connect(api, "/robots/" + robotId + "/jobs", jobId);
        connect(api, "/zones/" + pickupZoneId + "/pickupJobs", jobId);
        connect(api, "/zones/" + dropoffZoneId + "/dropoffJobs", jobId);

        return new RobotJobScenario(robotId, jobId, pickupZoneId, dropoffZoneId, null);
    }

    private RobotJobScenario createInProgressScenarioWithStock(
            final Thingifier thingifier,
            final String robotStatus,
            final int batteryLevel,
            final String payloadClass,
            final String payloadType,
            final String skuCode,
            final String sizeClass,
            final boolean fragile,
            final String storageRequirement,
            final String stockCondition,
            final int stockQuantity) {
        RobotJobScenario scenario =
                createInProgressScenarioWithoutStock(
                        thingifier, robotStatus, batteryLevel, payloadClass, payloadType);
        ThingifierHttpApi api = api(thingifier);
        String skuId = createSku(api, skuCode, sizeClass, fragile, storageRequirement);
        String stockId = createStock(api, stockQuantity, stockCondition);

        connect(api, "/zones/" + scenario.pickupZoneId() + "/stock", stockId);
        connect(api, "/skus/" + skuId + "/stock", stockId);

        return new RobotJobScenario(
                scenario.robotId(),
                scenario.jobId(),
                scenario.pickupZoneId(),
                scenario.dropoffZoneId(),
                stockId);
    }

    private WrongSkuScenario createWrongSkuScenario(final Thingifier thingifier) {
        RobotJobScenario scenario =
                createInProgressScenarioWithoutStock(
                        thingifier, "assigned", 80, "standard", "cold-chain");
        ThingifierHttpApi api = api(thingifier);
        String wrongSkuId = createSku(api, "crate-a", "medium", false, "ambient");
        String wrongStockId = createStock(api, 3, "sealed");
        String correctSkuId = createSku(api, "cold-a", "medium", false, "chilled");
        String correctStockId = createStock(api, 5, "sealed");

        connect(api, "/zones/" + scenario.pickupZoneId() + "/stock", wrongStockId);
        connect(api, "/skus/" + wrongSkuId + "/stock", wrongStockId);
        connect(api, "/zones/" + scenario.pickupZoneId() + "/stock", correctStockId);
        connect(api, "/skus/" + correctSkuId + "/stock", correctStockId);

        return new WrongSkuScenario(scenario.jobId(), wrongStockId, correctStockId);
    }

    private RoboDepotTickResult tickOnce(
            final Thingifier thingifier, final RoboDepotBugMode bugMode) {
        MutableClock clock = new MutableClock(Instant.parse("2026-07-27T12:00:00Z"));
        RoboDepotSimulation simulation = new RoboDepotSimulation(thingifier, bugMode, clock);
        RoboDepotTickResult result = simulation.tick(new HttpHeadersBlock());
        Assertions.assertEquals(200, result.statusCode(), result.asJson());
        return result;
    }

    private void assertRobotJobCap(final ThingifierHttpApi api, final String robotId) {
        for (int index = 0; index <= WarehouseRobotThingifier.MAX_JOBS_PER_ROBOT; index++) {
            String jobId =
                    create(
                            api,
                            "/jobs",
                            "{\"priority\":2,\"state\":\"queued\",\"payloadType\":\"small-bin\",\"createdTick\":"
                                    + index
                                    + "}");
            int expectedStatus = index == WarehouseRobotThingifier.MAX_JOBS_PER_ROBOT ? 422 : 201;
            Assertions.assertEquals(
                    expectedStatus,
                    post(api, "/robots/" + robotId + "/jobs", "{\"id\":" + jobId + "}")
                            .getStatusCode());
        }
    }

    private void assertZoneStockCap(final ThingifierHttpApi api, final String zoneId) {
        for (int index = 0; index <= WarehouseRobotThingifier.MAX_STOCK_PER_ZONE; index++) {
            String stockId = create(api, "/stock", "{\"quantity\":1,\"condition\":\"sealed\"}");
            int expectedStatus = index == WarehouseRobotThingifier.MAX_STOCK_PER_ZONE ? 422 : 201;
            Assertions.assertEquals(
                    expectedStatus,
                    post(api, "/zones/" + zoneId + "/stock", "{\"id\":" + stockId + "}")
                            .getStatusCode());
        }
    }

    private void assertCapacity(
            final Thingifier thingifier,
            final ThingifierHttpApi api,
            final String entityName,
            final String route,
            final int maxInstances,
            final String body) {
        while (count(thingifier, entityName) < maxInstances) {
            Assertions.assertEquals(201, post(api, route, body).getStatusCode());
        }
        Assertions.assertEquals(409, post(api, route, body).getStatusCode());
    }

    private void assertCrud(
            final Thingifier thingifier,
            final String route,
            final String createBody,
            final String amendBody) {
        ThingifierHttpApi api = api(thingifier);
        HttpApiResponse created = post(api, "/" + route, createBody);
        Assertions.assertEquals(201, created.getStatusCode());
        String id = created.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER);

        Assertions.assertEquals(200, get(api, "/" + route + "/" + id).getStatusCode());
        Assertions.assertEquals(200, post(api, "/" + route + "/" + id, amendBody).getStatusCode());
        Assertions.assertEquals(204, delete(api, "/" + route + "/" + id).getStatusCode());
        Assertions.assertEquals(404, get(api, "/" + route + "/" + id).getStatusCode());
    }

    private void assertEntity(
            final Thingifier thingifier,
            final String name,
            final String plural,
            final int maxInstances) {
        EntityDefinition entity = thingifier.getDefinitionNamed(name);
        Assertions.assertNotNull(entity);
        Assertions.assertEquals(plural, entity.getPlural());
        Assertions.assertEquals(maxInstances, entity.getMaxInstanceLimit());
        Assertions.assertEquals("id", entity.getPrimaryKeyField().getName());
        Assertions.assertEquals(FieldType.AUTO_INCREMENT, entity.getPrimaryKeyField().getType());
    }

    private void assertEntityDescription(
            final EntityDefinition entity, final String expectedDescription) {
        Assertions.assertTrue(entity.hasDescription());
        Assertions.assertEquals(expectedDescription, entity.getDescription());
    }

    private String createZone(final ThingifierHttpApi api) {
        return createZone(api, "pick", 5, "ambient", false);
    }

    private String createZone(
            final ThingifierHttpApi api,
            final String zoneType,
            final int capacity,
            final String temperatureBand,
            final boolean closed) {
        return create(
                api,
                "/zones",
                "{\"zoneType\":\""
                        + zoneType
                        + "\",\"capacity\":"
                        + capacity
                        + ",\"temperatureBand\":\""
                        + temperatureBand
                        + "\",\"closed\":"
                        + closed
                        + "}");
    }

    private String createModel(final ThingifierHttpApi api, final String payloadClass) {
        return create(
                api,
                "/robotmodels",
                "{\"modelCode\":\"rb100\",\"payloadClass\":\""
                        + payloadClass
                        + "\",\"driveType\":\"wheeled\",\"maxBatteryHours\":8}");
    }

    private String createSku(
            final ThingifierHttpApi api,
            final String skuCode,
            final String sizeClass,
            final boolean fragile,
            final String storageRequirement) {
        return create(
                api,
                "/skus",
                "{\"skuCode\":\""
                        + skuCode
                        + "\",\"sizeClass\":\""
                        + sizeClass
                        + "\",\"fragile\":"
                        + fragile
                        + ",\"storageRequirement\":\""
                        + storageRequirement
                        + "\"}");
    }

    private String createRobot(
            final ThingifierHttpApi api,
            final String color,
            final String status,
            final int batteryLevel) {
        return create(
                api,
                "/robots",
                "{\"color\":\""
                        + color
                        + "\",\"status\":\""
                        + status
                        + "\",\"batteryLevel\":"
                        + batteryLevel
                        + "}");
    }

    private String createJob(
            final ThingifierHttpApi api,
            final int priority,
            final String state,
            final String payloadType,
            final int createdTick) {
        return create(
                api,
                "/jobs",
                "{\"priority\":"
                        + priority
                        + ",\"state\":\""
                        + state
                        + "\",\"payloadType\":\""
                        + payloadType
                        + "\",\"createdTick\":"
                        + createdTick
                        + "}");
    }

    private String createStock(
            final ThingifierHttpApi api, final int quantity, final String condition) {
        return create(
                api,
                "/stock",
                "{\"quantity\":" + quantity + ",\"condition\":\"" + condition + "\"}");
    }

    private void connect(final ThingifierHttpApi api, final String path, final String id) {
        Assertions.assertEquals(201, post(api, path, "{\"id\":" + id + "}").getStatusCode(), path);
    }

    private String create(final ThingifierHttpApi api, final String path, final String body) {
        HttpApiResponse response = post(api, path, body);
        Assertions.assertEquals(201, response.getStatusCode(), response.getBody());
        return response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER);
    }

    private HttpApiResponse get(final ThingifierHttpApi api, final String path) {
        return get(api, request(path, HttpApiRequest.VERB.GET, null, null));
    }

    private HttpApiResponse get(final ThingifierHttpApi api, final HttpApiRequest request) {
        return api.get(request);
    }

    private HttpApiResponse post(
            final ThingifierHttpApi api, final String path, final String body) {
        return post(api, request(path, HttpApiRequest.VERB.POST, body, null));
    }

    private HttpApiResponse post(final ThingifierHttpApi api, final HttpApiRequest request) {
        return api.post(request);
    }

    private HttpApiResponse put(final ThingifierHttpApi api, final String path, final String body) {
        return api.put(request(path, HttpApiRequest.VERB.PUT, body, null));
    }

    private HttpApiResponse delete(final ThingifierHttpApi api, final String path) {
        return api.delete(request(path, HttpApiRequest.VERB.DELETE, null, null));
    }

    private HttpApiRequest request(
            final String path,
            final HttpApiRequest.VERB verb,
            final String body,
            final String sessionId) {
        HttpApiRequest request = new HttpApiRequest(path).setVerb(verb);
        request.getHeaders().put("Accept", "application/json");
        if (body != null) {
            request.getHeaders().put("Content-Type", "application/json");
            request.setBody(body);
        }
        if (sessionId != null) {
            request.getHeaders().put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, sessionId);
        }
        return request;
    }

    private ThingifierHttpApi api(final Thingifier thingifier, final HttpApiRequestHook... hooks) {
        return new ThingifierHttpApi(thingifier, Arrays.asList(hooks), null);
    }

    private Thingifier emptyRoboDepot() {
        Thingifier thingifier = new WarehouseRobotThingifier().get();
        thingifier.clearAllData(EntityRelModel.DEFAULT_DATABASE_NAME);
        return thingifier;
    }

    private int count(final Thingifier thingifier, final String entityName) {
        return count(thingifier, entityName, EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private int count(
            final Thingifier thingifier, final String entityName, final String databaseName) {
        return thingifier
                .getStore(databaseName)
                .entityQueries()
                .count(thingifier.getDefinitionNamed(entityName));
    }

    private int relatedCount(
            final Thingifier thingifier,
            final String entityName,
            final String id,
            final String relationshipName) {
        return relatedInstances(thingifier, entityName, id, relationshipName).size();
    }

    private String relatedId(
            final Thingifier thingifier,
            final String entityName,
            final String id,
            final String relationshipName) {
        List<EntityInstance> related =
                relatedInstances(thingifier, entityName, id, relationshipName);
        Assertions.assertEquals(1, related.size());
        return related.get(0).getPrimaryKeyValue();
    }

    private List<EntityInstance> relatedInstances(
            final Thingifier thingifier,
            final String entityName,
            final String id,
            final String relationshipName) {
        EntityInstance instance = findById(thingifier, entityName, id);
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .relationships()
                .listRelated(instance, relationshipName);
    }

    private EntityInstance findById(
            final Thingifier thingifier, final String entityName, final String id) {
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entityQueries()
                .findByPrimaryKey(thingifier.getDefinitionNamed(entityName), id);
    }

    private String field(
            final Thingifier thingifier,
            final String entityName,
            final String id,
            final String fieldName) {
        return findById(thingifier, entityName, id).getFieldValue(fieldName).asString();
    }

    private record PriorityScenario(String highPriorityJobId, String lowPriorityJobId) {}

    private record RobotJobScenario(
            String robotId,
            String jobId,
            String pickupZoneId,
            String dropoffZoneId,
            String stockId) {}

    private record WrongSkuScenario(String jobId, String wrongStockId, String correctStockId) {}

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(final Instant instant) {
            this.instant = instant;
        }

        private void advanceSeconds(final int seconds) {
            instant = instant.plusSeconds(seconds);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
