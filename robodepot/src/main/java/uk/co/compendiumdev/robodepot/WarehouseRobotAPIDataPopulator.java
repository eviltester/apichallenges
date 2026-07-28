package uk.co.compendiumdev.robodepot;

import uk.co.compendiumdev.thingifier.core.domain.datapopulator.RepositoryDataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class WarehouseRobotAPIDataPopulator implements RepositoryDataPopulator {

    @Override
    public void populate(final ERSchema schema, final ThingStore store) {
        EntityDefinition robotModel = schema.getEntityDefinitionNamed("robotmodel");
        EntityDefinition sku = schema.getEntityDefinitionNamed("sku");
        EntityDefinition zone = schema.getEntityDefinitionNamed("zone");
        EntityDefinition robot = schema.getEntityDefinitionNamed("robot");
        EntityDefinition job = schema.getEntityDefinitionNamed("job");
        EntityDefinition stock = schema.getEntityDefinitionNamed("stock");

        EntityInstance rb100 =
                create(
                        store,
                        robotModel,
                        "modelCode",
                        "rb100",
                        "payloadClass",
                        "standard",
                        "driveType",
                        "wheeled",
                        "maxBatteryHours",
                        "8");
        EntityInstance rb200 =
                create(
                        store,
                        robotModel,
                        "modelCode",
                        "rb200",
                        "payloadClass",
                        "heavy",
                        "driveType",
                        "tracked",
                        "maxBatteryHours",
                        "12");
        EntityInstance lf300 =
                create(
                        store,
                        robotModel,
                        "modelCode",
                        "lf300",
                        "payloadClass",
                        "light",
                        "driveType",
                        "omni",
                        "maxBatteryHours",
                        "6");
        EntityInstance sr400 =
                create(
                        store,
                        robotModel,
                        "modelCode",
                        "sr400",
                        "payloadClass",
                        "standard",
                        "driveType",
                        "omni",
                        "maxBatteryHours",
                        "10");

        EntityInstance crateA =
                create(
                        store,
                        sku,
                        "skuCode",
                        "crate-a",
                        "sizeClass",
                        "medium",
                        "fragile",
                        "false",
                        "storageRequirement",
                        "ambient");
        EntityInstance crateB =
                create(
                        store,
                        sku,
                        "skuCode",
                        "crate-b",
                        "sizeClass",
                        "small",
                        "fragile",
                        "false",
                        "storageRequirement",
                        "ambient");
        EntityInstance coldA =
                create(
                        store,
                        sku,
                        "skuCode",
                        "cold-a",
                        "sizeClass",
                        "medium",
                        "fragile",
                        "true",
                        "storageRequirement",
                        "chilled");

        EntityInstance inbound =
                create(
                        store,
                        zone,
                        "zoneType",
                        "inbound",
                        "capacity",
                        "20",
                        "temperatureBand",
                        "ambient",
                        "closed",
                        "false");
        EntityInstance pick =
                create(
                        store,
                        zone,
                        "zoneType",
                        "pick",
                        "capacity",
                        "30",
                        "temperatureBand",
                        "ambient",
                        "closed",
                        "false");
        EntityInstance cold =
                create(
                        store,
                        zone,
                        "zoneType",
                        "cold",
                        "capacity",
                        "10",
                        "temperatureBand",
                        "chilled",
                        "closed",
                        "false");
        EntityInstance charging =
                create(
                        store,
                        zone,
                        "zoneType",
                        "charging",
                        "capacity",
                        "8",
                        "temperatureBand",
                        "ambient",
                        "closed",
                        "false");

        EntityInstance robotOne =
                create(store, robot, "color", "red", "status", "idle", "batteryLevel", "95");
        EntityInstance robotTwo =
                create(store, robot, "color", "blue", "status", "assigned", "batteryLevel", "72");
        EntityInstance robotThree =
                create(store, robot, "color", "green", "status", "charging", "batteryLevel", "28");
        EntityInstance robotFour =
                create(
                        store,
                        robot,
                        "color",
                        "yellow",
                        "status",
                        "maintenance",
                        "batteryLevel",
                        "44");
        EntityInstance robotFive =
                create(store, robot, "color", "black", "status", "idle", "batteryLevel", "88");
        EntityInstance robotSix =
                create(store, robot, "color", "white", "status", "offline", "batteryLevel", "5");

        connectRobot(store, rb100, inbound, robotOne);
        connectRobot(store, rb200, pick, robotTwo);
        connectRobot(store, lf300, charging, robotThree);
        connectRobot(store, sr400, charging, robotFour);
        connectRobot(store, rb100, pick, robotFive);
        connectRobot(store, rb200, cold, robotSix);

        EntityInstance jobOne = createJob(store, job, "1", "queued", "standard-tote", "100");
        EntityInstance jobTwo = createJob(store, job, "2", "in-progress", "small-bin", "105");
        EntityInstance jobThree = createJob(store, job, "3", "blocked", "cold-chain", "110");
        EntityInstance jobFour = createJob(store, job, "4", "queued", "oversize", "115");
        EntityInstance jobFive = createJob(store, job, "5", "completed", "standard-tote", "120");
        EntityInstance jobSix = createJob(store, job, "2", "cancelled", "small-bin", "125");

        connectJob(store, robotOne, inbound, pick, jobOne);
        connectJob(store, robotTwo, pick, inbound, jobTwo);
        connectJob(store, robotTwo, cold, pick, jobThree);
        connectJob(store, robotFive, inbound, cold, jobFour);
        connectJob(store, robotOne, pick, charging, jobFive);
        connectJob(store, robotSix, cold, inbound, jobSix);

        connectStock(store, inbound, crateA, createStock(store, stock, "12", "sealed"));
        connectStock(store, inbound, crateB, createStock(store, stock, "4", "open"));
        connectStock(store, pick, crateB, createStock(store, stock, "18", "sealed"));
        connectStock(store, pick, crateA, createStock(store, stock, "3", "held"));
        connectStock(store, cold, coldA, createStock(store, stock, "7", "sealed"));
        connectStock(store, cold, crateA, createStock(store, stock, "2", "damaged"));
        connectStock(store, charging, coldA, createStock(store, stock, "1", "held"));
    }

    private EntityInstance createJob(
            final ThingStore store,
            final EntityDefinition job,
            final String priority,
            final String state,
            final String payloadType,
            final String createdTick) {
        return create(
                store,
                job,
                "priority",
                priority,
                "state",
                state,
                "payloadType",
                payloadType,
                "createdTick",
                createdTick);
    }

    private EntityInstance createStock(
            final ThingStore store,
            final EntityDefinition stock,
            final String quantity,
            final String condition) {
        return create(store, stock, "quantity", quantity, "condition", condition);
    }

    private void connectRobot(
            final ThingStore store,
            final EntityInstance robotModel,
            final EntityInstance zone,
            final EntityInstance robot) {
        store.relationships().connect(robotModel, "robots", robot);
        store.relationships().connect(zone, "robots", robot);
    }

    private void connectJob(
            final ThingStore store,
            final EntityInstance robot,
            final EntityInstance pickupZone,
            final EntityInstance dropoffZone,
            final EntityInstance job) {
        store.relationships().connect(robot, "jobs", job);
        store.relationships().connect(pickupZone, "pickupJobs", job);
        store.relationships().connect(dropoffZone, "dropoffJobs", job);
    }

    private void connectStock(
            final ThingStore store,
            final EntityInstance zone,
            final EntityInstance sku,
            final EntityInstance stock) {
        store.relationships().connect(zone, "stock", stock);
        store.relationships().connect(sku, "stock", stock);
    }

    private EntityInstance create(
            final ThingStore store, final EntityDefinition entity, final String... fieldPairs) {
        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(entity);
        for (int index = 0; index < fieldPairs.length; index += 2) {
            draft.withField(fieldPairs[index], fieldPairs[index + 1]);
        }
        return store.entities().create(draft);
    }
}
