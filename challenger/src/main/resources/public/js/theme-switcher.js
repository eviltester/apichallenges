(function () {
    "use strict";

    const themes = [
        {
            value: "clean-docs",
            label: "Clean Developer Docs",
            title: "Use Clean Developer Docs theme",
            icon: "<svg viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\"><path d=\"M6 3.75h8.25L18 7.5v12.75H6z\"/><path d=\"M14 3.75V7.5h4\"/><path d=\"M9 11h6\"/><path d=\"M9 14.25h6\"/><path d=\"M9 17.5h4\"/></svg>"
        },
        {
            value: "learning-platform",
            label: "Learning Platform",
            title: "Use Learning Platform theme",
            icon: "<svg viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\"><path d=\"M4 6.75c2.55 0 4.9.55 7 1.65v10.35c-2.1-1.1-4.45-1.65-7-1.65z\"/><path d=\"M20 6.75c-2.55 0-4.9.55-7 1.65v10.35c2.1-1.1 4.45-1.65 7-1.65z\"/><path d=\"M12 8.4v10.35\"/><path d=\"M7 10.25c1.2.1 2.35.38 3.45.82\"/><path d=\"M17 10.25c-1.2.1-2.35.38-3.45.82\"/></svg>"
        },
        {
            value: "dark-lab",
            label: "Dark Lab",
            title: "Use Dark Lab theme",
            icon: "<svg viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\"><path d=\"M20 14.6A7.8 7.8 0 0 1 9.4 4a8.4 8.4 0 1 0 10.6 10.6z\"/><path d=\"M14.5 5.25h.01\"/><path d=\"M17.75 8h.01\"/></svg>"
        }
    ];
    const themeValues = themes.map((theme) => theme.value);
    const themeKey = "apichallenges-css-theme";

    function readStorage(key) {
        try {
            return window.localStorage.getItem(key);
        } catch (error) {
            return null;
        }
    }

    function writeStorage(key, value) {
        try {
            window.localStorage.setItem(key, value);
        } catch (error) {
            // Storage can be unavailable in private contexts; keep the switcher interactive.
        }
    }

    function isValidTheme(theme) {
        return themeValues.includes(theme);
    }

    function preferredTheme() {
        return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches
            ? "dark-lab"
            : "clean-docs";
    }

    function storedTheme() {
        const theme = readStorage(themeKey);
        return isValidTheme(theme) ? theme : null;
    }

    function currentTheme() {
        const fromPage = document.documentElement.getAttribute("data-theme");
        return isValidTheme(fromPage) ? fromPage : storedTheme() || preferredTheme();
    }

    function syncControl(theme) {
        document.querySelectorAll(".theme-switcher-button").forEach((button) => {
            const isActive = button.dataset.themeValue === theme;
            button.setAttribute("aria-pressed", isActive ? "true" : "false");
        });
    }

    function setTheme(theme, persist) {
        const selectedTheme = isValidTheme(theme) ? theme : preferredTheme();
        document.documentElement.setAttribute("data-theme", selectedTheme);
        if (persist) {
            writeStorage(themeKey, selectedTheme);
        }
        syncControl(selectedTheme);
    }

    function createIcon(markup) {
        const template = document.createElement("template");
        template.innerHTML = markup.trim();
        const icon = template.content.firstElementChild;
        icon.classList.add("theme-switcher-icon");
        return icon;
    }

    function createButton(theme) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "theme-switcher-button";
        button.dataset.themeValue = theme.value;
        button.setAttribute("aria-label", theme.label);
        button.setAttribute("aria-pressed", "false");
        button.title = theme.title;
        button.appendChild(createIcon(theme.icon));
        button.addEventListener("click", () => setTheme(theme.value, true));
        return button;
    }

    function switcherHost() {
        return document.querySelector(".container.cssmenu .css-menu")
            || document.querySelector(".container.cssmenu")
            || document.body;
    }

    function buildControl() {
        if (document.querySelector(".theme-switcher")) {
            return;
        }

        const form = document.createElement("form");
        form.className = "theme-switcher";
        form.setAttribute("aria-label", "Theme switcher");
        form.addEventListener("submit", (event) => event.preventDefault());

        themes.forEach((theme) => form.appendChild(createButton(theme)));
        switcherHost().appendChild(form);
        setTheme(storedTheme() || currentTheme(), false);
    }

    function ensureId(element, id) {
        if (!element.id) {
            element.id = id;
        }
        return element.id;
    }

    function buildSiteNavControl() {
        const container = document.querySelector(".container.cssmenu");
        const menu = container && container.querySelector(".css-menu");
        const subMenu = menu && menu.querySelector(".sub-menu");
        if (!container || !menu || !subMenu || menu.querySelector(".mobile-nav-toggle")) {
            return;
        }

        container.classList.add("is-compact");
        const button = document.createElement("button");
        button.type = "button";
        button.className = "mobile-nav-toggle";
        button.textContent = "Menu";
        button.setAttribute("aria-controls", ensureId(subMenu, "site-menu-links"));
        button.setAttribute("aria-expanded", "false");
        button.addEventListener("click", () => {
            const isOpen = container.classList.toggle("is-open");
            button.setAttribute("aria-expanded", isOpen ? "true" : "false");
        });
        menu.insertBefore(button, subMenu);
    }

    function buildContentNavControls() {
        document.querySelectorAll(".left-column").forEach((column, index) => {
            const sideToc = column.querySelector(".side-toc");
            if (!sideToc || column.querySelector(".mobile-content-nav-toggle")) {
                return;
            }

            column.classList.add("is-collapsible");
            const button = document.createElement("button");
            button.type = "button";
            button.className = "mobile-content-nav-toggle";
            button.textContent = "Content links";
            button.setAttribute("aria-controls", ensureId(sideToc, `content-links-${index + 1}`));
            button.setAttribute("aria-expanded", "false");
            button.addEventListener("click", () => {
                const isOpen = column.classList.toggle("is-open");
                button.setAttribute("aria-expanded", isOpen ? "true" : "false");
            });
            column.insertBefore(button, sideToc);
        });
    }

    function buildCompactNavigationControls() {
        buildSiteNavControl();
        buildContentNavControls();
    }

    setTheme(storedTheme() || currentTheme(), false);

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", () => {
            buildControl();
            buildCompactNavigationControls();
        });
    } else {
        buildControl();
        buildCompactNavigationControls();
    }
})();
