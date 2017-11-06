package com.movideo.whitelabel.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.movideo.baracus.model.collection.Collection;
import com.movideo.baracus.model.playlist.Playlist;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.product.Products;
import com.movideo.whitelabel.FilterActivity;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.WhiteLabelApplication;
import com.movideo.whitelabel.adapter.MovieTrailerListAdapter;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetCollectionTask;
import com.movideo.whitelabel.communication.GetPlaylistTask;
import com.movideo.whitelabel.communication.GetSearchResultTask;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.model.ProductReleaseDateComparator;
import com.movideo.whitelabel.model.ProductTitleAscendingComparator;
import com.movideo.whitelabel.model.ProductTitleDescendingComparator;
import com.movideo.whitelabel.util.OnProductItemClickListener;
import com.movideo.whitelabel.util.PicassoHelper;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.view.ProgressView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieTrailersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieTrailersFragment extends Fragment implements ContentRequestListener, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    // the fragment initialization parameters
    private static final String TAG = MovieTrailersFragment.class.getSimpleName();

    private static final String ARG_TITLE = "movie_trailer_title";
    private static final String ARG_COLLECTION_ID = "movie_trailer_collection_id";
    private static final String ARG_PLAYLIST_ID = "movie_trailer_playlist_id";
    private static final String ARG_CONTENT_TYPE = "movie_trailer_content_types";
    private static final String ARG_GENRES = "movie_trailer_genres";
    private static final String ARG_PAGE_TYPE = "movie_trailer_page_types";

    private static final String PAGE_TYPE_SEARCH = "page_type_search";
    private static final String PAGE_TYPE_COLLECTION = "page_type_collection";
    private static final String PAGE_TYPE_PLAYLIST = "page_type_playlist";
    private static final int CLIPS_PHONE_COLUMNS_COUNT = 2;
    private static final int CLIPS_TABLET_COLUMNS_COUNT = 4;
    private static final int POSTER_PHONE_COLUMNS_COUNT = 3;
    private static final int POSTER_TABLET_COLUMNS_COUNT = 6;

    private static final int PAGE_ITEM_LIMIT = 15;


    @Bind(R.id.textViewMovieTrailersTitle)
    TextView titleText;
    @Bind(R.id.textViewNoResults)
    TextView noResultsText;
    @Bind(R.id.gridViewMovieTrailer)
    GridView gridView;
    @Bind(R.id.imageViewMovieTrailerFilter)
    ImageView filterImage;
    @Bind(R.id.progressBarMovieTrailerBottom)
    ProgressBar progressBarBottom;


    private int page;
    private int totalPages;
    private String title;
    private String minYear;
    private String maxYear;
    private String sortBy;
    private String pageType;
    private String collectionId;
    private String playlistId;
    private List<String> contentTypes;
    private List<String> genres;
    private List<String> countries;
    private List<Product> products;
    private LicenseType licenseType;
    private MovieTrailerListAdapter adapter;
    private OnProductItemClickListener listener;
    private OnFilterClickListener onFilterClickListener;
    private ProgressView progressView;

    /**
     * Empty public constructor
     */
    public MovieTrailersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     * Use one of these parameters to use in populating the page.
     *
     * @param title        Page title.
     * @param contentTypes {@link List<String>} list of content types.
     * @param genres       {@link List<String>} list of genres.
     * @return A new instance of fragment MovieTrailersFragment.
     */
    public static MovieTrailersFragment newInstance(String title, ArrayList<String> contentTypes, ArrayList<String> genres) {
        MovieTrailersFragment fragment = new MovieTrailersFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_PAGE_TYPE, PAGE_TYPE_SEARCH);
        args.putStringArrayList(ARG_CONTENT_TYPE, contentTypes);
        args.putStringArrayList(ARG_GENRES, genres);

        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     * Use one of these parameters to use in populating the page.
     *
     * @param title        Page title.
     * @param playlistId Collection id.
     * @return A new instance of fragment MovieTrailersFragment.
     */
    public static MovieTrailersFragment newInstance(String title, String playlistId) {
        MovieTrailersFragment fragment = new MovieTrailersFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_PAGE_TYPE, PAGE_TYPE_PLAYLIST);
        args.putString(ARG_PLAYLIST_ID,playlistId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            pageType = getArguments().getString(ARG_PAGE_TYPE);

            if (PAGE_TYPE_SEARCH.equals(pageType)) {
                contentTypes = getArguments().getStringArrayList(ARG_CONTENT_TYPE);
                genres = getArguments().getStringArrayList(ARG_GENRES);
            }
            if (PAGE_TYPE_COLLECTION.equals(pageType)) {
                collectionId = getArguments().getString(ARG_COLLECTION_ID);
            }
            if (PAGE_TYPE_PLAYLIST.equals(pageType)) {
                playlistId = getArguments().getString(ARG_PLAYLIST_ID);
            }
        }

        page = 1;
        sortBy = FilterActivity.SORT_BY_CODE_RELEASE_DATE;
        products = new ArrayList<>();
        licenseType = WhiteLabelApplication.getInstance().getLicenseType();
        progressView = new ProgressView(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_trailers, container, false);
        ButterKnife.bind(this, view);
        progressBarBottom.setVisibility(View.GONE);
        progressView.show();
        titleText.setText(title);
        noResultsText.setVisibility(View.GONE);

        switch (licenseType) {

            case AVOD:
                filterImage.setImageResource(R.drawable.icon_filter_green);
                break;
            case SVOD:
                filterImage.setImageResource(R.drawable.icon_filter_blue);
                break;
            case TVOD:
                filterImage.setImageResource(R.drawable.icon_filter_red);
                break;
        }

        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(this);

        if (PAGE_TYPE_SEARCH.equals(pageType)) {
            filterImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFilterClickListener.onFilterClick(MovieTrailersFragment.this, contentTypes, genres, countries, minYear, maxYear, sortBy);
                }
            });
        } else {
            filterImage.setVisibility(View.INVISIBLE);
        }
        loadProducts();

        return view;
    }

    @Override
    public void onDestroy() {
        progressView = null;
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        titleText = null;
        filterImage = null;
        progressBarBottom = null;
        gridView = null;
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
//        loadProducts();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnProductItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnProductItemClickListener");
        }
        try {
            onFilterClickListener = (OnFilterClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFilterClickListener");
        }
    }

    @Override
    public void onDetach() {
        PicassoHelper.getInstance(getContext()).clearCache();
        listener = null;
        onFilterClickListener = null;
        super.onDetach();
    }

    private void loadProducts() {
        if (PAGE_TYPE_SEARCH.equals(pageType)) {
            Integer minYearInt = null;
            Integer maxYearInt = null;

            if (minYear != null && !minYear.isEmpty())
                minYearInt = Integer.parseInt(minYear);
            if (maxYear != null && !maxYear.isEmpty())
                maxYearInt = Integer.parseInt(maxYear);

            List<String> offerings = new ArrayList<>();
            offerings.add(licenseType.toString().toUpperCase());

            Object[] params = new Object[]{"", contentTypes, offerings, genres, countries, minYearInt, maxYearInt, page, PAGE_ITEM_LIMIT};

            GetSearchResultTask getSearchResultTask = new GetSearchResultTask(this);

            getSearchResultTask.execute(params);
        }
        if (PAGE_TYPE_COLLECTION.equals(pageType)) {
            GetCollectionTask getCollectionTask = new GetCollectionTask(this);
            getCollectionTask.execute(collectionId);
        }
        if (PAGE_TYPE_PLAYLIST.equals(pageType)){
            GetPlaylistTask getPlaylistTask = new GetPlaylistTask(this);
            getPlaylistTask.execute(playlistId);
        }
    }

    private void configureGridView(Product product) {

        if (product != null) {
            if ("clip".equals(product.getType())) {
                if (ViewUtils.isTablet(getContext())) {
                    gridView.setNumColumns(CLIPS_TABLET_COLUMNS_COUNT);
                } else {
                    gridView.setNumColumns(CLIPS_PHONE_COLUMNS_COUNT);
                }
            } else {
                if (ViewUtils.isTablet(getContext())) {
                    gridView.setNumColumns(POSTER_TABLET_COLUMNS_COUNT);
                } else {
                    gridView.setNumColumns(POSTER_PHONE_COLUMNS_COUNT);
                }
                adapter = new MovieTrailerListAdapter(getContext(), R.layout.list_item_movie_trailer, products, false);
            }
            gridView.setAdapter(adapter);
        }
    }

    @Override
    public void onRequestCompleted(Object result) {
        if (progressView!=null)
            if (progressView!=null)
                progressView.dismiss();
        try {
            noResultsText.setVisibility(View.GONE);
            if (result != null) {
                if (PAGE_TYPE_SEARCH.equals(pageType)) {
                    Products productResults = (Products) result;
                    if (productResults.getCount() > 0) {
                        products = productResults.getProductList();
//                        sortProducts();

                        if (page == 1) {

                            calculateTotalPages(productResults.getCount());
                            configureGridView(products.get(0));
                        } else {
                            adapter.addList(products);
                        }
                    } else {
                        if (adapter != null)
                            adapter.clear();
                        noResultsText.setVisibility(View.VISIBLE);
                    }
                }
                if (PAGE_TYPE_COLLECTION.equals(pageType)) {
                    Collection collectionResults = (Collection) result;
                    if (collectionResults.getProductList().size() > 0) {
                        products = collectionResults.getProductList();

                        configureGridView(products.get(0));
                    } else {
                        if (adapter != null)
                            adapter.clear();
                        noResultsText.setVisibility(View.VISIBLE);
                    }
                }
                if (PAGE_TYPE_PLAYLIST.equals(pageType)) {
                    Playlist playlistResults = (Playlist) result;
                    if (playlistResults.getProductList().size() > 0) {
                        products = playlistResults.getProductList();
                        configureGridView(products.get(0));
                    } else {
                        if (adapter != null)
                            adapter.clear();
                        noResultsText.setVisibility(View.VISIBLE);
                    }
                }
            }
            else {
                if (adapter != null)
                    adapter.clear();
                noResultsText.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (progressBarBottom != null) progressBarBottom.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestFail(Throwable throwable) {
        if (progressView!=null)
            progressView.dismiss();
        if (progressBarBottom != null) progressBarBottom.setVisibility(View.GONE);
        Log.e(TAG, throwable.getMessage(), throwable);
    }

    private void calculateTotalPages(int totalCount) {
        if (totalCount % PAGE_ITEM_LIMIT == 0)
            totalPages = totalCount / PAGE_ITEM_LIMIT;
        else
            totalPages = totalCount / PAGE_ITEM_LIMIT + 1;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (products != null)
            if (totalItemCount - visibleItemCount == firstVisibleItem) {
                if (PAGE_TYPE_SEARCH.equals(pageType) && totalPages > page && products.size() == PAGE_ITEM_LIMIT) {
                    page++;
                    loadProducts();
                    progressBarBottom.setVisibility(View.VISIBLE);
                }
            }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object tag = view.getTag();

        if (!(tag instanceof MovieTrailerListAdapter.ProductViewHolder))
            return;

        MovieTrailerListAdapter.ProductViewHolder viewHolder = (MovieTrailerListAdapter.ProductViewHolder) tag;
        listener.onItemClick(viewHolder.getProduct());
    }

    public void callWhenFilterActivated(ArrayList<String> genres, ArrayList<String> countries, String minYear, String maxYear, String sortBy) {
        this.genres = genres;
        this.countries = countries;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.sortBy = FilterActivity.SORT_BY_CODE_A_Z; //TODO Sorting temporary disabled.
        page = 1;
        if (products != null && !products.isEmpty())
            products.clear();
        products = null;
        if (adapter != null)
            adapter.clear();
        loadProducts();
    }

    private void sortProducts() {
        if (FilterActivity.SORT_BY_CODE_RELEASE_DATE.equals(sortBy)) {
            Collections.sort(products, new ProductReleaseDateComparator());
        } else if (FilterActivity.SORT_BY_CODE_A_Z.equals(sortBy)) {
            Collections.sort(products, new ProductTitleAscendingComparator());
        } else if (FilterActivity.SORT_BY_CODE_Z_A.equals(sortBy)) {
            Collections.sort(products, new ProductTitleDescendingComparator());
        }
    }

    /**
     * Implement to get response when to launch {@link FilterActivity}.
     */
    public interface OnFilterClickListener {

        /**
         * When filter icon is clicked this method is invoked.
         *
         * @param fragment  {@link MovieTrailersFragment}.
         * @param types     {@link List<String>} of types Eg: movie, series...
         * @param genres    {@link List<String>} of genres. Selected genres for the filter.
         * @param countries {@link List<String>} of countries. Selected countries for the filter.
         * @param minYear   Selected min year.
         * @param maxYear   Selected max year.
         * @param sortBy    Selected sort method.
         */
        void onFilterClick(MovieTrailersFragment fragment, List<String> types, List<String> genres, List<String> countries, String minYear, String maxYear, String sortBy);
    }
}
