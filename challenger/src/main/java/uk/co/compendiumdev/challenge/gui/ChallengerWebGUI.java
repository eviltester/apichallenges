package uk.co.compendiumdev.challenge.gui;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.AssetVersion;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.*;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.challenge.persistence.PersistenceResponse;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class ChallengerWebGUI {
    private static final LocalDate SEO_FIXED_LASTMOD = LocalDate.parse("2026-02-18");
    private static final String CANONICAL_HOST = "https://apichallenges.com";
    private static final String LEGACY_HOST = "apichallenges.eviltester.com";
    private static final String WWW_HOST = "www.apichallenges.com";
    private static final String LEGACY_CANONICAL_HOST = "https://" + LEGACY_HOST;
    private static final String API_CHALLENGE_ALLOWED_PATH_PREFIXES =
            "/todos||/todo||/challenges||/challenger||/secret||/heartbeat";
    private static final Gson GSON = new Gson();

    private final PageNotFoundResponse pageNotFoundHtmlResponse;
    Logger logger = LoggerFactory.getLogger(ChallengerWebGUI.class);
    private final DefaultGUIHTML guiManagement;
    private final boolean guiStayAlive;
    private PersistenceLayer persistenceLayer;

    public ChallengerWebGUI(final DefaultGUIHTML defaultGui, final boolean guiStayAlive) {
        this.guiManagement = defaultGui;
        this.guiManagement.setCanonicalHost(CANONICAL_HOST);
        this.guiStayAlive = guiStayAlive;
        this.pageNotFoundHtmlResponse = new PageNotFoundResponse(guiManagement);
    }

    String getChallengesPageHtmlHeader() {
        return guiManagement.getPageStart(
                "API Challenges - Improve your API Skills",
                "<script src='"
                        + AssetVersion.versionedPath("/js/challengerui.js")
                        + "'></script>"
                        + "<script src='"
                        + AssetVersion.versionedPath("/js/api-live-request.js")
                        + "' defer></script>"
                        + "<meta name='description' content='A free online set of gamified REST API Challenges to practice and improve your API Testing Skills'>",
                "/gui/challenges");
    }

    String getSponsorMessage() {
        String sponsorMessage =
                """
            <div class='sponsor-top'>
            <p>
                Support this site by joining our Patreon.
                For as little as $1 a month you receive exclusive ad-free content,
                ebooks and online training courses. -
                <a href='https://patreon.com/eviltester' target='_blank'>Learn more</a>
            </p>
            </div>
                """;

        return sponsorMessage;
    }

    public void setup(
            final Challengers challengers,
            final ChallengeDefinitions challengeDefinitions,
            final PersistenceLayer persistenceLayer,
            final boolean single_player_mode) {

        this.persistenceLayer = persistenceLayer;

        guiManagement.appendMenuItem("Home", "/");
        guiManagement.appendMenuItem("Entities Explorer", "/gui/entities");
        guiManagement.appendMenuItem("Challenges", "/gui/challenges");
        guiManagement.appendMenuItem("API documentation", "/docs");
        guiManagement.appendMenuItem("Learning", "/learning");

        String actualMenu =
                """
                <script>
                    function setMenuNavBasedOnUrl(){
                        const pathToCheck = window.location.pathname;
                        urlMapping = [
                            ['/simpleapi/', 'simple-api-root-menu'],
                            ['/practice-modes/simpleapi', 'simple-api-root-menu'],
                            ['/shop/', 'shop-api-root-menu'],
                            ['/practice-modes/shoppingcart', 'shop-api-root-menu'],
                            ['/gui/', 'api-challenges-root-menu'],
                            ['/docs', 'api-challenges-root-menu'],
                            ['/apichallenges', 'api-challenges-root-menu'],
                            ['/sim/docs', 'sim-api-root-menu'],
                            ['/practice-modes/simulation', 'sim-api-root-menu'],
                            ['/mirror/', 'mirror-api-root-menu'],
                            ['/practice-modes/mirror', 'mirror-api-root-menu'],
                            ['/blog', 'blog-root-menu']
                        ];
                        foundMapping = false;
                        for(const mapping of urlMapping){
                            if(pathToCheck.startsWith(mapping[0])){
                                document.getElementById(mapping[1]).classList.add('dropped');
                                foundMapping=true;
                                break;
                            }
                        }
                        if(!foundMapping && pathToCheck=='/'){
                            document.getElementById('home-root-menu').classList.add('dropped');
                            foundMapping=true;
                        }
                        if(!foundMapping){
                            // assume it is a learning page
                            document.getElementById('learning-root-menu').classList.add('dropped');
                        }
                    }
                    document.addEventListener("DOMContentLoaded", setMenuNavBasedOnUrl);
                </script>
                <div class="container cssmenu">
                <nav aria-label="Site menu">
                    <div class="css-menu">
                        <ul class="sub-menu">
                            <li id='home-root-menu'><a class="brand-link" href="/">Home</a></li>

                            <li id='learning-root-menu'><a href="/learning">Learning Zone</a>
                                <ul>
                                    <li><a href="/tutorials/rest-api-tutorial">REST API Tutorial</a></li>
                                    <li><a href="/reference/http-basics">HTTP Basics</a></li>
                                    <li><a href="/reference/rest-api-basics">REST API Basics</a></li>
                                    <li><a href="/reference/http-verbs">HTTP Methods</a></li>
                                    <li><a href="/reference/http-basics#toc7">HTTP Status Codes</a></li>
                                    <li><a href="/reference/openapi">OpenAPI</a></li>
                                    <li><a href="/tutorials/rest-api-testing">How to Test REST APIs</a></li>
                                </ul>
                            </li>


                            <li id='sim-api-root-menu'><a href="/practice-modes/simulation">API Simulator</a>
                                <ul>
                                    <li><a href="/practice-modes/simulation">About API Simulator</a></li>
                                    <li><a href="/sim/docs">API Docs</a></li>
                                    <li><a href="/sim/docs/swagger-ui">Swagger UI</a></li>
                                    <li><a href="/practice-modes/simulation-openapi">OpenAPI File</a></li>

                                </ul>
                            </li>

                            <li id='api-challenges-root-menu'><a href="/gui/challenges">API Challenges</a>
                                    <ul>
                                        <li><a href="/apichallenges">About API Challenges</a></li>
                                        <li><a href="/docs">API Docs</a></li>
                                        <li><a href="/docs/swagger-ui">Swagger UI</a></li>
                                        <li><a href="/apichallenges/client">API Client</a></li>
                                        <li><a href="/gui/challenges">Progress</a></li>
                                        <li><a href="/gui/entities">Data Explorer</a></li>
                                        <li><a href="/apichallenges/solutions">Solutions</a></li>
                                        <li><a href="/apichallenges/openapi">OpenAPI File</a>
                                    </ul>
                                </li>

                                <li id='simple-api-root-menu'><a href="/practice-modes/simpleapi">Simple API</a>
                                    <ul>
                                        <li><a href="/practice-modes/simpleapi">About Simple API</a>
                                        <li><a href="/simpleapi/docs">API Docs</a>
                                        <li><a href="/simpleapi/docs/swagger-ui">Swagger UI</a>
                                        <li><a href="/simpleapi/client">API Client</a></li>
                                        <li><a href="/simpleapi/gui/entities">Data Explorer</a></li>
                                        <li><a href="/practice-modes/simpleapi-openapi">OpenAPI File</a>
                                    </ul>
                                </li>

                                <li id='shop-api-root-menu'><a href="/practice-modes/shoppingcart">Buggy API</a>
                                    <ul>
                                        <li><a href="/practice-modes/shoppingcart">About Buggy API</a>
                                        <li><a href="/shop/docs">API Docs</a>
                                        <li><a href="/shop/docs/swagger-ui">Swagger UI</a>
                                        <li><a href="/shop/client">API Client</a></li>
                                        <li><a href="/shop/gui/entities">Data Explorer</a></li>
                                        <li><a href="/practice-modes/shoppingcart-openapi">OpenAPI File</a>
                                    </ul>
                                </li>

                            <li id='mirror-api-root-menu'><a href="/practice-modes/mirror">HTTP Mirror</a>
                                <ul>
                                    <li><a href="/practice-modes/mirror">About HTTP Mirror</a></li>
                                    <li><a href="/mirror/docs">Mirror API Docs</a></li>
                                    <li><a href="/mirror/docs/openapi.json?download">OpenAPI File</a></li>

                                </ul>
                            </li>
                            <li id='blog-root-menu'><a href="/blog">Blog</a></li>
                        </ul>
                    </div>
                </nav>
                </div>
        """
                        .stripIndent();

        guiManagement.setActualMenuHtml(getSponsorMessage() + actualMenu);

        // Add the Default GUI Endpoiints for entity exploration

        guiManagement.setHomePageContent(
                """
                    <h2 id="challenges">Challenges</h2>
                    <p>The challenges can be completed by issuing API requests to the API.</p>
                    <p>e.g. <code>GET http://localhost:4567/todos</code> would complete the challenge to &quot;GET the list of todos&quot;</p>
                    <p>You can also <code>GET http://localhost:4567/challenges</code> to get the list of challenges and their status as an API call. </p>
                """);

        guiManagement.setFooter(getSponsorMessage() + getChallengesFooter());

        installCanonicalHostRedirect();
        installCanonicalHostHeadRewrite();

        ResourceContentScanner contentScanner = new ResourceContentScanner();
        List<String> pathsToFileContent =
                contentScanner.scanForFullPathsOfExtensionsIn("content/", "md");
        contentScanner.addPathsToAvailableContent(pathsToFileContent);

        // add an endpoint for each markdown content file
        MarkdownContentManager contentManager =
                new MarkdownContentManager(pathsToFileContent, guiManagement, challengeDefinitions);
        BlogContentManager blogContentManager =
                new BlogContentManager(pathsToFileContent, CANONICAL_HOST);
        for (String pathToMarkdownFile : pathsToFileContent) {
            String endPointForMarkdownFile =
                    pathToMarkdownFile.replaceFirst("content/", "/").replace(".md", "");
            String trailingSlashEndPointForMarkdownFile = endPointForMarkdownFile + "/";
            get(
                    endPointForMarkdownFile,
                    ((request, response) -> {
                        try {
                            String responseBody =
                                    contentManager.getResourceMarkdownFileAsHtml(
                                            "content",
                                            request.pathInfo(),
                                            getMarkdownParamsFromRequest(request));
                            response.body(responseBody);
                            response.type("text/html");
                            if (response.containsHeader("x-robots-tag")) {
                                // we want it indexed because it is content
                                response.header("x-robots-tag", "all");
                            }
                            response.status(200);
                        } catch (IllegalArgumentException e) {
                            // in theory this will never happen because we are only creating
                            // endpoints for existing resources
                            pageNotFoundHtmlResponse.amendResponse(response, "");
                        }
                        return "";
                    }));
            get(
                    trailingSlashEndPointForMarkdownFile,
                    ((request, response) -> {
                        response.redirect(endPointForMarkdownFile, 301);
                        return "";
                    }));
            head(
                    endPointForMarkdownFile,
                    ((request, response) -> {
                        try {
                            contentManager.getResourceMarkdownFileAsHtml(
                                    "content",
                                    request.pathInfo(),
                                    getMarkdownParamsFromRequest(request));
                            response.type("text/html");
                            if (response.containsHeader("x-robots-tag")) {
                                // we want it indexed because it is content
                                response.header("x-robots-tag", "all");
                            }
                            response.status(200);
                        } catch (IllegalArgumentException e) {
                            // in theory this will never happen because we are only creating
                            // endpoints for existing resources
                            pageNotFoundHtmlResponse.amendResponse(response, "");
                        }
                        return "";
                    }));
            head(
                    trailingSlashEndPointForMarkdownFile,
                    ((request, response) -> {
                        response.redirect(endPointForMarkdownFile, 301);
                        return "";
                    }));
        }

        registerBlogGeneratedRoutes(blogContentManager);
        registerApiClientRoutes();
        registerZudokuEmbedRoute();

        // using the ResourceContentScanner, we can build the sitemap.xml automatically
        SiteMapXml siteMap = new SiteMapXml();
        Map<String, LocalDate> contentUrls = contentScanner.scanForUrlsWithDates("content/", "md");
        for (String pathToMarkdownFile : contentUrls.keySet()) {
            siteMap.addUrl(
                    CANONICAL_HOST + "/" + pathToMarkdownFile,
                    contentUrls.get(pathToMarkdownFile).toString());
        }
        siteMap.addUrl(CANONICAL_HOST, SEO_FIXED_LASTMOD.toString());
        siteMap.addUrl(CANONICAL_HOST + "/docs", SEO_FIXED_LASTMOD.toString());
        siteMap.addUrl(CANONICAL_HOST + "/gui/challenges", SEO_FIXED_LASTMOD.toString());
        siteMap.addUrl(CANONICAL_HOST + "/blog/all-posts", blogContentManager.latestLastMod());
        for (int pageNumber = 2; pageNumber <= blogContentManager.blogPageCount(); pageNumber++) {
            siteMap.addUrl(
                    CANONICAL_HOST + blogContentManager.blogPagePath(pageNumber),
                    blogContentManager.latestLastMod());
        }
        siteMap.addUrl(CANONICAL_HOST + "/blog/categories", blogContentManager.latestLastMod());
        for (String categorySlug : blogContentManager.categorySlugs()) {
            siteMap.addUrl(
                    CANONICAL_HOST + "/blog/categories/" + categorySlug,
                    blogContentManager.categoryLastMod(categorySlug));
            for (int pageNumber = 2;
                    pageNumber <= blogContentManager.categoryPageCount(categorySlug);
                    pageNumber++) {
                siteMap.addUrl(
                        CANONICAL_HOST
                                + blogContentManager.categoryPagePath(categorySlug, pageNumber),
                        blogContentManager.categoryLastMod(categorySlug));
            }
        }

        get(
                "/sitemap.xml",
                (request, response) -> {
                    response.type("application/xml");
                    response.status(200);
                    return siteMap.asSitemapXml();
                });

        // Redirect legacy URLs to avoid SEO penalties from old inbound links.
        permanentRedirect("/tutorials/web-basics", "/reference/web-basics");
        permanentRedirect("/tutorials/web-basics/", "/reference/web-basics");
        permanentRedirect("/tutorials/http-basics", "/reference/http-basics");
        permanentRedirect("/tutorials/http-basics/", "/reference/http-basics");
        permanentRedirect("/tutorials/http-verbs", "/reference/http-verbs");
        permanentRedirect("/tutorials/http-verbs/", "/reference/http-verbs");
        permanentRedirect("/tutorials/rest-api-basics", "/reference/rest-api-basics");
        permanentRedirect("/tutorials/rest-api-basics/", "/reference/rest-api-basics");
        permanentRedirect("/tutorials/testing-apis", "/reference/testing-apis");
        permanentRedirect("/tutorials/testing-apis/", "/reference/testing-apis");
        permanentRedirect("/tutorials/openapi", "/reference/openapi");
        permanentRedirect("/tutorials/openapi/", "/reference/openapi");
        permanentRedirect("/tutorials/swagger", "/reference/open-api-uis/swagger");
        permanentRedirect("/tutorials/swagger/", "/reference/open-api-uis/swagger");
        permanentRedirect("/reference/swagger", "/reference/open-api-uis/swagger");
        permanentRedirect("/reference/swagger/", "/reference/open-api-uis/swagger");
        permanentRedirect("/tutorials/summary", "/reference/summary");
        permanentRedirect("/tutorials/summary/", "/reference/summary");
        permanentRedirect("/tutorials/openapi-swagger", "/reference/openapi");
        permanentRedirect("/tutorials/openapi-swagger/", "/reference/openapi");
        permanentRedirect("/reference/openapi-swagger", "/reference/openapi");
        permanentRedirect("/reference/openapi-swagger/", "/reference/openapi");
        permanentRedirect("/changes", "/blog/categories/change-log");
        permanentRedirect("/changes/", "/blog/categories/change-log");

        get(
                "/apichallenges/solutions/method-overrides/all-method-overrides",
                (request, response) -> {
                    response.redirect(
                            "/apichallenges/solutions/method-override/post-heartbeat-as-delete-405",
                            301);
                    return "";
                });

        get(
                "/apichallenges/solutions/manage-session/save-restore-session",
                (request, response) -> {
                    response.redirect(
                            "/apichallenges/solutions/manage-session/get-challenger-guid-existing-x-challenger-200",
                            301);
                    return "";
                });

        get(
                "/apichallenges/solutions/status-codes/status-codes-405-500-501-204",
                (request, response) -> {
                    response.redirect(
                            "/apichallenges/solutions/status-codes/delete-heartbeat-405", 301);
                    return "";
                });

        get(
                "/apichallenges/solutions/method-override/all-method-overrides",
                (request, response) -> {
                    response.redirect(
                            "/apichallenges/solutions/method-override/post-heartbeat-as-delete-405",
                            301);
                    return "";
                });

        get(
                "/apichallenges/solutions/authorization/post-secret-note-401-403",
                (request, response) -> {
                    response.redirect(
                            "/apichallenges/solutions/authorization/post-secret-note-401", 301);
                    return "";
                });

        get(
                "/apichallenges/solutions/authorization/get-post-secret-note-bearer",
                (request, response) -> {
                    response.redirect(
                            "/apichallenges/solutions/authorization/get-secret-note-bearer", 301);
                    return "";
                });

        get(
                "/tools/clients/soapyi",
                (request, response) -> {
                    response.redirect("/tools/clients/soapui", 301);
                    return "";
                });

        get(
                "/apichallenges/solutions/query/query-todos-200-filter",
                (request, response) -> {
                    response.redirect("/apichallenges/solutions/query/query-todos-200", 301);
                    return "";
                });

        // use the site/index.md to allow easier creation of landing page, rather than
        // public/index.html
        get(
                "/",
                (request, response) -> {
                    String responseBody =
                            contentManager.getHtmlVersionOfMarkdownContent(
                                    "site", "/index", getMarkdownParamsFromRequest(request));
                    response.body(responseBody);
                    response.type("text/html");
                    if (response.containsHeader("x-robots-tag")) {
                        // we want it indexed because it is content
                        response.header("x-robots-tag", "all");
                    }
                    response.status(200);
                    return "";
                });
        head(
                "/",
                (request, response) -> {
                    contentManager.getHtmlVersionOfMarkdownContent(
                            "site", "/index", getMarkdownParamsFromRequest(request));
                    response.type("text/html");
                    if (response.containsHeader("x-robots-tag")) {
                        // we want it indexed because it is content
                        response.header("x-robots-tag", "all");
                    }
                    response.status(200);
                    return "";
                });

        // single user / default session
        get(
                "/gui/challenges",
                (request, result) -> {
                    String challengerCookie = request.cookie("X-CHALLENGER");
                    if (challengerCookie == null) {
                        challengerCookie = request.cookie("X-THINGIFIER-DATABASE-NAME");
                    }
                    if (challengerCookie != null) {
                        // we didn't add a challenger in the URL but we do have one in the cookie
                        result.header("location", "/gui/challenges/" + challengerCookie);
                        result.status(302);
                        return "";
                    }

                    result.type("text/html");
                    result.status(200);

                    StringBuilder html = new StringBuilder();
                    html.append(getChallengesPageHtmlHeader());
                    html.append(guiManagement.getMenuAsHTML());
                    html.append("<h1>API Challenges Progress</h1>");
                    html.append(guiManagement.getStartOfMainContentMarker());

                    // todo explain challenges - single user mode

                    // List<ChallengeData> reportOn = new ArrayList<>();

                    if (single_player_mode) {
                        // reportOn = new ChallengesPayload(challengeDefinitions,
                        // challengers.SINGLE_PLAYER).getAsChallenges();
                        String json = "{}";
                        if (challengers
                                .getErModel()
                                .getDatabaseNames()
                                .contains(EntityRelModel.DEFAULT_DATABASE_NAME)) {
                            json =
                                    challengers
                                            .getErModel()
                                            .exportInstanceDataAsJson(
                                                    EntityRelModel.DEFAULT_DATABASE_NAME);
                        }
                        html.append(outputChallengeDataAsJS(challengers.SINGLE_PLAYER, json));
                        html.append(showAchievements());
                        html.append(playerChallengesIntro());
                        html.append(
                                renderChallengeData(
                                        challengeDefinitions, challengers.SINGLE_PLAYER));
                        html.append(injectCookieFunctions());
                        html.append(
                                storeThingifierDatabaseNameCookie(
                                        challengers.SINGLE_PLAYER.getXChallenger()));
                    } else {
                        html.append(outputEmptyChallengeDataAsJS());
                        html.append(showAchievements());
                        html.append(playerChallengesIntro());
                        html.append(
                                "<div style='clear:both'><p><strong>Unknown Challenger ID</strong></p></div>");
                        html.append(unknownChallengerActions());
                        html.append(
                                multiUserShortHelp(
                                        persistenceLayer
                                                .willAutoSaveChallengerStatusToPersistenceLayer(),
                                        persistenceLayer
                                                .willAutoLoadChallengerStatusFromPersistenceLayer(),
                                        persistenceLayer.autoSaveAfterCompletedChallenges()));
                        html.append(injectCookieFunctions());
                        html.append(showPreviousGuids());

                        // reportOn = new ChallengesPayload(challengeDefinitions,
                        // challengers.DEFAULT_PLAYER_DATA).getAsChallenges();
                        html.append(
                                renderChallengeData(
                                        challengeDefinitions, challengers.DEFAULT_PLAYER_DATA));
                    }

                    // html.append(renderChallengeData(reportOn));

                    html.append(guiManagement.getEndOfMainContentMarker());
                    html.append(guiManagement.getPageFooter());
                    html.append(guiManagement.getPageEnd());
                    return html.toString();
                });

        get(
                "/gui/challenge-status",
                (request, result) -> {
                    result.type("application/json");
                    result.status(200);
                    return renderChallengeProgressStatusJson(
                            request, challengers, challengeDefinitions, single_player_mode);
                });

        get(
                "/gui/challenge-status/*",
                (request, result) -> {
                    result.type("application/json");
                    result.status(200);
                    return renderChallengeStatusJson(
                            request.splat(),
                            request,
                            challengers,
                            challengeDefinitions,
                            single_player_mode);
                });

        // multi user
        get(
                "/gui/challenges/*",
                (request, result) -> {
                    result.type("text/html");
                    result.status(200);

                    StringBuilder html = new StringBuilder();
                    html.append(getChallengesPageHtmlHeader());
                    html.append(guiManagement.getMenuAsHTML());
                    html.append("<h1>API Challenges Progress</h1>");
                    html.append(guiManagement.getStartOfMainContentMarker());

                    // List<ChallengeData> reportOn = null;

                    String xChallenger = null;

                    try {
                        xChallenger = request.splat();
                    } catch (Exception e) {
                        logger.warn("No challenger id to render");
                    }

                    // is there an in memory challenger with this id?
                    ChallengerAuthData challenger = null;
                    PersistenceResponse persistence = null;

                    // only check if an xchallenger was passed in
                    if (xChallenger != null && !xChallenger.trim().isEmpty()) {
                        xChallenger = santitizeChallengerGuid(xChallenger);
                        challenger = challengers.getChallenger(xChallenger);

                        persistence = new PersistenceResponse();

                        // if no inmemory challenger then ask the persistence layer
                        if (challenger == null) {
                            persistence =
                                    persistenceLayer.tryToLoadChallenger(challengers, xChallenger);
                        }
                    }

                    if (challenger == null) {
                        html.append(outputEmptyChallengeDataAsJS());
                        html.append(showAchievements());
                        html.append(playerChallengesIntro());

                        String persistenceReason = "";
                        if (persistence != null) {
                            persistenceReason = persistence.getErrorMessage();
                        }
                        html.append("<div class='standoutblock'>");
                        html.append(
                                String.format(
                                        "<p><strong>Unknown Challenger ID %s</strong></p>",
                                        persistenceReason));
                        html.append(unknownChallengerActions());
                        html.append(showCurrentStatus());
                        html.append("</div>");
                        html.append(
                                multiUserShortHelp(
                                        persistenceLayer
                                                .willAutoSaveChallengerStatusToPersistenceLayer(),
                                        persistenceLayer
                                                .willAutoLoadChallengerStatusFromPersistenceLayer(),
                                        persistenceLayer.autoSaveAfterCompletedChallenges()));
                        html.append(injectCookieFunctions());
                        html.append(showPreviousGuids());
                        html.append(
                                renderChallengeData(
                                        challengeDefinitions, challengers.DEFAULT_PLAYER_DATA));
                    } else {
                        html.append(injectCookieFunctions());

                        String json = "{}";
                        if (challengers.getErModel().getDatabaseNames().contains(xChallenger)) {
                            json = challengers.getErModel().exportInstanceDataAsJson(xChallenger);
                        }
                        html.append(outputChallengeDataAsJS(challenger, json));
                        html.append(showAchievements());
                        html.append(playerChallengesIntro());

                        if (!single_player_mode) {
                            html.append(storeThingifierDatabaseNameCookie(xChallenger));
                            html.append(storeCurrentGuidInLocalStorage(xChallenger));

                            html.append("<div class='standoutblock'>");
                            html.append(
                                    String.format(
                                            "<p><strong>%s</strong></p>",
                                            activeChallengerProgressHeading(xChallenger)));
                            // keep challenge session alive when refresh
                            challenger.touch();
                            html.append(showCurrentStatus());
                            html.append(showPreviousGuids());
                            html.append("</div>");
                        } else {
                            html.append(storeThingifierDatabaseNameCookie(xChallenger));
                            html.append(storeCurrentGuidInLocalStorage(xChallenger));
                            html.append("<div class='standoutblock'>");
                            html.append(showCurrentStatus());
                            html.append("</div>");
                        }

                        html.append(renderChallengeData(challengeDefinitions, challenger));

                        html.append(refreshScriptFor(challenger.getXChallenger()));
                    }

                    // html.append(renderChallengeData(reportOn));

                    html.append(guiManagement.getEndOfMainContentMarker());
                    html.append(guiManagement.getPageFooter());
                    html.append(guiManagement.getPageEnd());
                    return html.toString();
                });

        get(
                "/gui/404",
                (request, result) -> {
                    pageNotFoundHtmlResponse.amendResponse(result, "");
                    return "";
                });

        get(
                "/gui/404/*",
                (request, result) -> {
                    result.status(404);
                    result.type("text/html");

                    String urltoshow = "";

                    try {
                        urltoshow = request.splat();
                    } catch (Exception e) {
                        logger.error("No url to pretend to be on 404", e);
                    }

                    pageNotFoundHtmlResponse.amendResponse(
                            result,
                            "<script>window.history.pushState({id:\"404sim\"},\"\",\"/"
                                    + urltoshow
                                    + "\");</script>");
                    return "";
                });

        after(
                (request, response) -> {

                    // Since we already scanned for static content we can just htmlise a 404 if
                    // necessary
                    if (response.status() == 404
                            && request.header("accept") != null
                            && request.header("accept").contains("html")) {

                        logger.info("An HTML 404");
                        pageNotFoundHtmlResponse.amendResponse(response, "");
                    }
                });
    }

    private record ApiClientPage(
            String path,
            String title,
            String heading,
            String defaultMethod,
            String defaultPath,
            String allowedPathPrefixes,
            boolean useChallenger) {}

    private void registerApiClientRoutes() {
        final List<ApiClientPage> pages =
                List.of(
                        new ApiClientPage(
                                "/apichallenges/client",
                                "API Challenges Client",
                                "API Challenges Client",
                                "GET",
                                "/todos",
                                API_CHALLENGE_ALLOWED_PATH_PREFIXES,
                                true),
                        new ApiClientPage(
                                "/simpleapi/client",
                                "Simple API Client",
                                "Simple API Client",
                                "GET",
                                "/simpleapi/items",
                                "/simpleapi",
                                false),
                        new ApiClientPage(
                                "/shop/client",
                                "Buggy API Client",
                                "Buggy API Client",
                                "GET",
                                "/shop/products",
                                "/shop",
                                false));

        for (ApiClientPage page : pages) {
            get(
                    page.path(),
                    (request, response) -> {
                        response.type("text/html");
                        response.status(200);
                        return renderApiClientPage(page);
                    });
            head(
                    page.path(),
                    (request, response) -> {
                        response.type("text/html");
                        response.status(200);
                        return "";
                    });
        }
    }

    private void registerZudokuEmbedRoute() {
        get(
                "/zudoku-embed",
                (request, response) -> {
                    response.type("text/html");
                    response.header("x-robots-tag", "noindex");
                    response.status(200);
                    return resourceAsStringOrEmpty("public/zudoku-embed.html");
                });
        get(
                "/zudoku-embed/*",
                (request, response) -> {
                    response.type("text/html");
                    response.header("x-robots-tag", "noindex");
                    response.status(200);
                    return resourceAsStringOrEmpty("public/zudoku-embed.html");
                });
        head(
                "/zudoku-embed",
                (request, response) -> {
                    response.type("text/html");
                    response.header("x-robots-tag", "noindex");
                    response.status(200);
                    return "";
                });
        head(
                "/zudoku-embed/*",
                (request, response) -> {
                    response.type("text/html");
                    response.header("x-robots-tag", "noindex");
                    response.status(200);
                    return "";
                });
    }

    private String renderApiClientPage(final ApiClientPage page) {
        final StringBuilder html = new StringBuilder();
        html.append(
                guiManagement.getPageStart(
                        page.title(),
                        "<script src='"
                                + AssetVersion.versionedPath("/js/api-live-request.js")
                                + "' defer></script>",
                        page.path()));
        html.append(guiManagement.getMenuAsHTML());
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append("<div class=\"main-text-content\">");
        html.append("<h1>").append(escapeHtmlAttribute(page.heading())).append("</h1>");
        if ("/simpleapi/client".equals(page.path())) {
            html.append(renderSimpleApiRandomIsbnGenerator());
        }
        html.append(renderApiClientWidget(page));
        html.append("</div>");
        html.append(guiManagement.getEndOfMainContentMarker());
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());
        return html.toString();
    }

    private void registerBlogGeneratedRoutes(final BlogContentManager blogContentManager) {
        get(
                "/blog/all-posts",
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);
                    return blogContentManager.renderAllPostsIndexPage(guiManagement);
                });
        head(
                "/blog/all-posts",
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);
                    return "";
                });
        get(
                "/blog/all-posts/",
                (request, response) -> {
                    response.redirect("/blog/all-posts", 301);
                    return "";
                });
        head(
                "/blog/all-posts/",
                (request, response) -> {
                    response.redirect("/blog/all-posts", 301);
                    return "";
                });

        registerRedirect("/blog/page/1", "/blog");
        registerRedirect("/blog/page/1/", "/blog");
        for (int pageNumber = 2; pageNumber <= blogContentManager.blogPageCount(); pageNumber++) {
            final String blogPagePath = blogContentManager.blogPagePath(pageNumber);
            final int currentPageNumber = pageNumber;
            get(
                    blogPagePath,
                    (request, response) -> {
                        response.type("text/html");
                        response.status(200);
                        return blogContentManager.renderBlogPage(guiManagement, currentPageNumber);
                    });
            head(
                    blogPagePath,
                    (request, response) -> {
                        response.type("text/html");
                        response.status(200);
                        return "";
                    });
            registerRedirect(blogPagePath + "/", blogPagePath);
        }

        get(
                "/blog/categories",
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);
                    return blogContentManager.renderCategoryIndexPage(guiManagement);
                });
        head(
                "/blog/categories",
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);
                    return "";
                });
        get(
                "/blog/categories/",
                (request, response) -> {
                    response.redirect("/blog/categories", 301);
                    return "";
                });
        head(
                "/blog/categories/",
                (request, response) -> {
                    response.redirect("/blog/categories", 301);
                    return "";
                });

        for (String categorySlug : blogContentManager.categorySlugs()) {
            final String categoryPath = "/blog/categories/" + categorySlug;
            get(
                    categoryPath,
                    (request, response) -> {
                        response.type("text/html");
                        response.status(200);
                        return blogContentManager.renderCategoryPage(guiManagement, categorySlug);
                    });
            head(
                    categoryPath,
                    (request, response) -> {
                        response.type("text/html");
                        response.status(200);
                        return "";
                    });
            get(
                    categoryPath + "/",
                    (request, response) -> {
                        response.redirect(categoryPath, 301);
                        return "";
                    });
            head(
                    categoryPath + "/",
                    (request, response) -> {
                        response.redirect(categoryPath, 301);
                        return "";
                    });

            registerRedirect(categoryPath + "/page/1", categoryPath);
            registerRedirect(categoryPath + "/page/1/", categoryPath);
            for (int pageNumber = 2;
                    pageNumber <= blogContentManager.categoryPageCount(categorySlug);
                    pageNumber++) {
                final String categoryPagePath =
                        blogContentManager.categoryPagePath(categorySlug, pageNumber);
                final int currentPageNumber = pageNumber;
                get(
                        categoryPagePath,
                        (request, response) -> {
                            response.type("text/html");
                            response.status(200);
                            return blogContentManager.renderCategoryPage(
                                    guiManagement, categorySlug, currentPageNumber);
                        });
                head(
                        categoryPagePath,
                        (request, response) -> {
                            response.type("text/html");
                            response.status(200);
                            return "";
                        });
                registerRedirect(categoryPagePath + "/", categoryPagePath);
            }
        }

        get(
                "/blog/feed.xml",
                (request, response) -> {
                    response.type("application/rss+xml; charset=utf-8");
                    response.status(200);
                    return blogContentManager.renderRssFeed();
                });
        head(
                "/blog/feed.xml",
                (request, response) -> {
                    response.type("application/rss+xml; charset=utf-8");
                    response.status(200);
                    return "";
                });
    }

    private void registerRedirect(final String fromPath, final String toPath) {
        get(
                fromPath,
                (request, response) -> {
                    response.redirect(toPath, 301);
                    return "";
                });
        head(
                fromPath,
                (request, response) -> {
                    response.redirect(toPath, 301);
                    return "";
                });
    }

    private String renderApiClientWidget(final ApiClientPage page) {
        return "<div class=\"api-live-request\" data-method=\""
                + escapeHtmlAttribute(page.defaultMethod())
                + "\" data-path=\""
                + escapeHtmlAttribute(page.defaultPath())
                + "\" data-editable=\"true\" data-edit-mode=\"adhoc\" data-allowed-path-prefixes=\""
                + escapeHtmlAttribute(page.allowedPathPrefixes())
                + "\" data-use-challenger=\""
                + page.useChallenger()
                + "\" data-headers=\"Accept: application/json\"></div>";
    }

    private String renderSimpleApiRandomIsbnGenerator() {
        return resourceAsStringOrEmpty("partials/generate-random-isbn.html");
    }

    private void permanentRedirect(final String fromPath, final String toPath) {
        get(
                fromPath,
                (request, response) -> {
                    response.redirect(toPath, 301);
                    return "";
                });
        head(
                fromPath,
                (request, response) -> {
                    response.redirect(toPath, 301);
                    return "";
                });
    }

    private Map<String, String> getMarkdownParamsFromRequest(HttpServerRequest request) {
        final String originUrl = originUrlFrom(request);
        final String hostUrl = hostFrom(request);
        Map<String, String> params = new HashMap<>();
        params.put("ORIGIN_URL", originUrl);
        params.put("HOST_URL", hostUrl);
        return params;
    }

    private String originUrlFrom(final HttpServerRequest request) {
        return "%s://%s".formatted(schemeFrom(request), hostFrom(request));
    }

    private void installCanonicalHostRedirect() {
        before(
                (request, response) -> {
                    if (!isRedirectHost(hostFrom(request))) {
                        return;
                    }

                    final String canonicalUrl = canonicalUrlFor(request);
                    final int redirectStatus = canonicalRedirectStatusFor(request);

                    response.header("Location", canonicalUrl);
                    halt(redirectStatus, "");
                });
    }

    private void installCanonicalHostHeadRewrite() {
        after(
                (request, response) -> {
                    final String body = response.body();
                    if (!isHtmlResponse(response) || body == null) {
                        return;
                    }

                    final int headEndIndex = body.indexOf("</head>");
                    if (headEndIndex < 0) {
                        return;
                    }

                    final String head = body.substring(0, headEndIndex);
                    if (!head.contains(LEGACY_CANONICAL_HOST)) {
                        return;
                    }

                    response.body(
                            head.replace(LEGACY_CANONICAL_HOST, CANONICAL_HOST)
                                    + body.substring(headEndIndex));
                });
    }

    private boolean isHtmlResponse(final HttpServerResponse response) {
        String contentType = response.type();
        if (!hasText(contentType)) {
            contentType = responseHeaderValue(response, "Content-Type");
        }
        return hasText(contentType) && contentType.toLowerCase(Locale.ROOT).contains("text/html");
    }

    private String responseHeaderValue(final HttpServerResponse response, final String headerName) {
        for (Map.Entry<String, String> header : response.headers().entrySet()) {
            if (header.getKey().equalsIgnoreCase(headerName)) {
                return header.getValue();
            }
        }
        return null;
    }

    private String canonicalUrlFor(final HttpServerRequest request) {
        final String query = request.queryString();
        return CANONICAL_HOST + request.path() + (hasText(query) ? "?" + query : "");
    }

    private int canonicalRedirectStatusFor(final HttpServerRequest request) {
        if (isGetOrHead(request.method()) && isContentDocumentationPath(request.path())) {
            return 301;
        }
        return 308;
    }

    private boolean isGetOrHead(final String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }

    private boolean isContentDocumentationPath(final String path) {
        return path.equals("/")
                || path.equals("/sitemap.xml")
                || path.equals("/robots.txt")
                || path.startsWith("/apichallenges")
                || path.startsWith("/author")
                || path.startsWith("/blog")
                || path.startsWith("/learning")
                || path.startsWith("/practice-modes")
                || path.startsWith("/reference")
                || path.startsWith("/tools")
                || path.startsWith("/tutorials")
                || path.equals("/sponsors")
                || path.equals("/changes")
                || path.startsWith("/seo-metadata-");
    }

    private boolean isRedirectHost(final String host) {
        final String normalizedHost = normalizeHost(host);
        return LEGACY_HOST.equals(normalizedHost) || WWW_HOST.equals(normalizedHost);
    }

    private String normalizeHost(final String host) {
        if (!hasText(host)) {
            return "";
        }

        final String trimmed = firstHeaderValue(host).toLowerCase(Locale.ROOT);
        final int portIndex = trimmed.indexOf(":");
        if (portIndex > -1) {
            return trimmed.substring(0, portIndex);
        }
        return trimmed;
    }

    private String schemeFrom(final HttpServerRequest request) {
        final String forwardedProto = forwardedHeaderValue(request.header("Forwarded"), "proto");
        if (hasText(forwardedProto)) {
            return forwardedProto;
        }

        final String proxyProto = firstHeaderValue(request.header("X-Forwarded-Proto"));
        if (hasText(proxyProto)) {
            return proxyProto;
        }

        return request.scheme();
    }

    private String hostFrom(final HttpServerRequest request) {
        final String forwardedHost = forwardedHeaderValue(request.header("Forwarded"), "host");
        if (hasText(forwardedHost)) {
            return forwardedHost;
        }

        final String proxyHost = firstHeaderValue(request.header("X-Forwarded-Host"));
        if (hasText(proxyHost)) {
            return proxyHost;
        }

        return request.host();
    }

    private String forwardedHeaderValue(final String header, final String key) {
        final String firstValue = firstHeaderValue(header);
        if (!hasText(firstValue)) {
            return "";
        }

        final String prefix = "%s=".formatted(key);
        final String[] parts = firstValue.split(";");
        for (final String part : parts) {
            final String trimmed = part.trim();
            if (trimmed.toLowerCase().startsWith(prefix)) {
                return unquote(trimmed.substring(prefix.length()).trim());
            }
        }

        return "";
    }

    private String firstHeaderValue(final String header) {
        if (!hasText(header)) {
            return "";
        }
        return header.split(",", 2)[0].trim();
    }

    private String unquote(final String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private boolean hasText(final String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String escapeHtmlAttribute(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String renderChallengeStatusJson(
            final String rawChallengeId,
            final HttpServerRequest request,
            final Challengers challengers,
            final ChallengeDefinitions challengeDefinitions,
            final boolean singlePlayerMode) {

        final Optional<ChallengeDefinitionData> challenge =
                challengeDefinitionForId(rawChallengeId, challengeDefinitions);
        if (challenge.isEmpty()) {
            return "{\"id\":\""
                    + escapeJsonValue(rawChallengeId)
                    + "\",\"status\":false,\"known\":false}";
        }

        final ChallengerAuthData challenger =
                challengerForStatusRequest(request, challengers, singlePlayerMode);
        boolean passed = false;
        if (challenger != null) {
            final CHALLENGE challengeKey = challengeDefinitions.getChallenge(challenge.get().name);
            passed = challengeKey != null && challenger.statusOfChallenge(challengeKey);
        }

        return "{\"id\":\""
                + normalizeChallengeId(challenge.get().id)
                + "\",\"status\":"
                + passed
                + ",\"known\":true}";
    }

    private String renderChallengeProgressStatusJson(
            final HttpServerRequest request,
            final Challengers challengers,
            final ChallengeDefinitions challengeDefinitions,
            final boolean singlePlayerMode) {

        final ChallengerAuthData challenger =
                challengerForStatusRequest(request, challengers, singlePlayerMode);
        if (challenger == null) {
            return unknownChallengeProgressStatusJson(challengeDefinitions);
        }

        challenger.touch();

        final JsonObject databaseData = databaseDataFor(challenger, challengers);
        final JsonArray challenges = challengeProgressArray(challenger, challengeDefinitions);
        final int doneCount = completedChallengeCount(challenges);
        final int totalCount = challenges.size();
        final int percentComplete =
                totalCount == 0 ? 0 : Math.round((doneCount * 100.0f) / totalCount);
        final int todoCount =
                databaseData.has("todos") && databaseData.get("todos").isJsonArray()
                        ? databaseData.getAsJsonArray("todos").size()
                        : 0;

        final JsonObject summary = new JsonObject();
        summary.addProperty("doneCount", doneCount);
        summary.addProperty("totalCount", totalCount);
        summary.addProperty("percentComplete", percentComplete);
        summary.addProperty("todoCount", todoCount);

        final JsonObject root = new JsonObject();
        root.addProperty("known", true);
        root.add("challengerData", JsonParser.parseString(challenger.asJson()).getAsJsonObject());
        root.add("databaseData", databaseData);
        root.add("summary", summary);
        root.add("challenges", challenges);
        return GSON.toJson(root);
    }

    private String unknownChallengeProgressStatusJson(
            final ChallengeDefinitions challengeDefinitions) {
        final JsonObject summary = new JsonObject();
        summary.addProperty("doneCount", 0);
        summary.addProperty("totalCount", challengeDefinitions.getChallenges().size());
        summary.addProperty("percentComplete", 0);
        summary.addProperty("todoCount", 0);

        final JsonObject root = new JsonObject();
        root.addProperty("known", false);
        root.add("challengerData", new JsonObject());
        root.add("databaseData", new JsonObject());
        root.add("summary", summary);
        root.add("challenges", new JsonArray());
        return GSON.toJson(root);
    }

    private int completedChallengeCount(final JsonArray challenges) {
        int count = 0;
        for (JsonElement challenge : challenges) {
            if (challenge.getAsJsonObject().get("status").getAsBoolean()) {
                count++;
            }
        }
        return count;
    }

    private JsonObject databaseDataFor(
            final ChallengerAuthData challenger, final Challengers challengers) {

        String databaseName = challenger.getXChallenger();
        if (Challengers.SINGLE_PLAYER_GUID.equals(databaseName)) {
            databaseName = EntityRelModel.DEFAULT_DATABASE_NAME;
        }

        if (challengers.getErModel().getDatabaseNames().contains(databaseName)) {
            return parseJsonObjectOrEmpty(
                    challengers.getErModel().exportInstanceDataAsJson(databaseName));
        }

        return new JsonObject();
    }

    private JsonObject parseJsonObjectOrEmpty(final String json) {
        try {
            final JsonElement element = JsonParser.parseString(json);
            if (element != null && element.isJsonObject()) {
                return element.getAsJsonObject();
            }
        } catch (Exception ignored) {
            // Return empty JSON below.
        }
        return new JsonObject();
    }

    private JsonArray challengeProgressArray(
            final ChallengerAuthData challenger, final ChallengeDefinitions challengeDefinitions) {

        final JsonArray challenges = new JsonArray();
        for (ChallengeDefinitionData challenge : challengeDefinitions.getChallenges()) {
            final CHALLENGE challengeKey = challengeDefinitions.getChallenge(challenge.name);
            final boolean passed =
                    challengeKey != null && challenger.statusOfChallenge(challengeKey);

            final JsonObject challengeStatus = new JsonObject();
            challengeStatus.addProperty("id", challenge.id);
            challengeStatus.addProperty("name", challenge.name);
            challengeStatus.addProperty("status", passed);
            challenges.add(challengeStatus);
        }
        return challenges;
    }

    private Optional<ChallengeDefinitionData> challengeDefinitionForId(
            final String rawChallengeId, final ChallengeDefinitions challengeDefinitions) {
        if (!hasText(rawChallengeId)) {
            return Optional.empty();
        }

        final String challengeId = normalizeChallengeId(rawChallengeId);
        for (ChallengeDefinitionData challenge : challengeDefinitions.getChallenges()) {
            if (challenge.id.equals(rawChallengeId.trim())
                    || normalizeChallengeId(challenge.id).equals(challengeId)) {
                return Optional.of(challenge);
            }
        }
        return Optional.empty();
    }

    private String normalizeChallengeId(final String rawChallengeId) {
        if (rawChallengeId == null) {
            return "";
        }
        return rawChallengeId.trim().replaceFirst("^0+(?!$)", "");
    }

    private ChallengerAuthData challengerForStatusRequest(
            final HttpServerRequest request,
            final Challengers challengers,
            final boolean singlePlayerMode) {

        String xChallenger = request.header("X-CHALLENGER");
        if (!hasText(xChallenger)) {
            xChallenger = request.header("X-Challenger");
        }
        if (!hasText(xChallenger)) {
            xChallenger = request.cookie("X-CHALLENGER");
        }
        if (!hasText(xChallenger)) {
            xChallenger = request.cookie("X-THINGIFIER-DATABASE-NAME");
        }

        if (hasText(xChallenger)) {
            return challengers.getChallenger(santitizeChallengerGuid(xChallenger));
        }

        if (singlePlayerMode) {
            return challengers.SINGLE_PLAYER;
        }

        return null;
    }

    private String escapeJsonValue(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String showCurrentStatus() {
        return "<script>showCurrentStatus()</script>";
    }

    private String showAchievements() {
        return "<script>showAchievements()</script>";
    }

    private String santitizeChallengerGuid(String xChallenger) {
        return xChallenger.replaceAll("[^\\-a-zA-Z0-9]", "");
    }

    private String activeChallengerProgressHeading(final String xChallenger) {
        final String savedStatus = persistenceLayer.savedStatusTextFor(xChallenger);
        final String storageStatus = savedStatus.isEmpty() ? "" : " - " + savedStatus;
        return String.format(
                "Progress For Challenger ID %s - Active%s", xChallenger, storageStatus);
    }

    private String injectCookieFunctions() {
        return "";
    }

    private String storeThingifierDatabaseNameCookie(String xChallenger) {
        return "<script>"
                + "setCookie('X-THINGIFIER-DATABASE-NAME','"
                + xChallenger
                + "',365);"
                + "setCookie('X-CHALLENGER','"
                + xChallenger
                + "',365);"
                + "</script>";
    }

    private String storeCurrentGuidInLocalStorage(final String xChallenger) {
        return "<script>"
                + "var guids = localStorage.getItem('challenges-guids') || '';"
                + String.format("if(guids==null || !guids.includes('|%s|')){", xChallenger)
                + String.format(
                        "localStorage.setItem('challenges-guids',guids + '|%s|');", xChallenger)
                + "}"
                + "</script>";
    }

    private String unknownChallengerActions() {
        return "<p><button onclick=inputChallengeGuid()>Input Challenger GUID</button>"
                + " <a href='#gettingstarted'>Create Challenger</a></p>";
    }

    private String showPreviousGuids() {
        return "<script>displayLocalGuids()</script>"; // +
    }

    private String getChallengesFooter() {
        return """
                <p>&nbsp;</p><hr/><div class='footer'><p>Copyright Compendium Developments Ltd 2020 - 2026</p>
                <ul class='footerlinks'><li><a href='https://eviltester.com/apichallenges'>API Challenges Info</a></li>
                <li><a href='https://eviltester.com'>EvilTester.com</a></li>
                <li><a href='https://linkedin.com/in/eviltester'>Contact</a></li>
                </ul></div>
                """;
    }

    private String playerChallengesIntro() {
        final StringBuilder html = new StringBuilder();
        html.append("<div style='clear:both'>");
        html.append("<h2 id='gettingstarted'>Getting Started</h2>");
        html.append(
                "<p>Use the Descriptions of the challenges below to explore the API and solve the challenges."
                        + " Remember to use the API documentation to see the format of POST requests.</p>"
                        + "<p>Progress, and the TODOs database content can be saved to, and restored from, LocalStorage in the browser - or managed via the API."
                        + "</p>");
        html.append("</div>");
        return html.toString();
    }

    private String multiUserShortHelp(
            Boolean canSaveToPersistence,
            boolean canRestoreFromPersistence,
            int saveAfterCompletedChallenges) {
        final StringBuilder html = new StringBuilder();
        html.append("<div style='clear:both' class='headertextblock'>");
        html.append(
                "<p>To view your challenges status in multi-user mode, make sure you have registered as a challenger using a `POST` request to `/challenger` and are including an `X-CHALLENGER` header in all your requests.</p>");
        html.append(
                "<p>Then view the challenges in the GUI by visiting `/gui/challenges/{GUID}`, where `{GUID}` is the value in the `X-CHALLENGER` header.<p>");
        html.append(
                "<p>Challenger sessions are purged from the server memory after 10 minutes of inactivity.</p>");
        if (canSaveToPersistence) {
            if (saveAfterCompletedChallenges > 0) {
                html.append(
                        String.format(
                                "Challenger progress is configured to save on the server after %d challenges are completed.<p>",
                                saveAfterCompletedChallenges));
            } else {
                html.append("Challenger progress is configured to save on the server.<p>");
            }
        } else {
            html.append(
                    "Challenger progress is not configured to automatically save on the server. Use the GUI or UI to save progress locally.<p>");
        }
        if (canRestoreFromPersistence) {
            html.append(
                    "To restore a previously saved session progress from the server, issue an API request with the X-CHALLENGER header (note this will restore the completion state of challenges, but not the data you were using).<p>");
        }
        html.append(
                "<p>Session state and current todo list can be stored to local storage, and later restored using the GUI buttons or via API.</p>");
        html.append(
                "<p>You can find more information about this on the <a href='/gui/multiuser'>Multi User Help Page</a><p>");
        html.append("</div>");
        return html.toString();
    }

    private String refreshScriptFor(final String xChallenger) {

        if (!guiStayAlive) {
            return "";
        }

        StringBuilder html = new StringBuilder();

        html.append("<script>");
        html.append("/* keep session alive */");
        html.append("setInterval(function(){");
        html.append(
                "var oReq = new XMLHttpRequest();\n"
                        + "oReq.open('GET', '/challenger/"
                        + xChallenger
                        + "');\n"
                        + "oReq.send();");
        html.append("},300000);");
        html.append("</script>");
        return html.toString();
    }

    private String renderChallengeData(final List<ChallengeDefinitionData> reportOn) {
        StringBuilder html = new StringBuilder();

        html.append("<table class='challenge-progress-table'>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th class='challenge-id-heading'>ID</th>");
        html.append("<th class='challenge-name-heading'>Challenge</th>");
        html.append("<th class='challenge-done-heading'>Done</th>");
        html.append("<th class='challenge-description-heading'>Description</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for (ChallengeDefinitionData challenge : reportOn) {
            html.append(
                    String.format(
                            "<tr class='status%b' data-challenge-id='%s'>",
                            challenge.status, escapeHtmlAttribute(challenge.id)));
            html.append(String.format("<td class='challenge-id-cell'>%s</td>", challenge.id));
            html.append(String.format("<td class='challenge-name-cell'>%s</td>", challenge.name));
            html.append(
                    String.format("<td class='challenge-done-status'>%b</td>", challenge.status));

            String descriptionHTML = String.format("<p>%s</p>", challenge.description);
            if (challenge.hasHints() || challenge.hasSolutionLinks()) {
                descriptionHTML = descriptionHTML + "<br/>";
            }
            if (challenge.hasHints()) {
                descriptionHTML = descriptionHTML + "<details><summary>Hints</summary>";
                descriptionHTML = descriptionHTML + "<ul>";
                String hintHtml = "";
                for (ChallengeHint hint : challenge.hints) {
                    hintHtml = hintHtml + "<li>" + hint.hintText;
                    if (hint.hintLink != null && !hint.hintLink.isEmpty()) {
                        String target = "target='_blank'";
                        if (!hint.hintLink.startsWith("http")) {
                            target = "";
                        }
                        hintHtml =
                                hintHtml
                                        + String.format(
                                                " <a href='%s' %s>Learn More</a>",
                                                hint.hintLink, target);
                    }
                    hintHtml = hintHtml + "</li>";
                }
                descriptionHTML = descriptionHTML + hintHtml + "</ul>";
                descriptionHTML = descriptionHTML + "</details>";
            }
            String solveNowHtml = solveNowWidgetFor(challenge);
            if (!solveNowHtml.isEmpty()) {
                descriptionHTML = descriptionHTML + solveNowHtml;
            }
            if (challenge.hasSolutionLinks()) {

                descriptionHTML = descriptionHTML + "<details><summary>Solution</summary>";
                descriptionHTML = descriptionHTML + "<ul>";
                String solutionsHtml = "";
                for (ChallengeSolutionLink solution : challenge.solutions) {
                    solutionsHtml = solutionsHtml + "<li>" + solution.asHtmlAHref() + "</li>";
                }
                descriptionHTML = descriptionHTML + solutionsHtml + "</ul>";
                descriptionHTML = descriptionHTML + "</details>";
            }

            html.append(
                    String.format(
                            "<td class='challenge-description-cell'>%s</td>", descriptionHTML));
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        return html.toString();
    }

    private String solveNowWidgetFor(final ChallengeDefinitionData challenge) {
        final Map<String, String> mainRequestAttributes =
                mainApiSolvingRequestAttributesFor(challenge);
        if (mainRequestAttributes.isEmpty()) {
            return "";
        }

        final Map<String, String> solveNowAttributes = new LinkedHashMap<>(mainRequestAttributes);
        solveNowAttributes.remove("open");
        solveNowAttributes.remove("details");
        solveNowAttributes.remove("summary");
        solveNowAttributes.remove("challenge-request");
        solveNowAttributes.put("editable", "true");
        solveNowAttributes.put("edit-mode", "adhoc");
        solveNowAttributes.put("allowed-path-prefixes", API_CHALLENGE_ALLOWED_PATH_PREFIXES);
        solveNowAttributes.put("challenge-id", challenge.id);

        return "<details class=\"sim-live-request-details\"><summary>Solve Now</summary>"
                + renderApiLiveRequestPlaceholder(solveNowAttributes)
                + "</details>";
    }

    private Map<String, String> mainApiSolvingRequestAttributesFor(
            final ChallengeDefinitionData challenge) {
        if (challenge == null || !challenge.hasSolutionLinks()) {
            return Collections.emptyMap();
        }

        for (ChallengeSolutionLink solution : challenge.solutions) {
            if (!"HREF".equals(solution.linkType)
                    || !solution.linkData.startsWith("/apichallenges/solutions/")) {
                continue;
            }

            final String markdown = resourceAsStringOrEmpty("content" + solution.linkData + ".md");
            if (markdown.isEmpty()) {
                continue;
            }

            final Pattern macroPattern =
                    Pattern.compile("\\{\\{<api-live-request\\s+([\\s\\S]*?)>}}");
            final Matcher matcher = macroPattern.matcher(markdown);
            while (matcher.find()) {
                final Map<String, String> attributes = parseMacroAttributes(matcher.group(1));
                if (isTruthy(attributes.get("open"))
                        || isTruthy(attributes.get("challenge-request"))) {
                    return attributes;
                }
            }
        }

        return Collections.emptyMap();
    }

    private Map<String, String> parseMacroAttributes(final String rawAttributes) {
        final Map<String, String> attributes = new LinkedHashMap<>();
        final Pattern attributePattern =
                Pattern.compile("([a-zA-Z][a-zA-Z0-9-]*)=(\"([^\"]*)\"|'([^']*)')");
        final Matcher attributeMatcher = attributePattern.matcher(rawAttributes);
        while (attributeMatcher.find()) {
            final String key = attributeMatcher.group(1);
            final String doubleQuotedValue = attributeMatcher.group(3);
            final String singleQuotedValue = attributeMatcher.group(4);
            attributes.put(key, doubleQuotedValue == null ? singleQuotedValue : doubleQuotedValue);
        }
        return attributes;
    }

    private boolean isTruthy(final String value) {
        if (value == null) {
            return false;
        }
        final String trimmed = value.trim().toLowerCase();
        return trimmed.equals("true") || trimmed.equals("yes") || trimmed.equals("on");
    }

    private String renderApiLiveRequestPlaceholder(final Map<String, String> attributes) {
        final String method = attributes.getOrDefault("method", "GET");
        final String path = attributes.getOrDefault("path", "/");
        final String editable = attributes.getOrDefault("editable", "true");
        final String editMode = attributes.getOrDefault("edit-mode", "fixed");
        final String allowedPathPrefixes =
                attributes.getOrDefault(
                        "allowed-path-prefixes", API_CHALLENGE_ALLOWED_PATH_PREFIXES);

        final StringBuilder html = new StringBuilder();
        html.append("<div class=\"api-live-request\" data-method=\"")
                .append(escapeHtmlAttribute(method))
                .append("\" data-path=\"")
                .append(escapeHtmlAttribute(path))
                .append("\" data-editable=\"")
                .append(escapeHtmlAttribute(editable))
                .append("\" data-edit-mode=\"")
                .append(escapeHtmlAttribute(editMode))
                .append("\" data-allowed-path-prefixes=\"")
                .append(escapeHtmlAttribute(allowedPathPrefixes))
                .append("\"");

        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            final String key = attribute.getKey();
            if (key.equals("method")
                    || key.equals("path")
                    || key.equals("editable")
                    || key.equals("edit-mode")
                    || key.equals("allowed-path-prefixes")
                    || key.equals("details")
                    || key.equals("summary")
                    || key.equals("open")
                    || key.equals("challenge-request")) {
                continue;
            }
            html.append(" data-")
                    .append(escapeHtmlAttribute(key))
                    .append("=\"")
                    .append(escapeHtmlAttribute(attribute.getValue()))
                    .append("\"");
        }
        html.append("></div>");
        return html.toString();
    }

    private String resourceAsStringOrEmpty(final String resourceName) {
        try (Scanner scanner =
                new Scanner(
                                Objects.requireNonNull(
                                        ChallengerWebGUI.class
                                                .getClassLoader()
                                                .getResourceAsStream(resourceName)))
                        .useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private String outputChallengeDataAsJS(final ChallengerAuthData challenger, String json) {

        StringBuilder html = new StringBuilder();

        // add the challenge data as JSON
        final String dataString = challenger.asJson();
        html.append("<script>document.challengerData=" + dataString + ";</script>");
        // add the current todos as JSON
        html.append("<script>document.databaseData=" + json + ";</script>");

        return html.toString();
    }

    private String outputEmptyChallengeDataAsJS() {
        return "<script>document.challengerData={};</script>"
                + "<script>document.databaseData={};</script>";
    }

    private String renderChallengeData(
            final ChallengeDefinitions challengeDefinitions, final ChallengerAuthData challenger) {
        StringBuilder html = new StringBuilder();

        final Collection<ChallengeSection> sections = challengeDefinitions.getChallengeSections();

        // add a toc
        html.append("<h2 id='toc'>Challenge Sections</h2>");
        html.append("<ul>");
        for (ChallengeSection section : sections) {
            html.append(
                    String.format(
                            "<li><a href='#%s'>%s</a></li>",
                            section.getTitle().replaceAll(" ", "").toLowerCase(),
                            section.getTitle()));
        }
        html.append("</ul>");

        for (ChallengeSection section : sections) {

            html.append(
                    String.format(
                                    "<h2 id='%s'>",
                                    section.getTitle().replaceAll(" ", "").toLowerCase())
                            + section.getTitle()
                            + "</h2>");
            html.append(
                    "<p class='challengesectiondescription'>" + section.getDescription() + "</p>");

            List<ChallengeDefinitionData> sectionData = new ArrayList<>();
            for (ChallengeDefinitionData challenge : section.getChallenges()) {
                final ChallengeDefinitionData data =
                        new ChallengeDefinitionData(
                                challenge.id, challenge.name, challenge.description);
                CHALLENGE challengeKey = challengeDefinitions.getChallenge(challenge.name);
                if (challenge != null) {
                    data.status = challenger.statusOfChallenge(challengeKey);
                    data.addHints(challenge.hints);
                    data.addSolutions(challenge.solutions);
                }
                sectionData.add(data);
            }

            html.append(renderChallengeData(sectionData));
            html.append("<p><a href='#toc'>Back to Section List</a></p>");
        }

        return html.toString();
    }
}
