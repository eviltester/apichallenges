package uk.co.compendiumdev.robodepot;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;

public final class RoboDepotHookConfigurator {

    private RoboDepotHookConfigurator() {}

    public static void registerSafetyAndBugHooks(
            final ThingifierHttpApiRoutings routings,
            final Thingifier thingifier,
            final String bugModeValue) {
        routings.registerHttpApiRequestHook(new DefaultDatabaseOnlyHook());
        routings.registerHttpApiRequestHook(new CatalogWriteBlockHook());

        RoboDepotBugMode bugMode = RoboDepotBugMode.fromValue(bugModeValue);
        routings.registerHttpApiRequestHook(new RoboDepotBusinessRulesHook(thingifier, bugMode));
    }
}
