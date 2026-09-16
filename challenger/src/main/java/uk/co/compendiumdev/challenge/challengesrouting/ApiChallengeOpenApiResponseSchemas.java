package uk.co.compendiumdev.challenge.challengesrouting;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

final class ApiChallengeOpenApiResponseSchemas {

    private ApiChallengeOpenApiResponseSchemas() {}

    static ObjectSchema challengeList() {
        final ObjectSchema challenge = new ObjectSchema();
        challenge.addProperty("id", new IntegerSchema());
        challenge.addProperty("name", new StringSchema());
        challenge.addProperty("description", new StringSchema());
        challenge.addProperty("status", new BooleanSchema());

        final ArraySchema challenges = new ArraySchema();
        challenges.setItems(challenge);

        final ObjectSchema response = new ObjectSchema();
        response.addProperty("challenges", challenges);
        return response;
    }

    static ObjectSchema challenger() {
        final ObjectSchema challengeStatus = new ObjectSchema();
        challengeStatus.setAdditionalProperties(new BooleanSchema());

        final ObjectSchema response = new ObjectSchema();
        response.addProperty("xAuthToken", new StringSchema().format("uuid"));
        response.addProperty("xChallenger", new StringSchema().format("uuid"));
        response.addProperty("secretNote", new StringSchema());
        response.addProperty("challengeStatus", challengeStatus);
        return response;
    }

    static ObjectSchema todoCollection() {
        final ArraySchema todos = new ArraySchema();
        todos.setItems(todo());

        final ObjectSchema response = new ObjectSchema();
        response.addProperty("todos", todos);
        return response;
    }

    static ObjectSchema errorMessages() {
        final ArraySchema messages = new ArraySchema();
        messages.setItems(new StringSchema());

        final ObjectSchema response = new ObjectSchema();
        response.addProperty("errorMessages", messages);
        response.addRequiredItem("errorMessages");
        return response;
    }

    private static Schema<?> todo() {
        final ObjectSchema todo = new ObjectSchema();
        todo.addProperty("id", new IntegerSchema());
        todo.addProperty("title", new StringSchema());
        todo.addProperty("doneStatus", new BooleanSchema());
        todo.addProperty("description", new StringSchema());
        return todo;
    }
}
