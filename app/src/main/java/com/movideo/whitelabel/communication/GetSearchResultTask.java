package com.movideo.whitelabel.communication;

import com.movideo.baracus.model.product.Products;
import com.movideo.whitelabel.ContentHandler;

import java.util.List;

/**
 * {@link android.os.AsyncTask} to get search result.
 */
public class GetSearchResultTask extends GetContentRequest<Object, Object, Products> {

    /**
     * Constructor with listener.
     *
     * @param listener {@link ContentRequestListener}
     */
    public GetSearchResultTask(ContentRequestListener listener) {
        super(listener);
    }

    @Override
    Products run(ContentHandler contentHandler, Object... params) throws Exception {

        return contentHandler.getSearchResult((String) params[0], (List<String>) params[1], (List<String>) params[2], (List<String>) params[3], (List<String>) params[4], (Integer) params[5], (Integer) params[6], (Integer) params[7], (Integer) params[8]);
    }
}
