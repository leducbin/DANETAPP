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
import com.movideo.whitelabel.util.DisplayResources;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RelatedClipAdapter extends RecyclerView.Adapter<RelatedClipAdapter.RelatedProductViewHolder> {

    private List<Product> relatedClipItems = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;
    private DisplayResources displayResources;
    private int width;
    private int height;


    public RelatedClipAdapter(Context context, List<Product> relatedClipItems) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.relatedClipItems = relatedClipItems;
        displayResources = new DisplayResources(context);
        width = displayResources.dpToPx((int) context.getResources().getDimension(R.dimen.horizontal_clips_list_item_image_width));
        height = displayResources.dpToPx((int) context.getResources().getDimension(R.dimen.horizontal_clips_list_item_image_height));

    }

    @Override
    public RelatedProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_horizontal_clips_list, parent, false);
        RelatedProductViewHolder holder = new RelatedProductViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(RelatedProductViewHolder holder, int position) {
        Product currentItem = relatedClipItems.get(position);

        if (currentItem.getTitle() != null && !currentItem.getTitle().equals(""))
            holder.itemTitle.setText(currentItem.getTitle());
        else
            holder.itemTitle.setText(" ");
        if (currentItem.getDuration() != null && !currentItem.getDuration().equals(""))
            holder.itemDuration.setText(Float.toString(Float.parseFloat(currentItem.getDuration())));
        else
            holder.itemDuration.setText(Float.toString(Float.parseFloat("0")));

        Picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, currentItem, Page.CLIP_DETAIL_PAGE, ImageURL.ImageProfile.POSTER))
                .resize(width, height)
                .onlyScaleDown()
                .noFade()
                .placeholder(R.drawable.fallback_shortclip_thumbnail)
                .centerCrop()
                .into(holder.itemImage);
    }

    @Override
    public int getItemCount() {
        return relatedClipItems.size();
    }

    class RelatedProductViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.imageViewHorizontalClipsListItem)
        ImageView itemImage;
        @Bind(R.id.textViewHorizontalClipsListItem)
        TextView itemTitle;
        @Bind(R.id.textViewHorizontalClipsListItemDuration)
        TextView itemDuration;
        @Bind(R.id.textViewHorizontalClipsListItemMin)
        TextView itemMinLabel;

        public RelatedProductViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

