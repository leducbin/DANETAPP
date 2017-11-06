package com.movideo.whitelabel;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.product.Products;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.adapter.RelatedClipAdapter;
import com.movideo.whitelabel.communication.AuthenticationParser;
import com.movideo.whitelabel.communication.AuthenticationUserResultReceived;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.BlurBuilder;
import com.movideo.whitelabel.util.DisplayResources;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.ClickListener;
import com.movideo.whitelabel.view.RelatedItemTouchListener;
import com.movideo.whitelabel.view.SpaceDecoration;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ClipDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String KEY_PRODUCT = "show_details_product";

    @Bind(R.id.imageViewClipBgBlur)
    ImageView imageViewClipBgBlur;


    @Bind(R.id.headerTitleTextView)
    TextView headerTitleTextView;
    @Bind(R.id.closeImageButton)
    ImageButton closeImageButton;

    @Bind(R.id.clipScrollView)
    ScrollView clipScrollView;

    @Bind(R.id.clipThumbImageView)
    ImageView clipThumbImageView;
    @Bind(R.id.clipPlayImageButton)
    ImageButton clipPlayImageButton;

    @Bind(R.id.clipTitleTextView)
    TextView clipTitleTextView;
    @Bind(R.id.clipTimeTextView)
    TextView clipTimeTextView;
    @Bind(R.id.clipDescTextView)
    TextView clipDescTextView;
    @Bind(R.id.clipRelatedHeaderTextView)
    TextView clipRelatedHeaderTextView;

    @Bind(R.id.relatedClipDetailRecyclerView)
    RecyclerView relatedClipDetailRecyclerView;
    @Bind(R.id.progressBarRelated)
    ProgressBar progressBarRelated;

    private List<Product> relatedClipItems;

    private DisplayResources displayResources;
    private int displayHeight;
    private int displayWidth;

    private Product product;
    private WhiteLabelApplication whiteLabelApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_details);
        ButterKnife.bind(this);

        if (ViewUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        /*Get the application instance and get License Type*/
        whiteLabelApplication = WhiteLabelApplication.getInstance();

        closeImageButton.setOnClickListener(this);
        clipPlayImageButton.setOnClickListener(this);
        clipScrollView.fullScroll(View.FOCUS_UP);

        /* get device resources (navigation/status bar height)*/
        displayResources = new DisplayResources(this);
        displayHeight = displayResources.getDisplayHeight();
        displayWidth = displayResources.getDisplayWidth();

        /*set all widgets/view values*/
        product = (Product) getIntent().getSerializableExtra(KEY_PRODUCT);

        setWidgetsValues();
        progressBarRelated.setVisibility(View.VISIBLE);
        relatedClipDetailRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        relatedClipDetailRecyclerView.setLayoutManager(layoutManager);
        relatedClipDetailRecyclerView.addItemDecoration(new SpaceDecoration(this, R.dimen.horizontal_list_space,
                true, true));
        clipScrollView.fullScroll(View.FOCUS_UP);
        clipScrollView.smoothScrollTo(0, 0);

        RelatedVideoParser relatedVideoParser = new RelatedVideoParser();
        ViewUtils.execute(relatedVideoParser);
    }

    /*set all widgets/view values*/
    private void setWidgetsValues() {
        setTitleText();
         /*set product image */
        int imageHeight = displayResources.dpToPx((int) getResources().getDimension(R.dimen.clip_image_hieght));

        ImageURL.ImageProfile profile = ImageURL.ImageProfile.HERO_BANNER;
        if (displayHeight < displayWidth){
            profile = ImageURL.ImageProfile.BACKGROUND;
        }

        Picasso.with(this).load(ViewUtils.getImageUrlOfProduct(ClipDetailsActivity.this, product, Page.CLIP_DETAIL_PAGE, profile))
                .resize(displayWidth, imageHeight)
                .onlyScaleDown()
                .noFade()
                .placeholder(R.drawable.fallback_poster)
                .into(clipThumbImageView);

        /*set background blur image */
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                setBackgroundImage(imageViewClipBgBlur, ViewUtils.getImageUrlOfProduct(ClipDetailsActivity.this, product, Page.CLIP_DETAIL_PAGE, ImageURL.ImageProfile.BACKGROUND));
            }
        });

        if (product.getTitle() != null && !product.getTitle().equals("null")) {
            clipTitleTextView.setText(product.getTitle());
        } else {
            clipTitleTextView.setText("");
        }

        if (product.getReleased() != null && !product.getReleased().equals("null")) {
            clipTimeTextView.setText(product.getReleased());
        } else {
            clipTimeTextView.setText("");
        }

        clipTimeTextView.append(" ");
        if (product.getDuration() != null && !product.getDuration().equals("null")) {
            clipTimeTextView.setText(product.getDuration());
        }

        if (product.getDescription() != null && !product.getDescription().equals("null")) {
            clipDescTextView.setText(product.getDescription());
        } else {
            clipDescTextView.setText("");
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


    private void setRelatedClipVideos() {
        relatedClipDetailRecyclerView.setAdapter(new RelatedClipAdapter(this, relatedClipItems));

        relatedClipDetailRecyclerView.addOnItemTouchListener(new RelatedItemTouchListener(this, relatedClipDetailRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(ClipDetailsActivity.this, ClipDetailsActivity.class);
                intent.putExtra(KEY_PRODUCT, relatedClipItems.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
//                ClipDetailsActivity.this.finish();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeImageButton:
                onBackPressed();
                break;
            case R.id.clipPlayImageButton:
                break;
            default:
                break;
        }
    }
        /* load showing blur image in background */

    private void setBackgroundImage(final View view, String imageUrl) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                view.setBackground(new BitmapDrawable(getResources(), BlurBuilder.blur(ClipDetailsActivity.this, bitmap)));
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

    /*Call webservice for load related products for this product*/
    public class RelatedVideoParser extends AsyncTask<Object, String, Products> {

        private String error = null;
        private String authToken = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                authToken = PreferenceHelper.getSharedPrefData(ClipDetailsActivity.this, getResources().getString(R.string.user_info), User.class).getAccessToken();
            } catch (Exception e) {
                authToken = null;
            }
        }

        @Override
        protected Products doInBackground(Object... params) {
            ContentHandler contentHandler = ContentHandler.getInstance();
            Products products = null;
            try {
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
                if (progressBarRelated.isShown())
                    progressBarRelated.setVisibility(View.GONE);

                relatedClipItems = products.getProductList();
                setRelatedClipVideos();
                if (clipScrollView.getScrollY() == 0) {
                    clipScrollView.fullScroll(View.FOCUS_UP);
                }
            } else {
                if (error != null && error.equals(getResources().getString(R.string.un_authorised_exception_code)) && PreferenceHelper.getSharedPrefData(ClipDetailsActivity.this, getResources().getString(R.string.user_is_logged_in))) {
                    User userInfo = PreferenceHelper.getSharedPrefData(ClipDetailsActivity.this, getResources().getString(R.string.user_info), User.class);
                    String[] params = {userInfo.getProvider(), userInfo.getIdentifier(), userInfo.getPassword()};
                    AuthenticationParser authenticationParser = new AuthenticationParser(ClipDetailsActivity.this, new AuthenticationUserResultReceived() {
                        @Override
                        public void onResult(String error, User result) {
                            if (error == null && result != null) {
                                RelatedVideoParser relatedVideoParser = new RelatedVideoParser();
                                ViewUtils.execute(relatedVideoParser);
                            } else {
                                progressBarRelated.setVisibility(View.GONE);
                                Toast.makeText(ClipDetailsActivity.this, getResources().getString(R.string.response_error), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    ViewUtils.execute(authenticationParser, params);
                } else {
                    progressBarRelated.setVisibility(View.GONE);
                    Toast.makeText(ClipDetailsActivity.this, getResources().getString(R.string.response_error), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}



