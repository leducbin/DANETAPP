package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Handles list of short clips and populate {@link RecyclerView}.
 */
public class HomeClipsListAdapter extends RecyclerView.Adapter<HomeClipsListAdapter.ProductViewHolder> {

    private static final String TAG = HomeClipsListAdapter.class.getSimpleName();

    private final Picasso picasso;

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
    public HomeClipsListAdapter(Context context, int resource, List<Product> list) {

        this.context = context;
        this.listItemLayout = resource;
        this.list = list;
        this.licenseType = WhiteLabelApplication.getInstance().getLicenseType();
        picasso = PicassoHelper.getInstance(context).getPicasso();

        height = (int) context.getResources().getDimension(R.dimen.horizontal_clips_list_item_image_height);
        width = (int) context.getResources().getDimension(R.dimen.horizontal_clips_list_item_image_width);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(listItemLayout, parent, false);
        ProductViewHolder holder = new ProductViewHolder(view);
        view.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        try {
            Product product = list.get(position);

            if (product != null) {
                holder.product = product;
                holder.itemTitle.setText(product.getTitle());
                holder.itemDuration.setText(product.getDuration());

                ImageURL.ImageProfile profile = ImageURL.ImageProfile.CLIP;
                if (height < width){
                    profile = ImageURL.ImageProfile.BACKGROUND;
                }

                picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, product, Page.HOME_PAGE, profile))
                        .resize(width, height)
                        .onlyScaleDown()
                        .noFade()
                        .placeholder(R.drawable.fallback_shortclip_thumbnail)
                        .into(holder.itemImage);

                switch (licenseType) {

                    case AVOD:
                        holder.layout.setBackgroundResource(R.drawable.border_green);
                        break;
                    case SVOD:
                        holder.layout.setBackgroundResource(R.drawable.border_blue);
                        break;
                    case TVOD:
                        holder.layout.setBackgroundResource(R.drawable.border_red);
                        break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * View holder for the list item in this list view
     */
    public class ProductViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.imageViewHorizontalClipsListItem)
        ImageView itemImage;
        @Bind(R.id.textViewHorizontalClipsListItem)
        TextView itemTitle;
        @Bind(R.id.textViewHorizontalClipsListItemDuration)
        TextView itemDuration;
        @Bind(R.id.textViewHorizontalClipsListItemMin)
        TextView itemMinLabel;
        @Bind(R.id.layoutHorizontalClipsListItem)
        RelativeLayout layout;
        Product product;

        public ProductViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public Product getProduct() {
            return product;
        }
    }
}
