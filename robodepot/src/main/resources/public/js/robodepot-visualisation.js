(function () {
  'use strict';

  const ZONES_PATH = '/robodepot/zones';
  const ROBOTS_PATH = '/robodepot/robots';
  const JOBS_PATH = '/robodepot/jobs';
  const TICK_PATH = '/robodepot/tick-forward';
  const DEFAULT_REFRESH_SECONDS = 30;
  const ROBOT_CARD_HEIGHT = 58;
  const ROBOT_CARD_GAP = 8;
  const JOB_PANEL_GAP = 12;
  const JOB_PANEL_MIN_HEIGHT = 118;
  const JOB_PANEL_HEADER_HEIGHT = 54;
  const JOB_ROW_HEIGHT = 36;
  const COLOR_MAP = {
    red: '#c93a3a',
    blue: '#2563eb',
    green: '#2f855a',
    yellow: '#d99a16',
    black: '#20242b',
    white: '#f8fafc',
  };
  const STATUS_ACCENTS = {
    idle: '#2f855a',
    charging: '#6d44b8',
    assigned: '#1f4e79',
    maintenance: '#b45309',
    offline: '#667085',
  };

  function onReady(callback) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', callback);
      return;
    }
    callback();
  }

  function fetchJson(path) {
    const headers = { Accept: 'application/json' };
    return fetch(path, { headers, cache: 'no-store' }).then((response) => {
      if (!response.ok) {
        throw new Error(`${path} returned ${response.status}`);
      }
      return response.json();
    });
  }

  function tickForward() {
    const headers = { Accept: 'application/json' };
    return fetch(TICK_PATH, {
      method: 'POST',
      headers,
      cache: 'no-store',
    }).then((response) => {
      if (response.status === 429) {
        return {
          throttled: true,
          retryAfter: response.headers.get('Retry-After') || '',
        };
      }
      if (!response.ok) {
        throw new Error(`${TICK_PATH} returned ${response.status}`);
      }
      return response.json();
    });
  }

  function asArray(value) {
    return Array.isArray(value) ? value : [];
  }

  function idOf(value) {
    if (value === null || value === undefined) {
      return '';
    }
    if (typeof value === 'object' && value.id !== undefined) {
      return String(value.id);
    }
    return String(value);
  }

  function relatedIds(source, relationshipName) {
    return asArray(source && source[relationshipName])
      .map(idOf)
      .filter((id) => id.length > 0);
  }

  function addUniqueRobot(robotsByZone, assignedRobotIds, zoneId, robot) {
    const zoneRobots = robotsByZone.get(zoneId);
    if (!zoneRobots || zoneRobots.some((candidate) => candidate.id === robot.id)) {
      return;
    }
    zoneRobots.push(robot);
    assignedRobotIds.add(robot.id);
  }

  function addUniqueId(collection, id) {
    if (id && !collection.includes(id)) {
      collection.push(id);
    }
  }

  function normaliseState(zonesPayload, robotsPayload, jobsPayload) {
    const zones = asArray(zonesPayload.zones).map((zone) => ({
      ...zone,
      id: idOf(zone),
    }));
    const robots = asArray(robotsPayload.robots).map((robot) => ({
      ...robot,
      id: idOf(robot),
      assignedJobIds: [],
    }));
    const jobs = asArray(jobsPayload.jobs).map((job) => ({
      ...job,
      id: idOf(job),
    }));

    const robotsById = new Map(robots.map((robot) => [robot.id, robot]));
    const robotsByZone = new Map(zones.map((zone) => [zone.id, []]));
    const assignedRobotIds = new Set();

    robots.forEach((robot) => {
      relatedIds(robot, 'jobs').forEach((jobId) => {
        addUniqueId(robot.assignedJobIds, jobId);
      });
    });

    jobs.forEach((job) => {
      relatedIds(job, 'robot').forEach((robotId) => {
        const robot = robotsById.get(robotId);
        if (robot) {
          addUniqueId(robot.assignedJobIds, job.id);
        }
      });
    });

    zones.forEach((zone) => {
      relatedIds(zone, 'robots').forEach((robotId) => {
        const robot = robotsById.get(robotId);
        if (robot) {
          addUniqueRobot(robotsByZone, assignedRobotIds, zone.id, robot);
        }
      });
    });

    robots.forEach((robot) => {
      relatedIds(robot, 'zone').forEach((zoneId) => {
        addUniqueRobot(robotsByZone, assignedRobotIds, zoneId, robot);
      });
    });

    return {
      zones,
      jobs,
      robotsByZone,
      unassignedRobots: robots.filter((robot) => !assignedRobotIds.has(robot.id)),
      robotCount: robots.length,
    };
  }

  function batteryColor(value) {
    const battery = Number(value);
    if (battery >= 66) {
      return '#2f855a';
    }
    if (battery >= 30) {
      return '#c47f00';
    }
    return '#c93a3a';
  }

  function robotFill(robot) {
    return COLOR_MAP[String(robot.color || '').toLowerCase()] || '#64748b';
  }

  function robotTextColor(robot) {
    const color = String(robot.color || '').toLowerCase();
    return color === 'white' || color === 'yellow' ? '#111827' : '#ffffff';
  }

  function compareIds(left, right) {
    const leftNumber = Number(left);
    const rightNumber = Number(right);
    if (!Number.isNaN(leftNumber) && !Number.isNaN(rightNumber)) {
      return leftNumber - rightNumber;
    }
    return String(left).localeCompare(String(right));
  }

  function jobIdsCsv(jobIds) {
    const uniqueJobIds = [];
    asArray(jobIds).forEach((jobId) => addUniqueId(uniqueJobIds, jobId));
    if (uniqueJobIds.length === 0) {
      return 'none';
    }
    return uniqueJobIds.sort(compareIds).join(', ');
  }

  function trimToWidth(context, text, maxWidth) {
    const value = String(text || '');
    if (context.measureText(value).width <= maxWidth) {
      return value;
    }
    let trimmed = value;
    while (trimmed.length > 1 && context.measureText(`${trimmed}...`).width > maxWidth) {
      trimmed = trimmed.slice(0, -1);
    }
    return `${trimmed}...`;
  }

  function roundedRect(context, x, y, width, height, radius) {
    const adjustedRadius = Math.min(radius, width / 2, height / 2);
    if (typeof context.roundRect === 'function') {
      context.roundRect(x, y, width, height, adjustedRadius);
      return;
    }
    context.moveTo(x + adjustedRadius, y);
    context.lineTo(x + width - adjustedRadius, y);
    context.quadraticCurveTo(x + width, y, x + width, y + adjustedRadius);
    context.lineTo(x + width, y + height - adjustedRadius);
    context.quadraticCurveTo(x + width, y + height, x + width - adjustedRadius, y + height);
    context.lineTo(x + adjustedRadius, y + height);
    context.quadraticCurveTo(x, y + height, x, y + height - adjustedRadius);
    context.lineTo(x, y + adjustedRadius);
    context.quadraticCurveTo(x, y, x + adjustedRadius, y);
  }

  function drawRobot(context, robot, x, y, width) {
    const battery = Math.max(0, Math.min(100, Number(robot.batteryLevel) || 0));
    const status = String(robot.status || 'unknown');
    const accent = STATUS_ACCENTS[status] || '#64748b';

    context.save();
    context.beginPath();
    roundedRect(context, x, y, width, ROBOT_CARD_HEIGHT, 6);
    context.fillStyle = '#ffffff';
    context.fill();
    context.strokeStyle = '#ccd6e0';
    context.stroke();

    context.beginPath();
    context.arc(x + 20, y + 22, 13, 0, Math.PI * 2);
    context.fillStyle = robotFill(robot);
    context.fill();
    context.lineWidth = 2;
    context.strokeStyle = accent;
    context.stroke();

    context.fillStyle = robotTextColor(robot);
    context.font = '700 11px Arial, sans-serif';
    context.textAlign = 'center';
    context.textBaseline = 'middle';
    context.fillText(`#${robot.id}`, x + 20, y + 22);

    const barX = x + 40;
    const barY = y + 8;
    const barWidth = Math.max(26, width - 50);
    context.fillStyle = '#e5eaf0';
    context.fillRect(barX, barY, barWidth, 7);
    context.fillStyle = batteryColor(battery);
    context.fillRect(barX, barY, barWidth * (battery / 100), 7);
    context.strokeStyle = '#9aa9b8';
    context.strokeRect(barX, barY, barWidth, 7);

    context.fillStyle = '#172033';
    context.font = '11px Arial, sans-serif';
    context.textAlign = 'left';
    context.textBaseline = 'alphabetic';
    context.fillText(trimToWidth(context, `${battery}% | ${status}`, barWidth), barX, y + 29);

    context.fillStyle = '#415065';
    context.fillText(
      trimToWidth(context, `jobs: ${jobIdsCsv(robot.assignedJobIds)}`, barWidth),
      barX,
      y + 47,
    );
    context.restore();
  }

  function zoneFill(zone) {
    if (zone.closed === true || zone.closed === 'true') {
      return '#eef0f2';
    }
    const temperatureBand = String(zone.temperatureBand || '').toLowerCase();
    if (temperatureBand === 'chilled') {
      return '#edf7fb';
    }
    if (temperatureBand === 'frozen') {
      return '#eef2ff';
    }
    return '#f8fbfd';
  }

  function drawZone(context, zone, robots, x, y, width, height) {
    context.save();
    context.beginPath();
    roundedRect(context, x, y, width, height, 8);
    context.fillStyle = zoneFill(zone);
    context.fill();
    context.strokeStyle = zone.closed === true || zone.closed === 'true' ? '#8a94a3' : '#9db4c7';
    context.lineWidth = 1.5;
    context.stroke();

    context.fillStyle = '#172033';
    context.font = '700 14px Arial, sans-serif';
    context.textAlign = 'left';
    context.textBaseline = 'alphabetic';
    context.fillText(trimToWidth(context, `Zone ${zone.id} - ${zone.zoneType || 'untyped'}`, width - 22), x + 12, y + 23);

    context.font = '11px Arial, sans-serif';
    context.fillStyle = '#415065';
    const zoneDetails = `cap ${zone.capacity || '-'} | ${zone.temperatureBand || 'ambient'}${zone.closed === true || zone.closed === 'true' ? ' | closed' : ''}`;
    context.fillText(trimToWidth(context, zoneDetails, width - 22), x + 12, y + 41);

    const robotAreaTop = y + 54;
    const robotWidth = Math.max(88, Math.min(126, (width - 32) / 2));
    const columns = Math.max(1, Math.floor((width - 20) / (robotWidth + 8)));
    const rows = Math.max(1, Math.floor((height - 66) / (ROBOT_CARD_HEIGHT + ROBOT_CARD_GAP)));
    const visibleRobotCount = Math.min(robots.length, columns * rows);

    for (let index = 0; index < visibleRobotCount; index += 1) {
      const column = index % columns;
      const row = Math.floor(index / columns);
      drawRobot(
        context,
        robots[index],
        x + 12 + column * (robotWidth + 8),
        robotAreaTop + row * (ROBOT_CARD_HEIGHT + ROBOT_CARD_GAP),
        robotWidth,
      );
    }

    if (robots.length > visibleRobotCount) {
      context.fillStyle = '#415065';
      context.font = '12px Arial, sans-serif';
      context.fillText(`+${robots.length - visibleRobotCount} more robots`, x + 12, y + height - 12);
    }
    context.restore();
  }

  function jobPanelColumnCount(width) {
    return Math.max(1, Math.floor((width - 24) / 180));
  }

  function calculateJobPanelHeight(width, jobCount) {
    const columns = jobPanelColumnCount(width);
    const rows = Math.max(1, Math.ceil(jobCount / columns));
    return Math.max(JOB_PANEL_MIN_HEIGHT, JOB_PANEL_HEADER_HEIGHT + rows * JOB_ROW_HEIGHT + 12);
  }

  function jobRelationshipSummary(job) {
    const robotId = relatedIds(job, 'robot')[0] || '-';
    const pickupZoneId = relatedIds(job, 'pickupZone')[0] || '-';
    const dropoffZoneId = relatedIds(job, 'dropoffZone')[0] || '-';
    return `p${job.priority || '-'} | ${job.payloadType || '-'} | r${robotId} | z${pickupZoneId}->z${dropoffZoneId}`;
  }

  function jobStatusColor(job) {
    const state = String(job.state || '').toLowerCase();
    if (state === 'completed') {
      return '#e8f5ec';
    }
    if (state === 'in-progress') {
      return '#edf4fb';
    }
    if (state === 'blocked') {
      return '#fff3e4';
    }
    if (state === 'cancelled') {
      return '#eef0f2';
    }
    return '#ffffff';
  }

  function drawJobsPanel(context, state, x, y, width, height) {
    context.save();
    context.beginPath();
    roundedRect(context, x, y, width, height, 8);
    context.fillStyle = '#ffffff';
    context.fill();
    context.strokeStyle = '#9db4c7';
    context.lineWidth = 1.5;
    context.stroke();

    context.fillStyle = '#172033';
    context.font = '700 14px Arial, sans-serif';
    context.textAlign = 'left';
    context.textBaseline = 'alphabetic';
    context.fillText('Jobs', x + 12, y + 23);

    context.font = '11px Arial, sans-serif';
    context.fillStyle = '#415065';
    context.fillText('id, status, priority, payload, robot, pickup->dropoff', x + 12, y + 41);

    const sortedJobs = [...state.jobs].sort((left, right) => compareIds(left.id, right.id));
    const columns = jobPanelColumnCount(width);
    const columnGap = 8;
    const itemWidth = (width - 24 - columnGap * (columns - 1)) / columns;

    for (let index = 0; index < sortedJobs.length; index += 1) {
      const job = sortedJobs[index];
      const column = index % columns;
      const row = Math.floor(index / columns);
      const itemX = x + 12 + column * (itemWidth + columnGap);
      const itemY = y + 50 + row * JOB_ROW_HEIGHT;

      context.beginPath();
      roundedRect(context, itemX, itemY, itemWidth, 29, 5);
      context.fillStyle = jobStatusColor(job);
      context.fill();
      context.strokeStyle = '#d1dae5';
      context.stroke();

      context.fillStyle = '#172033';
      context.font = '700 11px Arial, sans-serif';
      context.fillText(trimToWidth(context, `#${job.id}, ${job.state || 'unknown'}`, itemWidth - 10), itemX + 6, itemY + 12);
      context.font = '10px Arial, sans-serif';
      context.fillStyle = '#415065';
      context.fillText(trimToWidth(context, jobRelationshipSummary(job), itemWidth - 10), itemX + 6, itemY + 25);
    }
    context.restore();
  }

  function drawMessage(canvas, message) {
    const { context, width, height } = resizeCanvas(canvas);
    context.clearRect(0, 0, width, height);
    context.fillStyle = '#f8fbfd';
    context.fillRect(0, 0, width, height);
    context.fillStyle = '#172033';
    context.font = '16px Arial, sans-serif';
    context.textAlign = 'center';
    context.textBaseline = 'middle';
    context.fillText(message, width / 2, height / 2);
  }

  function resizeCanvas(canvas, preferredHeight) {
    const ratio = window.devicePixelRatio || 1;
    const cssWidth = Math.max(320, Math.floor(canvas.clientWidth || 960));
    const cssHeight = Math.max(420, Math.floor(preferredHeight || cssWidth * 0.72));
    const pixelWidth = Math.floor(cssWidth * ratio);
    const pixelHeight = Math.floor(cssHeight * ratio);
    if (canvas.width !== pixelWidth || canvas.height !== pixelHeight) {
      canvas.width = pixelWidth;
      canvas.height = pixelHeight;
    }
    const context = canvas.getContext('2d');
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
    return { context, width: cssWidth, height: cssHeight };
  }

  function drawState(canvas, state) {
    const displayWidth = Math.max(320, Math.floor(canvas.clientWidth || 960));
    const displayZones = state.unassignedRobots.length > 0
      ? state.zones.concat([{ id: 'open', zoneType: 'unassigned', capacity: '-', temperatureBand: 'ambient', closed: false }])
      : state.zones;
    const panelHeight = calculateJobPanelHeight(displayWidth, state.jobs.length);
    const { context, width, height } = resizeCanvas(
      canvas,
      Math.max(420, displayWidth * 0.72, panelHeight + 260),
    );

    context.clearRect(0, 0, width, height);
    context.fillStyle = '#f3f6f8';
    context.fillRect(0, 0, width, height);

    if (displayZones.length === 0) {
      drawMessage(canvas, 'No RoboDepot zones found');
      return;
    }

    const padding = 14;
    const gap = 12;
    const columns = width < 640 ? 1 : Math.ceil(Math.sqrt(displayZones.length));
    const rows = Math.ceil(displayZones.length / columns);
    const cellWidth = (width - padding * 2 - gap * (columns - 1)) / columns;
    const zoneAreaHeight = height - padding * 2 - panelHeight - JOB_PANEL_GAP;
    const cellHeight = (zoneAreaHeight - gap * (rows - 1)) / rows;

    displayZones.forEach((zone, index) => {
      const column = index % columns;
      const row = Math.floor(index / columns);
      const robots = zone.id === 'open' ? state.unassignedRobots : (state.robotsByZone.get(zone.id) || []);
      drawZone(
        context,
        zone,
        robots,
        padding + column * (cellWidth + gap),
        padding + row * (cellHeight + gap),
        cellWidth,
        cellHeight,
      );
    });

    drawJobsPanel(
      context,
      state,
      padding,
      height - padding - panelHeight,
      width - padding * 2,
      panelHeight,
    );
  }

  function updateSummary(summaryElement, state) {
    const lowBatteryCount = [
      ...state.robotsByZone.values(),
      state.unassignedRobots,
    ].flat().filter((robot) => Number(robot.batteryLevel) < 30).length;
    const closedZoneCount = state.zones.filter(
      (zone) => zone.closed === true || zone.closed === 'true',
    ).length;

    const parts = [
      `${state.robotCount} robots`,
      `${state.zones.length} zones`,
      `${state.jobs.length} jobs`,
    ];
    const assignedJobLinks = new Set();
    [...state.robotsByZone.values(), state.unassignedRobots]
      .flat()
      .forEach((robot) => robot.assignedJobIds.forEach((jobId) => assignedJobLinks.add(jobId)));
    if (assignedJobLinks.size > 0) {
      parts.push(`${assignedJobLinks.size} assigned job links`);
    }
    if (lowBatteryCount > 0) {
      parts.push(`${lowBatteryCount} low battery`);
    }
    if (closedZoneCount > 0) {
      parts.push(`${closedZoneCount} closed zones`);
    }
    summaryElement.textContent = parts.join(' | ');
  }

  function updateStatus(statusElement, message) {
    if (statusElement) {
      statusElement.textContent = message;
    }
  }

  async function refreshVisualisation(elements) {
    updateStatus(elements.status, 'Advancing warehouse state...');
    const tickResult = await tickForward();
    updateStatus(elements.status, 'Refreshing warehouse state...');
    const [zonesPayload, robotsPayload, jobsPayload] = await Promise.all([
      fetchJson(ZONES_PATH),
      fetchJson(ROBOTS_PATH),
      fetchJson(JOBS_PATH),
    ]);
    const state = normaliseState(zonesPayload, robotsPayload, jobsPayload);
    drawState(elements.canvas, state);
    updateSummary(elements.summary, state);
    const tickText = tickResult.throttled
      ? `tick waiting ${tickResult.retryAfter}s`
      : `tick ${tickResult.tick}`;
    updateStatus(elements.status, `Last refresh ${new Date().toLocaleTimeString()} | ${tickText}`);
  }

  function initialiseVisualisation(container) {
    const elements = {
      canvas: container.querySelector('#robodepot-canvas'),
      status: container.querySelector('#robodepot-visualisation-status'),
      summary: container.querySelector('#robodepot-visualisation-summary'),
      refreshButton: container.querySelector('#robodepot-refresh-now'),
    };

    if (!elements.canvas) {
      return;
    }

    const refreshSeconds = Number(container.dataset.refreshSeconds) || DEFAULT_REFRESH_SECONDS;
    const refresh = () => refreshVisualisation(elements).catch((error) => {
      updateStatus(elements.status, `RoboDepot visualisation error: ${error.message}`);
      drawMessage(elements.canvas, 'RoboDepot data could not be loaded');
    });

    if (elements.refreshButton) {
      elements.refreshButton.addEventListener('click', refresh);
    }
    window.addEventListener('resize', refresh);
    refresh();
    window.setInterval(refresh, refreshSeconds * 1000);
  }

  onReady(() => {
    document.querySelectorAll('.robodepot-visualisation').forEach(initialiseVisualisation);
  });
}());
