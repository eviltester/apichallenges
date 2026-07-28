package uk.co.compendiumdev.robodepot;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.AUTO_INCREMENT;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.BOOLEAN;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.ENUM;
import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.INTEGER;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfigProfile;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProvider;

public class WarehouseRobotThingifier {

    public static final int MAX_ROBOT_MODELS = 6;
    public static final int MAX_SKUS = 4;
    public static final int MAX_ZONES = 6;
    public static final int MAX_ROBOTS = 24;
    public static final int MAX_JOBS = 60;
    public static final int MAX_STOCK = 48;
    public static final int MAX_JOBS_PER_ROBOT = 10;
    public static final int MAX_ROBOTS_PER_ZONE = 24;
    public static final int MAX_STOCK_PER_ZONE = 8;
    public static final String ROBOT_MODEL_DESCRIPTION =
            "A read-only catalog entry describing a robot hardware type available in RoboDepot.";
    public static final String SKU_DESCRIPTION =
            "A read-only catalog entry describing a constrained stock keeping unit that can be stored in zones.";
    public static final String ZONE_DESCRIPTION =
            "A warehouse area where robots can work, charge, or store stock.";
    public static final String ROBOT_DESCRIPTION =
            "A warehouse robot with a color, battery level, and operational status.";
    public static final String JOB_DESCRIPTION =
            "A warehouse task that can be assigned to a robot and linked to pickup and dropoff zones.";
    public static final String STOCK_DESCRIPTION =
            "A quantity of a SKU stored in a zone with a constrained condition.";

    public Thingifier get() {
        return get(new EntityRelModel());
    }

    public Thingifier get(final ThingStoreProvider storeProvider) {
        return get(new EntityRelModel(storeProvider));
    }

    public Thingifier get(final EntityRelModel entityRelModel) {

        Thingifier roboDepot = new Thingifier(entityRelModel);
        roboDepot.setDocumentation(
                "RoboDepot", "A constrained warehouse robot API for API testing challenges.");

        EntityDefinition robotModel =
                roboDepot.defineThing("robotmodel", "robotmodels", MAX_ROBOT_MODELS);
        robotModel.withDescription(ROBOT_MODEL_DESCRIPTION);
        robotModel.addAsPrimaryKeyField(
                idField("Unique identifier for a robot model catalog entry."));
        robotModel.addFields(
                enumField(
                                "modelCode",
                                "Constrained catalog code for the robot hardware model.",
                                "rb100",
                                "rb100",
                                "rb200",
                                "lf300",
                                "sr400")
                        .makeMandatory(),
                enumField(
                        "payloadClass",
                        "Payload weight class the model is designed to carry.",
                        "standard",
                        "light",
                        "standard",
                        "heavy"),
                enumField(
                        "driveType",
                        "Locomotion system used by robots of this model.",
                        "wheeled",
                        "wheeled",
                        "tracked",
                        "omni"),
                integerField(
                        "maxBatteryHours",
                        "Maximum operating hours this model can run on a full battery.",
                        1,
                        24,
                        "8"));

        EntityDefinition sku = roboDepot.defineThing("sku", "skus", MAX_SKUS);
        sku.withDescription(SKU_DESCRIPTION);
        sku.addAsPrimaryKeyField(idField("Unique identifier for a SKU catalog entry."));
        sku.addFields(
                enumField(
                                "skuCode",
                                "Constrained stock keeping unit code used by warehouse stock.",
                                "crate-a",
                                "crate-a",
                                "crate-b",
                                "pallet-a",
                                "pallet-b",
                                "cold-a",
                                "cold-b",
                                "fragile-a",
                                "hazmat-a")
                        .makeMandatory(),
                enumField(
                        "sizeClass",
                        "Physical size class used for storage and robot payload matching.",
                        "medium",
                        "small",
                        "medium",
                        "large"),
                booleanField("fragile", "Whether the SKU needs fragile handling.", "false"),
                enumField(
                        "storageRequirement",
                        "Storage environment required for this SKU.",
                        "ambient",
                        "ambient",
                        "chilled",
                        "frozen",
                        "hazmat"));

        EntityDefinition zone = roboDepot.defineThing("zone", "zones", MAX_ZONES);
        zone.withDescription(ZONE_DESCRIPTION);
        zone.addAsPrimaryKeyField(idField("Unique identifier for a warehouse zone."));
        zone.addFields(
                enumField(
                        "zoneType",
                        "Operational purpose of the warehouse zone.",
                        "inbound",
                        "inbound",
                        "outbound",
                        "pick",
                        "pack",
                        "cold",
                        "charging"),
                integerField(
                        "capacity",
                        "Maximum number of operational units the zone is configured to support.",
                        1,
                        100,
                        "12"),
                enumField(
                        "temperatureBand",
                        "Temperature range maintained in the zone.",
                        "ambient",
                        "ambient",
                        "chilled",
                        "frozen"),
                booleanField("closed", "Whether the zone is closed to new robot work.", "false"));

        EntityDefinition robot = roboDepot.defineThing("robot", "robots", MAX_ROBOTS);
        robot.withDescription(ROBOT_DESCRIPTION);
        robot.addAsPrimaryKeyField(idField("Unique identifier for a warehouse robot."));
        robot.addFields(
                enumField(
                        "color",
                        "Display color used for the robot in RoboDepot visualisations.",
                        "red",
                        "red",
                        "blue",
                        "green",
                        "yellow",
                        "black",
                        "white"),
                enumField(
                        "status",
                        "Current operational state of the robot.",
                        "idle",
                        "idle",
                        "charging",
                        "assigned",
                        "maintenance",
                        "offline"),
                integerField(
                        "batteryLevel",
                        "Robot battery charge percentage from 0 to 100.",
                        0,
                        100,
                        "100"));

        EntityDefinition job = roboDepot.defineThing("job", "jobs", MAX_JOBS);
        job.withDescription(JOB_DESCRIPTION);
        job.addAsPrimaryKeyField(idField("Unique identifier for a warehouse job."));
        job.addFields(
                integerField(
                        "priority",
                        "Relative urgency of the job, where 1 is lowest and 5 is highest.",
                        1,
                        5,
                        "3"),
                enumField(
                        "state",
                        "Current progress state for the job.",
                        "queued",
                        "queued",
                        "in-progress",
                        "blocked",
                        "completed",
                        "cancelled"),
                enumField(
                        "payloadType",
                        "Type of load the job expects a robot to move.",
                        "standard-tote",
                        "small-bin",
                        "standard-tote",
                        "oversize",
                        "cold-chain"),
                integerField(
                        "createdTick",
                        "Simulated creation tick used for ordering jobs without using dates.",
                        0,
                        999999,
                        "0"));

        EntityDefinition stock = roboDepot.defineThing("stock", "stock", MAX_STOCK);
        stock.withDescription(STOCK_DESCRIPTION);
        stock.addAsPrimaryKeyField(idField("Unique identifier for a stock record."));
        stock.addFields(
                integerField(
                        "quantity",
                        "Number of units represented by this stock record.",
                        0,
                        999,
                        "0"),
                enumField(
                        "condition",
                        "Handling condition currently assigned to the stock.",
                        "sealed",
                        "sealed",
                        "open",
                        "damaged",
                        "held"));

        roboDepot
                .defineRelationship(robotModel, robot, "robots", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "model");
        roboDepot
                .defineRelationship(zone, robot, "robots", new Cardinality(1, MAX_ROBOTS_PER_ZONE))
                .whenReversed(Cardinality.ONE_TO_ONE(), "zone");
        roboDepot
                .defineRelationship(robot, job, "jobs", new Cardinality(1, MAX_JOBS_PER_ROBOT))
                .whenReversed(Cardinality.ONE_TO_ONE(), "robot");
        roboDepot
                .defineRelationship(zone, job, "pickupJobs", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "pickupZone");
        roboDepot
                .defineRelationship(zone, job, "dropoffJobs", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "dropoffZone");
        roboDepot
                .defineRelationship(zone, stock, "stock", new Cardinality(1, MAX_STOCK_PER_ZONE))
                .whenReversed(Cardinality.ONE_TO_ONE(), "zone");
        roboDepot
                .defineRelationship(sku, stock, "stock", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "sku");

        roboDepot.setDataGenerator(new WarehouseRobotAPIDataPopulator());
        roboDepot.generateData(EntityRelModel.DEFAULT_DATABASE_NAME);

        ThingifierApiConfigProfile profile =
                roboDepot
                        .apiConfigProfiles()
                        .createDefaultProfile("v1", "RoboDepot constrained public API");
        ThingifierApiConfig config = profile.apiConfig();
        config.setFrom(new ThingifierApiConfig("/robodepot"));
        configurePublicApi(config);
        roboDepot.apiConfig().setFrom(new ThingifierApiConfig("/robodepot"));
        configurePublicApi(roboDepot.apiConfig());

        return roboDepot;
    }

    private static Field enumField(
            final String name,
            final String description,
            final String defaultValue,
            final String... examples) {
        Field field =
                Field.is(name, ENUM).withDescription(description).withDefaultValue(defaultValue);
        for (String example : examples) {
            field.withExample(example);
        }
        return field;
    }

    private static Field integerField(
            final String name,
            final String description,
            final int minimum,
            final int maximum,
            final String defaultValue) {
        return Field.is(name, INTEGER)
                .withDescription(description)
                .withMinMaxValues(minimum, maximum)
                .withDefaultValue(defaultValue);
    }

    private static Field booleanField(
            final String name, final String description, final String defaultValue) {
        return Field.is(name, BOOLEAN).withDescription(description).withDefaultValue(defaultValue);
    }

    private static Field idField(final String description) {
        return Field.is("id", AUTO_INCREMENT).withDescription(description);
    }

    private static void configurePublicApi(final ThingifierApiConfig config) {
        config.setUrlToShowSingleInstancesAsPlural(true);
        config.setApiToShowPrimaryKeyHeaderInResponse(true);
        config.jsonOutput().setCompressRelationships(true);
        config.jsonOutput().setConvertFieldsToDefinedTypes(true);
        config.setApiToEnforceDeclaredTypesInInput(true);
        config.statusCodes().setMaxRequestBodyLengthBytes(4096);
    }
}
