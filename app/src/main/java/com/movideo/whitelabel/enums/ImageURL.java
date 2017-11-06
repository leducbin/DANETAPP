package com.movideo.whitelabel.enums;

import android.util.DisplayMetrics;

/**
 * Holds the image url for different images.
 */
public enum ImageURL {

    HOME_PHONE_POSTER(ImageProfile.POSTER, "113x169", "165x248", "200x300", "210x315", "300x450", "440x660", "600x900", ImageType.JPG),
    HOME_PHONE_HERO_BANNER(ImageProfile.HERO_BANNER, "270x405", "380x570", "479x719", "540x810", "760x1140", "1080x1620", "1080x1620", ImageType.JPG),
    HOME_PHONE_CLIP(ImageProfile.CLIP, "188x108", "279x161", "333x192", "375x216", "500x288", "750x432", "1116x644", ImageType.JPG),

    HOME_TABLET_POSTER(ImageProfile.POSTER, "165x248", "210x315", "293x439", "380x570", "440x660", "600x900", "880x1320", ImageType.JPG),
    HOME_TABLET_HERO_BANNER(ImageProfile.HERO_BANNER_LANDSCAPE, "960x540", "960x540", "1704x959", "1920x1080", "2560x1440", "2560x1440", "2560x1440", ImageType.JPG),
    HOME_TABLET_CLIP(ImageProfile.CLIP, "279x161", "375x216", "500x288", "558x322", "750x432", "1116x644", "1488x858", ImageType.JPG),

    MY_LIBRARY_PHONE_POSTER(ImageProfile.POSTER, "79x118", "113x169", "140x210", "165x248", "210x315", "300x450", "440x660", ImageType.JPG),
    MY_LIBRARY_TABLET_POSTER(ImageProfile.POSTER, "143x214", "190x285", "250x375", "293x439", "380x570", "570x855", "760x1140", ImageType.JPG),

    SEARCH_POSTER(ImageProfile.POSTER, "79x118", "79x118", "79x118", "79x118", "113x169", "165x248", "210x315", ImageType.JPG),
    SEARCH_CLIP(ImageProfile.BACKGROUND, "279x161", "279x161", "279x161", "279x161", "279x161", "279x161", "375x216", ImageType.JPG),

    MOVIE_TRAILER_PHONE_POSTER(ImageProfile.POSTER, "79x118", "113x169", "140x210", "165x248", "210x315", "300x450", "440x660", ImageType.JPG),
    MOVIE_TRAILER_TABLET_POSTER(ImageProfile.POSTER, "143x214", "190x285", "250x375", "293x439", "380x570", "570x855", "760x1140", ImageType.JPG),

    MOVIE_TRAILER_PHONE_CLIP(ImageProfile.BACKGROUND, "279x161", "279x161", "279x161", "279x161", "279x161", "279x161", "375x216", ImageType.JPG),
    MOVIE_TRAILER_TABLET_CLIP(ImageProfile.BACKGROUND, "279x161", "279x161", "279x161", "279x161", "279x161", "279x161", "375x216", ImageType.JPG);

    private ImageProfile profile;
    private String ldpi;
    private String mdpi;
    private String tvdpi;
    private String hdpi;
    private String xhdpi;
    private String xxhdpi;
    private String xxxhdpi;
    private ImageType imageType;

    /**
     * Constructor with arguments.
     *
     * @param profile
     * @param ldpi
     * @param mdpi
     * @param tvdpi
     * @param hdpi
     * @param xhdpi
     * @param xxhdpi
     * @param xxxhdpi
     * @param imageType
     */
    ImageURL(ImageProfile profile, String ldpi, String mdpi, String tvdpi, String hdpi, String xhdpi, String xxhdpi, String xxxhdpi, ImageType imageType) {
        this.profile = profile;
        this.ldpi = ldpi;
        this.mdpi = mdpi;
        this.tvdpi = tvdpi;
        this.hdpi = hdpi;
        this.xhdpi = xhdpi;
        this.xxhdpi = xxhdpi;
        this.xxxhdpi = xxxhdpi;
        this.imageType = imageType;
    }

    /**
     * Returns the complete image url for the enum.
     *
     * @param baseUrl Base url ending with '/'.
     * @param density {@link DisplayMetrics#densityDpi}
     * @return complete url.
     */
    public String getImageUrl(String baseUrl, int density) {

        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                return baseUrl + profile.getValue() + ldpi + "." + imageType.getValue();
            case DisplayMetrics.DENSITY_MEDIUM:
                return baseUrl + profile.getValue() + mdpi + "." + imageType.getValue();
            case DisplayMetrics.DENSITY_TV:
                return baseUrl + profile.getValue() + tvdpi + "." + imageType.getValue();
            case DisplayMetrics.DENSITY_HIGH:
                return baseUrl + profile.getValue() + hdpi + "." + imageType.getValue();
            case DisplayMetrics.DENSITY_XHIGH:
                return baseUrl + profile.getValue() + xhdpi + "." + imageType.getValue();
            case DisplayMetrics.DENSITY_XXHIGH:
                return baseUrl + profile.getValue() + xxhdpi + "." + imageType.getValue();
            case DisplayMetrics.DENSITY_XXXHIGH:
                return baseUrl + profile.getValue() + xxxhdpi + "." + imageType.getValue();
        }
        return baseUrl;
    }

    /**
     * Holds image type as in JPG or PNG
     */
    public enum ImageType {

        JPG("jpg"),
        PNG("png");

        private String value;

        ImageType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Holds image profile.
     */
    public enum ImageProfile {

        POSTER("poster/"),
        CLIP(""),
        HERO_BANNER("poster/"),
        HERO_BANNER_LANDSCAPE("background/"),
        BACKGROUND("background/");

        private String value;

        ImageProfile(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

