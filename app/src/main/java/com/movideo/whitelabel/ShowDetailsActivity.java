package com.movideo.whitelabel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
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
import com.movideo.whitelabel.adapter.SeasonsTabletAdapter;
import com.movideo.whitelabel.communication.AuthenticationParser;
import com.movideo.whitelabel.communication.AuthenticationUserResultReceived;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetWishListTask;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.fragment.SeasonsExpandableListViewFragment;
import com.movideo.whitelabel.util.BlurBuilder;
import com.movideo.whitelabel.util.DialogEventListeners;
import com.movideo.whitelabel.util.DisplayResources;
import com.movideo.whitelabel.util.NetworkAvailability;
import com.movideo.whitelabel.util.PlaybackHandler;
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

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ShowDetailsActivity extends AppCompatActivity implements ViewTreeObserver.OnScrollChangedListener, View.OnClickListener, ContentRequestListener<List<Product>> {

    public static final String KEY_PRODUCT = "show_details_product";
    public static final int animationDuration = 300;
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    @Bind(R.id.headerTitleTextView)
    TextView headerTitleTextView;
    @Bind(R.id.closeImageButton)
    ImageButton closeImageButton;
    @Bind(R.id.closeButton)
    Button closeButton;
    @Bind(R.id.scrollViewShowDetail)
    ParallaxScrollView scrollViewShowDetail;
    @Bind(R.id.showFullImageView)
    ImageView showFullImageView;
    @Bind(R.id.imageViewShowBgBlur)
    ImageView imageViewShowBgBlur;
    @Bind(R.id.showPlayButtonLayout)
    RelativeLayout showPlayButtonLayout;
    @Bind(R.id.showPlayImageButton)
    ImageButton showPlayImageButton;
    @Bind(R.id.daysLeftTextView)
    TextView daysLeftTextView;
    @Bind(R.id.showTitleTextView)
    TextView showTitleTextView;
    @Bind(R.id.showTimeTextView)
    TextView showTimeTextView;
    @Bind(R.id.toggleUpDownArrow)
    ToggleButton toggleUpDownArrow;
    @Bind(R.id.showToggleDownLayout)
    LinearLayout showToggleDownLayout;
    @Bind(R.id.showDescTextView)
    TextView showDescTextView;
    @Bind(R.id.showGenresTextView)
    TextView showGenresTextView;
    @Bind(R.id.showCastCrewActorTextView)
    TextView showCastCrewActorTextView;
    @Bind(R.id.showCastCrewDirectorTextView)
    TextView showCastCrewDirectorTextView;
    @Bind(R.id.showLangHeaderTextView)
    TextView showLangTextView;
    @Bind(R.id.showRelatedHeaderTextView)
    TextView showRelatedHeaderTextView;
    @Bind(R.id.showPlayTrailerButton)
    Button showPlayTrailerButton;
    @Bind(R.id.showWishListButton)
    Button showWishListButton;
    @Bind(R.id.relatedShowRecyclerView)
    RecyclerView relatedShowRecyclerView;
    @Bind(R.id.layoutRelated)
    RelativeLayout layoutRelated;
    @Bind(R.id.dividerViewRelatedItems)
    View dividerViewRelatedItems;
    @Bind(R.id.horizontal_scroll_view)
    HorizontalScrollView horizontalScrollView;
    @Bind(R.id.horizontal_scroll_linear)
    LinearLayout horizontalScrollLinearLayout;
    @Bind(R.id.expandableListFragmentContainer)
    FrameLayout expandableListFragmentContainer;
    @Bind(R.id.progressBarRelated)
    ProgressBar progressBarRelated;
    @Bind(R.id.progressBarCounter)
    ProgressBar progressBarCounter;
    @Bind(R.id.progressBarSeasons)
    ProgressBar progressBarSeasons;
    @Bind(R.id.showSmallImageViewPoster)
    ImageView showSmallImageViewPoster;
    @Bind(R.id.seasonsShowRecyclerView)
    RecyclerView seasonsShowRecyclerView;
    @Bind(R.id.buttonMovieDetailsSD)
    ToggleButton buttonSD;
    @Bind(R.id.buttonMovieDetailsHD)
    ToggleButton buttonHD;
    @Bind(R.id.buttonMovieDetails4K)
    ToggleButton button4K;
    @Bind(R.id.shareButton)
    Button shareButton;



    private boolean isDown = false;
    private boolean isScrollWith = false;
    private boolean isToggleWith = true;
    private int displayHeight;
    private int displayWidth;
    private int statusBarHeight;
    private int toggleButtonLayoutHeight;
    private String entitledVariant;
    private RelatedMovieAdapter relatedMovieAdapter;
    private List<Product> relatedMovieItems;
    private ProgressView progressView;
    private Product product;
    private DisplayResources displayResources;
    private List<Product> seasonsProductsList;
    private WhiteLabelApplication whiteLabelApplication;
    private SeasonsExpandableListViewFragment fragment;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail);
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

        closeImageButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        showPlayImageButton.setOnClickListener(this);
        showPlayTrailerButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);

        showWishListButton.setOnClickListener(this);
        scrollViewShowDetail.getViewTreeObserver().addOnScrollChangedListener(this);
        scrollViewShowDetail.fullScroll(View.FOCUS_UP);

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
            /*movie image height set according to device height*/
            //showFullImageView.getLayoutParams().height = displayHeight - (toggleButtonLayoutHeight + statusBarHeight);

            showPlayButtonLayout.getLayoutParams().height = displayHeight - (toggleButtonLayoutHeight + statusBarHeight);
        }

        scrollViewShowDetail.setParallaxImageView(showFullImageView);
        scrollViewShowDetail.setZoomRatio(ParallaxScrollView.NO_ZOOM);
        scrollViewShowDetail.setScrollSpeed(ParallaxScrollView.SCROLL_SPEED_X0_5);

        /*get product value from intent*/
        product = (Product) getIntent().getSerializableExtra(KEY_PRODUCT);
        /*set all widgets/view values*/

        ProductInfoTask productInfoTask = new ProductInfoTask();
        ViewUtils.execute(productInfoTask);
        setWidgetsValues();
    }

    /*set all widgets/view values*/
    private void setWidgetsValues() {
        if (!ViewUtils.isTablet(this)) {
            toggleUpDownArrow.setOnClickListener(this);
            showToggleDownLayout.setVisibility(View.VISIBLE);
            expandableListFragmentContainer.setVisibility(View.VISIBLE);
            seasonsShowRecyclerView.setVisibility(View.GONE);
        } else {
            Picasso.with(this).load(ViewUtils.getImageUrlOfProduct(ShowDetailsActivity.this, product, Page.MOVIE_DETAIL_PAGE, ImageURL.ImageProfile.POSTER))
                    .resizeDimen(R.dimen.movie_poster_small_width, R.dimen.movie_poster_small_height)
                    .onlyScaleDown()
                    .noFade()
                    .placeholder(R.drawable.fallback_poster)
                    .into(showSmallImageViewPoster);
            expandableListFragmentContainer.setVisibility(View.GONE);
            seasonsShowRecyclerView.setVisibility(View.VISIBLE);
        }

        setTitleText();
        progressBarRelated.setVisibility(View.VISIBLE);
        progressBarCounter.setVisibility(View.VISIBLE);
        progressBarSeasons.setVisibility(View.VISIBLE);

        relatedShowRecyclerView.setVisibility(View.VISIBLE);


        /*set product image*/
        ImageURL.ImageProfile profile = ImageURL.ImageProfile.HERO_BANNER;
        if (displayHeight < displayWidth){
            profile = ImageURL.ImageProfile.HERO_BANNER_LANDSCAPE;
        }

        Picasso.with(this).load(ViewUtils.getImageUrlOfProduct(ShowDetailsActivity.this, product, Page.SHOW_DETAIL_PAGE, profile))
                .resize(displayWidth, displayHeight)
                .onlyScaleDown()
                .noFade()
                .placeholder(R.drawable.fallback_poster)
                .into(showFullImageView);

        /*set background blur image */
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                setBackgroundImage(imageViewShowBgBlur, ViewUtils.getImageUrlOfProduct(ShowDetailsActivity.this, product, Page.SHOW_DETAIL_PAGE, ImageURL.ImageProfile.BACKGROUND));
            }
        });

        daysLeftTextView.setText("");
        daysLeftTextView.setVisibility(View.GONE);


        showCastCrewActorTextView.setText("Diễn viên - ");

        showCastCrewDirectorTextView.setText("Đạo diễn - ");

        RelatedVideoParser relatedVideoParser = new RelatedVideoParser();
        ViewUtils.execute(relatedVideoParser);

        SeasonProductsParser seasonProductsParser = new SeasonProductsParser();
        ViewUtils.execute(seasonProductsParser);

        Integer[] param = new Integer[]{0, 0};
        GetWishListTask getWishListTask = new GetWishListTask(this);
        getWishListTask.execute(param);

        loadVariants();
    }

    private void showProductInfo(){
        if (product.getTitle() != null && !product.getTitle().equals("null")) {
            showTitleTextView.setText(product.getTitle());
        } else {
            showTitleTextView.setText("");
        }

        if (product.getReleased() != null && !product.getReleased().equals("null")) {
            showTimeTextView.setText(product.getReleased());
        } else {
            showTimeTextView.setText("");
        }

        showTimeTextView.append(" ");
        if (product.getDuration() != null && !product.getDuration().equals("null")) {
            showTimeTextView.setText(product.getDuration());
        }

        if (product.getDescription() != null && !product.getDescription().equals("null")) {
            showDescTextView.setText(product.getDescription());
        } else {
            showDescTextView.setText("");
        }

        if (product.getGenres() != null && product.getGenres().size() > 0) {
            showGenresTextView.setText(TextUtils.join(", ", product.getGenres()));
        } else {
            showGenresTextView.setText("");
        }

        if (product.getActors() != null && product.getActors().size() > 0) {
            showCastCrewActorTextView.append(TextUtils.join(", ", product.getActors()));
        }

        if (product.getDirectors() != null && product.getDirectors().size() > 0) {
            showCastCrewDirectorTextView.append(TextUtils.join(", ", product.getDirectors()));
        }

        if (product.getLanguage() != null && product.getLanguage().getSubtitles().size() > 0) {
            showLangTextView.setText(TextUtils.join(", ", product.getLanguage().getSubtitles()));
        } else {
            showLangTextView.setText("");
        }


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

            if ("SD".equals(offering.getVariant().toUpperCase())) {
                buttonSD.setVisibility(View.VISIBLE);
                if (!offering.getEntitled()) {
                    buttonSD.setEnabled(false);
                    buttonSD.setAlpha(0.5f);
                }
            }
            if ("HD".equals(offering.getVariant().toUpperCase())) {
                buttonHD.setVisibility(View.VISIBLE);
                if (!offering.getEntitled()) {
                    buttonHD.setEnabled(false);
                    buttonHD.setAlpha(0.5f);
                }
            }
            if ("4K".equals(offering.getVariant().toUpperCase())) {
                button4K.setVisibility(View.VISIBLE);
                if (!offering.getEntitled()) {
                    button4K.setEnabled(false);
                    button4K.setAlpha(0.5f);
                }
            }
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
        if(ViewUtils.isTablet(this)) {
            if(seasonsShowRecyclerView.getAdapter() != null) ((SeasonsTabletAdapter) seasonsShowRecyclerView.getAdapter()).setEntitledVariant(entitledVariant);
        }else{
            if(fragment != null) fragment.setEntitledVariant(entitledVariant);
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

    /*Create TextView with round Circle background for set seasons count*/
    private void seasonsTypeCount() {
        for (int i = 1; i <= seasonsProductsList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            TextView tv = (TextView) inflater.inflate(R.layout.season_text_view_item, null);
            tv.setText("" + i);
            if (i == 1) {
                tv.setBackground(displayResources.getDrawable(R.drawable.circle_highlight));
                tv.setSelected(true);
            }
            tv.setTag(i-1);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected()) {

                        for (int j = 0; j < horizontalScrollLinearLayout.getChildCount(); j++) {
                            TextView textView = (TextView) horizontalScrollLinearLayout.getChildAt(j);
                            textView.setBackground(null);
                            textView.setSelected(false);
                        }
                        v.setBackground(displayResources.getDrawable(R.drawable.circle_highlight));
                        v.setSelected(true);
                        PlaybackHandler.play(ShowDetailsActivity.this, product.getId(), "", seasonsProductsList.get((int) v.getTag()).getId(), null, seasonsProductsList.get((int) v.getTag()).getTitle());

//                        if (ViewUtils.isTablet(ShowDetailsActivity.this)) {
//                            seasonsRecycleView((int) v.getTag() - 1);
//                        } else {
//                            seasonsFragment((int) v.getTag() - 1);
//                        }
                    }
                }
            });
            horizontalScrollLinearLayout.addView(tv);
        }
    }

    /*load seasons fragment for showing ExpandableListView*/
    private void seasonsFragment(int position) {
        if (seasonsProductsList.size() > 0) {
            renderEpisode();
        } else {
            expandableListFragmentContainer.setVisibility(View.GONE);
            progressBarSeasons.setVisibility(View.GONE);
        }
    }

    /*load seasons RecyclerView for showing horizontal list item for Tablet*/
    private void seasonsRecycleView(int position) {
        if (seasonsProductsList.size() > 0) {
            seasonsShowRecyclerView.setVisibility(View.GONE);
            progressBarSeasons.setVisibility(View.VISIBLE);
            renderEpisode();
        } else {
            seasonsShowRecyclerView.setVisibility(View.GONE);
            progressBarSeasons.setVisibility(View.GONE);
        }

    }


    private void setRelatedVideos() {
        relatedMovieAdapter = new RelatedMovieAdapter(this, relatedMovieItems);
        relatedShowRecyclerView.setAdapter(relatedMovieAdapter);
        relatedShowRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        relatedShowRecyclerView.setLayoutManager(layoutManager);
        relatedShowRecyclerView.addItemDecoration(new SpaceDecoration(this, R.dimen.horizontal_list_space,
                true, true));

        relatedShowRecyclerView.addOnItemTouchListener(new RelatedItemTouchListener(this, relatedShowRecyclerView, new ClickListener() {
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

                Intent intent = new Intent(ShowDetailsActivity.this, ShowDetailsActivity.class);
                intent.putExtra(KEY_PRODUCT, relatedMovieItems.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
//                ShowDetailsActivity.this.finish();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeButton:
                onBackPressed();
                break;
            case R.id.showPlayImageButton:
                PlaybackHandler.play(this, product.getId(), entitledVariant);
                break;
            case R.id.showPlayTrailerButton:
                String trailerUrl = product.getTrailer().trim().toLowerCase();
                if(trailerUrl.isEmpty() || trailerUrl.equals("not found")){
                    AddMessageDialogView dialogView = new AddMessageDialogView(this,"Trailer đang được cập nhật", null, "OK");
                    dialogView.show();
                }
                else
                    PlaybackHandler.playTrailer(this, product.getId(), entitledVariant, product.getTrailer());
                break;
            case R.id.showWishListButton:
                if (PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_is_logged_in))) {
                    if (NetworkAvailability.chkStatus(this)) {
                        if (showWishListButton.getTag().toString().equals("add")) {
                            AddFavouriteProductParser addFavouriteProductParser = new AddFavouriteProductParser();
                            ViewUtils.execute(addFavouriteProductParser);
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
                    scrollViewShowDetail.smoothScrollTo(0, 0);
                } else if (((ToggleButton) v).isChecked()) {
                    isToggleWith = true;
                    isScrollWith = false;
                    scrollViewShowDetail.smoothScrollTo(0, 0);
                    arrowUpDownAnimation(showPlayButtonLayout, (displayHeight / 3) + statusBarHeight, (displayHeight - (toggleButtonLayoutHeight + statusBarHeight)), true);
                } else {
                    isToggleWith = false;
                    scrollViewShowDetail.fullScroll(View.FOCUS_UP);
                    scrollViewShowDetail.smoothScrollTo(0, 1);
                    arrowUpDownAnimation(showPlayButtonLayout, (displayHeight - (toggleButtonLayoutHeight + statusBarHeight)), (displayHeight / 3) + statusBarHeight, false);
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
            case R.id.shareButton:
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
                            .setContentUrl(Uri.parse("http://www.danet.vn/series/" + product.getId()))
                            .setImageUrl(Uri.parse(ImageRepo.instance().getImage(product.getId(), "poster")))
                            .build();
                    shareDialog.show(linkContent);
                }


//                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                sharingIntent.setType("image/*");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, product.getTitle());
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, product.getDescription());
//                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(ImageRepo.instance().getImage(product.getId(), "poster", ShowDetailsActivity.this.getResources().getDisplayMetrics().densityDpi)));
//                startActivity(Intent.createChooser(sharingIntent, "Chia sẻ bằng: "));
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        ShowDetailsActivity.this.finish();
    }

    @Override
    public void onScrollChanged() {
        int scrollY = scrollViewShowDetail.getScrollY();
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
                    //showToggleDownLayout.setVisibility(View.GONE);
                } else {
                    if (relatedMovieItems != null && relatedMovieItems.size() > 0)
                        progressBarRelated.setVisibility(View.GONE);
                    if (seasonsProductsList != null && seasonsProductsList.size() > 0)
                        progressBarCounter.setVisibility(View.GONE);
                    if (expandableListFragmentContainer.getVisibility() == View.VISIBLE)
                        progressBarSeasons.setVisibility(View.GONE);
                }
                scrollViewShowDetail.setOnTouchListener(null);
                isDown = true;
            }
        });
        va.start();
    }

    /* set disable the scrollView scrolling method*/
    private void disableScrollViewScrolling() {
        scrollViewShowDetail.setOnTouchListener(new View.OnTouchListener() {
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
                view.setBackground(new BitmapDrawable(getResources(), BlurBuilder.blur(ShowDetailsActivity.this, bitmap)));

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
    public void onRequestCompleted(List<Product> productList) {
        try {
            if (productList != null && productList.size() > 0) {
                for (Product currentProduct : productList) {
                    if (currentProduct.getId().toString().trim().equals(product.getId().toString().trim())) {
                        showWishListButton.setTag("added");
                        showWishListButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wishlist_added, 0, 0, 0);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onRequestFail(Throwable throwable) {

    }

    private void loadPromptUserLoginDialog() {

        AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(this, getString(R.string.dialog_msg_prompt_login), null, getString(R.string.label_sign_in), getString(R.string.label_cancel), new DialogEventListeners() {
            @Override
            public void onPositiveButtonClick(DialogInterface dialog) {

                Intent intent = new Intent(ShowDetailsActivity.this, LoginActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }

            @Override
            public void onNegativeButtonClick(DialogInterface dialog) {
            }
        });
        dialogView.show();
    }

    /*Call webservice for load related products for this product*/
    public class RelatedVideoParser extends AsyncTask<Object, String, Products> {

        private String error = null;
        private String authToken = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                authToken = PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_info), User.class).getAccessToken();
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
            try {
                if (error == null && products != null) {

                    if(products.getCount() <= 0){
                        relatedShowRecyclerView.setVisibility(View.GONE);
                        showRelatedHeaderTextView.setVisibility(View.GONE);
                        layoutRelated.setVisibility(View.GONE);
                        dividerViewRelatedItems.setVisibility(View.GONE);
                    } else {
                        relatedShowRecyclerView.setVisibility(View.VISIBLE);
                        showRelatedHeaderTextView.setVisibility(View.VISIBLE);
                        layoutRelated.setVisibility(View.VISIBLE);
                        dividerViewRelatedItems.setVisibility(View.VISIBLE);
                    }

                    if (progressBarRelated.isShown())
                        progressBarRelated.setVisibility(View.GONE);
                    relatedShowRecyclerView.setVisibility(View.VISIBLE);
                    relatedMovieItems = products.getProductList();
                    int scrollY = scrollViewShowDetail.getScrollY();
                    setRelatedVideos();
                    scrollViewShowDetail.setScrollY(scrollY);

                } else {
                    relatedShowRecyclerView.setVisibility(View.GONE);
                    showRelatedHeaderTextView.setVisibility(View.GONE);
                    layoutRelated.setVisibility(View.GONE);
                    dividerViewRelatedItems.setVisibility(View.GONE);

                    if (error != null && error.equals(getResources().getString(R.string.un_authorised_exception_code)) && PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_is_logged_in))) {
                        User userInfo = PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_info), User.class);
                        String[] params = {userInfo.getProvider(), userInfo.getIdentifier(), userInfo.getPassword()};
                        AuthenticationParser authenticationParser = new AuthenticationParser(ShowDetailsActivity.this, new AuthenticationUserResultReceived() {
                            @Override
                            public void onResult(String error, User result) {
                                if (error == null && result != null) {
                                    RelatedVideoParser relatedVideoParser = new RelatedVideoParser();
                                    ViewUtils.execute(relatedVideoParser);
                                } else {
                                    progressBarRelated.setVisibility(View.GONE);
                                    Toast.makeText(ShowDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        ViewUtils.execute(authenticationParser, params);
                    } else {
                        progressBarRelated.setVisibility(View.GONE);
                        Toast.makeText(ShowDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    /*Call webservice for load season products for this product*/
    public class SeasonProductsParser extends AsyncTask<Object, String, Products> {

        private String error = null;
        private String authToken = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                authToken = PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_info), User.class).getAccessToken();
            } catch (Exception e) {
                authToken = null;
            }
        }

        @Override
        protected Products doInBackground(Object... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();
            Products products = null;
            try {
                products = contentHandler.getChildProducts(authToken, product.getId().toString());

                for (Product childProduct : products.getProductList()){
                    childProduct.setParentProduct(product);
                }
            } catch (Exception e) {
                error = e.getMessage();
            }
            return products;
        }

        @Override
        protected void onPostExecute(Products products) {
            super.onPostExecute(products);
            try {
                if (error == null && products != null) {
                    if (progressBarCounter.isShown())
                        progressBarCounter.setVisibility(View.GONE);
                    seasonsProductsList = products.getProductList();
                    if (ViewUtils.isTablet(ShowDetailsActivity.this)) {
                        seasonsRecycleView(0);
                    } else {
                        seasonsFragment(0);
                    }
                    seasonsTypeCount();
                    int scrollY = scrollViewShowDetail.getScrollY();
                    scrollViewShowDetail.setScrollY(scrollY);
                } else {
                    if (error != null && error.equals(getResources().getString(R.string.un_authorised_exception_code)) && PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_is_logged_in))) {
                        User userInfo = PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_info), User.class);
                        String[] params = {userInfo.getProvider(), userInfo.getIdentifier(), userInfo.getPassword()};
                        AuthenticationParser authenticationParser = new AuthenticationParser(ShowDetailsActivity.this, new AuthenticationUserResultReceived() {
                            @Override
                            public void onResult(String error, User result) {
                                if (error == null && result != null) {
                                    SeasonProductsParser seasonProductsParser = new SeasonProductsParser();
                                    ViewUtils.execute(seasonProductsParser);
                                } else {
                                    progressBarCounter.setVisibility(View.GONE);
                                    Toast.makeText(ShowDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        ViewUtils.execute(authenticationParser, params);
                    } else {
                        progressBarCounter.setVisibility(View.GONE);
                        Toast.makeText(ShowDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
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
                authToken = PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_info), User.class).getAccessToken();
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
            try {
                if (progressView!=null)
                    progressView.dismiss();
                if (error == null && favourite.getSuccess()) {
                    Toast.makeText(ShowDetailsActivity.this, getResources().getString(R.string.added_favorite_items), Toast.LENGTH_LONG).show();
                    showWishListButton.setTag("added");
                    showWishListButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.wishlist_added, 0, 0, 0);
                } else {
                    if (error != null && error.equals(getResources().getString(R.string.un_authorised_exception_code)) && PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_is_logged_in))) {
                        User userInfo = PreferenceHelper.getSharedPrefData(ShowDetailsActivity.this, getResources().getString(R.string.user_info), User.class);
                        String[] params = {userInfo.getProvider(), userInfo.getIdentifier(), userInfo.getPassword()};
                        AuthenticationParser authenticationParser = new AuthenticationParser(ShowDetailsActivity.this, new AuthenticationUserResultReceived() {
                            @Override
                            public void onResult(String error, User result) {
                                if (error == null && result != null) {
                                    AddFavouriteProductParser addFavouriteProductParser = new AddFavouriteProductParser();
                                    ViewUtils.execute(addFavouriteProductParser);
                                } else {
                                    Toast.makeText(ShowDetailsActivity.this,error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        ViewUtils.execute(authenticationParser, params);
                    } else
                        Toast.makeText(ShowDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }


    private void renderEpisode()
    {
        Products products = new Products();
        products.setObject("episode");
        products.setProductList(seasonsProductsList);
        products.setCount(seasonsProductsList.size());

        int scrollY = scrollViewShowDetail.getScrollY();
        if (ViewUtils.isTablet(ShowDetailsActivity.this)) {
            seasonsShowRecyclerView.setVisibility(View.VISIBLE);
            expandableListFragmentContainer.setVisibility(View.GONE);
            progressBarSeasons.setVisibility(View.GONE);

            seasonsShowRecyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(ShowDetailsActivity.this,
                    LinearLayoutManager.HORIZONTAL, false);
            seasonsShowRecyclerView.setLayoutManager(layoutManager);
            seasonsShowRecyclerView.addItemDecoration(new SpaceDecoration(ShowDetailsActivity.this, R.dimen.horizontal_list_space,
                    true, true));

            seasonsShowRecyclerView.setAdapter(new SeasonsTabletAdapter(ShowDetailsActivity.this, products.getProductList(), entitledVariant));

            seasonsShowRecyclerView.addOnItemTouchListener(new RelatedItemTouchListener(ShowDetailsActivity.this, seasonsShowRecyclerView, new ClickListener() {
                @Override
                public void onClick(View view, int position) {

                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));
        } else {
            expandableListFragmentContainer.setVisibility(View.VISIBLE);
            seasonsShowRecyclerView.setVisibility(View.GONE);
            progressBarSeasons.setVisibility(View.GONE);

            fragment = SeasonsExpandableListViewFragment.newInstance(entitledVariant, products);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.expandableListFragmentContainer, fragment);
            ft.commit();
        }
        scrollViewShowDetail.setScrollY(scrollY);
    }



    public class ProductInfoTask extends AsyncTask<Object, String, Product> {

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
                mProduct = contentHandler.getProductById(authToken,product.getId().toString());
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
                showProductInfo();
            } else {
                progressBarSeasons.setVisibility(View.GONE);
                Toast.makeText(ShowDetailsActivity.this, error, Toast.LENGTH_LONG).show();

            }
        }
    }
}