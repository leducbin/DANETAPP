package com.movideo.whitelabel.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.movideo.baracus.model.collection.Collection;
import com.movideo.baracus.model.metadata.Content;
import com.movideo.baracus.model.playlist.Playlist;
import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.ContentHandler;
import com.movideo.whitelabel.MainActivity;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.WhiteLabelApplication;
import com.movideo.whitelabel.adapter.HomeClipsListAdapter;
import com.movideo.whitelabel.adapter.HomeImageSliderAdapter;
import com.movideo.whitelabel.adapter.HomeMovieListAdapter;
import com.movideo.whitelabel.animator.ViewPagerSlideAnimator;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetPlaylistTask;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.BlurBuilder;
import com.movideo.whitelabel.util.OnProductItemClickListener;
import com.movideo.whitelabel.util.PicassoHelper;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.ClickListener;
import com.movideo.whitelabel.view.ParallaxScrollView;
import com.movideo.whitelabel.view.ProductListViewGroup;
import com.movideo.whitelabel.view.ProgressView;
import com.movideo.whitelabel.widget.CircleIndicator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tobishiba.circularviewpager.library.CircularViewPagerHandler;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Home page fragment.
 * Activities that contain this fragment must implement the
 * {@link OnProductItemClickListener} interface to handle interaction events.
 * Use the {@link HomePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomePageFragment extends Fragment implements ViewPager.OnPageChangeListener, ClickListener, ContentRequestListener<Playlist>, ViewTreeObserver.OnScrollChangedListener, View.OnClickListener {

    public static final String TAG = HomePageFragment.class.getSimpleName();
    public static final String KEY_TITLE = "title";
    private static final String ARG_LICENSE_TYPE = "home_license_type";
    private static final int SLIDER_WAIT_TIME = 7000;

    @Bind(R.id.viewPagerHomeImageSlider)
    ViewPager imageSlider;
    @Bind(R.id.viewPageIndicatorHome)
    CircleIndicator indicator;
    @Bind(R.id.parallaxScrollViewHome)
    ParallaxScrollView scrollView;
    @Bind(R.id.imageViewHomePageBg)
    ImageView bgImage;
    @Bind(R.id.linearLayoutHome)
    LinearLayout linearLayout;
    @Bind(R.id.textViewHomeTitle)
    TextView titleText;
    @Bind(R.id.imageViewHomeBlankHeader)
    ImageView parallaxImage;
    @Bind(R.id.searchButton)
    Button butonSearch;

    private boolean isCalledOnce;
    private boolean postersLoaded;
    private int screenHeight;
    private int screenWidth;
    private float previousX = 0;
    private float previousY = 0;
    private Picasso picasso;
    private LicenseType licenseType;
    private List<Product> posters;
    private List<Product> movies;
    private List<Product> shortClips;
    private OnProductItemClickListener listener;
    private ViewPagerSlideAnimator slideAnimator;
    private HomeImageSliderAdapter adaptorSlider;
    private CircularViewPagerHandler circularViewPagerHandler;

    private MotionEvent previousEvent;
    private String dacSacId;
    private String moiCapNhatId;
    private String moiraRapId;
    private List<String> playlistIds;
    private List<String> playlistIdsPriority = new ArrayList<>();
    private List<String> playlistPriorityPosition = new ArrayList<>();
    private List<String> playlistRecommended = new ArrayList<>();
    private ProgressView progressView;
    private int loadedPlaylist;
    /**
     * Empty public constructor
     */
    public HomePageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param licenseType {@link LicenseType}.
     * @return A new instance of fragment HomePageFragment.
     */
    public static HomePageFragment newInstance(LicenseType licenseType) {
        HomePageFragment fragment = new HomePageFragment();
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

        isCalledOnce = false;
        postersLoaded = false;
        picasso = PicassoHelper.getInstance(getContext()).getPicasso();

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        posters = getPosters();
        movies = getMovies();
        shortClips = getShortClips();
        loadedPlaylist=0;
        progressView = new ProgressView(getActivity());

        loadedPlaylist =0;


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        //final ContentHandler contentHandler = ContentHandler.getInstance();
        ButterKnife.bind(this, view);
        progressView.show();
        List<Content> contentList = PreferenceHelper.getContentList(getActivity(), this.licenseType);

        switch (licenseType) {
            case AVOD:
                dacSacId = "1";
                moiCapNhatId = "83";
                playlistRecommended.add("92");
                break;
            case SVOD:
                dacSacId = "80";
                moiCapNhatId = "77";
                playlistRecommended.add("69");
                break;
            case TVOD:
                dacSacId = "86";
                moiCapNhatId = "2";
                moiraRapId = "7";
                playlistRecommended.add("30");
                break;
        };

        final Handler handler = new Handler(Looper.myLooper());
        if(contentList!=null) {
            loadCarousel(handler, contentList);
            playlistIds = getPlaylists(contentList);

            if (moiraRapId != null) {
                playlistIdsPriority.add(moiraRapId);
                playlistPriorityPosition.add(moiraRapId);
            }
            playlistIdsPriority.add(moiCapNhatId);
            playlistIdsPriority.add(dacSacId);
            playlistPriorityPosition.add(moiCapNhatId);
            playlistPriorityPosition.add(dacSacId);

            if (playlistIds != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (String playlistId:playlistIds){
                            GetPlaylistTask getPlaylistTask = new GetPlaylistTask(HomePageFragment.this);
                            Utils.executeInMultiThread(getPlaylistTask,playlistId);
                        }
//                        List<String> playlistIdsPriority = new ArrayList<>();
//
//                        if (moiraRapId != null)
//                            playlistIdsPriority.add(moiraRapId);
//                        playlistIdsPriority.add(moiCapNhatId);
//                        playlistIdsPriority.add(dacSacId);
//
//                        playlistIds.remove(moiCapNhatId);
//                        playlistIds.remove(moiraRapId);
//                        playlistIds.remove(dacSacId);
//                        loadPlaylistPriority(playlistIdsPriority);


                    }
                });
            }
        }
        setTitleText();

        scrollView.setParallaxImageView(parallaxImage);
        scrollView.setZoomRatio(ParallaxScrollView.ZOOM_X2);
        scrollView.setScrollSpeed(ParallaxScrollView.SCROLL_SPEED_X0_5);
        butonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment fragment = SearchFragment.newInstance();
                ((MainActivity) getActivity()).getMenuHandler().navigateToPage(fragment);
            }
        });
        butonSearch.bringToFront();
        return view;
    }

    @Override
    public void onDestroy() {
        progressView = null;
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        imageSlider = null;
        indicator = null;
        scrollView = null;
        bgImage = null;
        linearLayout = null;
        titleText = null;
        parallaxImage = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        imageSlider.addOnPageChangeListener(circularViewPagerHandler);
        imageSlider.addOnPageChangeListener(HomePageFragment.this);
        if (postersLoaded && slideAnimator != null) slideAnimator.onResume();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        imageSlider.removeOnPageChangeListener(circularViewPagerHandler);
        imageSlider.removeOnPageChangeListener(HomePageFragment.this);
        if (slideAnimator != null) {
            if (postersLoaded) slideAnimator.onPause();
        }
        scrollView.getViewTreeObserver().removeOnScrollChangedListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (progressView!=null)
            progressView.dismiss();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnProductItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + OnProductItemClickListener.class.getName());
        }
    }

    @Override
    public void onDetach() {
        listener = null;
        PicassoHelper.getInstance(getContext()).clearCache();
        super.onDetach();
    }

    public class CarouselLoadingTask extends AsyncTask<List<Content>, Void, List<Product>> {
        private String authToken;
        private Exception error;

        @Override
        protected List<Product> doInBackground(List<Content>... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();

            List<Product> productList = new ArrayList<>();
            try {
                Content carouselContent = null;
                for (Content content : params[0]) {
                    if (content.getType() == Content.Type.carousel && content.getItems() != null &&
                            !content.getItems().isEmpty()) {
                        carouselContent = content;
                        break;
                    }
                }
                for (Content imageContent: carouselContent.getItems()){
                    for (Content imageSubContent: imageContent.getItems() ){
                        if (imageSubContent.getType() == Content.Type.button){
                            String href = imageSubContent.getHref();
                            String[] hrefParts =  href.split("/");
                            String ProductID =  hrefParts[hrefParts.length - 1];
                            try {
                                productList.add(contentHandler.getProductById(authToken, ProductID, true));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                error = e;
            }

            return productList;
        }

        @Override
        protected void onPreExecute() {
            authToken = WhiteLabelApplication.getInstance().getAccessToken();
        }

        @Override
        protected void onPostExecute(List<Product> products) {
            if (products != null && products.size() > 0 && error == null) {

                try {
                    posters = products;
                    postersLoaded = true;

                    adaptorSlider = new HomeImageSliderAdapter(getContext(), screenWidth, getChildFragmentManager(), posters, new HomeImageSliderFragment.ImageDrawListener() {
                        @Override
                        public void onAfterDraw(ImageView imageView) {
                            updateOnImageSliderCreate(0, imageView);
                        }
                    });

                    imageSlider.setAdapter(adaptorSlider);

                    circularViewPagerHandler = new CircularViewPagerHandler(imageSlider);

                    imageSlider.addOnPageChangeListener(circularViewPagerHandler);
                    imageSlider.addOnPageChangeListener(HomePageFragment.this);

                    indicator.setViewPager(imageSlider);

                    slideAnimator = new ViewPagerSlideAnimator(imageSlider, SLIDER_WAIT_TIME);

                    slideAnimator.onResume();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

    }

    private void loadCarousel(Handler handler, final List<Content> contentList) {
        //here is new implementation, basically based button url, we detect product ID, from that build product collection
        CarouselLoadingTask carouselLoadingTask = new CarouselLoadingTask();
        Utils.executeInMultiThread(carouselLoadingTask, contentList);


    }

    public class PlaylistPriority extends AsyncTask<List<String>, Void, List<Playlist>> {
        private String authToken;
        private Exception error;

        @Override
        protected List<Playlist> doInBackground(List<String>... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();

            List<Playlist> playlist = new ArrayList<>();
            try {
                for (String id : params[0]){
                    if (id != null)
                        playlist.add(contentHandler.getPlaylistById(id));
                }
            } catch (Exception e) {
                error = e;
            }
            return playlist;


        }

        @Override
        protected void onPostExecute(List<Playlist> playlistResult) {
            for( Playlist playlist : playlistResult ){
                if (playlist != null && error == null) {
                    try {

                        loadPlaylist(playlist);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
            for (String playlistId:playlistIds){
                GetPlaylistTask getPlaylistTask = new GetPlaylistTask(HomePageFragment.this);
                Utils.executeInMultiThread(getPlaylistTask,playlistId);
            }
        }

    }
    private void loadPlaylistPriority( final List<String> playlistIds) {
        //here is new implementation, basically based button url, we detect product ID, from that build product collection
        PlaylistPriority playlistPriority = new PlaylistPriority();
        Utils.executeInMultiThread(playlistPriority, playlistIds);

    }

        @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        try {
            Fragment fragment = adaptorSlider.getFragment(position);

            if (fragment != null) {
                HomeImageSliderFragment sliderFragment = (HomeImageSliderFragment) fragment;
                ImageView imageView = sliderFragment.getSliderImage();

                if (imageView != null)
                    scrollView.setParallaxImageView(imageView);

                int realPosition = Utils.getRealPosition(position, adaptorSlider.getCount());

                setHomeBackgroundImage(realPosition);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private List<Product> getMovies() {
        List<Product> movies = new ArrayList<>();
        return movies;
    }

    private List<Product> getPosters() {
        List<Product> movies = new ArrayList<>();
        return movies;
    }

    private List<Product> getShortClips() {
        List<Product> shortClips = new ArrayList<>();
        return shortClips;
    }

    private List<String> getCollections(List<Content> contentList) {
        List<String> collections = new ArrayList<>();
        for (Content item : contentList) {
            if (item.getType() == Content.Type.collection) {
                collections.add(item.getIdentifier());
            }
        }

        return collections;
    }
    private List<String> getPlaylists(List<Content> contentList) {
        List<String>  playlists  = new ArrayList<>();
        for (Content item : contentList) {
            if (item.getType() == Content.Type.playlist) {
                playlists.add(item.getIdentifier());
            }
        }

        return playlists;
    }
    private void setHomeBackgroundImage(int position) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (isAdded()) {
                    bgImage.setBackground(new BitmapDrawable(getResources(), BlurBuilder.blur(getContext(), bitmap)));
                }
            }

            @Override
            public void onBitmapFailed(final Drawable errorDrawable) {
                Log.d(TAG, "FAILED");
            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {
                Log.d(TAG, "Prepare Load");
            }
        };
        bgImage.setTag(target);

        ImageURL.ImageProfile profile = ImageURL.ImageProfile.HERO_BANNER;
        if (screenHeight < screenWidth){
            profile = ImageURL.ImageProfile.BACKGROUND;
        }

        picasso.with(getContext()).load(ViewUtils.getImageUrlOfProduct(getContext(), posters.get(position), Page.HOME_PAGE, profile))
                .noPlaceholder()
                .resize(screenWidth, 0)
                .onlyScaleDown()
                .into(target);
    }

    public void updateOnImageSliderCreate(int position, ImageView imageView) {
        if (!isCalledOnce && position == 0) {
            scrollView.setParallaxImageView(imageView);
            setHomeBackgroundImage(0);
            isCalledOnce = true;
        }
    }

    private void setTitleText() {
        switch (licenseType) {

            case AVOD:
                titleText.setText(R.string.label_menu_header_avod_title);
                break;
            case SVOD:
                titleText.setText(R.string.label_menu_header_svod_title);
                break;
            case TVOD:
                titleText.setText(R.string.label_menu_header_tvod_title);
                break;
        }
    }

    private void loadCollection(Collection collection) {
        try {
            ProductListViewGroup productListViewGroup = new ProductListViewGroup(getContext(), collection);
            productListViewGroup.setOnItemClickListenerToRecyclerView(this);
            productListViewGroup.setOnClickListenerToTitleTextView(this);

            linearLayout.addView(productListViewGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    //Added by Thanh Tam
    private void loadPlaylist(Playlist playlist) {
        try {
            ProductListViewGroup productListViewGroup = new ProductListViewGroup(getContext(), playlist);
            productListViewGroup.setOnItemClickListenerToRecyclerView(this);
            productListViewGroup.setOnClickListenerToTitleTextView(this);
            int start = 0;
            Log.d("Playlist",playlist.toString());
            String playlistId = playlist.getId().toString();
            if(playlistIdsPriority.contains(playlistId)) {
                int position = playlistIdsPriority.indexOf(playlistId) - playlistPriorityPosition.indexOf(playlistId);
                linearLayout.addView(productListViewGroup, position,
                                new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                playlistPriorityPosition.remove(playlistId);
            }
            else {
                if(playlistRecommended.isEmpty()) {
                    linearLayout.addView(productListViewGroup, loadedPlaylist-1 , new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                else{
                    linearLayout.addView(productListViewGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    if(playlistRecommended.contains(playlistId))
                        playlistRecommended.remove(playlistId);
                }
            }
            //Log.d("Playlist Id",playlistId);
            loadedPlaylist++;
            //Log.d("Loaded PLaylists", String.valueOf(loadedPlaylist));
            //Log.d("PlaylistIds", String.valueOf(loadedPlaylist == playlistIds.size()));
            if(loadedPlaylist == playlistIds.size()) {
                Log.d("ProgressView","dismiss");
                if (progressView!=null)
                    progressView.dismiss();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    //End added by Thanh Tam
    @Override
    public void onRequestCompleted(Playlist result) {
        //Log.d("homepagefragment", result.toString());
        loadPlaylist(result);

    }

    @Override
    public void onRequestFail(Throwable throwable) {
        Log.e(TAG, throwable.getMessage(), throwable);
        if (progressView!=null)
            progressView.dismiss();
    }

    @Override
    public void onClick(View view, int position) {
        Product product = null;
        Playlist playlist = null;
        Object tag = view.getTag();
        if (tag instanceof HomeMovieListAdapter.ProductViewHolder) {

            HomeMovieListAdapter.ProductViewHolder viewHolder = (HomeMovieListAdapter.ProductViewHolder) tag;
            product = viewHolder.getProduct();
            playlist = viewHolder.getPlaylist();
        } else if (tag instanceof HomeClipsListAdapter.ProductViewHolder) {

            HomeClipsListAdapter.ProductViewHolder viewHolder = (HomeClipsListAdapter.ProductViewHolder) tag;
            product = viewHolder.getProduct();
        }

        if (product != null) {
            listener.onItemClick(product);
        }

        if (playlist != null){
            MovieTrailersFragment fragment = MovieTrailersFragment.newInstance(playlist.getExtend_name(), playlist.getExtend().toString());
            ((MainActivity) getActivity()).getMenuHandler().navigateToPage(fragment);
        }
    }

    @Override
    public void onLongClick(View view, int position) {
    }

    @Override
    public void onScrollChanged() {
        float scroll = scrollView.getScrollY();

        int height = imageSlider.getHeight();

        if (30 <= scroll / height * 100f) {
            if (slideAnimator != null) slideAnimator.onPause();
        } else {
            if (slideAnimator != null && !slideAnimator.isRunning() && slideAnimator.isStopped()) slideAnimator.onResume();
        }
    }

    @Override
    public void onClick(View view) {
        //String collectionId = (String) view.getTag();
        String playlistId = (String) view.getTag();
        MovieTrailersFragment fragment = MovieTrailersFragment.newInstance(((TextView) view).getText().toString(), playlistId);

//        getFragmentManager().beginTransaction()
//                .replace(R.id.frame_layout_main,fragment)
//                .commit();
////        ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction()
//                .add(R.id.frame_layout_main, fragment)
//                .commit();
        ((MainActivity) getActivity()).getMenuHandler().navigateToPage(fragment);
    }
}
