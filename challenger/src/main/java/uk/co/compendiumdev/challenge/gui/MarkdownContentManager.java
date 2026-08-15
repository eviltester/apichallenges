package uk.co.compendiumdev.challenge.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.AssetVersion;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.challenges.ChallengeSolutionLink;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

// TODO: consider adding caching for generated markdown pages

public class MarkdownContentManager {

    private static final String DEFAULT_CANONICAL_HOST = "https://apichallenges.com";
    private static final String DEFAULT_SITE_NAME = "API Challenges";
    private static final String DEFAULT_OG_IMAGE_PATH =
            "/images/hero/apichallenges-whole-site-gauntlet-1600x720.jpg";
    private static final String DEFAULT_SCHEMA_LOGO_PATH =
            "/images/social/apichallenges-og-1200x630.png";
    private static final String DEFAULT_OG_TYPE_CONTENT = "article";
    private static final String DEFAULT_OG_TYPE_WEBSITE = "website";
    private static final Pattern WIDE_TOOL_CLIENT_SECTION_PATTERN =
            Pattern.compile(
                    "(?s)(<section\\s+class=\"(?:online-openapi-ui-client|online-swagger-client)\"\\s+[^>]*>.*?</section>)");
    private static final Pattern HTML_UL_TAG_PATTERN =
            Pattern.compile("<(/?)ul\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_LI_TAG_PATTERN =
            Pattern.compile("<(/?)li\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final String DEFAULT_TWITTER_CARD = "summary_large_image";
    private static final String DEFAULT_META_ROBOTS = "index,follow";
    private static final String DEFAULT_SCHEMA_TYPE_CONTENT = "Article";
    private static final String DEFAULT_SCHEMA_TYPE_INDEX = "WebPage";
    private static final String DEFAULT_SCHEMA_AUTHOR_NAME = "alan-richardson";
    private static final String DEFAULT_SCHEMA_PUBLISHER_NAME = "eviltester.com";
    private static final String DEFAULT_SCHEMA_AUTHOR_RESOURCE = "seo/schema-author.properties";
    private static final String DEFAULT_SCHEMA_PUBLISHER_RESOURCE =
            "seo/schema-publisher.properties";
    private static final String DEFAULT_AUTHOR_BIO_PATH = "/author/alan-richardson";
    private static final String DEFAULT_AUTHOR_BIO_SNIPPET_RESOURCE =
            "partials/author-bio-snippet.html";
    private static final String DEFAULT_NEXT_CHALLENGE_CTA_RESOURCE =
            "partials/next-challenge-cta.html";
    private static final String API_CHALLENGE_ALLOWED_PATH_PREFIXES =
            "/api||/todos||/todo||/challenges||/challenger||/secret||/heartbeat";

    private final DefaultGUIHTML guiManagement;
    private final Map<String, String> solutionChallengeIds;
    private final BlogContentManager blogContentManager;
    Logger logger = LoggerFactory.getLogger(MarkdownContentManager.class);
    private final Set<String> markdownContentPaths;
    private final Properties schemaAuthorDefaults;
    private final Properties schemaPublisherDefaults;
    private String sideMenuText;

    public MarkdownContentManager(
            final List<String> pathsToFileContent,
            final DefaultGUIHTML defaultGui,
            final ChallengeDefinitions challengeDefinitions) {
        markdownContentPaths = new HashSet<>();
        markdownContentPaths.addAll(pathsToFileContent);
        this.guiManagement = defaultGui;
        this.solutionChallengeIds = buildSolutionChallengeIds(challengeDefinitions);
        this.blogContentManager =
                new BlogContentManager(pathsToFileContent, DEFAULT_CANONICAL_HOST);
        this.schemaAuthorDefaults = loadPropertiesFromResource(DEFAULT_SCHEMA_AUTHOR_RESOURCE);
        this.schemaPublisherDefaults =
                loadPropertiesFromResource(DEFAULT_SCHEMA_PUBLISHER_RESOURCE);
        sideMenuText = "";
    }

    private Map<String, String> buildSolutionChallengeIds(
            final ChallengeDefinitions challengeDefinitions) {
        final Map<String, String> challengeIds = new HashMap<>();
        if (challengeDefinitions == null) {
            return challengeIds;
        }

        for (ChallengeDefinitionData challenge : challengeDefinitions.getChallenges()) {
            for (ChallengeSolutionLink solution : challenge.solutions) {
                if ("HREF".equals(solution.linkType)
                        && solution.linkData.startsWith("/apichallenges/solutions/")) {
                    challengeIds.put(solution.linkData, challenge.id);
                }
            }
        }
        return challengeIds;
    }

    // TODO: this is currently a hacked in solution for experimenting, pull it out into classes and
    // create state enum
    public String getResourceMarkdownFileAsHtml(
            String contentFolder, String contentPath, Map<String, String> params) {

        if (contentPath.endsWith(".html")) {
            contentPath = contentPath.replace(".html", "");
        }

        if (contentPath.endsWith(".md")) {
            contentPath = contentPath.replace(".md", "");
        }

        String contentToFind = contentFolder + contentPath + ".md";

        // if content does not exist in the list then exit
        if (!markdownContentPaths.contains(contentToFind)) {
            throw new IllegalArgumentException("Resource not found %s.md".formatted(contentPath));
        }

        return getHtmlVersionOfMarkdownContent(contentFolder, contentPath, params);
    }

    public String getHtmlVersionOfMarkdownContent(
            String contentFolder, String contentPath, Map<String, String> params) {

        InputStream inputStream = getResourceAsStream(contentFolder + contentPath + ".md");

        String[] breadcrumbs =
                Arrays.stream(contentPath.split("/"))
                        .filter(item -> item != null && !item.isEmpty())
                        .toArray(String[]::new);

        StringBuilder bcHtmlHeader;

        String headerInject = "";
        String youtubeHeaderInject = "";

        List<Extension> extensions = List.of(TablesExtension.create());
        // parse this html and output
        Parser parser = Parser.builder().extensions(extensions).build();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        List<String> mdheaders = new ArrayList<>();

        StringBuilder mdcontent = new StringBuilder();

        // mdcontent.append(bcHeader);

        String state = "EXPECTING_HEADER";
        boolean addedToc = false;
        boolean tocPendingAfterFirstHeading = false;
        boolean readingLeadFigureBeforeToc = false;
        String firstYouTubeVideoId = "";
        boolean liveRequestWidgetUsed = false;

        try {
            while ((line = reader.readLine()) != null) {

                if (line.equals("---") && state.equals("EXPECTING_HEADER")) {
                    state = "READING_HEADER"; // start of headers
                    continue;
                }

                if (line.equals("---") && state.equals("READING_HEADER")) {
                    state = "READING_CONTENT"; // end of headers
                    continue;
                }

                if (line.contains(": ") && state.equals("READING_HEADER")) {
                    mdheaders.add(line);
                    continue;
                }

                if (state.equals("READING_HEADER") && line.trim().isEmpty()) {
                    // ignore empty lines in the header
                    continue;
                }

                if (state.equals("READING_HEADER") && !line.trim().isEmpty()) {
                    // probably shouldn't be reading headers we found a non-empty line
                    state = "READING_CONTENT";
                }

                if (line.contains("{{<sim-live-request") || line.contains("{{<api-live-request")) {
                    liveRequestWidgetUsed = true;
                }

                // process any macros
                line = processMacrosInContentLine(line, params, contentPath);

                if (firstYouTubeVideoId.isEmpty()) {
                    firstYouTubeVideoId = extractYouTubeVideoId(line);
                }

                if (!mdheaders.contains("template: index")
                        && tocPendingAfterFirstHeading
                        && !addedToc
                        && !readingLeadFigureBeforeToc) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("<figure")) {
                        mdcontent.append("\n<div id='toc'></div>\n\n");
                        addedToc = true;
                        tocPendingAfterFirstHeading = false;
                    }
                }

                if (line.contains("youtube.com/watch")) {
                    if (youtubeHeaderInject.isEmpty()) {
                        // only import the facade if we are rendering youtube
                        youtubeHeaderInject =
                                "<script type=\"module\" src=\"https://cdn.jsdelivr.net/npm/@justinribeiro/lite-youtube@1.5.0/lite-youtube.js\"></script>";
                    }
                }

                mdcontent.append(line + "\n");

                // todo: better header parsing, and parse headers separate from the main body
                if (!mdheaders.contains("template: index")) {
                    // inject table of contents
                    if (line.startsWith("# ") && !addedToc) {
                        tocPendingAfterFirstHeading = true;
                    } else if (tocPendingAfterFirstHeading && !addedToc) {
                        String trimmedLine = line.trim();
                        if (trimmedLine.startsWith("<figure")) {
                            readingLeadFigureBeforeToc = true;
                        }
                        if (readingLeadFigureBeforeToc && trimmedLine.equals("</figure>")) {
                            readingLeadFigureBeforeToc = false;
                            tocPendingAfterFirstHeading = false;
                            addedToc = true;
                            mdcontent.append("\n<div id='toc'></div>\n\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Markdown parsing error", e);
        }

        final String solutionExperiment = solutionEndpointExperiment(contentFolder, contentPath);
        if (!solutionExperiment.isEmpty()) {
            liveRequestWidgetUsed = true;
            mdcontent.append("\n").append(solutionExperiment).append("\n");
        }

        if (mdheaders.contains("showads: true")) {
            // this did render google ads
            //            headerInject = headerInject +
            //                    "<script async
            // src=\"https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-7132305589272099\"" +
            //                    " crossorigin=\"anonymous\"></script>";
        }

        headerInject = headerInject + youtubeHeaderInject;
        if (liveRequestWidgetUsed) {
            headerInject =
                    headerInject
                            + "<script src='"
                            + AssetVersion.versionedPath("/js/api-live-request.js")
                            + "' defer></script>";
        }

        String markdownFromResource = solutionChallengeCompletedMessage(contentPath) + mdcontent;
        Node document = parser.parse(markdownFromResource);

        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

        String pageTitle = "Content Page";
        String seoTitle = "";
        String pageDescription = "";
        String seoDescription = "";
        String metaRobots = "";
        String ogImage = "";
        String ogImageAlt = "";
        String ogType = "";
        String twitterCard = "";
        String twitterSite = "";
        String schemaType = "";
        String schemaAuthor = "";
        String schemaPublisher = "";
        String schemaImage = "";
        String schemaBreadcrumbEnabledRaw = "";
        String schemaHowToEnabledRaw = "";
        String schemaHowToStepsRaw = "";
        String schemaVideoEnabledRaw = "";
        String schemaVideoId = "";
        String nextChallengePath = "";
        String conceptsLearnedRaw = "";
        String conceptSummary = "";
        String conceptReferenceLabel = "";
        String conceptReferenceUrl = "";
        String conceptReferenceLabel2 = "";
        String conceptReferenceUrl2 = "";
        String categoriesRaw = "";
        String hideSidebarRaw = "";
        String layout = "";
        String pageDatePublished = "";
        String pageLastModified = "";
        String canonicalUrl = DEFAULT_CANONICAL_HOST + contentPath;

        for (String aHeader : mdheaders) {
            if (aHeader.startsWith("title: ")) {
                pageTitle = aHeader.replace("title: ", "");
            }
            if (aHeader.startsWith("seo_title: ")) {
                seoTitle = aHeader.replace("seo_title: ", "");
            }
            if (aHeader.startsWith("description: ")) {
                pageDescription = aHeader.replace("description: ", "");
            }
            if (aHeader.startsWith("seo_description: ")) {
                seoDescription = aHeader.replace("seo_description: ", "");
            }
            if (aHeader.startsWith("meta_robots: ")) {
                metaRobots = aHeader.replace("meta_robots: ", "");
            }
            if (aHeader.startsWith("canonical: ")) {
                canonicalUrl = aHeader.replace("canonical: ", "");
            }
            if (aHeader.startsWith("og_image: ")) {
                ogImage = aHeader.replace("og_image: ", "");
            }
            if (aHeader.startsWith("og_image_alt: ")) {
                ogImageAlt = aHeader.replace("og_image_alt: ", "");
            }
            if (aHeader.startsWith("og_type: ")) {
                ogType = aHeader.replace("og_type: ", "");
            }
            if (aHeader.startsWith("twitter_card: ")) {
                twitterCard = aHeader.replace("twitter_card: ", "");
            }
            if (aHeader.startsWith("twitter_site: ")) {
                twitterSite = aHeader.replace("twitter_site: ", "");
            }
            if (aHeader.startsWith("schema_type: ")) {
                schemaType = aHeader.replace("schema_type: ", "");
            }
            if (aHeader.startsWith("schema_author: ")) {
                schemaAuthor = aHeader.replace("schema_author: ", "");
            }
            if (aHeader.startsWith("schema_publisher: ")) {
                schemaPublisher = aHeader.replace("schema_publisher: ", "");
            }
            if (aHeader.startsWith("schema_image: ")) {
                schemaImage = aHeader.replace("schema_image: ", "");
            }
            if (aHeader.startsWith("schema_breadcrumb_enabled: ")) {
                schemaBreadcrumbEnabledRaw = aHeader.replace("schema_breadcrumb_enabled: ", "");
            }
            if (aHeader.startsWith("schema_howto_enabled: ")) {
                schemaHowToEnabledRaw = aHeader.replace("schema_howto_enabled: ", "");
            }
            if (aHeader.startsWith("schema_howto_steps: ")) {
                schemaHowToStepsRaw = aHeader.replace("schema_howto_steps: ", "");
            }
            if (aHeader.startsWith("schema_video_enabled: ")) {
                schemaVideoEnabledRaw = aHeader.replace("schema_video_enabled: ", "");
            }
            if (aHeader.startsWith("schema_video_id: ")) {
                schemaVideoId = aHeader.replace("schema_video_id: ", "");
            }
            if (aHeader.startsWith("next_challenge: ")) {
                nextChallengePath = aHeader.replace("next_challenge: ", "");
            }
            if (aHeader.startsWith("concepts_learned: ")) {
                conceptsLearnedRaw = aHeader.replace("concepts_learned: ", "");
            }
            if (aHeader.startsWith("concept_summary: ")) {
                conceptSummary = aHeader.replace("concept_summary: ", "");
            }
            if (aHeader.startsWith("concept_reference_label: ")) {
                conceptReferenceLabel = aHeader.replace("concept_reference_label: ", "");
            }
            if (aHeader.startsWith("concept_reference_url: ")) {
                conceptReferenceUrl = aHeader.replace("concept_reference_url: ", "");
            }
            if (aHeader.startsWith("concept_reference_label_2: ")) {
                conceptReferenceLabel2 = aHeader.replace("concept_reference_label_2: ", "");
            }
            if (aHeader.startsWith("concept_reference_url_2: ")) {
                conceptReferenceUrl2 = aHeader.replace("concept_reference_url_2: ", "");
            }
            if (aHeader.startsWith("categories: ")) {
                categoriesRaw = aHeader.replace("categories: ", "");
            }
            if (aHeader.startsWith("hide_sidebar: ")) {
                hideSidebarRaw = aHeader.replace("hide_sidebar: ", "");
            }
            if (aHeader.startsWith("layout: ")) {
                layout = aHeader.replace("layout: ", "");
            }
            if (aHeader.startsWith("date:")) {
                pageDatePublished = aHeader.replaceFirst("^date:\\s*", "");
            }
            if (aHeader.startsWith("lastmod:")) {
                pageLastModified = aHeader.replaceFirst("^lastmod:\\s*", "");
            }
        }

        bcHtmlHeader =
                new StringBuilder(
                        buildBreadcrumbHtml(
                                contentFolder, contentPath, breadcrumbs, categoriesRaw));

        final String htmlTitle = seoTitle.isEmpty() ? pageTitle : seoTitle;
        final String htmlDescription = seoDescription.isEmpty() ? pageDescription : seoDescription;
        final String robotsValue = metaRobots.isEmpty() ? DEFAULT_META_ROBOTS : metaRobots;

        final String canonicalHost =
                getEnvironmentOrDefault("SEO_CANONICAL_HOST", DEFAULT_CANONICAL_HOST);
        final String canonicalAbsoluteUrl = absolutizeUrl(canonicalUrl, canonicalHost);
        final String defaultOgImagePath =
                getEnvironmentOrDefault("SEO_DEFAULT_OG_IMAGE", DEFAULT_OG_IMAGE_PATH);
        final String ogImageAbsoluteUrl =
                absolutizeUrl(ogImage.isEmpty() ? defaultOgImagePath : ogImage, canonicalHost);
        final String ogImageAltValue = ogImageAlt.isEmpty() ? htmlTitle : ogImageAlt;
        final boolean indexTemplate = mdheaders.contains("template: index");
        final String ogTypeValue =
                ogType.isEmpty()
                        ? (indexTemplate ? DEFAULT_OG_TYPE_WEBSITE : DEFAULT_OG_TYPE_CONTENT)
                        : ogType;
        final String twitterCardValue = twitterCard.isEmpty() ? DEFAULT_TWITTER_CARD : twitterCard;
        final String twitterSiteValue =
                twitterSite.isEmpty()
                        ? getEnvironmentOrDefault("SEO_TWITTER_SITE", "")
                        : twitterSite;
        if (isBlogContentPath(contentPath)) {
            headerInject = headerInject + blogContentManager.rssDiscoveryLink();
        }

        if (!htmlDescription.isEmpty()) {
            headerInject =
                    headerInject
                            + "<meta name='description' content='"
                            + escapeHtmlAttribute(htmlDescription)
                            + "'>";
        }
        headerInject =
                headerInject
                        + "<meta name='robots' content='"
                        + escapeHtmlAttribute(robotsValue)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta property='og:title' content='"
                        + escapeHtmlAttribute(htmlTitle)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta property='og:description' content='"
                        + escapeHtmlAttribute(htmlDescription)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta property='og:type' content='"
                        + escapeHtmlAttribute(ogTypeValue)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta property='og:url' content='"
                        + escapeHtmlAttribute(canonicalAbsoluteUrl)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta property='og:site_name' content='"
                        + escapeHtmlAttribute(DEFAULT_SITE_NAME)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta property='og:image' content='"
                        + escapeHtmlAttribute(ogImageAbsoluteUrl)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta property='og:image:alt' content='"
                        + escapeHtmlAttribute(ogImageAltValue)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta name='twitter:card' content='"
                        + escapeHtmlAttribute(twitterCardValue)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta name='twitter:title' content='"
                        + escapeHtmlAttribute(htmlTitle)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta name='twitter:description' content='"
                        + escapeHtmlAttribute(htmlDescription)
                        + "'>";
        headerInject =
                headerInject
                        + "<meta name='twitter:image' content='"
                        + escapeHtmlAttribute(ogImageAbsoluteUrl)
                        + "'>";
        if (!twitterSiteValue.isEmpty()) {
            headerInject =
                    headerInject
                            + "<meta name='twitter:site' content='"
                            + escapeHtmlAttribute(twitterSiteValue)
                            + "'>";
        }

        final String schemaTypeValue =
                schemaType.isEmpty()
                        ? (indexTemplate ? DEFAULT_SCHEMA_TYPE_INDEX : DEFAULT_SCHEMA_TYPE_CONTENT)
                        : schemaType;
        final String schemaImageAbsoluteUrl =
                absolutizeUrl(
                        schemaImage.isEmpty() ? ogImageAbsoluteUrl : schemaImage, canonicalHost);
        final String defaultSchemaAuthor = getSchemaAuthorDefaultName();
        final String schemaAuthorValue =
                schemaAuthor.isEmpty() ? defaultSchemaAuthor : schemaAuthor;
        final String defaultSchemaPublisher = getSchemaPublisherDefaultName();
        final String schemaPublisherValue =
                schemaPublisher.isEmpty() ? defaultSchemaPublisher : schemaPublisher;
        final String authorJobTitle = schemaAuthorDefaults.getProperty("jobTitle", "").trim();
        final String authorBioSnippet =
                buildAuthorBioSnippet(
                        contentFolder,
                        contentPath,
                        schemaAuthorValue,
                        authorJobTitle,
                        DEFAULT_AUTHOR_BIO_PATH);
        final String nextChallengeCtaSnippet =
                buildNextChallengeCtaSnippet(contentFolder, contentPath, nextChallengePath);
        final String conceptLearnedSnippet =
                buildConceptLearnedSnippet(
                        contentFolder,
                        contentPath,
                        conceptsLearnedRaw,
                        conceptSummary,
                        conceptReferenceLabel,
                        conceptReferenceUrl,
                        conceptReferenceLabel2,
                        conceptReferenceUrl2);
        final String blogCategorySnippet = buildBlogCategorySnippet(contentPath, categoriesRaw);
        final String blogPostNavigationSnippet =
                blogContentManager.renderPostNavigationHtml(contentPath);
        final Boolean schemaBreadcrumbEnabled = parseOptionalBoolean(schemaBreadcrumbEnabledRaw);
        final Boolean schemaHowToEnabled = parseOptionalBoolean(schemaHowToEnabledRaw);
        final List<String> schemaHowToSteps = parseHowToSteps(schemaHowToStepsRaw);
        final Boolean schemaVideoEnabled = parseOptionalBoolean(schemaVideoEnabledRaw);
        final boolean hideSidebar = Boolean.TRUE.equals(parseOptionalBoolean(hideSidebarRaw));
        final boolean wideToolLayout = "wide-tool".equals(layout);
        final boolean renderDocumentColumns = !indexTemplate && !hideSidebar;
        final String pageDateModified = resolveDateModified(pageLastModified, pageDatePublished);
        final String articleBylineSnippet =
                buildArticleBylineSnippet(
                        contentFolder,
                        contentPath,
                        schemaAuthorValue,
                        pageDatePublished,
                        pageDateModified,
                        DEFAULT_AUTHOR_BIO_PATH,
                        indexTemplate);
        final String schemaJsonLd =
                buildSchemaJsonLd(
                        canonicalHost,
                        canonicalAbsoluteUrl,
                        contentFolder,
                        contentPath,
                        breadcrumbs,
                        htmlTitle,
                        htmlDescription,
                        schemaTypeValue,
                        schemaImageAbsoluteUrl,
                        schemaAuthorValue,
                        schemaPublisherValue,
                        pageDatePublished,
                        pageDateModified,
                        firstYouTubeVideoId,
                        schemaBreadcrumbEnabled,
                        schemaHowToEnabled,
                        schemaHowToSteps,
                        schemaVideoEnabled,
                        schemaVideoId,
                        categoriesRaw);
        if (!schemaJsonLd.isEmpty()) {
            headerInject = headerInject + schemaJsonLd;
        }

        StringBuilder html = new StringBuilder();
        String pageStart =
                guiManagement.getPageStart(
                        htmlTitle,
                        """
        <script src='%s'></script>
        <script src='%s'></script>
        """
                                        .formatted(
                                                AssetVersion.versionedPath("/js/toc.js"),
                                                AssetVersion.versionedPath(
                                                        "/js/externalize-links.js"))
                                + headerInject,
                        canonicalAbsoluteUrl);
        if (wideToolLayout) {
            pageStart =
                    pageStart.replace(
                            "<body><div class='content'>",
                            "<body class='wide-tool-page'><div class='content'>");
        }
        html.append(pageStart);

        html.append(guiManagement.getMenuAsHTML());
        // todo: create proper templates
        if (renderDocumentColumns) {
            html.append("<section class='doc-columns'>");
            html.append("<div class='right-column'>");
        }
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append(bcHtmlHeader.toString());
        html.append("<div class=\"main-text-content\">\n");
        final String postIntroSnippet = articleBylineSnippet + blogCategorySnippet;
        String renderedMarkdown =
                insertArticleBylineAfterFirstHeading(renderer.render(document), postIntroSnippet);
        if (wideToolLayout) {
            renderedMarkdown = wrapWideToolRenderedContent(renderedMarkdown);
        }
        html.append(renderedMarkdown);
        html.append(conceptLearnedSnippet);
        html.append("</div>\n");
        html.append(nextChallengeCtaSnippet);
        html.append(blogPostNavigationSnippet);
        html.append(authorBioSnippet);
        html.append(guiManagement.getEndOfMainContentMarker());
        if (renderDocumentColumns) {
            html.append("</div>");
            html.append("<aside class='left-column' aria-label='Learning links'>");
            html.append("<nav class='side-toc' aria-label='Learning and reference links'>");
            final String renderedSideToc = renderer.render(parser.parse(dropDownMenuAsMarkdown()));
            html.append(wideToolLayout ? wrapWideToolSideToc(renderedSideToc) : renderedSideToc);
            html.append("</nav>");
            html.append("</aside>");
            html.append("</section>");
        }
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());

        return html.toString();
    }

    private String wrapWideToolRenderedContent(final String renderedMarkdown) {
        final Matcher matcher = WIDE_TOOL_CLIENT_SECTION_PATTERN.matcher(renderedMarkdown);
        if (!matcher.find()) {
            return renderedMarkdown;
        }

        final String beforeClient = renderedMarkdown.substring(0, matcher.start()).strip();
        final String client = matcher.group(1).strip();
        final String afterClient = renderedMarkdown.substring(matcher.end()).strip();

        final StringBuilder wrapped = new StringBuilder();
        if (!beforeClient.isEmpty()) {
            wrapped.append("<div class=\"wide-tool-copy-block wide-tool-copy-block-top\">\n");
            wrapped.append(beforeClient);
            wrapped.append("\n</div>\n");
        }
        wrapped.append("<div class=\"wide-tool-client-breakout\">\n");
        wrapped.append(client);
        wrapped.append("\n</div>\n");
        if (!afterClient.isEmpty()) {
            wrapped.append("<div class=\"wide-tool-copy-block wide-tool-copy-block-bottom\">\n");
            wrapped.append(afterClient);
            wrapped.append("\n</div>\n");
        }
        return wrapped.toString();
    }

    private String wrapWideToolSideToc(final String renderedSideToc) {
        final String rootStartTag = "<ul class=\"side-toc-root\">";
        final int rootStart = renderedSideToc.indexOf(rootStartTag);
        if (rootStart < 0) {
            return renderedSideToc;
        }

        final int rootStartTagEnd = rootStart + rootStartTag.length();
        int depth = 1;
        int rootCloseStart = -1;
        int rootEnd = -1;
        final Matcher ulMatcher = HTML_UL_TAG_PATTERN.matcher(renderedSideToc);
        ulMatcher.region(rootStartTagEnd, renderedSideToc.length());
        while (ulMatcher.find()) {
            if ("/".equals(ulMatcher.group(1))) {
                depth--;
                if (depth == 0) {
                    rootCloseStart = ulMatcher.start();
                    rootEnd = ulMatcher.end();
                    break;
                }
            } else {
                depth++;
            }
        }

        if (rootCloseStart < 0 || rootEnd < 0) {
            return renderedSideToc;
        }

        final List<String> topLevelItems =
                splitTopLevelListItems(renderedSideToc.substring(rootStartTagEnd, rootCloseStart));
        if (topLevelItems.size() < 9) {
            return renderedSideToc;
        }

        final String prefix = renderedSideToc.substring(0, rootStart).strip();
        final String supportHtml = renderedSideToc.substring(rootEnd).strip();
        final StringBuilder wrapped = new StringBuilder();
        if (!prefix.isEmpty()) {
            wrapped.append(prefix).append("\n");
        }
        wrapped.append("<div class=\"wide-tool-side-toc-grid\">\n");
        appendWideToolSideTocColumn(wrapped, "learning", topLevelItems.subList(0, 2), "");
        appendWideToolSideTocColumn(wrapped, "reference", topLevelItems.subList(2, 7), "");
        appendWideToolSideTocColumn(
                wrapped, "support", topLevelItems.subList(7, topLevelItems.size()), supportHtml);
        wrapped.append("</div>\n");
        return wrapped.toString();
    }

    private List<String> splitTopLevelListItems(final String listContent) {
        final List<String> items = new ArrayList<>();
        final Matcher liMatcher = HTML_LI_TAG_PATTERN.matcher(listContent);
        int depth = 0;
        int itemStart = -1;
        while (liMatcher.find()) {
            if ("/".equals(liMatcher.group(1))) {
                depth--;
                if (depth == 0 && itemStart >= 0) {
                    items.add(listContent.substring(itemStart, liMatcher.end()));
                    itemStart = -1;
                }
            } else {
                if (depth == 0) {
                    itemStart = liMatcher.start();
                }
                depth++;
            }
        }
        return items;
    }

    private void appendWideToolSideTocColumn(
            final StringBuilder wrapped,
            final String columnName,
            final List<String> items,
            final String supportHtml) {
        wrapped.append("<div class=\"wide-tool-side-toc-column wide-tool-side-toc-column-")
                .append(columnName)
                .append("\">\n");
        wrapped.append("<ul class=\"side-toc-root\">\n");
        for (final String item : items) {
            wrapped.append(item.strip()).append("\n");
        }
        wrapped.append("</ul>\n");
        if (!supportHtml.isEmpty()) {
            wrapped.append("<div class=\"wide-tool-side-toc-support\">\n");
            wrapped.append(supportHtml);
            wrapped.append("\n</div>\n");
        }
        wrapped.append("</div>\n");
    }

    // TODO: move this into a markdown file so it can be cached and amended easily
    private String dropDownMenuAsMarkdown() {
        if (sideMenuText.isEmpty()) {
            sideMenuText = getResourceAsString("partials/content-index.md");
        }

        return sideMenuText;
    }

    // TODO: improve the macro parsing
    // TODO: add a variables macro so we can set variables like schemeHost (http://localhost:4567)
    // and replace variables in the docs - should add a 'default.varname': parsing in the markdown,
    // would allow showing the 'proper url' regardless of environment hosting
    /*
       Macros are added to the markdown with the following syntax {{<macro_name>}}
       e.g. {{<HOST_URL>}}
       Some of these macros are direct string replacement injection from the params map
       Others like youtube-embed have been hard coded here

    */
    private String processMacrosInContentLine(
            String line, Map<String, String> params, String contentPath) {

        if (!line.contains("{{<")) {
            return line;
        }

        //        String youTubeHtmlBlock = """
        // <div class="video-container">
        //    <iframe class='youtube-video' title='Watch Video - $2' loading='lazy'
        // src="https://www.youtube.com/embed/$1" allow="autoplay; encrypted-media"
        // allowfullscreen></iframe>
        // </div>
        // <div><p class="center-text"><a href="https://www.youtube.com/watch?v=$1"
        // target="_blank">Watch on YouTube - $2</a></p></div>
        //        """;

        // use YoutubeFacade https://github.com/justinribeiro/lite-youtube
        String youTubeHtmlBlock =
"""
<lite-youtube videoid="$1">
  <a class="lite-youtube-fallback" href="https://www.youtube.com/watch?v=$1">Watch on YouTube: "$2"</a>
</lite-youtube>
        """;

        String youtubeMacroRegex =
                "\\{\\{<youtube-embed key=\"([a-zA-Z0-9_-]+)\" title=\"(.+)\">}}";
        line = line.replaceAll(youtubeMacroRegex, youTubeHtmlBlock);

        line =
                processLiveRequestMacro(
                        line, "sim-live-request", "sim-live-request", "false", contentPath);
        line =
                processLiveRequestMacro(
                        line, "api-live-request", "api-live-request", "true", contentPath);

        if (line.contains("{{<blog-index>}}")) {
            line = line.replace("{{<blog-index>}}", blogContentManager.renderBlogIndexHtml());
        }

        if (line.contains("{{<PARTIAL_SNIPPET")) {
            String partialMacroRegex = "\\{\\{<PARTIAL_SNIPPET filename=\"(.+)\">}}";
            Pattern r = Pattern.compile(partialMacroRegex);
            Matcher m = r.matcher(line);
            if (m.find()) {
                String filename = m.group(1);
                String partialContent = getResourceAsString(filename);
                line = line.replaceAll(partialMacroRegex, partialContent);
            }
        }

        for (String paramReplace : params.keySet()) {
            String macroRegex = "\\{\\{<%s>}}".formatted(paramReplace);
            line = line.replaceAll(macroRegex, params.get(paramReplace));
        }
        return line;
    }

    private String processLiveRequestMacro(
            final String line,
            final String macroName,
            final String placeholderClass,
            final String defaultEditable,
            final String contentPath) {
        if (!line.contains("{{<" + macroName)) {
            return line;
        }

        final Pattern macroPattern = Pattern.compile("\\{\\{<" + macroName + "\\s+([\\s\\S]*?)>}}");
        final Matcher macroMatcher = macroPattern.matcher(line);
        final StringBuffer processedLine = new StringBuffer();
        while (macroMatcher.find()) {
            final Map<String, String> attributes = parseMacroAttributes(macroMatcher.group(1));
            final String replacement =
                    renderLiveRequestPlaceholder(
                            attributes, placeholderClass, defaultEditable, contentPath);
            macroMatcher.appendReplacement(processedLine, Matcher.quoteReplacement(replacement));
        }
        macroMatcher.appendTail(processedLine);
        return processedLine.toString();
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

    private String renderLiveRequestPlaceholder(
            final Map<String, String> attributes,
            final String placeholderClass,
            final String defaultEditable,
            final String contentPath) {
        final String method = attributes.getOrDefault("method", "GET");
        final String path = attributes.getOrDefault("path", "/");
        final String editable = attributes.getOrDefault("editable", defaultEditable);
        final String editMode =
                attributes.getOrDefault(
                        "edit-mode", defaultEditModeFor(placeholderClass, editable));
        final String allowedPathPrefixes =
                attributes.getOrDefault(
                        "allowed-path-prefixes", defaultAllowedPathPrefixesFor(placeholderClass));
        final boolean openDetails = isTruthy(attributes.get("open"));
        final boolean challengeRequest = isTruthy(attributes.get("challenge-request"));
        final boolean wrapInDetails = openDetails || isTruthy(attributes.get("details"));
        final String summary = attributes.getOrDefault("summary", "Try it now");
        if (isSolutionApiSolvingRequest(
                placeholderClass, openDetails, challengeRequest, contentPath)) {
            attributes.putIfAbsent("challenge-id", solutionChallengeIds.get(contentPath));
        }

        final StringBuilder html = new StringBuilder();
        html.append("<div class=\"")
                .append(placeholderClass)
                .append("\" data-method=\"")
                .append(escapeHtmlAttribute(method))
                .append("\" data-path=\"")
                .append(escapeHtmlAttribute(path))
                .append("\" data-editable=\"")
                .append(escapeHtmlAttribute(editable))
                .append("\" data-edit-mode=\"")
                .append(escapeHtmlAttribute(editMode))
                .append("\"");
        if (!allowedPathPrefixes.isEmpty()) {
            html.append(" data-allowed-path-prefixes=\"")
                    .append(escapeHtmlAttribute(allowedPathPrefixes))
                    .append("\"");
        }

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
        if (!wrapInDetails) {
            return html.toString();
        }

        return "<details class=\"sim-live-request-details\""
                + (openDetails ? " open" : "")
                + "><summary>"
                + escapeHtmlAttribute(summary)
                + "</summary>"
                + html
                + "</details>";
    }

    private String defaultEditModeFor(final String placeholderClass, final String editable) {
        if ("api-live-request".equals(placeholderClass)) {
            return "fixed";
        }
        return isTruthy(editable) ? "adhoc" : "readonly";
    }

    private String defaultAllowedPathPrefixesFor(final String placeholderClass) {
        if ("api-live-request".equals(placeholderClass)) {
            return API_CHALLENGE_ALLOWED_PATH_PREFIXES;
        }
        return "";
    }

    private String solutionEndpointExperiment(
            final String contentFolder, final String contentPath) {
        if (contentPath == null || !solutionChallengeIds.containsKey(contentPath)) {
            return "";
        }

        final Map<String, String> mainRequestAttributes =
                mainApiSolvingRequestAttributes(contentFolder, contentPath);
        if (mainRequestAttributes.isEmpty()) {
            return "";
        }

        final Map<String, String> experimentAttributes = new LinkedHashMap<>(mainRequestAttributes);
        experimentAttributes.remove("open");
        experimentAttributes.remove("challenge-request");
        experimentAttributes.remove("challenge-id");
        experimentAttributes.put("details", "true");
        experimentAttributes.put("summary", "Experiment with this endpoint");
        experimentAttributes.put("editable", "true");
        experimentAttributes.put("edit-mode", "adhoc");
        experimentAttributes.put("allowed-path-prefixes", API_CHALLENGE_ALLOWED_PATH_PREFIXES);
        return renderLiveRequestPlaceholder(experimentAttributes, "api-live-request", "true", null);
    }

    private Map<String, String> mainApiSolvingRequestAttributes(
            final String contentFolder, final String contentPath) {
        final String contentResource = contentFolder + contentPath + ".md";
        if (!markdownContentPaths.contains(contentResource)) {
            return Collections.emptyMap();
        }

        final String markdown = getResourceAsString(contentResource);
        final Pattern macroPattern = Pattern.compile("\\{\\{<api-live-request\\s+([\\s\\S]*?)>}}");
        final Matcher matcher = macroPattern.matcher(markdown);
        while (matcher.find()) {
            final Map<String, String> attributes = parseMacroAttributes(matcher.group(1));
            if (isTruthy(attributes.get("open")) || isTruthy(attributes.get("challenge-request"))) {
                return attributes;
            }
        }
        return Collections.emptyMap();
    }

    private boolean isTruthy(final String value) {
        if (value == null) {
            return false;
        }
        final String trimmed = value.trim().toLowerCase();
        return trimmed.equals("true") || trimmed.equals("yes") || trimmed.equals("on");
    }

    private boolean isSolutionApiSolvingRequest(
            final String placeholderClass,
            final boolean openDetails,
            final boolean challengeRequest,
            final String contentPath) {
        return "api-live-request".equals(placeholderClass)
                && (openDetails || challengeRequest)
                && contentPath != null
                && solutionChallengeIds.containsKey(contentPath);
    }

    private String solutionChallengeCompletedMessage(final String contentPath) {
        if (contentPath == null || !solutionChallengeIds.containsKey(contentPath)) {
            return "";
        }

        return "<aside class=\"solution-challenge-completed\" data-challenge-id=\""
                + escapeHtmlAttribute(solutionChallengeIds.get(contentPath))
                + "\" hidden role=\"status\"><strong>Challenge Completed</strong></aside>\n\n";
    }

    private String getResourceAsString(String fileName) {
        return new BufferedReader(new InputStreamReader(getResourceAsStream(fileName)))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    private String getEnvironmentOrDefault(final String envName, final String defaultValue) {
        final String envValue = System.getenv(envName);
        if (envValue == null || envValue.trim().isEmpty()) {
            return defaultValue;
        }
        return envValue.trim();
    }

    private String absolutizeUrl(final String url, final String host) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        final String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return host + trimmed;
        }
        return host + "/" + trimmed;
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

    private String buildBreadcrumbHtml(
            final String contentFolder,
            final String contentPath,
            final String[] breadcrumbs,
            final String categoriesRaw) {

        if (isIndividualBlogPostPath(contentPath)) {
            return buildBlogPostBreadcrumbHtml(contentPath, categoriesRaw);
        }
        if (isOpenApiUiBreadcrumbPath(contentPath)) {
            return buildOpenApiUiBreadcrumbHtml(contentPath);
        }

        final List<BreadcrumbHtml.Item> breadcrumbItems = new ArrayList<>();
        String bcPath = "";
        int linksInBreadcrumb = 0;
        if (breadcrumbs.length > 0) {
            for (String bc : breadcrumbs) {
                bcPath = bcPath + bc;

                if (!bc.isEmpty()) {

                    if (contentPath.endsWith(bc)) {
                        breadcrumbItems.add(BreadcrumbHtml.current(bc));
                    } else {
                        if (markdownContentPaths.contains(contentFolder + "/" + bcPath + ".md")) {
                            linksInBreadcrumb++;
                            breadcrumbItems.add(BreadcrumbHtml.link(bc, "/" + bcPath));
                        }
                    }
                }
                bcPath = bcPath + "/";
            }
        }

        if (linksInBreadcrumb == 0) {
            return "";
        }

        return BreadcrumbHtml.render(breadcrumbItems);
    }

    private String buildOpenApiUiBreadcrumbHtml(final String contentPath) {

        final Optional<String> aboutSlug = openApiUiToolAboutSlug(contentPath);
        if (aboutSlug.isPresent()) {
            return BreadcrumbHtml.render(
                    List.of(
                            BreadcrumbHtml.link("Tools", "/tools"),
                            BreadcrumbHtml.link("Online Clients", "/tools/online-clients"),
                            BreadcrumbHtml.link(
                                    openApiUiToolBreadcrumbLabel(contentPath),
                                    "/tools/online-clients/" + aboutSlug.get()),
                            BreadcrumbHtml.current("About")));
        }

        if (isOnlineOpenApiUiToolPath(contentPath)) {
            return BreadcrumbHtml.render(
                    List.of(
                            BreadcrumbHtml.link("Tools", "/tools"),
                            BreadcrumbHtml.link("Online Clients", "/tools/online-clients"),
                            BreadcrumbHtml.current(openApiUiToolBreadcrumbLabel(contentPath))));
        }

        return BreadcrumbHtml.render(
                List.of(
                        BreadcrumbHtml.link("Reference", "/reference"),
                        BreadcrumbHtml.link("OpenAPI", "/reference/openapi"),
                        BreadcrumbHtml.current(openApiUiToolBreadcrumbLabel(contentPath))));
    }

    private String buildBlogPostBreadcrumbHtml(
            final String contentPath, final String categoriesRaw) {
        final String postSlug = contentPath.substring(contentPath.lastIndexOf("/") + 1);
        final Optional<String> primaryCategory = primaryBlogCategory(categoriesRaw);

        final List<BreadcrumbHtml.Item> breadcrumbItems = new ArrayList<>();
        breadcrumbItems.add(BreadcrumbHtml.link("Blog", "/blog"));
        if (primaryCategory.isPresent()) {
            breadcrumbItems.add(
                    BreadcrumbHtml.link(
                            primaryCategory.get(),
                            "/blog/categories/"
                                    + BlogContentManager.categorySlug(primaryCategory.get())));
        }
        breadcrumbItems.add(BreadcrumbHtml.current(postSlug));
        return BreadcrumbHtml.render(breadcrumbItems);
    }

    private String buildSchemaJsonLd(
            final String canonicalHost,
            final String canonicalAbsoluteUrl,
            final String contentFolder,
            final String contentPath,
            final String[] breadcrumbs,
            final String htmlTitle,
            final String htmlDescription,
            final String schemaTypeValue,
            final String schemaImageAbsoluteUrl,
            final String schemaAuthor,
            final String schemaPublisherValue,
            final String pageDatePublished,
            final String pageDateModified,
            final String firstYouTubeVideoId,
            final Boolean schemaBreadcrumbEnabled,
            final Boolean schemaHowToEnabled,
            final List<String> schemaHowToSteps,
            final Boolean schemaVideoEnabled,
            final String schemaVideoId,
            final String categoriesRaw) {

        final String orgName =
                schemaPublisherValue.isEmpty()
                        ? getEnvironmentOrDefault("SEO_SCHEMA_ORG_NAME", DEFAULT_SITE_NAME)
                        : schemaPublisherValue;
        final String websiteName =
                getEnvironmentOrDefault("SEO_SCHEMA_WEBSITE_NAME", DEFAULT_SITE_NAME);
        final String logoUrl =
                absolutizeUrl(
                        getEnvironmentOrDefault("SEO_SCHEMA_LOGO_URL", DEFAULT_SCHEMA_LOGO_PATH),
                        canonicalHost);

        final List<String> sameAsLinks =
                parseCommaSeparatedUrls(System.getenv("SEO_SCHEMA_SAME_AS"));
        final String searchActionTemplate = System.getenv("SEO_SCHEMA_SEARCH_URL_TEMPLATE");
        final String authorUrl = getSchemaAuthorDefaultUrl();
        final List<String> authorSameAs =
                parseCommaSeparatedUrls(schemaAuthorDefaults.getProperty("sameAs", ""));
        final String authorJobTitle = schemaAuthorDefaults.getProperty("jobTitle", "").trim();
        final String publisherLegalName =
                schemaPublisherDefaults.getProperty("legalName", "").trim();
        final List<String> publisherSameAs =
                parseCommaSeparatedUrls(schemaPublisherDefaults.getProperty("sameAs", ""));
        final String publisherContactType =
                schemaPublisherDefaults.getProperty("contactType", "").trim();
        final String publisherEmail = schemaPublisherDefaults.getProperty("email", "").trim();
        final String publisherPhone = schemaPublisherDefaults.getProperty("telephone", "").trim();
        final String orgId = canonicalHost + "#organization";
        final String websiteId = canonicalHost + "#website";
        final String personId = canonicalHost + "#author";
        final String pageId = canonicalAbsoluteUrl + "#webpage";

        final StringBuilder scripts = new StringBuilder();
        scripts.append(
                toJsonLdScript(
                        buildOrganizationJson(
                                orgId,
                                orgName,
                                canonicalHost,
                                logoUrl,
                                sameAsLinks,
                                publisherLegalName,
                                publisherSameAs,
                                publisherContactType,
                                publisherEmail,
                                publisherPhone)));
        scripts.append(
                toJsonLdScript(
                        buildPersonJson(
                                personId,
                                schemaAuthor,
                                authorUrl,
                                authorSameAs,
                                authorJobTitle,
                                orgId)));
        scripts.append(
                toJsonLdScript(
                        buildWebsiteJson(
                                websiteId,
                                websiteName,
                                canonicalHost,
                                orgId,
                                searchActionTemplate)));
        scripts.append(
                toJsonLdScript(
                        buildPageJson(
                                schemaTypeValue,
                                pageId,
                                htmlTitle,
                                htmlDescription,
                                canonicalAbsoluteUrl,
                                schemaImageAbsoluteUrl,
                                personId,
                                orgId,
                                pageDatePublished,
                                pageDateModified)));

        final boolean includeBreadcrumb =
                schemaBreadcrumbEnabled == null || schemaBreadcrumbEnabled;
        if (includeBreadcrumb) {
            final String breadcrumbJson =
                    buildBreadcrumbListJson(
                            canonicalHost, contentFolder, contentPath, breadcrumbs, categoriesRaw);
            scripts.append(toJsonLdScript(breadcrumbJson));
        }

        final String howToJson =
                buildHowToJson(
                        contentPath,
                        canonicalAbsoluteUrl,
                        htmlTitle,
                        htmlDescription,
                        schemaImageAbsoluteUrl,
                        schemaHowToEnabled,
                        schemaHowToSteps);
        scripts.append(toJsonLdScript(howToJson));

        final String videoJson =
                buildVideoObjectJson(
                        canonicalAbsoluteUrl,
                        htmlTitle,
                        htmlDescription,
                        schemaImageAbsoluteUrl,
                        firstYouTubeVideoId,
                        orgId,
                        schemaVideoEnabled,
                        schemaVideoId);
        scripts.append(toJsonLdScript(videoJson));

        return scripts.toString();
    }

    private String toJsonLdScript(final String json) {
        if (json == null || json.isEmpty()) {
            return "";
        }
        // Prevent accidental script close within inline JSON.
        final String safeJson = json.replace("</", "<\\/");
        return "<script type='application/ld+json'>" + safeJson + "</script>";
    }

    private List<String> parseCommaSeparatedUrls(final String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String buildOrganizationJson(
            final String organizationId,
            final String organizationName,
            final String canonicalHost,
            final String logoUrl,
            final List<String> sameAsLinks,
            final String legalName,
            final List<String> publisherSameAs,
            final String contactType,
            final String email,
            final String telephone) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"Organization\",");
        json.append("\"@id\":\"").append(escapeJsonValue(organizationId)).append("\",");
        json.append("\"name\":\"").append(escapeJsonValue(organizationName)).append("\",");
        json.append("\"url\":\"").append(escapeJsonValue(canonicalHost)).append("\"");
        if (!legalName.isEmpty()) {
            json.append(",\"legalName\":\"").append(escapeJsonValue(legalName)).append("\"");
        }

        if (!logoUrl.isEmpty()) {
            json.append(",\"logo\":{\"@type\":\"ImageObject\",\"url\":\"")
                    .append(escapeJsonValue(logoUrl))
                    .append("\"}");
        }

        final List<String> mergedSameAs = new ArrayList<>();
        mergedSameAs.addAll(sameAsLinks);
        for (String aLink : publisherSameAs) {
            if (!mergedSameAs.contains(aLink)) {
                mergedSameAs.add(aLink);
            }
        }

        if (!mergedSameAs.isEmpty()) {
            json.append(",\"sameAs\":[");
            for (int i = 0; i < mergedSameAs.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append("\"").append(escapeJsonValue(mergedSameAs.get(i))).append("\"");
            }
            json.append("]");
        }

        if (!contactType.isEmpty() || !email.isEmpty() || !telephone.isEmpty()) {
            json.append(",\"contactPoint\":{\"@type\":\"ContactPoint\"");
            if (!contactType.isEmpty()) {
                json.append(",\"contactType\":\"")
                        .append(escapeJsonValue(contactType))
                        .append("\"");
            }
            if (!email.isEmpty()) {
                json.append(",\"email\":\"").append(escapeJsonValue(email)).append("\"");
            }
            if (!telephone.isEmpty()) {
                json.append(",\"telephone\":\"").append(escapeJsonValue(telephone)).append("\"");
            }
            json.append("}");
        }

        json.append("}");
        return json.toString();
    }

    private String buildPersonJson(
            final String personId,
            final String authorName,
            final String authorUrl,
            final List<String> sameAsLinks,
            final String jobTitle,
            final String worksForOrgId) {
        if (authorName == null || authorName.trim().isEmpty()) {
            return "";
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"Person\",");
        json.append("\"@id\":\"").append(escapeJsonValue(personId)).append("\",");
        json.append("\"name\":\"").append(escapeJsonValue(authorName)).append("\"");
        if (authorUrl != null && !authorUrl.isEmpty()) {
            json.append(",\"url\":\"").append(escapeJsonValue(authorUrl)).append("\"");
        }
        if (jobTitle != null && !jobTitle.isEmpty()) {
            json.append(",\"jobTitle\":\"").append(escapeJsonValue(jobTitle)).append("\"");
        }
        if (!sameAsLinks.isEmpty()) {
            json.append(",\"sameAs\":[");
            for (int i = 0; i < sameAsLinks.size(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append("\"").append(escapeJsonValue(sameAsLinks.get(i))).append("\"");
            }
            json.append("]");
        }
        if (worksForOrgId != null && !worksForOrgId.isEmpty()) {
            json.append(",\"worksFor\":{\"@id\":\"")
                    .append(escapeJsonValue(worksForOrgId))
                    .append("\"}");
        }
        json.append("}");
        return json.toString();
    }

    private String buildWebsiteJson(
            final String websiteId,
            final String websiteName,
            final String canonicalHost,
            final String organizationId,
            final String searchActionTemplate) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"WebSite\",");
        json.append("\"@id\":\"").append(escapeJsonValue(websiteId)).append("\",");
        json.append("\"name\":\"").append(escapeJsonValue(websiteName)).append("\",");
        json.append("\"url\":\"").append(escapeJsonValue(canonicalHost)).append("\"");
        json.append(",\"publisher\":{\"@id\":\"")
                .append(escapeJsonValue(organizationId))
                .append("\"}");

        if (searchActionTemplate != null && !searchActionTemplate.trim().isEmpty()) {
            json.append(",\"potentialAction\":{");
            json.append("\"@type\":\"SearchAction\",");
            json.append("\"target\":\"")
                    .append(escapeJsonValue(searchActionTemplate.trim()))
                    .append("\",");
            json.append("\"query-input\":\"required name=search_term_string\"");
            json.append("}");
        }

        json.append("}");
        return json.toString();
    }

    private String buildPageJson(
            final String schemaTypeValue,
            final String pageId,
            final String htmlTitle,
            final String htmlDescription,
            final String canonicalAbsoluteUrl,
            final String schemaImageAbsoluteUrl,
            final String personId,
            final String organizationId,
            final String pageDatePublished,
            final String pageDateModified) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"").append(escapeJsonValue(schemaTypeValue)).append("\",");
        json.append("\"@id\":\"").append(escapeJsonValue(pageId)).append("\",");
        json.append("\"name\":\"").append(escapeJsonValue(htmlTitle)).append("\",");
        if (isArticleLikeSchemaType(schemaTypeValue)) {
            json.append("\"headline\":\"").append(escapeJsonValue(htmlTitle)).append("\",");
        }
        if (!htmlDescription.isEmpty()) {
            json.append("\"description\":\"")
                    .append(escapeJsonValue(htmlDescription))
                    .append("\",");
        }
        json.append("\"url\":\"").append(escapeJsonValue(canonicalAbsoluteUrl)).append("\",");
        json.append("\"mainEntityOfPage\":\"")
                .append(escapeJsonValue(canonicalAbsoluteUrl))
                .append("\"");

        if (!schemaImageAbsoluteUrl.isEmpty()) {
            json.append(",\"image\":\"")
                    .append(escapeJsonValue(schemaImageAbsoluteUrl))
                    .append("\"");
        }
        if (personId != null && !personId.isEmpty()) {
            json.append(",\"author\":{\"@id\":\"").append(escapeJsonValue(personId)).append("\"}");
        }
        if (organizationId != null && !organizationId.isEmpty()) {
            json.append(",\"publisher\":{\"@id\":\"")
                    .append(escapeJsonValue(organizationId))
                    .append("\"}");
        }
        if (!pageDatePublished.isEmpty()) {
            json.append(",\"datePublished\":\"")
                    .append(escapeJsonValue(pageDatePublished))
                    .append("\"");
        }
        if (!pageDateModified.isEmpty()) {
            json.append(",\"dateModified\":\"")
                    .append(escapeJsonValue(pageDateModified))
                    .append("\"");
        }

        json.append("}");
        return json.toString();
    }

    private String buildBreadcrumbListJson(
            final String canonicalHost,
            final String contentFolder,
            final String contentPath,
            final String[] breadcrumbs,
            final String categoriesRaw) {
        if (breadcrumbs == null || breadcrumbs.length == 0) {
            return "";
        }
        if ("/index".equals(contentPath)) {
            return "";
        }
        if (isIndividualBlogPostPath(contentPath)) {
            return buildBlogPostBreadcrumbListJson(canonicalHost, contentPath, categoriesRaw);
        }
        if (isOpenApiUiBreadcrumbPath(contentPath)) {
            return buildOpenApiUiBreadcrumbListJson(canonicalHost, contentPath);
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"BreadcrumbList\",");
        json.append("\"itemListElement\":[");

        int position = 1;
        json.append("{\"@type\":\"ListItem\",\"position\":")
                .append(position++)
                .append(",\"name\":\"Home\",\"item\":\"")
                .append(escapeJsonValue(canonicalHost))
                .append("\"}");

        String path = "";
        for (String crumb : breadcrumbs) {
            if (crumb == null || crumb.isEmpty()) {
                continue;
            }
            path = path + "/" + crumb;
            if (!path.equals(contentPath)
                    && !markdownContentPaths.contains(contentFolder + path + ".md")) {
                continue;
            }
            json.append(",{\"@type\":\"ListItem\",\"position\":")
                    .append(position++)
                    .append(",\"name\":\"")
                    .append(escapeJsonValue(humanizeSlug(crumb)))
                    .append("\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + path))
                    .append("\"}");
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private String buildOpenApiUiBreadcrumbListJson(
            final String canonicalHost, final String contentPath) {

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"BreadcrumbList\",");
        json.append("\"itemListElement\":[");
        json.append("{\"@type\":\"ListItem\",\"position\":1,\"name\":\"Home\",\"item\":\"")
                .append(escapeJsonValue(canonicalHost))
                .append("\"}");
        final Optional<String> aboutSlug = openApiUiToolAboutSlug(contentPath);
        if (aboutSlug.isPresent()) {
            final String liveToolPath = "/tools/online-clients/" + aboutSlug.get();
            json.append(",{\"@type\":\"ListItem\",\"position\":2,\"name\":\"Tools\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + "/tools"))
                    .append("\"}");
            json.append(
                            ",{\"@type\":\"ListItem\",\"position\":3,\"name\":\"Online Clients\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + "/tools/online-clients"))
                    .append("\"}");
            json.append(",{\"@type\":\"ListItem\",\"position\":4,\"name\":\"")
                    .append(escapeJsonValue(openApiUiToolBreadcrumbLabel(contentPath)))
                    .append("\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + liveToolPath))
                    .append("\"}");
            json.append(",{\"@type\":\"ListItem\",\"position\":5,\"name\":\"About\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + contentPath))
                    .append("\"}");
        } else if (isOnlineOpenApiUiToolPath(contentPath)) {
            json.append(",{\"@type\":\"ListItem\",\"position\":2,\"name\":\"Tools\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + "/tools"))
                    .append("\"}");
            json.append(
                            ",{\"@type\":\"ListItem\",\"position\":3,\"name\":\"Online Clients\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + "/tools/online-clients"))
                    .append("\"}");
        } else {
            json.append(
                            ",{\"@type\":\"ListItem\",\"position\":2,\"name\":\"Reference\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + "/reference"))
                    .append("\"}");
            json.append(",{\"@type\":\"ListItem\",\"position\":3,\"name\":\"OpenAPI\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + "/reference/openapi"))
                    .append("\"}");
        }
        if (aboutSlug.isEmpty()) {
            json.append(",{\"@type\":\"ListItem\",\"position\":4,\"name\":\"")
                    .append(escapeJsonValue(openApiUiToolBreadcrumbLabel(contentPath)))
                    .append("\",\"item\":\"")
                    .append(escapeJsonValue(canonicalHost + contentPath))
                    .append("\"}");
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private String buildBlogPostBreadcrumbListJson(
            final String canonicalHost, final String contentPath, final String categoriesRaw) {
        final String postSlug = contentPath.substring(contentPath.lastIndexOf("/") + 1);
        final Optional<String> primaryCategory = primaryBlogCategory(categoriesRaw);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"BreadcrumbList\",");
        json.append("\"itemListElement\":[");
        json.append("{\"@type\":\"ListItem\",\"position\":1,\"name\":\"Home\",\"item\":\"")
                .append(escapeJsonValue(canonicalHost))
                .append("\"}");
        json.append(",{\"@type\":\"ListItem\",\"position\":2,\"name\":\"Blog\",\"item\":\"")
                .append(escapeJsonValue(canonicalHost + "/blog"))
                .append("\"}");
        int currentPosition = 3;
        if (primaryCategory.isPresent()) {
            json.append(",{\"@type\":\"ListItem\",\"position\":")
                    .append(currentPosition++)
                    .append(",\"name\":\"")
                    .append(escapeJsonValue(primaryCategory.get()))
                    .append("\",\"item\":\"")
                    .append(
                            escapeJsonValue(
                                    canonicalHost
                                            + "/blog/categories/"
                                            + BlogContentManager.categorySlug(
                                                    primaryCategory.get())))
                    .append("\"}");
        }
        json.append(",{\"@type\":\"ListItem\",\"position\":")
                .append(currentPosition)
                .append(",\"name\":\"")
                .append(escapeJsonValue(humanizeSlug(postSlug)))
                .append("\",\"item\":\"")
                .append(escapeJsonValue(canonicalHost + contentPath))
                .append("\"}");
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private String buildHowToJson(
            final String contentPath,
            final String canonicalAbsoluteUrl,
            final String htmlTitle,
            final String htmlDescription,
            final String schemaImageAbsoluteUrl,
            final Boolean schemaHowToEnabled,
            final List<String> schemaHowToSteps) {
        final boolean isHowToSection =
                contentPath.startsWith("/apichallenges/solutions/")
                        || contentPath.startsWith("/tutorials/");
        final boolean includeHowTo =
                schemaHowToEnabled == null ? isHowToSection : schemaHowToEnabled;
        if (!includeHowTo) {
            return "";
        }

        final List<String> steps = new ArrayList<>();
        if (schemaHowToSteps != null && !schemaHowToSteps.isEmpty()) {
            steps.addAll(schemaHowToSteps);
        }
        if (steps.size() < 2) {
            return "";
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"HowTo\",");
        json.append("\"name\":\"").append(escapeJsonValue(htmlTitle)).append("\",");
        json.append("\"description\":\"").append(escapeJsonValue(htmlDescription)).append("\",");
        json.append("\"url\":\"").append(escapeJsonValue(canonicalAbsoluteUrl)).append("\"");
        if (!schemaImageAbsoluteUrl.isEmpty()) {
            json.append(",\"image\":\"")
                    .append(escapeJsonValue(schemaImageAbsoluteUrl))
                    .append("\"");
        }
        json.append(",\"step\":[");
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"@type\":\"HowToStep\",\"name\":\"")
                    .append(escapeJsonValue(steps.get(i)))
                    .append("\"}");
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private String buildVideoObjectJson(
            final String canonicalAbsoluteUrl,
            final String htmlTitle,
            final String htmlDescription,
            final String schemaImageAbsoluteUrl,
            final String firstYouTubeVideoId,
            final String organizationId,
            final Boolean schemaVideoEnabled,
            final String schemaVideoId) {
        final String videoIdToUse =
                (schemaVideoId != null && !schemaVideoId.trim().isEmpty())
                        ? schemaVideoId.trim()
                        : firstYouTubeVideoId;
        final boolean includeVideoObject =
                schemaVideoEnabled == null
                        ? videoIdToUse != null && !videoIdToUse.isEmpty()
                        : schemaVideoEnabled;
        if (!includeVideoObject || videoIdToUse == null || videoIdToUse.isEmpty()) {
            return "";
        }
        final String videoWatchUrl = "https://www.youtube.com/watch?v=" + videoIdToUse;
        final String embedUrl = "https://www.youtube.com/embed/" + videoIdToUse;

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"VideoObject\",");
        json.append("\"name\":\"").append(escapeJsonValue(htmlTitle)).append("\",");
        json.append("\"description\":\"").append(escapeJsonValue(htmlDescription)).append("\",");
        json.append("\"url\":\"").append(escapeJsonValue(canonicalAbsoluteUrl)).append("\",");
        json.append("\"contentUrl\":\"").append(escapeJsonValue(videoWatchUrl)).append("\",");
        json.append("\"embedUrl\":\"").append(escapeJsonValue(embedUrl)).append("\"");
        if (!schemaImageAbsoluteUrl.isEmpty()) {
            json.append(",\"thumbnailUrl\":\"")
                    .append(escapeJsonValue(schemaImageAbsoluteUrl))
                    .append("\"");
        }
        if (organizationId != null && !organizationId.isEmpty()) {
            json.append(",\"publisher\":{\"@id\":\"")
                    .append(escapeJsonValue(organizationId))
                    .append("\"}");
        }
        json.append("}");
        return json.toString();
    }

    private String humanizeSlug(final String slug) {
        return slug.replace("-", " ").trim();
    }

    private boolean isOpenApiUiBreadcrumbPath(final String contentPath) {
        return openApiUiToolSlug(contentPath).isPresent()
                || openApiUiToolAboutSlug(contentPath).isPresent();
    }

    private boolean isOnlineOpenApiUiToolPath(final String contentPath) {
        return contentPath != null
                && contentPath.startsWith("/tools/online-clients/")
                && openApiUiToolSlug(contentPath).isPresent();
    }

    private Optional<String> openApiUiToolSlug(final String contentPath) {
        if (contentPath == null) {
            return Optional.empty();
        }

        final String referencePrefix = "/reference/open-api-uis/";
        final String onlineClientPrefix = "/tools/online-clients/";
        final String slug;

        if (contentPath.startsWith(referencePrefix)) {
            slug = contentPath.substring(referencePrefix.length());
        } else if (contentPath.startsWith(onlineClientPrefix)) {
            slug = contentPath.substring(onlineClientPrefix.length());
        } else {
            return Optional.empty();
        }

        return switch (slug) {
            case "swagger", "openapi-explorer", "scalar", "stoplight", "zudoku", "redoc" ->
                    Optional.of(slug);
            default -> Optional.empty();
        };
    }

    private Optional<String> openApiUiToolAboutSlug(final String contentPath) {
        if (contentPath == null) {
            return Optional.empty();
        }

        final String onlineClientPrefix = "/tools/online-clients/";
        final String aboutSuffix = "/about";
        if (!contentPath.startsWith(onlineClientPrefix) || !contentPath.endsWith(aboutSuffix)) {
            return Optional.empty();
        }

        final String slug =
                contentPath.substring(
                        onlineClientPrefix.length(), contentPath.length() - aboutSuffix.length());
        return switch (slug) {
            case "swagger", "openapi-explorer", "scalar", "stoplight", "zudoku", "redoc" ->
                    Optional.of(slug);
            default -> Optional.empty();
        };
    }

    private String openApiUiToolBreadcrumbLabel(final String contentPath) {
        final String slug =
                openApiUiToolSlug(contentPath)
                        .or(() -> openApiUiToolAboutSlug(contentPath))
                        .orElse("");
        return switch (slug) {
            case "swagger" -> "Swagger";
            case "openapi-explorer" -> "OpenAPI Explorer UI";
            case "scalar" -> "Scalar";
            case "stoplight" -> "Stoplight Elements";
            case "zudoku" -> "Zudoku";
            case "redoc" -> "Redoc";
            default -> humanizeSlug(slug);
        };
    }

    private boolean isArticleLikeSchemaType(final String schemaTypeValue) {
        return "Article".equalsIgnoreCase(schemaTypeValue)
                || "BlogPosting".equalsIgnoreCase(schemaTypeValue);
    }

    private String extractYouTubeVideoId(final String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        Pattern macroPattern = Pattern.compile("youtube-embed key=\"([a-zA-Z0-9_-]+)\"");
        Matcher macroMatcher = macroPattern.matcher(line);
        if (macroMatcher.find()) {
            return macroMatcher.group(1);
        }

        Pattern watchPattern = Pattern.compile("youtube\\.com/watch\\?v=([a-zA-Z0-9_-]+)");
        Matcher watchMatcher = watchPattern.matcher(line);
        if (watchMatcher.find()) {
            return watchMatcher.group(1);
        }
        return "";
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

    private Properties loadPropertiesFromResource(final String resourcePath) {
        final Properties properties = new Properties();
        try (InputStream inputStream = getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            logger.warn("Could not load schema properties from {}", resourcePath, e);
        }
        return properties;
    }

    private String getSchemaAuthorDefaultName() {
        final String authorFromResource = schemaAuthorDefaults.getProperty("name", "").trim();
        if (!authorFromResource.isEmpty()) {
            return authorFromResource;
        }
        return DEFAULT_SCHEMA_AUTHOR_NAME;
    }

    private String getSchemaAuthorDefaultUrl() {
        return schemaAuthorDefaults.getProperty("url", "").trim();
    }

    private String getSchemaPublisherDefaultName() {
        final String publisherFromResource = schemaPublisherDefaults.getProperty("name", "").trim();
        if (!publisherFromResource.isEmpty()) {
            return publisherFromResource;
        }
        return DEFAULT_SCHEMA_PUBLISHER_NAME;
    }

    static String resolveDateModified(
            final String pageLastModified, final String pageDatePublished) {
        if (pageLastModified != null && !pageLastModified.trim().isEmpty()) {
            return pageLastModified.trim();
        }
        if (pageDatePublished != null && !pageDatePublished.trim().isEmpty()) {
            return pageDatePublished.trim();
        }
        return "";
    }

    private Boolean parseOptionalBoolean(final String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        final String value = rawValue.trim().toLowerCase();
        if (value.equals("true") || value.equals("yes") || value.equals("on")) {
            return true;
        }
        if (value.equals("false") || value.equals("no") || value.equals("off")) {
            return false;
        }
        return null;
    }

    private List<String> parseHowToSteps(final String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(rawValue.split("\\|\\|"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String insertArticleBylineAfterFirstHeading(
            final String renderedContent, final String articleBylineSnippet) {
        if (articleBylineSnippet == null || articleBylineSnippet.isEmpty()) {
            return renderedContent;
        }

        final int firstHeadingEnd = renderedContent.indexOf("</h1>");
        if (firstHeadingEnd < 0) {
            return articleBylineSnippet + renderedContent;
        }

        final int insertionPoint = firstHeadingEnd + "</h1>".length();
        return renderedContent.substring(0, insertionPoint)
                + "\n"
                + articleBylineSnippet
                + "\n"
                + renderedContent.substring(insertionPoint);
    }

    private String buildArticleBylineSnippet(
            final String contentFolder,
            final String contentPath,
            final String authorName,
            final String datePublished,
            final String dateModified,
            final String authorBioPath,
            final boolean indexTemplate) {
        final boolean isContentPage = "content".equalsIgnoreCase(contentFolder);
        final boolean isAuthorPage =
                contentPath != null && contentPath.equalsIgnoreCase(authorBioPath);
        if (!isContentPage || isAuthorPage || indexTemplate) {
            return "";
        }

        final String safeAuthorName = escapeHtmlAttribute(authorName);
        final String safeAuthorBioPath = escapeHtmlAttribute(authorBioPath);
        final String trimmedPublishedDate = datePublished == null ? "" : datePublished.trim();
        final String trimmedModifiedDate = dateModified == null ? "" : dateModified.trim();
        final String visibleDate =
                trimmedPublishedDate.isEmpty() ? trimmedModifiedDate : trimmedPublishedDate;
        final String dateLabel = trimmedPublishedDate.isEmpty() ? "Updated" : "Published";

        StringBuilder snippet = new StringBuilder();
        snippet.append("<p class='article-byline'>");
        snippet.append("<span>By <a href='")
                .append(safeAuthorBioPath)
                .append("' rel='author'>")
                .append(safeAuthorName)
                .append("</a></span>");
        if (!visibleDate.isEmpty()) {
            snippet.append("<span class='article-byline-separator' aria-hidden='true'>")
                    .append(" &middot; ")
                    .append("</span>");
            snippet.append("<span class='article-byline-date'>")
                    .append(dateLabel)
                    .append(" <time datetime='")
                    .append(escapeHtmlAttribute(visibleDate))
                    .append("'>")
                    .append(escapeHtmlAttribute(displayDateForArticleByline(visibleDate)))
                    .append("</time></span>");
        }
        snippet.append("</p>");
        return snippet.toString();
    }

    private String displayDateForArticleByline(final String dateValue) {
        final String trimmedDate = dateValue == null ? "" : dateValue.trim();
        final int dateTimeSeparator = trimmedDate.indexOf("T");
        if (dateTimeSeparator > 0) {
            return trimmedDate.substring(0, dateTimeSeparator);
        }
        return trimmedDate;
    }

    private String buildAuthorBioSnippet(
            final String contentFolder,
            final String contentPath,
            final String authorName,
            final String authorJobTitle,
            final String authorBioPath) {
        final boolean isContentPage = "content".equalsIgnoreCase(contentFolder);
        final boolean isAuthorPage =
                contentPath != null && contentPath.equalsIgnoreCase(authorBioPath);
        if (!isContentPage || isAuthorPage) {
            return "";
        }

        final String safeAuthorName = escapeHtmlAttribute(authorName);
        final String safeAuthorJobTitle = escapeHtmlAttribute(authorJobTitle);
        final String safeAuthorJobTitleWithPrefix =
                safeAuthorJobTitle.isEmpty() ? "" : ", " + safeAuthorJobTitle;
        final String safeAuthorBioPath = escapeHtmlAttribute(authorBioPath);

        try {
            return getResourceAsString(DEFAULT_AUTHOR_BIO_SNIPPET_RESOURCE)
                    .replace("{{AUTHOR_NAME}}", safeAuthorName)
                    .replace("{{AUTHOR_JOB_TITLE_WITH_PREFIX}}", safeAuthorJobTitleWithPrefix)
                    .replace("{{AUTHOR_BIO_PATH}}", safeAuthorBioPath);
        } catch (Exception e) {
            logger.warn(
                    "Could not load author snippet resource {}, using fallback html",
                    DEFAULT_AUTHOR_BIO_SNIPPET_RESOURCE,
                    e);
            return buildInlineAuthorBioSnippetFallback(
                    safeAuthorName, safeAuthorJobTitle, safeAuthorBioPath);
        }
    }

    private String buildInlineAuthorBioSnippetFallback(
            final String safeAuthorName,
            final String safeAuthorJobTitle,
            final String safeAuthorBioPath) {
        StringBuilder snippet = new StringBuilder();
        snippet.append("<aside class='author-bio-snippet' aria-label='About the author'>");
        snippet.append("<p><strong>Written by ").append(safeAuthorName).append("</strong>");
        if (!safeAuthorJobTitle.isEmpty()) {
            snippet.append(", ").append(safeAuthorJobTitle);
        }
        snippet.append(".</p>");
        snippet.append("<p><a href='")
                .append(safeAuthorBioPath)
                .append("'>Read the full author bio and credentials</a>")
                .append(".</p>");
        snippet.append("</aside>");
        return snippet.toString();
    }

    private String buildBlogCategorySnippet(final String contentPath, final String categoriesRaw) {
        if (contentPath == null
                || !contentPath.startsWith("/blog/")
                || categoriesRaw == null
                || categoriesRaw.trim().isEmpty()) {
            return "";
        }

        final List<String> categories =
                Arrays.stream(categoriesRaw.split("\\|\\|"))
                        .map(String::trim)
                        .filter(category -> !category.isEmpty())
                        .toList();
        if (categories.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<p class='blog-post-categories'>Categories: ");
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) {
                html.append(", ");
            }
            final String category = categories.get(i);
            html.append("<a href='/blog/categories/")
                    .append(escapeHtmlAttribute(BlogContentManager.categorySlug(category)))
                    .append("'>")
                    .append(escapeHtmlAttribute(category))
                    .append("</a>");
        }
        html.append("</p>");
        return html.toString();
    }

    private boolean isBlogContentPath(final String contentPath) {
        return contentPath != null
                && (contentPath.equals("/blog") || contentPath.startsWith("/blog/"));
    }

    private boolean isIndividualBlogPostPath(final String contentPath) {
        return contentPath != null
                && contentPath.startsWith("/blog/")
                && !contentPath.startsWith("/blog/categories/");
    }

    private Optional<String> primaryBlogCategory(final String categoriesRaw) {
        if (categoriesRaw == null || categoriesRaw.trim().isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(categoriesRaw.split("\\|\\|"))
                .map(String::trim)
                .filter(category -> !category.isEmpty())
                .findFirst();
    }

    private String buildNextChallengeCtaSnippet(
            final String contentFolder, final String contentPath, final String nextChallengePath) {
        if (!"content".equalsIgnoreCase(contentFolder) || contentPath == null) {
            return "";
        }
        if (!contentPath.startsWith("/apichallenges/solutions/")) {
            return "";
        }
        if (nextChallengePath == null || nextChallengePath.trim().isEmpty()) {
            return "";
        }

        final String trimmedPath = nextChallengePath.trim();
        final String ctaUrl = trimmedPath.startsWith("/") ? trimmedPath : "/" + trimmedPath;
        final String ctaLabel = "Try the next challenge walkthrough";

        try {
            return getResourceAsString(DEFAULT_NEXT_CHALLENGE_CTA_RESOURCE)
                    .replace("{{NEXT_URL}}", escapeHtmlAttribute(ctaUrl))
                    .replace("{{NEXT_LABEL}}", escapeHtmlAttribute(ctaLabel));
        } catch (Exception e) {
            logger.warn(
                    "Could not load next challenge cta resource {}, using fallback html",
                    DEFAULT_NEXT_CHALLENGE_CTA_RESOURCE,
                    e);
            return "<aside class='next-challenge-cta'><a class='next-challenge-cta-link' href='"
                    + escapeHtmlAttribute(ctaUrl)
                    + "'>"
                    + escapeHtmlAttribute(ctaLabel)
                    + "</a></aside>";
        }
    }

    private String buildConceptLearnedSnippet(
            final String contentFolder,
            final String contentPath,
            final String conceptsLearnedRaw,
            final String conceptSummary,
            final String conceptReferenceLabel,
            final String conceptReferenceUrl,
            final String conceptReferenceLabel2,
            final String conceptReferenceUrl2) {

        if (!"content".equalsIgnoreCase(contentFolder)
                || contentPath == null
                || !contentPath.startsWith("/apichallenges/solutions/")) {
            return "";
        }

        final List<String> concepts = parseDelimitedMetadata(conceptsLearnedRaw);
        final List<ConceptReference> references =
                conceptReferences(
                        conceptReferenceLabel,
                        conceptReferenceUrl,
                        conceptReferenceLabel2,
                        conceptReferenceUrl2);

        if (concepts.isEmpty()
                || conceptSummary == null
                || conceptSummary.trim().isEmpty()
                || references.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<aside class='concept-learned' aria-label='Concept learned'>");
        html.append("<p class='concept-learned-title'><strong>Concept learned</strong></p>");
        html.append("<p class='concept-learned-summary'>")
                .append(escapeHtmlAttribute(conceptSummary.trim()))
                .append("</p>");
        html.append("<ul class='concept-learned-tags'>");
        for (String concept : concepts) {
            html.append("<li>").append(escapeHtmlAttribute(concept)).append("</li>");
        }
        html.append("</ul>");
        html.append("<p class='concept-learned-links'>Reference tutorials: ");
        for (int index = 0; index < references.size(); index++) {
            if (index > 0) {
                html.append(" | ");
            }
            final ConceptReference reference = references.get(index);
            html.append("<a href='")
                    .append(escapeHtmlAttribute(reference.url))
                    .append("'>")
                    .append(escapeHtmlAttribute(reference.label))
                    .append("</a>");
        }
        html.append("</p>");
        html.append("</aside>");
        return html.toString();
    }

    private List<String> parseDelimitedMetadata(final String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(rawValue.split("\\|\\|"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private List<ConceptReference> conceptReferences(
            final String label, final String url, final String label2, final String url2) {
        final List<ConceptReference> references = new ArrayList<>();
        addConceptReference(references, label, url);
        addConceptReference(references, label2, url2);
        return references;
    }

    private void addConceptReference(
            final List<ConceptReference> references, final String label, final String url) {
        if (label == null || label.trim().isEmpty() || url == null || url.trim().isEmpty()) {
            return;
        }
        references.add(new ConceptReference(label.trim(), url.trim()));
    }

    private static class ConceptReference {
        private final String label;
        private final String url;

        private ConceptReference(final String label, final String url) {
            this.label = label;
            this.url = url;
        }
    }

    private InputStream getResourceAsStream(String fileName) {

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            logger.error("content file not found: " + fileName);
            return null;
        } else {
            return inputStream;
        }
    }
}
