package com.movideo.whitelabel.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.product.Products;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.adapter.SeasonsExpandableListViewAdapter;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.model.ProductEpisodeAscendingComparator;
import com.movideo.whitelabel.model.SeasonsProduct;
import com.movideo.whitelabel.util.DisplayResources;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SeasonsExpandableListViewFragment extends Fragment {

    public static final String KEY_PRODUCT = "product";
    public static final String KEY_VARIANT = "variant";

    @Bind(R.id.seasonsExpandableListView)
    ExpandableListView mListView;

    private int groupItemHeight;
    private String entitledVariant;
    private List<Product> productList;
    private DisplayResources displayResources;
    private SeasonsExpandableListViewAdapter adapter;
    private SparseArray<SeasonsProduct> mGroups = null;

    /**
     * Empty public constructor
     */
    public SeasonsExpandableListViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param entitledVariant video quality entitle (SD/HD/4K).
     * @param products         {@link Products}.
     * @return A new instance of fragment SeasonsExpandableListViewFragment.
     */
    public static SeasonsExpandableListViewFragment newInstance(String entitledVariant, Products products) {
        SeasonsExpandableListViewFragment fragment = new SeasonsExpandableListViewFragment();
        Bundle args = new Bundle();
        args.putString(KEY_VARIANT, entitledVariant);
        args.putSerializable(KEY_PRODUCT, products);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productList = ((Products) getArguments().getSerializable(KEY_PRODUCT)).getProductList();
            entitledVariant = getArguments().getString(KEY_VARIANT);

//            if (productList != null)
//                Collections.sort(productList, new ProductEpisodeAscendingComparator());
        }
        mGroups = new SparseArray<SeasonsProduct>();
        displayResources = new DisplayResources(getActivity());
        groupItemHeight = displayResources.dpToPx((int) getActivity().getResources().getDimension(R.dimen.expandable_list_view_group_header_height));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seasons_expandable_list_view, container, false);
        ButterKnife.bind(this, view);

        loadSeasonsProduct();
        return view;
    }

    /*Seasons product item*/
    private void loadSeasonsProduct() {
        for (int j = 0; j < productList.size(); j++) {
            SeasonsProduct seasonsProduct = new SeasonsProduct();
            seasonsProduct.setTitle(productList.get(j).getTitle());
            seasonsProduct.setSeasonsProductSubItem(productList.get(j));



            mGroups.append(j, seasonsProduct);
        }

        adapter = new SeasonsExpandableListViewAdapter(getActivity(), mGroups, mListView, entitledVariant);
        mListView.setAdapter(adapter);
        setListViewHeight(mListView);
        mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true;
            }
        });
    }

    private void setListViewHeight(ExpandableListView listView) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            totalHeight += groupItemHeight;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (displayResources.dpToPx(1) * (listAdapter.getGroupCount() - 2));
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = height;
        } else {
            params.height = height / 2;
        }

        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public void setEntitledVariant(String entitledVariant) {
        this.entitledVariant = entitledVariant;
        if(adapter != null) adapter.setEntitledVariant(entitledVariant);
    }
}
