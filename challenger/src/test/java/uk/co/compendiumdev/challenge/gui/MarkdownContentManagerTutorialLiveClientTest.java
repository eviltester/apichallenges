package uk.co.compendiumdev.challenge.gui;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class MarkdownContentManagerTutorialLiveClientTest {

    @Test
    void restApiTestingTutorialRendersSimpleApiLiveClients() {

        String html = renderRestApiTestingTutorial();

        Assertions.assertEquals(13, countOccurrences(html, "class=\"api-live-request\""));
        Assertions.assertEquals(13, countOccurrences(html, "data-use-challenger=\"false\""));
        Assertions.assertEquals(
                13, countOccurrences(html, "data-allowed-path-prefixes=\"/simpleapi\""));
        Assertions.assertEquals(12, countOccurrences(html, "data-query-editable=\"false\""));
        Assertions.assertEquals(7, countOccurrences(html, "data-body-editable=\"false\""));
        Assertions.assertEquals(1, countOccurrences(html, "data-edit-mode=\"adhoc\""));
        Assertions.assertTrue(html.contains("data-path=\"/simpleapi/items\""));
        Assertions.assertTrue(html.contains("data-path=\"/simpleapi/randomisbn\""));
        Assertions.assertTrue(
                html.contains("data-path=\"/simpleapi/items/{{lastCreatedSimpleApiItemId}}\""));
        Assertions.assertTrue(html.contains("{{randomSimpleApiIsbn}}"));
        Assertions.assertTrue(html.contains("{{lastCreatedSimpleApiItemIsbn}}"));
    }

    @Test
    void restApiTestingTutorialRendersMainContentBeforeSidebarLinks() {

        String html = renderRestApiTestingTutorial();

        final int section = html.indexOf("<section class='doc-columns'>");
        final int rightColumn = html.indexOf("<div class='right-column'>");
        final int main = html.indexOf("<main id='maincontentstartshere'>");
        final int h1 = html.indexOf("<h1>How to Test REST APIs Step by Step</h1>");
        final int byline = html.indexOf("<p class='article-byline'>");
        final int toc = html.indexOf("<div id='toc'>");
        final int leftColumn = html.indexOf("<aside class='left-column'");
        final int sideToc = html.indexOf("<nav class='side-toc'");

        Assertions.assertTrue(section >= 0);
        Assertions.assertTrue(section < rightColumn);
        Assertions.assertTrue(rightColumn < main);
        Assertions.assertTrue(main < h1);
        Assertions.assertTrue(h1 < byline);
        Assertions.assertTrue(byline < toc);
        Assertions.assertTrue(h1 < leftColumn);
        Assertions.assertTrue(leftColumn < sideToc);
        Assertions.assertTrue(
                html.contains(
                        "<a href='/author/alan-richardson' rel='author'>Alan Richardson</a>"));
        Assertions.assertTrue(
                html.contains("Updated <time datetime='2026-08-11'>2026-08-11</time>"));
        Assertions.assertTrue(
                html.contains("<nav class='side-toc' aria-label='Learning and reference links'>"));
    }

    @Test
    void sideTocRendersCollapsibleSectionsWithDescriptions() {

        String html = renderRestApiTestingTutorial();

        Assertions.assertTrue(
                html.contains(
                        "<details class=\"side-toc-section\" data-side-toc-section=\"tools\">"));
        Assertions.assertTrue(
                html.contains(
                        "<details class=\"side-toc-section\" data-side-toc-section=\"reference-tutorials\">"));
        Assertions.assertTrue(
                html.contains(
                        "<details class=\"side-toc-section\" data-side-toc-section=\"interactive-tutorials\">"));
        Assertions.assertTrue(
                html.contains("<a class=\"side-toc-section-title\" href=\"/tools\">Tools</a>"));
        Assertions.assertTrue(
                html.contains(
                        "<a class=\"side-toc-section-title\" href=\"/tutorials\">Interactive Tutorials</a>"));
        Assertions.assertTrue(
                html.contains(
                        "Tool reviews for REST HTTP clients, proxies, and online API clients to help with API testing."));
        Assertions.assertTrue(
                html.contains(
                        "Hands-on tutorial pages for learning REST concepts and API testing workflows."));
        Assertions.assertTrue(
                html.contains(
                        "Reference material in a supporting learning order for web, HTTP, REST, testing, and OpenAPI."));
        Assertions.assertTrue(
                html.contains(
                        "<a class=\"side-toc-section-title\" href=\"/reference\">Reference</a>"));
        Assertions.assertFalse(
                html.contains("<span class=\"side-toc-section-title\">Reference Tutorials</span>"));
        assertContainsInOrder(
                html,
                "<ul class=\"side-toc-root\">",
                "<li><a href=\"/learning\">Learning Zone</a></li>",
                "<li class=\"side-toc-syllabus\" aria-label=\"REST API Tutorial path\">",
                "<a class=\"side-toc-syllabus-title\" href=\"/tutorials/rest-api-tutorial-path\">REST API Tutorial Path</a>",
                "<li><a href=\"/tutorials/rest-api-tutorial\">Interactive REST API Tutorial</a></li>",
                "<li><a href=\"/reference/http-basics\">HTTP basics</a></li>",
                "<li><a href=\"/reference/rest-api-basics\">REST basics</a></li>",
                "<li><a href=\"/reference/http-verbs\">HTTP methods</a></li>",
                "<li><a href=\"/reference/http-basics#toc7\">Status codes</a></li>",
                "<li><a href=\"/reference/openapi\">OpenAPI</a></li>",
                "<li><a href=\"/tutorials/rest-api-testing\">Interactive How to Test REST APIs Tutorial</a></li>",
                "<li><a href=\"/tutorials/api-simulator-walkthrough\">Interactive API Simulation</a></li>",
                "<li><a href=\"/apichallenges\">API Challenges</a></li>",
                "<li><a href=\"/apichallenges/solutions\">Challenge Solutions</a></li>",
                "<li><a href=\"/practice-modes/simpleapi/experiments\">Simple API Experiments</a></li>",
                "<details class=\"side-toc-section\" data-side-toc-section=\"reference-tutorials\">",
                "<a class=\"side-toc-section-title\" href=\"/reference\">Reference</a>",
                "<li><a href=\"/reference/web-basics\">Web Applications</a></li>",
                "<li><a href=\"/reference/http-basics\">HTTP Basics</a></li>",
                "<a href=\"/reference/http-verbs\">HTTP Verbs</a>",
                "<li><a href=\"/reference/http-verbs/http-get\">GET</a></li>",
                "<li><a href=\"/reference/http-verbs/http-head\">HEAD</a></li>",
                "<li><a href=\"/reference/http-verbs/http-options\">OPTIONS</a></li>",
                "<li><a href=\"/reference/http-verbs/http-query\">QUERY</a></li>",
                "<li><a href=\"/reference/http-verbs/http-post\">POST</a></li>",
                "<li><a href=\"/reference/http-verbs/http-put\">PUT</a></li>",
                "<li><a href=\"/reference/http-verbs/http-patch\">PATCH</a></li>",
                "<li><a href=\"/reference/http-verbs/http-delete\">DELETE</a></li>",
                "<li><a href=\"/reference/http-verbs/http-trace\">TRACE</a></li>",
                "<li><a href=\"/reference/http-verbs/http-connect\">CONNECT</a></li>",
                "<li><a href=\"/reference/rest-api-basics\">REST API Basics</a></li>",
                "<li><a href=\"/reference/testing-apis\">API Testing Concepts</a></li>",
                "<li><a href=\"/reference/openapi\">OpenAPI</a></li>",
                "<li><a href=\"/reference/summary\">Summary</a></li>",
                "<details class=\"side-toc-section\" data-side-toc-section=\"interactive-tutorials\">",
                "<a class=\"side-toc-section-title\" href=\"/tutorials\">Interactive Tutorials</a>",
                "<li><a href=\"/tutorials/rest-api-tutorial\">Rest API Tutorial</a></li>",
                "<li><a href=\"/tutorials/rest-api-testing\">Testing REST APIs Tutorial</a></li>",
                "<li><a href=\"/tutorials/api-simulator-walkthrough\">API Simulator Walkthrough</a></li>",
                "<details class=\"side-toc-section\" data-side-toc-section=\"practice-modes\">",
                "<a class=\"side-toc-section-title\" href=\"/practice-modes\">Practice Modes</a>",
                "<details class=\"side-toc-section\" data-side-toc-section=\"tools\">",
                "<a class=\"side-toc-section-title\" href=\"/tools\">Tools</a>",
                "<details class=\"side-toc-section\" data-side-toc-section=\"practice-sites\">",
                "<a class=\"side-toc-section-title\" href=\"/practice-sites\">Practice Sites</a>",
                "<li><a href=\"/sponsors\">Sponsors</a></li>",
                "<li><a href=\"/blog\">Blog</a></li>");
        Assertions.assertFalse(
                html.contains(
                        "<details class=\"side-toc-section\" data-side-toc-section=\"tutorials\">"));
        Assertions.assertFalse(html.contains("Guided Tutorials"));
        assertContainsInOrder(
                html,
                "<a href=\"/tools/clients\">REST/HTTP Clients</a>",
                "<li><a href=\"/tools/proxies\">Proxies</a></li>",
                "<a href=\"/tools/online-clients\">Online Clients</a>",
                "<li><a href=\"/tools/online-clients/basic-client\">Basic Client</a></li>",
                "<a href=\"/tools/online-clients/swagger\">Swagger UI</a>",
                "<li><a href=\"/tools/online-clients/swagger/about\">About Swagger UI</a></li>",
                "<a href=\"/tools/online-clients/openapi-explorer\">OpenAPI Explorer</a>",
                "<li><a href=\"/tools/online-clients/openapi-explorer/about\">About OpenAPI Explorer</a></li>",
                "<a href=\"/tools/online-clients/scalar\">Scalar</a>",
                "<li><a href=\"/tools/online-clients/scalar/about\">About Scalar</a></li>",
                "<a href=\"/tools/online-clients/stoplight\">Stoplight Elements</a>",
                "<li><a href=\"/tools/online-clients/stoplight/about\">About Stoplight Elements</a></li>",
                "<a href=\"/tools/online-clients/zudoku\">Zudoku</a>",
                "<li><a href=\"/tools/online-clients/zudoku/about\">About Zudoku</a></li>",
                "<a href=\"/tools/online-clients/redoc\">Redoc</a>",
                "<li><a href=\"/tools/online-clients/redoc/about\">About Redoc</a></li>",
                "<li><a href=\"/tools/online-clients/openapi-converter\">OpenAPI Converter</a></li>");
        Assertions.assertEquals(1, countOccurrences(html, "href=\"/blog\""));
        Assertions.assertFalse(html.contains("href=\"/changes\""));
        Assertions.assertFalse(html.contains("All Practice Sites"));
    }

    @Test
    void blogIndexAndPostsRenderWithoutLeftSidebarOnPosts() {

        String blogHtml = renderContentPage("/blog");

        Assertions.assertTrue(blogHtml.contains("<h1>API Challenges Blog</h1>"));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/feed.xml\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/categories\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/all-posts\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/page/2\""));
        Assertions.assertTrue(blogHtml.contains("Page 1 of 2"));
        Assertions.assertEquals(15, countOccurrences(blogHtml, "class=\"blog-list-item\""));
        Assertions.assertTrue(
                blogHtml.contains(
                        "href=\"/blog/changelog-2026-08-14-simple-api-testing-experiments\""));
        Assertions.assertTrue(
                blogHtml.contains("Simple API Testing Experiments For API Test Planning"));
        Assertions.assertTrue(
                blogHtml.contains(
                        "href=\"/blog/changelog-2026-08-13-accept-q-json-xml-challenges\""));
        Assertions.assertTrue(
                blogHtml.contains("Accept q Values, JSON, XML, And New API Challenges"));
        Assertions.assertFalse(
                blogHtml.contains("href=\"/blog/api-challenges-practice-api-overview\""));
        Assertions.assertTrue(
                blogHtml.contains(
                        "href=\"/blog/changelog-2026-08-07-initial-api-spector-review\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/api-simulator-walkthrough-launch\""));
        Assertions.assertTrue(
                blogHtml.contains(
                        "href=\"/blog/changelog-2026-08-06-interactive-rest-api-tutorial\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/categories/api-testing\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/categories/rest-api-tutorial\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/categories/change-log\""));
        assertContainsInOrder(
                blogHtml,
                "API Spector Review: Best New API Client for API Testing",
                "Interactive API Simulator Walkthrough Launch",
                "Interactive REST API Tutorial and Raw Response View",
                "API Challenges Site Refresh and Hosted API Clients");

        String videoPostHtml = renderContentPage("/blog/api-challenges-practice-api-overview");

        Assertions.assertTrue(
                videoPostHtml.contains(
                        "<h1>API Challenges Practice API Overview Video Summary</h1>"));
        Assertions.assertTrue(
                videoPostHtml.contains(
                        "Published <time datetime='2024-04-13T15:36:44Z'>2024-04-13</time>"));
        Assertions.assertTrue(videoPostHtml.contains("<lite-youtube videoid=\"rxEwPMM_Qyc\">"));
        Assertions.assertTrue(videoPostHtml.contains("\"@type\":\"BlogPosting\""));
        Assertions.assertTrue(videoPostHtml.contains("\"@type\":\"VideoObject\""));
        Assertions.assertTrue(videoPostHtml.contains("href='/blog/categories/api-testing'"));
        Assertions.assertTrue(videoPostHtml.contains("href='/blog/categories/rest-api-tutorial'"));
        Assertions.assertTrue(
                videoPostHtml.contains("<nav class=\"breadcrumb\" aria-label=\"Breadcrumb\">"));
        Assertions.assertTrue(videoPostHtml.contains("<li><a href=\"/blog\">Blog</a></li>"));
        Assertions.assertTrue(
                videoPostHtml.contains(
                        "<li><a href=\"/blog/categories/api-testing\">API Testing</a></li>"));
        Assertions.assertTrue(
                videoPostHtml.contains(
                        "<li aria-current=\"page\">api-challenges-practice-api-overview</li>"));
        Assertions.assertTrue(
                videoPostHtml.contains(
                        "\"name\":\"API Testing\",\"item\":\"https://apichallenges.com/blog/categories/api-testing\""));
        Assertions.assertTrue(videoPostHtml.contains("<nav class='blog-post-navigation'"));
        Assertions.assertTrue(
                videoPostHtml.contains(
                        "<a rel='next' href='/blog/changelog-2025-02-16-practice-apps'>Practice API Apps for REST API Testing Exercises</a>"));
        Assertions.assertTrue(
                videoPostHtml.contains(
                        "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"API Challenges Blog RSS\" href=\"/blog/feed.xml\">"));
        Assertions.assertFalse(videoPostHtml.contains("<aside class='left-column'"));
        Assertions.assertFalse(videoPostHtml.contains("<nav class='side-toc'"));
        assertContainsInOrder(
                videoPostHtml,
                "<h1>API Challenges Practice API Overview Video Summary</h1>",
                "<p class='article-byline'>",
                "<p class='blog-post-categories'>",
                "<div id='toc'></div>");

        String simpleApiExperimentsPostHtml =
                renderContentPage("/blog/changelog-2026-08-14-simple-api-testing-experiments");

        Assertions.assertTrue(
                simpleApiExperimentsPostHtml.contains(
                        "<h1>Simple API Testing Experiments For API Test Planning</h1>"));
        Assertions.assertTrue(
                simpleApiExperimentsPostHtml.contains(
                        "Published <time datetime='2026-08-14T10:00:00Z'>2026-08-14</time>"));
        Assertions.assertTrue(
                simpleApiExperimentsPostHtml.contains(
                        "href=\"/practice-modes/simpleapi/experiments\""));
        Assertions.assertTrue(
                simpleApiExperimentsPostHtml.contains("href=\"/reference/testing-apis\""));
        Assertions.assertTrue(simpleApiExperimentsPostHtml.contains("API test plan"));
        Assertions.assertTrue(simpleApiExperimentsPostHtml.contains("API test approach"));
        Assertions.assertTrue(simpleApiExperimentsPostHtml.contains("exploratory API testing"));
        Assertions.assertTrue(
                simpleApiExperimentsPostHtml.contains("href='/blog/categories/api-testing'"));
        Assertions.assertTrue(
                simpleApiExperimentsPostHtml.contains("href='/blog/categories/rest-api-tutorial'"));
        Assertions.assertTrue(simpleApiExperimentsPostHtml.contains("\"@type\":\"BlogPosting\""));
        Assertions.assertFalse(simpleApiExperimentsPostHtml.contains("<aside class='left-column'"));
        Assertions.assertFalse(simpleApiExperimentsPostHtml.contains("<nav class='side-toc'"));

        String acceptPostHtml =
                renderContentPage("/blog/changelog-2026-08-13-accept-q-json-xml-challenges");

        Assertions.assertTrue(
                acceptPostHtml.contains(
                        "<h1>Accept q Values, JSON, XML, And New API Challenges</h1>"));
        Assertions.assertTrue(
                acceptPostHtml.contains(
                        "Published <time datetime='2026-08-13T10:00:00Z'>2026-08-13</time>"));
        Assertions.assertTrue(
                acceptPostHtml.contains("href=\"/reference/http-basics#accept-header\""));
        Assertions.assertTrue(acceptPostHtml.contains("href=\"/gui/challenges\""));
        Assertions.assertTrue(acceptPostHtml.contains("href='/blog/categories/change-log'"));
        Assertions.assertTrue(acceptPostHtml.contains("\"@type\":\"BlogPosting\""));
        Assertions.assertFalse(acceptPostHtml.contains("<aside class='left-column'"));
        Assertions.assertFalse(acceptPostHtml.contains("<nav class='side-toc'"));

        String launchPostHtml = renderContentPage("/blog/api-simulator-walkthrough-launch");

        Assertions.assertTrue(
                launchPostHtml.contains("<h1>Interactive API Simulator Walkthrough Launch</h1>"));
        Assertions.assertTrue(
                launchPostHtml.contains(
                        "Published <time datetime='2026-08-07T09:00:00Z'>2026-08-07</time>"));
        Assertions.assertTrue(launchPostHtml.contains("\"@type\":\"BlogPosting\""));
        Assertions.assertFalse(launchPostHtml.contains("<aside class='left-column'"));
        Assertions.assertFalse(launchPostHtml.contains("<nav class='side-toc'"));

        String apiSpectorPostHtml =
                renderContentPage("/blog/changelog-2026-08-07-initial-api-spector-review");

        Assertions.assertTrue(
                apiSpectorPostHtml.contains(
                        "<h1>API Spector Review: Best New API Client for API Testing</h1>"));
        Assertions.assertTrue(apiSpectorPostHtml.contains("https://api-spector.dev/"));
        Assertions.assertTrue(apiSpectorPostHtml.contains("href=\"/tools/clients/api-spector\""));
        Assertions.assertTrue(apiSpectorPostHtml.contains("href='/blog/categories/tools'"));
        Assertions.assertTrue(apiSpectorPostHtml.contains("\"@type\":\"BlogPosting\""));
        Assertions.assertFalse(apiSpectorPostHtml.contains("<aside class='left-column'"));
        Assertions.assertFalse(apiSpectorPostHtml.contains("<nav class='side-toc'"));

        String changelogPostHtml =
                renderContentPage("/blog/changelog-2025-04-19-bruno-client-demo");

        Assertions.assertTrue(
                changelogPostHtml.contains(
                        "<h1>Bruno API Client Demo for Exploratory API Testing</h1>"));
        Assertions.assertTrue(changelogPostHtml.contains("<lite-youtube videoid=\"3TlwUKyfOMw\">"));
        Assertions.assertTrue(changelogPostHtml.contains("\"@type\":\"BlogPosting\""));
        Assertions.assertTrue(changelogPostHtml.contains("\"@type\":\"VideoObject\""));
        Assertions.assertTrue(changelogPostHtml.contains("href='/blog/categories/change-log'"));
        Assertions.assertTrue(
                changelogPostHtml.contains("<nav class=\"breadcrumb\" aria-label=\"Breadcrumb\">"));
        Assertions.assertTrue(changelogPostHtml.contains("<li><a href=\"/blog\">Blog</a></li>"));
        Assertions.assertTrue(
                changelogPostHtml.contains(
                        "<li><a href=\"/blog/categories/change-log\">Change Log</a></li>"));
        Assertions.assertTrue(
                changelogPostHtml.contains(
                        "<li aria-current=\"page\">changelog-2025-04-19-bruno-client-demo</li>"));
        Assertions.assertTrue(
                changelogPostHtml.contains(
                        "<a rel='prev' href='/blog/changelog-2025-04-13-simple-api-bruno-curl'>Simple API Overview with Bruno and cURL Guides</a>"));
        Assertions.assertTrue(
                changelogPostHtml.contains(
                        "<a rel='next' href='/blog/changelog-2025-04-20-api-clients-page'>Expanded API Clients Page for Tool Selection</a>"));
        Assertions.assertFalse(changelogPostHtml.contains("<aside class='left-column'"));
        Assertions.assertFalse(changelogPostHtml.contains("<nav class='side-toc'"));
    }

    @Test
    void restApiTutorialPillarPageRendersInteractiveIntroduction() {

        String html = renderContentPage("/tutorials/rest-api-tutorial");

        Assertions.assertTrue(
                html.contains("<title>REST API Tutorial: Learn REST by Using a Live API</title>"));
        Assertions.assertTrue(
                html.contains("<h1>REST API Tutorial: Learn REST by Using a Live API</h1>"));
        Assertions.assertTrue(
                html.contains(
                        "<meta name='description' content='Learn REST API basics with live HTTP requests"));
        Assertions.assertTrue(html.contains("href=\"/tutorials/rest-api-tutorial\""));
        Assertions.assertTrue(html.contains("href=\"/reference/rest-api-basics\""));
        Assertions.assertTrue(html.contains("href=\"/reference/http-basics\""));
        Assertions.assertTrue(html.contains("href=\"/reference/http-verbs\""));
        Assertions.assertTrue(html.contains("href=\"/reference/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger/about\""));
        Assertions.assertTrue(html.contains("href=\"/reference/testing-apis\""));
        Assertions.assertTrue(html.contains("When you want more hands-on repetition"));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simulation\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simpleapi\""));
        Assertions.assertTrue(html.contains("href=\"/apichallenges\""));
        Assertions.assertTrue(html.contains("href=\"/apichallenges/solutions\""));
        Assertions.assertEquals(15, countOccurrences(html, "class=\"api-live-request\""));
        Assertions.assertEquals(15, countOccurrences(html, "data-use-challenger=\"false\""));
        Assertions.assertEquals(
                5, countOccurrences(html, "data-allowed-path-prefixes=\"/simpleapi\""));
        Assertions.assertEquals(
                5, countOccurrences(html, "data-allowed-path-prefixes=\"/api/todos\""));
        Assertions.assertEquals(
                1, countOccurrences(html, "data-allowed-path-prefixes=\"/api/docs\""));
        Assertions.assertEquals(
                1, countOccurrences(html, "data-allowed-path-prefixes=\"/api/heartbeat\""));
        Assertions.assertEquals(
                3, countOccurrences(html, "data-allowed-path-prefixes=\"/api/secret\""));
        Assertions.assertEquals(14, countOccurrences(html, "data-editable=\"false\""));
        Assertions.assertEquals(1, countOccurrences(html, "data-editable=\"true\""));
        Assertions.assertEquals(
                1, countOccurrences(html, "data-editable=\"true\" data-edit-mode=\"fixed\""));
        Assertions.assertEquals(
                15,
                countOccurrences(html, "<details class=\"sim-live-request-details\"><summary>"));
        Assertions.assertEquals(
                0, countOccurrences(html, "<details class=\"sim-live-request-details\" open"));
        Assertions.assertTrue(html.contains("data-path=\"/api/todos\""));
        Assertions.assertTrue(html.contains("data-path=\"/api/todos/1\""));
        Assertions.assertTrue(html.contains("data-method=\"HEAD\" data-path=\"/api/todos/1\""));
        Assertions.assertTrue(html.contains("data-path=\"/api/heartbeat\""));
        Assertions.assertTrue(html.contains("data-expected-status=\"405\""));
        Assertions.assertTrue(html.contains("data-path=\"/api/secret/token\""));
        Assertions.assertTrue(html.contains("data-path=\"/api/secret/note\""));
        Assertions.assertTrue(html.contains("{{authToken}}"));
        Assertions.assertTrue(html.contains("data-path=\"/api/docs/openapi.json\""));
        Assertions.assertFalse(html.contains("data-path=\"/simpleapi/docs/openapi.json\""));
        Assertions.assertTrue(html.contains("data-path=\"/simpleapi/items\""));
        Assertions.assertTrue(
                html.contains("data-path=\"/simpleapi/items/{{lastCreatedSimpleApiItemId}}\""));
        Assertions.assertTrue(html.contains("{{randomSimpleApiIsbn}}"));
        Assertions.assertTrue(html.contains("numberinstock&quot;:&quot;3"));

        String learningHtml = renderContentPage("/learning");
        assertContainsInOrder(
                learningHtml,
                "REST API Tutorial Syllabus",
                "href=\"/tutorials/rest-api-tutorial\"",
                "href=\"/reference/http-basics\"",
                "href=\"/reference/rest-api-basics\"",
                "href=\"/reference/http-verbs\"",
                "href=\"/reference/http-basics#toc7\"",
                "href=\"/reference/openapi\"",
                "href=\"/tutorials/rest-api-testing\"",
                "href=\"/tutorials/api-simulator-walkthrough\"",
                "href=\"/apichallenges\"");
        Assertions.assertTrue(learningHtml.contains("Interactive REST API Tutorial"));
        Assertions.assertTrue(learningHtml.contains("How to Test REST APIs"));
        Assertions.assertTrue(learningHtml.contains("Interactive API Simulation"));
        Assertions.assertTrue(learningHtml.contains("href=\"/practice-modes/simulation\""));
        assertContainsInOrder(
                learningHtml,
                "<h2>Reference</h2>",
                "href=\"/reference/web-basics\"",
                "href=\"/reference/http-basics\"",
                "href=\"/reference/http-verbs\"",
                "href=\"/reference/rest-api-basics\"",
                "href=\"/reference/testing-apis\"",
                "href=\"/reference/openapi\"",
                "href=\"/tools/online-clients/swagger/about\"");
        Assertions.assertFalse(learningHtml.contains("<h2>Reference Tutorials</h2>"));
    }

    @Test
    void restApiTutorialPathExplainsTheElevenStepLearningRoute() {

        String html = renderContentPage("/tutorials/rest-api-tutorial-path");

        Assertions.assertTrue(html.contains("<h1>REST API Tutorial Path</h1>"));
        Assertions.assertTrue(
                html.contains(
                        "what that page is for, what you should practise there, and why it prepares you for the next step"));
        assertContainsInOrder(
                html,
                "href=\"/tutorials/rest-api-tutorial\"",
                "hands-on starting point",
                "href=\"/reference/http-basics\"",
                "protocol details",
                "href=\"/reference/rest-api-basics\"",
                "REST style",
                "href=\"/reference/http-verbs\"",
                "which method fits a test idea",
                "href=\"/reference/http-basics#toc7\"",
                "response code into a useful testing observation",
                "href=\"/reference/openapi\"",
                "tool-friendly API contracts",
                "href=\"/tutorials/rest-api-testing\"",
                "designing API tests",
                "href=\"/tutorials/api-simulator-walkthrough\"",
                "controlled environment",
                "href=\"/apichallenges\"",
                "main challenge application",
                "href=\"/apichallenges/solutions\"",
                "worked examples for the challenges",
                "href=\"/practice-modes/simpleapi/experiments\"",
                "exploratory test plan");
    }

    @Test
    void referenceTutorialsRenderPracticeSpineLinks() {

        assertContentPageContains(
                "/reference/rest-api-basics",
                "Practise This Concept",
                "href=\"/tutorials/rest-api-tutorial\"",
                "href=\"/reference/http-verbs\"",
                "href=\"/tutorials/rest-api-testing\"",
                "href=\"/practice-modes/simpleapi\"",
                "href=\"/apichallenges/solutions\"");

        assertContentPageContains(
                "/reference/http-basics",
                "Practise This Concept",
                "href=\"/tutorials/rest-api-tutorial\"",
                "href=\"/reference/http-verbs\"",
                "href=\"/tutorials/rest-api-testing\"",
                "href=\"/practice-modes/simulation\"",
                "href=\"/apichallenges/solutions\"");

        assertContentPageContains(
                "/reference/http-verbs",
                "Practise This Concept",
                "href=\"/tutorials/rest-api-tutorial\"",
                "href=\"/reference/rest-api-basics\"",
                "href=\"/tutorials/rest-api-testing\"",
                "href=\"/practice-modes/simpleapi\"",
                "href=\"/apichallenges/solutions\"");

        assertContentPageContains(
                "/reference/testing-apis",
                "Practise This Concept",
                "href=\"/tutorials/rest-api-tutorial\"",
                "href=\"/tutorials/rest-api-testing\"",
                "href=\"/practice-modes/simulation\"",
                "href=\"/practice-modes/simpleapi\"",
                "href=\"/apichallenges/solutions\"");

        assertContentPageContains(
                "/tutorials/rest-api-testing",
                "Where to Go Next",
                "href=\"/tutorials/rest-api-tutorial\"",
                "href=\"/reference/rest-api-basics\"",
                "href=\"/reference/http-verbs\"",
                "href=\"/practice-modes/simpleapi\"",
                "href=\"/apichallenges\"",
                "href=\"/apichallenges/solutions\"");
    }

    @Test
    void openApiReferenceLinksToOnlineClientAboutPages() {

        String html = renderContentPage("/reference/openapi");

        Assertions.assertTrue(html.contains("<h1>OpenAPI for API Testing</h1>"));
        Assertions.assertTrue(html.contains("<section class='doc-columns'>"));
        Assertions.assertTrue(html.contains("<main id='maincontentstartshere'>"));
        Assertions.assertFalse(html.contains("wide-tool-page"));
        Assertions.assertTrue(html.contains("OpenAPI UIs"));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger/about\""));
        Assertions.assertTrue(
                html.contains("href=\"/tools/online-clients/openapi-explorer/about\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/scalar/about\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/stoplight/about\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/zudoku/about\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/redoc/about\""));
        Assertions.assertTrue(
                html.contains(
                        "The open source version is primarily a viewer, not a request-sending client"));

        html = renderContentPage("/tools/online-clients/swagger/about");
        Assertions.assertTrue(html.contains("<h1>About Swagger UI</h1>"));
        Assertions.assertTrue(
                html.contains("OpenAPI is the standard specification. Swagger is tooling"));
        assertOpenApiUiToolAboutBreadcrumb(html, "Swagger", "swagger");
        assertOpenApiUiReferenceLaunchLinks(html, "Swagger", "swagger");

        html = renderContentPage("/tools/online-clients/openapi-explorer/about");
        Assertions.assertTrue(html.contains("<h1>About OpenAPI Explorer</h1>"));
        Assertions.assertTrue(html.contains("web component"));
        assertOpenApiUiToolAboutBreadcrumb(html, "OpenAPI Explorer UI", "openapi-explorer");
        assertOpenApiUiReferenceLaunchLinks(html, "OpenAPI Explorer UI", "openapi-explorer");

        html = renderContentPage("/tools/online-clients/scalar/about");
        Assertions.assertTrue(html.contains("<h1>About Scalar</h1>"));
        Assertions.assertTrue(html.contains("REST API client"));
        assertOpenApiUiToolAboutBreadcrumb(html, "Scalar", "scalar");
        assertOpenApiUiReferenceLaunchLinks(html, "Scalar", "scalar");

        html = renderContentPage("/tools/online-clients/stoplight/about");
        Assertions.assertTrue(html.contains("<h1>About Stoplight Elements</h1>"));
        Assertions.assertTrue(html.contains("React components"));
        assertOpenApiUiToolAboutBreadcrumb(html, "Stoplight Elements", "stoplight");
        assertOpenApiUiReferenceLaunchLinks(html, "Stoplight Elements", "stoplight");

        html = renderContentPage("/tools/online-clients/zudoku/about");
        Assertions.assertTrue(html.contains("<h1>About Zudoku</h1>"));
        Assertions.assertTrue(html.contains("developer portals"));
        assertOpenApiUiToolAboutBreadcrumb(html, "Zudoku", "zudoku");
        assertOpenApiUiReferenceLaunchLinks(html, "Zudoku", "zudoku");

        html = renderContentPage("/tools/online-clients/redoc/about");
        Assertions.assertTrue(html.contains("<h1>About Redoc</h1>"));
        Assertions.assertTrue(html.contains("not a request-sending API client"));
        assertOpenApiUiToolAboutBreadcrumb(html, "Redoc", "redoc");
        assertOpenApiUiReferenceLaunchLinks(html, "Redoc", "redoc");
    }

    @Test
    void onlineClientContentPagesRenderToolsAndSidebarLinks() {

        String html = renderContentPage("/tools/online-clients");

        Assertions.assertTrue(
                html.contains(
                        "<title>Online API Clients and OpenAPI UI Tools for Testing</title>"));
        Assertions.assertTrue(html.contains("<h1>Online API Clients and OpenAPI UI Tools</h1>"));
        Assertions.assertTrue(html.contains("Browser tools are convenient for learning"));
        Assertions.assertTrue(html.contains("How The Tools Differ"));
        Assertions.assertTrue(html.contains("CORS And Browser Limits"));
        Assertions.assertTrue(html.contains("Testing With Browser Developer Tools"));
        Assertions.assertTrue(html.contains("When To Use A Desktop REST API Client"));
        Assertions.assertTrue(html.contains("HAR file"));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/basic-client\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/openapi-explorer\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/scalar\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/stoplight\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/zudoku\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/redoc\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/openapi-converter\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger/about\""));
        Assertions.assertTrue(
                html.contains("href=\"/tools/online-clients/openapi-explorer/about\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/scalar/about\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/stoplight/about\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/zudoku/about\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/redoc/about\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/summary-reviews\""));
        Assertions.assertTrue(html.contains("href=\"/tools/proxies\""));

        html = renderContentPage("/tools/online-clients/basic-client");

        Assertions.assertTrue(
                html.contains(
                        "<title>Free Online REST API Client for Testing HTTP Requests</title>"));
        Assertions.assertTrue(html.contains("class=\"sim-live-request\""));
        Assertions.assertTrue(html.contains("data-custom-method=\"true\""));
        Assertions.assertTrue(html.contains("data-body-methods=\"all\""));
        Assertions.assertFalse(html.contains("data-allowed-path-prefixes"));
        Assertions.assertTrue(html.contains("For general browser client limits, CORS notes"));
        Assertions.assertFalse(html.contains("Use Browser Dev Tools To Help Test REST APIs"));
        Assertions.assertFalse(html.contains("HAR file"));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/basic-client\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/openapi-converter\""));
        Assertions.assertTrue(html.contains("href=\"/reference/http-basics\""));
        Assertions.assertTrue(html.contains("href=\"/reference/http-verbs\""));
        Assertions.assertTrue(html.contains("href=\"/reference/rest-api-basics\""));

        html = renderContentPage("/tools/online-clients/swagger");

        Assertions.assertTrue(
                html.contains(
                        "<title>Online Swagger UI: Open OpenAPI Files from URL or Disk</title>"));
        assertWideToolEmbedPage(html);
        Assertions.assertTrue(html.contains("data-online-swagger-client"));
        Assertions.assertTrue(html.contains("data-openapi-url"));
        Assertions.assertTrue(html.contains("data-openapi-file"));
        Assertions.assertTrue(html.contains("data-openapi-profile"));
        Assertions.assertTrue(html.contains("data-openapi-custom-options"));
        Assertions.assertTrue(html.contains("data-openapi-copy-converted"));
        Assertions.assertTrue(html.contains("data-openapi-download-converted"));
        Assertions.assertFalse(html.contains("data-openapi-example"));
        Assertions.assertTrue(html.contains("<ul class=\"openapi-option-grid\">"));
        Assertions.assertTrue(html.contains("<ul class=\"openapi-verb-grid\""));
        Assertions.assertTrue(html.contains("data-openapi-copy-converted disabled"));
        Assertions.assertTrue(html.contains("data-openapi-download-converted disabled"));
        Assertions.assertTrue(html.contains("https://unpkg.com/swagger-ui-dist@5.32.12"));
        Assertions.assertTrue(html.contains("href=\"/css/online-swagger-theme.css\""));
        Assertions.assertTrue(html.contains("src=\"/js/vendor/js-yaml.min.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/openapi-text-loader.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/openapi-tester-converter.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/openapi-tool-controls.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/online-swagger-client.js\""));
        assertContainsInOrder(
                html,
                "src=\"/js/vendor/js-yaml.min.js\"",
                "src=\"/js/openapi-text-loader.js\"",
                "src=\"/js/openapi-tester-converter.js\"",
                "src=\"/js/openapi-tool-controls.js\"",
                "src=\"/js/online-swagger-client.js\"");
        Assertions.assertTrue(html.contains("Swagger UI In This Page"));
        Assertions.assertTrue(html.contains("Tester OpenAPI Profile"));
        Assertions.assertTrue(html.contains("Swagger UI Testing Limits"));
        Assertions.assertFalse(html.contains("How To Use Swagger UI For REST API Testing"));
        Assertions.assertFalse(html.contains("CORS Limits For Browser Swagger UI"));
        Assertions.assertFalse(html.contains("When To Use A REST Client Instead Of Swagger UI"));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger/about\""));
        Assertions.assertTrue(html.contains("href=\"/reference/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/openapi-converter\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/basic-client\""));
        Assertions.assertTrue(html.contains("href=\"/apichallenges/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simulation-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simpleapi-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/shoppingcart-openapi\""));
        assertOpenApiUiReferenceLaunchLinks(html, "Swagger", "swagger");

        assertOnlineOpenApiUiClientPage(
                "/tools/online-clients/openapi-explorer",
                "<title>Online OpenAPI Explorer UI for Loading API Specs</title>",
                "openapi-explorer",
                "OpenAPI Explorer In This Page",
                "https://unpkg.com/openapi-explorer@2.4.820/dist/browser/openapi-explorer.min.js");

        assertOnlineOpenApiUiClientPage(
                "/tools/online-clients/scalar",
                "<title>Online Scalar OpenAPI UI for API Reference Testing</title>",
                "scalar",
                "Scalar In This Page",
                "https://cdn.jsdelivr.net/npm/@scalar/api-reference@1.64.1");

        assertOnlineOpenApiUiClientPage(
                "/tools/online-clients/stoplight",
                "<title>Online Stoplight Elements UI for OpenAPI Documentation</title>",
                "stoplight",
                "Stoplight Elements In This Page",
                "https://unpkg.com/@stoplight/elements@9.0.24/web-components.min.js");
        html = renderContentPage("/tools/online-clients/stoplight");
        assertContainsInOrder(
                html,
                "href=\"https://unpkg.com/@stoplight/elements@9.0.24/styles.min.css\"",
                "href=\"/css/online-swagger-theme.css\"");

        assertOnlineOpenApiUiClientPage(
                "/tools/online-clients/zudoku",
                "<title>Online Zudoku OpenAPI UI for API Reference Demos</title>",
                "zudoku",
                "Zudoku In This Page",
                "src=\"/js/online-openapi-ui-client.js\"");

        assertOnlineOpenApiUiClientPage(
                "/tools/online-clients/redoc",
                "<title>Online Redoc OpenAPI Viewer for API Reference Docs</title>",
                "redoc",
                "Redoc In This Page",
                "https://cdn.jsdelivr.net/npm/redoc@2.5.3/bundles/redoc.standalone.js");

        html = renderContentPage("/tools/online-clients/openapi-converter");

        Assertions.assertTrue(
                html.contains("<title>Convert OpenAPI To A More Permissive Tester Spec</title>"));
        Assertions.assertTrue(html.contains("data-openapi-converter"));
        Assertions.assertTrue(html.contains("data-openapi-url"));
        Assertions.assertTrue(html.contains("data-openapi-file"));
        Assertions.assertTrue(html.contains("data-openapi-profile"));
        Assertions.assertTrue(html.contains("data-openapi-custom-options"));
        Assertions.assertTrue(html.contains("data-openapi-copy-converted"));
        Assertions.assertTrue(html.contains("data-openapi-download-converted"));
        Assertions.assertTrue(html.contains("data-openapi-open-swagger"));
        Assertions.assertTrue(html.contains("data-openapi-open-client=\"swagger\""));
        Assertions.assertTrue(html.contains("data-openapi-open-client=\"openapi-explorer\""));
        Assertions.assertTrue(html.contains("data-openapi-open-client=\"scalar\""));
        Assertions.assertTrue(html.contains("data-openapi-open-client=\"stoplight\""));
        Assertions.assertTrue(html.contains("data-openapi-open-client=\"zudoku\""));
        Assertions.assertTrue(html.contains("data-openapi-open-client=\"redoc\""));
        Assertions.assertTrue(
                html.contains("data-openapi-client-path=\"/tools/online-clients/swagger\""));
        Assertions.assertTrue(
                html.contains(
                        "data-openapi-client-path=\"/tools/online-clients/openapi-explorer\""));
        Assertions.assertTrue(
                html.contains("data-openapi-client-path=\"/tools/online-clients/scalar\""));
        Assertions.assertTrue(
                html.contains("data-openapi-client-path=\"/tools/online-clients/stoplight\""));
        Assertions.assertTrue(
                html.contains("data-openapi-client-path=\"/tools/online-clients/zudoku\""));
        Assertions.assertTrue(
                html.contains("data-openapi-client-path=\"/tools/online-clients/redoc\""));
        Assertions.assertTrue(html.contains("data-openapi-output"));
        Assertions.assertFalse(html.contains("data-openapi-example"));
        Assertions.assertTrue(html.contains("<ul class=\"openapi-option-grid\">"));
        Assertions.assertTrue(html.contains("<ul class=\"openapi-verb-grid\""));
        Assertions.assertTrue(html.contains("data-openapi-copy-converted disabled"));
        Assertions.assertTrue(html.contains("data-openapi-download-converted disabled"));
        Assertions.assertTrue(html.contains("data-openapi-open-swagger disabled"));
        Assertions.assertTrue(html.contains("Open in Embedded Client"));
        assertContainsInOrder(
                html,
                "Converted OpenAPI JSON",
                "Open in Embedded Client",
                "Open in Swagger UI",
                "Open in Redoc");
        Assertions.assertTrue(html.contains("Convert OpenAPI To A More Permissive Tester Spec"));
        Assertions.assertTrue(
                html.contains("Create Practical Or Aggressive OpenAPI Testing Files"));
        Assertions.assertTrue(
                html.contains("Download A Less Restrictive OpenAPI File For REST Client Testing"));
        Assertions.assertTrue(
                html.contains("Use Converted OpenAPI Files In Embedded Clients And REST Clients"));
        Assertions.assertTrue(html.contains("CORS Limits For Browser OpenAPI Conversion"));
        Assertions.assertTrue(html.contains("src=\"/js/openapi-tester-converter.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/openapi-text-loader.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/openapi-tool-controls.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/openapi-converter-page.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/vendor/js-yaml.min.js\""));
        assertContainsInOrder(
                html,
                "src=\"/js/vendor/js-yaml.min.js\"",
                "src=\"/js/openapi-text-loader.js\"",
                "src=\"/js/openapi-tester-converter.js\"",
                "src=\"/js/openapi-tool-controls.js\"",
                "src=\"/js/openapi-converter-page.js\"");
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/basic-client\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/summary-reviews\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/bruno\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/postman\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/insomnia\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/curl\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/rest-api-testing\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/api-simulator-walkthrough\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simulation\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger/about\""));
        Assertions.assertTrue(html.contains("href=\"/reference/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/apichallenges/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simulation-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simpleapi-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/shoppingcart-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/tools/proxies\""));
    }

    private void assertOnlineOpenApiUiClientPage(
            final String path,
            final String expectedTitle,
            final String expectedClient,
            final String expectedHeading,
            final String expectedDependency) {

        final String html = renderContentPage(path);

        assertWideToolEmbedPage(html);
        Assertions.assertTrue(html.contains(expectedTitle));
        Assertions.assertTrue(html.contains("data-online-openapi-ui-client"));
        Assertions.assertTrue(html.contains("data-openapi-ui=\"" + expectedClient + "\""));
        Assertions.assertTrue(html.contains("data-openapi-url"));
        Assertions.assertTrue(html.contains("data-openapi-file"));
        Assertions.assertTrue(html.contains("data-openapi-render-target"));
        Assertions.assertTrue(html.contains("Open local JSON or YAML file"));
        Assertions.assertTrue(html.contains(expectedHeading));
        Assertions.assertTrue(html.contains(expectedDependency));
        Assertions.assertTrue(html.contains("href=\"/css/online-swagger-theme.css\""));
        Assertions.assertTrue(html.contains("src=\"/js/vendor/js-yaml.min.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/openapi-text-loader.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/openapi-tool-controls.js\""));
        Assertions.assertTrue(html.contains("src=\"/js/online-openapi-ui-client.js\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/redoc\""));
        Assertions.assertTrue(
                html.contains("href=\"/tools/online-clients/" + expectedClient + "/about\""));
        assertOpenApiUiReferenceLaunchLinks(
                html, openApiUiToolDisplayName(expectedClient), expectedClient);
    }

    private void assertWideToolEmbedPage(final String html) {
        Assertions.assertTrue(html.contains("<body class='wide-tool-page'>"));
        Assertions.assertTrue(html.contains("<div class='content'>"));
        Assertions.assertTrue(html.contains("<section class='doc-columns'>"));
        Assertions.assertTrue(html.contains("<div class='right-column'>"));
        Assertions.assertTrue(html.contains("<main id='maincontentstartshere'>"));
        Assertions.assertTrue(html.contains("<div class=\"main-text-content\">"));
        Assertions.assertTrue(html.contains("wide-tool-copy-block wide-tool-copy-block-top"));
        Assertions.assertTrue(html.contains("wide-tool-client-breakout"));
        Assertions.assertTrue(html.contains("wide-tool-copy-block wide-tool-copy-block-bottom"));
        Assertions.assertTrue(html.contains("online-openapi-ui-wide-embed"));
        Assertions.assertTrue(html.contains("<aside class='left-column'"));
        Assertions.assertTrue(
                html.contains("<nav class='side-toc' aria-label='Learning and reference links'>"));
        Assertions.assertTrue(html.contains("wide-tool-side-toc-grid"));
        Assertions.assertTrue(html.contains("wide-tool-side-toc-column-learning"));
        Assertions.assertTrue(html.contains("wide-tool-side-toc-column-reference"));
        Assertions.assertTrue(html.contains("wide-tool-side-toc-column-support"));
        Assertions.assertTrue(html.contains("wide-tool-side-toc-support"));
        Assertions.assertTrue(html.contains("<div id='toc'>"));
        Assertions.assertFalse(html.contains("content-wide-tool"));
        Assertions.assertFalse(html.contains("doc-columns-wide-tool"));
        Assertions.assertFalse(html.contains("wide-tool-main"));
        Assertions.assertFalse(html.contains("wide-tool-main-text"));
    }

    private void assertOpenApiUiReferenceLaunchLinks(
            final String html, final String toolName, final String clientPath) {

        Assertions.assertTrue(html.contains("Try " + toolName + " with our APIs:"));
        Assertions.assertTrue(html.contains("openapi-ui-launch-panel"));
        Assertions.assertTrue(html.contains("openapi-ui-launch-link"));
        assertOpenApiUiReferenceLaunchLink(
                html, clientPath, "%2Fsimpleapi%2Fdocs%2Fopenapi.json", "Simple API");
        assertOpenApiUiReferenceLaunchLink(
                html, clientPath, "%2Fsim%2Fdocs%2Fopenapi.json", "API Simulator");
        assertOpenApiUiReferenceLaunchLink(
                html, clientPath, "%2Fapi%2Fdocs%2Fopenapi.json", "API Challenges");
        assertOpenApiUiReferenceLaunchLink(
                html, clientPath, "%2Fshop%2Fdocs%2Fopenapi.json", "Buggy API");
    }

    private void assertOpenApiUiToolAboutBreadcrumb(
            final String html, final String toolName, final String clientPath) {

        Assertions.assertTrue(
                html.contains("<nav class=\"breadcrumb\" aria-label=\"Breadcrumb\">"));
        Assertions.assertFalse(html.contains("</a> &gt;"));
        Assertions.assertTrue(html.contains("<li><a href=\"/tools\">Tools</a></li>"));
        Assertions.assertTrue(
                html.contains("<li><a href=\"/tools/online-clients\">Online Clients</a></li>"));
        Assertions.assertTrue(
                html.contains(
                        "<li><a href=\"/tools/online-clients/"
                                + clientPath
                                + "\">"
                                + toolName
                                + "</a></li>"));
        Assertions.assertTrue(html.contains("<li aria-current=\"page\">About</li>"));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":2,\"name\":\"Tools\",\"item\":\"https://apichallenges.com/tools\""));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":3,\"name\":\"Online Clients\",\"item\":\"https://apichallenges.com/tools/online-clients\""));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":4,\"name\":\""
                                + toolName
                                + "\",\"item\":\"https://apichallenges.com/tools/online-clients/"
                                + clientPath
                                + "\""));
        Assertions.assertTrue(
                html.contains(
                        "\"position\":5,\"name\":\"About\",\"item\":\"https://apichallenges.com/tools/online-clients/"
                                + clientPath
                                + "/about\""));
    }

    private void assertOpenApiUiReferenceLaunchLink(
            final String html,
            final String clientPath,
            final String encodedOpenApiPath,
            final String linkText) {

        Assertions.assertTrue(
                html.contains(
                        "href=\"/tools/online-clients/"
                                + clientPath
                                + "?url="
                                + encodedOpenApiPath
                                + "\">"
                                + linkText
                                + "</a>"));
    }

    private String openApiUiToolDisplayName(final String clientPath) {
        return switch (clientPath) {
            case "swagger" -> "Swagger";
            case "openapi-explorer" -> "OpenAPI Explorer UI";
            case "scalar" -> "Scalar";
            case "stoplight" -> "Stoplight Elements";
            case "zudoku" -> "Zudoku";
            case "redoc" -> "Redoc";
            default -> clientPath;
        };
    }

    private String renderRestApiTestingTutorial() {
        return renderContentPage("/tutorials/rest-api-testing");
    }

    private String renderContentPage(final String contentPath) {
        ResourceContentScanner contentScanner = new ResourceContentScanner();
        List<String> pathsToFileContent =
                contentScanner.scanForFullPathsOfExtensionsIn("content/", "md");
        MarkdownContentManager contentManager =
                new MarkdownContentManager(pathsToFileContent, new DefaultGUIHTML(), null);

        return contentManager.getResourceMarkdownFileAsHtml(
                "content",
                contentPath,
                Map.of("ORIGIN_URL", "http://localhost:4567", "HOST_URL", "localhost:4567"));
    }

    private int countOccurrences(final String value, final String substring) {
        return value.split(java.util.regex.Pattern.quote(substring), -1).length - 1;
    }

    private void assertContentPageContains(final String contentPath, final String... expectedText) {
        final String html = renderContentPage(contentPath);
        for (String expected : expectedText) {
            Assertions.assertTrue(
                    html.contains(expected),
                    "Expected " + contentPath + " to contain: " + expected);
        }
    }

    private void assertContainsInOrder(final String value, final String... substrings) {
        int previousIndex = -1;
        for (String substring : substrings) {
            final int index = value.indexOf(substring, previousIndex + 1);
            Assertions.assertTrue(index >= 0, "Missing expected text: " + substring);
            Assertions.assertTrue(
                    index > previousIndex,
                    "Expected text to appear later than previous entry: " + substring);
            previousIndex = index;
        }
    }
}
