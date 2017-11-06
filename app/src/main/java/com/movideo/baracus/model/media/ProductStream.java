package com.movideo.baracus.model.media;

import java.io.Serializable;

public class ProductStream implements Serializable {
    VariantStream hd;
    VariantStream sd;
    private String productId;
    private String episodeId;


    public ProductStream() {

    }

    public VariantStream getHd() {
        return hd;
    }

    public void setHd(VariantStream hd) {
        this.hd = hd;
    }

    public VariantStream getSd() {
        return sd;
    }

    public void setSd(VariantStream sd) {
        this.sd = sd;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
    }

    public String getEpisodeId() {
        return episodeId;
    }
}
