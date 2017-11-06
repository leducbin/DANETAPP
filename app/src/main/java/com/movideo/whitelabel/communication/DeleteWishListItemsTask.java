package com.movideo.whitelabel.communication;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.ContentHandler;

import java.util.List;

/**
 * {@link android.os.AsyncTask} to delete wish list items provided in the item id list and returns updated wish list {@link List<Product>}.
 * Provide the wish list item ids as a execution method parameter as {@link <List<String>}.
 */
public class DeleteWishListItemsTask extends GetContentRequest<List<String>, Object, List<Product>> {

    /**
     * Constructor with listener.
     *
     * @param listener {@link ContentRequestListener}
     */
    public DeleteWishListItemsTask(ContentRequestListener listener) {
        super(listener);
    }

    @Override
    List<Product> run(ContentHandler contentHandler, List<String>... params) throws Exception {
        return contentHandler.deleteWishListItems(params[0]);
    }
}
