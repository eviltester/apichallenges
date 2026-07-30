package uk.co.compendiumdev.challenge.challengehooks;

import static uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi.HTTP_SESSION_HEADER_NAME;

import java.util.List;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

public class ChallengerApiRequestHook implements HttpApiRequestHook {

    private final Challengers challengers;

    public ChallengerApiRequestHook(Challengers challengers) {
        this.challengers = challengers;
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request, final ThingifierApiConfig config) {

        final boolean paginationLimitTooHigh = isTodosPaginationLimitTooHigh(request, config);

        ChallengerAuthData challenger =
                challengers.getChallenger(request.getHeader("X-CHALLENGER"));
        if (challenger == null) {
            if (paginationLimitTooHigh) {
                return rejectPaginationLimitTooHigh(request, config);
            }

            // if there is no x-challenger and we are in multi-player mode then do not allow any
            // POST, DELETE, PUT, PATCH through to the API as this would amend the default database
            if (challengers.isMultiPlayerMode()) {
                if (request.getVerb().equals(HttpApiRequest.VERB.POST)
                        || request.getVerb().equals(HttpApiRequest.VERB.PUT)
                        || request.getVerb().equals(HttpApiRequest.VERB.PATCH)
                        || request.getVerb().equals(HttpApiRequest.VERB.DELETE)) {
                    return new HttpApiResponse(
                            request.getHeaders(),
                            new ApiResponse(
                                    401,
                                    true,
                                    List.of(
                                            "Cannot amend details. Missing a valid X-CHALLENGER header.")),
                            new JsonThing(challengers.getApiConfig().jsonOutput()),
                            challengers.getApiConfig());
                }
            }

            // cannot track challenges
            return null;
        }

        // extend the life of the challenger
        challenger.touch();

        // trim the list of challengers
        challengers.purgeOldAuthData();

        // add challenger guid as session id to request
        request.addHeader(HTTP_SESSION_HEADER_NAME, challenger.getXChallenger());

        if (paginationLimitTooHigh) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_LIMIT_TOO_HIGH);
            return rejectPaginationLimitTooHigh(request, config);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().size() == 0) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS);
        }

        if (request.getVerb() == HttpApiRequest.VERB.HEAD
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().size() == 0) {
            challengers.pass(challenger, CHALLENGE.GET_HEAD_TODOS);
        }

        return null;
    }

    private boolean isTodosPaginationLimitTooHigh(
            final HttpApiRequest request, final ThingifierApiConfig config) {
        return request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && config.forParams().willAllowPagingThroughUrlParams()
                && queryParamIntegerGreaterThan(
                        request, "_limit", config.forParams().maxPagingLimit());
    }

    private boolean queryParamIntegerGreaterThan(
            final HttpApiRequest request, final String paramName, final int minimumValue) {
        Integer actualValue = queryParamInteger(request, paramName);
        return actualValue != null && actualValue > minimumValue;
    }

    private Integer queryParamInteger(final HttpApiRequest request, final String paramName) {
        if (!request.getQueryParams().containsKey(paramName)) {
            return null;
        }

        try {
            return Integer.parseInt(request.getQueryParams().get(paramName));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private HttpApiResponse rejectPaginationLimitTooHigh(
            final HttpApiRequest request, final ThingifierApiConfig config) {
        return new HttpApiResponse(
                request.getHeaders(),
                new ApiResponse(
                        400,
                        true,
                        List.of(
                                String.format(
                                        "_limit must be no more than %d",
                                        config.forParams().maxPagingLimit()))),
                new JsonThing(config.jsonOutput()),
                config);
    }
}
