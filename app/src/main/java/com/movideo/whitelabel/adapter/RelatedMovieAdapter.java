package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RelatedMovieAdapter extends RecyclerView.Adapter<RelatedMovieAdapter.RelatedProductViewHolder> {

    private List<Product> relatedMovieItems = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;

    public RelatedMovieAdapter(Context context, List<Product> relatedMovieItems) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.relatedMovieItems = relatedMovieItems;
    }

    @Override
    public RelatedProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.related_movie_list_item, parent, false);
        RelatedProductViewHolder holder = new RelatedProductViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(RelatedProductViewHolder holder, int position) {
        Product product = relatedMovieItems.get(position);
        holder.listItemTextView.setText(product.getTitle());
         /*set product image */
        Picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, product, Page.MOVIE_DETAIL_PAGE, ImageURL.ImageProfile.POSTER))
                .resizeDimen(R.dimen.list_item_imageview_width, R.dimen.list_item_imageview_height)
                .onlyScaleDown()
                .noFade()
                .placeholder(R.drawable.fallback_thumbnail)
                .centerCrop()
                .into(holder.listItemImageView);
    }

    @Override
    public int getItemCount() {
        return relatedMovieItems.size();
    }

    class RelatedProductViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.listItemTextView)
        TextView listItemTextView;
        @Bind(R.id.listItemImageView)
        ImageView listItemImageView;

        public RelatedProductViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            listItemImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
}
