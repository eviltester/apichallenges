package uk.co.compendiumdev.robodepot;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreTransaction;

public final class RoboDepotSimulation {

    public static final int SECONDS_BETWEEN_TICKS = 20;

    private final Thingifier thingifier;
    private final RoboDepotBugMode bugMode;
    private final Clock clock;
    private final Map<String, Instant> lastTickAt;
    private final Map<String, Integer> currentTickByDatabase;

    public RoboDepotSimulation(final Thingifier thingifier, final String bugModeValue) {
        this(thingifier, RoboDepotBugMode.fromValue(bugModeValue), Clock.systemUTC());
    }

    public RoboDepotSimulation(
            final Thingifier thingifier, final RoboDepotBugMode bugMode, final Clock clock) {
        this.thingifier = thingifier;
        this.bugMode = bugMode;
        this.clock = clock;
        this.lastTickAt = new HashMap<>();
        this.currentTickByDatabase = new HashMap<>();
    }

    public synchronized RoboDepotTickResult tick(final HttpHeadersBlock headers) {
        ThingifierRequestContext context = ThingifierRequestContext.from(thingifier, headers);
        String databaseName = context.databaseName();
        Instant now = clock.instant();
        Instant previousTick = lastTickAt.get(databaseName);
        if (previousTick != null) {
            long millisSinceLastTick = Duration.between(previousTick, now).toMillis();
            long tickWindowMillis = SECONDS_BETWEEN_TICKS * 1000L;
            if (millisSinceLastTick < tickWindowMillis) {
                long retryAfter = (tickWindowMillis - millisSinceLastTick + 999L) / 1000L;
                return RoboDepotTickResult.rateLimited((int) Math.max(1L, retryAfter));
            }
        }

        ThingStore store = context.store();
        int nextTick =
                currentTickByDatabase.computeIfAbsent(
                                databaseName, ignored -> highestCreatedTick(store))
                        + 1;

        try (ThingStoreTransaction transaction = store.beginTransaction()) {
            RoboDepotTickResult.TickCounters counters = advanceStore(store);
            transaction.commit();
            currentTickByDatabase.put(databaseName, nextTick);
            lastTickAt.put(databaseName, now);
            return RoboDepotTickResult.advanced(nextTick, counters);
        } catch (RuntimeException e) {
            return RoboDepotTickResult.error(
                    e.getMessage() == null ? "RoboDepot tick failed" : e.getMessage());
        }
    }

    private int highestCreatedTick(final ThingStore store) {
        return RoboDepotSupport.list(thingifier, store, "job").stream()
                .mapToInt(job -> RoboDepotSupport.intValue(job, "createdTick"))
                .max()
                .orElse(0);
    }

    private RoboDepotTickResult.TickCounters advanceStore(final ThingStore store) {
        RoboDepotTickResult.TickCounters counters = new RoboDepotTickResult.TickCounters();
        chargeRobots(store);

        List<EntityInstance> inProgressAtStart =
                RoboDepotSupport.list(thingifier, store, "job").stream()
                        .filter(
                                job ->
                                        "in-progress"
                                                .equals(RoboDepotSupport.stringValue(job, "state")))
                        .toList();

        startQueuedJobs(store, counters);
        completeInProgressJobs(store, inProgressAtStart, counters);
        return counters;
    }

    private void chargeRobots(final ThingStore store) {
        for (EntityInstance robot : RoboDepotSupport.list(thingifier, store, "robot")) {
            if ("charging".equals(RoboDepotSupport.stringValue(robot, "status"))) {
                int battery = RoboDepotSupport.intValue(robot, "batteryLevel");
                RoboDepotSupport.patch(
                        store, robot, "batteryLevel", String.valueOf(Math.min(100, battery + 10)));
            }
        }
    }

    private void startQueuedJobs(
            final ThingStore store, final RoboDepotTickResult.TickCounters counters) {
        List<EntityInstance> queuedJobs =
                RoboDepotSupport.list(thingifier, store, "job").stream()
                        .filter(job -> "queued".equals(RoboDepotSupport.stringValue(job, "state")))
                        .sorted(jobComparator())
                        .toList();

        for (EntityInstance job : queuedJobs) {
            EntityInstance robot = robotForQueuedJob(store, job);
            if (robot == null || !robotCanWorkOnJob(store, robot, job)) {
                blockJob(store, job, counters);
                continue;
            }

            if (RoboDepotSupport.firstRelated(store, job, "robot") == null) {
                store.relationships().connect(robot, "jobs", job);
            }

            RoboDepotSupport.patch(store, job, "state", "in-progress");
            RoboDepotSupport.patch(store, robot, "status", "assigned");
        }
    }

    private Comparator<EntityInstance> jobComparator() {
        Comparator<EntityInstance> priority =
                Comparator.comparingInt(job -> RoboDepotSupport.intValue(job, "priority"));
        if (!bugMode.enables(RoboDepotBugMode.PRIORITY_INVERTED)) {
            priority = priority.reversed();
        }
        return priority.thenComparingInt(job -> RoboDepotSupport.intValue(job, "createdTick"));
    }

    private EntityInstance robotForQueuedJob(final ThingStore store, final EntityInstance job) {
        EntityInstance assignedRobot = RoboDepotSupport.firstRelated(store, job, "robot");
        if (assignedRobot != null) {
            return assignedRobot;
        }

        return RoboDepotSupport.sortedById(RoboDepotSupport.list(thingifier, store, "robot"))
                .stream()
                .filter(robot -> robotCanWorkOnJob(store, robot, job))
                .filter(robot -> robotCanAcceptAnotherJob(store, robot))
                .findFirst()
                .orElse(null);
    }

    private boolean robotCanAcceptAnotherJob(final ThingStore store, final EntityInstance robot) {
        return RoboDepotSupport.related(store, robot, "jobs").size()
                < WarehouseRobotThingifier.MAX_JOBS_PER_ROBOT;
    }

    private void completeInProgressJobs(
            final ThingStore store,
            final List<EntityInstance> inProgressAtStart,
            final RoboDepotTickResult.TickCounters counters) {
        for (EntityInstance originalJob : inProgressAtStart) {
            EntityInstance job =
                    RoboDepotSupport.findById(
                            thingifier, store, "job", originalJob.getPrimaryKeyValue());
            if (job == null || !"in-progress".equals(RoboDepotSupport.stringValue(job, "state"))) {
                continue;
            }

            EntityInstance robot = RoboDepotSupport.firstRelated(store, job, "robot");
            EntityInstance pickupZone = RoboDepotSupport.firstRelated(store, job, "pickupZone");
            EntityInstance dropoffZone = RoboDepotSupport.firstRelated(store, job, "dropoffZone");

            if (robot == null
                    || pickupZone == null
                    || dropoffZone == null
                    || RoboDepotSupport.booleanValue(pickupZone, "closed")
                    || RoboDepotSupport.booleanValue(dropoffZone, "closed")
                    || !robotCanWorkOnJob(store, robot, job)) {
                blockJob(store, job, counters);
                continue;
            }

            EntityInstance stock = stockToConsume(store, pickupZone, job);
            if (stock == null && !bugMode.enables(RoboDepotBugMode.STOCK_SHORTAGE_COMPLETES)) {
                blockJob(store, job, counters);
                continue;
            }

            if (stock != null) {
                int currentQuantity = RoboDepotSupport.intValue(stock, "quantity");
                if (currentQuantity <= 0
                        && !bugMode.enables(RoboDepotBugMode.STOCK_SHORTAGE_COMPLETES)) {
                    blockJob(store, job, counters);
                    continue;
                }
                if (currentQuantity > 0) {
                    RoboDepotSupport.patch(
                            store, stock, "quantity", String.valueOf(currentQuantity - 1));
                    counters.stockAdjusted++;
                }
            }

            moveRobotToZone(store, robot, dropoffZone, counters);
            drainRobotBattery(store, robot, job);
            String finalRobotStatus =
                    bugMode.enables(RoboDepotBugMode.ROBOT_STATUS_STALE) ? "assigned" : "idle";
            RoboDepotSupport.patch(store, robot, "status", finalRobotStatus);
            RoboDepotSupport.patch(store, job, "state", "completed");
            counters.jobsCompleted++;
        }
    }

    private void blockJob(
            final ThingStore store,
            final EntityInstance job,
            final RoboDepotTickResult.TickCounters counters) {
        RoboDepotSupport.patch(store, job, "state", "blocked");
        counters.jobsBlocked++;
    }

    private boolean robotCanWorkOnJob(
            final ThingStore store, final EntityInstance robot, final EntityInstance job) {
        String status = RoboDepotSupport.stringValue(robot, "status");
        if ("maintenance".equals(status)) {
            return false;
        }
        if ("charging".equals(status)
                && !bugMode.enables(RoboDepotBugMode.CHARGING_ROBOT_ASSIGNED_JOB)) {
            return false;
        }
        if ("offline".equals(status) && !bugMode.enables(RoboDepotBugMode.OFFLINE_ROBOT_MOVES)) {
            return false;
        }
        if (!"idle".equals(status)
                && !"assigned".equals(status)
                && !"charging".equals(status)
                && !"offline".equals(status)) {
            return false;
        }
        if (RoboDepotSupport.intValue(robot, "batteryLevel") < 15
                && !bugMode.enables(RoboDepotBugMode.LOW_BATTERY_ROBOT_WORKS)) {
            return false;
        }
        return RoboDepotBusinessRulesHook.payloadMatches(store, robot, job)
                || bugMode.enables(RoboDepotBugMode.PAYLOAD_MISMATCH_ALLOWED);
    }

    private EntityInstance stockToConsume(
            final ThingStore store, final EntityInstance pickupZone, final EntityInstance job) {
        List<EntityInstance> zoneStock =
                RoboDepotSupport.sortedById(RoboDepotSupport.related(store, pickupZone, "stock"));
        if (bugMode.enables(RoboDepotBugMode.WRONG_SKU_ADJUSTED)
                && "cold-chain".equals(RoboDepotSupport.stringValue(job, "payloadType"))) {
            return zoneStock.stream()
                    .filter(stock -> RoboDepotSupport.intValue(stock, "quantity") > 0)
                    .filter(stock -> stockUsable(stock))
                    .findFirst()
                    .orElse(null);
        }

        return zoneStock.stream()
                .filter(stock -> RoboDepotSupport.intValue(stock, "quantity") > 0)
                .filter(stock -> stockUsable(stock))
                .filter(stock -> stockMatchesPayload(store, stock, job))
                .findFirst()
                .orElse(null);
    }

    private boolean stockUsable(final EntityInstance stock) {
        String condition = RoboDepotSupport.stringValue(stock, "condition");
        if ("sealed".equals(condition) || "open".equals(condition)) {
            return true;
        }
        return "damaged".equals(condition)
                && bugMode.enables(RoboDepotBugMode.DAMAGED_STOCK_PICKED);
    }

    private boolean stockMatchesPayload(
            final ThingStore store, final EntityInstance stock, final EntityInstance job) {
        EntityInstance sku = RoboDepotSupport.firstRelated(store, stock, "sku");
        if (sku == null) {
            return false;
        }

        String payloadType = RoboDepotSupport.stringValue(job, "payloadType");
        String sizeClass = RoboDepotSupport.stringValue(sku, "sizeClass");
        String storageRequirement = RoboDepotSupport.stringValue(sku, "storageRequirement");
        return switch (payloadType) {
            case "small-bin" -> "small".equals(sizeClass);
            case "standard-tote" ->
                    "medium".equals(sizeClass) && "ambient".equals(storageRequirement);
            case "oversize" -> "large".equals(sizeClass);
            case "cold-chain" ->
                    "chilled".equals(storageRequirement) || "frozen".equals(storageRequirement);
            default -> false;
        };
    }

    private void moveRobotToZone(
            final ThingStore store,
            final EntityInstance robot,
            final EntityInstance dropoffZone,
            final RoboDepotTickResult.TickCounters counters) {
        for (EntityInstance zone : RoboDepotSupport.related(store, robot, "zone")) {
            store.relationships().disconnectBetween(zone, robot, "robots");
        }
        store.relationships().connect(dropoffZone, "robots", robot);
        counters.robotsMoved++;
    }

    private void drainRobotBattery(
            final ThingStore store, final EntityInstance robot, final EntityInstance job) {
        int currentBattery = RoboDepotSupport.intValue(robot, "batteryLevel");
        int drain =
                switch (RoboDepotSupport.stringValue(job, "payloadType")) {
                    case "small-bin" -> 5;
                    case "oversize" -> 18;
                    case "cold-chain" -> 12;
                    default -> 10;
                };
        RoboDepotSupport.patch(
                store, robot, "batteryLevel", String.valueOf(Math.max(0, currentBattery - drain)));
    }
}
