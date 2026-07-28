package uk.co.compendiumdev.robodepot;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;

import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public final class RoboDepotHomePage {

    private final DefaultGUIHTML gui;

    public RoboDepotHomePage(final DefaultGUIHTML gui) {
        this.gui = gui;
    }

    public void configureRoutes() {
        get(
                "/",
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);
                    return page("RoboDepot", homeContent(), "/");
                });

        get(
                "/bugs",
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);
                    return page("RoboDepot Deliberate Bugs", bugsContent(), "/bugs");
                });
    }

    private String page(final String title, final String body, final String canonicalPath) {
        return gui.getPageStart(title, "", canonicalPath)
                + gui.getStartOfMainContentMarker()
                + body
                + gui.getEndOfMainContentMarker()
                + gui.getPageFooter()
                + gui.getPageEnd();
    }

    private String homeContent() {
        return """
                <section class="robodepot-hero">
                  <h1>RoboDepot</h1>
                  <p>A standalone warehouse robot API for API testing practice. It is public, in-memory, constrained to safe field values, and deliberately buggy by default.</p>
                </section>

                <section>
                  <h2>API Endpoints</h2>
                  <table>
                    <thead><tr><th>Endpoint</th><th>Purpose</th></tr></thead>
                    <tbody>
                      <tr><td><code>/robodepot/robotmodels</code></td><td>Read-only robot model catalog</td></tr>
                      <tr><td><code>/robodepot/skus</code></td><td>Read-only SKU catalog</td></tr>
                      <tr><td><code>/robodepot/zones</code></td><td>Create, read, update, and delete warehouse zones</td></tr>
                      <tr><td><code>/robodepot/robots</code></td><td>Create, read, update, and delete robots</td></tr>
                      <tr><td><code>/robodepot/jobs</code></td><td>Create, read, update, and delete robot jobs</td></tr>
                      <tr><td><code>/robodepot/stock</code></td><td>Create, read, update, and delete stock records</td></tr>
                    </tbody>
                  </table>
                  <p><a href="/robodepot/docs">Read the generated API docs</a>, <a href="/robodepot/docs/swagger-ui">open Swagger UI</a>, or <a href="/robodepot/gui/entities">inspect the data</a>.</p>
                </section>

                <section>
                  <h2>Expected Simulation Behaviour</h2>
                  <p>The API stores the factory state: robots, zones, jobs, stock, SKUs, and robot models. A hidden UI endpoint advances the simulation every 20 seconds at most.</p>
                  <table>
                    <thead><tr><th>Area</th><th>Expected rule</th></tr></thead>
                    <tbody>
                      <tr><td>Robot status</td><td><code>idle</code> and <code>assigned</code> robots can work. <code>charging</code>, <code>maintenance</code>, and <code>offline</code> robots should not be assigned new work.</td></tr>
                      <tr><td>Battery</td><td>Robots below 15 battery should not start work. Charging robots gain battery up to 100.</td></tr>
                      <tr><td>Job priority</td><td>Queued jobs should be selected by highest <code>priority</code> first, then oldest <code>createdTick</code>.</td></tr>
                      <tr><td>Job state</td><td>Queued jobs can become <code>in-progress</code>. In-progress jobs can become <code>completed</code> or <code>blocked</code>.</td></tr>
                      <tr><td>Payload matching</td><td><code>small-bin</code> jobs can use any payload class. <code>standard-tote</code> and <code>cold-chain</code> need standard or heavy robots. <code>oversize</code> needs heavy robots.</td></tr>
                      <tr><td>Zones</td><td>Closed zones should not accept new robots or stock. Robots move to the dropoff zone when a job completes.</td></tr>
                      <tr><td>Stock</td><td>Jobs complete only when matching usable stock exists in the pickup zone. Completion decrements existing stock.</td></tr>
                    </tbody>
                  </table>
                </section>

                <section>
                  <h2>Jobs And Movement Flow</h2>
                  <p>A job is a warehouse movement task. Pickup and dropoff are relationship roles, not <code>zoneType</code> enum values.</p>
                  <div class="robodepot-flowchart" aria-label="RoboDepot clean job and movement flow">
                    <div class="robodepot-flow-step"><strong>1. Queued job</strong><span>Job waits with <code>state=queued</code>, <code>priority</code>, <code>payloadType</code>, and pickup/dropoff zone relationships.</span></div>
                    <div class="robodepot-flow-arrow">-&gt;</div>
                    <div class="robodepot-flow-step"><strong>2. Tick assigns work</strong><span>Highest priority queued job gets a valid robot. The job becomes <code>in-progress</code>; the robot becomes <code>assigned</code>.</span></div>
                    <div class="robodepot-flow-arrow">-&gt;</div>
                    <div class="robodepot-flow-step"><strong>3. Later tick completes work</strong><span>If robot, zones, payload, battery, and stock rules pass, matching stock is decremented.</span></div>
                    <div class="robodepot-flow-arrow">-&gt;</div>
                    <div class="robodepot-flow-step"><strong>4. Robot moves</strong><span>The robot's zone relationship changes from its current zone to the job's dropoff zone.</span></div>
                    <div class="robodepot-flow-arrow">-&gt;</div>
                    <div class="robodepot-flow-step"><strong>5. Finished state</strong><span>The job becomes <code>completed</code>; the robot becomes <code>idle</code>. If a rule fails, the job becomes <code>blocked</code>.</span></div>
                  </div>
                </section>

                <section>
                  <h2>RoboDepot Live Map</h2>
                  <p>The warehouse map calls the hidden tick endpoint before refresh, then reloads every 30 seconds.</p>
                  <div class="robodepot-visualisation" data-refresh-seconds="30">
                    <div class="robodepot-visualisation-header">
                      <div>
                        <h3>RoboDepot Live Warehouse</h3>
                        <p id="robodepot-visualisation-status" class="robodepot-visualisation-status">Loading warehouse state...</p>
                      </div>
                      <button type="button" id="robodepot-refresh-now" class="robodepot-refresh-now">Refresh</button>
                    </div>
                    <canvas id="robodepot-canvas" width="960" height="540" aria-label="RoboDepot warehouse zones, robots, and jobs"></canvas>
                    <p id="robodepot-visualisation-summary" class="robodepot-visualisation-summary" aria-live="polite"></p>
                  </div>
                  <script src="/js/robodepot-visualisation.js" defer></script>
                </section>
                """;
    }

    private String bugsContent() {
        return """
                <h1>RoboDepot Deliberate Bugs</h1>
                <p>RoboDepot is deliberately buggy by default. Start with <code>-robodepotbugs=none</code> to switch to clean behaviour.</p>
                <p>The visualisation calls <code>POST /robodepot/tick-forward</code>. That endpoint accepts no query parameters, accepts no request body, and returns <code>429</code> with <code>Retry-After</code> if called more often than every 20 seconds.</p>
                <table>
                  <thead><tr><th>Bug</th><th>Symptom</th></tr></thead>
                  <tbody>
                    <tr><td><code>stock-put-increments</code></td><td><code>PUT /robodepot/stock/{id}</code> adds supplied quantity instead of replacing it.</td></tr>
                    <tr><td><code>valid-robot-color-yellow-rejected</code></td><td><code>yellow</code> is a valid robot colour but is rejected.</td></tr>
                    <tr><td><code>valid-job-state-cancelled-rejected</code></td><td><code>cancelled</code> is a valid job state but is rejected.</td></tr>
                    <tr><td><code>active-job-unassign-blocked</code></td><td>Unassigning an <code>in-progress</code> job from a robot is blocked.</td></tr>
                    <tr><td><code>held-stock-unlink-blocked</code></td><td>Unlinking <code>held</code> stock from a zone is blocked.</td></tr>
                    <tr><td><code>zone-capacity-off-by-one</code></td><td>Zone robot relationships reject one valid slot early.</td></tr>
                    <tr><td><code>closed-zone-accepts-robots</code></td><td>Closed zones accept robot relationships.</td></tr>
                    <tr><td><code>closed-zone-accepts-stock</code></td><td>Closed zones accept stock relationships.</td></tr>
                    <tr><td><code>fragile-frozen-stock-allowed</code></td><td>Fragile stock can be linked to frozen zones.</td></tr>
                    <tr><td><code>payload-mismatch-allowed</code></td><td>Robots can be assigned jobs they should not be able to carry.</td></tr>
                    <tr><td><code>priority-inverted</code></td><td>Low-priority queued jobs are assigned before high-priority jobs.</td></tr>
                    <tr><td><code>robot-status-stale</code></td><td>Robots can stay <code>assigned</code> after completing work.</td></tr>
                    <tr><td><code>stock-shortage-completes</code></td><td>Jobs can complete when stock is missing or depleted.</td></tr>
                    <tr><td><code>wrong-sku-adjusted</code></td><td>Cold-chain completion can decrement the wrong stock record.</td></tr>
                    <tr><td><code>damaged-stock-picked</code></td><td>Damaged stock can be used to complete work.</td></tr>
                    <tr><td><code>low-battery-robot-works</code></td><td>Robots below the battery threshold can be assigned/completed.</td></tr>
                    <tr><td><code>charging-robot-assigned-job</code></td><td>Charging robots can be assigned new work.</td></tr>
                    <tr><td><code>offline-robot-moves</code></td><td>Offline robots can be treated as usable by the simulation.</td></tr>
                  </tbody>
                </table>
                """;
    }
}
