package com.movideo.whitelabel.model;

import android.util.Log;

import com.movideo.baracus.model.product.Product;

import java.util.Comparator;

/**
 * Compare {@link Product} by episode number in ascending order
 */
public class ProductEpisodeAscendingComparator implements Comparator<Product> {

    @Override
    public int compare(Product lhs, Product rhs) {
        try {
            if (lhs == null || lhs.getEpisode() == null || !isNumeric(lhs.getEpisode()))
                return -1;
            if (rhs == null || rhs.getEpisode() == null || !isNumeric(rhs.getEpisode()))
                return 1;
            return ((Integer) Integer.parseInt(lhs.getEpisode())).compareTo(Integer.parseInt(rhs.getEpisode()));
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            return -1;
        }
    }

    private boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

}
