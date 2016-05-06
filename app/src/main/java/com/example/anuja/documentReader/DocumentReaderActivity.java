package com.example.anuja.documentReader;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by anuja on 5/5/16.
 */

public class DocumentReaderActivity extends Activity {
    private TextView mTextView;
    private ImageView mNextButton;
    private ImageView mPrevButton;
    private ImageView mZoomIn;
    private ImageView mZoomOut;
    private TextView mPageNum;
    private ProgressBar mProgressBar;

    private Pages mPages;
    private CharSequence mText;
    private Handler mButtonHandler;
    private OnClickListener mLayouOnTouchListener;
    private Runnable mButtonRunnable;

    public static final int NORMAL = 0;
    public static final int ZOOM_FIRST_PART = 1;
    public static final int ZOOM_SECOND_PART = 2;

    public static final float SIZE_NORMAL = 2f;
    public static final float SIZE_ZOOM = 3f;

    private int mCurrentIndex = 0;
    private static final int mHeadingText = 1;
    private static int mTextSize = 1000;
    private static int HIDE_TIMER_DELAY = 5000;
    private boolean mZoomedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_reader);

        mTextView = (TextView) findViewById(R.id.tv);
    	mNextButton = (ImageView) findViewById(R.id.btn_back);
    	mPrevButton = (ImageView) findViewById(R.id.btn_forward);
        mZoomIn = (ImageView) findViewById(R.id.zoom_in);
        mZoomOut = (ImageView) findViewById(R.id.zoom_out);
        mPageNum = (TextView) findViewById(R.id.page_number);
        mProgressBar = (ProgressBar) findViewById(R.id.loader);

    	mTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonHandler.removeCallbacks(mButtonRunnable);
                mNextButton.setVisibility(View.VISIBLE);
                mPrevButton.setVisibility(View.VISIBLE);
                mZoomOut.setVisibility(View.VISIBLE);
                mZoomIn.setVisibility(View.VISIBLE);
                mPageNum.setVisibility(View.VISIBLE);
                mButtonHandler.postDelayed(mButtonRunnable, HIDE_TIMER_DELAY);
            }
        });

    	mButtonHandler = new Handler();
    	mButtonRunnable = new Runnable() {
    	    public void run() {
    	        mNextButton.setVisibility(View.GONE);
    	        mPrevButton.setVisibility(View.GONE);
                mZoomOut.setVisibility(View.GONE);
                mZoomIn.setVisibility(View.GONE);
                mPageNum.setVisibility(View.GONE);
    	    }
    	};

        mButtonHandler.postDelayed(mButtonRunnable, HIDE_TIMER_DELAY);
        mText = new SpannableString(getString(R.string.long_string));
        mTextSize = mText.length();
        setTextInView(SIZE_NORMAL, (Spannable) mText, NORMAL);

        mZoomOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mZoomedIn) {
                    /*Get first index of current page, find page of normal size where
                    this index falls and thus show that page when zoom out*/
                    mProgressBar.setVisibility(View.VISIBLE);
                    findViewById(R.id.container).setEnabled(false);
                    PagesArrays.createPageStartIndexZoom();
                    mCurrentIndex = PagesArrays.getNormalPageIndexWhereOffsetFalls(PagesArrays.getStartOffsetFor(mCurrentIndex));
                    mZoomedIn = false;
                    mText = new SpannableString(getString(R.string.long_string));
                    mTextSize = mText.length();
                    setTextInView(SIZE_NORMAL, (Spannable) mText, NORMAL);
                    mProgressBar.setVisibility(View.GONE);
                    findViewById(R.id.container).setEnabled(true);
                }
            }
        });

        mZoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mZoomedIn) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    findViewById(R.id.container).setEnabled(false);
                    /*split our pages into - before first index of 
                    current page and after first index of current page*/
                    mZoomedIn = true;
                    int i = mPages.getStartOffsetForCurrentPage(mCurrentIndex);
                    Spannable s;
                    Spannable t;
                    int start, mid, last;
                    start = 0;
                    mid = mPages.getStartOffsetForCurrentPage(mCurrentIndex) -1;
                    last = mTextSize;
                    if(i > 0) {
                        s = (Spannable) mText.subSequence(mid, last);                    
                    }
                    else
                        s = (Spannable) mText.subSequence(0, mTextSize);
                    setTextInView(SIZE_ZOOM, s, ZOOM_SECOND_PART);
                    if(i > 0) {
                        t = (Spannable) mText.subSequence(start, mid);
                        setTextInView(SIZE_ZOOM, t, ZOOM_FIRST_PART);
                    }
                    mProgressBar.setVisibility(View.GONE);
                    findViewById(R.id.container).setEnabled(true);
                }
            }
        });
        
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex > 0) ? mCurrentIndex - 1 : 0;
                if(mZoomedIn)
                    updateZoom();
                else
                    update();
            }
        });
        findViewById(R.id.btn_forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                
                if(mZoomedIn) {
                    mCurrentIndex = (mCurrentIndex < PagesArrays.getPagesZoomComplete().size() - 1) ? mCurrentIndex + 1 : PagesArrays.getPagesZoomComplete().size() - 1;
                    updateZoom();
                }
                else {
                    mCurrentIndex = (mCurrentIndex < mPages.size() - 1) ? mCurrentIndex + 1 : mPages.size() - 1;
                    update();
                }
            }
        });
    }

    /*We split the character sequence including new line to call - pages
    and as per user's next and prev button moves - display text of current page.
    In case of zoom in - we create all together new set of pages conditioning 
    that one page must start at firt character of the normal page user had zoomed in
    and display that page when zoom in clicked. for zoom out - show the page of normal
    font where first character of current zoomed page lies*/
    private void setTextInView(float proportion, Spannable span, final int type) {
        final  Spannable spannable = span;
        int textSize = spannable.length();
        /*setting first letter with bigger font and blue color, similar way need to handle the Title*/
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0, mHeadingText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new RelativeSizeSpan(2*proportion), 0, mHeadingText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.MONOSPACE.getStyle()), 0, mHeadingText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        /*setting rest of text*/
        spannable.setSpan(new ForegroundColorSpan(Color.BLACK), mHeadingText, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new RelativeSizeSpan(proportion), mHeadingText, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.MONOSPACE.getStyle()), mHeadingText, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mTextView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                mPages = new Pages((CharSequence) spannable,
                        mTextView.getWidth(),
                        mTextView.getHeight(),
                        mTextView.getPaint(),
                        mTextView.getLineSpacingMultiplier(),
                        mTextView.getLineSpacingExtra(),
                        mTextView.getIncludeFontPadding(),
                        type);
                if(type == NORMAL) 
                    update();
                else {
                    mCurrentIndex = PagesArrays.getPagesZoomFirstPart().size();
                    updateZoom();
                }
            }
        });
    }

    private void update() {
        mPageNum.setText("Page "+(mCurrentIndex+1));
        final CharSequence text = mPages.get(mCurrentIndex);
        if(text != null) mTextView.setText(text);
    }

    private void updateZoom() {
        mPageNum.setText("Page "+(mCurrentIndex+1));
        final CharSequence text = PagesArrays.getPagesZoomComplete().get(mCurrentIndex);
        if(text != null) mTextView.setText(text);
    }
}
