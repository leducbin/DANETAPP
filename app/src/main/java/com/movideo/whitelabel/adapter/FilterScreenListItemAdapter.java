package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.movideo.whitelabel.R;
import com.movideo.whitelabel.model.FilterItem;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FilterScreenListItemAdapter extends RecyclerView.Adapter<FilterScreenListItemAdapter.ViewHolder> {

    private List<FilterItem> listData = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;


    public FilterScreenListItemAdapter(Context context, List<FilterItem> listData) {
        this.context = context;
        this.listData = listData;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.filter_country_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FilterItem filterItem = listData.get(position);
        holder.countryFilterTextView.setText(Html.fromHtml(filterItem.getTitle()));
        holder.countryFilterTextView.setSelected(filterItem.getIsSelected());

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.countryFilterTextView)
        TextView countryFilterTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}