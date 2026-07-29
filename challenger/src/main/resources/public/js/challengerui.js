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

function isAchievementUnlocked(definition, challengeStatus, doneCount, totalCount){
    if(definition.challengeKey){
        return challengeStatus[definition.challengeKey]===true;
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

function setAchievementDetails(medal){
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
    panel.querySelectorAll(".achievement-medal").forEach(function(item){
        item.classList.remove("is-selected");
        item.setAttribute("aria-pressed", "false");
    });
    medal.classList.add("is-selected");
    medal.setAttribute("aria-pressed", "true");
    detail.innerHTML = achievementDetailHtml(achievement);
}

function initialiseAchievementRail(){
    document.querySelectorAll(".achievement-rail-panel").forEach(function(panel){
        panel.querySelectorAll(".achievement-medal").forEach(function(medal){
            medal.addEventListener("mouseenter", function(){ setAchievementDetails(medal); });
            medal.addEventListener("focus", function(){ setAchievementDetails(medal); });
            medal.addEventListener("click", function(){ setAchievementDetails(medal); });
        });
    });
}

function showAchievements(){
    const achievements = achievementsForChallenger(document.challengerData);
    const unlockedCount = achievements.filter(achievement => achievement.unlocked).length;
    const selectedIndex = selectedAchievementIndex(achievements);
    const selectedAchievement = achievements[selectedIndex] || achievements[0];

    document.writeln("<section class='achievement-rail-panel' aria-label='Achievements'>");
    document.writeln("<div class='achievement-rail-header'>");
    document.writeln("<h2 class='achievement-rail-title'>Achievements</h2>");
    document.writeln(`<div class='achievement-rail-meta'>${unlockedCount} of ${achievements.length} unlocked</div>`);
    document.writeln("</div>");
    document.writeln("<div class='achievement-rail-track' role='list' aria-label='Achievement medals'>");
    achievements.forEach(function(achievement, index){
        document.writeln(achievementMedalHtml(achievement, index, selectedIndex));
    });
    document.writeln("</div>");
    document.writeln("<div class='achievement-detail' aria-live='polite'>");
    document.writeln(achievementDetailHtml(selectedAchievement));
    document.writeln("</div>");
    document.writeln("</section>");
    setTimeout(initialiseAchievementRail, 0);
}

function showCurrentStatus(){
    const challengerData = document.challengerData;
    const databaseData = document.databaseData;

    document.writeln("<div>");

    document.writeln(`<p><button onclick=location.reload()>Refresh Status</button>`);

    // issue if immediately autosave then it will wipe out any saved
    // use a session cookie to force setting for everyone
    // and have a last-auto-saved-guid and only switch on auto save if current guid matches last autosaved
    var lastAutoSavedGuid = getCookie("last-auto-saved-x-challenger")
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

    var autoSaveOn = autoSave ? "checked" : ""
    var autoSaveOnChange = autoSave ? "'deleteCookieSaveLocally();location.reload();'" : "'setCookieSaveLocally();location.reload()'"
    var autoSaveLocallyHtml = ` <input type='checkbox' id='auto-save-locally-check' name='auto-save-locally-check' value='auto-save-locally-check' ${autoSaveOn} onchange=${autoSaveOnChange}><label for='auto-save-locally-check'> Auto Save Todos and Progress Locally on refresh</label>`
    document.writeln(autoSaveLocallyHtml);

    document.writeln("</p>");


    if(challengerData && challengerData.xChallenger){
        var xChallengerGuid = challengerData.xChallenger;
        sessionStorage.removeItem(autoRestoreGuardKey(xChallengerGuid));


        if(challengerData.challengeStatus){
            var status = challengerData.challengeStatus;
            var doneCount = Object.values(challengerData.challengeStatus).filter(x=>x).length;
            var totalCount = Object.values(challengerData.challengeStatus).length;
            var percentComplete = totalCount===0 ? 0 : Math.round((doneCount/totalCount)*100);
            var todoCount = (databaseData && databaseData.todos) ? databaseData.todos.length : 0;

            if(autoSave){
                saveCurrentChallengerToLocalStorage(challengerData, databaseData);
            }

            document.writeln(`<p>${doneCount} / ${totalCount} Challenges: ${percentComplete}% complete - ${todoCount} todos <a href='/gui/instances?entity=todo'>View Todos</a> - `);
            if(autoSave || isCurrentChallengerSavedLocally(challengerData, databaseData)){
                document.writeln(`<button disabled>Saved Locally</button>`);
            }else{
                document.writeln(`<button onclick="saveCurrentChallengerToLocalStorage(document.challengerData,document.databaseData);this.innerText='Saved Locally';this.setAttribute('disabled',true)">Save Locally</button>`);
            }
            document.writeln(`</p>`);
        }
    }else{
        // if we have a guid in the url then allow restoring
        var possibleUuid = challengerGuidFromLocation();

        if(hasLocalSavedProgress(possibleUuid)){
            document.writeln(`<p id='local-restore-status'>Restoring locally saved session...</p>`);
            setTimeout(function(){autoRestoreLocalChallenger(possibleUuid);}, 0);
        }else if(hasLocalSavedTodos(possibleUuid)){
            document.writeln(`<p id='local-restore-status'>Saved todos found for ${possibleUuid}, but saved progress is needed to restore the challenger.</p>`);
        }

    }
    document.writeln("</div>");
    // if we haven't managed to create the challenger yet
}

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
            location.href = `/gui/challenges/${sanitizedGuid}`;
            return true;
        })
        .catch((error) => {
            console.log(error);
            setLocalRestoreMessage(error.message, sanitizedGuid);
            setRestoreButtonState(button, "restore", false);
            return false;
        });
}
