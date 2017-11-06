package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movideo.baracus.model.product.Offerings;
import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.PicassoHelper;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Handles list of movies and populate ListView.
 */
public class MovieListAdapter extends ArrayAdapter<Product> {

    private static final String TAG = MovieListAdapter.class.getSimpleName();

    private final Picasso picasso;

    private boolean showRentDue;
    private int height;
    private int width;
    private Context context;
    private int listItemLayout;
    private List<Product> list;

    /**
     * Default constructor.
     *
     * @param context  {@link Context}
     * @param resource Id of the list item layout.
     * @param list     List of {@link Product}.
     */
    public MovieListAdapter(Context context, int resource, List<Product> list) {
        super(context, resource, list);

        this.context = context;
        this.listItemLayout = resource;
        this.list = list;
        picasso = PicassoHelper.getInstance(context).getPicasso();

        height = (int) context.getResources().getDimension(R.dimen.horizontal_list_item_image_height);
        width = (int) context.getResources().getDimension(R.dimen.horizontal_list_item_image_width);
    }

    /**
     * Construct to show rent time left.
     *
     * @param context     {@link Context}
     * @param resource    Id of the list item layout.
     * @param list        List of {@link Product}.
     * @param showRentDue Set true to show rent time left.
     */
    public MovieListAdapter(Context context, int resource, List<Product> list, boolean showRentDue) {
        super(context, resource, list);

        this.context = context;
        this.listItemLayout = resource;
        this.list = list;
        this.showRentDue = showRentDue;

        picasso = PicassoHelper.getInstance(context).getPicasso();

        height = (int) context.getResources().getDimension(R.dimen.horizontal_list_item_image_height);
        width = (int) context.getResources().getDimension(R.dimen.horizontal_list_item_image_width);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolderItem viewHolder;
        boolean isMyLibrary;
        Page page;
        try {
            if (listItemLayout == R.layout.list_item_my_library) {
                page = Page.MY_LIBRARY;
                isMyLibrary = true;
            } else {
                page = Page.HOME_PAGE;
                isMyLibrary = false;
            }

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(listItemLayout, parent, false);

                if (isMyLibrary) {
                    viewHolder = new ViewHolderItemExtra(convertView);
                } else {
                    viewHolder = new ViewHolderItem(convertView);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            Product product = list.get(position);

            if (product != null) {
                viewHolder.product = product;
                viewHolder.itemTitle.setText(product.getTitle());
                picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, product, page, ImageURL.ImageProfile.POSTER))
                        .resize(width, height)
                        .onlyScaleDown()
                        .noFade()
                        .placeholder(R.drawable.fallback_thumbnail)
                        .into(viewHolder.itemImage);

                if (showRentDue) {
                    ((ViewHolderItemExtra) viewHolder).rentDue.setVisibility(View.VISIBLE);
                    ((ViewHolderItemExtra) viewHolder).rentDue.setText(getRentDuePeriodString(product.getOfferings()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    /**
     * Returns the list.
     *
     * @return {@link List <Product>}
     */
    public List<Product> getList() {
        return list;
    }

    /**
     * Sets the list when the list is updated. This will notify the adaptor with {@link #notifyDataSetChanged()}
     *
     * @param list {@link List<Product>}.
     */
    public void setList(List<Product> list) {
        this.list.clear();
        this.list.addAll(list);
        this.notifyDataSetChanged();
    }

    /**
     * Add list to current list. This will notify the adaptor with {@link #notifyDataSetChanged()}.
     *
     * @param list {@link List<Product>}.
     */
    public void addList(List<Product> list) {
        this.list.addAll(list);
        this.notifyDataSetChanged();
    }

    private String getRentDuePeriodString(List<Offerings> offeringsList) {
        String[] timePostfixes = context.getResources().getStringArray(R.array.string_array_my_library_rent_due_period);

        for (Offerings offering : offeringsList) {
            if (offering.getEntitled()) {
                Date currentDate = new Date();

                long diffInMillies = offering.getEndDate().getTime() - currentDate.getTime();
                long timePeriod = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
                timePeriod = Math.abs(timePeriod);
//                return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
//                int timePeriod = offering.getEndDate().compareTo(currentDate) / (1000 * 60);

                if (timePeriod < 60) {
                    return "CÒN " + timePeriod + " " + timePostfixes[0];
                } else if (timePeriod < 1440) {
                    return "CÒN " + timePeriod / 60 + " " + timePostfixes[1];
                } else {
                    return "CÒN " + timePeriod / 1440 + " " + timePostfixes[2];
                }
            }
        }
        return "0 " + timePostfixes[0];
    }

    /**
     * View holder for the list item in this list view
     */
    public static class ViewHolderItem {

        @Bind(R.id.imageViewHorizontalListItem)
        ImageView itemImage;
        @Bind(R.id.textViewHorizontalListItem)
        TextView itemTitle;

        boolean selected;
        Product product;

        public ViewHolderItem() {
            selected = false;
        }

        public ViewHolderItem(View view) {

            selected = false;
            ButterKnife.bind(this, view);
        }

        /**
         * Returns product associate with the item.
         *
         * @return {@link Product}
         */
        public Product getProduct() {
            return product;
        }

        /**
         * Returns true if item is selected.
         *
         * @return true/false
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Sets true if item is selected.
         *
         * @param selected true/false
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    public static class ViewHolderItemExtra extends ViewHolderItem {

        @Bind(R.id.textViewRentDue)
        TextView rentDue;
        @Bind(R.id.imageViewSelected)
        ImageView selectedIcon;
        @Bind(R.id.imageViewSelectedBg)
        ImageView selectedBg;

        public ViewHolderItemExtra(View view) {
            ButterKnife.bind(this, view);
        }

        /**
         * Returns select indicator icon image view.
         *
         * @return {@link ImageView}
         */
        public ImageView getSelectedIcon() {
            return selectedIcon;
        }

        /**
         * Returns select indicator item darken filter image view.
         *
         * @return {@link ImageView}
         */
        public ImageView getSelectedBg() {
            return selectedBg;
        }
    }
}
