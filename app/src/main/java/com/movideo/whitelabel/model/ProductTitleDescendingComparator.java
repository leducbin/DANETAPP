package com.movideo.whitelabel.model;

import android.util.Log;

import com.movideo.baracus.model.product.Product;

import java.util.Comparator;

/**
 * Compare {@link Product} by title in descending order.
 */
public class ProductTitleDescendingComparator implements Comparator<Product> {

    @Override
    public int compare(Product lhs, Product rhs) {
        try {
            if (lhs == null || lhs.getTitle() == null)
                return 1;
            if (rhs == null || rhs.getTitle() == null)
                return -1;
            return rhs.getTitle().compareTo(lhs.getTitle());
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            return -1;
        }
    }
}
