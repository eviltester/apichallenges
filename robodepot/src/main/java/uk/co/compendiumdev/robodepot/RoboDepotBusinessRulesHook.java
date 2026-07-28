package uk.co.compendiumdev.robodepot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class RoboDepotBusinessRulesHook implements HttpApiRequestHook {

    private final Thingifier thingifier;
    private final RoboDepotBugMode bugMode;

    public RoboDepotBusinessRulesHook(final Thingifier thingifier, final RoboDepotBugMode bugMode) {
        this.thingifier = thingifier;
        this.bugMode = bugMode;
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request, final ThingifierApiConfig config) {
        List<String> segments = ApiPath.segments(request.getPath());

        HttpApiResponse enumBug = rejectSelectedValidEnums(request, config, segments);
        if (enumBug != null) {
            return enumBug;
        }

        HttpApiResponse stockPutBug = incrementStockInsteadOfReplacing(request, config, segments);
        if (stockPutBug != null) {
            return stockPutBug;
        }

        HttpApiResponse deleteBug =
                rejectStateSpecificRelationshipDeletes(request, config, segments);
        if (deleteBug != null) {
            return deleteBug;
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST && segments.size() == 3) {
            return enforceRelationshipBusinessRules(request, config, segments);
        }

        return null;
    }

    private HttpApiResponse rejectSelectedValidEnums(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final List<String> segments) {
        if (request.getVerb() != HttpApiRequest.VERB.POST
                && request.getVerb() != HttpApiRequest.VERB.PUT) {
            return null;
        }

        if (isEntityRoute(segments, "robots")
                && bugMode.enables(RoboDepotBugMode.VALID_ROBOT_COLOR_YELLOW_REJECTED)
                && "yellow".equalsIgnoreCase(bodyField(request, "robot", "color"))) {
            return HookResponses.error(
                    request, config, 422, "Robot color yellow is temporarily unavailable");
        }

        if (isEntityRoute(segments, "jobs")
                && bugMode.enables(RoboDepotBugMode.VALID_JOB_STATE_CANCELLED_REJECTED)
                && "cancelled".equalsIgnoreCase(bodyField(request, "job", "state"))) {
            return HookResponses.error(
                    request, config, 422, "Job state cancelled is not accepted by this scheduler");
        }

        return null;
    }

    private HttpApiResponse incrementStockInsteadOfReplacing(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final List<String> segments) {
        if (!bugMode.enables(RoboDepotBugMode.STOCK_PUT_INCREMENTS)
                || request.getVerb() != HttpApiRequest.VERB.PUT
                || segments.size() != 2
                || !"stock".equals(segments.get(0))) {
            return null;
        }

        String requestedQuantity = bodyField(request, "stock", "quantity");
        if (requestedQuantity.isEmpty()) {
            return null;
        }

        int requested = RoboDepotSupport.asInt(requestedQuantity, -1);
        if (requested < 0) {
            return null;
        }

        ThingifierRequestContext context =
                ThingifierRequestContext.from(thingifier, request.getHeaders());
        ThingStore store = context.store();
        EntityInstance stock =
                RoboDepotSupport.findById(thingifier, store, "stock", segments.get(1));
        if (stock == null) {
            return null;
        }

        int newQuantity = RoboDepotSupport.intValue(stock, "quantity") + requested;
        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(stock.getEntity());
        draft.withField("quantity", String.valueOf(newQuantity));

        String requestedCondition = bodyField(request, "stock", "condition");
        if (!requestedCondition.isEmpty()) {
            draft.withField("condition", requestedCondition);
        }

        try {
            EntityInstance updated = store.entities().patch(stock, draft);
            ApiResponse apiResponse =
                    ApiResponse.success()
                            .returnSingleInstance(updated)
                            .usingRelationships(store.relationships());
            return new HttpApiResponse(
                    request.getHeaders(), apiResponse, new JsonThing(config.jsonOutput()), config);
        } catch (RuntimeException e) {
            return HookResponses.error(request, config, 422, e.getMessage());
        }
    }

    private HttpApiResponse rejectStateSpecificRelationshipDeletes(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final List<String> segments) {
        if (request.getVerb() != HttpApiRequest.VERB.DELETE || segments.size() != 4) {
            return null;
        }

        ThingifierRequestContext context =
                ThingifierRequestContext.from(thingifier, request.getHeaders());
        ThingStore store = context.store();

        if ("robots".equals(segments.get(0))
                && "jobs".equals(segments.get(2))
                && bugMode.enables(RoboDepotBugMode.ACTIVE_JOB_UNASSIGN_BLOCKED)) {
            EntityInstance job =
                    RoboDepotSupport.findById(thingifier, store, "job", segments.get(3));
            if (job != null && "in-progress".equals(RoboDepotSupport.stringValue(job, "state"))) {
                return HookResponses.error(
                        request, config, 409, "In-progress jobs cannot be unassigned");
            }
        }

        if ("zones".equals(segments.get(0))
                && "stock".equals(segments.get(2))
                && bugMode.enables(RoboDepotBugMode.HELD_STOCK_UNLINK_BLOCKED)) {
            EntityInstance stock =
                    RoboDepotSupport.findById(thingifier, store, "stock", segments.get(3));
            if (stock != null && "held".equals(RoboDepotSupport.stringValue(stock, "condition"))) {
                return HookResponses.error(request, config, 409, "Held stock cannot be unlinked");
            }
        }

        return null;
    }

    private HttpApiResponse enforceRelationshipBusinessRules(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final List<String> segments) {
        ThingifierRequestContext context =
                ThingifierRequestContext.from(thingifier, request.getHeaders());
        ThingStore store = context.store();

        if ("zones".equals(segments.get(0)) && "robots".equals(segments.get(2))) {
            return enforceZoneRobotRules(request, config, store, segments);
        }

        if ("zones".equals(segments.get(0)) && "stock".equals(segments.get(2))) {
            return enforceZoneStockRules(request, config, store, segments);
        }

        if ("skus".equals(segments.get(0)) && "stock".equals(segments.get(2))) {
            return enforceSkuStockRules(request, config, store, segments);
        }

        if ("robots".equals(segments.get(0)) && "jobs".equals(segments.get(2))) {
            return enforceRobotJobRules(request, config, store, segments);
        }

        return null;
    }

    private HttpApiResponse enforceZoneRobotRules(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final ThingStore store,
            final List<String> segments) {
        EntityInstance zone = RoboDepotSupport.findById(thingifier, store, "zone", segments.get(1));
        if (zone == null) {
            return null;
        }

        if (RoboDepotSupport.booleanValue(zone, "closed")
                && !bugMode.enables(RoboDepotBugMode.CLOSED_ZONE_ACCEPTS_ROBOTS)) {
            return HookResponses.error(request, config, 409, "Closed zones cannot accept robots");
        }

        int robotCount = RoboDepotSupport.related(store, zone, "robots").size();
        int capacity =
                Math.min(
                        RoboDepotSupport.intValue(zone, "capacity"),
                        WarehouseRobotThingifier.MAX_ROBOTS_PER_ZONE);
        if (bugMode.enables(RoboDepotBugMode.ZONE_CAPACITY_OFF_BY_ONE)) {
            if (robotCount >= Math.max(0, capacity - 1)) {
                return HookResponses.error(
                        request, config, 409, "Zone capacity reached before final robot slot");
            }
        } else if (robotCount >= capacity) {
            return HookResponses.error(request, config, 409, "Zone robot capacity reached");
        }

        return null;
    }

    private HttpApiResponse enforceZoneStockRules(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final ThingStore store,
            final List<String> segments) {
        EntityInstance zone = RoboDepotSupport.findById(thingifier, store, "zone", segments.get(1));
        EntityInstance stock =
                RoboDepotSupport.findById(thingifier, store, "stock", bodyField(request, "", "id"));
        if (zone == null || stock == null) {
            return null;
        }

        if (RoboDepotSupport.booleanValue(zone, "closed")
                && !bugMode.enables(RoboDepotBugMode.CLOSED_ZONE_ACCEPTS_STOCK)) {
            return HookResponses.error(request, config, 409, "Closed zones cannot accept stock");
        }

        EntityInstance sku = RoboDepotSupport.firstRelated(store, stock, "sku");
        if (isFragileFrozen(zone, sku)
                && !bugMode.enables(RoboDepotBugMode.FRAGILE_FROZEN_STOCK_ALLOWED)) {
            return HookResponses.error(
                    request, config, 409, "Fragile stock cannot be stored in frozen zones");
        }

        return null;
    }

    private HttpApiResponse enforceSkuStockRules(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final ThingStore store,
            final List<String> segments) {
        EntityInstance sku = RoboDepotSupport.findById(thingifier, store, "sku", segments.get(1));
        EntityInstance stock =
                RoboDepotSupport.findById(thingifier, store, "stock", bodyField(request, "", "id"));
        if (sku == null || stock == null) {
            return null;
        }

        EntityInstance zone = RoboDepotSupport.firstRelated(store, stock, "zone");
        if (isFragileFrozen(zone, sku)
                && !bugMode.enables(RoboDepotBugMode.FRAGILE_FROZEN_STOCK_ALLOWED)) {
            return HookResponses.error(
                    request, config, 409, "Fragile stock cannot be stored in frozen zones");
        }

        return null;
    }

    private HttpApiResponse enforceRobotJobRules(
            final HttpApiRequest request,
            final ThingifierApiConfig config,
            final ThingStore store,
            final List<String> segments) {
        EntityInstance robot =
                RoboDepotSupport.findById(thingifier, store, "robot", segments.get(1));
        EntityInstance job =
                RoboDepotSupport.findById(thingifier, store, "job", bodyField(request, "", "id"));
        if (robot == null || job == null) {
            return null;
        }

        if (!payloadMatches(store, robot, job)
                && !bugMode.enables(RoboDepotBugMode.PAYLOAD_MISMATCH_ALLOWED)) {
            return HookResponses.error(
                    request, config, 409, "Robot payload class cannot handle job payload type");
        }

        return null;
    }

    static boolean payloadMatches(
            final ThingStore store, final EntityInstance robot, final EntityInstance job) {
        EntityInstance model = RoboDepotSupport.firstRelated(store, robot, "model");
        if (model == null) {
            return true;
        }

        String payloadClass = RoboDepotSupport.stringValue(model, "payloadClass");
        String payloadType = RoboDepotSupport.stringValue(job, "payloadType");
        return switch (payloadType) {
            case "small-bin" -> true;
            case "standard-tote", "cold-chain" ->
                    "standard".equals(payloadClass) || "heavy".equals(payloadClass);
            case "oversize" -> "heavy".equals(payloadClass);
            default -> true;
        };
    }

    private boolean isFragileFrozen(final EntityInstance zone, final EntityInstance sku) {
        if (zone == null || sku == null) {
            return false;
        }
        return "frozen".equals(RoboDepotSupport.stringValue(zone, "temperatureBand"))
                && RoboDepotSupport.booleanValue(sku, "fragile");
    }

    private boolean isEntityRoute(final List<String> segments, final String pluralName) {
        return (segments.size() == 1 || segments.size() == 2) && pluralName.equals(segments.get(0));
    }

    private String bodyField(
            final HttpApiRequest request, final String entityName, final String fieldName) {
        if (request.getBody() == null || request.getBody().trim().isEmpty()) {
            return "";
        }

        try {
            JsonElement parsed = JsonParser.parseString(request.getBody());
            if (!parsed.isJsonObject()) {
                return "";
            }
            JsonObject object = parsed.getAsJsonObject();
            if (!entityName.isEmpty()
                    && object.has(entityName)
                    && object.get(entityName).isJsonObject()) {
                object = object.getAsJsonObject(entityName);
            }
            JsonElement value = object.get(fieldName);
            return value == null || !value.isJsonPrimitive() ? "" : value.getAsString();
        } catch (RuntimeException e) {
            return "";
        }
    }
}
