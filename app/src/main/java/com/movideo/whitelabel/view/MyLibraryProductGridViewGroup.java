package com.movideo.whitelabel.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.movideo.baracus.model.product.Offerings;
import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.adapter.MovieListAdapter;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetUserRentedItemsTask;
import com.movideo.whitelabel.communication.GetWishListTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Contains the view group to generate My Library single tab.
 */
public class MyLibraryProductGridViewGroup extends RelativeLayout implements ContentRequestListener<List<Product>> {

    private static final String TAG = MyLibraryProductGridViewGroup.class.getSimpleName();

    @Bind(R.id.gridViewMyLibrary)
    GridView gridView;

    private int page;
    private int limit;
    private PageType pageType;
    private List<Product> products;
    private MovieListAdapter adapter;
    private AbsListView.OnScrollListener listener;
    private AdapterView.OnItemClickListener itemClickListener;

    public MyLibraryProductGridViewGroup(Context context, PageType pageType, AbsListView.OnScrollListener listener, AdapterView.OnItemClickListener itemClickListener) {
        super(context);

        this.pageType = pageType;
        this.listener = listener;
        this.itemClickListener = itemClickListener;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.layout_my_library_grid_content, this, true);

        page = 1;
        limit = 10;

        ButterKnife.bind(this);

        //loadCollection();
        loadPlaylist();
        populateView();
    }

    private void populateView() {
        if (PageType.RENTED.equals(pageType)) {
            adapter = new MovieListAdapter(getContext(), R.layout.list_item_my_library, new ArrayList<Product>(), true);
        } else {
            adapter = new MovieListAdapter(getContext(), R.layout.list_item_my_library, new ArrayList<Product>());
        }

        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(listener);
        gridView.setOnItemClickListener(itemClickListener);
    }

    public void loadCollection() {
        Integer[] param = new Integer[]{page, limit};

        switch (pageType) {

            case WISH_LIST:
                GetWishListTask getWishListTask = new GetWishListTask(this);
                getWishListTask.execute(param);
                break;
//            case WATCH_HISTORY:
//                GetCollectionTask getCollection = new GetCollectionTask(this);
//                getCollection.execute("93040");
//                break;
            case RENTED:
                GetUserRentedItemsTask getUserRentedItemsTask = new GetUserRentedItemsTask(this);
                getUserRentedItemsTask.execute(param);
                break;
        }
    }

    public void loadPlaylist() {
        Integer[] param = new Integer[]{page, limit};

        switch (pageType) {

            case WISH_LIST:
                GetWishListTask getWishListTask = new GetWishListTask(this);
                getWishListTask.execute(param);
                break;
//            case WATCH_HISTORY:
//                GetCollectionTask getCollection = new GetCollectionTask(this);
//                getCollection.execute("93040");
//                break;
            case RENTED:
                GetUserRentedItemsTask getUserRentedItemsTask = new GetUserRentedItemsTask(this);
                getUserRentedItemsTask.execute(param);
                break;
        }
    }

    @Override
    public void onRequestCompleted(List<Product> result) {
        products = result;
        if (result != null) {
            if (PageType.RENTED.equals(pageType)) {
                for (Iterator<Product> it = products.iterator(); it.hasNext(); ) {
                    Product product = it.next();
                    boolean entitled = false;

                    for (Offerings offerings : product.getOfferings()) {
                        if (offerings.getEntitled())
                            entitled = true;
                    }
                    if (entitled)
                        continue;
                    it.remove();
                }
            }
            adapter.setList(products);
        }
    }

    @Override
    public void onRequestFail(Throwable throwable) {
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    public void scrollToY(final int point) {
        gridView.post(new Runnable() {
            @Override
            public void run() {
                gridView.smoothScrollToPosition(point);
            }
        });
    }

    /**
     * This define the page types support by the {@link MyLibraryProductGridViewGroup}
     */
    public enum PageType {

        WISH_LIST,

//        WATCH_HISTORY,

        RENTED;
    }
}
