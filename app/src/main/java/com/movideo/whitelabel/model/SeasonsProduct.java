package com.movideo.whitelabel.model;

import com.movideo.baracus.model.product.Product;

public class SeasonsProduct {

    private String title;
    private Product product;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public com.movideo.baracus.model.product.Product getSeasonsProductSubItem() {
        return product;
    }

    public void setSeasonsProductSubItem(Product product) {
        this.product = product;
    }


}
