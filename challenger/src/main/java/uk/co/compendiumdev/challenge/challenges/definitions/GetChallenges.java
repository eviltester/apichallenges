package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class GetChallenges {

    public static ChallengeDefinitionData getChallenges200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /challenges (200)",
                        "Issue a GET request on the `/challenges` end point");

        aChallenge.addHint(
                "Remember to add the X-CHALLENGER header so you see the progress of the challenges for your session.",
                "");
        aChallenge.addHint(
                "If you issue a GET request without an X-CHALLENGER header you will see the default challenge values.",
                "");
        aChallenge.addHint("By default the response body will be JSON format.", "");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/first-challenge/get-challenges-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "DrAjk2NaPRo");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodos200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200)",
                        "Issue a GET request on the `/todos` end point");

        aChallenge.addHint(
                "Remember to add the X-CHALLENGER header so you see the data for your session.",
                "");
        aChallenge.addHint(
                "If you issue a GET request without an X-CHALLENGER header you will see the default todo values.",
                "");
        aChallenge.addHint("By default the response body will be JSON format.", "");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/get/get-todos-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "OpisB0UZq0c");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodos404(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todo (404) not plural",
                        "Issue a GET request on the `/todo` end point should 404 because nouns should be plural");

        aChallenge.addHint("Use the singular `/todo` endpoint, not `/todos`.");
        aChallenge.addHint(
                "No todo id is needed for this request; the challenge is the 404 route.");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/get/get-todo-404");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "gAJzqgcN9dc");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodo200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos/{id} (200)",
                        "Issue a GET request on the `/todos/{id}` end point to return a specific todo");

        aChallenge.addHint(
                "Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/get/get-todos-id-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "JDbbSY3U_rY");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodo404(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos/{id} (404)",
                        "Issue a GET request on the `/todos/{id}` end point for a todo that does not exist");

        aChallenge.addHint(
                "Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint("Make sure the id is an integer e.g. /todos/1");
        aChallenge.addHint("Make sure you are using the /todos end point e.g. /todos/1");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/get/get-todos-id-404");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "1S5kpd8-xfM");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosFiltered200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?filter",
                        "Issue a GET request on the `/todos` end point with a query filter to get only todos which are 'done'. There must exist both 'done' and 'not done' todos, to pass this challenge.");

        aChallenge.addHint("A query filter is a URL parameter using the field name and a value");
        aChallenge.addHint(
                "A URL parameter is added to the end of a url with a ? e.g. /todos?id=1");
        aChallenge.addHint(
                "To filter on 'done' we use the 'doneStatus' field  ? e.g. ?doneStatus=true");
        aChallenge.addHint("Make sure there are todos which are done, and not yet done");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/get/get-todos-200-filter");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "G-sLuhyPMuw");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosFilteredIdGreaterThan200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?filter id greater than",
                        "Issue a GET request on the `/todos` end point with an id filter to return todos with an id greater than a supplied value.");

        aChallenge.addHint("Use the `id` field with the greater than operator.");
        aChallenge.addHint("For example, `?id>5` returns todos with an id greater than 5.");
        aChallenge.addHint("Make sure the filter returns at least one todo, but not all todos.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-filter-id-greater-than");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosFilteredIdLessThan200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?filter id less than",
                        "Issue a GET request on the `/todos` end point with an id filter to return todos with an id less than a supplied value.");

        aChallenge.addHint("Use the `id` field with the less than operator.");
        aChallenge.addHint("For example, `?id<6` returns todos with an id less than 6.");
        aChallenge.addHint("Make sure the filter returns at least one todo, but not all todos.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-filter-id-less-than");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosFilteredIdSingleResult200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?filter id single result",
                        "Issue a GET request on the `/todos` end point with an id filter that returns one todo while multiple todos exist in the database.");

        aChallenge.addHint("Use the `id` field with an exact value.");
        aChallenge.addHint("For example, `?id=3` returns the todo with id 3.");
        aChallenge.addHint("Make sure there is more than one todo in the database.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-filter-id-single-result");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosFilteredDescriptionRegex200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?filter description regex",
                        "Issue a GET request on the `/todos` end point with a regular expression filter on description that returns todos with non-empty descriptions.");

        aChallenge.addHint(
                "Use the `description` field with the regular expression operator `~=`.");
        aChallenge.addHint(
                "For example, `?description~=.*fixture.*` returns descriptions that match the regular expression.");
        aChallenge.addHint("Create a todo with a non-empty description if you need matching data.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-filter-description-regex");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosFilteredDescriptionWildcard200(
            int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?filter description wildcard",
                        "Issue a GET request on the `/todos` end point with a wildcard filter on description that returns todos with non-empty descriptions.");

        aChallenge.addHint("Use the `description` field with the wildcard operator `*=`.");
        aChallenge.addHint(
                "For example, `?description*=*fixture*` matches descriptions containing fixture.");
        aChallenge.addHint("Create a todo with a non-empty description if you need matching data.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-filter-description-wildcard");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosSortedAscending200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?_sortBy ascending",
                        "Issue a GET request on the `/todos` end point with a query parameter to sort todos ascending by a field.");

        aChallenge.addHint("Sorting is controlled by the `_sortBy` URL parameter.");
        aChallenge.addHint("Use a field name to sort ascending, e.g. `?_sortBy=title`.");
        aChallenge.addHint(
                "You can also prefix the field with `+` for ascending, e.g. `?_sortBy=+title`.");
        aChallenge.addHint("Make sure you sort by a field that exists on a todo.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-sort-ascending");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosSortedDescending200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?_sortBy descending",
                        "Issue a GET request on the `/todos` end point with a query parameter to sort todos descending by a field.");

        aChallenge.addHint("Sorting is controlled by the `_sortBy` URL parameter.");
        aChallenge.addHint("Prefix a field name with `-` to sort descending.");
        aChallenge.addHint("For example, use `?_sortBy=-id` to sort by id descending.");
        aChallenge.addHint("Make sure you sort by a field that exists on a todo.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-sort-descending");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosSortedMultipleFields200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?_sortBy multiple",
                        "Issue a GET request on the `/todos` end point with a query parameter to sort todos by multiple fields.");

        aChallenge.addHint("Sorting is controlled by the `_sortBy` URL parameter.");
        aChallenge.addHint("Separate multiple sort fields with commas.");
        aChallenge.addHint(
                "For example, `?_sortBy=+doneStatus,-id` sorts by doneStatus, then id descending.");
        aChallenge.addHint("Make sure every sort field exists on a todo.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-sort-multiple");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosFilteredAndSorted200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?filter&_sortBy",
                        "Issue a GET request on the `/todos` end point with a query filter and a query parameter to sort the filtered todos.");

        aChallenge.addHint("Use a todo field as a URL parameter to filter the collection.");
        aChallenge.addHint("Use `_sortBy` to sort the filtered results.");
        aChallenge.addHint(
                "For example, `?doneStatus=false&_sortBy=-id` filters not done todos and sorts them by id descending.");
        aChallenge.addHint("Make sure the filter field and sort field both exist on a todo.");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/get/get-todos-200-filter-sort");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosPaginatedLimit200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?_limit",
                        "Issue a GET request on the `/todos` end point with a query parameter to limit the returned todos to 8 items.");

        aChallenge.addHint(
                "Pagination is controlled by the `_limit` and `_offset` URL parameters.");
        aChallenge.addHint("Use `_limit=8` to return at most 8 todos.");
        aChallenge.addHint(
                "Send an `Accept: application/json` header so you can inspect the returned collection.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-pagination-limit");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosPaginatedLimitOffset200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?_limit&_offset",
                        "Issue a GET request on the `/todos` end point with query parameters to limit the returned todos to 5 items starting from offset 5.");

        aChallenge.addHint("Use `_limit=5` to set the page size.");
        aChallenge.addHint("Use `_offset=5` to skip the first 5 todos.");
        aChallenge.addHint("The default `_offset` is 0 when it is not supplied.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-pagination-limit-offset");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosPaginatedLimitTooHigh400(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (400) ?_limit too high",
                        "Issue a GET request on the `/todos` end point with a pagination limit above the configured maximum to receive a 400 status code.");

        aChallenge.addHint("The configured maximum `_limit` for todos is 20.");
        aChallenge.addHint("Use `_limit=21` to request more than the maximum page size.");
        aChallenge.addHint("The API should reject a pagination limit that is too high.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-400-pagination-limit-too-high");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosPaginatedAndSorted200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?_sortBy&_limit&_offset",
                        "Issue a GET request on the `/todos` end point with query parameters to sort todos by id descending, then return a page of 5 todos from offset 5.");

        aChallenge.addHint("Use `_sortBy=-id` to sort todos by id descending.");
        aChallenge.addHint(
                "Use `_limit=5&_offset=5` to request the second page of 5 sorted todos.");
        aChallenge.addHint("Sorting should be applied before pagination.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-pagination-sort");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosPaginatedAndFiltered200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ?filter&_limit&_offset",
                        "Issue a GET request on the `/todos` end point with query parameters to filter todos with doneStatus=false, then return a page of 2 todos from offset 1.");

        aChallenge.addHint("Use `doneStatus=false` to filter the collection.");
        aChallenge.addHint(
                "Use `_limit=2&_offset=1` to request 2 filtered todos after skipping the first match.");
        aChallenge.addHint("Filtering should be applied before pagination.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/get/get-todos-200-pagination-filter");
        return aChallenge;
    }

    /*
       ACCEPT HEADERS
    */
    // GET accept type
    //      specify accept type - XML
    //      specify accept type - JSON
    //      specify accept type - */* (ANY) to get default
    //      specify multiple accept type with a preference for XML - should receive XML
    //      none specified - get default
    //      cannot supply accepted type 406

    public static ChallengeDefinitionData getTodosAcceptXML200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) XML",
                        "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml` to receive results in XML format");

        aChallenge.addHint("Set the `Accept` header to `application/xml`.");
        aChallenge.addHint("The request path is still `/todos`; only the response format changes.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/accept-header/get-todos-200-xml");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "cLeEuZm2VG8");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosAcceptJson200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) JSON",
                        "Issue a GET request on the `/todos` end point with an `Accept` header of `application/json` to receive results in JSON format");

        aChallenge.addHint("Set the `Accept` header to `application/json`.");
        aChallenge.addHint("Use a GET request; there is no request body for this challenge.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/accept-header/get-todos-200-json");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "79JTHiby2Qw");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosAcceptAny200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) ANY",
                        "Issue a GET request on the `/todos` end point with an `Accept` header of `*/*` to receive results in default JSON format");

        aChallenge.addHint("Set the `Accept` header to `*/*`.");
        aChallenge.addHint("The API default response format for `/todos` is JSON.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/accept-header/get-todos-200-any");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "O4DhJ8Ohkk8");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosPreferAcceptXML200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) XML pref",
                        "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml, application/json` to receive results in the preferred XML format");

        aChallenge.addHint("Send both media types in the `Accept` header.");
        aChallenge.addHint("Put `application/xml` before `application/json` to prefer XML.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/accept-header/get-todos-200-xml-pref");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "sLChuy9pc9U");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosNoAccept200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (200) no accept",
                        "Issue a GET request on the `/todos` end point with no `Accept` header present in the message to receive results in default JSON format");

        aChallenge.addHint("Remove the `Accept` header from the request.");
        aChallenge.addHint(
                "Some API clients add an `Accept` header automatically, so check the raw request.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/accept-header/get-todos-200-no-accept");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "CSVP2PcvOdg");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosUnavailableAccept406(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos (406)",
                        "Issue a GET request on the `/todos` end point with an `Accept` header `application/gzip` to receive 406 'NOT ACCEPTABLE' status code");

        aChallenge.addHint("Set the `Accept` header to a response type the API does not support.");
        aChallenge.addHint("e.g. `application/gzip` could trigger a 406 response.");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/accept-header/get-todos-406");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "QzfbegkY1ok");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodoAcceptTextCalendar200(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos/{id} (200) text/calendar",
                        "Issue a GET request on the `/todos/{id}` end point with an `Accept` header of `text/calendar` to receive the todo as a VTODO.");

        aChallenge.addHint("Use an id for a todo that already exists.");
        aChallenge.addHint("Set the `Accept` header to `text/calendar`.");
        aChallenge.addHint("The request is for one todo instance, e.g. `/todos/1`, not `/todos`.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/accept-header/get-todos-id-200-calendar");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosExportCsvContentDisposition200(
            int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos/export (200) CSV download",
                        "Issue a GET request on the `/todos/export?format=csv` end point and receive a CSV response with a `Content-Disposition` header for `todos.csv`");

        aChallenge.addHint("Use the `format=csv` query parameter.");
        aChallenge.addHint("Check the response has `Content-Disposition: attachment`.");
        aChallenge.addHint("The filename should be `todos.csv`.");
        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/content-disposition-header/get-todos-export-csv");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosExportHtmlContentDisposition200(
            int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos/export (200) HTML download",
                        "Issue a GET request on the `/todos/export?format=html` end point and receive an HTML response with a `Content-Disposition` header for `todos.html`");

        aChallenge.addHint("Use the `format=html` query parameter.");
        aChallenge.addHint("Check the response has `Content-Disposition: attachment`.");
        aChallenge.addHint("The filename should be `todos.html`.");
        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/content-disposition-header/get-todos-export-html");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosExportTsvContentDisposition200(
            int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /todos/export (200) tab-delimited download",
                        "Issue a GET request on the `/todos/export?format=tsv` end point and receive a tab-delimited response with a `Content-Disposition` header for `todos.tsv`");

        aChallenge.addHint("Use the `format=tsv` or `format=tab-delimited` query parameter.");
        aChallenge.addHint("Check the response has `Content-Disposition: attachment`.");
        aChallenge.addHint("The filename should be `todos.tsv`.");
        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/content-disposition-header/get-todos-export-tsv");

        return aChallenge;
    }
}
