package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.ClipDetailsActivity;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.DisplayResources;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShortClipsFragmentAdapter extends RecyclerView.Adapter<ShortClipsFragmentAdapter.MediaListViewHolder> {

    private List<Product> relatedMediaItems;
    private Context context;
    private DisplayResources displayResources;
    private int width;
    private int height;

    public ShortClipsFragmentAdapter(Context context, List<Product> relatedMediaItems) {
        this.relatedMediaItems = relatedMediaItems;
        this.context = context;
        displayResources = new DisplayResources(context);
        width = displayResources.getDisplayWidth();
        height = displayResources.dpToPx((int) context.getResources().getDimension(R.dimen.show_item_height));
    }

    @Override
    public MediaListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.related_fragment_show_recycler_item, parent, false);
        return new MediaListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MediaListViewHolder holder, final int position) {

        Product product = relatedMediaItems.get(position);
        if (product.getTitle() != null && !product.getTitle().equals("null"))
            holder.textViewHorizontalClipsListItem.setText(product.getTitle());
        else
            holder.textViewHorizontalClipsListItem.setText("");

        if (product.getDuration() != null && !product.getDuration().equals("null"))
            holder.textViewHorizontalClipsListItemDuration.setText(Float.toString(Float.parseFloat(product.getDuration())));
        else
            holder.textViewHorizontalClipsListItemDuration.setText(Float.toString(Float.parseFloat("0")));

        Picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, product, Page.CLIP_DETAIL_PAGE, ImageURL.ImageProfile.POSTER))
                .resize(width, height)
                .onlyScaleDown()
                .noFade()
                .placeholder(R.drawable.fallback_thumbnail)
                .centerCrop()
                .into(holder.imageViewHorizontalClipsListItem);

        holder.imageViewHorizontalClipsListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ClipDetailsActivity.class);
                intent.putExtra("product", relatedMediaItems.get(position));
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return relatedMediaItems.size();
    }


    class MediaListViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.imageViewHorizontalClipsListItem)
        ImageView imageViewHorizontalClipsListItem;

        @Bind(R.id.textViewHorizontalClipsListItem)
        TextView textViewHorizontalClipsListItem;

        @Bind(R.id.textViewHorizontalClipsListItemDuration)
        TextView textViewHorizontalClipsListItemDuration;

        @Bind(R.id.textViewHorizontalClipsListItemMin)
        TextView textViewHorizontalClipsListItemMin;

        public MediaListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}