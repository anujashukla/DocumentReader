package com.example.anuja.documentReader;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anuja on 5/5/16.
 */

public class Pages {
    private final boolean mIncludePad;
    private final int mWidth;
    private final int mHeight;
    private final float mSpacingMult;
    private final float mSpacingAdd;
    private final CharSequence mText;
    private final TextPaint mPaint;
    private final List<CharSequence> mPages = new ArrayList<CharSequence>();
    private HashMap<Integer, Integer> mPageStartIndex= new HashMap<>();
    private int startOffset = 0;
    private int mType;

    public Pages(CharSequence text, int bufstart, int bufend, TextPaint paint, float spacingMult, float spacingAdd, boolean inclidePad, int type) {
        this.mText = text;
        this.mWidth = bufstart;
        this.mHeight = bufend;
        this.mPaint = paint;
        this.mSpacingMult = spacingMult;
        this.mSpacingAdd = spacingAdd;
        this.mIncludePad = inclidePad;
        this.mType = type;
        formPages();
    }

    private void formPages() {
        final StaticLayout layout = new StaticLayout(mText, mPaint, mWidth, Layout.Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, mIncludePad);
        final int lines = layout.getLineCount();
        final CharSequence text = layout.getText();
        int height = mHeight;

        for (int i = 0; i < lines; i++) {
            if (height < layout.getLineBottom(i)) {
                // when text is going out of the layout add this content to a page and continue
                addPage(text.subSequence(startOffset, layout.getLineStart(i)));
                startOffset = layout.getLineStart(i);
                height = layout.getLineTop(i) + mHeight;
            }

            if (i == lines - 1) {
                addPage(text.subSequence(startOffset, layout.getLineEnd(i)));

                if(this.mType == DocumentReaderActivity.NORMAL)
                    PagesArrays.setPagesNormalFont(mPages);
                if(this.mType == DocumentReaderActivity.ZOOM_FIRST_PART)
                    PagesArrays.setPagesZoomFirstPart(mPages);
                if(this.mType == DocumentReaderActivity.ZOOM_SECOND_PART)
                    PagesArrays.setPagesZoomSecondPart(mPages);

                return;
            }
        }
    }

    /*This function adds a page (page is nothing but char sequence set in a array) for given content*/
    private void addPage(CharSequence text) {
        mPages.add(text);
        if(this.mType == DocumentReaderActivity.NORMAL) 
            PagesArrays.mPageStartIndexNormal.put(mPages.size()-1, startOffset);
        mPageStartIndex.put(mPages.size()-1, startOffset);
    }

    public int size() {
        return mPages.size();
    }

    /*returns the charsequece i.e page at given index in Page instance*/
    public CharSequence get(int index) {
        return (index >= 0 && index < mPages.size()) ? mPages.get(index) : null;
    }

    /*as name suggests*/
    public int getStartOffsetForCurrentPage(int index) {
        return mPageStartIndex.get(index);
    }
}