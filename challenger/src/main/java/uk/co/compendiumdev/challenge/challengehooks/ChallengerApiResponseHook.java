package uk.co.compendiumdev.challenge.challengehooks;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;

public class ChallengerApiResponseHook implements HttpApiResponseHook {

    private final Challengers challengers;
    private final Thingifier thingifier;

    public ChallengerApiResponseHook(final Challengers challengers, final Thingifier thingifier) {
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

    private EntityInstance findTodoByIdentifier(
            final ChallengerAuthData challenger, final String identifier) {
        EntityDefinition todo = todoDefinition();
        EntityInstanceQuery query = queryFor(challenger);
        if (todo == null || query == null) {
            return null;
        }
        return query.findByQueryIdentifier(todo, identifier);
    }

    private EntityInstanceQuery queryFor(final ChallengerAuthData challenger) {
        return thingifier.getStore(challenger.getXChallenger()).entityQueries();
    }

    private EntityDefinition todoDefinition() {
        return thingifier.getDefinitionNamed("todo");
    }
}
