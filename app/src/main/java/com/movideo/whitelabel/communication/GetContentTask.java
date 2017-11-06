package com.movideo.whitelabel.communication;

import android.util.Log;

import com.movideo.baracus.model.metadata.Content;
import com.movideo.whitelabel.ContentHandler;

import java.util.List;

/**
 * {@link android.os.AsyncTask} to get {@link Content} relate to the given license type.
 * Provide the play list item id as a execution method parameter.
 */
public class GetContentTask extends GetContentRequest<String, Object, List<Content>> {

    /**
     * Constructor with listener.
     *
     * @param listener {@link ContentRequestListener}
     */
    public GetContentTask(ContentRequestListener<List<Content>> listener) {
        super(listener);
    }

    @Override
    List<Content> run(ContentHandler contentHandler, String... params) throws Exception {
        return contentHandler.getContentRails(params[0]);
    }
}
