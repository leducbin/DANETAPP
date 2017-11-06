package com.movideo.whitelabel.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.movideo.whitelabel.R;

/**
 * Provides a parallax scroll view.
 */
public class ParallaxScrollView extends ScrollView implements ViewTreeObserver.OnScrollChangedListener {

    public final static double NO_ZOOM = 1;
    public final static double ZOOM_X2 = 2;

    public final static float SCROLL_SPEED_X0_25 = 0.25f;
    public final static float SCROLL_SPEED_X0_5 = 0.5f;
    public final static float SCROLL_SPEED_X0_75 = 0.75f;
    public final static float SCROLL_NO = 1;

    private ImageView imageViewHeader;

    private boolean scrollable = true;
    private int drawableMaxHeight = -1;
    private int imageViewHeight = -1;
    private int defaultImageViewHeight = 0;
    private double zoomRatio = 1;
    private float scrollY = 0;
    private float scrollSpeed = 1;

    private OnOverScrollByListener scrollByListener;
    private OnTouchEventListener touchListener;

    public ParallaxScrollView(Context context) {
        super(context);
        init(context);
    }

    public ParallaxScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ParallaxScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        defaultImageViewHeight = context.getResources().getDimensionPixelSize(R.dimen.parallax_view_header_height);
        this.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.getViewTreeObserver().removeOnScrollChangedListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        initViewsBounds(zoomRatio);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        boolean isCollapseAnimation = false;

        isCollapseAnimation = scrollByListener.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent) || isCollapseAnimation;

        return isCollapseAnimation ? true : super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (imageViewHeader != null) {
            View firstView = (View) imageViewHeader.getParent();
            // firstView.getTop < getPaddingTop means imageViewHeader will be covered by top padding,
            // so we can layout it to make it shorter
            if (firstView.getTop() < getPaddingTop() && imageViewHeader.getHeight() > imageViewHeight) {
                imageViewHeader.getLayoutParams().height = Math.max(imageViewHeader.getHeight() - (getPaddingTop() - firstView.getTop()), imageViewHeight);
                // to set the firstView.mTop to 0,
                // maybe use View.setTop() is more easy, but it just support from Android 3.0 (API 11)
                firstView.layout(firstView.getLeft(), 0, firstView.getRight(), firstView.getHeight());
                imageViewHeader.requestLayout();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (scrollable) {
            touchListener.onTouchEvent(ev);
            return super.onTouchEvent(ev);
        }
        return scrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if it is not scrollable.
        if (!scrollable) return false;
        else return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onScrollChanged() {
        scrollY = this.getScrollY();

        imageViewHeader.setTranslationY((float) (scrollY * scrollSpeed));
        imageViewHeader.requestLayout();
    }

    /**
     * Returns true if scrollable.
     *
     * @return true/false
     */
    public boolean isScrollable() {
        return scrollable;
    }

    /**
     * Sets false to stop scrolling.
     *
     * @param scrollable true/false
     */
    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    /**
     * Set the header image to provide the parallax effect.
     *
     * @param imageView {@link ImageView}
     */
    public void setParallaxImageView(ImageView imageView) {
        imageViewHeader = imageView;
        imageViewHeader.setScaleType(ImageView.ScaleType.CENTER_CROP);
        scrollByListener = getScrollByListener();
        touchListener = getTouchListener();
    }

    /**
     * Sets the zoom ratio wanted with the parallax effect on header image. Value takes how many times against the header images own size.
     *
     * @param zoomRatio Can set from {@link #NO_ZOOM} or {@link #ZOOM_X2}
     */
    public void setZoomRatio(double zoomRatio) {
        this.zoomRatio = zoomRatio;
    }

    /**
     * Sets required scrolling speed to the header image to make the parallax effect. Value takes how many times against the scrolling speed.
     *
     * @param speed Can set from {@link #SCROLL_SPEED_X0_25}, {@link #SCROLL_SPEED_X0_5}, {@link #SCROLL_SPEED_X0_75} or {@link #SCROLL_NO} to have no effect.
     */
    public void setScrollSpeed(float speed) {
        scrollSpeed = speed;
    }

    private void initViewsBounds(double zoomRatio) {
        if (imageViewHeight == -1 && imageViewHeader != null) {
            imageViewHeight = imageViewHeader.getHeight();
            if (imageViewHeight <= 0) {
                imageViewHeight = defaultImageViewHeight;
            }
            double ratio = ((double) imageViewHeader.getDrawable().getIntrinsicWidth()) / ((double) imageViewHeader.getWidth());

            drawableMaxHeight = (int) ((imageViewHeader.getDrawable().getIntrinsicHeight() / ratio) * (zoomRatio > 1 ? zoomRatio : 1));
        }
    }

    private OnOverScrollByListener getScrollByListener() {
        return new OnOverScrollByListener() {
            @Override
            public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
                if (scrollY == 0 && imageViewHeader.getHeight() <= drawableMaxHeight && isTouchEvent) {
                    if (deltaY < 0) {
                        if (imageViewHeader.getHeight() - deltaY / 2 >= imageViewHeight) {
                            imageViewHeader.getLayoutParams().height = imageViewHeader.getHeight() - deltaY / 2 < drawableMaxHeight ? imageViewHeader.getHeight() - deltaY / 2 : drawableMaxHeight;
                            imageViewHeader.requestLayout();
                        }
                    } else {
                        if (imageViewHeader.getHeight() > imageViewHeight) {
                            imageViewHeader.getLayoutParams().height = imageViewHeader.getHeight() - deltaY > imageViewHeight ? imageViewHeader.getHeight() - deltaY : imageViewHeight;
                            imageViewHeader.requestLayout();
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    private OnTouchEventListener getTouchListener() {
        return new OnTouchEventListener() {
            @Override
            public void onTouchEvent(MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (imageViewHeight - 1 < imageViewHeader.getHeight()) {
                        ResetAnimation animation = new ResetAnimation(imageViewHeader, imageViewHeight);
                        animation.setDuration(300);
                        imageViewHeader.startAnimation(animation);
                    }
                }
            }
        };
    }

    private interface OnOverScrollByListener {
        public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent);
    }

    private interface OnTouchEventListener {
        public void onTouchEvent(MotionEvent ev);
    }

    public class ResetAnimation extends Animation {
        int targetHeight;
        int originalHeight;
        int extraHeight;
        View mView;

        protected ResetAnimation(View view, int targetHeight) {
            this.mView = view;
            this.targetHeight = targetHeight;
            originalHeight = view.getHeight();
            extraHeight = this.targetHeight - originalHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            int newHeight;
            newHeight = (int) (targetHeight - extraHeight * (1 - interpolatedTime));
            mView.getLayoutParams().height = newHeight;
            mView.requestLayout();
        }
    }
}
