package com.movideo.whitelabel.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movideo.baracus.model.collection.Collection;
import com.movideo.baracus.model.playlist.Playlist;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.adapter.HomeClipsListAdapter;
import com.movideo.whitelabel.adapter.HomeMovieListAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Contains a view group combination of a {@link TextView} and a {@link HorizontalListView} .
 */
public class ProductListViewGroup extends RelativeLayout implements View.OnTouchListener {

    @Bind(R.id.textViewHorizontal)
    TextView textView;
    @Bind(R.id.recyclerViewHorizontalList)
    RecyclerView recyclerView;

    private float previousX;
    private float previousY;
    private Collection collection;
    private Playlist playlist;
    private ArrayAdapter adapter;
    private HomeMovieListAdapter movieListAdapter;
    private HomeClipsListAdapter clipsListAdapter;

    public ProductListViewGroup(Context context, Collection collection) {
        super(context);
        this.collection = collection;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if ("clip".equals(collection.getType())) {
            layoutInflater.inflate(R.layout.layout_horizontal_clips_list, this, true);
        } else {
            layoutInflater.inflate(R.layout.layout_horizontal_list, this, true);
        }

        ButterKnife.bind(this);

        populateListView();
    }

    public ProductListViewGroup(Context context, Playlist playlist) {
        super(context);
        this.playlist = playlist;
        if( !((playlist.getProductList()).isEmpty()) ) {

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        if ("clip".equals(collection.getType())) {
//            layoutInflater.inflate(R.layout.layout_horizontal_clips_list, this, true);
//        } else {
//            layoutInflater.inflate(R.layout.layout_horizontal_list, this, true);
//        }
            layoutInflater.inflate(R.layout.layout_horizontal_list, this, true);

            ButterKnife.bind(this);

            populateListView();
        }
    }

    private void populateListView() {

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpaceDecoration(getContext(), R.dimen.horizontal_list_space, true, true));

//        if ("clip".equals(collection.getType())) {
//            clipsListAdapter = new HomeClipsListAdapter(getContext(), R.layout.list_item_horizontal_clips_list, collection.getProductList());
//            recyclerView.setAdapter(clipsListAdapter);
//        } else {
//            movieListAdapter = new HomeMovieListAdapter(getContext(), R.layout.list_item_horizontal_list, collection.getProductList());
//            recyclerView.setAdapter(movieListAdapter);
//        }
            movieListAdapter = new HomeMovieListAdapter(getContext(), R.layout.list_item_horizontal_list, playlist);
            recyclerView.setAdapter(movieListAdapter);
            textView.setText(playlist.getName().toUpperCase());
            textView.setTag(playlist.getId().toString());

            recyclerView.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                previousX = event.getX();
                previousY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                // Allow ScrollView to intercept touch events.
                v.getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();

                float dx = Math.abs(x - previousX);
                float dy = Math.abs(y - previousY);

                if (dx > dy) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            default:
                v.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        // Handle ListView touch events.
        v.onTouchEvent(event);
        return true;
    }

    /**
     * Sets on item click listener to {@link RecyclerView} in this view group.
     *
     * @param listener {@link ClickListener}
     */
    public void setOnItemClickListenerToRecyclerView(ClickListener listener) {
        recyclerView.addOnItemTouchListener(new RelatedItemTouchListener(getContext(), recyclerView, listener));
    }

    /**
     * Sets on click listener
     *
     * @param listener
     */
    public void setOnClickListenerToTitleTextView(OnClickListener listener) {
        textView.setOnClickListener(listener);
    }
}
