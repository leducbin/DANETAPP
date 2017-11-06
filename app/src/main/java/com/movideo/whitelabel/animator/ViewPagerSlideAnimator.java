package com.movideo.whitelabel.animator;

import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Animates the view pager by sliding through the pages forwards and backwards.
 * Have to provide the number of pages.
 */
public class ViewPagerSlideAnimator implements ViewPager.OnPageChangeListener, Runnable, View.OnTouchListener {

    private static final String TAG = "ViewPagerSlideAnimator";
    private static final int START = 1;

    private boolean toLeft;
    private volatile boolean isRunning;
    private volatile boolean isStopped;
    private volatile boolean waitForTouch;
    private int currentPosition;
    private int totalPageCount;
    private int transitionWaitTime;
    private Handler handler;
    private ViewPager viewPager;

    /**
     * Construct the animator with following parameters.
     *
     * @param viewPager          {@link ViewPager} to be animated.
     * @param transitionWaitTime Waiting time of the page transition. Must set it in milliseconds and must be grater than 10 ms.
     */
    public ViewPagerSlideAnimator(ViewPager viewPager, int transitionWaitTime) throws Exception {

        if (viewPager == null)
            throw new Exception("ViewPager cannot be null");
        if (viewPager.getAdapter().getCount() <= 1)
            throw new Exception("ViewPager total page count cannot be less than 2");
        if (transitionWaitTime < 10)
            throw new Exception("ViewPager page transition wait time cannot be less than 10 ms");

        this.viewPager = viewPager;
        this.totalPageCount = viewPager.getAdapter().getCount();
        this.transitionWaitTime = transitionWaitTime;

        isRunning = true;
        isStopped = true;
        toLeft = true;
        waitForTouch = false;
        currentPosition = 1;
        handler = new Handler();
        this.viewPager.setOnTouchListener(this);
    }

    /**
     * Call this method on resume.
     */
    public void onResume() {
//        currentPosition = 0;
        isRunning = true;
        toLeft = true;

        viewPager.addOnPageChangeListener(this);
        if (isStopped) new Thread(this).start();
//        sliderChangePage(currentPosition);
    }

    /**
     * Call this method on pause.
     */
    public void onPause() {
        isRunning = false;
        synchronized (viewPager) {
            viewPager.notify();
        }
        viewPager.removeOnPageChangeListener(this);
    }

    /**
     * Changes the page of the view pager in to given page index position.
     *
     * @param position Page index, which starts with 0.
     */
    private void sliderChangePage(final int position) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(position, true);
            }
        });
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void run() {
        isStopped = false;
        final int last = totalPageCount - 2;
        if(isRunning) {
            try {
                synchronized (viewPager) {
                    viewPager.wait(transitionWaitTime);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        while (isRunning) {
            try {
                if (START == currentPosition) {
                    toLeft = true;
                    sliderChangePage(2);
                } else if (last == currentPosition) {

                    toLeft = false;
                    sliderChangePage(last - 1);
                } else {
                    if (toLeft)
                        sliderChangePage(currentPosition + 1);
                    else
                        sliderChangePage(currentPosition - 1);
                }
                synchronized (viewPager) {
                    viewPager.wait(transitionWaitTime);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        isStopped = true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                isRunning = false;
                synchronized (viewPager) {
                    viewPager.notify();
                }
                break;
            case MotionEvent.ACTION_DOWN:
                isRunning = false;
                synchronized (viewPager) {
                    viewPager.notify();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!waitForTouch) waitForTouch();
                break;
        }
        v.onTouchEvent(event);
        return true;
    }

    public boolean isRunning() {
        return isRunning && !isStopped;
    }

    public boolean isStopped() {
        return isStopped;
    }

    private void waitForTouch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    waitForTouch = true;
                    Thread.sleep(10000);

                    isRunning = true;

                    if (isStopped) new Thread(this).start();

                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage(), e);
                } finally {
                    waitForTouch = false;
                }
            }
        }).start();
    }
}