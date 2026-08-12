package uk.co.compendiumdev.challenge.gui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import uk.co.compendiumdev.challenge.AssetVersion;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class BlogContentManager {

    private static final String BLOG_RESOURCE_PREFIX = "content/blog/";
    private static final String BLOG_INDEX_RESOURCE = "content/blog.md";
    private static final String BLOG_FEED_PATH = "/blog/feed.xml";
    private static final String BLOG_FEED_TITLE = "API Challenges Blog RSS";
    private static final String DEFAULT_AUTHOR = "Alan Richardson";
    private static final int RSS_ITEM_LIMIT = 5;
    private static final int POSTS_PER_PAGE = 15;

    private final List<BlogPost> posts;
    private final String canonicalHost;

    public BlogContentManager(final List<String> pathsToFileContent, final String canonicalHost) {
        this.canonicalHost = canonicalHost;
        this.posts =
                pathsToFileContent.stream()
                        .filter(BlogContentManager::isBlogPostResource)
                        .map(this::readBlogPost)
                        .flatMap(Optional::stream)
                        .sorted(Comparator.comparing(BlogPost::publishedInstant).reversed())
                        .toList();
    }

    public List<BlogPost> posts() {
        return posts;
    }

    public String rssDiscoveryLink() {
        return "<link rel=\"alternate\" type=\"application/rss+xml\" title=\""
                + BLOG_FEED_TITLE
                + "\" href=\""
                + BLOG_FEED_PATH
                + "\">";
    }

    public String renderBlogIndexHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<p class=\"blog-feed-link\"><a href=\"")
                .append(BLOG_FEED_PATH)
                .append("\">Subscribe to the API Challenges blog RSS feed</a>.</p>\n");
        html.append(
                "<p><a href=\"/blog/categories\">Browse blog categories</a> or <a href=\"/blog/all-posts\">view the all posts index</a>.</p>\n");
        html.append(renderPagedPostList(posts, 1, "/blog", "Blog posts"));
        return html.toString();
    }

    public String renderBlogPage(final DefaultGUIHTML guiManagement, final int pageNumber) {
        if (pageNumber < 2 || pageNumber > blogPageCount()) {
            return "";
        }

        final String title = "API Challenges Blog - Page " + pageNumber + " | API Challenges";
        final String canonicalUrl = canonicalHost + blogPagePath(pageNumber);
        StringBuilder html = new StringBuilder();
        html.append(
                guiManagement.getPageStart(
                        title,
                        contentPageHeader(
                                "Read older API Challenges blog posts about REST APIs, API testing practice, tutorials, tools, and site updates."),
                        canonicalUrl));
        html.append(guiManagement.getMenuAsHTML());
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append(
                BreadcrumbHtml.render(
                        List.of(
                                BreadcrumbHtml.link("Blog", "/blog"),
                                BreadcrumbHtml.current("page " + pageNumber))));
        html.append("<div class=\"main-text-content blog-index-page\">");
        html.append("<h1>API Challenges Blog - Page ").append(pageNumber).append("</h1>");
        html.append("<p>Older posts from the API Challenges blog archive.</p>");
        html.append(
                "<p><a href=\"/blog/categories\">Browse blog categories</a> or <a href=\"/blog/all-posts\">view the all posts index</a>.</p>");
        html.append(renderPagedPostList(posts, pageNumber, "/blog", "Blog posts"));
        html.append("</div>");
        html.append(guiManagement.getEndOfMainContentMarker());
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());
        return html.toString();
    }

    public String renderAllPostsIndexPage(final DefaultGUIHTML guiManagement) {
        final String title = "All Blog Posts | API Challenges";
        final String canonicalUrl = canonicalHost + "/blog/all-posts";
        StringBuilder html = new StringBuilder();
        html.append(
                guiManagement.getPageStart(
                        title,
                        contentPageHeader(
                                "Browse every API Challenges blog post in date order, including REST API tutorials, API testing updates, and change log posts."),
                        canonicalUrl));
        html.append(guiManagement.getMenuAsHTML());
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append(
                BreadcrumbHtml.render(
                        List.of(
                                BreadcrumbHtml.link("Blog", "/blog"),
                                BreadcrumbHtml.current("all posts index"))));
        html.append("<div class=\"main-text-content blog-all-posts-index\">");
        html.append("<h1>All Blog Posts</h1>");
        html.append("<p>Every API Challenges blog post, listed newest first.</p>");
        html.append("<ul class=\"blog-all-post-list\">");
        for (BlogPost post : posts) {
            html.append("<li><time datetime=\"")
                    .append(escapeHtmlAttribute(post.date()))
                    .append("\">")
                    .append(escapeHtmlAttribute(displayDate(post.date())))
                    .append("</time> <a href=\"")
                    .append(escapeHtmlAttribute(post.path()))
                    .append("\">")
                    .append(escapeHtmlAttribute(post.title()))
                    .append("</a></li>");
        }
        html.append("</ul>");
        html.append("</div>");
        html.append(guiManagement.getEndOfMainContentMarker());
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());
        return html.toString();
    }

    public String renderCategoryIndexPage(final DefaultGUIHTML guiManagement) {
        final String title = "Blog Categories | API Challenges";
        final String canonicalUrl = canonicalHost + "/blog/categories";
        StringBuilder html = new StringBuilder();
        html.append(
                guiManagement.getPageStart(
                        title,
                        contentPageHeader(
                                "Browse API Challenges blog categories for REST API tutorials, API testing updates, practice APIs, and tool guidance."),
                        canonicalUrl));
        html.append(guiManagement.getMenuAsHTML());
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append(
                BreadcrumbHtml.render(
                        List.of(
                                BreadcrumbHtml.link("Blog", "/blog"),
                                BreadcrumbHtml.current("categories"))));
        html.append("<div class=\"main-text-content blog-category-index\">");
        html.append("<h1>Blog Categories</h1>");
        html.append("<p>Use these categories to find API Challenges blog posts by topic.</p>");
        html.append("<ul class=\"blog-category-list\">");
        for (Map.Entry<String, List<BlogPost>> entry : postsByCategory().entrySet()) {
            html.append("<li><a href=\"/blog/categories/")
                    .append(escapeHtmlAttribute(categorySlug(entry.getKey())))
                    .append("\">")
                    .append(escapeHtmlAttribute(entry.getKey()))
                    .append("</a> <span>(")
                    .append(entry.getValue().size())
                    .append(")</span></li>");
        }
        html.append("</ul>");
        html.append("</div>");
        html.append(guiManagement.getEndOfMainContentMarker());
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());
        return html.toString();
    }

    public String renderCategoryPage(
            final DefaultGUIHTML guiManagement, final String categorySlug) {
        return renderCategoryPage(guiManagement, categorySlug, 1);
    }

    public String renderCategoryPage(
            final DefaultGUIHTML guiManagement, final String categorySlug, final int pageNumber) {
        final Optional<String> category = categoryLabelForSlug(categorySlug);
        if (category.isEmpty()) {
            return "";
        }

        final List<BlogPost> categoryPosts = postsByCategory().get(category.get());
        final int totalPages = pageCount(categoryPosts);
        if (pageNumber < 1 || pageNumber > totalPages) {
            return "";
        }
        final String title =
                category.get()
                        + " Blog Posts"
                        + (pageNumber > 1 ? " - Page " + pageNumber : "")
                        + " | API Challenges";
        final String canonicalUrl = canonicalHost + categoryPagePath(categorySlug, pageNumber);
        StringBuilder html = new StringBuilder();
        html.append(
                guiManagement.getPageStart(
                        title,
                        contentPageHeader(
                                "Read API Challenges blog posts about "
                                        + category.get()
                                        + ", with links to practical API testing tutorials and examples."),
                        canonicalUrl));
        html.append(guiManagement.getMenuAsHTML());
        html.append(guiManagement.getStartOfMainContentMarker());
        html.append(
                BreadcrumbHtml.render(
                        List.of(
                                BreadcrumbHtml.link("Blog", "/blog"),
                                BreadcrumbHtml.link("categories", "/blog/categories"),
                                BreadcrumbHtml.current(category.get()))));
        html.append("<div class=\"main-text-content blog-category-page\">");
        html.append("<h1>").append(escapeHtmlAttribute(category.get())).append(" Blog Posts");
        if (pageNumber > 1) {
            html.append(" - Page ").append(pageNumber);
        }
        html.append("</h1>");
        html.append("<p><a href=\"/blog/categories\">All blog categories</a></p>");
        html.append(
                renderPagedPostList(
                        categoryPosts,
                        pageNumber,
                        "/blog/categories/" + categorySlug,
                        category.get() + " blog posts"));
        html.append("</div>");
        html.append(guiManagement.getEndOfMainContentMarker());
        html.append(guiManagement.getPageFooter());
        html.append(guiManagement.getPageEnd());
        return html.toString();
    }

    public String renderRssFeed() {
        StringBuilder rss = new StringBuilder();
        rss.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        rss.append(
                "<rss version=\"2.0\" xmlns:content=\"http://purl.org/rss/1.0/modules/content/\">\n");
        rss.append("<channel>\n");
        rss.append("<title>API Challenges Blog</title>\n");
        rss.append("<link>").append(escapeXml(canonicalHost + "/blog")).append("</link>\n");
        rss.append(
                "<description>API testing tutorials, REST API practice updates, and API Challenges news.</description>\n");
        rss.append("<language>en-gb</language>\n");
        for (BlogPost post : posts.stream().limit(RSS_ITEM_LIMIT).toList()) {
            rss.append("<item>\n");
            rss.append("<title>").append(escapeXml(post.title())).append("</title>\n");
            rss.append("<link>").append(escapeXml(canonicalHost + post.path())).append("</link>\n");
            rss.append("<guid isPermaLink=\"true\">")
                    .append(escapeXml(canonicalHost + post.path()))
                    .append("</guid>\n");
            rss.append("<pubDate>")
                    .append(escapeXml(rfc1123Date(post.publishedInstant())))
                    .append("</pubDate>\n");
            rss.append("<author>")
                    .append(escapeXml("contact@eviltester.com (" + DEFAULT_AUTHOR + ")"))
                    .append("</author>\n");
            rss.append("<description>")
                    .append(escapeXml(post.description()))
                    .append("</description>\n");
            for (String category : post.categories()) {
                rss.append("<category>").append(escapeXml(category)).append("</category>\n");
            }
            rss.append("<content:encoded><![CDATA[")
                    .append(cdataSafe(renderPostBodyHtml(post)))
                    .append("]]></content:encoded>\n");
            rss.append("</item>\n");
        }
        rss.append("</channel>\n");
        rss.append("</rss>\n");
        return rss.toString();
    }

    public Set<String> categorySlugs() {
        return postsByCategory().keySet().stream()
                .map(BlogContentManager::categorySlug)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public int blogPageCount() {
        return pageCount(posts);
    }

    public int categoryPageCount(final String categorySlug) {
        final Optional<String> category = categoryLabelForSlug(categorySlug);
        if (category.isEmpty()) {
            return 0;
        }
        return pageCount(postsByCategory().get(category.get()));
    }

    public String blogPagePath(final int pageNumber) {
        return pageNumber <= 1 ? "/blog" : "/blog/page/" + pageNumber;
    }

    public String categoryPagePath(final String categorySlug, final int pageNumber) {
        final String categoryPath = "/blog/categories/" + categorySlug;
        return pageNumber <= 1 ? categoryPath : categoryPath + "/page/" + pageNumber;
    }

    public String categoryLastMod(final String categorySlug) {
        final Optional<String> category = categoryLabelForSlug(categorySlug);
        if (category.isEmpty()) {
            return latestLastMod();
        }
        return postsByCategory().get(category.get()).stream()
                .map(BlogPost::lastmod)
                .max(String::compareTo)
                .orElse(latestLastMod());
    }

    public String latestLastMod() {
        return posts.stream().map(BlogPost::lastmod).max(String::compareTo).orElse("2026-08-07");
    }

    public boolean hasCategory(final String categorySlug) {
        return categoryLabelForSlug(categorySlug).isPresent();
    }

    public String renderPostNavigationHtml(final String contentPath) {
        if (contentPath == null
                || contentPath.equals("/blog")
                || contentPath.startsWith("/blog/categories")) {
            return "";
        }

        int currentIndex = -1;
        for (int index = 0; index < posts.size(); index++) {
            if (posts.get(index).path().equals(contentPath)) {
                currentIndex = index;
                break;
            }
        }

        if (currentIndex < 0) {
            return "";
        }

        final BlogPost newerPost = currentIndex > 0 ? posts.get(currentIndex - 1) : null;
        final BlogPost olderPost =
                currentIndex < posts.size() - 1 ? posts.get(currentIndex + 1) : null;
        if (newerPost == null && olderPost == null) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<nav class='blog-post-navigation' aria-label='Blog post navigation'>");
        if (olderPost != null) {
            html.append("<div class='blog-post-nav-item blog-post-nav-previous'>")
                    .append("<span class='blog-post-nav-label'>Previous post</span>")
                    .append("<a rel='prev' href='")
                    .append(escapeHtmlAttribute(olderPost.path()))
                    .append("'>")
                    .append(escapeHtmlAttribute(olderPost.title()))
                    .append("</a>")
                    .append("</div>");
        }
        if (newerPost != null) {
            html.append("<div class='blog-post-nav-item blog-post-nav-next'>")
                    .append("<span class='blog-post-nav-label'>Next post</span>")
                    .append("<a rel='next' href='")
                    .append(escapeHtmlAttribute(newerPost.path()))
                    .append("'>")
                    .append(escapeHtmlAttribute(newerPost.title()))
                    .append("</a>")
                    .append("</div>");
        }
        html.append("</nav>");
        return html.toString();
    }

    private String contentPageHeader(final String description) {
        return "<meta name='description' content='"
                + escapeHtmlAttribute(description)
                + "'>"
                + "<meta name='robots' content='index,follow'>"
                + rssDiscoveryLink()
                + "<script src='"
                + AssetVersion.versionedPath("/js/externalize-links.js")
                + "'></script>";
    }

    private String renderPostListItem(final BlogPost post) {
        StringBuilder html = new StringBuilder();
        html.append("<article class=\"blog-list-item\">");
        html.append("<h2><a href=\"")
                .append(escapeHtmlAttribute(post.path()))
                .append("\">")
                .append(escapeHtmlAttribute(post.title()))
                .append("</a></h2>");
        html.append("<p class=\"blog-list-meta\"><time datetime=\"")
                .append(escapeHtmlAttribute(post.date()))
                .append("\">")
                .append(escapeHtmlAttribute(displayDate(post.date())))
                .append("</time></p>");
        html.append("<p>").append(escapeHtmlAttribute(post.description())).append("</p>");
        html.append(renderCategoryLinks(post.categories()));
        html.append("</article>\n");
        return html.toString();
    }

    private String renderPagedPostList(
            final List<BlogPost> postList,
            final int pageNumber,
            final String basePath,
            final String ariaLabel) {
        StringBuilder html = new StringBuilder();
        html.append("<section class=\"blog-post-list\" aria-label=\"")
                .append(escapeHtmlAttribute(ariaLabel))
                .append("\">\n");
        for (BlogPost post : postsForPage(postList, pageNumber)) {
            html.append(renderPostListItem(post));
        }
        html.append("</section>\n");
        html.append(renderPagination(basePath, pageNumber, pageCount(postList)));
        return html.toString();
    }

    private String renderPagination(
            final String basePath, final int currentPage, final int totalPages) {
        if (totalPages <= 1) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<nav class=\"blog-pagination\" aria-label=\"Blog pagination\">");
        if (currentPage > 1) {
            html.append("<a class=\"blog-pagination-previous\" rel=\"prev\" href=\"")
                    .append(escapeHtmlAttribute(pagePath(basePath, currentPage - 1)))
                    .append("\">Previous</a>");
        }
        html.append("<span class=\"blog-pagination-current\">Page ")
                .append(currentPage)
                .append(" of ")
                .append(totalPages)
                .append("</span>");
        for (int page = 1; page <= totalPages; page++) {
            if (page == currentPage) {
                html.append("<strong aria-current=\"page\">").append(page).append("</strong>");
            } else {
                html.append("<a href=\"")
                        .append(escapeHtmlAttribute(pagePath(basePath, page)))
                        .append("\">")
                        .append(page)
                        .append("</a>");
            }
        }
        if (currentPage < totalPages) {
            html.append("<a class=\"blog-pagination-next\" rel=\"next\" href=\"")
                    .append(escapeHtmlAttribute(pagePath(basePath, currentPage + 1)))
                    .append("\">Next</a>");
        }
        html.append("</nav>");
        return html.toString();
    }

    private List<BlogPost> postsForPage(final List<BlogPost> postList, final int pageNumber) {
        final int fromIndex = Math.max(0, (pageNumber - 1) * POSTS_PER_PAGE);
        if (fromIndex >= postList.size()) {
            return List.of();
        }
        return postList.subList(fromIndex, Math.min(fromIndex + POSTS_PER_PAGE, postList.size()));
    }

    private int pageCount(final List<BlogPost> postList) {
        if (postList.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) postList.size() / POSTS_PER_PAGE);
    }

    private String pagePath(final String basePath, final int pageNumber) {
        return pageNumber <= 1 ? basePath : basePath + "/page/" + pageNumber;
    }

    private String renderCategoryLinks(final List<String> categories) {
        if (categories.isEmpty()) {
            return "";
        }
        StringBuilder html = new StringBuilder();
        html.append("<p class=\"blog-categories\">Categories: ");
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) {
                html.append(", ");
            }
            final String category = categories.get(i);
            html.append("<a href=\"/blog/categories/")
                    .append(escapeHtmlAttribute(categorySlug(category)))
                    .append("\">")
                    .append(escapeHtmlAttribute(category))
                    .append("</a>");
        }
        html.append("</p>");
        return html.toString();
    }

    private Map<String, List<BlogPost>> postsByCategory() {
        Map<String, List<BlogPost>> categories = new LinkedHashMap<>();
        for (BlogPost post : posts) {
            for (String category : post.categories()) {
                categories.computeIfAbsent(category, key -> new ArrayList<>()).add(post);
            }
        }
        return categories;
    }

    private Optional<String> categoryLabelForSlug(final String categorySlug) {
        return postsByCategory().keySet().stream()
                .filter(category -> categorySlug(category).equals(categorySlug))
                .findFirst();
    }

    private Optional<BlogPost> readBlogPost(final String resourcePath) {
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                openResource(resourcePath), StandardCharsets.UTF_8))) {
            String line;
            boolean inHeader = false;
            boolean readingContent = false;
            Map<String, String> headers = new LinkedHashMap<>();
            StringBuilder body = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.equals("---") && !inHeader && !readingContent) {
                    inHeader = true;
                    continue;
                }
                if (line.equals("---") && inHeader) {
                    inHeader = false;
                    readingContent = true;
                    continue;
                }
                if (inHeader) {
                    final int separator = line.indexOf(":");
                    if (separator > -1) {
                        headers.put(
                                line.substring(0, separator).trim(),
                                line.substring(separator + 1).trim());
                    }
                    continue;
                }
                if (readingContent) {
                    body.append(line).append("\n");
                }
            }

            final String title = headers.getOrDefault("title", "").trim();
            final String date = headers.getOrDefault("date", headers.getOrDefault("lastmod", ""));
            final String lastmod = headers.getOrDefault("lastmod", displayDate(date));
            if (title.isEmpty() || date.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(
                    new BlogPost(
                            resourcePath,
                            resourcePath.replaceFirst("^content", "").replaceFirst("\\.md$", ""),
                            title,
                            headers.getOrDefault("description", "").trim(),
                            date.trim(),
                            lastmod.trim(),
                            parseList(headers.getOrDefault("categories", "")),
                            parseList(headers.getOrDefault("tags", "")),
                            parseDate(date),
                            body.toString()));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String renderPostBodyHtml(final BlogPost post) {
        final List<Extension> extensions = List.of(TablesExtension.create());
        final Parser parser = Parser.builder().extensions(extensions).build();
        final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        final Node document = parser.parse(processFeedMacros(post.bodyMarkdown()));
        return renderer.render(document);
    }

    private String processFeedMacros(final String markdown) {
        final Pattern youtubeMacro =
                Pattern.compile("\\{\\{<youtube-embed key=\"([a-zA-Z0-9_-]+)\" title=\"(.+?)\">}}");
        final Matcher matcher = youtubeMacro.matcher(markdown);
        StringBuffer processed = new StringBuffer();
        while (matcher.find()) {
            final String videoId = matcher.group(1);
            final String title = matcher.group(2);
            matcher.appendReplacement(
                    processed,
                    Matcher.quoteReplacement(
                            "<lite-youtube videoid=\""
                                    + escapeHtmlAttribute(videoId)
                                    + "\"><a class=\"lite-youtube-fallback\" href=\"https://www.youtube.com/watch?v="
                                    + escapeHtmlAttribute(videoId)
                                    + "\">Watch on YouTube: "
                                    + escapeHtmlAttribute(title)
                                    + "</a></lite-youtube>"));
        }
        matcher.appendTail(processed);
        return processed.toString();
    }

    private InputStream openResource(final String resourcePath) {
        final InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found " + resourcePath);
        }
        return inputStream;
    }

    private static boolean isBlogPostResource(final String resourcePath) {
        return resourcePath.startsWith(BLOG_RESOURCE_PREFIX)
                && resourcePath.endsWith(".md")
                && !BLOG_INDEX_RESOURCE.equals(resourcePath);
    }

    private static List<String> parseList(final String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return List.of();
        }
        return Pattern.compile("\\|\\|")
                .splitAsStream(rawValue)
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private static Instant parseDate(final String rawDate) {
        final String value = rawDate == null ? "" : rawDate.trim();
        if (value.isEmpty()) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeException ignored) {
            // try offset date-time
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeException ignored) {
            // try date only
        }
        try {
            return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (DateTimeException ignored) {
            return Instant.EPOCH;
        }
    }

    static String categorySlug(final String category) {
        final String normalized =
                Normalizer.normalize(category == null ? "" : category, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "");
        final String slug =
                normalized
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]+", "-")
                        .replaceAll("^-+", "")
                        .replaceAll("-+$", "");
        return slug.isEmpty() ? "uncategorized" : slug;
    }

    private static String displayDate(final String rawDate) {
        final String value = rawDate == null ? "" : rawDate.trim();
        final int dateTimeSeparator = value.indexOf("T");
        if (dateTimeSeparator > 0) {
            return value.substring(0, dateTimeSeparator);
        }
        return value;
    }

    private static String rfc1123Date(final Instant instant) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(
                ZonedDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    private static String cdataSafe(final String value) {
        return value == null ? "" : value.replace("]]>", "]]]]><![CDATA[>");
    }

    private static String escapeHtmlAttribute(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String escapeXml(final String value) {
        return escapeHtmlAttribute(value);
    }
}

record BlogPost(
        String resourcePath,
        String path,
        String title,
        String description,
        String date,
        String lastmod,
        List<String> categories,
        List<String> tags,
        Instant publishedInstant,
        String bodyMarkdown) {}
