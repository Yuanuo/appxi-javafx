jQuery.fn.cssSelectorEx = function () {
    if (this.is("[id]"))
        return this.tagName() + '#' + this.attr('id');
    else if (this.is("span.note"))
        return "span." + this.attr("class").replaceAll("  ", " ").replaceAll(" ", ".") + "[data-n='" + (this.attr('data-n')) + "']";
    else if (this.is("[data-n]"))
        return this.tagName() + "." + this.attr("class").replaceAll("  ", " ").replaceAll(" ", ".") + "[data-n='" + (this.attr('data-n')) + "']";
    else if (this.is("span.lb"))
        return "span.lb[data-n='" + (this.attr('data-n')) + "']:first";
    return this.cssSelector();
};

function getScrollTopNElements(num = 10) {
    const list = $("body > article *:in-viewport");
    const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
    const result = [];
    for (let i = 0; i < list.length; i++) {
        const item = $(list[i]);
        if (item.offset().top >= scrollTop) {
            result.push(item);
            if (result.length >= num)
                break;
        }
    }
    return result;
}

function getScrollTop1Element() {
    const list = getScrollTopNElements(1);
    return list.length > 0 ? list[0] : null;
}

function getScrollTop1Selector($ele = null) {
    $ele = $ele || getScrollTop1Element();
    return $ele && $ele.cssSelectorEx();
}

function getScrollTop1AnchorInfo(outMapOrElseStr = true) {
    const $ele = getScrollTop1Element();
    if (!$ele) return;
    const selector = $ele.cssSelectorEx();
    if (!selector) return; // cannot restore
    const ele = $ele[0];
    let text = "";
    let handled = false;
    $('body > article').traverse(function (node) {
        if (!handled)
            handled = node === ele;
        if (handled && node.nodeType === 3)
            text = text + $(node).text();
        return text.length > 64;
    });
    if (!handled || text.length === 0) return;
    const map = {
        "anchor": selector,
        "text": text,
        "rangyPos": rangy.serializePosition(ele, 0, document)
    };
    return outMapOrElseStr ? map : JSON.stringify(map);
}

function setScrollTop1BySelectors(selector, percent = 0) {
    let scrollTop = 0;
    let target = selector && $(selector);
    if (selector && !target) target = $('#' + selector);
    if (target && target.length > 0)
        scrollTop = target.offset().top;
    else scrollTop = percent * document.body.scrollHeight;
    scrollTop = scrollTop - 3;
    scrollTop = scrollTop < 0 ? 0 : scrollTop;
    $("html, body").animate({scrollTop: scrollTop}, 150);
}


var javaApp;
let resizeBodyTimer;
let markedScrollTop1Selector;
let documentLoaded = false;

function onBodyResizeBefore() {
    if (!documentLoaded) return;
    markedScrollTop1Selector = markedScrollTop1Selector || getScrollTop1Selector();
}

$(document).ready(function () {
    documentLoaded = true;
    document.body.onresize = function () {
        if (!documentLoaded) return;
        if (resizeBodyTimer) clearTimeout(resizeBodyTimer);
        resizeBodyTimer = setTimeout(function () {
            resizeBodyTimer = null;
            if (!markedScrollTop1Selector) return;
            setScrollTop1BySelectors(markedScrollTop1Selector, 0);
            markedScrollTop1Selector = null;
        }, 200);
    };

    try {
        if (rangy) rangy.init();
    } catch (err) {
    }
});

function getValidSelection() {
    const selection = window.getSelection();
    if (!selection.anchorNode || !selection.anchorNode.parentElement)
        return null;
    const parentEle = $(selection.anchorNode.parentElement);
    if (parentEle.is('article') || parentEle.parents('article'))
        return selection;
    return null;
}

function getValidSelectionText() {
    const validSelection = getValidSelection();
    if (!validSelection) return null;
    const selected = validSelection.toString().trim();
    return selected.length < 1 ? null : selected;
}

/* ************************************************************************************************************************************* */

function handleOnWrapLines() {
    const markPos = getScrollTop1Selector();
    const article = $('body > article');
    article.toggleClass('wrap-lines-on');
    setScrollTop1BySelectors(markPos);
}

function handleOnWrapPages() {
    const markPos = getScrollTop1Selector();
    const article = $('body > article');
    article.toggleClass('wrap-pages-on');
    setScrollTop1BySelectors(markPos);
}

function handleOnPrettyIndent() {
    const article = $('body > article');
    const markOn = 'pretty-indent-on';
    // first time need to detect
    if (!article.prop(markOn)) {
        article.find('p, lg').each(function () {
            const $ele = $(this);
            const txt = $ele.text().trimLeft();
            if (txt.match(/^((“「)|(“『)|(「『)|(『「))/)) {
                if ($ele.hasClass("lg")) {
                    $ele.addClass('indent-first2-letter-lg');
                    if (txt.trimRight().match(/((”」)|(”』)|(」』)|(』」))$/))
                        $ele.addClass('indent-last2-letter-lg');
                    else if (txt.trimRight().match(/[”」』]$/))
                        $ele.addClass('indent-last-letter-lg');
                } else $ele.addClass('indent-first2-letter');
            } else if (txt.match(/^[“「『]/)) {
                if ($ele.hasClass("lg")) {
                    $ele.addClass('indent-first-letter-lg');
                    if (txt.trimRight().match(/[”」』]$/))
                        $ele.addClass('indent-last-letter-lg');
                } else $ele.addClass('indent-first-letter');
            }
        });
        article.prop(markOn, true);
    }
    article.toggleClass(markOn);
}

/* ************************************************************************************************************************************* */

function getBookmarkAnchorInfo() {
    const map = getScrollTop1AnchorInfo();
    if (!map) return null;
    return JSON.stringify(map);
}

function getFavoriteAnchorInfo() {
    let map = getValidSelectionAnchorInfo();
    if (!map) map = getScrollTop1AnchorInfo();
    if (!map) return null;
    return JSON.stringify(map);
}

function getHeadings() {
    let result = [];
    $("h1, h2, h3, h4, h5, h6").each(function() {
        const text = $(this).text();
        if (text && text.length > 0) result.push($(this).attr('id') + "#" + text);
    });
    return result.join('\n');
}

function _dict_SeeAlso(dictId, obj) {
    if (window.javaApp) javaApp.seeAlso(dictId, $(obj).text());
}
