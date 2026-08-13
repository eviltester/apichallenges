package uk.co.compendiumdev.challenge.challengehooks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.UrlQueryParamParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.FilterOperation;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;

public class ChallengerApiResponseHook implements HttpApiResponseHook {

    private static final String STRUCTURED_JSON_QUERY_CONTENT_TYPE =
            ThingifierHttpApi.STRUCTURED_QUERY_CONTENT_TYPE;

    Logger logger = LoggerFactory.getLogger(ChallengerApiResponseHook.class);

    private final Challengers challengers;
    private final Thingifier thingifier;

    public ChallengerApiResponseHook(final Challengers challengers, Thingifier thingifier) {
        this.challengers = challengers;
        this.thingifier = thingifier;
    }

    @Override
    public HttpApiResponse run(
            final HttpApiRequest request,
            final HttpApiResponse response,
            final ThingifierApiConfig config) {

        ChallengerAuthData challenger =
                challengers.getChallenger(request.getHeader("X-CHALLENGER"));

        if (challenger == null) {
            // cannot track challenges
            return null;
        }

        HttpApiResponse calendarResponse = todoCalendarResponseFor(request, challenger, config);
        if (calendarResponse != null) {
            return calendarResponse;
        }

        HttpApiResponse putNotFoundResponse =
                putTodoNotFoundResponseFor(request, response, challenger, config);
        if (putNotFoundResponse != null) {
            return putNotFoundResponse;
        }

        // READ
        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_TODO);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 404) {
            challengers.pass(challenger, CHALLENGE.GET_TODO_404);
        }

        final AcceptHeaderParser acceptParser = new AcceptHeaderParser(request.getHeader("accept"));
        final ContentTypeHeaderParser contentTypeParser =
                new ContentTypeHeaderParser(request.getHeader("content-type"));

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.hasAskedForXML()
                && response.getType().contentEquals("application/xml")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_XML);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.hasAskedForJSON()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_JSON);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.missingAcceptHeader()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && !acceptParser.isSupportedHeader()
                && response.getStatusCode() == 406) {
            challengers.pass(challenger, CHALLENGE.GET_UNSUPPORTED_ACCEPT_406);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.hasAskedForANY()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().isEmpty()
                && acceptParser.hasAskedForXML()
                && acceptParser.hasAskedForJSON()
                && acceptParser.hasAPreferenceForXml()
                && response.getType().contentEquals("application/xml")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.GET_ACCEPT_XML_PREFERRED);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && request.getQueryParams().containsKey("doneStatus")
                && request.getQueryParams().get("doneStatus").contentEquals("true")
                && response.getStatusCode() == 200) {
            if (hasDoneAndNotDoneTodos(challenger)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED);
            }
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && response.getStatusCode() == 200) {
            List<SortCriterion> sortCriteria = sortCriteriaFrom(request);
            if (isSingleFieldAscendingSort(sortCriteria)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_SORTED_ASCENDING);
            }
            if (isSingleFieldDescendingSort(sortCriteria)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_SORTED_DESCENDING);
            }
            if (isMultipleFieldSort(sortCriteria)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_SORTED_MULTIPLE_FIELDS);
            }
            if (hasTodoFilter(request) && hasValidSort(sortCriteria)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_AND_SORTED);
            }
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && queryParamIntegerGreaterThan(
                        request, "_limit", config.forParams().maxPagingLimit())
                && response.getStatusCode() == 400) {
            challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_LIMIT_TOO_HIGH);
        }

        if (request.getVerb() == HttpApiRequest.VERB.GET
                && request.getPath().contentEquals("todos")
                && response.getStatusCode() == 200) {
            List<JsonObject> responseTodos = todosFromJsonResponse(response);
            List<SortCriterion> sortCriteria = sortCriteriaFrom(request);

            if (idFilterResponseIsProperSubset(
                    request, responseTodos, challenger, FilterOperation.GREATER_THAN)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_ID_GREATER_THAN);
            }

            if (idFilterResponseIsProperSubset(
                    request, responseTodos, challenger, FilterOperation.LESS_THAN)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_ID_LESS_THAN);
            }

            if (idSingleResultFilterResponseMatches(request, responseTodos, challenger)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_ID_SINGLE_RESULT);
            }

            if (descriptionRegexFilterResponseMatches(request, responseTodos)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_REGEX);
            }

            if (descriptionWildcardFilterResponseMatches(request, responseTodos)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_FILTERED_DESCRIPTION_WILDCARD);
            }

            if (queryParamIntegerEquals(request, "_limit", 8)
                    && responseTodos.size() == expectedPaginatedTodoCount(challenger, 8, 0)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_LIMIT);
            }

            if (queryParamIntegerEquals(request, "_limit", 5)
                    && queryParamIntegerEquals(request, "_offset", 5)
                    && responseTodos.size() == expectedPaginatedTodoCount(challenger, 5, 5)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_LIMIT_OFFSET);
            }

            if (queryParamIntegerEquals(request, "_limit", 5)
                    && queryParamIntegerEquals(request, "_offset", 5)
                    && isDescendingIdSort(sortCriteria)
                    && responseTodos.size() == expectedPaginatedTodoCount(challenger, 5, 5)
                    && idsAreDescending(responseTodos)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_SORTED);
            }

            if (queryParamIntegerEquals(request, "_limit", 2)
                    && queryParamIntegerEquals(request, "_offset", 1)
                    && queryParamEquals(request, "doneStatus", "false")
                    && responseTodos.size()
                            == expectedFilteredPaginatedTodoCount(challenger, "false", 2, 1)
                    && allDoneStatusFalse(responseTodos)) {
                challengers.pass(challenger, CHALLENGE.GET_TODOS_PAGINATED_FILTERED);
            }
        }

        if (request.getVerb() == HttpApiRequest.VERB.QUERY
                && request.getPath().contentEquals("todos")
                && contentTypeParser.isFormUrlEncoded()
                && queryBodyContainsDoneStatusTrue(request)
                && response.getStatusCode() == 200) {
            if (hasDoneAndNotDoneTodos(challenger)) {
                challengers.pass(challenger, CHALLENGE.QUERY_TODOS_FILTERED);
            }
        }

        if (request.getVerb() == HttpApiRequest.VERB.QUERY
                && request.getPath().contentEquals("todos")
                && contentTypeParser.isJsonPath()
                && queryJsonPathBodyTargetsDoneStatusTrue(request)
                && response.getStatusCode() == 200) {
            List<JsonObject> responseTodos = todosFromJsonResponse(response);
            if (hasDoneAndNotDoneTodos(challenger)
                    && !responseTodos.isEmpty()
                    && allDoneStatusTrue(responseTodos)) {
                challengers.pass(challenger, CHALLENGE.QUERY_TODOS_JSONPATH_FILTERED);
            }
        }

        if (request.getVerb() == HttpApiRequest.VERB.QUERY
                && request.getPath().contentEquals("todos")
                && contentTypeIs(contentTypeParser, STRUCTURED_JSON_QUERY_CONTENT_TYPE)
                && queryStructuredJsonBodyTargetsDoneStatusTrue(request)
                && response.getStatusCode() == 200) {
            List<JsonObject> responseTodos = todosFromJsonResponse(response);
            if (hasDoneAndNotDoneTodos(challenger)
                    && !responseTodos.isEmpty()
                    && allDoneStatusTrue(responseTodos)) {
                challengers.pass(challenger, CHALLENGE.QUERY_TODOS_STRUCTURED_JSON_FILTERED);
            }
        }

        // CREATE
        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 404) {
            for (String errorMessage : response.apiResponse().getErrorMessages()) {
                if (errorMessage.startsWith("No such todo entity instance with id ==")) {
                    challengers.pass(challenger, CHALLENGE.POST_TODOS_404);
                }
            }
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 201) {

            try {

                String location = response.getHeaders().get("Location");
                String[] locationParts = location.split("/");

                if (locationParts.length > 2) {
                    // to check it is an int
                    Integer.parseInt(locationParts[2]);
                    EntityInstance aTodo = findTodoByIdentifier(challenger, locationParts[2]);
                    if (aTodo != null
                            && aTodo.getFieldValue("title").asString().length() == 50
                            && aTodo.getFieldValue("description").asString().length() == 200) {
                        challengers.pass(
                                challenger, CHALLENGE.POST_MAX_OUT_TITLE_DESCRIPTION_LENGTH);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error checking post todos 201 for max length ", e);
            }
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && contentTypeParser.isXML()
                && response.getType().contentEquals("application/xml")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_XML);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && contentTypeParser.isJSON()
                && acceptParser.hasAskedForJSON()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_JSON);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && contentTypeParser.isJSON()
                && response.getType().contentEquals("application/xml")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_JSON_ACCEPT_XML);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && acceptParser.hasAskedForJSON()
                && contentTypeParser.isXML()
                && response.getType().contentEquals("application/json")
                && response.getStatusCode() == 201) {
            challengers.pass(challenger, CHALLENGE.POST_CREATE_XML_ACCEPT_JSON);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 422
                && collate(response.apiResponse().getErrorMessages())
                        .contains("Failed Validation: doneStatus should be BOOLEAN")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_BAD_DONE_STATUS);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                &&
                // trap when creating or amending
                request.getPath().startsWith("todo")
                && response.getStatusCode() == 422
                && collate(response.apiResponse().getErrorMessages())
                        .contains(
                                "Failed Validation: Maximum allowable length exceeded for title - maximum allowed is 50")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_TOO_LONG_TITLE_LENGTH);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                &&
                // trap when creating or amending
                request.getPath().startsWith("todo")
                && response.getStatusCode() == 422
                && collate(response.apiResponse().getErrorMessages())
                        .contains(
                                "Failed Validation: Maximum allowable length exceeded for description - maximum allowed is 200")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_TOO_LONG_DESCRIPTION_LENGTH);
        }

        // POST to create too many todos
        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().contentEquals("todos")
                && response.getStatusCode() == 409
                && collate(response.apiResponse().getErrorMessages())
                        .contains("ERROR: Cannot add instance, maximum limit of 20 reached")) {
            challengers.pass(challenger, CHALLENGE.POST_ALL_TODOS);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 415) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_415);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 413
                && collate(response.apiResponse().getErrorMessages())
                        .toLowerCase()
                        .contains("request body too large")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_TOO_LONG_PAYLOAD_SIZE);
        }

        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos")
                && response.getStatusCode() == 422
                && collate(response.apiResponse().getErrorMessages())
                        .contains("Could not find field:")) {
            challengers.pass(challenger, CHALLENGE.POST_TODOS_INVALID_EXTRA_FIELD);
        }

        // UPDATE
        if (request.getVerb() == HttpApiRequest.VERB.POST
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 200) {
            challengers.pass(challenger, CHALLENGE.POST_UPDATE_TODO);
        }

        if (request.getVerb() == HttpApiRequest.VERB.PUT
                && request.getPath().contentEquals("todos")
                && response.getStatusCode() == 200
                && requestBodyHasIdField(request)) {
            challengers.pass(challenger, CHALLENGE.PUT_TODOS_BODY_ID_200);
        }

        if (request.getVerb() == HttpApiRequest.VERB.PUT
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 200
                && !requestBodyHasIdField(request)) {
            challengers.pass(challenger, CHALLENGE.PUT_TODOS_ID_NO_BODY_ID_200);
        }

        if (request.getVerb() == HttpApiRequest.VERB.PUT
                && request.getPath().contentEquals("todos")
                && response.getStatusCode() == 422
                && !requestBodyHasIdField(request)) {
            challengers.pass(challenger, CHALLENGE.PUT_TODOS_NO_ID_422);
        }

        if (request.getVerb() == HttpApiRequest.VERB.PUT
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 404
                && !requestBodyHasIdField(request)) {
            challengers.pass(challenger, CHALLENGE.PUT_TODOS_ID_NOT_FOUND_404);
        }

        if (request.getVerb() == HttpApiRequest.VERB.PATCH
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 200) {
            String patchContentType = contentTypeParser.mediaType();
            if (patchContentType.contentEquals("application/json")) {
                challengers.pass(challenger, CHALLENGE.PATCH_TODOS_PARTIAL_200);
            }
            if (patchContentType.contentEquals("application/merge-patch+json")) {
                challengers.pass(challenger, CHALLENGE.PATCH_TODOS_MERGE_PATCH_200);
            }
            if (patchContentType.contentEquals("application/json-patch+json")) {
                challengers.pass(challenger, CHALLENGE.PATCH_TODOS_JSON_PATCH_200);
            }
        }

        // DELETE
        if (request.getVerb() == HttpApiRequest.VERB.DELETE
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 204) {
            challengers.pass(challenger, CHALLENGE.DELETE_A_TODO);
        }

        if (request.getVerb() == HttpApiRequest.VERB.DELETE
                && request.getPath().matches("todos/.*")
                && response.getStatusCode() == 204
                && countTodos(challenger) == 0) {
            challengers.pass(challenger, CHALLENGE.DELETE_ALL_TODOS);
        }

        // do not interfere with api and return null
        return null;
    }

    private HttpApiResponse putTodoNotFoundResponseFor(
            final HttpApiRequest request,
            final HttpApiResponse response,
            final ChallengerAuthData challenger,
            final ThingifierApiConfig config) {

        String todoIdentifier = todoInstanceIdentifierFromPath(request);
        if (request.getVerb() != HttpApiRequest.VERB.PUT
                || todoIdentifier == null
                || requestBodyHasIdField(request)
                || (response.getStatusCode() != 422 && response.getStatusCode() != 404)
                || findTodoByIdentifier(challenger, todoIdentifier) != null) {
            return null;
        }

        challengers.pass(challenger, CHALLENGE.PUT_TODOS_ID_NOT_FOUND_404);
        if (response.getStatusCode() == 404) {
            return null;
        }

        return httpResponseFor(
                request,
                ApiResponse.error404("No such todo entity instance with id == " + todoIdentifier),
                config,
                "application/json",
                challenger);
    }

    private HttpApiResponse todoCalendarResponseFor(
            final HttpApiRequest request,
            final ChallengerAuthData challenger,
            final ThingifierApiConfig config) {

        String todoIdentifier = todoInstanceIdentifierForCalendarRequest(request);
        if (todoIdentifier == null || !prefersTextCalendar(request.getHeader("accept"))) {
            return null;
        }

        EntityInstance todo = findTodoByIdentifier(challenger, todoIdentifier);
        if (todo == null) {
            challengers.pass(challenger, CHALLENGE.GET_TODO_404);
            return httpResponseFor(
                    request,
                    ApiResponse.error404(
                            "No such todo entity instance with id == " + todoIdentifier),
                    config,
                    "application/json",
                    challenger);
        }

        challengers.pass(challenger, CHALLENGE.GET_TODO);
        challengers.pass(challenger, CHALLENGE.GET_TODO_ACCEPT_TEXT_CALENDAR);

        ApiResponse apiResponse = ApiResponse.success();
        apiResponse.setBody(todoAsCalendar(todo));
        return httpResponseFor(request, apiResponse, config, "text/calendar", challenger);
    }

    private String todoInstanceIdentifierForCalendarRequest(final HttpApiRequest request) {
        if (request.getVerb() != HttpApiRequest.VERB.GET
                || !request.getPath().startsWith("todos/")) {
            return null;
        }
        return todoInstanceIdentifierFromPath(request);
    }

    private String todoInstanceIdentifierFromPath(final HttpApiRequest request) {
        if (!request.getPath().startsWith("todos/")) {
            return null;
        }
        String identifier = request.getPath().substring("todos/".length());
        if (identifier.isEmpty()
                || identifier.contains("/")
                || identifier.equalsIgnoreCase("export")) {
            return null;
        }
        return identifier;
    }

    private boolean requestBodyHasIdField(final HttpApiRequest request) {
        String body = request.getBody();
        if (body == null || body.trim().isEmpty()) {
            return false;
        }

        try {
            JsonElement parsed = JsonParser.parseString(body);
            return parsed.isJsonObject() && parsed.getAsJsonObject().has("id");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean prefersTextCalendar(final String acceptHeader) {
        if (acceptHeader == null || acceptHeader.trim().isEmpty()) {
            return false;
        }

        String preferredMediaType = "";
        double preferredQuality = -1.0;
        for (String acceptedType : acceptHeader.split(",")) {
            String[] mediaTypeParts = acceptedType.trim().split(";");
            String mediaType = mediaTypeParts[0].trim();
            if (mediaType.isEmpty()) {
                continue;
            }

            double quality = qualityFromAcceptParameters(mediaTypeParts);
            if (quality > 0 && quality > preferredQuality) {
                preferredMediaType = mediaType;
                preferredQuality = quality;
            }
        }
        return preferredMediaType.equalsIgnoreCase("text/calendar");
    }

    private double qualityFromAcceptParameters(final String[] mediaTypeParts) {
        double quality = 1.0;
        for (int index = 1; index < mediaTypeParts.length; index++) {
            String parameter = mediaTypeParts[index].trim();
            if (parameter.regionMatches(true, 0, "q=", 0, 2)) {
                try {
                    quality = Double.parseDouble(parameter.substring(2).trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }
        return quality;
    }

    private String todoAsCalendar(final EntityInstance todo) {
        String id = todo.getPrimaryKeyValue();
        String title = calendarText(todoFieldValue(todo, "title"));
        String description = calendarText(todoFieldValue(todo, "description"));
        String status =
                Boolean.parseBoolean(todoFieldValue(todo, "doneStatus"))
                        ? "COMPLETED"
                        : "NEEDS-ACTION";

        return String.join(
                "\r\n",
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "PRODID:-//EvilTester//API Challenges//EN",
                "BEGIN:VTODO",
                "UID:todo-" + id + "@apichallenges",
                "SUMMARY:" + title,
                "DESCRIPTION:" + description,
                "STATUS:" + status,
                "END:VTODO",
                "END:VCALENDAR");
    }

    private String todoFieldValue(final EntityInstance todo, final String fieldName) {
        if (todo.getFieldValue(fieldName) == null) {
            return "";
        }
        return todo.getFieldValue(fieldName).asString();
    }

    private String calendarText(final String value) {
        String normalised = value == null ? "" : value.replace("\r\n", "\n").replace('\r', '\n');
        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < normalised.length(); index++) {
            char character = normalised.charAt(index);
            switch (character) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case ',':
                    escaped.append("\\,");
                    break;
                case ';':
                    escaped.append("\\;");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                default:
                    escaped.append(character);
                    break;
            }
        }
        return escaped.toString();
    }

    private HttpApiResponse httpResponseFor(
            final HttpApiRequest request,
            final ApiResponse apiResponse,
            final ThingifierApiConfig config,
            final String contentType,
            final ChallengerAuthData challenger) {

        apiResponse.setHeader("X-CHALLENGER", challenger.getXChallenger());
        HttpApiResponse httpResponse =
                new HttpApiResponse(
                        request.getHeaders(),
                        apiResponse,
                        new JsonThing(config.jsonOutput()),
                        config);
        httpResponse.getHeaders().put("Content-Type", contentType);
        httpResponse.getHeaders().put("X-CHALLENGER", challenger.getXChallenger());
        return httpResponse;
    }

    private EntityInstance findTodoByField(
            final ChallengerAuthData challenger, final String fieldName, final String fieldValue) {
        EntityDefinition todo = todoDefinition();
        EntityInstanceQuery query = queryFor(challenger);
        if (todo == null || query == null) {
            return null;
        }
        return query.findByField(todo, fieldName, fieldValue);
    }

    private boolean isSingleFieldAscendingSort(final List<SortCriterion> sortCriteria) {
        return sortCriteria.size() == 1
                && sortCriteria.get(0).ascending
                && isTodoField(sortCriteria.get(0).fieldName);
    }

    private boolean isSingleFieldDescendingSort(final List<SortCriterion> sortCriteria) {
        return sortCriteria.size() == 1
                && !sortCriteria.get(0).ascending
                && isTodoField(sortCriteria.get(0).fieldName);
    }

    private boolean isMultipleFieldSort(final List<SortCriterion> sortCriteria) {
        if (sortCriteria.size() < 2) {
            return false;
        }

        for (SortCriterion criterion : sortCriteria) {
            if (!isTodoField(criterion.fieldName)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasValidSort(final List<SortCriterion> sortCriteria) {
        if (sortCriteria.isEmpty()) {
            return false;
        }

        for (SortCriterion criterion : sortCriteria) {
            if (!isTodoField(criterion.fieldName)) {
                return false;
            }
        }

        return true;
    }

    private boolean isDescendingIdSort(final List<SortCriterion> sortCriteria) {
        return sortCriteria.size() == 1
                && sortCriteria.get(0).fieldName.equals("id")
                && !sortCriteria.get(0).ascending;
    }

    private boolean hasTodoFilter(final HttpApiRequest request) {
        for (FilterBy filterBy : request.getFilterableQueryParams().toList()) {
            if (!filterBy.fieldName.equals("_sortBy") && isTodoField(filterBy.fieldName)) {
                return true;
            }
        }

        for (String queryParamName : request.getQueryParams().keySet()) {
            if (!queryParamName.equals("_sortBy") && isTodoField(queryParamName)) {
                return true;
            }
        }

        return false;
    }

    private boolean queryParamEquals(
            final HttpApiRequest request, final String paramName, final String expectedValue) {
        return request.getQueryParams().containsKey(paramName)
                && request.getQueryParams().get(paramName).contentEquals(expectedValue);
    }

    private boolean queryParamIntegerEquals(
            final HttpApiRequest request, final String paramName, final int expectedValue) {
        Integer actualValue = queryParamInteger(request, paramName);
        return actualValue != null && actualValue == expectedValue;
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

    private boolean idFilterResponseIsProperSubset(
            final HttpApiRequest request,
            final List<JsonObject> responseTodos,
            final ChallengerAuthData challenger,
            final FilterOperation filterOperation) {

        FilterBy filterBy = filterBy(request, "id", filterOperation);
        Integer filterValue = filterIntegerValue(filterBy);
        if (filterValue == null || !isNonEmptyProperSubset(responseTodos, challenger)) {
            return false;
        }

        if (filterOperation == FilterOperation.GREATER_THAN) {
            return allTodoIdsGreaterThan(responseTodos, filterValue);
        }
        if (filterOperation == FilterOperation.LESS_THAN) {
            return allTodoIdsLessThan(responseTodos, filterValue);
        }
        return false;
    }

    private boolean idSingleResultFilterResponseMatches(
            final HttpApiRequest request,
            final List<JsonObject> responseTodos,
            final ChallengerAuthData challenger) {

        FilterBy filterBy = filterBy(request, "id", FilterOperation.EQUALS);
        Integer expectedId = filterIntegerValue(filterBy);
        if (expectedId == null || countTodos(challenger) <= 1 || responseTodos.size() != 1) {
            return false;
        }

        Integer actualId = todoId(responseTodos.get(0));
        return actualId != null && actualId.equals(expectedId);
    }

    private boolean descriptionRegexFilterResponseMatches(
            final HttpApiRequest request, final List<JsonObject> responseTodos) {

        FilterBy filterBy = filterBy(request, "description", FilterOperation.REGEX_MATCH);
        if (filterBy == null || responseTodos.isEmpty()) {
            return false;
        }

        final Pattern pattern;
        try {
            pattern = Pattern.compile(filterBy.fieldValue);
        } catch (PatternSyntaxException e) {
            return false;
        }

        for (JsonObject todo : responseTodos) {
            String description = todoDescription(todo);
            if (description.isEmpty() || !pattern.matcher(description).matches()) {
                return false;
            }
        }
        return true;
    }

    private boolean descriptionWildcardFilterResponseMatches(
            final HttpApiRequest request, final List<JsonObject> responseTodos) {

        FilterBy filterBy = filterBy(request, "description", FilterOperation.WILDCARD_MATCH);
        if (filterBy == null || responseTodos.isEmpty()) {
            return false;
        }

        Pattern wildcardPattern = Pattern.compile(wildcardPatternAsRegex(filterBy.fieldValue));
        for (JsonObject todo : responseTodos) {
            String description = todoDescription(todo);
            if (description.isEmpty() || !wildcardPattern.matcher(description).matches()) {
                return false;
            }
        }
        return true;
    }

    private FilterBy filterBy(
            final HttpApiRequest request,
            final String fieldName,
            final FilterOperation filterOperation) {
        for (FilterBy filterBy : request.getFilterableQueryParams().toList()) {
            if (filterBy.fieldName.equals(fieldName)
                    && filterBy.filterOperation == filterOperation) {
                return filterBy;
            }
        }
        return null;
    }

    private Integer filterIntegerValue(final FilterBy filterBy) {
        if (filterBy == null) {
            return null;
        }

        try {
            return Integer.parseInt(filterBy.fieldValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isNonEmptyProperSubset(
            final List<JsonObject> responseTodos, final ChallengerAuthData challenger) {
        int todoCount = countTodos(challenger);
        return !responseTodos.isEmpty() && todoCount > responseTodos.size();
    }

    private boolean allTodoIdsGreaterThan(final List<JsonObject> todos, final int value) {
        for (JsonObject todo : todos) {
            Integer id = todoId(todo);
            if (id == null || id <= value) {
                return false;
            }
        }
        return true;
    }

    private boolean allTodoIdsLessThan(final List<JsonObject> todos, final int value) {
        for (JsonObject todo : todos) {
            Integer id = todoId(todo);
            if (id == null || id >= value) {
                return false;
            }
        }
        return true;
    }

    private String todoDescription(final JsonObject todo) {
        try {
            JsonElement description = todo.get("description");
            if (description == null || description.isJsonNull()) {
                return "";
            }
            return description.getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    private String wildcardPatternAsRegex(final String wildcard) {
        StringBuilder regex = new StringBuilder();
        for (int index = 0; index < wildcard.length(); index++) {
            char character = wildcard.charAt(index);
            if (character == '*') {
                regex.append(".*");
            } else if (character == '?') {
                regex.append(".");
            } else {
                regex.append(Pattern.quote(String.valueOf(character)));
            }
        }
        return regex.toString();
    }

    private List<JsonObject> todosFromJsonResponse(final HttpApiResponse response) {
        List<JsonObject> todos = new ArrayList<>();
        String body = response.getBody();

        if (body == null || body.trim().isEmpty()) {
            return todos;
        }

        try {
            JsonElement parsed = JsonParser.parseString(body);
            if (!parsed.isJsonObject()) {
                return todos;
            }

            JsonElement todosElement = parsed.getAsJsonObject().get("todos");
            if (todosElement == null || !todosElement.isJsonArray()) {
                return todos;
            }

            JsonArray todosArray = todosElement.getAsJsonArray();
            for (JsonElement todoElement : todosArray) {
                if (todoElement.isJsonObject()) {
                    todos.add(todoElement.getAsJsonObject());
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing todos response body for pagination challenge", e);
        }

        return todos;
    }

    private boolean idsAreDescending(final List<JsonObject> todos) {
        for (int index = 1; index < todos.size(); index++) {
            Integer previousId = todoId(todos.get(index - 1));
            Integer currentId = todoId(todos.get(index));
            if (previousId == null || currentId == null || previousId < currentId) {
                return false;
            }
        }
        return true;
    }

    private Integer todoId(final JsonObject todo) {
        try {
            return todo.get("id").getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean allDoneStatusFalse(final List<JsonObject> todos) {
        for (JsonObject todo : todos) {
            try {
                if (todo.get("doneStatus").getAsBoolean()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private boolean allDoneStatusTrue(final List<JsonObject> todos) {
        for (JsonObject todo : todos) {
            try {
                if (!todo.get("doneStatus").getAsBoolean()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private int expectedPaginatedTodoCount(
            final ChallengerAuthData challenger, final int limit, final int offset) {
        return expectedPageSize(countTodos(challenger), limit, offset);
    }

    private int expectedFilteredPaginatedTodoCount(
            final ChallengerAuthData challenger,
            final String doneStatus,
            final int limit,
            final int offset) {
        return expectedPageSize(
                countTodosByField(challenger, "doneStatus", doneStatus), limit, offset);
    }

    private int expectedPageSize(final int availableCount, final int limit, final int offset) {
        if (availableCount < 0) {
            return -1;
        }
        return Math.max(0, Math.min(limit, availableCount - offset));
    }

    private boolean isTodoField(final String fieldName) {
        EntityDefinition todo = todoDefinition();
        return todo != null && todo.hasFieldNameDefined(fieldName);
    }

    private List<SortCriterion> sortCriteriaFrom(final HttpApiRequest request) {
        List<String> sortByValues = sortByValuesFrom(request);
        List<SortCriterion> criteria = new ArrayList<>();

        for (String sortByValue : sortByValues) {
            for (String rawCriterion : sortByValue.split(",")) {
                SortCriterion criterion = sortCriterionFrom(rawCriterion);
                if (criterion != null) {
                    criteria.add(criterion);
                }
            }
        }

        return criteria;
    }

    private List<String> sortByValuesFrom(final HttpApiRequest request) {
        List<String> values = new ArrayList<>();

        for (FilterBy filterBy : request.getFilterableQueryParams().toList()) {
            if (filterBy.fieldName.equals("_sortBy")) {
                values.add(filterBy.fieldValue);
            }
        }

        if (!values.isEmpty()) {
            return values;
        }

        if (request.getQueryParams().containsKey("_sortBy")) {
            values.add(request.getQueryParams().get("_sortBy"));
        }

        return values;
    }

    private SortCriterion sortCriterionFrom(final String rawCriterion) {
        String criterion = rawCriterion.trim();
        if (criterion.isEmpty()) {
            return null;
        }

        boolean ascending = true;
        if (criterion.startsWith("-")) {
            ascending = false;
            criterion = criterion.substring(1).trim();
        } else if (criterion.startsWith("+")) {
            criterion = criterion.substring(1).trim();
        }

        if (criterion.isEmpty()) {
            return null;
        }

        return new SortCriterion(criterion, ascending);
    }

    private boolean hasDoneAndNotDoneTodos(final ChallengerAuthData challenger) {
        final EntityInstance aDoneThing = findTodoByField(challenger, "doneStatus", "true");
        final EntityInstance aNotDoneThing = findTodoByField(challenger, "doneStatus", "false");
        return aDoneThing != null && aNotDoneThing != null;
    }

    private boolean queryBodyContainsDoneStatusTrue(final HttpApiRequest request) {
        final QueryFilterParams queryParams;
        try {
            queryParams = new UrlQueryParamParser().parseStrict(request.getBody());
        } catch (IllegalArgumentException e) {
            return false;
        }

        for (FilterBy filterBy : queryParams.toList()) {
            if (filterBy.fieldName.equals("doneStatus")
                    && filterBy.filterOperation == FilterOperation.EQUALS
                    && filterBy.fieldValue.equals("true")) {
                return true;
            }
        }
        return false;
    }

    private boolean queryJsonPathBodyTargetsDoneStatusTrue(final HttpApiRequest request) {
        final String body = request.getBody();
        if (body == null) {
            return false;
        }

        final String normalized = body.toLowerCase();
        return normalized.contains("donestatus") && normalized.contains("true");
    }

    private boolean queryStructuredJsonBodyTargetsDoneStatusTrue(final HttpApiRequest request) {
        final String body = request.getBody();
        if (body == null) {
            return false;
        }

        try {
            final JsonElement parsed = JsonParser.parseString(body);
            if (!parsed.isJsonObject()) {
                return false;
            }

            final JsonElement filter = parsed.getAsJsonObject().get("filter");
            if (filter == null || !filter.isJsonObject()) {
                return false;
            }

            final JsonElement doneStatus = filter.getAsJsonObject().get("doneStatus");
            return doneStatus != null
                    && doneStatus.isJsonPrimitive()
                    && doneStatus.getAsJsonPrimitive().isBoolean()
                    && doneStatus.getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean contentTypeIs(
            final ContentTypeHeaderParser contentTypeParser, final String expectedMediaType) {
        return contentTypeParser.mediaType() != null
                && contentTypeParser.mediaType().equalsIgnoreCase(expectedMediaType);
    }

    private EntityInstance findTodoByIdentifier(
            final ChallengerAuthData challenger, final String identifier) {
        EntityDefinition todo = todoDefinition();
        EntityInstanceQuery query = queryFor(challenger);
        if (todo == null || query == null) {
            return null;
        }
        return query.findByQueryIdentifier(todo, identifier);
    }

    private int countTodos(final ChallengerAuthData challenger) {
        EntityDefinition todo = todoDefinition();
        EntityInstanceQuery query = queryFor(challenger);
        if (todo == null || query == null) {
            return -1;
        }
        return query.count(todo);
    }

    private int countTodosByField(
            final ChallengerAuthData challenger, final String fieldName, final String fieldValue) {
        EntityDefinition todo = todoDefinition();
        EntityInstanceQuery query = queryFor(challenger);
        if (todo == null || query == null) {
            return -1;
        }

        int count = 0;
        for (EntityInstance todoInstance : query.list(todo)) {
            if (todoInstance.getFieldValue(fieldName) != null
                    && todoInstance.getFieldValue(fieldName).asString().equals(fieldValue)) {
                count++;
            }
        }
        return count;
    }

    private EntityInstanceQuery queryFor(final ChallengerAuthData challenger) {
        return thingifier.getStore(challenger.getXChallenger()).entityQueries();
    }

    private EntityDefinition todoDefinition() {
        return thingifier.getDefinitionNamed("todo");
    }

    String collate(Collection<String> strings) {
        StringBuilder collated = new StringBuilder();
        for (String string : strings) {
            collated.append(string);
            collated.append(" ");
        }
        return collated.toString();
    }

    private static class SortCriterion {
        private final String fieldName;
        private final boolean ascending;

        SortCriterion(final String fieldName, final boolean ascending) {
            this.fieldName = fieldName;
            this.ascending = ascending;
        }
    }
}
