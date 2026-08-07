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
                html.contains("Updated <time datetime='2026-08-04'>2026-08-04</time>"));
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
        Assertions.assertTrue(html.contains("<span class=\"side-toc-section-title\">Tools</span>"));
        Assertions.assertTrue(
                html.contains(
                        "<span class=\"side-toc-section-title\">Interactive Tutorials</span>"));
        Assertions.assertTrue(
                html.contains(
                        "Tool reviews for REST HTTP clients, proxies, and online API clients to help with API testing."));
        Assertions.assertTrue(
                html.contains(
                        "Hands-on tutorial pages for learning REST concepts and API testing workflows."));
        Assertions.assertTrue(
                html.contains(
                        "Reference material in a supporting learning order for web, HTTP, REST, testing, OpenAPI, and Swagger."));
        Assertions.assertTrue(
                html.contains("<span class=\"side-toc-section-title\">Reference</span>"));
        Assertions.assertFalse(
                html.contains("<span class=\"side-toc-section-title\">Reference Tutorials</span>"));
        assertContainsInOrder(
                html,
                "<ul class=\"side-toc-root\">",
                "<li><a href=\"/learning\">Learning Zone</a></li>",
                "<li class=\"side-toc-syllabus\" aria-label=\"REST API Tutorial path\">",
                "<span class=\"side-toc-syllabus-title\">REST API Tutorial Path</span>",
                "<li><a href=\"/tutorials/rest-api-tutorial\">Interactive REST API Tutorial</a></li>",
                "<li><a href=\"/reference/http-basics\">HTTP basics</a></li>",
                "<li><a href=\"/reference/rest-api-basics\">REST basics</a></li>",
                "<li><a href=\"/reference/http-verbs\">HTTP methods</a></li>",
                "<li><a href=\"/reference/http-basics#toc7\">Status codes</a></li>",
                "<li><a href=\"/reference/openapi\">OpenAPI</a></li>",
                "<li><a href=\"/tutorials/rest-api-testing\">Interactive How to Test REST APIs Tutorial</a></li>",
                "<li><a href=\"/tutorials/api-simulator-walkthrough\">Interactive API Simulation</a></li>",
                "<li><a href=\"/apichallenges\">API Challenges</a></li>",
                "<details class=\"side-toc-section\" data-side-toc-section=\"reference-tutorials\">",
                "<span class=\"side-toc-section-title\">Reference</span>",
                "<li><a href=\"/reference/web-basics\">Web Applications</a></li>",
                "<li><a href=\"/reference/http-basics\">HTTP Basics</a></li>",
                "<li><a href=\"/reference/http-verbs\">HTTP Verbs</a></li>",
                "<li><a href=\"/reference/rest-api-basics\">REST API Basics</a></li>",
                "<li><a href=\"/reference/testing-apis\">API Testing Concepts</a></li>",
                "<li><a href=\"/reference/openapi\">OpenAPI</a></li>",
                "<li><a href=\"/reference/swagger\">Swagger</a></li>",
                "<details class=\"side-toc-section\" data-side-toc-section=\"interactive-tutorials\">",
                "<span class=\"side-toc-section-title\">Interactive Tutorials</span>",
                "<li><a href=\"/tutorials/rest-api-tutorial\">Rest API Tutorial</a></li>",
                "<li><a href=\"/tutorials/rest-api-testing\">Testing REST APIs Tutorial</a></li>",
                "<li><a href=\"/tutorials/api-simulator-walkthrough\">API Simulator Walkthrough</a></li>",
                "<details class=\"side-toc-section\" data-side-toc-section=\"practice-modes\">",
                "<li><a href=\"/apichallenges/solutions\">Challenge Solutions</a></li>",
                "<details class=\"side-toc-section\" data-side-toc-section=\"tools\">",
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
                "Online Clients",
                "<li><a href=\"/tools/online-clients/basic-client\">Basic Client</a></li>",
                "<li><a href=\"/tools/online-clients/swagger\">Swagger</a></li>",
                "<li><a href=\"/tools/online-clients/openapi-converter\">OpenAPI Converter</a></li>");
        Assertions.assertEquals(1, countOccurrences(html, "href=\"/blog\""));
        Assertions.assertFalse(html.contains("href=\"/changes\""));
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
        Assertions.assertFalse(
                blogHtml.contains("href=\"/blog/api-challenges-practice-api-overview\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/api-simulator-walkthrough-launch\""));
        Assertions.assertTrue(
                blogHtml.contains(
                        "href=\"/blog/changelog-2026-08-06-interactive-rest-api-tutorial\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/categories/api-testing\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/categories/rest-api-tutorial\""));
        Assertions.assertTrue(blogHtml.contains("href=\"/blog/categories/change-log\""));
        assertContainsInOrder(
                blogHtml,
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
                videoPostHtml.contains(
                        "<a href=\"/blog\">Blog</a> &gt; <a href=\"/blog/categories/api-testing\">API Testing</a> &gt; api-challenges-practice-api-overview"));
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

        String launchPostHtml = renderContentPage("/blog/api-simulator-walkthrough-launch");

        Assertions.assertTrue(
                launchPostHtml.contains("<h1>Interactive API Simulator Walkthrough Launch</h1>"));
        Assertions.assertTrue(
                launchPostHtml.contains(
                        "Published <time datetime='2026-08-07T09:00:00Z'>2026-08-07</time>"));
        Assertions.assertTrue(launchPostHtml.contains("\"@type\":\"BlogPosting\""));
        Assertions.assertFalse(launchPostHtml.contains("<aside class='left-column'"));
        Assertions.assertFalse(launchPostHtml.contains("<nav class='side-toc'"));

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
                changelogPostHtml.contains(
                        "<a href=\"/blog\">Blog</a> &gt; <a href=\"/blog/categories/change-log\">Change Log</a> &gt; changelog-2025-04-19-bruno-client-demo"));
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
        Assertions.assertTrue(html.contains("href=\"/reference/swagger\""));
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
        Assertions.assertEquals(5, countOccurrences(html, "data-allowed-path-prefixes=\"/todos\""));
        Assertions.assertEquals(1, countOccurrences(html, "data-allowed-path-prefixes=\"/docs\""));
        Assertions.assertEquals(
                1, countOccurrences(html, "data-allowed-path-prefixes=\"/heartbeat\""));
        Assertions.assertEquals(
                3, countOccurrences(html, "data-allowed-path-prefixes=\"/secret\""));
        Assertions.assertEquals(14, countOccurrences(html, "data-editable=\"false\""));
        Assertions.assertEquals(1, countOccurrences(html, "data-editable=\"true\""));
        Assertions.assertEquals(
                1, countOccurrences(html, "data-editable=\"true\" data-edit-mode=\"fixed\""));
        Assertions.assertEquals(
                15,
                countOccurrences(html, "<details class=\"sim-live-request-details\"><summary>"));
        Assertions.assertEquals(
                0, countOccurrences(html, "<details class=\"sim-live-request-details\" open"));
        Assertions.assertTrue(html.contains("data-path=\"/todos\""));
        Assertions.assertTrue(html.contains("data-path=\"/todos/1\""));
        Assertions.assertTrue(html.contains("data-method=\"HEAD\" data-path=\"/todos/1\""));
        Assertions.assertTrue(html.contains("data-path=\"/heartbeat\""));
        Assertions.assertTrue(html.contains("data-expected-status=\"405\""));
        Assertions.assertTrue(html.contains("data-path=\"/secret/token\""));
        Assertions.assertTrue(html.contains("data-path=\"/secret/note\""));
        Assertions.assertTrue(html.contains("{{authToken}}"));
        Assertions.assertTrue(html.contains("data-path=\"/docs/openapi.json\""));
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
                "href=\"/reference/swagger\"");
        Assertions.assertFalse(learningHtml.contains("<h2>Reference Tutorials</h2>"));
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
    void onlineClientContentPagesRenderToolsAndSidebarLinks() {

        String html = renderContentPage("/tools/online-clients/basic-client");

        Assertions.assertTrue(
                html.contains(
                        "<title>Free Online REST API Client for Testing HTTP Requests</title>"));
        Assertions.assertTrue(html.contains("class=\"sim-live-request\""));
        Assertions.assertTrue(html.contains("data-custom-method=\"true\""));
        Assertions.assertTrue(html.contains("data-body-methods=\"all\""));
        Assertions.assertFalse(html.contains("data-allowed-path-prefixes"));
        Assertions.assertTrue(html.contains("limited by CORS"));
        Assertions.assertTrue(html.contains("Use Browser Dev Tools To Help Test REST APIs"));
        Assertions.assertTrue(html.contains("HAR file"));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/swagger\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/basic-client\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/openapi-converter\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/rest-api-testing\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/api-simulator-walkthrough\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simulation\""));
        Assertions.assertTrue(html.contains("href=\"/reference/http-basics\""));
        Assertions.assertTrue(html.contains("href=\"/reference/http-verbs\""));
        Assertions.assertTrue(html.contains("href=\"/reference/rest-api-basics\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/summary-reviews\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/bruno\""));
        Assertions.assertTrue(html.contains("href=\"/tools/proxies\""));

        html = renderContentPage("/tools/online-clients/swagger");

        Assertions.assertTrue(
                html.contains(
                        "<title>Online Swagger UI: Open OpenAPI Files from URL or Disk</title>"));
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
        Assertions.assertTrue(html.contains("https://unpkg.com/swagger-ui-dist"));
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
        Assertions.assertTrue(html.contains("Open OpenAPI And Swagger Files From URL Or Disk"));
        Assertions.assertTrue(html.contains("Render A Tester OpenAPI Spec In Swagger UI"));
        Assertions.assertTrue(html.contains("How To Use Swagger UI For REST API Testing"));
        Assertions.assertTrue(html.contains("CORS Limits For Browser Swagger UI"));
        Assertions.assertTrue(html.contains("When To Use A REST Client Instead Of Swagger UI"));
        Assertions.assertTrue(html.contains("less-validating, permissive file"));
        Assertions.assertTrue(html.contains("limited by CORS"));
        Assertions.assertTrue(html.contains("href=\"/reference/swagger\""));
        Assertions.assertTrue(html.contains("href=\"/reference/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/openapi-converter\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/rest-api-testing\""));
        Assertions.assertTrue(html.contains("href=\"/tutorials/api-simulator-walkthrough\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simulation\""));
        Assertions.assertTrue(html.contains("href=\"/tools/online-clients/basic-client\""));
        Assertions.assertTrue(html.contains("href=\"/apichallenges/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simpleapi-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/shoppingcart-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/summary-reviews\""));
        Assertions.assertTrue(html.contains("href=\"/tools/clients/insomnia\""));
        Assertions.assertTrue(html.contains("href=\"/tools/proxies\""));

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
        Assertions.assertTrue(html.contains("data-openapi-output"));
        Assertions.assertFalse(html.contains("data-openapi-example"));
        Assertions.assertTrue(html.contains("<ul class=\"openapi-option-grid\">"));
        Assertions.assertTrue(html.contains("<ul class=\"openapi-verb-grid\""));
        Assertions.assertTrue(html.contains("data-openapi-copy-converted disabled"));
        Assertions.assertTrue(html.contains("data-openapi-download-converted disabled"));
        Assertions.assertTrue(html.contains("data-openapi-open-swagger disabled"));
        Assertions.assertTrue(html.contains("Convert OpenAPI To A More Permissive Tester Spec"));
        Assertions.assertTrue(
                html.contains("Create Practical Or Aggressive OpenAPI Testing Files"));
        Assertions.assertTrue(
                html.contains("Download A Less Restrictive OpenAPI File For REST Client Testing"));
        Assertions.assertTrue(
                html.contains("Use Converted OpenAPI Files In Swagger UI And REST Clients"));
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
        Assertions.assertTrue(html.contains("href=\"/reference/swagger\""));
        Assertions.assertTrue(html.contains("href=\"/reference/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/apichallenges/openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/simpleapi-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/practice-modes/shoppingcart-openapi\""));
        Assertions.assertTrue(html.contains("href=\"/tools/proxies\""));
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
