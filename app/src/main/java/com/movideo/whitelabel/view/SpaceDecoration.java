package com.movideo.whitelabel.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class SpaceDecoration extends RecyclerView.ItemDecoration {
    private final int spaceShow;
    private boolean firstDivider = false;
    private boolean lastDivider = false;

    int mOrientation = -1;

    public SpaceDecoration(Context context, AttributeSet attrs) {
        spaceShow = 0;
    }

    public SpaceDecoration(Context context, AttributeSet attrs, boolean firstDivider,
                           boolean lastDivider) {
        this(context, attrs);
        firstDivider = firstDivider;
        firstDivider = lastDivider;
    }

    public SpaceDecoration(int spaceInPx) {
        spaceShow = spaceInPx;
    }

    public SpaceDecoration(int spaceInPx, boolean firstDivider,
                           boolean lastDivider) {
        this(spaceInPx);
        firstDivider = firstDivider;
        lastDivider = lastDivider;
    }

    public SpaceDecoration(Context ctx, int resId) {
        spaceShow = ctx.getResources().getDimensionPixelSize(resId);
    }

    public SpaceDecoration(Context ctx, int resId, boolean firstDivider,
                           boolean lastDivider) {
        this(ctx, resId);
        firstDivider = firstDivider;
        lastDivider = lastDivider;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (spaceShow == 0) {
            return;
        }

        if (mOrientation == -1)
            getOrientation(parent);

        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION || (position == 0 && !firstDivider)) {
            return;
        }

        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.top = spaceShow;
            if (lastDivider && position == (state.getItemCount() - 1)) {
                outRect.bottom = outRect.top;
            }
        } else {
            outRect.left = spaceShow;
            if (lastDivider && position == (state.getItemCount() - 1)) {
                outRect.right = outRect.left;
            }
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (mOrientation == -1) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
                mOrientation = layoutManager.getOrientation();
            } else {
                throw new IllegalStateException(
                        "SpaceItemDecoration can only be used with a LinearLayoutManager.");
            }
        }
        return mOrientation;
    }
}