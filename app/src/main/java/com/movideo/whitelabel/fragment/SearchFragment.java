package com.movideo.whitelabel.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.product.Products;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.adapter.SearchListAdapter;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetSearchResultTask;
import com.movideo.whitelabel.util.OnProductItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnProductItemClickListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements ContentRequestListener<Products>, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    @Bind(R.id.editTextSearchInput)
    EditText searchText;
    @Bind(R.id.imageViewSearchClear)
    ImageView clearButton;
    @Bind(R.id.listViewSearchResult)
    ListView searchList;
    @Bind(R.id.textViewSearchNoResult)
    TextView noResultText;
    @Bind(R.id.progressBarSearchBottom)
    ProgressBar progressBarBottom;

    private int page;
    private int limit;
    private int totalPages;
    Editable previous;
    private SearchListAdapter adapter;
    private OnProductItemClickListener listener;
    private List<Product> searchResults;
    private String TAG = getClass().getSimpleName();
    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        page = 1;
        limit = 10;
        searchResults = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);

        loadView();

        return view;
    }

    @Override
    public void onDestroyView() {
        progressBarBottom = null;
        searchText = null;
        clearButton = null;
        searchList = null;
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProductItemClickListener) {
            listener = (OnProductItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement " + OnProductItemClickListener.class.getName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void loadView() {
        noResultText.setVisibility(View.INVISIBLE);
        progressBarBottom.setVisibility(View.GONE);
        searchText.setCursorVisible(false);

        clearButton.setVisibility(View.INVISIBLE);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noResultText.setVisibility(View.INVISIBLE);
                searchText.setText("");
                adapter.clear();
                page = 1;
            }
        });

        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setCursorVisible(true);
            }
        });

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();

                    InputMethodManager imManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imManager.hideSoftInputFromWindow(searchText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    return true;
                }
                return false;
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchText.getText().toString().isEmpty())
                    clearButton.setVisibility(View.INVISIBLE);
                else
                    clearButton.setVisibility(View.VISIBLE);
                page = 1;

                if (!s.toString().isEmpty() && s.toString().length() > 2)
                    performSearch();

                previous = s;
            }

        });

        adapter = new SearchListAdapter(getContext(), R.layout.list_item_search, searchResults);
        searchList.setAdapter(adapter);

        searchList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchText.setCursorVisible(false);
                InputMethodManager imManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imManager.hideSoftInputFromWindow(searchList.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                return false;
            }
        });

        searchList.setOnScrollListener(this);
        searchList.setOnItemClickListener(this);
    }

    private void performSearch() {
        String query = searchText.getText().toString();
        List<String> contentTypes = Arrays.asList("movie", "series", "clip");
        Object[] params = new Object[]{query, contentTypes, null, null, null, null, null, page, limit};

        GetSearchResultTask getSearchResultTask = new GetSearchResultTask(this);

        getSearchResultTask.execute(params);
    }

    private void calculateTotalPages(int totalCount) {
        if (totalCount % limit == 0)
            totalPages = totalCount / limit;
        else
            totalPages = totalCount / limit + 1;
        Log.d(TAG,String.valueOf(totalPages));
    }

    @Override
    public void onRequestCompleted(Products products) {
        if (products != null && products.getCount() > 0) {

            searchResults = products.getProductList();
            if (page == 1) {
                calculateTotalPages(products.getCount());

                adapter.setList(searchResults);
            } else {
                adapter.addList(searchResults);
            }
            noResultText.setVisibility(View.INVISIBLE);
        } else {
            noResultText.setVisibility(View.VISIBLE);
            adapter.clear();
        }
        progressBarBottom.setVisibility(View.GONE);
    }

    @Override
    public void onRequestFail(Throwable throwable) {
        noResultText.setVisibility(View.VISIBLE);
        progressBarBottom.setVisibility(View.GONE);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount - visibleItemCount == firstVisibleItem) {
            if (totalPages > page) {
                page++;
                performSearch();
                progressBarBottom.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (view.getTag() instanceof SearchListAdapter.ViewHolder) {
            SearchListAdapter.ViewHolder viewHolder = (SearchListAdapter.ViewHolder) view.getTag();
            Product product = viewHolder.getProduct();
            listener.onItemClick(product);
        }
    }
}
