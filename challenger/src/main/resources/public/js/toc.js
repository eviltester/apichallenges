document.addEventListener('DOMContentLoaded', function() {
    htmlTableOfContents();
} );

// based on https://stackoverflow.com/a/41085566
function htmlTableOfContents( documentRef ) {
    documentRef = documentRef || document;
    var toc = documentRef.getElementById("toc");
    if(!toc)
        return;
    var content = toc.closest(".main-text-content") || documentRef.body;
    var headings = [].slice.call(content.querySelectorAll('h2, h3, h4, h5, h6'));
    if (!headings.length) {
        toc.remove();
        return;
    }
    var links = [];
    headings.forEach(function (heading, index) {
        var ref = "toc" + index;
        if ( heading.hasAttribute( "id" ) )
            ref = heading.getAttribute( "id" );
        else
            heading.setAttribute( "id", ref );

        var link = documentRef.createElement( "a" );
        link.setAttribute( "href", "#"+ ref );
        link.textContent = heading.textContent;

        var div = documentRef.createElement( "div" );
        div.setAttribute( "class", heading.tagName.toLowerCase() + " toc-entry" );
        div.setAttribute( "data-toc-target", ref );
        div.appendChild( link );
        toc.appendChild( div );
        links.push(link);
    });
    showTableOfContentsProgress(documentRef, toc, headings, links);
}

function showTableOfContentsProgress(documentRef, toc, headings, links) {
    if (!headings.length || !links.length) {
        return;
    }

    var windowRef = documentRef.defaultView || window;
    var activeLink;
    var activeEntry;
    var scheduled = false;

    function keepActiveEntryVisible(entry) {
        if (!entry || toc.scrollHeight <= toc.clientHeight) {
            return;
        }

        var entryTop = entry.offsetTop;
        var entryBottom = entryTop + entry.offsetHeight;
        var visibleTop = toc.scrollTop;
        var visibleBottom = visibleTop + toc.clientHeight;

        if (entryTop < visibleTop) {
            toc.scrollTop = Math.max(0, entryTop - 16);
        } else if (entryBottom > visibleBottom) {
            toc.scrollTop = entryBottom - toc.clientHeight + 16;
        }
    }

    function setActive(link) {
        if (!link || link === activeLink) {
            return;
        }

        if (activeEntry) {
            activeEntry.classList.remove("is-active");
        }
        if (activeLink) {
            activeLink.removeAttribute("aria-current");
        }

        activeLink = link;
        activeEntry = link.parentElement;
        activeEntry.classList.add("is-active");
        activeLink.setAttribute("aria-current", "location");
        keepActiveEntryVisible(activeEntry);
    }

    function activeLinkForScrollPosition() {
        var readingLine = windowRef.scrollY + Math.min(windowRef.innerHeight * 0.25, 220);
        var activeIndex = 0;

        headings.forEach(function (heading, index) {
            var headingTop = heading.getBoundingClientRect().top + windowRef.scrollY;
            if (headingTop <= readingLine) {
                activeIndex = index;
            }
        });

        return links[activeIndex];
    }

    function updateActiveLink() {
        scheduled = false;
        setActive(activeLinkForScrollPosition());
    }

    function scheduleUpdate() {
        if (scheduled) {
            return;
        }
        scheduled = true;
        windowRef.requestAnimationFrame(updateActiveLink);
    }

    links.forEach(function (link) {
        link.addEventListener("click", function () {
            setActive(link);
        });
    });

    windowRef.addEventListener("scroll", scheduleUpdate, {passive: true});
    windowRef.addEventListener("resize", scheduleUpdate);
    updateActiveLink();
}
