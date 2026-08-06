package uk.co.compendiumdev.challenger.restassured.api;

import io.restassured.RestAssured;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import uk.co.compendiumdev.serverstart.Environment;

public class RestAssuredBaseTest {

    static String environment = "";
    public static String xChallenger = "";
    private static final Set<String> HEAD_GET_HEADER_COMPARISON_SKIP =
            Set.of(
                    "connection",
                    "content-encoding",
                    "content-length",
                    "date",
                    "report-to",
                    "reporting-endpoints",
                    "transfer-encoding",
                    "x-cache",
                    "x-hikari-trace",
                    "x-railway-cdn-edge",
                    "x-railway-httpserverrequest-id",
                    "x-railway-request-id",
                    "x-served-by");

    @BeforeAll
    static void enableEnv() {
        Assumptions.assumeTrue(Environment.shouldRunFullSuite(), Environment.fullSuiteSkipReason());

        environment = Environment.getBaseUri();

        // switch on logging for RestAssured requests
        //        RestAssured.filters(
        //                new RequestLoggingFilter(),
        //                new ResponseLoggingFilter());

        // RestAssured.proxy("localhost",8888);

        if (xChallenger.isEmpty()) {
            xChallenger =
                    RestAssured.given()
                            .post(Environment.getEnv("/challenger"))
                            .then()
                            .statusCode(201)
                            .extract()
                            .header("X-CHALLENGER");
        }
    }

    public String apiPath(final String path) {
        return environment + path;
    }

    protected static boolean shouldSkipHeadGetHeaderComparison(final String headerName) {
        return HEAD_GET_HEADER_COMPARISON_SKIP.contains(
                headerName.toLowerCase(Locale.ROOT));
    }
}
