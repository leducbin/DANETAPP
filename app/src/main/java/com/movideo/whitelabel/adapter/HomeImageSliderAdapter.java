package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.fragment.HomeImageSliderFragment;
import com.tobishiba.circularviewpager.library.BaseCircularViewPagerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This adaptor helps to slide home page background images.
 * This slider restrict number of images to the list size.
 */
public class HomeImageSliderAdapter extends BaseCircularViewPagerAdapter<Product> {

    private int screenWidth;
    private Context context;
    private List<Product> products;
    private Map<Integer, Fragment> fragmentTags;
    private FragmentManager fragmentManager;
    private HomeImageSliderFragment.ImageDrawListener listener;

    public HomeImageSliderAdapter(Context context, int screenWidth, FragmentManager fragmentManager, List<Product> products, HomeImageSliderFragment.ImageDrawListener listener) {
        super(fragmentManager, products);
        this.context = context;
        this.screenWidth = screenWidth;
        this.fragmentManager = fragmentManager;
        this.products = products;
        this.listener = listener;
        fragmentTags = new HashMap<>();
    }

    @Override
    protected Fragment getFragmentForItem(Product product) {

        int index = products.indexOf(product);

        HomeImageSliderFragment fragment = HomeImageSliderFragment.newInstance(screenWidth, product);

        if(index == 0) {
            fragment.setImageDrawListener(listener);
        }

        return fragment;
    }

    @Override
    public Fragment getItem(int position) {
        final int itemsSize = products.size();

        if (fragmentTags.containsKey(position)) {
            return fragmentTags.get(position);
        } else {
            Product product;
            if (position == 0) {
                product = products.get(itemsSize - 1);
            } else if (position == itemsSize + 1) {
                product = products.get(0);
            } else {
                product = products.get(position - 1);
            }

            Fragment fragment = getFragmentForItem(product);
            fragmentTags.put(position, fragment);

            return fragment;
        }
    }

    public Fragment getFragment(int position) {
        return fragmentTags.get(position);
    }
}
