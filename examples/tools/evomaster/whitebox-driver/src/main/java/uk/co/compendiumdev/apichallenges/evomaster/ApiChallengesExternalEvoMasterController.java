package uk.co.compendiumdev.apichallenges.evomaster;

import com.webfuzzing.commons.auth.Header;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.evomaster.client.java.controller.ExternalSutController;
import org.evomaster.client.java.controller.InstrumentedSutStarter;
import org.evomaster.client.java.controller.api.dto.SutInfoDto;
import org.evomaster.client.java.controller.api.dto.auth.AuthenticationDto;
import org.evomaster.client.java.controller.problem.ProblemInfo;
import org.evomaster.client.java.controller.problem.RestProblem;
import org.evomaster.client.java.sql.DbSpecification;

public class ApiChallengesExternalEvoMasterController extends ExternalSutController {

    private static final String DEFAULT_X_CHALLENGER = "rest-api-challenges-single-player";
    private static final String OPENAPI_PATH =
            "/api/docs/openapi-3.0.json?strongschema=true&pathparams=operation";

    private final int port;
    private final Path executableJar;
    private final boolean resetWithAdminClear;

    public ApiChallengesExternalEvoMasterController() {
        this(null);
    }

    public ApiChallengesExternalEvoMasterController(String jarPath) {
        this.port = Integer.getInteger("apichallenges.port", 4567);
        this.executableJar =
                configuredJarPath(jarPath).toAbsolutePath().normalize();
        this.resetWithAdminClear = Boolean.getBoolean("apichallenges.resetWithAdminClear");

        setNeedsJdk17Options(true);

        String javaCommand = System.getProperty("apichallenges.java");
        if (javaCommand != null && !javaCommand.isBlank()) {
            setJavaCommand(javaCommand);
        }
    }

    @Override
    public String[] getInputParameters() {
        List<String> args = new ArrayList<>();
        args.add("-port=" + port);
        args.add("-memory");
        args.add("-sim-sqlite-memory");
        args.add("-noshutdown");
        if (resetWithAdminClear) {
            args.add("-enableadminapi");
        }
        return args.toArray(String[]::new);
    }

    @Override
    public String[] getJVMParameters() {
        return new String[0];
    }

    @Override
    public String getBaseURL() {
        return "http://localhost:" + port;
    }

    @Override
    public String getPathToExecutableJar() {
        return executableJar.toString();
    }

    @Override
    public String getLogMessageOfInitializedServer() {
        return "Started";
    }

    @Override
    public Boolean isSUTInitialized() {
        try {
            URL url = URI.create(getBaseURL() + "/api/heartbeat").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            int status = connection.getResponseCode();
            return status >= 200 && status < 500;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public long getMaxAwaitForInitializationInSeconds() {
        return 60;
    }

    @Override
    public void preStart() {
        if (!Files.exists(executableJar)) {
            throw new IllegalStateException("API Challenges jar does not exist: " + executableJar);
        }
    }

    @Override
    public void postStart() {
        System.out.println("API Challenges started for EvoMaster at " + getBaseURL());
        System.out.println("OpenAPI: " + getBaseURL() + OPENAPI_PATH);
    }

    @Override
    public void preStop() {
        // No external resources to stop before the jar process is destroyed.
    }

    @Override
    public void postStop() {
        // No external resources to stop after the jar process is destroyed.
    }

    @Override
    public void resetStateOfSUT() {
        if (!resetWithAdminClear) {
            return;
        }

        try {
            URL url = URI.create(getBaseURL() + "/admin/data/thingifier").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(new byte[0]);
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Admin reset returned HTTP " + status);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not reset API Challenges through admin clear", e);
        }
    }

    @Override
    public List<DbSpecification> getDbSpecifications() {
        return null;
    }

    @Override
    public String getPackagePrefixesToCover() {
        return "uk.co.compendiumdev.challenge,uk.co.compendiumdev.thingifier";
    }

    @Override
    public List<AuthenticationDto> getInfoForAuthentication() {
        AuthenticationDto authentication = new AuthenticationDto("single-player");
        Header header = new Header();
        header.setName("X-CHALLENGER");
        header.setValue(System.getProperty("apichallenges.xchallenger", DEFAULT_X_CHALLENGER));
        authentication.setFixedHeaders(List.of(header));
        return List.of(authentication);
    }

    @Override
    public ProblemInfo getProblemInfo() {
        return new RestProblem(getBaseURL() + OPENAPI_PATH, Collections.emptyList());
    }

    @Override
    public SutInfoDto.OutputFormat getPreferredOutputFormat() {
        return SutInfoDto.OutputFormat.JAVA_JUNIT_5;
    }

    private static Path configuredJarPath(String constructorJarPath) {
        if (constructorJarPath != null && !constructorJarPath.isBlank()) {
            return Paths.get(constructorJarPath);
        }

        String configured = System.getProperty("apichallenges.jar");
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured);
        }

        List<Path> candidates = List.of(
                Paths.get("challenger", "target", "apichallenges.jar"),
                Paths.get("..", "..", "..", "..", "challenger", "target", "apichallenges.jar"));

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        return candidates.get(0);
    }

    public static void main(String[] args) {
        ApiChallengesExternalEvoMasterController controller =
                new ApiChallengesExternalEvoMasterController();
        controller.setControllerPort(Integer.getInteger("evomaster.controller.port", 40100));
        new InstrumentedSutStarter(controller).start();
    }
}
