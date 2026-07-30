package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class PatchChallenges {

    public static ChallengeDefinitionData patchTodosPartial200(int challengeOrder) {
        String patchLink = "<a href='/tutorials/http-verbs#toc18'>learn more about patch</a>";
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "PATCH /todos/{id} (200) partial",
                        "Issue a PATCH request to update an existing todo using a partial JSON payload. "
                                + patchLink
                                + ".");

        aChallenge.addHint("Learn more about PATCH.", "/tutorials/http-verbs#toc18");
        aChallenge.addHint("Use `Content-Type: application/json`.");
        aChallenge.addHint("Only include the fields you want to change.");
        aChallenge.addHint("Do not include an `id` in the payload.");
        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/patch/patch-todos-id-200-partial");
        return aChallenge;
    }

    public static ChallengeDefinitionData patchTodosMergePatch200(int challengeOrder) {
        String patchLink = "<a href='/tutorials/http-verbs#toc18'>learn more about patch</a>";
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "PATCH /todos/{id} (200) merge-patch",
                        "Issue a PATCH request to update an existing todo using JSON Merge Patch. "
                                + patchLink
                                + ".");

        aChallenge.addHint("Learn more about PATCH.", "/tutorials/http-verbs#toc18");
        aChallenge.addHint(
                "Use `Content-Type: application/merge-patch+json` for JSON Merge Patch.",
                "https://www.rfc-editor.org/rfc/rfc7396");
        aChallenge.addHint("Send an object containing the fields to add, replace, or remove.");
        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/patch/patch-todos-id-200-merge-patch");
        return aChallenge;
    }

    public static ChallengeDefinitionData patchTodosJsonPatch200(int challengeOrder) {
        String patchLink = "<a href='/tutorials/http-verbs#toc18'>learn more about patch</a>";
        ChallengeDefinitionData aChallenge =
                new ChallengeDefinitionData(
                        ChallengeRenderer.renderChallengeNumber(challengeOrder),
                        "PATCH /todos/{id} (200) json-patch",
                        "Issue a PATCH request to update an existing todo using JSON Patch operations. "
                                + patchLink
                                + ".");

        aChallenge.addHint("Learn more about PATCH.", "/tutorials/http-verbs#toc18");
        aChallenge.addHint(
                "Use `Content-Type: application/json-patch+json` for JSON Patch.",
                "https://www.rfc-editor.org/rfc/rfc6902");
        aChallenge.addHint(
                "Send an array of JSON Patch operations, e.g. a `replace` operation for `/title`.");
        aChallenge.addSolutionLink(
                "Read Solution",
                "HREF",
                "/apichallenges/solutions/patch/patch-todos-id-200-json-patch");
        return aChallenge;
    }
}
