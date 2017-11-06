package com.movideo.whitelabel.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.enums.ImageURL;
import com.movideo.whitelabel.enums.Page;
import com.movideo.whitelabel.util.OnProductItemClickListener;
import com.movideo.whitelabel.util.PicassoHelper;
import com.movideo.whitelabel.util.ViewUtils;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeImageSliderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeImageSliderFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SCREEN_WIDTH = "screen_width";
    private static final String ARG_PRODUCT = "product";

    @BindDimen(R.dimen.parallax_view_header_height)
    int height;
    @Bind(R.id.imageViewHomeImageSlider)
    ImageView sliderImage;

    private int screenWidth;
    private Product product;
    private Picasso picasso;
    private ImageDrawListener imageDrawListener;

    private OnProductItemClickListener listener;

    public HomeImageSliderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param screenWidth       Screen width.
     * @param product           {@link Product}.
     *
     * @return A new instance of fragment HomeImageSliderFragment.
     */
    public static HomeImageSliderFragment newInstance(int screenWidth, Product product) {
        HomeImageSliderFragment fragment = new HomeImageSliderFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_SCREEN_WIDTH, screenWidth);
        args.putSerializable(ARG_PRODUCT, product);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            screenWidth = getArguments().getInt(ARG_SCREEN_WIDTH);
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
        picasso = PicassoHelper.getInstance(getContext()).getPicasso();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_image_slider, container, false);
        ButterKnife.bind(this, view);

        initView();

        return view;
    }

    private void initView() {
        sliderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onItemClick(product);
            }
        });

        ImageURL.ImageProfile profile = ImageURL.ImageProfile.HERO_BANNER;
        if (height < screenWidth){
            profile = ImageURL.ImageProfile.BACKGROUND;
        }

        picasso.with(getContext()).load(ViewUtils.getImageUrlOfProduct(getContext(), product, Page.HOME_PAGE, profile))
                //.resize(screenWidth, height)
                .noFade()
                .placeholder(R.drawable.fallback_poster)
                .into(sliderImage);

        if (imageDrawListener != null)
            imageDrawListener.onAfterDraw(sliderImage);
    }

    @Override
    public void onDestroyView() {
        sliderImage = null;
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        super.onDetach();
    }

    public ImageView getSliderImage() {
        return sliderImage;
    }

    /**
     * Set listener
     * @param imageDrawListener {@link ImageDrawListener}
     */
    public void setImageDrawListener(ImageDrawListener imageDrawListener) {
        this.imageDrawListener = imageDrawListener;
    }

    /**
     * Implement to notify when first image view is created.
     */
    public interface ImageDrawListener {

        /**
         * Called when first image view is created.
         *
         * @param imageView {@link ImageView}
         */
        void onAfterDraw(ImageView imageView);
    }
}
