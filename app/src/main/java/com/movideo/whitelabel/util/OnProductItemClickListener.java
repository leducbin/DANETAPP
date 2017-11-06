package com.movideo.whitelabel.util;


import com.movideo.baracus.model.product.Product;

/**
 * On click listener for {@link Product} lists.
 */
public interface OnProductItemClickListener {

    /**
     * Called when a product list item is clicked.
     *
     * @param product {@link Product}.
     */
    void onItemClick(Product product);
}
