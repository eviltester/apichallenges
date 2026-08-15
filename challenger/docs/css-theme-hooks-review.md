# CSS Theme Hooks Review

This branch adds a three-icon switcher and CSS-only themes for:

- Clean Developer Docs
- Learning Platform
- Dark Lab

The implementation intentionally avoids adding extra content wrappers, spans, or divs beyond the runtime switcher control. These recommendations describe markup hooks that would make the same visual concepts stronger and less dependent on broad CSS selectors.

## Recommended Hooks

1. Add page-level identity hooks.

   A `body` class or `data-page-type` would let themes distinguish the home page, learning pages, API docs, Swagger UI, API Challenges progress, data explorer, and practice-mode pages without inferring from headings or URLs.

   Suggested examples: `data-page-type="home"`, `data-page-type="docs"`, `data-page-type="challenge-progress"`, `data-page-type="learning"`, `data-page-type="practice-mode"`.

2. Add a semantic hero region for landing-style pages.

   The home page and practice-mode overview pages would look more modern if the opening H1, subtitle, intro text, and primary links were grouped.

   Suggested classes: `.page-hero`, `.page-kicker`, `.page-subtitle`, `.hero-actions`.

3. Mark primary and secondary actions.

   Important links are often plain anchors inside paragraphs or lists. CSS can guess, but it cannot reliably tell "start", "open docs", "download OpenAPI", and normal inline reference links apart.

   Suggested classes: `.button-primary`, `.button-secondary`, `.link-action`, `.download-link`.

4. Add section wrappers around major content blocks.

   Markdown-generated pages currently expose headings and content, but not complete sections. Wrapping each H2 section would allow the learning platform and card-based concepts to style sections cleanly.

   Suggested classes: `.content-section`, `.learning-section`, `.api-mode-section`, `.resources-section`.

5. Add hooks for the learning path and API mode groups.

   The home page list, API Simulator, Simple API, Buggy API, HTTP Mirror, and tooling sections would benefit from explicit structures instead of generic nested lists.

   Suggested classes: `.learning-path`, `.learning-step`, `.api-mode-list`, `.api-mode-card`, `.api-mode-title`, `.api-mode-description`.

6. Split generated API route text into method and path spans.

   Generated docs currently render route summaries as text like `GET /api/todos`. Console and dark-lab styles would be much better with method badges and path styling.

   Suggested classes: `.http-method`, `.method-get`, `.method-post`, `.method-put`, `.method-delete`, `.endpoint-path`, `.endpoint-summary`.

7. Add purpose classes for tables.

   Challenge status tables, schema tables, route tables, entity explorer tables, and relationship tables currently share generic table styling. Purpose classes would let themes tune density and emphasis by use case.

   Suggested classes: `.challenge-table`, `.schema-table`, `.route-table`, `.instance-table`, `.relationship-table`.

8. Replace inline layout styles with classes.

   Some generated content uses inline styles such as `style='clear:both'`. Moving these to classes would let each theme control spacing and layout consistently.

   Suggested classes: `.status-panel`, `.help-panel`, `.details-panel`, `.instance-as-list`.

9. Improve navigation state hooks.

   The custom navigation has `.dropped`, but the individual active link is not marked semantically. A shared current item hook would improve accessibility and styling.

   Suggested attributes/classes: `aria-current="page"`, `.is-current`, `.has-submenu`, `.submenu`.

10. Add sponsor banner placement hooks.

    The sponsor banner class appears at both the top and bottom. Placement hooks would allow a prominent top banner and subtler footer banner.

    Suggested examples: `.sponsor-banner`, `.sponsor-banner-top`, `.sponsor-banner-bottom`.

11. Add code/example context hooks.

    JSON and XML classes are useful, but request examples, response examples, curl commands, and sample payloads would benefit from explicit context.

    Suggested classes: `.api-example`, `.request-example`, `.response-example`, `.payload-example`, `.example-caption`.

12. Keep generated page headings inside `main` consistently.

    Some generated GUI pages place the H1 before the main landmark. Keeping the page title inside `main` would improve accessibility and make theme spacing more consistent.

## Highest Value Changes

If only a few hooks are added first, prioritize:

- `data-page-type` on `body`
- `.page-hero` around the opening title/subtitle/actions
- `.button-primary` and `.button-secondary` on action links
- `.http-method` and `.endpoint-path` in generated route documentation
- `aria-current="page"` on active navigation links
