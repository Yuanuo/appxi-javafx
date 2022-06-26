package org.appxi.javafx.web;

import com.sun.webkit.WebPage;

public class WebSelection {
    /**
     * 当前选中文字，可能为""，不会为null
     */
    public final String text;
    /**
     * 当前选中文字的清洁版本（已去除所有换行，前后空格），可能为""，不会为null
     */
    public final String trims;
    /**
     * 当前是否存在选中文字，为true时表示至少存在1个字符
     */
    public final boolean hasText;
    /**
     * 当前是否存在选中文字的清洁版本，为true时表示至少存在1个字符
     */
    public final boolean hasTrims;

    WebSelection(WebPage webPage) {
        this(null == webPage ? "" : webPage.getClientSelectedText());
    }

    WebSelection(String selection) {
        this.text = null == selection ? "" : selection;
        this.hasText = !text.isEmpty();

        this.trims = !hasText ? "" : text.replace('\n', ' ').strip();
        this.hasTrims = !trims.isEmpty();
    }
}
