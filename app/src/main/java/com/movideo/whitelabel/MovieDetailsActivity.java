package com.movideo.whitelabel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.movideo.baracus.clientimpl.ImageRepo;
import com.movideo.baracus.model.product.Offerings;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.product.Products;
import com.movideo.baracus.model.user.Favourite;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.adapter.RelatedMovieAdapter;
import com.movideo.whitelabel.communication.AuthenticationParser;
import com.movideo.whitelabel.communication.AuthenticationUserResultReceived;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetWishListTask;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.BlurBuilder;
import com.movideo.whitelabel.util.DialogEventListeners;
import com.movideo.whitelabel.util.DisplayResources;
import com.movideo.whitelabel.util.NetworkAvailability;
import com.movideo.whitelabel.util.PlaybackHandler;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.AddMessageDialogView;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;
import com.movideo.whitelabel.view.ClickListener;
import com.movideo.whitelabel.view.ParallaxScrollView;
import com.movideo.whitelabel.view.ProgressView;
import com.movideo.whitelabel.view.RelatedItemTouchListener;
import com.movideo.whitelabel.view.SpaceDecoration;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

//import com.movideo.android.activity.PlayerActivity;


public class MovieDetailsActivity extends AppCompatActivity implements ViewTreeObserver.OnScrollChangedListener, View.OnClickListener, ContentRequestListener<List<Product>> {

    public static final String KEY_PRODUCT = "movie_details_product";
    public static final int animationDuration = 300;
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    @Bind(R.id.headerTitleTextView)
    TextView headerTitleTextView;
    @Bind(R.id.closeImageButton)
    ImageButton closeImageButton;
    @Bind(R.id.closeButton)
    Button closeButton;
    @Bind(R.id.scrollViewMovieDetail)
    ParallaxScrollView scrollViewMovieDetail;
    @Bind(R.id.movieFullImageView)
    ImageView movieFullImageView;
    @Bind(R.id.moviePlayButtonLayout)
    RelativeLayout moviePlayButtonLayout;
    @Bind(R.id.moviePlayImageButton)
    ImageButton moviePlayImageButton;
    @Bind(R.id.daysLeftTextView)
    TextView daysLeftTextView;
    @Bind(R.id.movieTitleTextView)
    TextView movieTitleTextView;
    @Bind(R.id.movieTimeTextView)
    TextView movieTimeTextView;
    @Bind(R.id.toggleUpDownArrow)
    ToggleButton toggleUpDownArrow;
    @Bind(R.id.movieToggleDownLayout)
    LinearLayout movieToggleDownLayout;
    @Bind(R.id.movieDescTextView)
    TextView movieDescTextView;
    @Bind(R.id.movieGenresTextView)
    TextView movieGenresTextView;
    @Bind(R.id.movieCastCrewActorTextView)
    TextView movieCastCrewActorTextView;
    @Bind(R.id.movieCastCrewDirectorTextView)
    TextView movieCastCrewDirectorTextView;
    @Bind(R.id.movieLangTextView)
    TextView movieLangTextView;
    @Bind(R.id.movieRelatedHeaderTextView)
    TextView movieRelatedHeaderTextView;
    @Bind(R.id.moviePlayTrailerButton)
    Button moviePlayTrailerButton;
    @Bind(R.id.movieWishListButton)
    Button movieWishListButton;
    @Bind(R.id.relatedMovieRecyclerView)
    RecyclerView relatedMovieRecyclerView;
    @Bind(R.id.progressBarRelated)
    ProgressBar progressBarRelated;
    @Bind(R.id.imageViewMovieBgBlur)
    ImageView imageViewMovieBgBlur;
    @Bind(R.id.movieSmallImageViewPoster)
    ImageView movieSmallImageViewPoster;
    @Bind(R.id.buttonMovieDetailsSD)
    ToggleButton buttonSD;
    @Bind(R.id.buttonMovieDetailsHD)
    ToggleButton buttonHD;
    @Bind(R.id.buttonMovieDetails4K)
    ToggleButton button4K;
    @Bind(R.id.movieShareButton)
    Button shareButton;

    private DisplayResources displayResources;
    private int displayHeight;
    private int displayWidth;
    private int statusBarHeight;
    private int toggleButtonLayoutHeight;
    private RelatedMovieAdapter relatedMovieAdapter;
    private List<Product> relatedMovieItems;
    private String entitledVariant;

    private ProgressView progressView;
    private Product product;

    private boolean isDown = false;
    private boolean isScrollWith = false;
    private boolean isToggleWith = true;
    private boolean playButtonDown = false;
    private WhiteLabelApplication whiteLabelApplication;
    private Product trailerProduct;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        if (ViewUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        /*Get the application instance and get License Type*/
        whiteLabelApplication = WhiteLabelApplication.getInstance();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        progressView = new ProgressView(this);

        closeButton.setOnClickListener(this);
        closeImageButton.setOnClickListener(this);
        movieWishListButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);

        scrollViewMovieDetail.getViewTreeObserver().addOnScrollChangedListener(this);
        scrollViewMovieDetail.fullScroll(View.FOCUS_UP);

       /* get device resources (navigation/status bar height)*/
        displayResources = new DisplayResources(this);
        displayHeight = displayResources.getDisplayHeight();
        displayWidth = displayResources.getDisplayWidth();
        statusBarHeight = 0;//displayResources.getStatusBarHeight();

        if (ViewUtils.isTablet(this)) {
            //movieFullImageView.getLayoutParams().height = (displayHeight / 2) + statusBarHeight;
            //moviePlayButtonLayout.getLayoutParams().height = ((displayHeight / 2) + statusBarHeight) - (int) getResources().getDimension(R.dimen.page_top_gradient_height);
        } else {
            toggleButtonLayoutHeight = (int) getResources().getDimension(R.dimen.movie_toggle_up_layout_height);
            /*movie image height set according to device height*/
//            movieFullImageView.getLayoutParams().height = displayHeight - (toggleButtonLayoutHeight + statusBarHeight);
            moviePlayButtonLayout.getLayoutParams().height = displayHeight - (toggleButtonLayoutHeight + statusBarHeight);
        }

        scrollViewMovieDetail.setParallaxImageView(movieFullImageView);
        scrollViewMovieDetail.setZoomRatio(ParallaxScrollView.NO_ZOOM);
        scrollViewMovieDetail.setScrollSpeed(ParallaxScrollView.SCROLL_SPEED_X0_5);

        /*set all widgets/view values*/
        product = (Product) getIntent().getSerializableExtra(KEY_PRODUCT);

        getLatestProductInfo();


        setWidgetsValues();

        if (!ViewUtils.isTablet(this)) {
            toggleUpDownArrow.setOnClickListener(this);
            movieToggleDownLayout.setVisibility(View.VISIBLE);
        } else {
            Picasso.with(this).load(ViewUtils.getImageUrlOfProduct(MovieDetailsActivity.this, product, Page.MOVIE_DETAIL_PAGE, ImageURL.ImageProfile.POSTER))
                    .resizeDimen(R.dimen.movie_poster_small_width, R.dimen.movie_poster_small_height)
                    .onlyScaleDown()
                    .noFade()
                    .placeholder(R.drawable.fallback_poster)
                    .into(movieSmallImageViewPoster);
        }

        progressBarRelated.setVisibility(View.VISIBLE);
        relatedMovieRecyclerView.setVisibility(View.VISIBLE);

        moviePlayTrailerButton.setEnabled(false);
        moviePlayTrailerButton.setAlpha(0.5f);


        RelatedVideoParser relatedVideoParser = new RelatedVideoParser();
        Utils.executeInMultiThread(relatedVideoParser);

        Integer[] param = new Integer[]{0, 0};
        GetWishListTask getWishListTask = new GetWishListTask(this);
        getWishListTask.execute(param);
    }

    private void getLatestProductInfo() {
        new AsyncTask<Void, Void, Product>() {
            private String error = null;
            private ProgressView progressView;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressView = new ProgressView(MovieDetailsActivity.this);
                progressView.show();
            }

            @Override
            protected Product doInBackground(Void... params) {
                try {
                    String accessToken = WhiteLabelApplication.getInstance().getAccessToken();
                    product = ContentHandler.getInstance().getProductById(accessToken, Integer.toString(product.getId()));
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), e.getMessage());
                    error = e.getMessage();
                }
                return product;
            }

            @Override
            protected void onPostExecute(Product latestProduct) {
                try {
                    if (error == null && latestProduct != null) {
                        MovieTrailerTask movieTrailerTask = new MovieTrailerTask();
                        Utils.executeInMultiThread(movieTrailerTask, Integer.toString(product.getId()));
                    }
                } catch (Exception e){
                    Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
                } finally {
                    if (progressView!=null)
                        progressView.dismiss();
                }
            }
        }.execute();
    }

    /*set all widgets/view values*/
    private void setWidgetsValues() {
        setTitleText();
        /*set product image */

        ImageURL.ImageProfile profile = ImageURL.ImageProfile.HERO_BANNER;
        if (displayHeight < displayWidth){
            profile = ImageURL.ImageProfile.BACKGROUND;
        }

        Picasso.with(this).load(ViewUtils.getImageUrlOfProduct(MovieDetailsActivity.this, product, Page.MOVIE_DETAIL_PAGE, profile))
                .resize(displayWidth, displayHeight)
                .onlyScaleDown()
                .noFade()
                .placeholder(R.drawable.fallback_poster)
                .into(movieFullImageView);

        /*set background blur image */
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                setBackgroundImage(imageViewMovieBgBlur, ViewUtils.getImageUrlOfProduct(MovieDetailsActivity.this, product, Page.MOVIE_DETAIL_PAGE, ImageURL.ImageProfile.BACKGROUND));
            }
        });

        if (product.getOfferings().size() > 0) {
            String timeLeft = getTimeLeftString(product.getOfferings());

            if (timeLeft != null && !timeLeft.isEmpty()) {
                daysLeftTextView.setText(timeLeft);
            } else {
                daysLeftTextView.setVisibility(View.INVISIBLE);
            }
        } else {
            daysLeftTextView.setVisibility(View.INVISIBLE);
        }

        if (product.getTitle() != null && !product.getTitle().equals("null")) {
            movieTitleTextView.setText(product.getTitle());
        } else {
            movieTitleTextView.setText("");
        }

        if (product.getReleased() != null && !product.getReleased().equals("null")) {
            movieTimeTextView.setText(product.getReleased());
        } else {
            movieTimeTextView.setText("");
        }

        movieTimeTextView.append(" ");
        if (product.getDuration() != null && !product.getDuration().equals("null")) {
            movieTimeTextView.setText(product.getDuration());
        }

        if (product.getDescription() != null && !product.getDescription().equals("null")) {
            movieDescTextView.setText(product.getDescription());
        } else {
            movieDescTextView.setText("");
        }

        if (product.getGenres() != null && product.getGenres().size() > 0) {
            movieGenresTextView.setText(TextUtils.join(", ", product.getGenres()));
        } else {
            movieGenresTextView.setText("");
        }

        movieCastCrewActorTextView.setText("Diễn viên - ");
        if (product.getActors() != null && product.getActors().size() > 0) {
            movieCastCrewActorTextView.append(TextUtils.join(", ", product.getActors()));
        }


        movieCastCrewDirectorTextView.setText("Đạo diễn - ");
        if (product.getDirectors() != null && product.getDirectors().size() > 0) {
            movieCastCrewDirectorTextView.append(TextUtils.join(", ", product.getDirectors()));
        }

        if (product.getLanguage() != null && product.getLanguage().getSubtitles().size() > 0) {
            movieLangTextView.setText(TextUtils.join(", ", product.getLanguage().getSubtitles()));
        } else {
            movieLangTextView.setText("");
        }

        loadVariants();
    }

    private void loadVariants() {
        entitledVariant = "";
        buttonSD.setOnClickListener(this);
        buttonHD.setOnClickListener(this);
        button4K.setOnClickListener(this);

        buttonSD.setVisibility(View.GONE);
        buttonHD.setVisibility(View.GONE);
        button4K.setVisibility(View.GONE);

        for (Offerings offering : product.getOfferings()) {

            if (offering.getEntitled()) {
                if ("SD".equals(entitledVariant.toUpperCase())) {
                    entitledVariant = offering.getVariant();
                } else if ("HD".equals(entitledVariant.toUpperCase()) && !"SD".equals(offering.getVariant().toUpperCase())) {
                    entitledVariant = offering.getVariant();
                } else if ("".equals(entitledVariant)) {
                    entitledVariant = offering.getVariant();
                }
            }

//            if ("SD".equals(offering.getVariant().toUpperCase())) {
//                buttonSD.setVisibility(View.VISIBLE);
//                if (!offering.getEntitled()) {
//                    buttonSD.setEnabled(false);
//                    buttonSD.setAlpha(0.5f);
//                }
//            }
//            if ("HD".equals(offering.getVariant().toUpperCase())) {
//                buttonHD.setVisibility(View.VISIBLE);
//                if (!offering.getEntitled()) {
//                    buttonHD.setEnabled(false);
//                    buttonHD.setAlpha(0.5f);
//                }
//            }
//            if ("4K".equals(offering.getVariant().toUpperCase())) {
//                button4K.setVisibility(View.VISIBLE);
//                if (!offering.getEntitled()) {
//                    button4K.setEnabled(false);
//                    button4K.setAlpha(0.5f);
//                }
//            }
        }
        setEntitledVariantChecked(entitledVariant);
    }

    private void setEntitledVariantChecked(String variant) {
        if ("SD".equals(variant)) {
            buttonSD.setChecked(true);
            buttonHD.setChecked(false);
            button4K.setChecked(false);
        } else if ("HD".equals(variant)) {
            buttonSD.setChecked(false);
            buttonHD.setChecked(true);
            button4K.setChecked(false);
        } else if ("4K".equals(variant.toUpperCase())) {
            buttonSD.setChecked(false);
            buttonHD.setChecked(false);
            button4K.setChecked(true);
        } else {
            buttonSD.setChecked(false);
            buttonHD.setChecked(false);
            button4K.setChecked(false);
        }
    }

    private void setTitleText() {
        switch (whiteLabelApplication.getLicenseType()) {
            case AVOD:
                headerTitleTextView.setText(R.string.label_menu_header_avod_title);
                break;
            case SVOD:
                headerTitleTextView.setText(R.string.label_menu_header_svod_title);
                break;
            case TVOD:
                headerTitleTextView.setText(R.string.label_menu_header_tvod_title);
                break;
        }
    }

    /*set related videos*/
    private void setRelatedVideos() {
        relatedMovieAdapter = new RelatedMovieAdapter(this, relatedMovieItems);
        relatedMovieRecyclerView.setAdapter(relatedMovieAdapter);
        relatedMovieRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        relatedMovieRecyclerView.setLayoutManager(layoutManager);
        relatedMovieRecyclerView.addItemDecoration(new SpaceDecoration(this, R.dimen.horizontal_list_space,
                true, true));

        relatedMovieRecyclerView.addOnItemTouchListener(new RelatedItemTouchListener(this, relatedMovieRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

                //Firebase analytics tracking
                String package_type = product.getPackageType();
                if(package_type == null)
                    package_type= WhiteLabelApplication.getInstance().getLicenseType().name();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, product.getId().toString());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, product.getTitle());
                bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, package_type);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);


                Intent intent = new Intent(MovieDetailsActivity.this, MovieDetailsActivity.class);
                intent.putExtra(KEY_PRODUCT, relatedMovieItems.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                if (progressView!=null)
                    progressView.dismiss();
                startActivity(intent);
//                MovieDetailsActivity.this.finish();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    @OnClick(R.id.moviePlayImageButton)
    public void play() {
        if (playButtonDown) {
            return;
        }

        playButtonDown = true;
        moviePlayImageButton.setAlpha(0.5f);
        try {
            PlaybackHandler.play(this, product.getId(), entitledVariant);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
        } finally {
            moviePlayImageButton.setAlpha(1.0f);
            playButtonDown = false;
        }
    }

    @OnClick(R.id.moviePlayTrailerButton)
    public void playTrailer() {
        if (trailerProduct != null) {

            if (playButtonDown) {
                return;
            }

            playButtonDown = true;
            moviePlayTrailerButton.setAlpha(0.5f);
            String trailerUrl = trailerProduct.getTrailer().trim().toLowerCase();
            if(trailerUrl.isEmpty() || trailerUrl.equals("not found")){
                AddMessageDialogView dialogView = new AddMessageDialogView(this,"Trailer đang được cập nhật", null, "OK");
                dialogView.show();
                moviePlayTrailerButton.setAlpha(1.0f);
                playButtonDown = false;
            }
            else {
                try {
                    PlaybackHandler.play(this, trailerProduct.getId(), null, null, trailerProduct.getTrailer(),null);
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), e.getMessage());
                } finally {
                    moviePlayTrailerButton.setAlpha(1.0f);
                    playButtonDown = false;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeButton:
                onBackPressed();
                break;
            case R.id.movieWishListButton:
                if (whiteLabelApplication.isUserLoggedIn()) {
                    if (NetworkAvailability.chkStatus(this)) {
                        if (movieWishListButton.getTag().toString().equals("add")) {
                            AddFavouriteProductParser addFavouriteProductParser = new AddFavouriteProductParser();
                            Utils.executeInMultiThread(addFavouriteProductParser);
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.exits_favorite_items), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                    }
                } else {
                    loadPromptUserLoginDialog();
                }
                break;
            case R.id.toggleUpDownArrow:
                if (isScrollWith) {
                    scrollViewMovieDetail.smoothScrollTo(0, 0);
                } else if (((ToggleButton) v).isChecked()) {
                    isToggleWith = true;
                    isScrollWith = false;
                    scrollViewMovieDetail.smoothScrollTo(0, 0);
                    arrowUpDownAnimation(moviePlayButtonLayout, (displayHeight / 3) + statusBarHeight, (displayHeight - (toggleButtonLayoutHeight + statusBarHeight)), true);
                } else {
                    isToggleWith = false;
                    scrollViewMovieDetail.fullScroll(View.FOCUS_UP);
                    scrollViewMovieDetail.smoothScrollTo(0, 1);
                    arrowUpDownAnimation(moviePlayButtonLayout, (displayHeight - (toggleButtonLayoutHeight + statusBarHeight)), (displayHeight / 3) + statusBarHeight, false);
                }
                break;
            case R.id.buttonMovieDetailsSD:
                entitledVariant = "SD";
                setEntitledVariantChecked("SD");
                break;
            case R.id.buttonMovieDetailsHD:
                entitledVariant = "HD";
                setEntitledVariantChecked("HD");
                break;
            case R.id.buttonMovieDetails4K:
                entitledVariant = "4K";
                setEntitledVariantChecked("4K");
                break;
            case R.id.movieShareButton:
                FacebookSdk.sdkInitialize(getApplicationContext());
                CallbackManager callbackManager = CallbackManager.Factory.create();
                final ShareDialog shareDialog = new ShareDialog(this);

                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

                    @Override
                    public void onSuccess(Sharer.Result result) {

                    }

                    @Override
                    public void onError(FacebookException error) {

                    }

                    @Override
                    public void onCancel() {

                    }
                });


                if (shareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle(product.getTitle())
                            .setContentDescription(product.getDescription())
                            .setContentUrl(Uri.parse("http://www.danet.vn/movie/" + product.getId()))
                            .setImageUrl(Uri.parse(ImageRepo.instance().getImage(product.getId(), "poster")))
                            .build();
                    shareDialog.show(linkContent);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onScrollChanged() {
        int scrollY = scrollViewMovieDetail.getScrollY();
        if (!ViewUtils.isTablet(this)) {
            if (scrollY >= 340 && !isScrollWith && isToggleWith) {
                toggleUpDownArrow.setChecked(false);
                isScrollWith = true;
                isToggleWith = false;
            } else if (scrollY < 200 && isScrollWith && !isToggleWith) {
                toggleUpDownArrow.setChecked(true);
                isScrollWith = false;
                isToggleWith = true;
            }
        }

        if (scrollY == 0 && isDown) {
            //toggleUpDownArrow.setChecked(true);
        }
    }

    /*set the animation of arrow up/down*/
    private void arrowUpDownAnimation(final View view, int from, int to, final boolean isGone) {
        ValueAnimator va = ValueAnimator.ofInt(from, to);
        va.setDuration(animationDuration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                view.getLayoutParams().height = value.intValue();
                view.requestLayout();
            }
        });

        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                disableScrollViewScrolling();
                isDown = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isGone) {
                    //movieToggleDownLayout.setVisibility(View.GONE);
                } else {
                    if (relatedMovieItems != null && relatedMovieItems.size() > 0)
                        progressBarRelated.setVisibility(View.GONE);
                }
                scrollViewMovieDetail.setOnTouchListener(null);
                isDown = true;
            }
        });
        va.start();
    }

    /* set disable the scrollView scrolling method*/
    private void disableScrollViewScrolling() {
        scrollViewMovieDetail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    /* load showing blur image in background */
    private void setBackgroundImage(final View view, String imageUrl) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                view.setBackground(new BitmapDrawable(getResources(), BlurBuilder.blur(MovieDetailsActivity.this, bitmap)));
            }

            @Override
            public void onBitmapFailed(final Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {

            }
        };
        view.setTag(target);
        Picasso.with(this).load(imageUrl).resize(displayWidth, displayHeight).into(target);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateProductInfoParser updateProductInfoParser = new UpdateProductInfoParser();
        Utils.executeInMultiThread(updateProductInfoParser);
    }

    @Override
    public void onRequestCompleted(List<Product> productList) {
        try {
            if (productList != null && productList.size() > 0) {
                for (Product currentProduct : productList) {
                    if (currentProduct.getId().toString().trim().equals(product.getId().toString().trim())) {
                        movieWishListButton.setTag("added");
                        movieWishListButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wishlist_added, 0, 0, 0);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onRequestFail(Throwable throwable) {
    }

    private String getTimeLeftString(List<Offerings> offeringsList) {
        String[] timePostfixes = getResources().getStringArray(R.array.string_array_my_library_rent_due_period);

        for (Offerings offering : offeringsList) {
            if (offering.getEntitled() && LicenseType.TVOD.toString().equalsIgnoreCase(offering.getType())) {
                Date currentDate = new Date();

                long diffInMillies = offering.getEndDate().getTime() - currentDate.getTime();
                long timePeriod = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
                timePeriod = Math.abs(timePeriod);

                if (timePeriod < 60) {
                    return "CÒN " + timePeriod + " " + timePostfixes[0];
                } else if (timePeriod < 1440) {
                    return "CÒN " + timePeriod / 60 + " " + timePostfixes[1];
                } else {
                    return "CÒN " + timePeriod / 1440 + " " + timePostfixes[2];
                }
            }
        }
        return "";
    }

    private void loadPromptUserLoginDialog() {

        AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(this, getString(R.string.dialog_msg_prompt_login), null, getString(R.string.label_sign_in), getString(R.string.label_cancel), new DialogEventListeners() {
            @Override
            public void onPositiveButtonClick(DialogInterface dialog) {

                Intent intent = new Intent(MovieDetailsActivity.this, LoginActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }

            @Override
            public void onNegativeButtonClick(DialogInterface dialog) {
            }
        });
        dialogView.show();
    }

    private void handleContentRetrievingError(String error, ProgressBar progressBar, AuthenticationUserResultReceived listener) {
        if (error != null && error.equals(getResources().getString(R.string.un_authorised_exception_code)) && whiteLabelApplication.isUserLoggedIn()) {
            User userInfo = whiteLabelApplication.getUser();
            if (userInfo == null) {
                userInfo = PreferenceHelper.getSharedPrefData(MovieDetailsActivity.this, getResources().getString(R.string.user_info), User.class);
            }
            if (userInfo != null) {
                String[] params = {userInfo.getProvider(), userInfo.getIdentifier(), userInfo.getPassword()};
                AuthenticationParser authenticationParser = new AuthenticationParser(MovieDetailsActivity.this, listener);
                Utils.executeInMultiThread(authenticationParser, params);
            } else {
                loadPromptUserLoginDialog();
            }
        } else {
            if (progressBar != null)
                progressBar.setVisibility(View.GONE);
            Toast.makeText(MovieDetailsActivity.this, error, Toast.LENGTH_LONG).show();
        }
    }

    /*Call webservice for load related products for this product*/
    public class RelatedVideoParser extends AsyncTask<Object, String, Products> {

        private String error = null;
        private String authToken = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                authToken = whiteLabelApplication.getAccessToken();
            } catch (Exception e) {
                authToken = null;
            }

        }

        @Override
        protected Products doInBackground(Object... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();
            Products products = null;
            try {
                if (whiteLabelApplication.getLicenseType().name().equalsIgnoreCase("avod"))
                    products = contentHandler.getRelatedProducts(null, product.getId().toString());
                else
                    products = contentHandler.getRelatedProducts(authToken, product.getId().toString());
            } catch (Exception e) {
                error = e.getMessage();
            }
            return products;
        }

        @Override
        protected void onPostExecute(Products products) {
            super.onPostExecute(products);
            if (error == null && products != null) {
                if(products.getCount() <= 0){
                    relatedMovieRecyclerView.setVisibility(View.GONE);
                    movieRelatedHeaderTextView.setVisibility(View.GONE);
                } else {
                    relatedMovieRecyclerView.setVisibility(View.VISIBLE);
                    movieRelatedHeaderTextView.setVisibility(View.VISIBLE);
                }

                if (progressBarRelated.isShown())
                    progressBarRelated.setVisibility(View.GONE);
                relatedMovieRecyclerView.setVisibility(View.VISIBLE);
                relatedMovieItems = products.getProductList();
                int scrollY = scrollViewMovieDetail.getScrollY();
                setRelatedVideos();
                scrollViewMovieDetail.setScrollY(scrollY);
            } else {
                relatedMovieRecyclerView.setVisibility(View.GONE);
                movieRelatedHeaderTextView.setVisibility(View.GONE);

                handleContentRetrievingError(error, progressBarRelated, new AuthenticationUserResultReceived() {
                    @Override
                    public void onResult(String error, User result) {
                        if (error == null && result != null) {
                            RelatedVideoParser relatedVideoParser = new RelatedVideoParser();
                            Utils.executeInMultiThread(relatedVideoParser);
                        } else {
                            progressBarRelated.setVisibility(View.GONE);
                            Toast.makeText(MovieDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    /*Call webservice for save favourite  product*/
    public class AddFavouriteProductParser extends AsyncTask<Object, String, Favourite> {

        private String error = null;
        private String authToken = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressView.show();
            try {
//                authToken = PreferenceHelper.getSharedPrefData(MovieDetailsActivity.this, getResources().getString(R.string.user_info), User.class).getAccessToken();
                authToken = whiteLabelApplication.getAccessToken();
            } catch (Exception e) {
                authToken = null;
            }
        }

        @Override
        protected Favourite doInBackground(Object... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();
            Favourite favourite = null;
            try {
                favourite = contentHandler.addFavourite(authToken, product.getId().toString());
            } catch (Exception e) {
                error = e.getMessage();
            }
            return favourite;
        }

        @Override
        protected void onPostExecute(Favourite favourite) {
            super.onPostExecute(favourite);
            if (progressView!=null)
                progressView.dismiss();
            if (error == null && favourite.getSuccess()) {
                Toast.makeText(MovieDetailsActivity.this, getResources().getString(R.string.added_favorite_items), Toast.LENGTH_LONG).show();
                movieWishListButton.setTag("added");
                movieWishListButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wishlist_added, 0, 0, 0);
            } else {
                handleContentRetrievingError(error, null, new AuthenticationUserResultReceived() {
                    @Override
                    public void onResult(String error, User result) {
                        if (error == null && result != null) {
                            AddFavouriteProductParser addFavouriteProductParser = new AddFavouriteProductParser();
                            Utils.executeInMultiThread(addFavouriteProductParser);
                        } else {
                            Toast.makeText(MovieDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    /*Call webservice for reload product*/
    public class UpdateProductInfoParser extends AsyncTask<Object, String, Product> {

        private String error = null;
        private String authToken = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                authToken = whiteLabelApplication.getAccessToken();
            } catch (Exception e) {
                authToken = null;
            }
        }

        @Override
        protected Product doInBackground(Object... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();
            Product mProduct = null;
            try {
                mProduct = contentHandler.getProductById(authToken, product.getId().toString());
            } catch (Exception e) {
                error = e.getMessage();
            }
            return mProduct;
        }

        @Override
        protected void onPostExecute(Product productResult) {
            super.onPostExecute(productResult);
            if (error == null && productResult != null) {
                product = productResult;
                setWidgetsValues();
            } else {
                handleContentRetrievingError(error, null, new AuthenticationUserResultReceived() {
                    @Override
                    public void onResult(String error, User result) {
                        if (error == null && result != null) {
                            AddFavouriteProductParser addFavouriteProductParser = new AddFavouriteProductParser();
                            Utils.executeInMultiThread(addFavouriteProductParser);
                        } else {
                            Toast.makeText(MovieDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    public class MovieTrailerTask extends AsyncTask<String, Void, Products> {

        private String authToken;
        private Exception error;

        @Override
        protected void onPreExecute() {
            authToken = WhiteLabelApplication.getInstance().getAccessToken();
        }

        @Override
        protected Products doInBackground(String... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();
            Products childProducts = null;
            try {
                //childProducts = contentHandler.getChildProducts(authToken, params[0]);
                List<Product> productList = new ArrayList<Product>();
                productList.add(product);
                childProducts = new Products();
                childProducts.setCount(1);
                childProducts.setProductList(productList);
                childProducts.setObject("trailer");

            } catch (Exception e) {
                error = e;
            }

            return childProducts;
        }

        @Override
        protected void onPostExecute(Products childProducts) {
            if (childProducts != null && childProducts.getProductList() != null) {
                for (Product childProduct : childProducts.getProductList()) {
                   // if ("trailer".equalsIgnoreCase(childProduct.getType())) {
                        trailerProduct = childProduct;
                        moviePlayTrailerButton.setEnabled(true);
                        moviePlayTrailerButton.setAlpha(1f);
                        break;
                   // }
                }
            }
        }

    }
}
