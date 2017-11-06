package com.movideo.whitelabel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movideo.whitelabel.R;
import com.movideo.whitelabel.model.MenuItem;

import java.util.HashMap;
import java.util.List;

/**
 * Helps to populate drawer menu.
 */
public class MenuItemAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<MenuItem> menuItems;
    private HashMap<String, List<MenuItem>> subMenuItems;

    /**
     * Constructor with arguments.
     *
     * @param context      {@link Context}
     * @param menuItems    {@link List<MenuItem>}
     * @param subMenuItems {@link HashMap<String, List<MenuItem>>}
     */
    public MenuItemAdapter(Context context, List<MenuItem> menuItems, HashMap<String, List<MenuItem>> subMenuItems) {
        this.context = context;
        this.menuItems = menuItems;
        this.subMenuItems = subMenuItems;
    }

    @Override
    public int getGroupCount() {
        return menuItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (subMenuItems.containsKey(menuItems.get(groupPosition).getId())) {
            return subMenuItems.get(menuItems.get(groupPosition).getId()).size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return menuItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return subMenuItems.get(menuItems.get(groupPosition).getId()).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return subMenuItems.containsKey(menuItems.get(groupPosition).getId()) && subMenuItems.get(menuItems.get(groupPosition).getId()).size() > childPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.list_item_menu_normal, parent, false);

            viewHolder.titleText = (TextView) convertView.findViewById(R.id.textViewListItemMenuNormalTitle);
            viewHolder.expandIndicator = (ImageView) convertView.findViewById(R.id.imageViewExpandIndicator);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MenuItem menuItem = (MenuItem) getGroup(groupPosition);

        viewHolder.titleText.setText(menuItem.getTitle());

        if (getChildrenCount(groupPosition) == 0) {
            viewHolder.expandIndicator.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.expandIndicator.setVisibility(View.VISIBLE);
            viewHolder.expandIndicator.setImageResource(isExpanded ? R.drawable.icon_chevron_up : R.drawable.icon_chevron_down);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.list_item_sub_menu, parent, false);

            viewHolder.titleText = (TextView) convertView.findViewById(R.id.textViewListItemSubMenulTitle);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MenuItem menuItem = (MenuItem) getChild(groupPosition, childPosition);

        viewHolder.titleText.setText(menuItem.getTitle());

        return convertView;
    }

    /**
     * Set new menu item list.
     *
     * @param newMenuItems    {@link List<MenuItem>}
     * @param newSubMenuItems {@link HashMap<String, List<MenuItem>>}
     */
    public void setNewMenuItemList(List<MenuItem> newMenuItems, HashMap<String, List<MenuItem>> newSubMenuItems) {
        menuItems.clear();
        menuItems.addAll(newMenuItems);

        subMenuItems.clear();
        subMenuItems.putAll(newSubMenuItems);

        notifyDataSetChanged();
    }

    /**
     * View holder of this adapter.
     */
    class ViewHolder {

        TextView titleText;
        ImageView expandIndicator;
    }
}
