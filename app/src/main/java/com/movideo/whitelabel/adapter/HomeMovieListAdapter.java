package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movideo.baracus.model.playlist.Playlist;
import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.WhiteLabelApplication;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.PicassoHelper;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Handles list of movies and populate {@link RecyclerView}.
 */
public class HomeMovieListAdapter extends RecyclerView.Adapter<HomeMovieListAdapter.ProductViewHolder> {

    private static final String TAG = HomeMovieListAdapter.class.getSimpleName();

    private final Picasso picasso;

    private int height;
    private int width;
    private Context context;
    private int listItemLayout;
    private LicenseType licenseType;
    private List<Product> list;
    private Playlist playlist;
    /**
     * Default constructor.
     *
     * @param context  {@link Context}
     * @param resource Id of the list item layout.
     * @param playlist     the playlist to show
     */
    public HomeMovieListAdapter(Context context, int resource, Playlist playlist) {
        this.playlist = playlist;
        this.list = playlist.getProductList();
        this.context = context;
        this.listItemLayout = resource;
        //this.list = list;
        this.licenseType = WhiteLabelApplication.getInstance().getLicenseType();
        picasso = PicassoHelper.getInstance(context).getPicasso();

        height = (int) context.getResources().getDimension(R.dimen.horizontal_list_item_image_height);
        width = (int) context.getResources().getDimension(R.dimen.horizontal_list_item_image_width);
        if (playlist.getExtend()!=null){
            this.list.add(null);
        }
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
        Product product = list.get(position);
        if (product != null) {
            holder.playlist = null;
            holder.itemExtend.setVisibility(View.GONE);
            holder.product = product;
            holder.itemTitle.setText(product.getTitle());

            String urlImage = ViewUtils.getImageUrlOfProduct(context, product, Page.HOME_PAGE, ImageURL.ImageProfile.POSTER);
            picasso.with(context).load(urlImage)
                    .resize(width, height)
                    .onlyScaleDown()
                    .noFade()
                    .placeholder(R.drawable.fallback_thumbnail)
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
        else{
            holder.product = null;
            holder.playlist = this.playlist;
            Log.d("PLaylist position",String.valueOf(position));
            holder.itemExtend.setVisibility(View.VISIBLE);
            //refresh the holder
            holder.itemImage.setImageDrawable(null);
            holder.itemTitle.setText("");
            holder.itemTitle.setHint("");
//            holder.itemTitle.setText("XEM THÊM");
//
//            holder.itemTitle.setGravity(Gravity.CENTER);
//            Log.d("itemTitle",String.valueOf(holder.itemTitle.getLayoutDirection()));
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

        @Bind(R.id.imageViewHorizontalListItem)
        ImageView itemImage;
        @Bind(R.id.textViewHorizontalListItem)
        TextView itemTitle;
        @Bind(R.id.layoutHorizontalListItem)
        RelativeLayout layout;
        @Bind(R.id.textViewExtend)
        TextView itemExtend;

        boolean selected;
        Product product;
        Playlist playlist;
        public ProductViewHolder(View view) {
            super(view);
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
         * Returns playlist associate with the item.
         *
         * @return {@link Playlist}
         */
        public Playlist getPlaylist() {
            return playlist;
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
}
