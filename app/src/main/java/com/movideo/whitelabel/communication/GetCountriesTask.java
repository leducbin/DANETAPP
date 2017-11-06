package com.movideo.whitelabel.communication;

import com.movideo.whitelabel.ContentHandler;

import java.util.List;

/**
 * {@link android.os.AsyncTask} to get {@link List<String>} relate to the given types.
 */
public class GetCountriesTask extends GetContentRequest<Object, Object, List<String>> {

    /**
     * Constructor with arguments.
     *
     * @param listener {@link ContentRequestListener}
     */
    public GetCountriesTask(ContentRequestListener listener) {
        super(listener);

    }

    @Override
    List<String> run(ContentHandler contentHandler, Object... params) throws Exception {
        return contentHandler.getCountries((List<String>) params[0]);
    }
}
