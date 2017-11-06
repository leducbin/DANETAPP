package com.movideo.whitelabel.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.movideo.baracus.clientimpl.ImageRepo;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.ContentHandler;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.WhiteLabelApplication;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;

/**
 * Handles {@link View} related utility functions.
 */
public class ViewUtils {

    private static final String TAG = ViewUtils.class.getSimpleName();

    /**
     * Sets height of the given list view dynamically.
     *
     * @param listView {@link ListView}
     */
    public static synchronized void setListViewDynamicHeight(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            // when adapter is null
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = height + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /**
     * Sets height of the given grid view dynamically.
     *
     * @param gridView {@link GridView}
     */
    public static synchronized void setGridViewDynamicHeight(GridView gridView, int numberOfColumn) {

        ListAdapter listAdapter = gridView.getAdapter();

        if (listAdapter == null) {
            // when adapter is null
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(gridView.getWidth(), View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < listAdapter.getCount(); i += numberOfColumn) {
            View listItem = listAdapter.getView(i, null, gridView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = height + (gridView.getVerticalSpacing() * (listAdapter.getCount() - 1) / numberOfColumn);
        gridView.setLayoutParams(params);
        gridView.requestLayout();
    }

    public static synchronized String changeImageUrlQuality(String defaultUrl) {
        String defaultSize = "100x56.png";
        String newSize = "cropped/394x221.jpg";

        return defaultUrl.replace(defaultSize, newSize);
    }

    /**
     * Determine if the device is a tablet (i.e. it has a large screen).
     *
     * @param context The calling context.
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Returns image url based on the given parameters.
     *
     * @param context      {@link Context}
     * @param product      {@link Product}
     * @param page         {@link Page}
     * @param imageProfile {@link ImageURL.ImageProfile}
     * @return Completed url.
     */
    public static String getImageUrlOfProduct(Context context, Product product, Page page, ImageURL.ImageProfile imageProfile) {
        String profile = "default";
        if (imageProfile == ImageURL.ImageProfile.BACKGROUND || imageProfile == ImageURL.ImageProfile.HERO_BANNER_LANDSCAPE){
            profile = "background";
        }
        if (imageProfile == ImageURL.ImageProfile.CLIP){
            profile = "default";
        }
        if (imageProfile == ImageURL.ImageProfile.HERO_BANNER || imageProfile == ImageURL.ImageProfile.POSTER){
            profile = "poster";
        }


        boolean isTablet = isTablet(context);
        int density = context.getResources().getDisplayMetrics().densityDpi;

        String url = ImageRepo.instance().getImage(product.getId(), profile);
        if (null == url){ //this image is not loaded yet. Try to save this product image then call again.
                ImageRepo.instance().saveAllProductImages(product);
                url = ImageRepo.instance().getImage(product.getId(), profile);
        }

        return url;

//        if (product == null || product.getImage() == null || product.getImage().getBaseUri() == null) {
//            return null;
//        }
//        String uri = product.getImage().getBaseUri();
//        switch (imageProfile) {
//            case POSTER:
//                switch (page) {
//                    case HOME_PAGE:
//                        if (isTablet) {
//                            return ImageURL.HOME_TABLET_POSTER.getImageUrl(uri, density);
//                        } else {
//                            return ImageURL.HOME_PHONE_POSTER.getImageUrl(uri, density);
//                        }
//                    case MY_LIBRARY:
//                        if (isTablet) {
//                            return ImageURL.MY_LIBRARY_TABLET_POSTER.getImageUrl(uri, density);
//                        } else {
//                            return ImageURL.MY_LIBRARY_PHONE_POSTER.getImageUrl(uri, density);
//                        }
//                    case MOVIE_DETAIL_PAGE:
//                        if (isTablet) {
//                            return ImageURL.HOME_TABLET_POSTER.getImageUrl(uri, density);
//                        } else {
//                            return ImageURL.HOME_PHONE_POSTER.getImageUrl(uri, density);
//                        }
//                    case SHOW_DETAIL_PAGE:
//                        if (isTablet) {
//                            return ImageURL.HOME_TABLET_POSTER.getImageUrl(uri, density);
//                        } else {
//                            return ImageURL.HOME_PHONE_POSTER.getImageUrl(uri, density);
//                        }
//                    case CLIP_DETAIL_PAGE:
//                        if (isTablet) {
//                            return ImageURL.HOME_TABLET_POSTER.getImageUrl(uri, density);
//                        } else {
//                            return ImageURL.HOME_PHONE_POSTER.getImageUrl(uri, density);
//                        }
//                    case SEARCH_PAGE:
//                        return ImageURL.SEARCH_POSTER.getImageUrl(uri, density);
//                    case MOVIE_TRAILER:
//                        if (isTablet) {
//                            return ImageURL.MOVIE_TRAILER_TABLET_POSTER.getImageUrl(uri, density);
//                        } else {
//                            return ImageURL.MOVIE_TRAILER_PHONE_POSTER.getImageUrl(uri, density);
//                        }
//                }
//                break;
//            case CLIP:
//                switch (page) {
//                    case HOME_PAGE:
//                        if (isTablet) {
//                            return ImageURL.HOME_TABLET_CLIP.getImageUrl(uri, density);
//                        } else {
//                            return ImageURL.HOME_PHONE_CLIP.getImageUrl(uri, density);
//                        }
//                    case CLIP_DETAIL_PAGE:
//                        if (isTablet) {
//                            return ImageURL.HOME_TABLET_CLIP.getImageUrl(uri, density);
//                        } else {
//                            return ImageURL.HOME_PHONE_CLIP.getImageUrl(uri, density);
//                        }
//                    case SEARCH_PAGE:
//                        return ImageURL.SEARCH_CLIP.getImageUrl(uri, density);
//                    case MOVIE_TRAILER:
//                        if (isTablet) {
//                            return ImageURL.MOVIE_TRAILER_TABLET_CLIP.getImageUrl(uri, density);
//                        } else {
//                            return ImageURL.MOVIE_TRAILER_PHONE_CLIP.getImageUrl(uri, density);
//                        }
//                }
//                break;
//            case HERO_BANNER:
//                if (isTablet) {
//                    return ImageURL.HOME_TABLET_HERO_BANNER.getImageUrl(uri, density);
//                } else {
//                    return ImageURL.HOME_PHONE_HERO_BANNER.getImageUrl(uri, density);
//                }
//            case BACKGROUND:
//                if (isTablet) {
//                    return ImageURL.HOME_TABLET_HERO_BANNER.getImageUrl(uri, density);
//                } else {
//                    return ImageURL.HOME_PHONE_HERO_BANNER.getImageUrl(uri, density);
//                }
//        }
//        return null;
    }

    /**
     * Sets AsyncTask call.
     *
     * @param asyncTask
     */
    public static void execute(AsyncTask asyncTask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            asyncTask.execute();
        }
    }

    /**
     * Sets AsyncTask call with arguments.
     *
     * @param asyncTask
     * @param params
     */
    public static void execute(AsyncTask asyncTask, Object... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            asyncTask.execute(params);
        }
    }

    /**
     * Unbind the drawables in given view.
     *
     * @param view {@link View}
     */
    public void unbindDrawables(final View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                unbind(view);
            }
        }).start();
    }

    private void unbind(final View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbind(((ViewGroup) view).getChildAt(i));
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (view instanceof ImageView) {
                            ImageView imageView = (ImageView) view;
                            Drawable drawable = imageView.getDrawable();
                            if (drawable instanceof BitmapDrawable) {
                                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                                Bitmap bitmap = bitmapDrawable.getBitmap();
                                if (!bitmap.isRecycled())
                                    bitmap.recycle();
                                bitmap = null;
                                imageView.setImageBitmap(null);
                            }
                        }
                        if (!(view instanceof AdapterView))
                            ((ViewGroup) view).removeAllViews();
                    } catch (Throwable e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
        }
    }

    //Added by Thanh Tam
    public static Button setButtonDynamicStyle(Button button, Context context){

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins( (int) (context.getResources().getDimension(R.dimen.cineplex_credit_button_margin)),  //marginLeft
                (int) (context.getResources().getDimension(R.dimen.cineplex_credit_button_margin)),             //marginTop
                (int) (context.getResources().getDimension(R.dimen.cineplex_credit_button_margin)),             //marginRight
                0);                       //marginBottom
        button.setLayoutParams(params);
        button.setPadding( (int) context.getResources().getDimension(R.dimen.cineplex_credit_button_padding_left_right),
                (int) context.getResources().getDimension(R.dimen.cineplex_credit_button_padding_top_bottom),
                (int) context.getResources().getDimension(R.dimen.cineplex_credit_button_padding_left_right),
                (int) context.getResources().getDimension(R.dimen.cineplex_credit_button_padding_top_bottom));
        button.setTextColor(Color.parseColor("#ffffff"));
        //btnSubscription.setTextSize(getResources().getDimension(R.dimen.text_view_medium_size));
        button.setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.signout_button_bg,null));
        button.setGravity(Gravity.CENTER);
        return button;
    }

    // End added by Thanh Tam

}
