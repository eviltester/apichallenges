package uk.co.compendiumdev.challenger.restassured._12_contenttype_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Challenge;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

public class C034PostTodosVendorXmlTest extends RestAssuredBaseTest {

    @Test
    void canCreateATodoWithVendorXmlPost() {
        final String title = "vendor xml " + System.currentTimeMillis();
        final String description = "created from vendor XML";
        final String payload =
                String.format(
                        "<todo><title>%s</title><doneStatus>true</doneStatus><description>%s</description></todo>",
                        title, description);

        final Response response =
                RestAssured.given()
                        .header("X-CHALLENGER", xChallenger)
                        .accept("application/json")
                        .contentType("application/vnd.apichallenges.todo+xml")
                        .body(payload)
                        .post(apiPath("/todos"))
                        .then()
                        .statusCode(201)
                        .contentType(ContentType.JSON)
                        .extract()
                        .response();

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Challenge challenge = statuses.getChallengeNamed("POST /todos vendor XML");
        Assertions.assertNotNull(challenge);
        Assertions.assertTrue(challenge.status);

        String locationHeader = response.getHeader("Location");
        Pattern getId = Pattern.compile("/todos/(.*)");
        Matcher matcher = getId.matcher(locationHeader);
        Assertions.assertTrue(matcher.find());

        final TodosApi api = new TodosApi();
        final Todo created = api.getTodo(matcher.group(1));

        Assertions.assertEquals(title, created.title);
        Assertions.assertEquals(description, created.description);
        Assertions.assertTrue(created.doneStatus);
    }
}
