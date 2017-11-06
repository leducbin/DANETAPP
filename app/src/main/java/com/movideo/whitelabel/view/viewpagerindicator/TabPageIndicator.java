/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.movideo.whitelabel.view.viewpagerindicator;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.movideo.whitelabel.R;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * This widget implements the dynamic action bar tab behavior that can change
 * across different configurations or circumstances.
 */
public class TabPageIndicator extends HorizontalScrollView implements PageIndicator {
    /**
     * Percentage indicating what percentage of the screen width away from
     * center should the underline be fully faded. A value of 0.25 means that
     * halfway between the center of the screen and an edge.
     */
    private static final float SELECTION_FADE_PERCENTAGE = 0.25f;

    /**
     * Title text used when no title is provided by the adapter.
     */
    private static final CharSequence EMPTY_TITLE = "";
    private final Paint mPaintText = new Paint();
    private final Paint mPaintFooterLine = new Paint();
    private final Paint mPaintFooterIndicator = new Paint();
    private IcsLinearLayout mTabLayout;
    private Runnable mTabSelector;
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mMaxTabWidth;
    private int mSelectedTabIndex;
    private int mCurrentPage = -1;
    private float mPageOffset;
    private Path mPath = new Path();
    private IndicatorStyle mFooterIndicatorStyle;
    private LinePosition mLinePosition;
    private float mFooterIndicatorHeight;
    private float mFooterIndicatorUnderlinePadding;
    private float mFooterPadding;
    private float mFooterLineHeight;
    private OnTabReselectedListener mTabReselectedListener;
    private final OnClickListener mTabClickListener = new OnClickListener() {
        public void onClick(View view) {
            TabView tabView = (TabView) view;
            final int oldSelected = mViewPager.getCurrentItem();
            final int newSelected = tabView.getIndex();
            mViewPager.setCurrentItem(newSelected);
            if (oldSelected == newSelected && mTabReselectedListener != null) {
                mTabReselectedListener.onTabReselected(newSelected);
            }
        }
    };

    public TabPageIndicator(Context context) {
        this(context, null);
    }

    public TabPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.tabPageIndicatorStyle);
        setHorizontalScrollBarEnabled(false);

        mTabLayout = new IcsLinearLayout(context, R.attr.tabPageIndicatorStyle);// Apply custom attr.
        addView(mTabLayout, new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));

        //Load defaults from resources
        final Resources res = getResources();
        final int defaultFooterColor = res.getColor(R.color.default_title_indicator_footer_color);
        final float defaultFooterLineHeight = res.getDimension(R.dimen.default_title_indicator_footer_line_height);
        final int defaultFooterIndicatorStyle = res.getInteger(R.integer.default_title_indicator_footer_indicator_style);
        final float defaultFooterIndicatorHeight = res.getDimension(R.dimen.default_title_indicator_footer_indicator_height);
        final float defaultFooterIndicatorUnderlinePadding = res.getDimension(R.dimen.default_title_indicator_footer_indicator_underline_padding);
        final float defaultFooterPadding = res.getDimension(R.dimen.default_title_indicator_footer_padding);
        final int defaultLinePosition = res.getInteger(R.integer.default_title_indicator_line_position);
        final float defaultTextSize = res.getDimension(R.dimen.default_title_indicator_text_size);

        //Retrieve styles attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);

        //Retrieve the colors to be used for this view and apply them.
        mFooterLineHeight = a.getDimension(R.styleable.ViewPagerIndicator_footerLineHeight, defaultFooterLineHeight);
        mFooterIndicatorStyle = IndicatorStyle.fromValue(a.getInteger(R.styleable.ViewPagerIndicator_footerIndicatorStyle, defaultFooterIndicatorStyle));
        mFooterIndicatorHeight = a.getDimension(R.styleable.ViewPagerIndicator_footerIndicatorHeight, defaultFooterIndicatorHeight);
        mFooterIndicatorUnderlinePadding = a.getDimension(R.styleable.ViewPagerIndicator_footerIndicatorUnderlinePadding, defaultFooterIndicatorUnderlinePadding);
        mFooterPadding = a.getDimension(R.styleable.ViewPagerIndicator_footerPadding, defaultFooterPadding);
        mLinePosition = LinePosition.fromValue(a.getInteger(R.styleable.ViewPagerIndicator_linePosition, defaultLinePosition));

        final float textSize = a.getDimension(R.styleable.ViewPagerIndicator_android_textSize, defaultTextSize);
        final int footerColor = a.getColor(R.styleable.ViewPagerIndicator_footerColor, defaultFooterColor);
        mPaintText.setTextSize(textSize);
        mPaintText.setAntiAlias(true);
        mPaintFooterLine.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintFooterLine.setStrokeWidth(mFooterLineHeight);
        mPaintFooterLine.setColor(footerColor);
        mPaintFooterIndicator.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintFooterIndicator.setColor(footerColor);

        a.recycle();
    }

    public TabPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) return;

        setHorizontalScrollBarEnabled(false);

        mTabLayout = new IcsLinearLayout(context, R.attr.tabPageIndicatorStyle);// Apply custom attr.
        addView(mTabLayout, new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT));

        //Load defaults from resources
        final Resources res = getResources();
        final int defaultFooterColor = res.getColor(R.color.default_title_indicator_footer_color);
        final float defaultFooterLineHeight = res.getDimension(R.dimen.default_title_indicator_footer_line_height);
        final int defaultFooterIndicatorStyle = res.getInteger(R.integer.default_title_indicator_footer_indicator_style);
        final float defaultFooterIndicatorHeight = res.getDimension(R.dimen.default_title_indicator_footer_indicator_height);
        final float defaultFooterIndicatorUnderlinePadding = res.getDimension(R.dimen.default_title_indicator_footer_indicator_underline_padding);
        final float defaultFooterPadding = res.getDimension(R.dimen.default_title_indicator_footer_padding);
        final int defaultLinePosition = res.getInteger(R.integer.default_title_indicator_line_position);
        final float defaultTextSize = res.getDimension(R.dimen.default_title_indicator_text_size);

        //Retrieve styles attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator, defStyle, 0);

        //Retrieve the colors to be used for this view and apply them.
        mFooterLineHeight = a.getDimension(R.styleable.ViewPagerIndicator_footerLineHeight, defaultFooterLineHeight);
        mFooterIndicatorStyle = IndicatorStyle.fromValue(a.getInteger(R.styleable.ViewPagerIndicator_footerIndicatorStyle, defaultFooterIndicatorStyle));
        mFooterIndicatorHeight = a.getDimension(R.styleable.ViewPagerIndicator_footerIndicatorHeight, defaultFooterIndicatorHeight);
        mFooterIndicatorUnderlinePadding = a.getDimension(R.styleable.ViewPagerIndicator_footerIndicatorUnderlinePadding, defaultFooterIndicatorUnderlinePadding);
        mFooterPadding = a.getDimension(R.styleable.ViewPagerIndicator_footerPadding, defaultFooterPadding);
        mLinePosition = LinePosition.fromValue(a.getInteger(R.styleable.ViewPagerIndicator_linePosition, defaultLinePosition));

        final float textSize = a.getDimension(R.styleable.ViewPagerIndicator_android_textSize, defaultTextSize);
        final int footerColor = a.getColor(R.styleable.ViewPagerIndicator_footerColor, defaultFooterColor);
        mPaintText.setTextSize(textSize);
        mPaintText.setAntiAlias(true);
        mPaintFooterLine.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintFooterLine.setStrokeWidth(mFooterLineHeight);
        mPaintFooterLine.setColor(footerColor);
        mPaintFooterIndicator.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintFooterIndicator.setColor(footerColor);

        a.recycle();
    }

    public int getFooterColor() {
        return mPaintFooterLine.getColor();
    }

    public void setFooterColor(int footerColor) {
        mPaintFooterLine.setColor(footerColor);
        mPaintFooterIndicator.setColor(footerColor);
        invalidate();
    }

    public float getFooterLineHeight() {
        return mFooterLineHeight;
    }

    public void setFooterLineHeight(float footerLineHeight) {
        mFooterLineHeight = footerLineHeight;
        mPaintFooterLine.setStrokeWidth(mFooterLineHeight);
        invalidate();
    }

    public float getFooterIndicatorHeight() {
        return mFooterIndicatorHeight;
    }

    public void setFooterIndicatorHeight(float footerTriangleHeight) {
        mFooterIndicatorHeight = footerTriangleHeight;
        invalidate();
    }

    public float getFooterIndicatorPadding() {
        return mFooterPadding;
    }

    public void setFooterIndicatorPadding(float footerIndicatorPadding) {
        mFooterPadding = footerIndicatorPadding;
        invalidate();
    }

    public IndicatorStyle getFooterIndicatorStyle() {
        return mFooterIndicatorStyle;
    }

    public void setFooterIndicatorStyle(IndicatorStyle indicatorStyle) {
        mFooterIndicatorStyle = indicatorStyle;
        invalidate();
    }

    public LinePosition getLinePosition() {
        return mLinePosition;
    }

    public void setLinePosition(LinePosition linePosition) {
        mLinePosition = linePosition;
        invalidate();
    }

    public void setOnTabReselectedListener(OnTabReselectedListener listener) {
        mTabReselectedListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }
        final int count = mViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }

        // mCurrentPage is -1 on first start and after orientation changed. If so, retrieve the correct index from viewpager.
        if (mCurrentPage == -1 && mViewPager != null) {
            mCurrentPage = mViewPager.getCurrentItem();
        }

        final View tabView = mTabLayout.getChildAt(mCurrentPage);

        //Calculate views bounds
        ArrayList<Rect> bounds = calculateAllBounds(mPaintText);
        final int boundsSize = bounds.size();

        int height = getHeight();
        int width = tabView.getWidth();

        final float halfWidth = width / 2f;

        float offsetPercent;
        int page = mCurrentPage;

        int startX = 0;

        for (int i = 0; i < mCurrentPage; i++) {
            startX += mTabLayout.getChildAt(i).getWidth();
        }

        if (mPageOffset <= 0.5) {
            offsetPercent = mPageOffset;
        } else {
            page += 1;
            offsetPercent = 1 - mPageOffset;
        }

        final boolean currentSelected = (offsetPercent <= SELECTION_FADE_PERCENTAGE);
        final float selectedPercent = (SELECTION_FADE_PERCENTAGE - offsetPercent) / SELECTION_FADE_PERCENTAGE;

        //If we want the line on the top change height to zero and invert the line height to trick the drawing code
        float footerLineHeight = mFooterLineHeight;
        float footerIndicatorLineHeight = mFooterIndicatorHeight;
        if (mLinePosition == LinePosition.Top) {
            height = 0;
            footerLineHeight = -footerLineHeight;
            footerIndicatorLineHeight = -footerIndicatorLineHeight;
        }

        if (footerLineHeight > 0) {
            //Draw the footer line
            mPath.reset();
            mPath.moveTo(0, height - footerLineHeight / 2f);
            mPath.lineTo(mTabLayout.getWidth(), height - footerLineHeight / 2f);
            mPath.close();
            canvas.drawPath(mPath, mPaintFooterLine);
        }


        float heightMinusLine = height - footerLineHeight;
        switch (mFooterIndicatorStyle) {
            case Triangle:
                mPath.reset();
                mPath.moveTo(startX + halfWidth, heightMinusLine - footerIndicatorLineHeight);
                mPath.lineTo(startX + halfWidth + footerIndicatorLineHeight, heightMinusLine);
                mPath.lineTo(startX + halfWidth - footerIndicatorLineHeight, heightMinusLine);
                mPath.close();
                canvas.drawPath(mPath, mPaintFooterIndicator);
                break;

            case Underline:
                if (!currentSelected || page >= boundsSize) {
                    break;
                }

                Rect underlineBounds = bounds.get(page);
                final float rightPlusPadding = underlineBounds.right + mFooterIndicatorUnderlinePadding;
                final float leftMinusPadding = underlineBounds.left - mFooterIndicatorUnderlinePadding;
                final float heightMinusLineMinusIndicator = heightMinusLine - footerIndicatorLineHeight;

                mPath.reset();
                mPath.moveTo(leftMinusPadding, heightMinusLine);
                mPath.lineTo(rightPlusPadding, heightMinusLine);
                mPath.lineTo(rightPlusPadding, heightMinusLineMinusIndicator);
                mPath.lineTo(leftMinusPadding, heightMinusLineMinusIndicator);
                mPath.close();

                mPaintFooterIndicator.setAlpha((int) (0xFF * selectedPercent));
                canvas.drawPath(mPath, mPaintFooterIndicator);
                mPaintFooterIndicator.setAlpha(0xFF);
                break;
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final boolean lockedExpanded = widthMode == MeasureSpec.EXACTLY;
        setFillViewport(lockedExpanded);

        final int childCount = mTabLayout.getChildCount();
        if (childCount > 1 && (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST)) {
            if (childCount > 2) {
                mMaxTabWidth = (int) (MeasureSpec.getSize(widthMeasureSpec) * 0.4f);
            } else {
                mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
            }
        } else {
            mMaxTabWidth = -1;
        }

        final int oldWidth = getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int newWidth = getMeasuredWidth();

        if (lockedExpanded && oldWidth != newWidth) {
            // Recenter the tab display if we're at a new (scrollable) size.
            setCurrentItem(mSelectedTabIndex);
        }
    }

    private void animateToTab(final int position) {
        final View tabView = mTabLayout.getChildAt(position);
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector);
        }
        mTabSelector = new Runnable() {
            public void run() {
                final int scrollPos = tabView.getLeft() - (getWidth() - tabView.getWidth()) / 2;
                smoothScrollTo(scrollPos, 0);
                mTabSelector = null;
            }
        };
        post(mTabSelector);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTabSelector != null) {
            // Re-post the selector we saved
            post(mTabSelector);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector);
        }
    }

    private void addTab(int index, CharSequence text, int iconResId) {
        final TabView tabView = new TabView(getContext());
        tabView.mIndex = index;
        tabView.setFocusable(true);
        tabView.setOnClickListener(mTabClickListener);
        tabView.setText(text);

        if (iconResId != 0) {
            tabView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
        }

        mTabLayout.addView(tabView, new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 1));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentPage = position;
        mPageOffset = positionOffset;
        invalidate();

        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentItem(position);

        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        final PagerAdapter adapter = view.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        view.setOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mTabLayout.removeAllViews();
        PagerAdapter adapter = mViewPager.getAdapter();
        IconPagerAdapter iconAdapter = null;
        if (adapter instanceof IconPagerAdapter) {
            iconAdapter = (IconPagerAdapter) adapter;
        }
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            CharSequence title = adapter.getPageTitle(i);
            if (title == null) {
                title = EMPTY_TITLE;
            }
            int iconResId = 0;
            if (iconAdapter != null) {
                iconResId = iconAdapter.getIconResId(i);
            }
            addTab(i, title, iconResId);
        }
        if (mSelectedTabIndex > count) {
            mSelectedTabIndex = count - 1;
        }
        setCurrentItem(mSelectedTabIndex);
        requestLayout();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager != null) {
            mSelectedTabIndex = item;
            mViewPager.setCurrentItem(item);

            final int tabCount = mTabLayout.getChildCount();
            for (int i = 0; i < tabCount; i++) {
                final View child = mTabLayout.getChildAt(i);
                final boolean isSelected = (i == item);
                child.setSelected(isSelected);
                if (isSelected) {
                    animateToTab(item);
                }
            }
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    /**
     * Calculate views bounds and scroll them according to the current index
     *
     * @param paint
     * @return
     */
    private ArrayList<Rect> calculateAllBounds(Paint paint) {
        ArrayList<Rect> list = new ArrayList<Rect>();
        //For each views (If no values then add a fake one)
        final int count = mViewPager.getAdapter().getCount();
//        final int width = getWidth();
//        final int halfWidth = width / 2;
        int startX = 0;
        int width = 0;
        for (int i = 0; i < count; i++) {

            width = mTabLayout.getChildAt(i).getWidth();

            Rect bounds = calcBounds(i, paint);
            int w = bounds.right - bounds.left;
            int h = bounds.bottom - bounds.top;

            bounds.left = startX + (int) (((width - w) / 2f) + bounds.left - ((i - mCurrentPage - mPageOffset) * width));
            bounds.right = (int) (bounds.left + w + w * .08f - ((i - mCurrentPage - mPageOffset) * width));
            bounds.top = 0;
            bounds.bottom = h;
            list.add(bounds);

            startX += width;


//            Rect bounds = calcBounds(i, paint);
//            int w = bounds.right - bounds.left;
//            int h = bounds.bottom - bounds.top;
//            bounds.left = (int) (halfWidth - (w / 2f) + ((i - mCurrentPage - mPageOffset) * width));
//            bounds.right = bounds.left + w;
//            bounds.top = 0;
//            bounds.bottom = h;
//            list.add(bounds);
        }

        return list;
    }

    /**
     * Calculate the bounds for a view's title
     *
     * @param index
     * @param paint
     * @return
     */
    private Rect calcBounds(int index, Paint paint) {
        //Calculate the text bounds
        Rect bounds = new Rect();
        CharSequence title = getTitle(index);
        bounds.right = (int) paint.measureText(title, 0, title.length());
        bounds.bottom = (int) (paint.descent() - paint.ascent());
        return bounds;
    }

    private CharSequence getTitle(int i) {
        CharSequence title = mViewPager.getAdapter().getPageTitle(i);
        if (title == null) {
            title = EMPTY_TITLE;
        }
        return title;
    }

    public enum IndicatorStyle {
        None(0), Triangle(1), Underline(2);

        public final int value;

        private IndicatorStyle(int value) {
            this.value = value;
        }

        public static IndicatorStyle fromValue(int value) {
            for (IndicatorStyle style : IndicatorStyle.values()) {
                if (style.value == value) {
                    return style;
                }
            }
            return null;
        }
    }

    public enum LinePosition {
        Bottom(0), Top(1);

        public final int value;

        private LinePosition(int value) {
            this.value = value;
        }

        public static LinePosition fromValue(int value) {
            for (LinePosition position : LinePosition.values()) {
                if (position.value == value) {
                    return position;
                }
            }
            return null;
        }
    }

    /**
     * Interface for a callback when the selected tab has been reselected.
     */
    public interface OnTabReselectedListener {
        /**
         * Callback when the selected tab has been reselected.
         *
         * @param position Position of the current center item.
         */
        void onTabReselected(int position);
    }

    static class SavedState extends BaseSavedState {
        @SuppressWarnings("UnusedDeclaration")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }
    }

    private class TabView extends TextView {
        private int mIndex;

        public TabView(Context context) {
            super(context, null, R.attr.tabPageIndicatorStyle);// Apply custom attr.
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            // Re-measure if we went beyond our maximum size.
            if (mMaxTabWidth > 0 && getMeasuredWidth() > mMaxTabWidth) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(mMaxTabWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        }

        public int getIndex() {
            return mIndex;
        }
    }
}