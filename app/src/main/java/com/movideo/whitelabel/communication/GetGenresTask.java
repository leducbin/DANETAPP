package com.movideo.whitelabel.communication;

import com.movideo.whitelabel.ContentHandler;

import java.util.List;

/**
 * {@link android.os.AsyncTask} to get {@link List<String>} relate to the given types.
 * Provide the play list item id as a execution method parameter.
 */
public class GetGenresTask extends GetContentRequest<Object, Object, List<String>> {

    private List<String> types;

    /**
     * Constructor with arguments.
     *
     * @param listener {@link ContentRequestListener}
     * @param types    {@link List<String>} Eg: movie, series...
     */
    public GetGenresTask(ContentRequestListener listener, List<String> types) {
        super(listener);
        this.types = types;
    }

    @Override
    List<String> run(ContentHandler contentHandler, Object... params) throws Exception {

        return contentHandler.getGenres(types);
    }
}
