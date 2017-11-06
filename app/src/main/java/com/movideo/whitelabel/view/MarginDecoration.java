package com.movideo.whitelabel.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.movideo.whitelabel.R;
import com.movideo.whitelabel.util.DisplayResources;

public class MarginDecoration extends RecyclerView.ItemDecoration {
    private int margin;

    public MarginDecoration(Context context) {
        float px = context.getResources().getDimensionPixelSize(R.dimen.show_item_margin);
        margin = (int) new DisplayResources(context).convertPixelsToDp(px, context);
    }

    @Override
    public void getItemOffsets(
            Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(margin, margin, margin, margin);
    }
}