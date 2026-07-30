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

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/accept-header/get-todos-406");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "QzfbegkY1ok");
        return aChallenge;
    }
}
