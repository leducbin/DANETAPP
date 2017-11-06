package com.movideo.whitelabel.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.movideo.android.activity.PlayerActivity;
import com.movideo.baracus.model.product.Offerings;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.ContentHandler;
import com.movideo.whitelabel.ExtraActivity;
import com.movideo.whitelabel.LoginActivity;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.SVODActivity;
import com.movideo.whitelabel.ShowExoPlayer;
import com.movideo.whitelabel.ShowVideoDemo;
import com.movideo.whitelabel.TVODActivity;
import com.movideo.whitelabel.WhiteLabelApplication;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;
import com.movideo.whitelabel.view.ProgressView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class PlaybackHandler {

    private static final String SITE_URL = "http://www.danet.vn/";
    private static final String TAG = "PlaybackHandler";
    private static LinkedHashMap<String,List<String>> variants;
    private static FirebaseAnalytics mFirebaseAnalytics;

    static {
        variants = new LinkedHashMap<>();
        variants.put("4K", Arrays.asList("4K", "HD", "SD"));
        variants.put("HD", Arrays.asList("HD", "SD"));
        variants.put("SD", Arrays.asList("SD"));
    }

    private static String getVariant(List<String> entitledVariants, String selectedVariant) {
        if (entitledVariants != null && !entitledVariants.isEmpty()) {
            if (selectedVariant != null) {
                if (entitledVariants.contains(selectedVariant)) {
                    return selectedVariant;
                } else {
                    for (String entitledVariant : entitledVariants) {
                        List<String> includedVariants = variants.get(entitledVariant);
                        if (includedVariants != null && includedVariants.contains(selectedVariant)) {
                            return selectedVariant;
                        }
                    }
                }
            }

            for (String variant : variants.keySet()) {
                if (entitledVariants.contains(variant)) {
                    return variant;
                }
            }

            return entitledVariants.get(0);
        }

        return null;
    }

    public static void playTrailer(Activity parentActivity, Integer productId, String selectedVariant, String trailer) {
        play(parentActivity, productId, selectedVariant, null, trailer, "trailer");
    }

    private static Activity _parentActivity;
    private static Integer _productId;
    private static String _selectedVariant;
    private static Integer _episodeId;
    private static String _trailer;
    private static Product _entitledProduct;



    public static void play(final Activity parentActivity, final Integer productId, final String selectedVariant, final Integer episodeId, final String trailer, final String episodeName) {
        _parentActivity = parentActivity;
        _productId = productId;
        _selectedVariant = selectedVariant;
        _episodeId = episodeId;
        _trailer = trailer;
        if (_entitledProduct != null && ! _entitledProduct.getId().equals(productId)){
            _entitledProduct = null;//clear cached entired product when viewing other product
        }

        new AsyncTask<Void, Void, Product>() {
            private String error = null;
            private ProgressView progressView;

            @Override
            protected void onPreExecute() {
                Log.d(TAG,"onPreExecute");
                super.onPreExecute();
                progressView = new ProgressView(parentActivity);
                progressView.show();
            }

            @Override
            protected Product doInBackground(Void... params) {
                Product product = null;
                try {
                    String accessToken = WhiteLabelApplication.getInstance().getAccessToken();
                    if(accessToken==null){
                        final User user = PreferenceHelper.getUser(parentActivity);
                        if(user!=null){
                            accessToken = user.getAccessToken();

                        }
                    }
                    product = ContentHandler.getInstance().getProductById(accessToken, Integer.toString(productId), false);
                } catch (Exception e) {
                    if(e.getMessage()!= null)
                        Log.e(this.getClass().getName(), e.getMessage());
                    error = e.getMessage();
                }
                return product;
            }

            @Override
            protected void onPostExecute(Product product) {
                try {
                    Log.e(TAG, product.toString());
                    mFirebaseAnalytics = FirebaseAnalytics.getInstance(parentActivity);
                    if (error == null && product != null) {
                        Offerings entitlmentOffering = null;
                        List<String> entitledVariants = new ArrayList<String>();

                        Offerings priorityOffering = null;

                        if (product.getOfferings() != null && getHDCreditOffering(product) != null ){
                            priorityOffering = getHDCreditOffering(product);
                        }

                        for (Offerings offering : product.getOfferings()) {
                            if (offering.getEntitled()) {
                                if (!entitledVariants.contains(offering.getVariant())) {
                                    entitledVariants.add(offering.getVariant());
                                }
                                entitlmentOffering = offering;
                                   break;
                            }
                        }
                        //after refresh the product, if still there is no entitled offering, try to check local entired product.
                        if (entitlmentOffering == null && _entitledProduct != null)
                        {
                            entitlmentOffering = getOfferingFromProductEntitled();
                        }
                        if (trailer == null && entitlmentOffering == null) {
                            final User user = PreferenceHelper.getUser(parentActivity);
                            if (user == null) {
                                loadPromptUserLoginDialog(parentActivity);
                            }
                            //user logged in
                            else {
                                //SVOD movie
                                if(priorityOffering == null) {
                                    //user not subscribe
                                    if (user.getSubscription() == null) {
                                        loadSubscribeDialog(parentActivity);
                                    }
                                }
                                // TVOD movie
                                if (priorityOffering != null){
                                    // user credits >= movie price
                                    if(user.getCredits() >= priorityOffering.getPrice()) {
                                        loadLocalRentProductDialog(parentActivity, product);
                                    } else {
                                        loadAddCreditsDialog(parentActivity, product);
                                    }
                                }
                            }
                            return;
                        }

                        Log.d("PLAYBACK", "entitled variants" + entitledVariants);
                        String variant = "HD"; //getVariant(entitledVariants, selectedVariant);
                        Log.d("PLAYBACK", "playing variant " + variant);

                        //Firebase analytics tracking
                        String package_type = product.getPackageType();
                        if(package_type == null)
                            package_type= WhiteLabelApplication.getInstance().getLicenseType().name();
                        Log.e(TAG, package_type);
                        String episode_name = episodeName;
                        if(episode_name== null){
                            episode_name=product.getTitle();
                            Log.e(TAG, "episode name null");
                        }
                        Log.e(TAG, episode_name);

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, episode_name);
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, product.getTitle());
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                        Log.e(TAG, product.toString());


                        //Start video player
                        //Intent intent = new Intent(parentActivity, ShowVideoDemo.class);
                        Intent intent;
                        Log.e(TAG, "package type avod :" +package_type.equals(LicenseType.AVOD.name()));
                        if(package_type.equals(LicenseType.AVOD.name()))
                            intent = new Intent(parentActivity, ShowExoPlayer.class);
                        else
                            intent = new Intent(parentActivity, ShowVideoDemo.class);
                        intent.putExtra(PlayerActivity.API_BASE_URL, ContentHandler.API_URL);
                        intent.putExtra(PlayerActivity.VARIANT, variant);
                        intent.putExtra(PlayerActivity.PRODUCT_ID, Integer.toString(product.getId()));
                        if (episodeId != null)
                            intent.putExtra("EPISODE_ID", Integer.toString(episodeId));
                        if (trailer != null)
                        {
                            intent.putExtra("TRAILER", trailer);
                        }
                        parentActivity.startActivity(intent);

                    } else {
                        if (error != null && error.equals(parentActivity.getResources().getString(R.string.un_authorised_exception_code))) {
                            Toast.makeText(parentActivity, error, Toast.LENGTH_LONG).show();
                        }
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

    public static void play(final Activity parentActivity, final int productId) {
        play(parentActivity, productId, null);
    }

    public static void play(final Activity parentActivity, final int productId, final String selectedVariant) {
        play(parentActivity, productId, selectedVariant, null, null,null);
    }

    private static void loadAddCreditsDialog(final Activity activity, final Product product) {

        AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(activity, "Không đủ điểm", "Bạn còn thiếu "+ (getHDCreditOffering(product).getPrice() - PreferenceHelper.getUser(activity).getCredits()) +" điểm để thuê phim này. Bạn muốn nạp điểm thêm không?", "Nạp điểm", "Để sau", new DialogEventListeners() {
            @Override
            public void onPositiveButtonClick(DialogInterface dialog) {
                AddTwoButtonDialogView creditSelectDialog = new AddTwoButtonDialogView(activity, "Nạp điểm DANET", null, "Thẻ cào ĐT", "Bằng tiền", new DialogEventListeners() {
                    @Override
                    public void onPositiveButtonClick(DialogInterface dialog) {
                        if (NetworkAvailability.chkStatus(activity)) {
                            Intent intent = new Intent(activity, ExtraActivity.class);
                            intent.putExtra("url", "https://payment.danet.vn/prepaidcard/?identifier=" + PreferenceHelper.getUser(activity).getIdentifier() + "&accessToken=" + WhiteLabelApplication.getInstance().getAccessToken());
                            activity.startActivity(intent);

                        } else {
                            Toast.makeText(activity, activity.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegativeButtonClick(DialogInterface dialog) {
                        if (NetworkAvailability.chkStatus(activity)) {
                            Intent intent = new Intent(activity, TVODActivity.class);
                            activity.startActivity(intent);
                        } else {
                            Toast.makeText(activity, activity.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        }
                    }

                });
                creditSelectDialog.show();
            }

            @Override
            public void onNegativeButtonClick(DialogInterface dialog) {
            }
        });
        dialogView.show();
    }

    private static void loadSubscribeDialog(final Activity activity) {

        AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(activity, "Chưa đăng ký phim gói", "Bạn cần đăng ký dịch vụ phim gói của DANET để xem được phim này. Bạn muốn đăng ký ngay?", "Đăng ký", "Để sau", new DialogEventListeners() {
            @Override
            public void onPositiveButtonClick(DialogInterface dialog) {
                AddTwoButtonDialogView subscriptionSelectDialog = new AddTwoButtonDialogView(activity, "Đăng ký phim gói", null, "Bằng tiền", "Bằng điểm", new DialogEventListeners() {
                    @Override
                    public void onPositiveButtonClick(DialogInterface dialog) {
                        if (NetworkAvailability.chkStatus(activity)) {
                            Intent intent = new Intent(activity, SVODActivity.class);
                            intent.putExtra("type", "1");
                            activity.startActivity(intent);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(activity, activity.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onNegativeButtonClick(DialogInterface dialog) {
                        Intent intent = new Intent(activity, SVODActivity.class);
                        intent.putExtra("type", "2");
                        activity.startActivity(intent);
                    }

                });
                subscriptionSelectDialog.show();

//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(SITE_URL + "go?action=choose-payment&productid=" + product.getId()));
//                activity.startActivity(intent);
                dialog.dismiss();
            }

            @Override
            public void onNegativeButtonClick(DialogInterface dialog) {
            }
        });
        dialogView.show();
    }

    public static void setProductEntitled(Product product){
        if (product != null){
            getHDCreditOffering(product).setEntitled(true);
        }
    }

    public static Offerings getHDCreditOffering(Product product){
        Offerings HD = null;
        for (Offerings offering : product.getOfferings() ){
            if (offering.getType().equals("TVOD") && offering.getVariant().equals("HD") && offering.getPrice() < 1000){
                HD = offering;
                break;
            }
        }

        return HD;
    }
    public static Offerings getOfferingFromProductEntitled(){
        if (_entitledProduct != null){
            Offerings offerings = getHDCreditOffering(_entitledProduct);
             if (offerings.getEntitled() == true )
                 return offerings;
        }
        return null;
    }

    private static void loadLocalRentProductDialog(final Activity activity, final Product product) {

        final double price = getHDCreditOffering(product).getPrice();
        AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(activity, activity.getString(R.string.dialog_msg_local_rent), "Quý khách cần có " + price + " điểm để xem được phim này", activity.getString(R.string.label_accept_credit_pay), activity.getString(R.string.label_cancel), new DialogEventListeners() {
            @Override
            public void onPositiveButtonClick(DialogInterface dialog) {
                new AsyncTask<Void, Void, Product>() {
                    private String error = null;
                    private ProgressView progressView;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressView = new ProgressView(activity);
                        progressView.show();
                    }

                    @Override
                    protected Product doInBackground(Void... params) {
                        try {
                            ContentHandler.getInstance().purchaseProductWithCredit(product);
                        } catch (Exception e) {
                            Log.e(this.getClass().getName(), e.getMessage());
                            error = e.getMessage();
                        }
                        return product;
                    }

                    @Override
                    protected void onPostExecute(Product product) {
                        try {
                            if (error == null && product != null) {
                                //Toast.makeText(activity, "Quý khách vừa thuê thành công phim: " + product.getTitle(), Toast.LENGTH_LONG).show();
                                User user = PreferenceHelper.getUser(activity);
                                Double oldCredits =  user.getCredits();
                                user.setCredits(oldCredits - price);
                                //successed, store this product as local entitled (just in case server is not uptodated)
                                _entitledProduct = product;
                                setProductEntitled(_entitledProduct);
                                //suceed play again.
                                play(_parentActivity, _productId, _selectedVariant, _episodeId, _trailer, null);

                            } else {
                                if (error != null && error.equals(activity.getResources().getString(R.string.un_authorised_exception_code))) {
                                    Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                                }
                            }
                        } catch (Exception e){
                            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
                        } finally {
                            if (progressView!=null)
                                progressView.dismiss();
                        }
                    }
                }.execute();

                dialog.dismiss();
            }

            @Override
            public void onNegativeButtonClick(DialogInterface dialog) {
            }
        });
        dialogView.show();
    }

    private static void loadPromptUserLoginDialog(final Activity activity) {

        AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(activity, activity.getString(R.string.dialog_msg_prompt_login), null, activity.getString(R.string.label_sign_in), activity.getString(R.string.label_cancel), new DialogEventListeners() {
            @Override
            public void onPositiveButtonClick(DialogInterface dialog) {

                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
                dialog.dismiss();
            }

            @Override
            public void onNegativeButtonClick(DialogInterface dialog) {
            }
        });
        dialogView.show();
    }



}
