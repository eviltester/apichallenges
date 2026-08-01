function setCookie(cname,cvalue,exdays) {
  let expires="";
  if(exdays!=undefined){ // use undefined to create a session cookie
      const d = new Date();
      d.setTime(d.getTime() + (exdays*24*60*60*1000));
      expires = 'expires=' + d.toUTCString() + ";";
  }
  document.cookie = cname + '=' + cvalue + ';' + expires + 'path=/';
}

function getCookie(cname) {
  let name = cname + '=';
  let decodedCookie = decodeURIComponent(document.cookie);
  let ca = decodedCookie.split(';');
  for(let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) == ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return '';
}

function inputChallengeGuid(){
    var dialog = ensureChallengerGuidDialog();
    var input = document.getElementById('challenger-guid-input');
    var error = document.getElementById('challenger-guid-error');
    error.innerText = "";
    input.value = "";

    if(dialog.showModal){
        dialog.showModal();
    }else{
        dialog.setAttribute('open', 'open');
    }
    input.focus();
}

function ensureChallengerGuidDialog(){
    var dialog = document.getElementById('challenger-guid-dialog');
    if(dialog){
        return dialog;
    }

    ensureChallengerGuidDialogStyles();
    dialog = document.createElement('dialog');
    dialog.id = 'challenger-guid-dialog';
    dialog.innerHTML = `
        <form id='challenger-guid-form' method='dialog'>
            <h2>Input Challenger GUID</h2>
            <p><label for='challenger-guid-input'>Challenger GUID</label></p>
            <p><input id='challenger-guid-input' name='challenger-guid-input' type='text' autocomplete='off'></p>
            <p id='challenger-guid-error'></p>
            <p id='challenger-guid-actions'><button type='submit'>Use GUID</button> <button type='button' id='challenger-guid-cancel'>Cancel</button></p>
        </form>`;
    document.body.appendChild(dialog);

    document.getElementById('challenger-guid-form').addEventListener('submit', function(event){
        event.preventDefault();
        useInputChallengerGuid();
    });
    document.getElementById('challenger-guid-cancel').addEventListener('click', function(){
        closeChallengerGuidDialog();
    });

    return dialog;
}

function ensureChallengerGuidDialogStyles(){
    if(document.getElementById('challenger-guid-dialog-styles')){
        return;
    }

    var styles = document.createElement('style');
    styles.id = 'challenger-guid-dialog-styles';
    styles.innerText = `
        #challenger-guid-dialog {
            width: min(92vw, 36rem);
            max-width: 36rem;
            padding: 1.25rem;
        }
        #challenger-guid-dialog h2 {
            margin-top: 0;
            text-align: center;
        }
        #challenger-guid-input {
            box-sizing: border-box;
            font-size: 1.1rem;
            padding: 0.5rem;
            width: 100%;
        }
        #challenger-guid-actions {
            text-align: center;
        }
        #challenger-guid-actions button {
            font-size: 2rem;
            margin: 0.25rem;
            padding: 1rem 1.5rem;
        }`;
    document.head.appendChild(styles);
}

function useInputChallengerGuid(){
    var input = document.getElementById('challenger-guid-input');
    var error = document.getElementById('challenger-guid-error');
    var guid = sanitizedChallengerGuid(input.value);

    if(!guid){
        error.innerText = "Enter a Challenger GUID.";
        input.focus();
        return;
    }

    location.href=`/gui/challenges/`+encodeURIComponent(guid);
}

function closeChallengerGuidDialog(){
    var dialog = document.getElementById('challenger-guid-dialog');
    if(dialog && dialog.close){
        dialog.close();
    }else if(dialog){
        dialog.removeAttribute('open');
    }
}

const SINGLE_PLAYER_CHALLENGER_GUID = "rest-api-challenges-single-player";
const PREVIOUS_CHALLENGER_GUIDS_MAX = 5;
const ACHIEVEMENT_DEFINITIONS = [
    {
        title: "A New Challenger",
        icon: "ID",
        tier: "new",
        condition: "Created an X-CHALLENGER session",
        challengeKey: "CREATE_NEW_CHALLENGER"
    },
    {
        title: "You Got This",
        icon: "GET",
        tier: "first",
        condition: "Passed GET /challenges (200)",
        challengeKey: "GET_CHALLENGES"
    },
    {
        title: "In the Race",
        icon: "10",
        tier: "race",
        condition: "10 challenges passed",
        threshold: 10,
        reward: "Reward Unlocked: Server session storage"
    },
    {
        title: "Moving on",
        icon: "20",
        tier: "bronze",
        condition: "Pass 20 challenges",
        threshold: 20
    },
    {
        title: "Dedicated Player",
        icon: "30",
        tier: "silver",
        condition: "Pass 30 challenges",
        threshold: 30
    },
    {
        title: "Better than the Rest",
        icon: "40",
        tier: "gold",
        condition: "Pass 40 challenges",
        threshold: 40
    },
    {
        title: "Among the Best",
        icon: "50",
        tier: "platinum",
        condition: "Pass 50 challenges",
        threshold: 50
    },
    {
        title: "Better than the Best",
        icon: "60",
        tier: "elite",
        condition: "Pass 60 challenges",
        threshold: 60
    },
    {
        title: "Clearance Granted",
        icon: "AUTH",
        tier: "clearance",
        condition: "Complete Authentication and Authorization challenges 71-80",
        challengeKeys: [
            "CREATE_SECRET_TOKEN_401",
            "CREATE_SECRET_TOKEN_201",
            "GET_SECRET_NOTE_403",
            "GET_SECRET_NOTE_401",
            "GET_SECRET_NOTE_200",
            "POST_SECRET_NOTE_200",
            "POST_SECRET_NOTE_401",
            "POST_SECRET_NOTE_403",
            "GET_SECRET_NOTE_BEARER_200",
            "POST_SECRET_NOTE_BEARER_200"
        ]
    },
    {
        title: "Misc Mastery",
        icon: "MISC",
        tier: "final",
        condition: "Complete Miscellaneous challenges 81 and 82",
        challengeKeys: [
            "DELETE_ALL_TODOS",
            "POST_ALL_TODOS"
        ]
    },
    {
        title: "Completist",
        icon: "ALL",
        tier: "complete",
        condition: "Pass every challenge",
        allChallenges: true
    }
];

function forgetGuid(aguid){
    if(isProtectedSinglePlayerGuid(aguid)){
        return;
    }

    var guids = localStorage.getItem('challenges-guids') || '';
    guids = guids.replace(`|${aguid}|`, '');
    localStorage.setItem('challenges-guids', guids);
    localStorage.removeItem(`${aguid}.data`);
    localStorage.removeItem(`${aguid}.progress`);
    document.getElementById('p'+aguid).remove();
    if(getCookie('X-THINGIFIER-DATABASE-NAME')== aguid){
        setCookie('X-THINGIFIER-DATABASE-NAME','',0);
    }
    if(getCookie('X-CHALLENGER')== aguid){
        setCookie('X-CHALLENGER','',0);
    }
}

function sanitizedChallengerGuid(guid){
    return (guid || '').replace(/[^\-a-zA-Z0-9]/g,'');
}

function isProtectedSinglePlayerGuid(guid){
    return sanitizedChallengerGuid(guid)===SINGLE_PLAYER_CHALLENGER_GUID;
}

function isUuidChallengerGuid(guid){
    return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/.test(sanitizedChallengerGuid(guid));
}

function challengerGuidFromLocation(){
    var parts = location.pathname.split("/");
    return sanitizedChallengerGuid(parts[parts.length-1]);
}

function addGuidToArray(guidsArray, guid){
    var sanitizedGuid = sanitizedChallengerGuid(guid);
    if(sanitizedGuid && !guidsArray.includes(`|${sanitizedGuid}|`)){
        guidsArray.push(`|${sanitizedGuid}|`);
    }
}

function hasLocalSavedProgress(guid){
    var sanitizedGuid = sanitizedChallengerGuid(guid);
    return sanitizedGuid && localStorage.getItem(`${sanitizedGuid}.progress`)!==null;
}

function hasLocalSavedTodos(guid){
    var sanitizedGuid = sanitizedChallengerGuid(guid);
    return sanitizedGuid && localStorage.getItem(`${sanitizedGuid}.data`)!==null;
}

function removeSavedGuidData(guid){
    var sanitizedGuid = sanitizedChallengerGuid(guid);
    if(sanitizedGuid && !isProtectedSinglePlayerGuid(sanitizedGuid)){
        localStorage.removeItem(`${sanitizedGuid}.data`);
        localStorage.removeItem(`${sanitizedGuid}.progress`);
    }
}

function canRestoreGuid(guid){
    return isUuidChallengerGuid(guid) && hasLocalSavedProgress(guid);
}

function shouldKeepGuidInPreviousList(guid){
    return isProtectedSinglePlayerGuid(guid) || canRestoreGuid(guid);
}

function cleanUnrestorableGuids(guidsArray){
    var rememberedGuids = [];

    for(var guidItem in guidsArray){
        var myguid = sanitizedChallengerGuid(guidsArray[guidItem].replace(/\|/g,''));
        if(!myguid || rememberedGuids.includes(myguid)){
            continue;
        }

        if(shouldKeepGuidInPreviousList(myguid)){
            rememberedGuids.push(myguid);
        }else{
            removeSavedGuidData(myguid);
        }
    }

    localStorage.setItem('challenges-guids', rememberedGuids.map(guid => `|${guid}|`).join(''));
    return rememberedGuids.map(guid => `|${guid}|`);
}

function capPreviousGuidArray(guidsArray){
    var allGuids = [];
    var protectedGuids = [];
    var normalGuids = [];

    for(var guidItem in guidsArray){
        var myguid = sanitizedChallengerGuid(guidsArray[guidItem].replace(/\|/g,''));
        if(!myguid || allGuids.includes(myguid)){
            continue;
        }
        allGuids.push(myguid);
        if(isProtectedSinglePlayerGuid(myguid)){
            protectedGuids.push(myguid);
        }else{
            normalGuids.push(myguid);
        }
    }

    var maxNormalGuids = Math.max(PREVIOUS_CHALLENGER_GUIDS_MAX - protectedGuids.length, 0);
    var keptNormalGuids = normalGuids.slice(Math.max(normalGuids.length - maxNormalGuids, 0));
    var cappedGuids = [];

    for(var guidIndex in allGuids){
        var guid = allGuids[guidIndex];
        if(isProtectedSinglePlayerGuid(guid) || keptNormalGuids.includes(guid)){
            cappedGuids.push(guid);
        }else{
            removeSavedGuidData(guid);
        }
    }

    localStorage.setItem('challenges-guids', cappedGuids.map(guid => `|${guid}|`).join(''));
    return cappedGuids.map(guid => `|${guid}|`);
}

function displayLocalGuids(){
    var guids = localStorage.getItem('challenges-guids') || '';
    var guidsArray = cleanUnrestorableGuids(guids.match(/\|([^|]*)\|/g) || []);
    var currGuid = getCookie('X-CHALLENGER') || getCookie('X-THINGIFIER-DATABASE-NAME');
    if(shouldKeepGuidInPreviousList(currGuid)){
        addGuidToArray(guidsArray, currGuid);
    }
    var urlGuid = challengerGuidFromLocation();
    if(shouldKeepGuidInPreviousList(urlGuid)){
        addGuidToArray(guidsArray, urlGuid);
    }
    guidsArray = capPreviousGuidArray(guidsArray);
    document.writeln('<details><summary>Manage Challenger GUIDs</summary>');
    document.writeln("<p><button onclick=inputChallengeGuid()>Input a Challenger GUID to use</button></p>");

    if(guidsArray.length>0){
        for(var guidItem in guidsArray){
            var myguid = sanitizedChallengerGuid(guidsArray[guidItem].replace(/\|/g,''));
            if(!myguid){
                continue;
            }
            document.writeln("<p id='p" + myguid + "'>");
            document.writeln("<a href='/gui/challenges/"+myguid+"'>"+myguid+"</a>");
            if(canRestoreGuid(myguid)){
                document.writeln(`&nbsp;<button onclick="restoreLocalChallenger('${myguid}', this)">restore</button>`);
            }else if(isProtectedSinglePlayerGuid(myguid)){
                document.writeln("&nbsp;<button disabled title='Single player is kept but cannot be restored here'>restore unavailable</button>");
            }else if(hasLocalSavedTodos(myguid)){
                document.writeln("&nbsp;<button disabled title='Saved todos need saved progress to restore'>restore unavailable</button>");
            }
            if(!isProtectedSinglePlayerGuid(myguid)){
                document.writeln("&nbsp;<button onclick=forgetGuid('"+myguid+"')>forget</button>");
            }
            document.writeln(`<br><span id='${restoreStatusElementId(myguid)}'></span>`);
            document.writeln("</p>");
        }
    }else{
        document.writeln("<p>No locally restorable challenger GUIDs saved.</p>");
    }
    document.writeln('</details>');
}

function setCookieSaveLocally(){
    setCookie("auto-save-x-challenger-locally","true");
    const challengerData = document.challengerData;
    if(challengerData && challengerData.xChallenger){
        var currentChallengerGuid = challengerData.xChallenger;
        setCookie("last-auto-saved-x-challenger", currentChallengerGuid);
    }
}

function deleteCookieSaveLocally(){
    setCookie("auto-save-x-challenger-locally","")
}

function escapeHtml(value){
    return String(value || "").replace(/[&<>"']/g, function(character){
        return {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            "\"": "&quot;",
            "'": "&#39;"
        }[character];
    });
}

function challengeStatuses(challengerData){
    return (challengerData && challengerData.challengeStatus) ? challengerData.challengeStatus : {};
}

function completedChallengeCount(challengeStatus){
    return Object.values(challengeStatus).filter(status => status===true).length;
}

function totalChallengeCount(challengeStatus){
    return Object.values(challengeStatus).length;
}

const CHALLENGE_PROGRESS_REFRESH_INTERVAL_MS = 60000;
const CHALLENGE_PROGRESS_AUTO_MONITOR_MS = 30 * 60 * 1000;
var challengeProgressAutoMonitorIntervalId = null;
var challengeProgressAutoMonitorTimeoutId = null;

function areChallengeKeysComplete(challengeStatus, challengeKeys){
    return Array.isArray(challengeKeys) &&
        challengeKeys.length>0 &&
        challengeKeys.every(challengeKey => challengeStatus[challengeKey]===true);
}

function isAchievementUnlocked(definition, challengeStatus, doneCount, totalCount){
    if(definition.challengeKey){
        return challengeStatus[definition.challengeKey]===true;
    }
    if(definition.challengeKeys){
        return areChallengeKeysComplete(challengeStatus, definition.challengeKeys);
    }
    if(definition.allChallenges){
        return totalCount>0 && doneCount>=totalCount;
    }
    return doneCount>=definition.threshold;
}

function achievementsForChallenger(challengerData){
    const status = challengeStatuses(challengerData);
    const doneCount = completedChallengeCount(status);
    const totalCount = totalChallengeCount(status);
    const achievements = ACHIEVEMENT_DEFINITIONS.map(function(definition){
        return Object.assign({}, definition, {
            unlocked: isAchievementUnlocked(definition, status, doneCount, totalCount)
        });
    });
    const nextIndex = achievements.findIndex(achievement => !achievement.unlocked);
    return achievements.map(function(achievement, index){
        const state = achievement.unlocked ? "Unlocked" : (index===nextIndex ? "Next" : "Locked");
        return Object.assign({}, achievement, {
            state,
            next: index===nextIndex
        });
    });
}

function selectedAchievementIndex(achievements){
    const unlockedIndexes = achievements
        .map((achievement, index) => achievement.unlocked ? index : -1)
        .filter(index => index>=0);
    if(unlockedIndexes.length>0){
        return unlockedIndexes[unlockedIndexes.length-1];
    }
    return achievements.findIndex(achievement => achievement.next);
}

function achievementDetailHtml(achievement){
    const reward = (achievement.reward && achievement.unlocked) ? achievement.reward : "";
    return `<span class='achievement-detail-status'>${escapeHtml(achievement.state)}</span>` +
        `<div><h3 class='achievement-detail-title'>${escapeHtml(achievement.title)}</h3>` +
        `<p class='achievement-detail-condition'>${escapeHtml(achievement.condition)}</p></div>` +
        `<div class='achievement-detail-reward'>${escapeHtml(reward)}</div>`;
}

function achievementMedalHtml(achievement, index, selectedIndex){
    const classes = [
        "achievement-medal",
        achievement.unlocked ? "" : "is-locked",
        achievement.next ? "is-next" : "",
        index===selectedIndex ? "is-selected" : ""
    ].filter(Boolean).join(" ");
    const label = `${achievement.title}, ${achievement.state.toLowerCase()}`;
    const title = `${achievement.title}: ${achievement.condition}`;
    return `<button type='button' class='${classes}' data-achievement-index='${index}' ` +
        `data-tier='${escapeHtml(achievement.tier)}' aria-label='${escapeHtml(label)}' ` +
        `aria-pressed='${index===selectedIndex}' title='${escapeHtml(title)}'>` +
        `${escapeHtml(achievement.icon)}</button>`;
}

function selectAchievementMedal(panel, medal){
    panel.dataset.selectedAchievementIndex = medal.dataset.achievementIndex;
    panel.querySelectorAll(".achievement-medal").forEach(function(item){
        item.classList.remove("is-selected");
        item.setAttribute("aria-pressed", "false");
    });
    medal.classList.add("is-selected");
    medal.setAttribute("aria-pressed", "true");
}

function setSelectedAchievementDetails(panel){
    const selectedIndex = panel.dataset.selectedAchievementIndex;
    const medal = panel.querySelector(`.achievement-medal[data-achievement-index='${selectedIndex}']`);
    if(medal){
        setAchievementDetails(medal, false);
    }
}

function setAchievementDetails(medal, selectMedal){
    const panel = medal.closest(".achievement-rail-panel");
    const detail = panel ? panel.querySelector(".achievement-detail") : null;
    if(!panel || !detail){
        return;
    }
    const achievements = achievementsForChallenger(document.challengerData);
    const index = parseInt(medal.dataset.achievementIndex, 10);
    const achievement = achievements[index];
    if(!achievement){
        return;
    }
    if(selectMedal){
        selectAchievementMedal(panel, medal);
    }
    detail.innerHTML = achievementDetailHtml(achievement);
}

function initialiseAchievementRail(){
    document.querySelectorAll(".achievement-rail-panel").forEach(function(panel){
        panel.querySelectorAll(".achievement-medal").forEach(function(medal){
            medal.addEventListener("mouseenter", function(){ setAchievementDetails(medal, false); });
            medal.addEventListener("mouseleave", function(){ setSelectedAchievementDetails(panel); });
            medal.addEventListener("focus", function(){ setAchievementDetails(medal, false); });
            medal.addEventListener("blur", function(){ setSelectedAchievementDetails(panel); });
            medal.addEventListener("click", function(){ setAchievementDetails(medal, true); });
        });
    });
}

function achievementRailHtml(challengerData){
    const achievements = achievementsForChallenger(challengerData);
    const unlockedCount = achievements.filter(achievement => achievement.unlocked).length;
    const selectedIndex = selectedAchievementIndex(achievements);
    const selectedAchievement = achievements[selectedIndex] || achievements[0];

    return `<section class='achievement-rail-panel' aria-label='Achievements' data-selected-achievement-index='${selectedIndex}'>` +
        "<div class='achievement-rail-header'>" +
        "<h2 class='achievement-rail-title'>Achievements</h2>" +
        `<div class='achievement-rail-meta'>${unlockedCount} of ${achievements.length} unlocked</div>` +
        "</div>" +
        "<div class='achievement-rail-track' role='list' aria-label='Achievement medals'>" +
        achievements.map(function(achievement, index){
            return achievementMedalHtml(achievement, index, selectedIndex);
        }).join("") +
        "</div>" +
        "<div class='achievement-detail' aria-live='polite'>" +
        achievementDetailHtml(selectedAchievement) +
        "</div>" +
        "</section>";
}

function showAchievements(){
    document.writeln(achievementRailHtml(document.challengerData));
    setTimeout(initialiseAchievementRail, 0);
}

function updateAchievements(){
    const panel = document.querySelector(".achievement-rail-panel");
    if(panel){
        panel.outerHTML = achievementRailHtml(document.challengerData);
        initialiseAchievementRail();
    }
}

function autoSaveLocallyHtml(challengerData, databaseData){
    var lastAutoSavedGuid = getCookie("last-auto-saved-x-challenger");
    var currentChallengerGuid = "";
    var canAutoSaveChallenger = false;
    if(challengerData && challengerData.xChallenger){
        currentChallengerGuid = challengerData.xChallenger;
        if(currentChallengerGuid!="" && currentChallengerGuid===lastAutoSavedGuid){
            canAutoSaveChallenger=true;
        }
    }

    var autoSave = false;
    if(getCookie("auto-save-x-challenger-locally")!='' && canAutoSaveChallenger){
        autoSave=true;
    }

    var autoSaveOn = autoSave ? "checked" : "";
    var autoSaveOnChange = autoSave ? "deleteCookieSaveLocally();location.reload();" : "setCookieSaveLocally();location.reload();";
    return ` <input type='checkbox' id='auto-save-locally-check' name='auto-save-locally-check' value='auto-save-locally-check' ${autoSaveOn} onchange="${autoSaveOnChange}"><label for='auto-save-locally-check'> Auto Save Todos and Progress Locally on refresh</label>`;
}

function autoMonitorHtml(challengerData){
    if(!(challengerData && challengerData.xChallenger)){
        return "";
    }
    var checked = isChallengeProgressAutoMonitorActive() ? "checked" : "";
    return ` <input type='checkbox' id='auto-monitor-challenge-progress' name='auto-monitor-challenge-progress' value='auto-monitor-challenge-progress' ${checked}><label for='auto-monitor-challenge-progress'> Auto monitor challenge progress for 30 minutes</label>`;
}

function challengeProgressRefreshControlsHtml(challengerData, databaseData){
    return `<p><button type='button' id='refresh-challenge-status'>Refresh Status</button>` +
        autoSaveLocallyHtml(challengerData, databaseData) +
        autoMonitorHtml(challengerData) +
        ` <span id='challenge-progress-refresh-message' class='challenge-progress-refresh-message' role='status' aria-live='polite'></span></p>`;
}

function currentStatusHtml(challengerData, databaseData){
    var html = "<div>";
    html += challengeProgressRefreshControlsHtml(challengerData, databaseData);

    if(challengerData && challengerData.xChallenger){
        var xChallengerGuid = challengerData.xChallenger;
        sessionStorage.removeItem(autoRestoreGuardKey(xChallengerGuid));

        if(challengerData.challengeStatus){
            var doneCount = completedChallengeCount(challengerData.challengeStatus);
            var totalCount = totalChallengeCount(challengerData.challengeStatus);
            var percentComplete = totalCount===0 ? 0 : Math.round((doneCount/totalCount)*100);
            var todoCount = (databaseData && databaseData.todos) ? databaseData.todos.length : 0;

            if(getCookie("auto-save-x-challenger-locally")!='' &&
                    xChallengerGuid===getCookie("last-auto-saved-x-challenger")){
                saveCurrentChallengerToLocalStorage(challengerData, databaseData);
            }

            html += `<p>${doneCount} / ${totalCount} Challenges: ${percentComplete}% complete - ${todoCount} todos <a href='/gui/instances?entity=todo'>View Todos</a> - `;
            if(isCurrentChallengerSavedLocally(challengerData, databaseData)){
                html += `<button disabled>Saved Locally</button>`;
            }else{
                html += `<button onclick="saveCurrentChallengerToLocalStorage(document.challengerData,document.databaseData);this.innerText='Saved Locally';this.setAttribute('disabled',true)">Save Locally</button>`;
            }
            html += `</p>`;
        }
    }else{
        var possibleUuid = challengerGuidFromLocation();

        if(hasLocalSavedProgress(possibleUuid)){
            html += `<p id='local-restore-status'>Restoring locally saved session...</p>`;
            setTimeout(function(){autoRestoreLocalChallenger(possibleUuid);}, 0);
        }else if(hasLocalSavedTodos(possibleUuid)){
            html += `<p id='local-restore-status'>Saved todos found for ${escapeHtml(possibleUuid)}, but saved progress is needed to restore the challenger.</p>`;
        }

    }
    html += "</div>";
    return html;
}

function showCurrentStatus(){
    document.writeln(`<div id='challenge-progress-status'>${currentStatusHtml(document.challengerData, document.databaseData)}</div>`);
}

function updateCurrentStatus(){
    const status = document.getElementById("challenge-progress-status");
    if(status){
        status.innerHTML = currentStatusHtml(document.challengerData, document.databaseData);
    }
}

function currentChallengerForProgressRefresh(){
    if(document.challengerData && document.challengerData.xChallenger){
        return document.challengerData.xChallenger;
    }
    return getCookie("X-CHALLENGER") || getCookie("X-THINGIFIER-DATABASE-NAME") || "";
}

function fetchChallengeProgressStatus(){
    const headers = { "Accept": "application/json" };
    const challenger = currentChallengerForProgressRefresh();
    if(challenger){
        headers["X-CHALLENGER"] = challenger;
    }

    return fetch("/gui/challenge-status", {
        method: "GET",
        headers
    }).then(function(response){
        if(!response.ok){
            throw new Error(`Could not refresh progress (${response.status})`);
        }
        return response.json();
    }).then(function(progress){
        if(!progress || progress.known!==true){
            throw new Error("Challenger progress is no longer available.");
        }
        return progress;
    });
}

function setChallengeProgressRefreshMessage(message, isError){
    const messageElement = document.getElementById("challenge-progress-refresh-message");
    if(messageElement){
        messageElement.textContent = message || "";
        messageElement.className = isError ?
            "challenge-progress-refresh-message is-error" :
            "challenge-progress-refresh-message";
    }
}

function challengeRowFor(challengeId){
    return Array.from(document.querySelectorAll("tr[data-challenge-id]")).find(function(row){
        return row.dataset.challengeId === String(challengeId);
    });
}

function updateChallengeRows(challenges){
    if(!Array.isArray(challenges)){
        return;
    }
    challenges.forEach(function(challenge){
        const row = challengeRowFor(challenge.id);
        if(!row){
            return;
        }
        const done = challenge.status === true;
        const wasDone = row.classList.contains("statustrue");
        row.classList.remove("statustrue", "statusfalse");
        row.classList.add(done ? "statustrue" : "statusfalse");
        const doneCell = row.querySelector(".challenge-done-status");
        if(doneCell){
            doneCell.textContent = done ? "true" : "false";
        }
        if(done && !wasDone){
            row.classList.add("challenge-status-newly-completed");
            window.setTimeout(function(){
                row.classList.remove("challenge-status-newly-completed");
            }, 4500);
        }
    });
}

function doneCountFromChallengeProgress(progress){
    if(progress && progress.summary && typeof progress.summary.doneCount === "number"){
        return progress.summary.doneCount;
    }
    if(progress && progress.challengerData && progress.challengerData.challengeStatus){
        return completedChallengeCount(progress.challengerData.challengeStatus);
    }
    return 0;
}

function currentProgressDoneCount(){
    return doneCountFromChallengeProgress({
        challengerData: document.challengerData || {}
    });
}

function showProgressRefreshChallengeFireworks(){
    if(window.ApiChallengesLiveRequest &&
            typeof window.ApiChallengesLiveRequest.showFireworks === "function"){
        window.ApiChallengesLiveRequest.showFireworks();
    }
}

function applyChallengeProgressStatus(progress, options){
    const newDoneCount = doneCountFromChallengeProgress(progress);
    if(options &&
            options.fireworksOnProgressIncrease === true &&
            newDoneCount > options.previousDoneCount){
        showProgressRefreshChallengeFireworks();
    }
    document.challengerData = progress.challengerData || {};
    document.databaseData = progress.databaseData || {};
    updateChallengeRows(progress.challenges || []);
    updateAchievements();
    updateCurrentStatus();
    setChallengeProgressRefreshMessage("Progress refreshed.", false);
    return progress;
}

function isChallengeProgressAutoMonitorActive(){
    return challengeProgressAutoMonitorIntervalId !== null;
}

function stopChallengeProgressAutoMonitor(message, isError){
    if(challengeProgressAutoMonitorIntervalId){
        window.clearInterval(challengeProgressAutoMonitorIntervalId);
        challengeProgressAutoMonitorIntervalId = null;
    }
    if(challengeProgressAutoMonitorTimeoutId){
        window.clearTimeout(challengeProgressAutoMonitorTimeoutId);
        challengeProgressAutoMonitorTimeoutId = null;
    }
    const checkbox = document.getElementById("auto-monitor-challenge-progress");
    if(checkbox){
        checkbox.checked = false;
    }
    if(message){
        setChallengeProgressRefreshMessage(message, isError === true);
    }
}

function refreshChallengeProgress(options){
    const isAuto = options && options.auto === true;
    const shouldShowFireworksOnProgressIncrease =
        isAuto || (options && options.fireworksOnProgressIncrease === true);
    const previousDoneCount = currentProgressDoneCount();
    setChallengeProgressRefreshMessage("Checking progress...", false);
    return fetchChallengeProgressStatus()
        .then(function(progress){
            return applyChallengeProgressStatus(progress, {
                auto: isAuto,
                previousDoneCount: previousDoneCount,
                fireworksOnProgressIncrease: shouldShowFireworksOnProgressIncrease
            });
        })
        .catch(function(error){
            const message = error && error.message ? error.message : "Could not refresh progress.";
            if(isAuto || isChallengeProgressAutoMonitorActive()){
                stopChallengeProgressAutoMonitor(`Auto monitor stopped: ${message}`, true);
            }else{
                setChallengeProgressRefreshMessage(message, true);
            }
            return false;
        });
}

function startChallengeProgressAutoMonitor(){
    stopChallengeProgressAutoMonitor("", false);
    challengeProgressAutoMonitorIntervalId = window.setInterval(function(){
        refreshChallengeProgress({auto: true});
    }, CHALLENGE_PROGRESS_REFRESH_INTERVAL_MS);
    challengeProgressAutoMonitorTimeoutId = window.setTimeout(function(){
        stopChallengeProgressAutoMonitor("Auto monitor stopped after 30 minutes.", false);
    }, CHALLENGE_PROGRESS_AUTO_MONITOR_MS);
    const checkbox = document.getElementById("auto-monitor-challenge-progress");
    if(checkbox){
        checkbox.checked = true;
    }
    refreshChallengeProgress({auto: true});
}

document.addEventListener("click", function(event){
    if(event.target && event.target.id === "refresh-challenge-status"){
        refreshChallengeProgress({auto: false, fireworksOnProgressIncrease: true});
    }
});

document.addEventListener("change", function(event){
    if(event.target && event.target.id === "auto-monitor-challenge-progress"){
        if(event.target.checked){
            startChallengeProgressAutoMonitor();
        }else{
            stopChallengeProgressAutoMonitor("Auto monitor switched off.", false);
        }
    }
});

window.addEventListener("apiChallenges:challenge-passed", function(){
    refreshChallengeProgress({auto: false});
});

    // get challenger progress and save to local storage
    // get challenger todos and save to local storage

function saveChallengerProgressToLocalStorage(aChallenger){
    if(aChallenger && aChallenger.xChallenger){
        localStorage.setItem(aChallenger.xChallenger + ".progress", JSON.stringify(aChallenger));
    }
}

function saveChallengerTodosToLocalStorage(data, aChallenger){
    var dataString = normalizedTodosDataString(data);
    if(dataString && aChallenger && aChallenger.xChallenger){
        localStorage.setItem(aChallenger.xChallenger + ".data", dataString);
    }
}

function saveCurrentChallengerToLocalStorage(aChallenger, data){
    saveChallengerProgressToLocalStorage(aChallenger);
    saveChallengerTodosToLocalStorage(data, aChallenger);
    if(aChallenger && aChallenger.xChallenger){
        rememberChallengerGuid(aChallenger.xChallenger);
    }
}

function normalizedTodosDataString(data){
    if(!(data && Array.isArray(data.todos))){
        return null;
    }
    var dataCopy = JSON.parse(JSON.stringify(data));
    dataCopy.todos.sort((a,b)=>a.id-b.id);
    return JSON.stringify(dataCopy);
}

function progressNeedsSaving(aChallenger){
    if(!(aChallenger && aChallenger.xChallenger)){
        return false;
    }
    return localStorage.getItem(`${aChallenger.xChallenger}.progress`)!==JSON.stringify(aChallenger);
}

function todosNeedSaving(data, aChallenger){
    if(!(aChallenger && aChallenger.xChallenger)){
        return false;
    }
    var dataString = normalizedTodosDataString(data);
    if(dataString===null){
        return false;
    }
    return localStorage.getItem(`${aChallenger.xChallenger}.data`)!==dataString;
}

function isCurrentChallengerSavedLocally(aChallenger, data){
    return !progressNeedsSaving(aChallenger) && !todosNeedSaving(data, aChallenger);
}

function rememberChallengerGuid(guid){
    var sanitizedGuid = sanitizedChallengerGuid(guid);
    if(!sanitizedGuid){
        return;
    }
    setCookie('X-THINGIFIER-DATABASE-NAME', sanitizedGuid, 365);
    setCookie('X-CHALLENGER', sanitizedGuid, 365);
    var guids = localStorage.getItem('challenges-guids') || '';
    if(!guids.includes(`|${sanitizedGuid}|`)){
        localStorage.setItem('challenges-guids', guids + `|${sanitizedGuid}|`);
    }
    var guidsArray = cleanUnrestorableGuids((localStorage.getItem('challenges-guids') || '').match(/\|([^|]*)\|/g) || []);
    capPreviousGuidArray(guidsArray);
}

function restoreChallengerProgressInSystem(xchallengeruuid){

    var data = localStorage.getItem(`${xchallengeruuid}.progress`);
    if(data==null) return Promise.resolve(false);

    return fetch(`/challenger/${xchallengeruuid}`, {
      method: "PUT",
      body: data,
      headers: {
        "Content-type": "application/json",
      },
    })
    .then((response) => {
        if(!response.ok){
            return restoreResponseError(response, `Could not restore progress for ${xchallengeruuid}`);
        }
        console.log(response);
        return response;
    });

}

function restoreTodosInSystem(xchallengeruuid){

    var data = localStorage.getItem(`${xchallengeruuid}.data`);
    if(data==null) return Promise.resolve(false);

    return fetch(`/challenger/database/${xchallengeruuid}`, {
      method: "PUT",
      body: data,
      headers: {
        "Content-type": "application/json",
      },
    })
    .then((response) => {
        if(!response.ok){
            return restoreResponseError(response, `Could not restore todos for ${xchallengeruuid}`);
        }
        console.log(response);
        return response;
    });

}

function refreshLocalChallengerProgressFromSystem(xchallengeruuid){
    return fetch(`/challenger/${xchallengeruuid}`, {
      method: "GET",
      headers: {
        "Accept": "application/json",
      },
    })
    .then((response) => {
        if(!response.ok){
            return restoreResponseError(response, `Could not refresh restored progress for ${xchallengeruuid}`);
        }
        return response.json();
    })
    .then((challenger) => {
        saveChallengerProgressToLocalStorage(challenger);
        return challenger;
    });
}

function restoreResponseError(response, defaultMessage){
    return response.text().then((body) => {
        var details = restoreErrorDetails(body);
        var status = response.status ? ` (${response.status})` : "";
        throw new Error(details ? `${defaultMessage}: ${details}` : `${defaultMessage}${status}`);
    });
}

function restoreErrorDetails(body){
    if(!body){
        return "";
    }

    try{
        var parsedBody = JSON.parse(body);
        if(parsedBody.errorMessages && parsedBody.errorMessages.length>0){
            return parsedBody.errorMessages.join(" ");
        }
    }catch(e){
        // use raw body text below
    }
    return body;
}

function autoRestoreGuardKey(guid){
    return `${sanitizedChallengerGuid(guid)}.auto-restore-attempted`;
}

function restoreStatusElementId(guid){
    return `restore-status-${sanitizedChallengerGuid(guid)}`;
}

function setLocalRestoreMessage(message, guid){
    var statusElement = null;
    if(guid){
        statusElement = document.getElementById(restoreStatusElementId(guid));
    }
    if(!statusElement){
        statusElement = document.getElementById('local-restore-status');
    }
    if(statusElement){
        statusElement.innerText = message;
    }
}

function setRestoreButtonState(button, label, disabled){
    if(button){
        button.innerText = label;
        if(disabled){
            button.setAttribute('disabled', true);
        }else{
            button.removeAttribute('disabled');
        }
    }
}

function autoRestoreLocalChallenger(guid){
    var sanitizedGuid = sanitizedChallengerGuid(guid);
    var guardKey = autoRestoreGuardKey(sanitizedGuid);
    if(sessionStorage.getItem(guardKey)){
        setLocalRestoreMessage("Local auto-restore was already attempted for this challenger.");
        return;
    }
    sessionStorage.setItem(guardKey, "true");
    restoreLocalChallenger(sanitizedGuid);
}

function restoreLocalChallenger(guid, button){
    var sanitizedGuid = sanitizedChallengerGuid(guid);
    if(!hasLocalSavedProgress(sanitizedGuid)){
        setLocalRestoreMessage("Saved progress is needed to restore this challenger.", sanitizedGuid);
        return Promise.resolve(false);
    }

    setRestoreButtonState(button, "restoring", true);
    setLocalRestoreMessage("Restoring locally saved session...", sanitizedGuid);
    return restoreChallengerProgressInSystem(sanitizedGuid)
        .then(() => {
            rememberChallengerGuid(sanitizedGuid);
            if(hasLocalSavedTodos(sanitizedGuid)){
                return restoreTodosInSystem(sanitizedGuid);
            }
            return true;
        })
        .then(() => refreshLocalChallengerProgressFromSystem(sanitizedGuid))
        .then(() => {
            setLocalRestoreMessage("Restored locally saved session. Loading...", sanitizedGuid);
            setRestoreButtonState(button, "restored", true);
            location.href = `/gui/challenges/${sanitizedGuid}${location.hash || ""}`;
            return true;
        })
        .catch((error) => {
            console.log(error);
            setLocalRestoreMessage(error.message, sanitizedGuid);
            setRestoreButtonState(button, "restore", false);
            return false;
        });
}
