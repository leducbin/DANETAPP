package com.movideo.whitelabel.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.model.SeasonsProduct;
import com.movideo.whitelabel.util.DisplayResources;
import com.movideo.whitelabel.util.PlaybackHandler;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SeasonsExpandableListViewAdapter extends BaseExpandableListAdapter implements CompoundButton.OnCheckedChangeListener {


    private int width;
    private int height;
    private int groupItemHeight;
    private int listHeightSetValue;
    private String entitledVariant;
    private Context context;
    private ExpandableListView mListView;
    private DisplayResources displayResources;
    private SparseArray<SeasonsProduct> mGroups = null;

    public SeasonsExpandableListViewAdapter(Context context, SparseArray<SeasonsProduct> groups, ExpandableListView mListView, String entitledVariant) {
        this.context = context;
        this.mGroups = groups;
        this.mListView = mListView;
        this.entitledVariant = entitledVariant;
        displayResources = new DisplayResources(context);
        width = displayResources.getDisplayWidth();
        height = displayResources.dpToPx((int) context.getResources().getDimension(R.dimen.expandable_list_view_sub_item_image_height));
        groupItemHeight = displayResources.dpToPx((int) context.getResources().getDimension(R.dimen.expandable_list_view_group_header_height));

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            listHeightSetValue = 1;
        } else {
            listHeightSetValue = 2;
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroups.get(groupPosition).getSeasonsProductSubItem();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final Product seasonsProductSubItem = (Product) getChild(groupPosition, childPosition);

        final SubItemViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.seasons_list_view_sub_item, parent, false);

            viewHolder = new SubItemViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SubItemViewHolder) convertView.getTag();
        }
        if (seasonsProductSubItem.getDescription() != null && !seasonsProductSubItem.getDescription().equals("null"))
            viewHolder.descTextView.setText(seasonsProductSubItem.getDescription());
        else if (seasonsProductSubItem.getTitle() != null && !seasonsProductSubItem.getTitle().equals("null"))
            viewHolder.descTextView.setText(seasonsProductSubItem.getTitle());
        else
            viewHolder.descTextView.setText("");

        Picasso.with(context).load(ViewUtils.getImageUrlOfProduct(context, seasonsProductSubItem, Page.SHOW_DETAIL_PAGE, ImageURL.ImageProfile.POSTER))
                .resize(width, height)
                .onlyScaleDown()
                .noFade()
                .placeholder(R.drawable.fallback_thumbnail)
                .centerCrop()
                .into(viewHolder.thumbImageView);

        viewHolder.playImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackHandler.play((Activity) context, seasonsProductSubItem.getParentProduct().getId(), entitledVariant, seasonsProductSubItem.getId(), null, seasonsProductSubItem.getTitle());
            }
        });
        return convertView;
    }

    public void setEntitledVariant(String entitledVariant) {
        this.entitledVariant = entitledVariant;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final GroupItemViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.seasons_list_view_group_item, parent, false);

            viewHolder = new GroupItemViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GroupItemViewHolder) convertView.getTag();
        }
        final SeasonsProduct seasonsProduct = (SeasonsProduct) getGroup(groupPosition);

        if (seasonsProduct.getTitle() != null && !seasonsProduct.getTitle().equals("null"))
            viewHolder.titleTextView.setText(groupPosition + 1 + ". " + seasonsProduct.getTitle());
        else
            viewHolder.titleTextView.setText(groupPosition + 1 + ".");

        viewHolder.itemToggleButton.setOnCheckedChangeListener(this);
        viewHolder.itemToggleButton.setTag(groupPosition);
        viewHolder.itemToggleButton.setChecked(isExpanded);

        viewHolder.playImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackHandler.play((Activity) context, seasonsProduct.getSeasonsProductSubItem().getParentProduct().getId(), entitledVariant, seasonsProduct.getSeasonsProductSubItem().getId(), null, seasonsProduct.getSeasonsProductSubItem().getTitle());
            }
        });

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int groupPosition;
        LinearLayout linearLayout;
        groupPosition = Integer.parseInt(buttonView.getTag().toString());
        linearLayout = (LinearLayout) buttonView.getParent();
        if (isChecked) {
            for (int i = 0; i < mListView.getExpandableListAdapter().getGroupCount(); i++) {
                if (i == groupPosition) {
                    mListView.expandGroup(i);
                    linearLayout.findViewById(R.id.seasonListViewItemPlayIcon).setVisibility(View.INVISIBLE);
                } else {
                    if (mListView.isGroupExpanded(i)) {
                        mListView.collapseGroup(i);
                        linearLayout.findViewById(R.id.seasonListViewItemPlayIcon).setVisibility(View.VISIBLE);
                    }
                }

            }

        } else {
            mListView.collapseGroup(groupPosition);
            linearLayout.findViewById(R.id.seasonListViewItemPlayIcon).setVisibility(View.VISIBLE);
        }

        setListViewHeight(mListView, groupPosition);

    }

    private void setListViewHeight(ExpandableListView listView,
                                   int group) {
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < this.getGroupCount(); i++) {
            totalHeight += groupItemHeight;
            if (((listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < this.getChildrenCount(i); j++) {
                    View listItem = this.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                    totalHeight += listItem.getMeasuredHeight() * listHeightSetValue;
                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (displayResources.dpToPx(1) * (this.getGroupCount() - 2));
        params.height = height / listHeightSetValue;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /**
     * View holder for the list sub item in this list view
     */
    public static class SubItemViewHolder {
        @Bind(R.id.seasonListViewSubItemThumbImageView)
        ImageView thumbImageView;
        @Bind(R.id.seasonListViewSubItemPlayImageButton)
        ImageButton playImageButton;
        @Bind(R.id.seasonListViewSubItemDescTextView)
        TextView descTextView;

        public SubItemViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    /**
     * View holder for the list group item in this list view
     */
    public static class GroupItemViewHolder {
        @Bind(R.id.seasonListViewItemToggleButton)
        ToggleButton itemToggleButton;
        @Bind(R.id.seasonListViewItemPlayIcon)
        ImageView playImageButton;
        @Bind(R.id.seasonListViewItemTextView)
        TextView titleTextView;

        public GroupItemViewHolder(View view) {
            ButterKnife.bind(this, view);

        }
    }
}