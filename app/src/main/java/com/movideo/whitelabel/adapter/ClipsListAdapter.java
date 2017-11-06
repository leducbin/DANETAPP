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
 * Handles list of short clips and populate ListView.
 */
public class ClipsListAdapter extends ArrayAdapter<Product> {

    private static final String TAG = ClipsListAdapter.class.getSimpleName();

    private final Picasso picasso;

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
    public ClipsListAdapter(Context context, int resource, List<Product> list) {
        super(context, resource, list);

        this.context = context;
        this.listItemLayout = resource;
        this.list = list;
        picasso = PicassoHelper.getInstance(context).getPicasso();

        height = (int) context.getResources().getDimension(R.dimen.horizontal_clips_list_item_image_height);
        width = (int) context.getResources().getDimension(R.dimen.horizontal_clips_list_item_image_width);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolderItem viewHolder;
        try {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(listItemLayout, parent, false);

                viewHolder = new ViewHolderItem(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            Product product = list.get(position);

            if (product != null) {
                viewHolder.product = product;
                viewHolder.itemTitle.setText(product.getTitle());
                viewHolder.itemDuration.setText(product.getDuration());
                picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, product, Page.HOME_PAGE, ImageURL.ImageProfile.CLIP))
                        .resize(width, height)
                        .onlyScaleDown()
                        .noFade()
                        .placeholder(R.drawable.fallback_shortclip_thumbnail)
                        .into(viewHolder.itemImage);
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
     * @param list List of data based from only two types mention in the class description.
     */
    public void setList(List<Product> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * View holder for the list item in this list view
     */
    public static class ViewHolderItem {

        @Bind(R.id.imageViewHorizontalClipsListItem)
        ImageView itemImage;
        @Bind(R.id.textViewHorizontalClipsListItem)
        TextView itemTitle;
        @Bind(R.id.textViewHorizontalClipsListItemDuration)
        TextView itemDuration;
        @Bind(R.id.textViewHorizontalClipsListItemMin)
        TextView itemMinLabel;
        Product product;

        public ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }

        public Product getProduct() {
            return product;
        }
    }
}
