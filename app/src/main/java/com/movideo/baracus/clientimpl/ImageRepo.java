package com.movideo.baracus.clientimpl;

import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import com.movideo.baracus.model.common.Image;
import com.movideo.baracus.model.common.ImageDetails;
import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.enums.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImageRepo {
    private ImageRepo(){

    }

    private static ImageRepo _instance;
    public static ImageRepo instance(){
        if (null == _instance){
            _instance = new ImageRepo();
        }
        return _instance;
    }

    //private Map<Integer, Map<String, Map<Integer, String>>> images = new HashMap<>() ;
    private Map<Integer,Map<String,String>> images = new HashMap<>();
    public static void saveAllProductImages(Product product) {
        Image image = product.getImage();
        if (null == image) {
            if(product.getPoster()!=null){
                ImageRepo.instance().setImage(product.getId(),"default",product.getPoster()+"_600x900.jpg");
                ImageRepo.instance().setImage(product.getId(),"poster",product.getPoster()+"_600x900.jpg");
                ImageRepo.instance().setImage(product.getId(),"background",product.getBackground()+"_1024x576.jpg");
            }
//            if (product.getThumbnail_path() != null) {
//                image = new Image();
//                String thumbnail_path = product.getThumbnail_path();
//                String hero_shot_path = product.getHero_shot_path();
//                image.setBaseUri(thumbnail_path);
//                List<String> profiles = new ArrayList<>();
//                profiles.add("default");
//                profiles.add("poster");
//                profiles.add("background");
//                Map<String, List<ImageDetails>> profile = new HashMap<>();
//                List<ImageDetails> imgdetails = new ArrayList<>();
//                List<ImageDetails> imgdetailsbg = new ArrayList<>();
//                imgdetails.add(new ImageDetails(210, 311, true, thumbnail_path + "_210x311.jpg"));
//                imgdetails.add(new ImageDetails(1080, 1620, true, thumbnail_path + "_1080x1620.jpg"));
//                imgdetails.add(new ImageDetails(880, 1320, true, thumbnail_path + "_880x1320.jpg"));
//                imgdetails.add(new ImageDetails(600, 900, true, thumbnail_path + "_600x900.jpg"));
//
//                imgdetailsbg.add(new ImageDetails(210, 311, true, hero_shot_path + "_210x311.jpg"));
//                imgdetailsbg.add(new ImageDetails(1080, 1620, true, hero_shot_path + "_1080x1620.jpg"));
//                imgdetailsbg.add(new ImageDetails(880, 1320, true, hero_shot_path + "_880x1320.jpg"));
//                imgdetailsbg.add(new ImageDetails(600, 900, true, hero_shot_path + "_600x900.jpg"));
//
//                for (String profile_name : profiles) {
//                    if (profile_name.equals("background"))
//                        profile.put(profile_name, imgdetailsbg);
//                    else
//                        profile.put(profile_name,imgdetails);
//                }
//                image.setProfile(profile);
//                image.setProfiles(profiles);
//                product.setImage(image);
//            }
        }
        else {
            Map<String, List<ImageDetails>> profiles = image.getProfile();
            if (null != profiles && profiles.size() > 0) {
                for (Map.Entry<String, List<ImageDetails>> profileEntry : profiles.entrySet()) {
                    List<ImageDetails> imageDetails = profileEntry.getValue();
                    if (null != imageDetails && imageDetails.size() > 0) {
                        for (ImageDetails imageDetail : imageDetails) {
                            ImageRepo.instance().setImage(product.getId(), profileEntry.getKey().toString(), imageDetail.getUrl());
                        }

                    }

                }

            }
        }
    }

    public void setImage(int productId, String profileKey, String Url){
        Map<String,String> profileMap = images.get(productId);
        if (profileMap == null) {
            profileMap = new HashMap<>();
            images.put(productId, profileMap);
        }
//        Map<Integer,String> densityMap = profileMap.get(profileKey);
//        if (densityMap == null) {
//            densityMap = new HashMap<>();
//            profileMap.put(profileKey, densityMap);
//        }
        profileMap.put(profileKey, Url);
    }

//    private Integer getDensity(int width, int height) {
//        Integer density = DisplayMetrics.DENSITY_HIGH;
//
//        if (width < 600){
//            density = DisplayMetrics.DENSITY_MEDIUM;
//        }
//        else if (width < 880){
//            density = DisplayMetrics.DENSITY_HIGH;
//        }
//        else if (width < 1080){
//            density = DisplayMetrics.DENSITY_XHIGH;
//        }
//        else if (width >= 1080)
//        {
//            density = DisplayMetrics.DENSITY_XXHIGH;
//        }
//        return density;
//    }

    public String getImage(int productId, String profileKey){
//        if (density < DisplayMetrics.DENSITY_MEDIUM)
//        {
//            density = DisplayMetrics.DENSITY_MEDIUM;
//        }
//
//        if (density > DisplayMetrics.DENSITY_LOW && density < DisplayMetrics.DENSITY_MEDIUM){
//            density = DisplayMetrics.DENSITY_MEDIUM;
//        }
//
//        if (density > DisplayMetrics.DENSITY_MEDIUM && density < DisplayMetrics.DENSITY_HIGH){
//            density = DisplayMetrics.DENSITY_HIGH;
//        }
//
//
//        if (density > DisplayMetrics.DENSITY_HIGH && density < DisplayMetrics.DENSITY_XHIGH){
//            density = DisplayMetrics.DENSITY_XHIGH;
//        }
//
//        if (density > DisplayMetrics.DENSITY_XHIGH && density < DisplayMetrics.DENSITY_XXHIGH){
//            density = DisplayMetrics.DENSITY_XXHIGH;
//        }
//
//        if (density > DisplayMetrics.DENSITY_XXHIGH);
//        {
//            density = DisplayMetrics.DENSITY_XXHIGH;
//        }

        Map<String,String> profileMap = images.get(productId);

        if (profileMap != null){
            return profileMap.get(profileKey);


        }

        return null;

    }


}
