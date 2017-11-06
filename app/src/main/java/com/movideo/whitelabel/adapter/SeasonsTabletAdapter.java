package com.movideo.whitelabel.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.model.ProductEpisodeAscendingComparator;
import com.movideo.whitelabel.util.DisplayResources;
import com.movideo.whitelabel.util.PlaybackHandler;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SeasonsTabletAdapter extends RecyclerView.Adapter<SeasonsTabletAdapter.RelatedProductViewHolder> {

    private int width;
    private int height;
    private String entitledVariant;
    private Context context;
    private LayoutInflater inflater;
    private DisplayResources displayResources;
    private List<OverlayDeselectListener> overlayDeselectListeners;
    private List<Product> relatedClipItems = Collections.emptyList();

    public SeasonsTabletAdapter(Context context, List<Product> relatedClipItems, String entitledVariant) {
        this.context = context;
        this.entitledVariant = entitledVariant;
        inflater = LayoutInflater.from(context);
        this.relatedClipItems = relatedClipItems;
        //Collections.sort(this.relatedClipItems, new ProductEpisodeAscendingComparator());
        displayResources = new DisplayResources(context);
        width = displayResources.dpToPx((int) context.getResources().getDimension(R.dimen.horizontal_clips_list_item_image_width));
        height = displayResources.dpToPx((int) context.getResources().getDimension(R.dimen.horizontal_clips_list_item_image_height));
        overlayDeselectListeners = new ArrayList<>();
    }

    @Override
    public RelatedProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_horizontal_seasons_tablet_list, parent, false);
        RelatedProductViewHolder holder = new RelatedProductViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(final RelatedProductViewHolder holder, int position) {
        final Product currentItem = relatedClipItems.get(position);
        int number = position + 1;

        if (currentItem.getTitle() != null && !"".equals(currentItem.getTitle())) {
            holder.itemTitle.setText(number + ". " + currentItem.getTitle());
            holder.itemTitleOverlay.setText(number + ". " + currentItem.getTitle());
        } else {
            holder.itemTitle.setText("");
            holder.itemTitleOverlay.setText("");
        }
        if (currentItem.getDescription() != null && !"".equals(currentItem.getDescription())) {
            holder.itemDescriptionOverlay.setText(currentItem.getDescription());
        } else {
            holder.itemDescriptionOverlay.setText("");
        }

        Picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, currentItem, Page.CLIP_DETAIL_PAGE, ImageURL.ImageProfile.POSTER))
                .resize(width, height)
                .onlyScaleDown()
                .noFade()
                .placeholder(R.drawable.fallback_shortclip_thumbnail)
                .centerCrop()
                .into(holder.itemImage);

        holder.itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (OverlayDeselectListener listener : overlayDeselectListeners) {
                    listener.onOverlayDeselect();
                }

                holder.playImageButton.setVisibility(View.INVISIBLE);
                holder.itemTitle.setVisibility(View.INVISIBLE);
                holder.overlayLayout.setVisibility(View.VISIBLE);
                holder.setOverlaySelected(true);
            }
        });

        holder.playImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackHandler.play((Activity) context, currentItem.getId(), entitledVariant);
            }
        });

        holder.playImageButtonOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackHandler.play((Activity) context, currentItem.getId(), entitledVariant);
            }
        });

        holder.overlayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.playImageButton.setVisibility(View.VISIBLE);
                holder.itemTitle.setVisibility(View.VISIBLE);
                holder.overlayLayout.setVisibility(View.GONE);
                holder.setOverlaySelected(false);
            }
        });
        holder.overlayLayout.setVisibility(View.GONE);

        if (holder.getOverlayDeselectListener() == null) {
            OverlayDeselectListener listener = new OverlayDeselectListener() {
                @Override
                public void onOverlayDeselect() {
                    if (holder.isOverlaySelected()) {
                        holder.playImageButton.setVisibility(View.VISIBLE);
                        holder.itemTitle.setVisibility(View.VISIBLE);
                        holder.overlayLayout.setVisibility(View.GONE);
                        holder.setOverlaySelected(false);
                    }
                }
            };

            holder.setOverlayDeselectListener(listener);
            overlayDeselectListeners.add(listener);
        }
    }

    @Override
    public int getItemCount() {
        return relatedClipItems.size();
    }

    public void setEntitledVariant(String entitledVariant) {
        this.entitledVariant = entitledVariant;
    }

    interface OverlayDeselectListener {

        void onOverlayDeselect();
    }

    class RelatedProductViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.imageViewHorizontalClipsListItem)
        ImageView itemImage;
        @Bind(R.id.textViewEpisodeListItemTitle)
        TextView itemTitle;
        @Bind(R.id.imageButtonEpisodeListItemPlay)
        ImageButton playImageButton;
        @Bind(R.id.layoutEpisodeListItemOverlay)
        RelativeLayout overlayLayout;
        @Bind(R.id.textViewEpisodeListItemTitleOverlay)
        TextView itemTitleOverlay;
        @Bind(R.id.textViewEpisodeListItemDescriptionOverlay)
        TextView itemDescriptionOverlay;
        @Bind(R.id.imageButtonEpisodeListItemPlayOverlay)
        ImageButton playImageButtonOverlay;

        private boolean overlaySelected;
        private OverlayDeselectListener overlayDeselectListener;

        public RelatedProductViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            overlaySelected = false;
        }

        public OverlayDeselectListener getOverlayDeselectListener() {
            return overlayDeselectListener;
        }

        public void setOverlayDeselectListener(OverlayDeselectListener overlayDeselectListener) {
            this.overlayDeselectListener = overlayDeselectListener;
        }

        public boolean isOverlaySelected() {
            return overlaySelected;
        }

        public void setOverlaySelected(boolean overlaySelected) {
            this.overlaySelected = overlaySelected;
        }
    }
}

