package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.movideo.whitelabel.R;
import com.movideo.whitelabel.view.MyLibraryProductGridViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * This adaptor handles My Library page tab pages.
 */
public class MyLibrarySubPageAdapter extends PagerAdapter {

    private static final int NO_PAGES = 2;

    private static final int PAGE_WISH_LIST = 0;
    //    private static final int PAGE_WATCHED_LIST = 1;
    private static final int PAGE_RENTED_LIST = 1;

    private Context context;
    private String[] pageNames;
    private AbsListView.OnScrollListener listener;
    private AdapterView.OnItemClickListener itemClickListener;
    private Map<Integer, MyLibraryProductGridViewGroup> viewGroupHashMap;

    public MyLibrarySubPageAdapter(Context context, AbsListView.OnScrollListener listener, AdapterView.OnItemClickListener itemClickListener) {
        this.context = context;
        this.listener = listener;
        this.itemClickListener = itemClickListener;

        pageNames = context.getResources().getStringArray(R.array.string_array_my_library_sub_pages);

        viewGroupHashMap = new HashMap<>();
    }

    @Override
    public int getCount() {
        return NO_PAGES;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        MyLibraryProductGridViewGroup myLibraryProductGridViewGroup = null;

        if (viewGroupHashMap.containsKey(position)) {
            myLibraryProductGridViewGroup = viewGroupHashMap.get(position);
        } else {

            switch (position) {
                case PAGE_WISH_LIST:
                    myLibraryProductGridViewGroup = new MyLibraryProductGridViewGroup(context, MyLibraryProductGridViewGroup.PageType.WISH_LIST, listener, itemClickListener);
                    break;
//                case PAGE_WATCHED_LIST:
//                    myLibraryProductGridViewGroup = new MyLibraryProductGridViewGroup(context, MyLibraryProductGridViewGroup.PageType.WATCH_HISTORY, listener, itemClickListener);
//                    break;
                case PAGE_RENTED_LIST:
                    myLibraryProductGridViewGroup = new MyLibraryProductGridViewGroup(context, MyLibraryProductGridViewGroup.PageType.RENTED, listener, itemClickListener);
                    break;
            }
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            myLibraryProductGridViewGroup.setLayoutParams(params);

            ((ViewPager) container).addView(myLibraryProductGridViewGroup);

            viewGroupHashMap.put(position, myLibraryProductGridViewGroup);
        }

        return myLibraryProductGridViewGroup;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return pageNames[position];
    }
}
