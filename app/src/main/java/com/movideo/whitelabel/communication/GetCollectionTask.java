package com.movideo.whitelabel.communication;

import com.movideo.baracus.model.collection.Collection;
import com.movideo.whitelabel.ContentHandler;

/**
 * {@link android.os.AsyncTask} to get {@link Collection} relate to the given collection Id.
 * Provide the play list item id as a execution method parameter.
 */
public class GetCollectionTask extends GetContentRequest<String, Object, Collection> {

    /**
     * Constructor with listener.
     *
     * @param listener {@link ContentRequestListener}
     */
    public GetCollectionTask(ContentRequestListener listener) {
        super(listener);
    }

    @Override
    Collection run(ContentHandler contentHandler, String... params) throws Exception {
        return contentHandler.getCollectionById(params[0]);
    }
}
