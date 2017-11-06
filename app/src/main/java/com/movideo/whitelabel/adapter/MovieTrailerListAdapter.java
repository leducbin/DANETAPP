package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.WhiteLabelApplication;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.PicassoHelper;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Handles list of movies and populate {@link RecyclerView}.
 */
public class MovieTrailerListAdapter extends ArrayAdapter<Product> {

    private static final String TAG = HomeMovieListAdapter.class.getSimpleName();

    private final Picasso picasso;

    private boolean isClips;
    private int height;
    private int width;
    private Context context;
    private int listItemLayout;
    private LicenseType licenseType;
    private List<Product> list;

    /**
     * Default constructor.
     *
     * @param context  {@link Context}
     * @param resource Id of the list item layout.
     * @param list     List of {@link Product}.
     */
    public MovieTrailerListAdapter(Context context, int resource, List<Product> list, boolean isClips) {
        super(context, resource, list);
        this.context = context;
        this.listItemLayout = resource;
        this.list = list;
        this.licenseType = WhiteLabelApplication.getInstance().getLicenseType();
        this.isClips = isClips;
        picasso = PicassoHelper.getInstance(context).getPicasso();

        if (isClips) {
            height = (int) context.getResources().getDimension(R.dimen.movie_trailer_list_item_clip_height);
            width = (int) context.getResources().getDimension(R.dimen.movie_trailer_list_item_clip_width);
        } else {
            height = (int) context.getResources().getDimension(R.dimen.movie_trailer_list_item_height);
            width = (int) context.getResources().getDimension(R.dimen.movie_trailer_list_item_width);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ProductViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(listItemLayout, parent, false);

            holder = new ProductViewHolder(convertView);
            if (isClips) {
                holder.itemImageClip = (ImageView) convertView.findViewById(R.id.imageViewMovieTrailerClipsListItem);
                holder.itemTitleClip = (TextView) convertView.findViewById(R.id.textViewMovieTrailerClipsListItemTitle);
                holder.itemDurationClip = (TextView) convertView.findViewById(R.id.textViewMovieTrailerClipsListItemDuration);
            } else {
                holder.itemImage = (ImageView) convertView.findViewById(R.id.imageViewMovieTrailerListItem);
                holder.itemTitle = (TextView) convertView.findViewById(R.id.textViewMovieTrailerListItemTitle);
            }
            convertView.setTag(holder);
        } else {
            holder = (ProductViewHolder) convertView.getTag();
        }
        Product product = list.get(position);

        if (product != null) {
            try {
                holder.product = product;

                if (isClips) {
                    holder.itemTitleClip.setText(product.getTitle().toUpperCase());
                    holder.itemDurationClip.setText(product.getDuration());
                    picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, product, Page.MOVIE_TRAILER, ImageURL.ImageProfile.CLIP))
                            .resize(width, height)
                            .onlyScaleDown()
                            .noFade()
                            .placeholder(R.drawable.fallback_shortclip_thumbnail)
                            .into(holder.itemImageClip);
                } else {
                    holder.itemTitle.setText(product.getTitle().toUpperCase());
                    picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, product, Page.MOVIE_TRAILER, ImageURL.ImageProfile.POSTER))
                            .resize(width, height)
                            .onlyScaleDown()
                            .noFade()
                            .placeholder(R.drawable.fallback_thumbnail)
                            .into(holder.itemImage);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
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

    /**
     * View holder for poster the list item in this list view
     */
    public class ProductViewHolder extends RecyclerView.ViewHolder {

        //        @Bind(R.id.imageViewMovieTrailerListItem)
        ImageView itemImage;
        //        @Bind(R.id.textViewMovieTrailerListItemTitle)
        TextView itemTitle;
        //        @Bind(R.id.imageViewMovieTrailerClipsListItem)
        ImageView itemImageClip;
        //        @Bind(R.id.textViewMovieTrailerClipsListItemTitle)
        TextView itemTitleClip;
        //        @Bind(R.id.textViewMovieTrailerClipsListItemDuration)
        TextView itemDurationClip;

        Product product;

        public ProductViewHolder(View view) {
            super(view);
//            ButterKnife.bind(this, view);
        }

        /**
         * Returns product associate with the item.
         *
         * @return {@link Product}
         */
        public Product getProduct() {
            return product;
        }
    }
}
