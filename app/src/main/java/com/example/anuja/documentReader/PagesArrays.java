package com.example.anuja.documentReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PagesArrays {
	public static List<CharSequence> mPagesNormalFont = new ArrayList<CharSequence>();
	public static List<CharSequence> mPagesZoomFirstPart = new ArrayList<CharSequence>();
	public static List<CharSequence> mPagesZoomSecondPart = new ArrayList<CharSequence>();
	public static List<CharSequence> mPagesZoomComplete = new ArrayList<CharSequence>();

	public static HashMap<Integer, Integer> mPageStartIndexNormal= new HashMap<>();
	public static HashMap<Integer, Integer> mPageStartIndexZoom= new HashMap<>();

	public static List<CharSequence> getPagesNormalFont() {
		return mPagesNormalFont;
	}

	public static void setPagesNormalFont(List<CharSequence> list) {
		mPagesNormalFont.clear();
		mPagesNormalFont = list;
	}

	public static List<CharSequence> getPagesZoomFirstPart() {
		return mPagesZoomFirstPart;
	}

	public static void setPagesZoomFirstPart(List<CharSequence> list) {
		mPagesZoomFirstPart.clear();
		mPagesZoomFirstPart = list;
	}

	public static List<CharSequence> getPagesZoomSecondPart() {
		return mPagesZoomSecondPart;
	}

	public static void setPagesZoomSecondPart(List<CharSequence> list) {
		mPagesZoomSecondPart.clear();
		mPagesZoomSecondPart = list;
	}

	/*creates a single set(technically arralist) of pages 
	(of pages before and after start index of page where we zoomed)*/
	public static List<CharSequence> getPagesZoomComplete() {
		mPagesZoomComplete.clear();	
		mPagesZoomComplete.addAll(mPagesZoomFirstPart);
		mPagesZoomComplete.addAll(mPagesZoomSecondPart);		
		return mPagesZoomComplete;
	}

	/*creats map of key=page number (starting frmo 0) and value=starting offset - for Zoomed pages*/
	public static void createPageStartIndexZoom() {
		int i = 0;
		int totalParsed = 0;
		mPageStartIndexZoom.put(i,0);
		for(CharSequence c : mPagesZoomComplete) {
			i++;
			totalParsed = totalParsed + c.length();
			mPageStartIndexZoom.put(i, totalParsed);
		}
	}

	/*returns start offset of page at given index for zoomed pages*/
	public static int getStartOffsetFor(int index) {
		return mPageStartIndexZoom.get(index);
	}

	/*as name suggests*/
	public static int getNormalPageIndexWhereOffsetFalls(int offset) {
		int i = 0;
		for(i = 0; i < mPageStartIndexNormal.size(); i++) {
			if(mPageStartIndexNormal.get(i) >= offset) {
				break;
			}
		}
		return (i<=0 ? 0 : i-1);
	}
}