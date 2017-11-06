package com.movideo.whitelabel.model;

/**
 * Created by Duy on 03/31/2016.
 */
public class VideoItem {

    private String bootAddress;

    private String companyName;

    private String src;

    private String systemBitrate;

    private String encodingProfileName;

    private int height;

    private int width;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSystemBitrate() {
        return systemBitrate;
    }

    public void setSystemBitrate(String systemBitrate) {
        this.systemBitrate = systemBitrate;
    }

    public String getEncodingProfileName() {
        return encodingProfileName;
    }

    public void setEncodingProfileName(String encodingProfileName) {
        this.encodingProfileName = encodingProfileName;
    }
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }


    public String getBootAddress() {
        return bootAddress;
    }

    public void setBootAddress(String bootAddress) {
        this.bootAddress = bootAddress;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
