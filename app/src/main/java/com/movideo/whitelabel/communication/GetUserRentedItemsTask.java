package com.movideo.whitelabel.communication;

import com.movideo.baracus.model.product.Product;
import com.movideo.whitelabel.ContentHandler;

import java.util.List;

/**
 * {@link android.os.AsyncTask} to get {@link List<Product>} of user rented items.
 */
public class GetUserRentedItemsTask extends GetContentRequest<Integer, Object, List<Product>> {

    /**
     * Constructor with listener.
     *
     * @param listener {@link ContentRequestListener}
     */
    public GetUserRentedItemsTask(ContentRequestListener listener) {
        super(listener);
    }

    @Override
    List<Product> run(ContentHandler contentHandler, Integer... params) throws Exception {
        return contentHandler.getRentedItemList(params[0], params[1]);
    }
}
