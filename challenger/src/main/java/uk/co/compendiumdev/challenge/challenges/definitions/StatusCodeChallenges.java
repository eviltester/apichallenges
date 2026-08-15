package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class StatusCodeChallenges {

    // extra status code challenges
    //      method not allowed - 405
    public static ChallengeDefinitionData methodNotAllowed405UsingDelete(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "DELETE /api/heartbeat (405)",
                        "Issue a DELETE request on the `/api/heartbeat` end point and receive 405 (Method Not Allowed)");

        aChallenge.addHint("Use the DELETE method on `/api/heartbeat`.");
        aChallenge.addHint("The endpoint exists, but DELETE is not allowed for it.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/status-codes/delete-heartbeat-405");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    // cannot process request server error 500
    public static ChallengeDefinitionData serverError500UsingPatch(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "PATCH /api/heartbeat (500)",
                        "Issue a PATCH request on the `/api/heartbeat` end point and receive 500 (internal server error)");

        aChallenge.addHint("Use the PATCH method on `/api/heartbeat`.");
        aChallenge.addHint("This endpoint deliberately returns 500 for PATCH requests.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/status-codes/patch-heartbeat-500");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    // 501
    public static ChallengeDefinitionData notImplemented501UsingTrace(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "TRACE /api/heartbeat (501)",
                        "Issue a TRACE request on the `/api/heartbeat` end point and receive 501 (Not Implemented)");

        aChallenge.addHint("Use the TRACE method on `/api/heartbeat`.");
        aChallenge.addHint(
                "If your API client cannot send TRACE, use one that supports custom methods.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/status-codes/trace-heartbeat-501");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    // No Content 204 - ping
    public static ChallengeDefinitionData noContent204UsingGet(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /api/heartbeat (204)",
                        "Issue a GET request on the `/api/heartbeat` end point and receive 204 when server is running");

        aChallenge.addHint("Use the GET method on `/api/heartbeat`.");
        aChallenge.addHint("A 204 response means success with no response body.");

        aChallenge.addSolutionLink(
                "Read Solution", "HREF", "/apichallenges/solutions/status-codes/get-heartbeat-204");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    public static ChallengeDefinitionData xChallengerTooLong431(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "GET /api/heartbeat (431) X-CHALLENGER too long",
                        "Issue a GET request on the `/api/heartbeat` end point with an X-CHALLENGER header value that is too long and receive 431 (Request Header Fields Too Large).");
        aChallenge.addHint(
                "Start the oversized X-CHALLENGER value with your real challenger GUID, then append extra characters.");
        aChallenge.addHint(
                "The header must be longer than 100 characters to trigger the 431 response.");

        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/status-codes/x-challenger-too-long-431");

        return aChallenge;
    }

    /*
       Status codes using method overrides
    */
    public static ChallengeDefinitionData overridePostToPatchFor500(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "POST /api/heartbeat as PATCH (500)",
                        "Issue a POST request on the `/api/heartbeat` end point and receive 500 when you override the Method Verb to a PATCH");

        aChallenge.addHint("Use a normal POST request, but add an X-HTTP-Method-Override header");

        aChallenge.addSolutionLink(
                "Add a header 'X-HTTP-Method-Override: PATCH' to a POST /api/heartbeat request",
                "",
                "");
        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/method-override/post-heartbeat-as-patch-500");
        // aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    public static ChallengeDefinitionData overridePostToDeleteFor405(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "POST /api/heartbeat as DELETE (405)",
                        "Issue a POST request on the `/api/heartbeat` end point and receive 405 when you override the Method Verb to a DELETE");

        aChallenge.addHint("Use a normal POST request, but add an X-HTTP-Method-Override header");

        aChallenge.addSolutionLink(
                "Add a header 'X-HTTP-Method-Override: DELETE' to a POST /api/heartbeat request",
                "",
                "");
        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/method-override/post-heartbeat-as-delete-405");
        // aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    // 501
    public static ChallengeDefinitionData overridePostToTraceFor501(int challengeOrder) {
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "POST /api/heartbeat as Trace (501)",
                        "Issue a POST request on the `/api/heartbeat` end point and receive 501 (Not Implemented) when you override the Method Verb to a TRACE");
        aChallenge.addHint("Use a normal POST request, but add an X-HTTP-Method-Override header");

        aChallenge.addSolutionLink(
                "Add a header 'X-HTTP-Method-Override: TRACE' to a POST /api/heartbeat request",
                "",
                "");
        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/method-override/post-heartbeat-as-trace-501");

        //        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }
}
