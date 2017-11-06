package com.movideo.whitelabel.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.adapter.MovieListAdapter;
import com.movideo.whitelabel.adapter.MyLibrarySubPageAdapter;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.DeleteWishListItemsTask;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.util.DialogEventListeners;
import com.movideo.whitelabel.util.OnProductItemClickListener;
import com.movideo.whitelabel.util.PicassoHelper;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;
import com.movideo.whitelabel.view.MyLibraryProductGridViewGroup;
import com.movideo.whitelabel.view.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Generate My Library page as a {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnProductItemClickListener} interface to handle interaction events.
 * Use the {@link MyLibraryPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyLibraryPageFragment extends Fragment implements ViewPager.OnPageChangeListener, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    // the fragment initialization parameters
    private static final String ARG_LICENSE_TYPE = "my_library_license_type";

    @Bind(R.id.textViewMyLibrarySelect)
    TextView textSelected;
    @Bind(R.id.textViewMyLibraryTitle)
    TextView textTitle;
    @Bind(R.id.tabIndicatorMyLibrary)
    TabPageIndicator pageIndicator;
    @Bind(R.id.viewPagerMyLibrary)
    ViewPager viewPager;
    @Bind(R.id.layoutMyLibraryTabIndicator)
    LinearLayout layoutTab;
    @Bind(R.id.layoutMyLibraryDelete)
    RelativeLayout layoutDelete;

    int currentPage;
    private boolean selectingEnable;
    private LicenseType licenseType;
    private List<View> selectedList;
    private MyLibrarySubPageAdapter subPageAdapter;
    private OnProductItemClickListener listener;

    public MyLibraryPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param licenseType {@link LicenseType}
     * @return A new instance of fragment MyLibraryPageFragment.
     */
    public static MyLibraryPageFragment newInstance(LicenseType licenseType) {
        MyLibraryPageFragment fragment = new MyLibraryPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LICENSE_TYPE, licenseType);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            licenseType = (LicenseType) getArguments().getSerializable(ARG_LICENSE_TYPE);
        }
        selectingEnable = false;
        currentPage = 0;
        selectedList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_library_page, container, false);
        ButterKnife.bind(this, view);

        subPageAdapter = new MyLibrarySubPageAdapter(getContext(), this, this);

        viewPager.setAdapter(subPageAdapter);
        pageIndicator.setViewPager(viewPager);
        pageIndicator.setFooterIndicatorStyle(TabPageIndicator.IndicatorStyle.Underline);
        pageIndicator.setFooterIndicatorHeight(getResources().getDimension(R.dimen.my_library_tab_indicator_height));

        viewPager.addOnPageChangeListener(this);

        textSelected.setOnClickListener(getSelectOnClickListener());
        layoutDelete.setOnClickListener(getDeleteOnClickListener());
        return view;
    }

    @Override
    public void onDestroyView() {
        textSelected = null;
        pageIndicator = null;
        viewPager = null;
        layoutTab = null;
        layoutDelete = null;
        subPageAdapter = null;
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnProductItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnProductItemClickListener");
        }
    }

    @Override
    public void onDetach() {
        PicassoHelper.getInstance(getContext()).clearCache();
        listener = null;
        super.onDetach();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentPage = position;
        for (int i = 0; i < subPageAdapter.getCount(); i++) {
            MyLibraryProductGridViewGroup child = (MyLibraryProductGridViewGroup) viewPager.getChildAt(i);
            if (child != null)
                child.scrollToY(0);
        }
        layoutTab.setTranslationY(0);
        if (position != 0) {
            textSelected.setVisibility(View.GONE);
            layoutDelete.setVisibility(View.GONE);
            textSelected.setText(R.string.label_select);
            textTitle.setText(R.string.label_my_library);
        } else {
            textSelected.setVisibility(View.VISIBLE);
            if (selectingEnable) {
                textSelected.setText(R.string.label_cancel);
                textTitle.setText(R.string.label_select_items);
                layoutDelete.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        View childAt = view.getChildAt(0);

        int offset = (childAt == null) ? 0 : childAt.getTop() - view.getPaddingTop();

        layoutTab.setTranslationY(offset * 3);
        layoutTab.setAlpha((float) (Math.exp(offset / 15)));
    }

    private View.OnClickListener getSelectOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectingEnable) {
                    textSelected.setText(R.string.label_select);
                    textTitle.setText(R.string.label_my_library);
                    layoutDelete.setVisibility(View.GONE);
                    selectingEnable = false;

                    for (View view : selectedList) {
                        setItemSelected(view, false);
                    }
                    selectedList.clear();
                } else {
                    textSelected.setText(R.string.label_cancel);
                    textTitle.setText(R.string.label_select_items);
                    layoutDelete.setVisibility(View.VISIBLE);
                    selectingEnable = true;
                }
            }
        };
    }

    private View.OnClickListener getDeleteOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDeleteConfirmationDialog();
            }
        };
    }

    private void loadDeleteConfirmationDialog() {

        AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(getContext(), getContext().getString(R.string.dialog_msg_delete_confirm), null, getContext().getString(R.string.label_delete), getContext().getString(R.string.label_cancel), new DialogEventListeners() {
            @Override
            public void onPositiveButtonClick(DialogInterface dialog) {
                List<String> deletingIds = new ArrayList<>();

                for (View view : selectedList) {
                    deletingIds.add(String.valueOf(getProductFromView(view).getId()));
                }

                DeleteWishListItemsTask deleteWishListItemsTask = new DeleteWishListItemsTask(getContentRequestListener());
                deleteWishListItemsTask.execute(deletingIds);
                dialog.dismiss();
            }

            @Override
            public void onNegativeButtonClick(DialogInterface dialog) {
            }
        });
        dialogView.show();
    }

    private ContentRequestListener getContentRequestListener() {
        return new ContentRequestListener<List<Product>>() {

            @Override
            public void onRequestCompleted(List<Product> result) {
                textSelected.setText(R.string.label_select);
                textTitle.setText(R.string.label_my_library);
                layoutDelete.setVisibility(View.GONE);
                selectingEnable = false;

                MyLibraryProductGridViewGroup child = (MyLibraryProductGridViewGroup) viewPager.getChildAt(0);
                if (child != null)
                    child.loadPlaylist();

                for (View view : selectedList) {
                    setItemSelected(view, false);
                }
                selectedList.clear();
            }

            @Override
            public void onRequestFail(Throwable throwable) {
                Toast.makeText(getContext(), "Deleting wish list items failed", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        switch (currentPage) {
            case 0:
                if (selectingEnable) {
                    Object tag = view.getTag();

                    if (!(tag instanceof MovieListAdapter.ViewHolderItemExtra))
                        return;

                    MovieListAdapter.ViewHolderItemExtra viewHolder = (MovieListAdapter.ViewHolderItemExtra) tag;

                    if (viewHolder.isSelected()) {
                        setItemSelected(view, false);
                        selectedList.remove(view);
                    } else {
                        setItemSelected(view, true);
                        selectedList.add(view);
                    }
                } else {
                    notifyOnProductItemClick(view);
                }
                break;
            case 1:
                notifyOnProductItemClick(view);
                break;
            case 2:
                notifyOnProductItemClick(view);
                break;
        }
    }

    private void setItemSelected(View view, boolean selected) {
        Object tag = view.getTag();

        if (!(tag instanceof MovieListAdapter.ViewHolderItemExtra))
            return;

        MovieListAdapter.ViewHolderItemExtra viewHolder = (MovieListAdapter.ViewHolderItemExtra) tag;

        if (selected) {
            viewHolder.getSelectedIcon().setVisibility(View.VISIBLE);
            viewHolder.getSelectedBg().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getSelectedIcon().setVisibility(View.GONE);
            viewHolder.getSelectedBg().setVisibility(View.GONE);
        }
        viewHolder.setSelected(selected);
    }

    private void notifyOnProductItemClick(View view) {
        Product product = getProductFromView(view);

        listener.onItemClick(product);
    }

    private Product getProductFromView(View view) {
        Object tag = view.getTag();

        if (!(tag instanceof MovieListAdapter.ViewHolderItemExtra))
            return null;

        MovieListAdapter.ViewHolderItemExtra viewHolder = (MovieListAdapter.ViewHolderItemExtra) tag;
        return viewHolder.getProduct();
    }
}
