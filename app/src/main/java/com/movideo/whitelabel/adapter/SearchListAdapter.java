package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.PicassoHelper;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Populates a ListView from list of products comes from a search.
 */
public class SearchListAdapter extends ArrayAdapter<Product> {

    private static final String TAG = SearchListAdapter.class.getSimpleName();

    private final Picasso picasso;

    private int posterHeight;
    private int posterWidth;
    private int clipHeight;
    private int clipWidth;
    private int listItemLayout;
    private List<Product> list;

    /**
     * Default constructor.
     *
     * @param context  {@link Context}.
     * @param resource Id of the list item layout.
     * @param list     {@link List<Product>}.
     */
    public SearchListAdapter(Context context, int resource, List<Product> list) {
        super(context, resource, list);

        this.listItemLayout = resource;
        this.list = list;
        picasso = PicassoHelper.getInstance(context).getPicasso();

        posterHeight = (int) context.getResources().getDimension(R.dimen.search_list_item_poster_height);
        posterWidth = (int) context.getResources().getDimension(R.dimen.search_list_item_poster_width);

        clipHeight = (int) context.getResources().getDimension(R.dimen.search_list_item_clip_height);
        clipWidth = (int) context.getResources().getDimension(R.dimen.search_list_item_clip_width);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        try {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(listItemLayout, parent, false);

                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            populateListItem(position, viewHolder);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return convertView;
    }

    /**
     * Sets the list when the list is updated. This will notify the adaptor with {@link #notifyDataSetChanged()}
     *
     * @param list {@link List<Product>}.
     */
    public void setList(List<Product> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
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

    private void populateListItem(int position, ViewHolder viewHolder) {
        Product product = list.get(position);

        if (product != null) {
            try {
                viewHolder.product = product;
                if (product.getTitle() != null)
                    viewHolder.titleText.setText(product.getTitle().toUpperCase());

                if ("movie".equals(product.getType()) || "series".equals(product.getType())) {
                    String extra = "";

                    if ("movie".equals(product.getType())) {
                        if (product.getDuration() != null && !product.getDuration().isEmpty())
                            extra = product.getDuration() + " " + getContext().getString(R.string.label_min);
                    } else {
                        if (product.getDuration() != null && !product.getDuration().isEmpty())
                            extra = product.getSeason();
                    }
                    populateListItemByType(product, viewHolder, extra, true, ImageURL.ImageProfile.POSTER, R.drawable.fallback_thumbnail, viewHolder.posterImage, posterWidth, posterHeight);
                } else if ("clip".equals(product.getType())) {
                    populateListItemByType(product, viewHolder, product.getSeason(), false, ImageURL.ImageProfile.CLIP, R.drawable.fallback_shortclip_thumbnail, viewHolder.clipImage, clipWidth, clipHeight);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    private void populateListItemByType(Product product, ViewHolder viewHolder, String extraText, boolean isPoster, ImageURL.ImageProfile imageProfile, int fallbackImageId, ImageView imageView, int width, int height) {
        if (extraText != null && !extraText.isEmpty())
            viewHolder.extraText.setText(extraText);
        else
            viewHolder.extraText.setVisibility(View.INVISIBLE);
        if (isPoster) {
            viewHolder.posterImage.setVisibility(View.VISIBLE);
            viewHolder.clipImage.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.posterImage.setVisibility(View.INVISIBLE);
            viewHolder.clipImage.setVisibility(View.VISIBLE);
        }

        picasso.with(getContext()).load(ViewUtils.getImageUrlOfProduct(getContext(), product, Page.SEARCH_PAGE, imageProfile))
                .resize(width, height)
                .onlyScaleDown()
                .noFade()
                .placeholder(fallbackImageId)
                .into(imageView);
    }

    /**
     * View holder for the list item in {@link SearchListAdapter}
     */
    public static class ViewHolder {

        @Bind(R.id.imageViewListItemSearchPoster)
        ImageView posterImage;
        @Bind(R.id.imageViewViewListItemSearchClip)
        ImageView clipImage;
        @Bind(R.id.textViewListItemTitle)
        TextView titleText;
        @Bind(R.id.textViewListItemExtra)
        TextView extraText;

        Product product;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
            clipImage.setVisibility(View.INVISIBLE);
        }

        public Product getProduct() {
            return product;
        }
    }
}
