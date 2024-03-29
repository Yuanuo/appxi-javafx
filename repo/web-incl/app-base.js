var urlParams = new URLSearchParams(window.location.search);

function setWebStyleTheme(theme) {
    const oldValue = document.body.getAttribute('class') || '';
    const oldTheme = document.body.getAttribute('theme');
    let newValue;
    if (oldTheme && oldTheme.length > 0) {
        newValue = oldValue.replace(oldTheme, theme);
    } else {
        newValue = oldValue.trim() + ' ' + theme;
    }
    document.body.setAttribute('theme', theme);
    document.body.setAttribute('class', newValue);
}

function setWebStyleSheetLocation(src, _id = 'CSS') {
   let ele = document.querySelector('html > head > link#' + _id);
   if (ele) {
       ele.setAttribute('href', src);
   } else {
       ele = document.createElement('link');
       ele.setAttribute('id', _id);
       ele.setAttribute('rel', 'stylesheet');
       ele.setAttribute('type', 'text/css');
       ele.setAttribute('href', src);
       document.head.appendChild(ele);
   }
}

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
    if (target && target.length > 0) {
        target = target.first();
        scrollTop = target.offset().top;
    }
    else scrollTop = percent * document.body.scrollHeight;
    scrollTop = scrollTop - 3;
    scrollTop = scrollTop < 0 ? 0 : scrollTop;
    $("html, body").animate({scrollTop: scrollTop}, 150);
}


function getValidSelection() {
    const selection = window.getSelection();
    if (!selection.anchorNode || !selection.anchorNode.parentElement)
        return null;
    const parentEle = $(selection.anchorNode.parentElement);
    if (parentEle.is('article') || parentEle.parents('article'))
        return selection;
    return null;
}

function _findContainerIndex(container, nodeList) {
    for (let i = 0; i < nodeList.length; i++) {
        if (container == nodeList[i]) {
            return i;
        }
    }
    return _findContainerIndex(container.parentNode, nodeList);
}

function getSelectionInfo() {
    const selection = window.getSelection();
    const noRange = selection.rangeCount < 1;
    
    const range = noRange ? null : selection.getRangeAt(0);
    const rangeNode = noRange ? null : range.commonAncestorContainer;

    const startNode = noRange ? null : range.startContainer;
    const startNodeOfs = noRange ? null : range.startOffset;
    
    const endNode = noRange ? null : range.endContainer;
    const endNodeOfs = noRange ? null : range.endOffset;

    let startNodeIdx, endNodeIdx;
    if (noRange || startNode === rangeNode) {
        startNodeIdx = -1;
        endNodeIdx = -1;
    } else {
        startNodeIdx = _findContainerIndex(startNode, rangeNode.childNodes);
        endNodeIdx = _findContainerIndex(endNode, rangeNode.childNodes);
    }

    if (startNodeIdx && startNodeIdx > endNodeIdx) {
        return {
            'startNode' : endNode,
            'startNodeIdx' : endNodeIdx,
            'startNodeOfs' : endNodeOfs,
            'endNode' : startNode,
            'endNodeIdx' : startNodeIdx,
            'endNodeOfs' : startNodeOfs,
            'range' : range,
            'rangeNode' : rangeNode,
            'selection' : selection
        };
    }
    return {
        'startNode' : startNode,
        'startNodeIdx' : startNodeIdx,
        'startNodeOfs' : startNodeOfs,
        'endNode' : endNode,
        'endNodeIdx' : endNodeIdx,
        'endNodeOfs' : endNodeOfs,
        'range' : range,
        'rangeNode' : rangeNode,
        'selection' : selection
    };
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
    return getPositions("h1, h2, h3, h4, h5, h6");
}

function getPositions(_posSelector, _txtSelector) {
    let result = [];
    $(_posSelector).each(function() {
        const _txtEle = _txtSelector ? $(this).next(_txtSelector) : $(this);
        const text = _txtEle.length > 0 && _txtEle.text();
        if (text && text.length > 0) {
            result.push($(this).cssSelector() + "||" + text);
        }
    });
    return result.join('\n');
}

function _dict_SeeAlso(dictId, obj) {
    if (window.javaApp) javaApp.seeAlso(dictId, $(obj).text());
}

function mark_text_and_count(text) {
    $(document.body).mark(text, { acrossElements: true });
    return $('mark').length;
}

function __openSearched(pieceId, isRefText) {
    if (window.javaApp){
        let refText = '';
        if (isRefText) {
            refText = $('#'+ pieceId).text();
            pieceId = pieceId.substring(0, pieceId.length - 2);
        }
        javaApp.openSearched(pieceId, refText);
    }
}
